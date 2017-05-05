package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Optional;
import java.util.Random;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class RandomPlayer implements ThinkingPlayer {
	private final Random r = new Random();

	@Override
	public void accept(Action action, GameState3 oldState, GameState3 newState) {}

	@Override
	public Action chooseAction(Player us, GameState3 state, Card inHand, Card justDrawn) {
		Action action;
		
		do {
			Card choice = r.nextBoolean() ? inHand : justDrawn;
			
			//PlayerNumber targetPlayer = r.nextBoolean() ? PlayerNumber.PLAYER_1 : PlayerNumber.PLAYER_2;
			Player targetPlayer = state.state(us.other()).isProtected() ? us : us.other();
			
			Card targetCard = Card.PRINCESS;
			//Card targetCard = Card.values()[r.nextInt(8)];
			
			action = new Action(us, choice, Optional.of(targetPlayer), Optional.of(targetCard));
		} while (!state.isValid(action, inHand, justDrawn));
		
		return action;
	}

}
