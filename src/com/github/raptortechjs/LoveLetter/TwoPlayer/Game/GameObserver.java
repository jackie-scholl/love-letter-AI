package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public interface GameObserver {
	public void accept(Action action, PublicGameState oldState, PublicGameState newState);
}
