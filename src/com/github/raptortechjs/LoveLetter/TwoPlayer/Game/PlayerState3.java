package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.collect.ImmutableList;

@FreeBuilder
public interface PlayerState3 {
	public ImmutableList<Card> discardPile();
	public boolean isProtected();
	
	Builder toBuilder();
	
	class Builder extends PlayerState3_Builder {}
}
