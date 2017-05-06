package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.*;
import com.google.common.base.Preconditions;

public class Expectiminimaxer {
	/*public Action getAction(GameState3 state, Card inHand, Card justDrawn) {
		List<ActionScorePair> actionScorePairs = validActions(state, inHand, justDrawn).parallel()
				.map(a -> createActionScorePair(state, a))
				.collect(Collectors.toList());
		
		Collections.shuffle(actionScorePairs);
		Collections.sort(actionScorePairs, (x, y) -> Double.compare(y.score, x.score));
		
		return actionScorePairs.get(0).action;
	}
	
	private ActionScorePair createActionScorePair(GameState3 state, Action a) {
		GameState nextBoard = BoardMoveImpl.makeMove(board, m);
		double score = alphabeta(nextBoard, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		return new MoveScorePair(score, m, heuristic(nextBoard));
	}
	
	private static class ActionScorePair {
		double score;
		Action action;

		public ActionScorePair(double score, Action action) {
			this.score = score;
			this.action = action;
		}
		
		public String toString() {
			return String.format("%s: %.3f)", action, score);
		}
	}*/
	
	public static double score(FullGameState3 state, int starting_depth) {
		//int starting_depth = 2;
		double score = expectimax(state, state.whoseTurn(), starting_depth,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		//System.out.println(statesConsidered);
		return score * (state.whoseTurn() == Player.ONE ? 1 : -1);
	}
	

	private static volatile long statesConsidered;
	//public static Map<Integer, Integer> depthMap = new HashMap<Integer, Integer>();
	public static int firstDepth = -1;
	
	private static double negamax(FullGameState3 state, Player player, int depth, double alpha, double beta) {
		Preconditions.checkArgument(player == state.whoseTurn());
		Preconditions.checkArgument(state.hasJustDrawn());
		
		statesConsidered++;
		
		if (state.winner().isPresent()) {
			return state.winner().map(player::equals).map(b -> b ? 100 : -100).get();
		}
		if (depth == 0) {
			//if (depthMap.containsKey())
			if (firstDepth == -1) {
				firstDepth = state.turnNumber();
				//System.out.println(state.deckSize());
			}
			return heuristic(state, player);
		}
		//if (!state.hasJustDrawn()) {
		//	return expectimax(state, player, depth);
		//}
		List<FullGameState3> possibleNexts = possibleNextStates(state);
		//Collections.sort(possibleNexts, Comparator.comparingDouble(s -> heuristic(s, player)));
		/*if (Math.random() < 1e-3) {
			System.out.println(possibleNexts.size());
		}*/
		double bestValue = Double.NEGATIVE_INFINITY;
		for (FullGameState3 s : possibleNexts) {
			//System.out.println("About to call expectimax");
			double v = -expectimax(s, player.other(), depth-1, -beta, -alpha);
			bestValue = Math.max(bestValue, v);
			//alpha = Math.max(alpha, v);
			/*if (alpha > beta) {
				break;
			}*/
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
	
	/*private static Stream<Action> validActions(GameState3 state, Card inHand, Card drawnCard) {
		return Stream.of(inHand, drawnCard).flatMap(c1 ->
			Arrays.stream(Player.values()).flatMap(p ->
				Arrays.stream(Card.values()).map(t -> new Action(state.whoseTurn(), c1, Optional.of(p), Optional.of(t)))))
		.map(Action::normalize)
		.distinct()
		.filter(a -> state.isValid(a, inHand, drawnCard));
	}*/
	
	private static Stream<Action> validActions(GameState3 state, Card inHand, Card drawnCard) {
		return Stream.of(inHand, drawnCard).map(c1 -> {
				Player targetPlayer = state.state(state.whoseTurn().other()).isProtected() ? state.whoseTurn() : state.whoseTurn().other();
				Card targetCard = Card.PRINCESS;
				return new Action(state.whoseTurn(), c1, Optional.of(targetPlayer), Optional.of(targetCard));
			})
			//Arrays.stream(Player.values()).flatMap(p ->
			//	Arrays.stream(Card.values()).map(t -> new Action(state.whoseTurn(), c1, Optional.of(p), Optional.of(t)))))
		.map(Action::normalize)
		.distinct()
		.filter(a -> state.isValid(a, inHand, drawnCard));
	}
	
	private static double heuristic(FullGameState3 state, Player player) {
		return (state.hand(player).value - state.hand(player.other()).value)*10;
	}

}
