package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

public enum PlayerNumber {
	PLAYER_1 ("Player 1"),
	PLAYER_2 ("Player 2");
	
	String humanName;

	private PlayerNumber(String humanName) {
		this.humanName = humanName;
	}
	
	public PlayerNumber other() {
		return (this == PLAYER_1? PLAYER_2 : PLAYER_1);
	}
	
	public String toString() {
		return humanName;
	}
}