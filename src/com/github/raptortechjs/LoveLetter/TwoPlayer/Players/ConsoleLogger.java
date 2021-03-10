package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class ConsoleLogger implements GameObserver {

	@Override
	public void accept(Action action, GameState oldState, GameState newState) {
		System.out.println("--------");
		System.out.printf("Action happened: %s%n", action);
		System.out.printf("Discards after: %s%n", getDiscardedCards(newState));
		if (newState.winner().isPresent()) {
			System.out.printf("Winner: %s%n", newState.winner().get());
		}
		System.out.println("--------");
	}
	
	public static Map<Card, Long> getDiscardedCards(GameState state) {
		return Stream.concat(state.visibleDiscard().stream(),
				Stream.concat(state.player1().discardPile().stream(), state.player2().discardPile().stream()))
				.collect(Collectors.groupingBy(c -> c, Collectors.counting()));
	}

}
