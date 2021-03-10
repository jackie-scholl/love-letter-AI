package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import com.github.raptortechjs.LoveLetter.TwoPlayer.AI.*;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

public class ConsolePlayer implements ThinkingPlayer {
	private final MonteCarloTreeSearch mcts;
	
	public ConsolePlayer(MonteCarloTreeSearch search) {
		this.mcts = search;
	}

	public Action chooseAction(Player us, GameState state, Card inHand, Card justDrawn) {
		Action actionChoice;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do {
			try {
				SortedMultiset<Card> remaining = TreeMultiset.create(state.remainingCards());
				System.out.printf("Remaining cards: %s%n", remaining);
				System.out.printf("They have2:%s%n", getOpponentHandDistribution2(us, state, inHand, justDrawn));
				doThings(us, state, inHand, justDrawn);
				
				System.out.printf("You are %s, and have %s and %s. Which do you choose?%n", us, inHand, justDrawn);
				String result = br.readLine();
				actionChoice = getAction(result, us);
			} catch (IOException | IllegalArgumentException e) {
				System.out.println(e);
				actionChoice = null;
			}
			if (!state.isValid(actionChoice, inHand, justDrawn)) {
				System.out.println("not valid");
			}
		} while (!state.isValid(actionChoice, inHand, justDrawn));
		
		return actionChoice;
	}
	
	private static Action getAction(String input, Player us) {
		input = input.trim().toUpperCase();
		String[] array = input.split(" ");
		Card cardChoice = Card.valueOf(array[0]);
		if (cardChoice.numberOfArguments != array.length - 1) {
			System.out.printf("Expected %d arguments, found %d%n", cardChoice.numberOfArguments, array.length - 1);
			return null;
		}
		Optional<Player> targetPlayer = Optional.empty();
		if (array.length > 1) {
			targetPlayer = Optional.of(getTargetPlayer(array[1], us));
		}
		Optional<Card> targetCard = Optional.empty();
		if (array.length > 2) {
			targetCard = Optional.of(Card.valueOf(array[2]));
		}
		
		return new Action(us, cardChoice, targetPlayer, targetCard);
	}
	
	private static Player getTargetPlayer(String input, Player us) {
		if (input.equals("1")) {
			return Player.ONE;
		}
		
		if (input.equals("2")) {
			return Player.TWO;
		}
		
		if (input.equals("US") || input.equals("ME")) {
			return us;
		}
		if (input.equals("THEM") || input.equals("OTHER") || input.equals("_") || input.equals(".")) {
			return us.other();
		}
		
		try {
			return Player.valueOf(input);
		} catch (IllegalArgumentException e) {}
		
		throw new IllegalArgumentException();
	}
	
	private String getOpponentHandDistribution2(Player us, GameState state, Card inHand, Card justDrawn) {
		System.out.println(mcts.actionHistory);
		Multiset<Card> remaining = HashMultiset.create(state.remainingCards());
		remaining.remove(inHand);
		Map<Card, Double> dist = Expectiminimaxer.multisetToNormalizedFrequencyMap(remaining);
		Map<Card, String> out = dist.entrySet().stream()
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), String.format("%2.0f%%", e.getValue() * 100.0)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new EnumMap<>(out).toString();
	}
	
	private String getOpponentHandDistribution(Player us, GameState state, Card inHand, Card justDrawn) {
		Map<Card, Double> dist = mcts.sampleOpponentHand(us, inHand, 100000, state.visibleDiscard());
		Map<Card, String> out = dist.entrySet().stream()
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), String.format("%2.0f%%", e.getValue() * 100.0)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new EnumMap<>(out).toString();
	}
	
	private void doThings(Player us, GameState state, Card inHand, Card justDrawn) {
		long start = System.currentTimeMillis();
		
		FullGameState state2 = (FullGameState) state;
		
		System.out.println(state2.hands());
		System.out.printf("Overall probability 1: %f%n", CounterfactualRegret.probability(Policies.uniformRandom(), state2, state.visibleDiscard()));
		System.out.printf("Overall probability 2: %f%n", CounterfactualRegret.probabilityP(us, Policies.uniformRandom(), state2, state.visibleDiscard()));
		System.out.printf("Overall probability 3: %f%n", CounterfactualRegret.probabilityPNeg(us, Policies.uniformRandom(), state2, state.visibleDiscard()));
		
		shouldBeEqual(CounterfactualRegret.probability(Policies.uniformRandom(), state2, state.visibleDiscard()), 
			CounterfactualRegret.probabilityP(us, Policies.uniformRandom(), state2, state.visibleDiscard()) 
			* CounterfactualRegret.probabilityPNeg(us, Policies.uniformRandom(), state2, state.visibleDiscard()));

		InformationSet is = InformationSet.fromFullGameState(state2, us);

		System.out.printf("Time taken: %.3f%n", (System.currentTimeMillis() - start)/1000.0);
		System.out.println(Expectiminimaxer.validActions(state, inHand, justDrawn)
				.sorted(Comparator.<Action, Integer>comparing(a -> a.card.value)
						.thenComparingInt(a -> a.targetCard.map(c -> c.value).orElse(0)))
				.collect(Collectors.toList()));
	}
	
	public static void shouldBeEqual(double a, double b) {
		if (Double.isFinite(a) && Double.isFinite(b)) {
			assert Math.abs(a - b) < 1e-9 : "Values should be equal: " + a + " and " + b;
		}
	}
}
