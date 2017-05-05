package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
// import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableSet;

public class Game {
	private FullGameState3 state;
	private final ThinkingPlayer player1;
	private final ThinkingPlayer player2;

	private final ImmutableSet<GameObserver> observers;
	

	public Game(ThinkingPlayer player1, ThinkingPlayer player2, GameObserver... observers) {
		this.player1 = player1;
		this.player2 = player2;
		state = FullGameState3.createNewGame();
		this.observers = ImmutableSet.of(observers[0]);
	}

	public void nextStep() {
		if (state.winner().isPresent()) {
			return;
		}

		GameState3 initialState = state.getPublicState();
		state = state.startTurn();
		Action action = chooseAction(state);
		state = state.endTurn(action);

		GameState3 currentState = state.getPublicState();

		for (GameObserver o : observers) {
			o.accept(action, initialState, currentState);
		}

	}

	public void runThrough() {
		while (!state.winner().isPresent()) {
			nextStep();
		}
	}

	private Action chooseAction(FullGameState3 state) {
		ThinkingPlayer current = (state.whoseTurn() == Player.ONE ? player1 : player2);
		Card currentPlayerHand = state.hand(state.whoseTurn());

		Action action = current.chooseAction(state.whoseTurn(), state.getPublicState(), 
				currentPlayerHand, state.drawnCard().get());
		return action;
	}
}
