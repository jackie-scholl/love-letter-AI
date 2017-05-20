package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MonteCarloTreeSearch implements GameObserver {
	//private GameState3 initialGameState = null;
	final List<Action> actionHistory = new ArrayList<>();

	public void accept(Action action, GameState oldState, GameState newState) {
		/*if (initialGameState == null) {
			initialGameState = oldState;
		}*/
		actionHistory.add(action);
	}
	
	/*public Optional<Map.Entry<FullGameState3, Double>> sampleStates() {
		return sampleStates(initialGameState.visibleDiscard(), ImmutableList.copyOf(actionHistory));
	}*/
	
	public static Optional<Map.Entry<FullGameState, Double>> sampleStates(ImmutableList<Card> visibleDiscard,
			ImmutableList<Action> actionHistory) {
		FullGameState state = FullGameState.createNewGame(Optional.of(visibleDiscard));
		
		return sampleStates(state, actionHistory);
	}
	
	private static Optional<Map.Entry<FullGameState, Double>> sampleStates(FullGameState state, List<Action> requiredFuture) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkNotNull(requiredFuture);
		
		if (requiredFuture.size() == 0) {
			return Optional.of(new AbstractMap.SimpleImmutableEntry<> (state, 1.0));
		}
		
		state = state.startTurn();
		
		Action requiredNextAction = requiredFuture.get(0).normalize();
		
		Set<Action> validActions = validActions(state).collect(Collectors.toSet());
		
		if (!validActions.contains(requiredNextAction)) {
			return Optional.empty();
		}
		
		double probability = probabilityOfChoosing(requiredNextAction, validActions, state.whoseTurn());
		
		return sampleStates(state.endTurn(requiredNextAction), requiredFuture.subList(1, requiredFuture.size()))
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue() * probability));
	}
	
	private static Stream<Action> validActions(FullGameState state) {
		return validActions(state.getPublicState(), state.hand(state.whoseTurn()), state.drawnCard().get());
	}
	
	private static Stream<Action> validActions(GameState state, Card inHand, Card drawnCard) {
		return Stream.of(inHand, drawnCard).flatMap(card ->
				Arrays.stream(Player.values()).flatMap(targetPlayer ->
					Arrays.stream(Card.values()).map(targetCard -> 
					new Action(state.whoseTurn(), card, Optional.of(targetPlayer), Optional.of(targetCard)))))
		.map(Action::normalize)
		.distinct()
		.filter(a -> state.isValid(a, inHand, drawnCard));
	}
	
	/* Given a set of valid actions, what is the probability that this player would choose this action?
	 * Current implementation assumes even weight across all actions.
	 */
	private static double probabilityOfChoosing(Action action, Set<Action> validActions, Player player) {
		
		return 1.0/validActions.size();
	}
	
	public Map<Card, Double> sampleOpponentHand(Player us, Card ourHand, long numberOfSamples, List<Card> visibleDiscard) {
		//Preconditions.checkNotNull(initialGameState);
		//System.out.println(actionHistory);
		return sampleOpponentHand(ImmutableList.copyOf(visibleDiscard),
				ImmutableList.copyOf(actionHistory), ourHand, us, numberOfSamples);
	}
	
	public static Map<Card, Double> sampleOpponentHand(ImmutableList<Card> visibleDiscard,
			ImmutableList<Action> actionHistory, Card ourHand, Player us, long numberOfSamples) {
		Map<Card, Double> distribution = new EnumMap<Card, Double>(Card.class);
		/*for (Card c : Card.values()) {
			distribution.put(c, 0.0);
		}*/
		
		for (int i=0; i<numberOfSamples; i++) {
			Optional<Map.Entry<FullGameState, Double>> result = sampleStates(visibleDiscard, actionHistory);
			if (!result.isPresent()) {
				continue;
			}
			FullGameState state = result.get().getKey();
			double probability = result.get().getValue();
			
			if (state.hand(us) != ourHand) {
				continue;
			}
			
			distribution.merge(state.hand(us.other()), probability, Double::sum);
		}
		
		return normalize(distribution);
	}
	
	private static <T> Map<T, Double> normalize(Map<T, Double> probabilities) {
		probabilities = new LinkedHashMap<>(probabilities);
		double sum = probabilities.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
		
		for (T key : probabilities.keySet()) {
			probabilities.compute(key, (k, v) -> v / sum);
		}
		
		return Collections.unmodifiableMap(probabilities);
		//Preconditions.checkArgument(expression);
	}
	
	/*public static Action chooseAction(FullGameState3 state) {
		return chooseAction(state.getPublicState(), state.hand(state.whoseTurn()), state.drawnCard().get());
	}

	private static final Random r = new Random();
	private static Action chooseAction(GameState3 state, Card inHand, Card drawnCard) {
		List<Action> possibleActions = Expectiminimaxer.validActions(state, inHand, drawnCard)
					.collect(Collectors.toList());
		int index = r.nextInt(possibleActions.size());
		return possibleActions.get(index);
	}*/

}
