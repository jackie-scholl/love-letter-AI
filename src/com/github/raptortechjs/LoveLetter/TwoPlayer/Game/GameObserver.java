package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public interface GameObserver {
	public void accept(Action action, GameState3 oldState, GameState3 newState);
}
