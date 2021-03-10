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
		return stateDistribution.entrySet().stream()
				.mapToDouble(e -> ((int) (e.getKey().winner()
						.map(p -> p.equals(perspective) ? 1 : 0).get())) *  (double) e.getValue())
				.sum();
	}	
}
