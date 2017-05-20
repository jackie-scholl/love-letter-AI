package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

/*
 * Bad name, but the idea is a reference to an actual player instance that we can ask for input, as opposed to the idea
 * of a player within the game model.
 */
public interface ThinkingPlayer {
	public Action chooseAction(Player us, GameState publicGameState, Card inHand, Card justDrawn);
}