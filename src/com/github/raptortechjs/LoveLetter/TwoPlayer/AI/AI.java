package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.stream.Collectors;

import org.inferred.freebuilder.shaded.com.google.common.base.Preconditions;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.*;

public class AI {
	public static final int NUMBER_OF_CARDS_TO_USE = 6; // 13 in full game
	public static boolean PARALLEL;
	public static int MAX_CACHE_SIZE = -1;
	
	public static void run(String... args) {
		long start = System.currentTimeMillis();
		
		int numberOfCardsToUse;
		
		if (args.length > 0) {
			numberOfCardsToUse = Integer.parseInt(args[0]);
		} else {
			numberOfCardsToUse = NUMBER_OF_CARDS_TO_USE;
		}
		
		System.out.println("args: " + Arrays.deepToString(args));
		
		System.out.println(Runtime.getRuntime().availableProcessors());
		MAX_CACHE_SIZE = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
		
		PARALLEL = args.length <= 2 || args[2].toUpperCase().equals("P");
		System.out.println("PARALLEL: " + PARALLEL);
		
		Collection<Card> initDiscard = getStandardVisibleDiscard(numberOfCardsToUse);
		
		System.out.println(getAllPossibleVisibleDiscards(numberOfCardsToUse).size());
		
		System.out.println(GameState.remaining(ImmutableMultiset.copyOf(initDiscard)));
		System.out.println(WinProbabilities.overallProbabilityOfWin(initDiscard, new GameStateDistribution(Policies.uniformRandom()), Player.ONE));
		Policy result = CounterfactualRegret.iteratePolicies(initDiscard, 1);
		
		System.out.println("xxxxx");
		System.out.println(WinProbabilities.overallProbabilityOfWin(initDiscard, new GameStateDistribution(Policies.uniformRandom()), Player.ONE));
		
		long end = System.currentTimeMillis();
		double diff = (end - start) / 1000.0;
		
		System.out.printf("Time taken: %6.3f%n", diff);		
	}
	
	// 72 minutes for size 7;
	
	private static double overallProbabilityOfWin(Collection<Card> visibleDiscard, GameStateDistribution calculator, Player perspective) {
		Map<FullGameState, Double> map = calculator.getFullDistribution2(visibleDiscard);
		double overallWin = probabilityOfWin(map, perspective);	
		return overallWin;
	}
	
	private static double probabilityOfWin(Map<FullGameState, Double> stateDistribution, Player perspective) {
		return stateDistribution.entrySet().stream()
				.mapToDouble(e -> (int) e.getKey().winner()
						.map(p -> p == perspective ? 1 : 0).get() *  (double) e.getValue())
				.sum();
	}
	
	static Multiset<FullGameState> getStartingStates(List<Card> visibleDiscard) {
		Multiset<FullGameState> stateOccurances = HashMultiset.create();
		
		Multiset<Card> remaining = HashMultiset.create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(remaining, visibleDiscard);
		
		for (Card a : remaining.elementSet()) {
			Multiset<Card> remainingA = HashMultiset.create(remaining);
			remainingA.remove(a);
			remainingA = Multisets.unmodifiableMultiset(remainingA);
			
			for (Card b : remainingA.elementSet()) {
				FullGameState s = FullGameState.createNewGame(visibleDiscard, a, b);
				stateOccurances.add(s);
			}
		}
		
		return Multisets.unmodifiableMultiset(stateOccurances);
	}
	
	public static List<Card> getStandardVisibleDiscard(int numberOfCards) {
		List<Card> deck = new ArrayList<>(Card.defaultDeckList());
		Collections.shuffle(deck, new Random(0));
		
		deck = deck.subList(0, deck.size() - numberOfCards); // discard all but 5 cards at the beginning
		
		return ImmutableList.copyOf(deck);
	}
	
	private static Set<Collection<Card>> getAllPossibleVisibleDiscards(int numberOfCards) {
		int size = 15 - numberOfCards;
		return Distributions.multiPowerSet(Card.defaultDeckMultiset()).stream()
				.filter(ms -> ms.size() == size)
				.collect(Collectors.toSet());
	}	
}
