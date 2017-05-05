package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Card;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.GameObserver;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.PublicGameState;

public class ConsoleLogger implements GameObserver {

	@Override
	public void accept(Action action, PublicGameState oldState, PublicGameState newState) {
		// TODO Auto-generated method stub
		System.out.println("--------");
		System.out.printf("Action happened: %s%n", action);
		//System.out.printf("Discards before: %s%n", getDiscardedCards(oldState));
		System.out.printf("Discards after: %s%n", getDiscardedCards(newState));
		if (newState.winner.isPresent()) {
			System.out.printf("Winner: %s%n", newState.winner.get());
		}
		System.out.println("--------");
	}
	
	public static Map<Card, Long> getDiscardedCards(PublicGameState state) {
		return Stream.concat(state.visibleDiscard.stream(),
				Stream.concat(state.player1Discard.stream(), state.player2Discard.stream()))
				.collect(Collectors.groupingBy(c -> c, Collectors.counting()));
	}

}
