package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.*;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Card;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.GameState;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.PublicGameState;
import com.google.common.collect.*;

public class Expectiminimaxer {
	private static ImmutableMultiset<Card> getDiscardedCards(PublicGameState state) {
		ImmutableMultiset.Builder<Card> builder = ImmutableMultiset.<Card>builder();
		builder.addAll(state.visibleDiscard);
		builder.addAll(state.player1Discard);
		builder.addAll(state.player2Discard);
		return builder.build();
	}
	
	private static ImmutableMultiset<Card> getDefaultCardMap() {
		ImmutableMultiset.Builder<Card> map = ImmutableMultiset.<Card>builder();
		for (Card c : Card.values()) {
			map.setCount(c, c.numberOfCopies);
		}
		return map.build();
	}
	
	private static <T> ImmutableMultiset<T> difference(Multiset<T> multiset1, Multiset<T> multiset2) {
		Multiset<T> temp = HashMultiset.<T>create(multiset1);
		Multisets.removeOccurrences(temp, multiset2);
		return ImmutableMultiset.copyOf(temp);
	}
	
	private static ImmutableMultiset<Card> remainingDeck(PublicGameState state) {
		return difference(getDefaultCardMap(), getDiscardedCards(state));
	}
	
	private static <T> Map<T, Double> multisetToNormalizedFrequencyMap(Multiset<T> multiset) {
		ImmutableMap.Builder<T, Double> frequencyMap = ImmutableMap.<T, Double>builder();
		for (Multiset.Entry<T> e : multiset.entrySet()) {
			double frequency = e.getCount() / multiset.size();
			frequencyMap.put(e.getElement(), frequency);
		}
		//frequencyMap.
		return frequencyMap.build();
	}

	public Action getAction(GameState state, Card justDrawn) {
		throw new UnsupportedOperationException();
		//return null;
	}
	
	//private static Stream<Action> possibleActions(PublicGameState )

}
