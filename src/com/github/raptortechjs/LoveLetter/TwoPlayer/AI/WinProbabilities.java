package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.stream.Collectors;

import org.inferred.freebuilder.shaded.com.google.common.collect.Maps;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Card;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.FullGameState;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Player;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.Expectiminimaxer;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;


/*
 * Not working
 */

public class WinProbabilities {
	public static Map<Action, Double> getWinProbabilityActionMap(Map<FullGameState, Double> stateDistribution,
			GameStateDistribution calculator, Player perspective) {
		return Distributions.expand2(stateDistribution, s -> getWinProbabilityActionMap(s, calculator, perspective));
	}
	
	public static Map<Action, Double> getWinProbabilityActionMap(FullGameState state,
			GameStateDistribution calculator, Player perspective) {
		Preconditions.checkArgument(state.hasJustDrawn());
		Preconditions.checkArgument(state.whoseTurn() == perspective);
		
		List<Action> actions = Expectiminimaxer.validActions(state).collect(Collectors.toList());
		
		return actions.stream().collect(Collectors.toMap(a -> a,
				a -> probabilityOfWinGivenDistribution(GameStateDistribution.calculateEndTurnDistribution(state, a), calculator, perspective)));
	}
	
	public static double getTotalWinProbability(Map<FullGameState, Double> stateDistribution,
				GameStateDistribution calculator, Player perspective) {
		return getWinProbabilities(stateDistribution, calculator, perspective).entrySet().stream().mapToDouble(e -> e.getValue()).sum();
	}
	
	public static Map<FullGameState, Double> getWinProbabilities(Map<FullGameState, Double> stateDistribution,
				GameStateDistribution calculator, Player perspective) {
		return stateDistribution.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
				e -> e.getValue() * probabilityOfWin(e.getKey(), calculator, perspective)));
	}
	
	public static double overallProbabilityOfWin(Collection<Card> visibleDiscard, GameStateDistribution calculator, Player perspective) {
		Map<FullGameState, Double> map = calculator.getFullDistribution2(visibleDiscard);
		
		double overallWin = probabilityOfWinGivenFinalDistribution(map, perspective);
		
		return overallWin;
	}

	private static double probabilityOfWin(FullGameState state, GameStateDistribution calculator, Player perspective) {
		return probabilityOfWinGivenFinalDistribution(calculator.getFullDistribution(state), perspective);
	}
	
	private static double probabilityOfWinGivenDistribution(Map<FullGameState, Double> stateDistribution, GameStateDistribution calculator, Player perspective) {
		return probabilityOfWinGivenFinalDistribution(calculator.getFullDistributionFromDistribution(stateDistribution), perspective);
	}
	
	public static double probabilityOfWinGivenFinalDistribution(Map<FullGameState, Double> stateDistribution, Player perspective) {
		/*for (FullGameState s : stateDistribution.keySet()) {
			if (s.winner().get().equals(Player.TWO)) {
				System.out.println(s);
				System.out.println(s.history());
				
				FullGameState s2 = s;
				while (s2.lastHalfStep().isPresent()) {
					s2 = s2.lastHalfStep().get();
					System.out.println(s2.hands());
				}
			}
		}*/
		
		//System.out.println(stateDistribution.entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().winner(), Collectors.mapping(e -> e.getValue(), Collectors.toList()))));
		return stateDistribution.entrySet().stream()
				.mapToDouble(e -> ((int) (e.getKey().winner()
						.map(p -> p.equals(perspective) ? 1 : 0).get())) *  (double) e.getValue())
				.sum();
	}
	
	/*static Map<FullGameState, Double> getWinProbabilities(FullGameState state, Policy p) {
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
	
		Preconditions.checkArgument(state.deckSize() > 0);
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		return Distributions.expand(getNextStepWinProbabilities(state, p), s -> getWinProbabilities(s, p));
	}
	

	private static Map<FullGameState, Double> getNextStepWinProbabilities(FullGameState state, Policy p) {
		Preconditions.checkNotNull(state);
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkArgument(state.deckSize() > 0);
		
		Map<FullGameState, Double> map = Distributions.expand(getNextStepDistributionStartTurn(state, p),
				s -> getNextStepDistributionEndTurn(s, p));
		
		assert map.keySet().stream().allMatch(s -> !s.hasJustDrawn());
		
		Map<FullGameState, Double> map2 = Maps.newHashMap(map);
		
		double sum = map2.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
		map2.put(state, sum);
		
		return map2;
	}

	private static Map<FullGameState, Double> getNextStepDistributionEndTurn(FullGameState state, Policy p) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(state.hasJustDrawn());
		
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, state.winner().get() == Player.ONE ? 1.0 : 0.0);
		}
		
		Map<FullGameState, Double> map = new HashMap<>();
		
		Map<Action, Double> choiceDistribution = 
				p.choiceDistribution(InformationSet.fromFullGameState(state, state.whoseTurn()));
		
		choiceDistribution = Distributions.normalize(choiceDistribution);
		
		for (Map.Entry<Action, Double> e : choiceDistribution.entrySet()) {
			if (e.getKey().card != Card.PRINCE || !FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK) {
				FullGameState s = state.endTurn(e.getKey());
				Map<FullGameState, Double> temp = getNextStepDistributionStartTurn(s, p);
				
				for (Map.Entry<FullGameState, Double> e2 : temp.entrySet()) {
					map.put(e2.getKey(), e2.getValue());
				}
				
				//double val = Distributions.sum(temp) * e.getValue();
				//map.put(s, val);
			} else {
				Map<FullGameState, Double> temp = getPrinceDistribution(state, e.getKey());
				//System.out.println(gameDistribution);
				//Distributions.mergeInByAdding(map, Maps.transformValues(gameDistribution, v -> v * e.getValue()));
				for (Map.Entry<FullGameState, Double> e2 : temp.entrySet()) {
					map.put(e2.getKey(), e2.getValue());
				}
				
				//double val = Distributions.sum(temp) * e.getValue();
				//map.put(s, val);
			}
		}
		
		//Map<FullGameState, Double> map2 = Distributions.expand(map, s -> getNextSetpDistributionStartTurn(s, p))
		
		//double sum = map.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
		//map.put(state, sum);
		
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

	private static Map<FullGameState, Double> getNextStepDistributionStartTurn(FullGameState state, Policy p) {
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
