package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
// import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.github.raptortechjs.LoveLetter.TwoPlayer.AI.AI;
import com.google.common.collect.ImmutableSet;

public class Game {
	private FullGameState state;
	private final ThinkingPlayer player1;
	private final ThinkingPlayer player2;

	private final ImmutableSet<GameObserver> observers;
	

	public Game(ThinkingPlayer player1, ThinkingPlayer player2, GameObserver... observers) {
		this.player1 = player1;
		this.player2 = player2;
		state = FullGameState.createNewGame(Optional.of(AI.getStandardVisibleDiscard(AI.NUMBER_OF_CARDS_TO_USE)));
		this.observers = ImmutableSet.copyOf(observers);
	}

	public void nextStep() {
		if (state.winner().isPresent()) {
			return;
		}

		GameState initialState = state.getPublicState();
		state = state.startTurn();
		Action action = chooseAction(state);
		
		if (FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK && action.card == Card.PRINCE) {
			Card targetCard = FGS3Helper.getRandomFromDeck(state.deck());
			action = new Action(action.player, action.card, action.targetPlayer, Optional.of(targetCard));
		}
		
		state = state.endTurn(action);

		GameState currentState = state.getPublicState();

		for (GameObserver o : observers) {
			o.accept(action, initialState, currentState);
		}

	}

	public void runThrough() {
		while (!state.winner().isPresent()) {
			nextStep();
		}
		System.out.println(state.hands());
	}

	private Action chooseAction(FullGameState state) {
		ThinkingPlayer current = (state.whoseTurn() == Player.ONE ? player1 : player2);
		Card currentPlayerHand = state.hand(state.whoseTurn());

		System.out.println(state);
		Action action = current.chooseAction(state.whoseTurn(), state/*.getPublicState()*/, 
				currentPlayerHand, state.drawnCard().get());
		return action;
	}
}
