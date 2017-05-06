package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

public class ConsolePlayer implements ThinkingPlayer {
	public void accept(Action action, GameState3 oldState, GameState3 newState) {}

	public Action chooseAction(Player us, FullGameState3 state, Card inHand, Card justDrawn) {
		//GameState3 state = GameState3.fromPublicGameState(state2);
		Action actionChoice;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do {
			try {
				SortedMultiset<Card> remaining = TreeMultiset.create(state.remainingCards());
				System.out.printf("Remaining cards: %s%n", remaining);
				System.out.printf("You are %s, and have %s and %s. Which do you choose?%n", us, inHand, justDrawn);
				String result = br.readLine();
				actionChoice = getAction(result, us);
			} catch (IOException | IllegalArgumentException e) {
				System.out.println(e);
				actionChoice = null;
			}
			if (!state.isValid(actionChoice, inHand, justDrawn)) {
				System.out.println("not valid");
			}
		} while (!state.isValid(actionChoice, inHand, justDrawn));
		
		return actionChoice;
	}
	
	private static Action getAction(String input, Player us) {
		input = input.trim().toUpperCase();
		String[] array = input.split(" ");
		Card cardChoice = Card.valueOf(array[0]);
		if (cardChoice.numberOfArguments != array.length - 1) {
			System.out.printf("Expected %d arguments, found %d%n", cardChoice.numberOfArguments, array.length - 1);
			return null;
		}
		Optional<Player> targetPlayer = Optional.empty();
		if (array.length > 1) {
			targetPlayer = Optional.of(getTargetPlayer(array[1], us));
			//targetPlayer = Optional.of(PlayerNumber.valueOf(array[1]));
		}
		Optional<Card> targetCard = Optional.empty();
		if (array.length > 2) {
			targetCard = Optional.of(Card.valueOf(array[2]));
		}
		return new Action(us, cardChoice, targetPlayer, targetCard);
	}
	
	private static Player getTargetPlayer(String input, Player us) {
		if (input.equals("1")) {
			return Player.ONE;
		}
		
		if (input.equals("2")) {
			return Player.TWO;
		}
		
		if (input.equals("US") || input.equals("ME")) {
			return us;
		}
		if (input.equals("THEM") || input.equals("OTHER") || input.equals("_") || input.equals(".")) {
			return us.other();
		}
		
		try {
			return Player.valueOf(input);
		} catch (IllegalArgumentException e) {}
		
		throw new IllegalArgumentException();
	}

}
