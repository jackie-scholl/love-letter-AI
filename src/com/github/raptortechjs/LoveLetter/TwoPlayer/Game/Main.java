package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.AI.AI;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

public class Main {

	public static void main(String[] args) {
		AI.run(args);
	}

	public static void runTest1() {
		for (int i = 0; i < 5; i++) {
			FullGameState s = FullGameState.createNewGame();
			Expectiminimaxer.score(s, 5);
		}

		List<Double> times = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();

			List<Double> scores = new ArrayList<>();

			for (int j = 0; j < 1; j++) {
				FullGameState s = FullGameState.createNewGame();
				double score = Expectiminimaxer.score(s, 4);
				scores.add(score);
			}
			double average = scores.stream().mapToDouble(x -> x).average().getAsDouble();

			long end = System.currentTimeMillis();
			double diff = (end - start) / 1000.0;

			times.add(diff / scores.size());
			double meanTime = times.stream().mapToDouble(x -> x).average().getAsDouble();
			double timeVariance = 0;
			for (double t : times) {
				timeVariance += Math.pow(t - meanTime, 2);
			}
			double timeStdErr = Math.sqrt(timeVariance) / times.size();

			System.out.printf("%4d: %6.2f; %5.2f seconds; mean %.3f, stderr %.3f%n", i, average, diff, meanTime,
					timeStdErr);
		}

		// MaxSize 10000:
		// 13: mean 0.947, stderr 0.020
		// 63: mean 0.976, stderr 0.011

		// MaxSize 1000:
		// 16: mean 0.904, stderr 0.016

		// No cache:
		// 10: mean 1.121, stderr 0.019

		// so cache 1000 > cache 10000 > no cache
	}

	public static void runTest() {
		List<Card> deck = ImmutableList.copyOf(Card.defaultDeckList());
		Set<Multiset<Card>> visibleDiscardsSet = new HashSet<>();
		for (int i = 0; i < deck.size(); i++) {
			List<Card> deckA = new ArrayList<>(deck);
			Card a = deckA.remove(i);
			deckA = Collections.unmodifiableList(deckA);
			for (int j = 0; j < deckA.size(); j++) {
				List<Card> deckB = new ArrayList<>(deckA);
				Card b = deckB.remove(j);
				deckB = Collections.unmodifiableList(deckB);
				for (int k = 0; k < deckB.size(); k++) {
					Card c = deckB.get(k);
					Multiset<Card> visibleDiscardExample = ImmutableMultiset.of(a, b, c);
					visibleDiscardsSet.add(visibleDiscardExample);
				}
			}
		}
		System.out.println(visibleDiscardsSet.size());
		long start = System.currentTimeMillis();

		long totalSum3 = runTest3(visibleDiscardsSet.iterator().next());

		long end = System.currentTimeMillis();
		double diff = (end - start) / 1000.0;
		System.out.printf("Time: %.2f%n", diff);

		System.out.println("Total sum: " + totalSum3);
	}

	private static long runTest2VisibleDiscards(Multiset<Card> visibleDiscards) {
		Multiset<Card> temp = HashMultiset.create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, visibleDiscards);
		temp = Multisets.unmodifiableMultiset(temp);
		long s = runTest2(temp);
		System.out.println(s);
		return s;
	}

	public static long runTest2(Multiset<Card> deck) {
		if (deck.isEmpty()) {
			return 1;
		}

		long sum = 0;
		for (Card c : deck.elementSet()) {
			Multiset<Card> newDeck = HashMultiset.create(deck);
			newDeck.remove(c);
			newDeck = Multisets.unmodifiableMultiset(newDeck);
			sum += runTest2(newDeck);
		}
		return sum;
	}

	public static long runTest3(Multiset<Card> visibleDiscard) {
		long sum = 0;

		Multiset<Card> remaining = HashMultiset.create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(remaining, visibleDiscard);
		remaining = ImmutableMultiset.copyOf(remaining);

		for (Card a : remaining.elementSet()) {
			Multiset<Card> remainingA = HashMultiset.create(remaining);
			remainingA.remove(a);
			remainingA = Multisets.unmodifiableMultiset(remainingA);

			for (Card b : remainingA.elementSet()) {

				long result = runTest3(visibleDiscard, a, b);
				System.out.println(result);
				sum += result;
			}
		}

		return sum;
	}

	private static long runTest3(Multiset<Card> visibleDiscard, Card a, Card b) {
		return runTest3StartTurn(FullGameState.createNewGame(ImmutableList.copyOf(visibleDiscard), a, b));
	}

	public static long runTest3StartTurn(FullGameState state) {
		if (state.winner().isPresent()) {
			return 1;
		}
		Preconditions.checkArgument(!state.hasJustDrawn());
		long sum = 0;
		for (Card c : state.deck().elementSet()) {
			sum += runTest3EndTurn(state.startTurn(c));
		}
		return sum;
	}

	public static long runTest3EndTurn(FullGameState state) {
		if (state.winner().isPresent()) {
			return 1;
		}
		Preconditions.checkArgument(state.hasJustDrawn());

		Stream<Action> actionStream = Expectiminimaxer.validActions(state);
		actionStream = actionStream.parallel();
		return actionStream.map(a -> state.endTurn(a)).mapToLong(Main::runTest3StartTurn).sum();
	}

	public static String getHash() {
		HashingLogger h = new HashingLogger();
		Game g = new Game(new RandomPlayer(), new RandomPlayer(), h);
		g.runThrough();
		return h.digest();
	}
}
