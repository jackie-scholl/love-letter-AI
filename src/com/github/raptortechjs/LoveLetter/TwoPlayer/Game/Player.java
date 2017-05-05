package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public enum Player {
	ONE ("Player 1"),
	TWO ("Player 2");
	
	String humanName;

	private Player(String humanName) {
		this.humanName = humanName;
	}
	
	public Player other() {
		return (this == ONE? TWO : ONE);
	}
	
	public String toString() {
		return humanName;
	}
}