package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Optional;
import java.util.Random;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class RandomPlayer implements ThinkingPlayer {
	private final Random r = new Random();

	@Override
	public Action chooseAction(Player us, GameState state, Card inHand, Card justDrawn) {
		Action action;
		
		do {
			Card choice = r.nextBoolean() ? inHand : justDrawn;
			
			Player targetPlayer = state.playerState(us.other()).isProtected() ? us : us.other();
			
			Card targetCard = Card.values()[r.nextInt(8)];
			
			action = new Action(us, choice, Optional.of(targetPlayer), Optional.of(targetCard));
		} while (!state.isValid(action, inHand, justDrawn));
		
		return action;
	}

}
