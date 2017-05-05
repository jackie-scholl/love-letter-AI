package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.collect.ImmutableList;

@FreeBuilder
public interface PlayerState3 {
	public ImmutableList<Card> discardPile();
	public boolean isProtected();
	
	Builder toBuilder();
	
	class Builder extends PlayerState3_Builder {
		public Builder() {
			this.clearDiscardPile();
			this.isProtected(false);
		}
	}
	
	default public PlayerState toPlayerState(Card hand) {
		return new PlayerState(hand, discardPile());
	}
	
	public static PlayerState3 fromPlayerState(PlayerState s, boolean isProtected) {
		return new PlayerState3.Builder().addAllDiscardPile(s.discardPile).isProtected(isProtected).build();
	}
	
	static PlayerState3 defaultPlayerState() {
		return new PlayerState3.Builder().build();
	}
}
