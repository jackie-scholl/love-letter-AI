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
		
		//MonteCarloTreeSearch mcts = new MonteCarloTreeSearch();
		//new Game(new ConsolePlayer(mcts), /*new ConsolePlayer(), *//*new Expectiminimaxer(4),*/new RandomPlayer2(), new ConsoleLogger(), mcts).runThrough();

		/*Set<String> hashes = new HashSet<>();
		
		long start = System.currentTimeMillis();
		int lastHashesSize = 0;
		for (int i=0; i<1e10; i++) {
			if (i % 1e6 == 1) {
				long end = System.currentTimeMillis();
				double differencePer = (end - start)*1.0/i;
				System.out.printf("%d; %.3e; %.3f%n", hashes.size(), differencePer, (hashes.size() - lastHashesSize)*1.0/1e6);
				lastHashesSize = hashes.size();
				//System.out.println(hashes.size());
			}
			hashes.add(getHash());
		}*/

		// GameState2 state = new GameState2.Builder().build();
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
			// DoubleSummaryStatistics stats = scores.stream().mapToDouble(x -> x).summaryStatistics();

			long end = System.currentTimeMillis();
			double diff = (end - start) / 1000.0;

			times.add(diff / scores.size());
			// double timesAverage
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
		/*long totalSum = 0;
		for (Multiset<Card> a1 : visibleDiscardsSet) {
			long start = System.currentTimeMillis();
			
			Multiset<Card> temp = HashMultiset.create(Card.defaultDeckMultiset());
			Multisets.removeOccurrences(temp, a1);
			temp = Multisets.unmodifiableMultiset(temp);
			//Set<List<Card>> s = runTest2(temp, ImmutableList.of());
			long s = runTest2(temp);
			System.out.print(s);
			totalSum += s;
		
			long end = System.currentTimeMillis();
			double diff = (end-start)/1000.0;
			System.out.printf("%6.2f%n", diff);
		}
		System.out.println("total sum: " + totalSum);*/

		long start = System.currentTimeMillis();

		/*long totalSum2 = visibleDiscardsSet.stream()
				//.limit(8)
				.parallel().mapToLong(Main::runTest2VisibleDiscards).sum();*/

		long totalSum3 = runTest3(visibleDiscardsSet.iterator().next());

		long end = System.currentTimeMillis();
		double diff = (end - start) / 1000.0;
		System.out.printf("Time: %.2f%n", diff);

		// System.out.println("Total sum: " + totalSum2);
		System.out.println("Total sum: " + totalSum3);
		// Multiset<Card> a1 = visibleDiscardsSet.iterator().next();
		// GS3Helper.
	}

	private static long runTest2VisibleDiscards(Multiset<Card> visibleDiscards) {
		Multiset<Card> temp = HashMultiset.create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, visibleDiscards);
		temp = Multisets.unmodifiableMultiset(temp);
		// Set<List<Card>> s = runTest2(temp, ImmutableList.of());
		// long s = runTest2(temp);
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
				// Multiset<Card> remainingB = HashMultiset.create(remainingA);
				// remainingB.remove(b);
				// remainingB = Multisets.unmodifiableMultiset(remainingB);

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
		// if (state.deck().size() > 10) {
		actionStream = actionStream.parallel();
		// }
		return actionStream.map(a -> state.endTurn(a)).mapToLong(Main::runTest3StartTurn).sum();

		/*
		long sum = 0; 
		Iterable<Action> actions = Expectiminimaxer.validActions(state)
				.map(Action::normalize)
				.filter(state::isValid)
				.collect(Collectors.toSet());
		
		for (Action a : actions) {
			sum += runTest3StartTurn(state.endTurn(a));
		}
		return sum;*/
	}

	public static String getHash() {
		HashingLogger h = new HashingLogger();
		// Game g = new Game(new ConsolePlayer(), new ConsolePlayer(), new ConsoleLogger(), h);
		Game g = new Game(new RandomPlayer(), new RandomPlayer(), h);
		g.runThrough();
		return h.digest();
	}

}
