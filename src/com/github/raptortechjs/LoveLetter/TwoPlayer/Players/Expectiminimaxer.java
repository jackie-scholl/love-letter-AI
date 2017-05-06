package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.*;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Expectiminimaxer implements ThinkingPlayer {
	private final int depth;
	
	public Expectiminimaxer(int depth) {
		this.depth = depth;
	}
	
	@Override
	public void accept(Action action, GameState3 oldState, GameState3 newState) {}
	
	public Action chooseAction(Player us, FullGameState3 publicGameState, Card inHand, Card justDrawn) {
		return getAction(publicGameState, inHand, justDrawn);
	}

	public Action getAction(FullGameState3 state, Card inHand, Card justDrawn) {
		long start = System.currentTimeMillis();
		System.out.println(validActions(state, inHand, justDrawn).collect(Collectors.toList()));
		List<ActionScorePair> actionScorePairs = validActions(state, inHand, justDrawn).parallel()
				.map(a -> createActionScorePair(state, a))
				.collect(Collectors.toList());
		
		Collections.shuffle(actionScorePairs);
		Collections.sort(actionScorePairs, (x, y) -> Double.compare(y.score, x.score));
		
		long end = System.currentTimeMillis();
		double diff = (end-start)/1000.0;
		System.out.println(actionScorePairs);
		System.out.printf("AI took %.3f seconds; score: %.3f; result: %s%n", diff, actionScorePairs.get(0).score, actionScorePairs.get(0).action);
		
		return actionScorePairs.get(0).action;
	}
	
	private ActionScorePair createActionScorePair(FullGameState3 state, Action a) {
		FullGameState3 nextState = state.endTurn(a);
		//double score = expectimax(nextState, nextState.whoseTurn(), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		double score = -score(nextState, depth);
		return new ActionScorePair(score, a);
	}
	
	private static class ActionScorePair {
		double score;
		Action action;

		public ActionScorePair(double score, Action action) {
			this.score = score;
			this.action = action;
		}
		
		public String toString() {
			return String.format("(%s: %.3f)", action, score);
		}
	}
	
	public static double score(FullGameState3 state, int starting_depth) {
		//double score = expectimax(state, state.whoseTurn(), starting_depth,
		double score = cachingExpectimax(state, starting_depth,
						Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		return score /* (state.whoseTurn() == Player.ONE ? 1 : -1)*/;
	}
	

	private static volatile long statesConsidered;
	//public static int firstDepth = -1;
	
	private static double negamax(FullGameState3 state, Player player, int depth, double alpha, double beta) {
		Preconditions.checkArgument(player == state.whoseTurn());
		Preconditions.checkArgument(state.hasJustDrawn());
		
		statesConsidered++;
		
		if (state.winner().isPresent()) {
			return state.winner().map(player::equals).map(b -> b ? +100 : -100).get();
		}
		if (depth == 0) {
			return heuristic(state, player);
		}
		List<FullGameState3> possibleNexts = possibleNextStates(state);
		Collections.sort(possibleNexts, Comparator.comparingDouble(s -> heuristic(s, player)));
		double bestValue = Double.NEGATIVE_INFINITY;
		for (FullGameState3 s : possibleNexts) {
			//double v = -expectimax(s, player.other(), depth-1, -beta, -alpha);
			double v = -cachingExpectimax(s, depth-1, -beta, -alpha);
			bestValue = Math.max(bestValue, v);
			alpha = Math.max(alpha, v);
			if (alpha > beta) {
				break;
			}
		}
		
		return bestValue;
	}
	
	private static double expectimax(FullGameState3 state, Player player, int depth, double alpha, double beta) {
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		Stream<Multiset.Entry<Card>> s = state.deck().entrySet().stream();
		if (depth > 6) {
			s = s.parallel();
		}
		return s.mapToDouble(e -> e.getCount() * negamax(state.startTurn(e.getElement()), player, depth, alpha, beta))
				.sum() * 1.0 / state.deck().size();
	}
	
	private static CacheLoader<CacheHelper, Double> loader = new CacheLoader<CacheHelper, Double>() {
	     public Double load(CacheHelper helper) {
	       return expectimax(helper.state, helper.state.whoseTurn(), helper.depth,
	    		   helper.alphaBetaBounds.lowerEndpoint(), helper.alphaBetaBounds.upperEndpoint());
	    	 //return createExpensiveGraph(key);
	     }
	   };
	private static LoadingCache<CacheHelper, Double> cache = CacheBuilder.newBuilder()
		       .maximumSize(1000)
		       .build(loader);
	
	private static double cachingExpectimax(FullGameState3 state, int depth, double alpha, double beta) {
		try {
			return cache.get(new CacheHelper(state, depth, alpha, beta));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	static class CacheHelper {
		final FullGameState3 state;
		final int depth;
		final Range<Double> alphaBetaBounds;
		
		public CacheHelper(FullGameState3 state, int depth, double alpha, double beta) {
			this.state = state;
			this.depth = depth;
			this.alphaBetaBounds = Range.closed(alpha, beta);
		}

		@Override
		public int hashCode() {
			return Objects.hash(state, depth, alphaBetaBounds);
			/*final int prime = 31;
			int result = 1;
			result = prime * result + ((alphaBetaBounds == null) ? 0 : alphaBetaBounds.hashCode());
			result = prime * result + depth;
			result = prime * result + ((state == null) ? 0 : state.hashCode());
			return result;*/
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CacheHelper other = (CacheHelper) obj;
			return Objects.equals(this.alphaBetaBounds, other.alphaBetaBounds)
					&& Objects.equals(this.state, other.state) && this.depth == other.depth;
			/*if (alphaBetaBounds == null) {
				if (other.alphaBetaBounds != null) return false;
			} else if (!alphaBetaBounds.equals(other.alphaBetaBounds)) return false;
			if (depth != other.depth) return false;
			if (state == null) {
				if (other.state != null) return false;
			} else if (!state.equals(other.state)) return false;
			return true;*/
		}
	}
	
	public static <T> Map<T, Double> multisetToNormalizedFrequencyMap(Multiset<T> multiset) {
		ImmutableMap.Builder<T, Double> frequencyMap = ImmutableMap.<T, Double>builder();
		for (Multiset.Entry<T> e : multiset.entrySet()) {
			double frequency = e.getCount() * 1.0 / multiset.size();
			frequencyMap.put(e.getElement(), frequency);
		}
		return frequencyMap.build();
	}
	
	private static List<FullGameState3> possibleNextStates(FullGameState3 state) {
		return validActions(state, state.hand(state.whoseTurn()), state.drawnCard().get())
				.map(a -> state.endTurn(a)).collect(Collectors.toList());
	}
	
	private static Stream<Action> validActions(FullGameState3 state) {
		return validActions(state.getPublicState(), state.hand(state.whoseTurn()), state.drawnCard().get());
	}
	
	private static Stream<Action> validActions(GameState3 state, Card inHand, Card drawnCard) {
		/*return Stream.of(inHand, drawnCard).map(c1 -> {
				Player targetPlayer = state.state(state.whoseTurn().other()).isProtected() ? state.whoseTurn() : state.whoseTurn().other();
				Card targetCard = Card.PRINCESS;
				return new Action(state.whoseTurn(), c1, Optional.of(targetPlayer), Optional.of(targetCard));
			})*/
		List<Card> topPicksForGuard = topPicksForGuard(state);
		Player targetPlayer = state.playerState(state.whoseTurn().other()).isProtected() ?
				state.whoseTurn() : state.whoseTurn().other();
		return Stream.of(inHand, drawnCard).flatMap(c1 -> topPicksForGuard.stream().map(targetCard -> {
				return new Action(state.whoseTurn(), c1, Optional.of(targetPlayer), Optional.of(targetCard));
			}))
		.map(Action::normalize)
		.distinct()
		.filter(a -> state.isValid(a, inHand, drawnCard));
	}
	
	private static List<Card> topPicksForGuard(GameState3 state) {
		//return state.remainingCards().asList();
		Multiset<Card> deck = state.remainingCards();
		List<Integer> distinctCounts = deck.entrySet().stream()
				.map(Multiset.Entry<Card>::getCount).distinct().collect(Collectors.toList());
		
		List<Card> bestChoices = new ArrayList<Card>();
		for (int count : distinctCounts) {
			Card topCardWithCount = deck.entrySet().stream()
					.filter(e -> e.getCount() == count)
					.map(Multiset.Entry<Card>::getElement)
					.max(Comparator.reverseOrder())
					.get();
			bestChoices.add(topCardWithCount);
		}
		return Collections.unmodifiableList(bestChoices);
	}
	
	private static double heuristic(FullGameState3 state, Player player) {
		return (state.hand(player).value - state.hand(player.other()).value)*10;
	}

}
