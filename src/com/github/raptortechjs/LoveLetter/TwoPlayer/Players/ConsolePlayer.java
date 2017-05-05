package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class ConsolePlayer implements ThinkingPlayer {
	public void accept(Action action, PublicGameState oldState, PublicGameState newState) {}

	public Action chooseAction(Players us, PublicGameState state, Card inHand, Card justDrawn) {
		Action actionChoice;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do {
			try {
				System.out.printf("You are %s, and have %s and %s. Which do you choose?%n", us, inHand, justDrawn);
				//String result = System.console().readLine("Make move (%s): ", us);
				String result = br.readLine().trim().toUpperCase();
				String[] array = result.split(" ");
				Card cardChoice = Card.valueOf(array[0]);
				if (cardChoice.numberOfArguments != array.length - 1) {
					System.out.printf("Expected %d arguments, found %d%n", cardChoice.numberOfArguments, array.length - 1);
					actionChoice = null;
					continue;
				}
				Optional<Players> targetPlayer = Optional.empty();
				if (array.length > 1) {
					targetPlayer = Optional.of(getTargetPlayer(array[1], us));
					//targetPlayer = Optional.of(PlayerNumber.valueOf(array[1]));
				}
				Optional<Card> targetCard = Optional.empty();
				if (array.length > 2) {
					targetCard = Optional.of(Card.valueOf(array[2]));
				}
				actionChoice = new Action(us, cardChoice, targetPlayer, targetCard);
			} catch (IOException | IllegalArgumentException e) {
				System.out.println(e);
				actionChoice = null;
			}
		} while (!Game.isValid(actionChoice, state, inHand, justDrawn));
		
		return actionChoice;
	}
	
	private static Players getTargetPlayer(String input, Players us) {
		if (input.equals("1")) {
			return Players.ONE;
		}
		
		if (input.equals("2")) {
			return Players.TWO;
		}
		
		if (input.equals("US") || input.equals("ME")) {
			return us;
		}
		if (input.equals("THEM") || input.equals("OTHER") || input.equals("_") || input.equals(".")) {
			return us.other();
		}
		
		try {
			return Players.valueOf(input);
		} catch (IllegalArgumentException e) {}
		
		throw new IllegalArgumentException();
	}

}
