package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.collect.ImmutableList;

@FreeBuilder
public interface GameState2 {
	PlayerState getPlayer1();
	PlayerState getPlayer2();
	
	default public PlayerState getPlayerState(PlayerNumber playerNum) {
		return (playerNum == PlayerNumber.PLAYER_1) ? getPlayer1() : getPlayer2();
	}
	
	boolean isPlayer1Protected();
	boolean isPlayer2Protected();
	
	default public boolean isPlayerProtected(PlayerNumber playerNum) {
		return (playerNum == PlayerNumber.PLAYER_1) ? isPlayer1Protected() : isPlayer2Protected();
	}

	ImmutableList<Card> getDeck();
	
	default public Card peekAtDeck() {
		return getDeck().get(getDeck().size() - 1);
	}
	
	default public GameState2 withoutTopCard() {
		return this.toBuilder().mutateDeck(l -> l.remove(l.size() - 1)).build();
	}
	
	ImmutableList<Card> getVisibleDiscard();
	
	Card getSetAside();
	
	Optional<PlayerNumber> getWinner();
	
	default public boolean hasAnyoneWon() {
		return getWinner().isPresent();
	}
	
	PlayerNumber getWhoseTurn();
	int getTurnNumber();
	
	
	Builder toBuilder();
	
	class Builder extends GameState2_Builder {}
	
	default public PublicGameState getPublicState() {
		return new PublicGameState(getPlayer1().discardPile, getPlayer2().discardPile,
				isPlayer1Protected(), isPlayer2Protected(),
				getVisibleDiscard(), getWinner(), getWhoseTurn(), getDeck().size());
	}
	
	public static GameState2 createNewGame() {
		// following rules from "Setup"
		
		GameState2.Builder builder = new GameState2.Builder();

		List<Card> drawDeck = new ArrayList<>(Card.defaultDeck());

		//Collections.shuffle(drawDeck); // "shuffle the 16 cards"
		shuffle(drawDeck);

		builder.setSetAside(drawDeck.remove(drawDeck.size()-1)); // "remove the top card of deck" (and keep it for later)

		//List<Card> subList = drawDeck.subList(drawDeck.size()-3, drawDeck.size()); // "in two-player games, take 3 more cards from the deck"
		// TODO: temporarily, we're increasing the amount drawn at the beginning to decrease the total number of games
		List<Card> subList = drawDeck.subList(drawDeck.size()-3, drawDeck.size());
		
		builder.addAllVisibleDiscard(ImmutableList.copyOf(subList)); // "and place them to the side, face up"
		subList.clear(); // actually remove the cards from the draw deck
		
		builder.addAllDeck(drawDeck);

		// "Each player draws one card from the deck"
		builder.setPlayer1(PlayerState.create(drawDeck.remove(drawDeck.size()-1)));
		builder.setPlayer2(PlayerState.create(drawDeck.remove(drawDeck.size()-1)));
		
		builder.setPlayer1Protected(false);
		builder.setPlayer2Protected(false);
		
		builder.clearWinner(); //Optional.empty(); // at the beginning of the game, nobody has won
		
		builder.setWhoseTurn(PlayerNumber.PLAYER_1);
		//builder.setNumberOfTurns(0);
		builder.setTurnNumber(0);
		
		//System.out.println(builder.getDeck().size());

		return builder.build();
		//return create(player1, player2, drawDeck, visibleDiscard, winner);
	}
	
	static void shuffle(List<Card> deck) {
		Collections.shuffle(deck);
	}
}
