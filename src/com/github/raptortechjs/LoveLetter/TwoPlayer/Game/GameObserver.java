package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public interface GameObserver {
	public void accept(Action action, GameState oldState, GameState newState);
}
