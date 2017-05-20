package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.Map;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.ThinkingPlayer;
import com.google.common.collect.ImmutableMap;

public interface Policy {
	public Map<Action, Double> choiceDistribution(InformationSet set);
	
	public static Policy fromThinkingPlayer(ThinkingPlayer p) {
		return (Policy) ((InformationSet s) -> {
			Action a = p.chooseAction(s.perspective(), s.state(), s.hand(), s.drawnCard().get());
			return ImmutableMap.of(a, 1.0);
		});
	}
}