package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public enum Card {
	PRINCESS (8, 1, 0),
	COUNTESS (7, 1, 0),
	KING     (6, 1, 1),
	PRINCE   (5, 2, 1),
	HANDMAID (4, 2, 0),
	BARON    (3, 2, 1),
	PRIEST   (2, 2, 1),
	GUARD    (1, 5, 2);
	
	public int value;
	public int numberOfCopies;
	public int numberOfArguments;
	
	private Card(int value, int number, int numberOfArguments) {
		this.value = value;
		this.numberOfCopies = number;
		this.numberOfArguments = numberOfArguments;
	}
	
	private static final ImmutableList<Card> defaultDeck;
	
	static {
		ImmutableList.Builder<Card> defaultDeckBuilder = ImmutableList.<Card>builder();
		
		for (Card c : Card.values()) {
			for (int i=0; i < c.numberOfCopies; i++) {
				defaultDeckBuilder.add(c);
			}
		}
		
		defaultDeck = defaultDeckBuilder.build();
	}
	
	public static List<Card> defaultDeck() {
		return defaultDeck;
	}
}
