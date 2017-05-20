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

public class Expectiminimaxer /*implements ThinkingPlayer*/ {
	private final int depth;
	
	public Expectiminimaxer(int depth) {
		this.depth = depth;
	}
	
	public Action chooseAction(Player us, FullGameState publicGameState, Card inHand, Card justDrawn) {
		return getAction(publicGameState, inHand, justDrawn);
	}

	public Action getAction(FullGameState state, Card inHand, Card justDrawn) {
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
	
	private ActionScorePair createActionScorePair(FullGameState state, Action a) {
		FullGameState nextState = state.endTurn(a);
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
	
	public static double score(FullGameState state, int starting_depth) {
		//double score = expectimax(state, state.whoseTurn(), starting_depth,
		double score = cachingExpectimax(state, starting_depth,
						Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		return score /* (state.whoseTurn() == Player.ONE ? 1 : -1)*/;
	}
	

	private static volatile long statesConsidered;
	//public static int firstDepth = -1;
	
	private static double negamax(FullGameState state, Player player, int depth, double alpha, double beta) {
		Preconditions.checkArgument(player == state.whoseTurn());
		Preconditions.checkArgument(state.hasJustDrawn());
		
		statesConsidered++;
		
		if (state.winner().isPresent()) {
			return state.winner().map(player::equals).map(b -> b ? +100 : -100).get();
		}
		if (depth == 0) {
			return heuristic(state, player);
		}
		List<FullGameState> possibleNexts = possibleNextStates(state);
		Collections.sort(possibleNexts, Comparator.comparingDouble(s -> heuristic(s, player)));
		double bestValue = Double.NEGATIVE_INFINITY;
		for (FullGameState s : possibleNexts) {
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
	
	private static double expectimax(FullGameState state, Player player, int depth, double alpha, double beta) {
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		Stream<Multiset.Entry<Card>> s = state.deck().entrySet().stream();
		if (depth > 6) {
			s = s.parallel();
		}
		return s.mapToDouble(e -> e.getCount() * negamax(state.startTurn(e.getElement()), player, depth, alpha, beta))
				.sum() * 1.0 / state.deck().size();
	}
	
	private static CacheLoader<CacheHelperKey, Double> loader = new CacheLoader<CacheHelperKey, Double>() {
	     public Double load(CacheHelperKey helper) {
	       return expectimax(helper.state, helper.state.whoseTurn(), helper.depth,
	    		   helper.alphaBetaBounds.lowerEndpoint(), helper.alphaBetaBounds.upperEndpoint());
	     }
	   };
	   
	private static LoadingCache<CacheHelperKey, Double> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			//.maximumWeight((long) 1e9)
			//.weigher((CacheHelperKey k, Double v) -> (int) Math.pow(10, 10-k.depth))
			.build(loader);
	
	private static double cachingExpectimax(FullGameState state, int depth, double alpha, double beta) {
		return expectimax(state, state.whoseTurn(), depth, alpha, beta);
		//return cache.getUnchecked(new CacheHelperKey(state, depth, alpha, beta));
	}
	
	static class CacheHelperKey {
		final FullGameState state;
		final int depth;
		final Range<Double> alphaBetaBounds;
		
		public CacheHelperKey(FullGameState state, int depth, double alpha, double beta) {
			this.state = state;
			this.depth = depth;
			this.alphaBetaBounds = Range.closed(alpha, beta);
		}

		@Override
		public int hashCode() {
			return Objects.hash(state, depth, alphaBetaBounds);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CacheHelperKey other = (CacheHelperKey) obj;
			return Objects.equals(this.alphaBetaBounds, other.alphaBetaBounds)
					&& Objects.equals(this.state, other.state) && this.depth == other.depth;
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
	
	private static List<FullGameState> possibleNextStates(FullGameState state) {
		return validActions(state, state.hand(state.whoseTurn()), state.drawnCard().get())
				.map(a -> state.endTurn(a)).collect(Collectors.toList());
	}
	
	public static Stream<Action> validActions(FullGameState state) {
		return validActions(state.getPublicState(), state.hand(state.whoseTurn()), state.drawnCard().get());
	}
	
	public static Stream<Action> validActions(GameState state, Card inHand, Card drawnCard) {
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
	
	private static List<Card> topPicksForGuard(GameState state) {
		Multiset<Card> deck = state.remainingCards();
		List<Integer> distinctCounts = deck.entrySet().stream()
				.filter(e -> e.getElement() != Card.GUARD)
				.map(Multiset.Entry<Card>::getCount).distinct().collect(Collectors.toList());
		
		if (distinctCounts.isEmpty()) {
			return ImmutableList.of(Card.PRINCESS);
		}
		
		List<Card> bestChoices = new ArrayList<Card>();
		for (int count : distinctCounts) {
			Card topCardWithCount = deck.entrySet().stream()
					.filter(e -> e.getCount() == count)
					.map(Multiset.Entry<Card>::getElement)
					.max(Comparator.reverseOrder())
					.get();
			bestChoices.add(topCardWithCount);
		}
		
		return Collections.unmodifiableList(bestChoices)
				//.subList(0, 1)
				;
	}
	
	private static double heuristic(FullGameState state, Player player) {
		return (state.hand(player).value - state.hand(player.other()).value)*10;
	}

}
