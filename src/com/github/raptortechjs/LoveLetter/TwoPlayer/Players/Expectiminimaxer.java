package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Card;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.PublicGameState;
import com.google.common.collect.*;

public class Expectiminimaxer {
	public static ImmutableMultiset<Card> getDiscardedCards(PublicGameState state) {
		ImmutableMultiset.Builder<Card> builder = ImmutableMultiset.<Card>builder();
		builder.addAll(state.visibleDiscard);
		builder.addAll(state.player1Discard);
		builder.addAll(state.player2Discard);
		return builder.build();
	}
	
	public static ImmutableMultiset<Card> getDefaultCardMap() {
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
	
	//private final PlayerNumber us;
	
	
	//public Action getAction(GameState state, )
	
	//private static Map<Card, Long> 

}
