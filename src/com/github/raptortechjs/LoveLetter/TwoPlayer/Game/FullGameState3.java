package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.FullGameState3.Builder;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;

@FreeBuilder
interface FullGameState3 extends GameState3 {
	public Card player1Hand();
	public Card player2Hand();
	public Optional<Card> justDrawn();
	
	public Card initialSetAside();
	public ImmutableList<Card> deck();
	
	
	public Builder toFullBuilder();
	class Builder extends FullGameState3_Builder {
		public Builder() {
			justDrawn(Optional.empty());
		}
		
		@Override
		public FullGameState3 build() {
			deckSize(deck().size());
			cardDrawn(justDrawn().isPresent());
			FullGameState3 state = super.build();
			//Preconditions.checkState(state.deckSize() == state.deck().size());
			//Preconditions.checkState(state.cardDrawn() == state.justDrawn().isPresent());
			return state;
		}
	}
	
	default public GameState toGameState() {
		return FGS3Helper.toGameState(this);
	}
	
	public static FullGameState3 createNewGame() { return FGS3Helper.createNewGame(); }
	
}

class FGS3Helper {
	static GameState toGameState(FullGameState3 s) {
		GameState.Builder b = new GameState.Builder();

		b.setPlayer1(s.state(Player.ONE).toPlayerState(s.player1Hand()));
		b.setPlayer1Protected(s.state(Player.ONE).isProtected());
		
		b.setPlayer2(s.state(Player.ONE).toPlayerState(s.player1Hand()));
		b.setPlayer2Protected(s.state(Player.TWO).isProtected());
		
		
		b.addAllDeck(s.deck());
		b.addAllVisibleDiscard(s.visibleDiscard());
		
		b.setSetAside(s.initialSetAside());
		b.setWinner(s.winner());
		
		b.setWhoseTurn(s.whoseTurn());
		b.setTurnNumber(s.turnNumber());
		
		return b.build();
	}
	
	public static FullGameState3 createNewGame() {
		// following rules from "Setup"
		
		FullGameState3.Builder builder = new FullGameState3.Builder();

		List<Card> drawDeck = new ArrayList<>(Card.defaultDeckList());
		shuffle(drawDeck); // "shuffle the 16 cards"

		builder.initialSetAside(drawDeck.remove(drawDeck.size()-1)); // "remove the top card of deck" (and keep it for later)

		List<Card> subList = drawDeck.subList(drawDeck.size()-3, drawDeck.size()); // "in two-player games, take 3 more cards from the deck"
		builder.addAllVisibleDiscard(ImmutableList.copyOf(subList)); // "and place them to the side, face up"
		subList.clear(); // actually remove the cards from the draw deck
		
		builder.addAllDeck(drawDeck);
		

		builder.putPlayers(Player.ONE, PlayerState3.defaultPlayerState());
		builder.putPlayers(Player.TWO, PlayerState3.defaultPlayerState());

		// "Each player draws one card from the deck"
		builder.player1Hand(drawDeck.remove(drawDeck.size()-1));
		builder.player2Hand(drawDeck.remove(drawDeck.size()-1));
		//builder.setPlayer2(PlayerState.create(drawDeck.remove(drawDeck.size()-1)));
		
		builder.clearWinner(); // at the beginning of the game, nobody has won
		
		builder.whoseTurn(Player.ONE);
		builder.turnNumber(0);

		return builder.build();
	}
	
	static void shuffle(List<Card> deck) {
		Collections.shuffle(deck);
	}
}

/*
GameState.Builder b = new GameState.Builder();

b.setPlayer1(state(Player.ONE).toPlayerState(player1Hand()));
b.setPlayer1Protected(state(Player.ONE).isProtected());

b.setPlayer2(state(Player.ONE).toPlayerState(player1Hand()));
b.setPlayer2Protected(state(Player.TWO).isProtected());


b.addAllDeck(deck());
b.addAllVisibleDiscard(visibleDiscard());

b.setSetAside(initialSetAside());
b.setWinner(winner());

b.setWhoseTurn(whoseTurn());
b.setTurnNumber(turnNumber());

return b.build();
*/