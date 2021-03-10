package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.List;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.ImmutableMap;

@FreeBuilder
public abstract class InformationSet {
	abstract public Player perspective();
	
	abstract public GameState state();
	
	abstract public Card hand();
	
	abstract public Optional<Card> drawnCard();
	
	abstract public Builder toBuilder();
	
	public static Builder builder() {
		return new Builder();
	}
	
	static class Builder extends InformationSet_Builder { }
	
	public static InformationSet fromFullGameState(FullGameState state, Player perspective) {
		InformationSet is = InfoSetHelper.fromFullGameState(state, perspective);
		return is;
	}
	
	public boolean includes(FullGameState s) {
		return this.equals(fromFullGameState(s, perspective()));
	}
	
	public String toString() {
		return ImmutableMap.of("hand", hand(), "drawnCard", drawnCard()).toString();
	}
}

class InfoSetHelper {
	static InformationSet fromFullGameState(FullGameState state, Player perspective) {
		InformationSet.Builder b  = InformationSet.builder()
				.hand(state.hand(perspective))
				.state(state.getPublicState())
				.perspective(perspective);
		if (state.whoseTurn() == perspective) {
			b.drawnCard(state.drawnCard());
		}
		return b.build();
	}
}
