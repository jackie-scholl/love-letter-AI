package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class RandomPlayer2 implements ThinkingPlayer {
	private final Random r = new Random();

	@Override
	public Action chooseAction(Player us, GameState state, Card inHand, Card justDrawn) {
		List<Action> actions = Expectiminimaxer.validActions(state, inHand, justDrawn).collect(Collectors.toList());
		
		return actions.get(r.nextInt(actions.size()));
	}
}
