package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public enum Players {
	ONE ("Player 1"),
	TWO ("Player 2");
	
	String humanName;

	private Players(String humanName) {
		this.humanName = humanName;
	}
	
	public Players other() {
		return (this == ONE? TWO : ONE);
	}
	
	public String toString() {
		return humanName;
	}
}