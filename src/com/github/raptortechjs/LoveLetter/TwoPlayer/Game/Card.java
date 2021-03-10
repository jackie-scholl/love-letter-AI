package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

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
		
	private static final ImmutableMultiset<Card> defaultDeckMultiset = 
			Arrays.stream(Card.values())
				.map(c -> Collections.nCopies(c.numberOfCopies, c))
				.flatMap(List::stream)
				.collect(ImmutableMultiset.toImmutableMultiset());
	
	public static Multiset<Card> defaultDeckMultiset() {
		return defaultDeckMultiset;
	}
	
	public static List<Card> defaultDeckList() {
		return defaultDeckMultiset.asList();
	}
}
