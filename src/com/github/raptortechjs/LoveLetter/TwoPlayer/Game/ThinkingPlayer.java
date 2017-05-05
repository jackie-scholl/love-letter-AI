package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

/*
 * Bad name, but the idea is a reference to an actual player instance that we can ask for input, as opposed to the idea
 * of a player within the game model.
 */
public interface ThinkingPlayer extends GameObserver {
	public Action chooseAction(PlayerNumber us, PublicGameState state, Card inHand, Card justDrawn);
}