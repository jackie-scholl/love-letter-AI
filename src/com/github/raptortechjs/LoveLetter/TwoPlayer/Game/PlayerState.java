package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class PlayerState {
	public final Card hand;
	public final ImmutableList<Card> discardPile;
	
	private PlayerState(Card hand, List<Card> discardPile) {
		this.hand = hand;
		this.discardPile = ImmutableList.copyOf(discardPile);
	}
	
	public static PlayerState create(Card hand) {
		return new PlayerState(hand, ImmutableList.of());
	}
	
	public PlayerState replaceHand(Card newCard) {
		return new PlayerState(newCard, discardPile);
	}

	public PlayerState replaceDiscard(List<Card> newDiscardPile) {
		return new PlayerState(hand, newDiscardPile);
	}
	
	public PlayerState addToDiscard(Card cardToDiscard) {
		return this.replaceDiscard(ImmutableList.<Card>builder().addAll(discardPile).add(cardToDiscard).build());
	}
	
	public PlayerState replaceAndDiscardHand(Card newCard) {
		return this.replaceHand(newCard).addToDiscard(hand);
	}
}
