package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.stream.Collectors;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Card;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.FullGameState;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Player;
import com.google.common.collect.*;

public class InformationSets {
	public static SetMultimap<InformationSet, FullGameState> getOverallInfoSets(Collection<Card> visibleDiscard,
			GameStateDistribution calculator, Player perspective) {
		Set<FullGameState> set = calculator.getAllGameStates(visibleDiscard);

		return getInfoSets(set, perspective);
	}

	public static Map<InformationSet, Map<FullGameState, Double>> getOverallInfoSets2(Collection<Card> visibleDiscard,
			GameStateDistribution calculator, Player perspective) {
		Map<FullGameState, Double> allProbabilities = calculator.getProbabilities(visibleDiscard);

		Multimap<InformationSet, FullGameState> multiMap = getOverallInfoSets(visibleDiscard, calculator, perspective);

		Map<InformationSet, Map<FullGameState, Double>> map = new HashMap<>();

		for (InformationSet is : multiMap.keySet()) {
			Map<FullGameState, Double> tempMap = new HashMap<>();

			for (FullGameState s : multiMap.get(is)) {
				double probability = allProbabilities.get(s);
				tempMap.put(s, probability);
			}

			map.put(is, Distributions.normalize(tempMap));
		}

		return map;
	}

	public static Table<InformationSet, FullGameState, Double> getOverallInfoSets3(Collection<Card> visibleDiscard,
			GameStateDistribution calculator, Player perspective) {
		return ImmutableTable.copyOf(table(getOverallInfoSets2(visibleDiscard, calculator, perspective)));
	}

	public static <R, C, V> Table<R, C, V> table(Map<R, Map<C, V>> fromTable) {
		Table<R, C, V> table = HashBasedTable.create();
		for (R rowKey : fromTable.keySet()) {
			Map<C, V> rowMap = fromTable.get(rowKey);
			for (C columnKey : rowMap.keySet()) {
				V value = rowMap.get(columnKey);
				table.put(rowKey, columnKey, value);
			}
		}
		return table;
	}

	public static SetMultimap<InformationSet, FullGameState> getInfoSets(Set<FullGameState> states, Player perspective) {
		Map<InformationSet, Collection<FullGameState>> map = states.stream()
				.collect(Collectors.groupingBy(s -> InformationSet.fromFullGameState(s, perspective),
						Collectors.toCollection(HashSet::new)));

		SetMultimap<InformationSet, FullGameState> multimap = HashMultimap.create();

		for (InformationSet is : map.keySet()) {
			multimap.putAll(is, map.get(is));
		}

		return Multimaps.unmodifiableSetMultimap(multimap);
	}
}
