package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
// import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableSet;

public class Game {
	private GameState state;
	private final ThinkingPlayer player1;
	private final ThinkingPlayer player2;

	private final ImmutableSet<GameObserver> observers;

	public Game(ThinkingPlayer player1, ThinkingPlayer player2, GameObserver... observers) {
		this.player1 = player1;
		this.player2 = player2;
		state = GameState.createNewGame();
		this.observers = ImmutableSet.of(observers[0]);
	}

	public void nextStep() {
		if (state.hasAnyoneWon()) {
			return;
		}

		PublicGameState initialState = state.getPublicState();

		state = endProtection(state);
		Card drawnCard = drawCard();
		Action action = chooseAction(drawnCard);
		discardCard(drawnCard, action);
		state = applyAction(action, state);
		state = checkWin(state);

		PublicGameState currentState = state.getPublicState();

		for (GameObserver o : observers) {
			o.accept(action, initialState, currentState);
		}

		state = state.toBuilder().mapWhoseTurn(PlayerNumber::other).mapTurnNumber(i -> i+1).build();
	}

	public void runThrough() {
		while (!state.hasAnyoneWon()) {
			nextStep();
		}
	}

	private static GameState endProtection(GameState state) {
		return state.getWhoseTurn() == PlayerNumber.PLAYER_1 ?
				state.toBuilder().setPlayer1Protected(false).build() :
				state.toBuilder().setPlayer2Protected(false).build();
	}

	private Card drawCard() {
		Card drawnCard = state.peekAtDeck();
		state = state.withoutTopCard();
		return drawnCard;
	}

	private Action chooseAction(Card drawnCard) {
		ThinkingPlayer current = (state.getWhoseTurn() == PlayerNumber.PLAYER_1 ? player1 : player2);
		Card currentPlayerHand = state.getPlayerState(state.getWhoseTurn()).hand;

		Action action = current.chooseAction(state.getWhoseTurn(), state.getPublicState(), currentPlayerHand, drawnCard);

		if (!isValid(action, state.getPublicState(), currentPlayerHand, drawnCard)) {
			throw new RuntimeException("Invalid action given");
		}
		return action;
	}

	private void discardCard(Card drawnCard, Action action) {
		UnaryOperator<PlayerState> mapper;
		if (action.card == state.getPlayerState(state.getWhoseTurn()).hand) {
			mapper = p -> p.replaceAndDiscardHand(drawnCard);
		} else {
			mapper = p -> p.addToDiscard(drawnCard);
		}
		state = mapPlayer(state, state.getWhoseTurn(), mapper);
	}

	private static GameState mapPlayer(GameState state, PlayerNumber whoseTurn, UnaryOperator<PlayerState> mapper) {
		// return .replacePlayer(playerNum, mapper.apply(this.getPlayerState(playerNum)));
		if (whoseTurn == PlayerNumber.PLAYER_1) {
			return state.toBuilder().mapPlayer1(mapper).build();
		} else {
			return state.toBuilder().mapPlayer2(mapper).build();
		}
	}

	public static boolean isValid(Action action, PublicGameState state, Card inHand,
			Card drawnCard) {
		if (action == null || action.player != state.whoseTurn || (action.card != drawnCard && action.card != inHand)) {
			return false;
		}
		// If Countess is caught with King or Prince, discard Countess
		if ((drawnCard == Card.COUNTESS || inHand == Card.COUNTESS) &&
				((drawnCard == Card.KING || inHand == Card.KING) ||
						(drawnCard == Card.PRINCE || inHand == Card.PRINCE))) {
			return action.card == Card.COUNTESS;
		}

		if (action.targetPlayer.isPresent() && state.isPlayerProtected(action.targetPlayer.get())) {
			return false;
		}
		// unimplemented: rest
		return true;
	}

	private static GameState applyAction(Action action, GameState state) {
		return applyActionHelper(action, state.toBuilder()).build();
	}

	private static GameState.Builder applyActionHelper(Action action, GameState.Builder state) {
		//throw new UnsupportedOperationException(); // not implemented
		switch (action.card) {
		case PRINCESS: 	PlayerNumber other = action.player.other();
						return state.setWinner(other);
		case COUNTESS:	return state;
		case KING:		Card temp = state.getPlayer1().hand;
						return state
							.mapPlayer1(p -> p.replaceHand(state.getPlayer2().hand))
							.mapPlayer2(p -> p.replaceHand(temp));
		case PRINCE:	return state.setWinner(action.targetPlayer.get().other());
		case HANDMAID:	return (action.player == PlayerNumber.PLAYER_1) ? 
							state.setPlayer1Protected(true) :
							state.setPlayer2Protected(true);
		case BARON:		int result = Comparator.<PlayerNumber, Card>comparing(p -> state.build().getPlayerState(p).hand)
								.compare(PlayerNumber.PLAYER_1, PlayerNumber.PLAYER_2);
						
						if (result > 0) {
							return state.setWinner(PlayerNumber.PLAYER_1);
						} else if (result < 0) {
							return state.setWinner(PlayerNumber.PLAYER_2);
						} else {
							return state;
						}
		case PRIEST:	return state; // TODO I don't know how to handle this
		case GUARD:		
						//return state.setWinner(Arrays.stream(PlayerNumber.values()).max(Comparator.comparing(p -> state.build().getPlayerState(p).hand)));
		default:		return state;
		}
	}

	private GameState checkWin(GameState state) {
		if (state.getDeck().size() == 0) { // "a round ends if the deck is empty at the end of a turn"
			Comparator<PlayerNumber> comparator = Comparator.comparing(state::getPlayerState,

			// "player with highest ranked person wins the round"
					Comparator.<PlayerState, Card> comparing(s -> s.hand)

			// "In case of a tie, the player who discarded the highest total value of cards wins"
							.thenComparing(s -> s.discardPile.stream().mapToInt(c -> c.value).sum()))
					.thenComparing(Comparator.naturalOrder()); // last resort, compare by player number

			Optional<PlayerNumber> winner = Arrays.stream(PlayerNumber.values()).max(comparator);
			state = state.toBuilder().setWinner(winner).build();
			return state;
		} else {
			return state;
		}
	}
}
