package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@FreeBuilder
public interface GameState3 {
	public ImmutableMap<Player, PlayerState3> players();
	
	public ImmutableList<Card> visibleDiscard();
	
	public Optional<Player> winner();
	
	public Player whoseTurn();
	public int turnNumber();

	public Builder toBuilder();
	class Builder extends GameState3_Builder {}
}
