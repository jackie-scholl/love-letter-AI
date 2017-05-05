package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Optional;
import java.util.Random;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class RandomPlayer implements ThinkingPlayer {
	private final Random r = new Random();

	@Override
	public void accept(Action action, PublicGameState oldState, PublicGameState newState) {}

	@Override
	public Action chooseAction(Players us, PublicGameState state, Card inHand, Card justDrawn) {
		// TODO Auto-generated method stub
		//return null;
		Action action;
		
		
		
		do {
			Card choice = r.nextBoolean() ? inHand : justDrawn;
			
			//PlayerNumber targetPlayer = r.nextBoolean() ? PlayerNumber.PLAYER_1 : PlayerNumber.PLAYER_2;
			Players targetPlayer = state.isPlayerProtected(us.other()) ? us : us.other();
			
			Card targetCard = Card.PRINCESS;
			//Card targetCard = Card.values()[r.nextInt(8)];
			
			action = new Action(us, choice, Optional.of(targetPlayer), Optional.of(targetCard));
		} while (!Game.isValid(action, state, inHand, justDrawn));
		
		return action;
	}

}
