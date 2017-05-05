package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;

public class ConsolePlayer implements ThinkingPlayer {
	public void accept(Action action, PublicGameState oldState, PublicGameState newState) {}

	public Action chooseAction(PlayerNumber us, PublicGameState state, Card inHand, Card justDrawn) {
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
				Optional<PlayerNumber> targetPlayer = Optional.empty();
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
	
	private static PlayerNumber getTargetPlayer(String input, PlayerNumber us) {
		if (input.equals("1")) {
			return PlayerNumber.PLAYER_1;
		}
		
		if (input.equals("2")) {
			return PlayerNumber.PLAYER_2;
		}
		
		if (input.equals("US") || input.equals("ME")) {
			return us;
		}
		if (input.equals("THEM") || input.equals("OTHER") || input.equals("_") || input.equals(".")) {
			return us.other();
		}
		
		try {
			return PlayerNumber.valueOf(input);
		} catch (IllegalArgumentException e) {}
		
		throw new IllegalArgumentException();
	}

}
