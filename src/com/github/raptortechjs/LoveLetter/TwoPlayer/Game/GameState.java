package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GameState {
	/*public final PlayerState player1;
	public final PlayerState player2;
	
	/*public final ImmutableMap<PlayerNumber, Boolean> isProtected;/

	public final ImmutableList<Card> drawDeck;
	public final ImmutableList<Card> visibleDiscard;
	
	/*public final Card setAside;/
	
	public final Optional<PlayerNumber> winner;
	
	/*public final PlayerNumber whoseTurn;
	public final int numberOfTurns;/

	private GameState(PlayerState player1, PlayerState player2, /*ImmutableMap<PlayerNumber, Boolean> isProtected,/
			ImmutableList<Card> drawDeck, ImmutableList<Card> visibleDiscard, /*Card setAside,/
			Optional<PlayerNumber> winner/*, PlayerNumber whoseTurn, int numberOfTurns/) {
		this.player1 = player1;
		this.player2 = player2;
		this.drawDeck = drawDeck;
		this.visibleDiscard = visibleDiscard;
		this.winner = winner;
		/*this.isProtected = isProtected;/
	}

	public static GameState create(PlayerState player1, PlayerState player2, List<Card> drawDeck,
			List<Card> visibleDiscard, Optional<PlayerNumber> winner/*, Map<PlayerNumber, Boolean> isProtected/) {
		return new GameState(player1, player2, ImmutableList.copyOf(drawDeck),
				ImmutableList.copyOf(visibleDiscard), winner/*, ImmutableMap.copyOf(isProtected)/);
	}

	public GameState replacePlayer1(PlayerState newPlayer1) {
		return create(newPlayer1, player2, drawDeck, visibleDiscard, winner);
	}

	public GameState replacePlayer2(PlayerState newPlayer2) {
		return create(player1, newPlayer2, drawDeck, visibleDiscard, winner);
	}
	
	public GameState replacePlayer(PlayerNumber playerNum, PlayerState newPlayerState) {
		return playerNum == PlayerNumber.PLAYER_1 ? replacePlayer1(newPlayerState) : replacePlayer2(newPlayerState);
	}
	
	public GameState mapPlayer(PlayerNumber playerNum, Function<PlayerState, PlayerState> mapper) {
		return this.replacePlayer(playerNum, mapper.apply(this.getPlayerState(playerNum)));
	}

	public GameState replaceDrawDeck(List<Card> newDrawDeck) {
		return create(player1, player2, newDrawDeck, visibleDiscard, winner);
	}

	public Card peekAtDrawDeck() {
		return drawDeck.get(drawDeck.size() - 1);
	}

	public GameState removeTopCard() {
		return create(player1, player2, drawDeck.subList(0, drawDeck.size() - 1), visibleDiscard, winner);
	}
	
	public GameState setWinner(Optional<PlayerNumber> newWinner) {
		return create(player1, player2, drawDeck, visibleDiscard, newWinner);
	}

	/*private GameState2Player replaceVisibleDiscard(List<Card> newVisibleDiscard) {
		return create(player1, player2, drawDeck, newVisibleDiscard);
	}/

	public static GameState createNewGame() {
		// following rules from "Setup"

		List<Card> drawDeck = new ArrayList<>(Card.defaultDeck());

		Collections.shuffle(drawDeck); // "shuffle the 16 cards"

		drawDeck.remove(0); // "remove the top card of deck"

		List<Card> subList = drawDeck.subList(0, 3); // "in two-player games, take 3 more cards from the deck"
		ImmutableList<Card> visibleDiscard = ImmutableList.copyOf(subList); // "and place them to the side, face up"
		subList.clear(); // actually remove the cards from the draw deck

		// "Each player draws one card from the deck"
		PlayerState player1 = PlayerState.create(drawDeck.remove(0));
		PlayerState player2 = PlayerState.create(drawDeck.remove(0));
		
		Optional<PlayerNumber> winner = Optional.empty(); // at the beginning of the game, nobody has won

		return create(player1, player2, drawDeck, visibleDiscard, winner);
	}
	
	public Optional<PlayerNumber> getOptionalWinner() {
		return winner;
	}
	
	public boolean hasAnyoneWon() {
		return winner.isPresent();
	}
	
	public PlayerNumber getWinner() {
		return winner.get();
	}
	
	public PublicGameState getPublicState() {
		return new PublicGameState(player1.discardPile, player2.discardPile, visibleDiscard, winner, drawDeck.size());
	}
	
	public PlayerState getPlayerState(PlayerNumber player) {
		return (player == PlayerNumber.PLAYER_1 ? player1 : player2);
	}*/
}
