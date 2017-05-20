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
		// Map<FullGameState, Double> map =
		// calculator.getFullDistribution2(visibleDiscard);
		// GameStateDistribution.getFinalDistribution(visibleDiscard, new ReasonableUniformPolicy());

		Set<FullGameState> set = calculator.getAllGameStates(visibleDiscard);

		return getInfoSets(set, perspective);
	}

	public static Map<InformationSet, Map<FullGameState, Double>> getOverallInfoSets2(Collection<Card> visibleDiscard,
			GameStateDistribution calculator, Player perspective) {
		// Map<FullGameState, Double> overallDistribution = calculator.getFullDistribution2(visibleDiscard);
		// Map<FullGameState, Double> initialDistribution = GameStateDistribution.initialDistribution(visibleDiscard);
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

	/*private static double probabilityOfGameStateGivenDistribution(FullGameState state, Map<FullGameState, Double> distribution) {
		
	}*/

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

	/*private static Multimap<InformationSet, FullGameState> getInfoSets(FullGameState state) {
		Preconditions.checkNotNull(state);
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkArgument(state.deckSize() > 0);
		
		Map<FullGameState, Double> map = Distributions.expand(getNextStepDistributionStartTurn(state),
				s -> getNextStepDistributionEndTurn(s, p));
		
		assert map.keySet().stream().allMatch(s -> !s.hasJustDrawn());
		
		return map;
	}
	
	private static Map<FullGameState, Double> getNextStepDistributionEndTurn(FullGameState state, Policy p) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(state.hasJustDrawn());
		
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		Map<FullGameState, Double> map = new HashMap<>();
		
		Map<Action, Double> choiceDistribution = 
				p.choiceDistribution(InformationSet.fromFullGameState(state, state.whoseTurn()));
		
		choiceDistribution = Distributions.normalize(choiceDistribution);
		
		for (Map.Entry<Action, Double> e : choiceDistribution.entrySet()) {
			if (e.getKey().card != Card.PRINCE || !FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK) {
				FullGameState s = state.endTurn(e.getKey());
				map.put(s, e.getValue());
			} else {
				Map<FullGameState, Double> gameDistribution = getPrinceDistribution(state, e.getKey());
				//System.out.println(gameDistribution);
				Distributions.mergeInByAdding(map, Maps.transformValues(gameDistribution, v -> v * e.getValue()));
			}
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	private static Map<FullGameState, Double> getPrinceDistribution(FullGameState state, Action a) {
		Preconditions.checkArgument(a.card == Card.PRINCE);
		Preconditions.checkArgument(a.targetPlayer.isPresent());
		Preconditions.checkArgument(!a.targetCard.isPresent());
		
		Map<Card, Double> deck = Distributions.fromMultiset(state.deck());
		Map<FullGameState, Double> stateDistribution = new HashMap<>();
		
		for (Card c : deck.keySet()) {
			double value = deck.get(c);
			Action newAction = new Action(a.player, a.card, a.targetPlayer, Optional.of(c));
			FullGameState newState = state.endTurn(newAction);
			stateDistribution.put(newState, value);
		}
		
		return Collections.unmodifiableMap(stateDistribution);
	}
	
	private static Map<FullGameState, Double> getNextStepDistributionStartTurn(FullGameState state) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkArgument(!state.winner().isPresent());
	
		Preconditions.checkArgument(state.deckSize() > 0);
		
		Multiset<FullGameState> states = HashMultiset.create();
		
		for (Card c : state.deck()) {
			FullGameState s = state.startTurn(c);
			assert (s.hasJustDrawn());
			states.add(s);
		}
		
		assert !states.isEmpty();
		
		return Distributions.fromMultiset(states);
	}*/
}
