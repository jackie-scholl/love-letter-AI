package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

public class PublicGameState {
	public final ImmutableList<Card> player1Discard;
	public final ImmutableList<Card> player2Discard;
	public final boolean isPlayer1Protected;
	public final boolean isPlayer2Protected;
	
	public final ImmutableList<Card> visibleDiscard;
	public final Optional<PlayerNumber> winner;
	
	public final PlayerNumber whoseTurn;
	
	public final int deckSize;

	public PublicGameState(ImmutableList<Card> player1Discard, ImmutableList<Card> player2Discard,
			boolean isPlayer1Protected, boolean isPlayer2Protected, 
			ImmutableList<Card> visibleDiscard, Optional<PlayerNumber> winner, PlayerNumber whoseTurn, int deckSize) {
		this.player1Discard = player1Discard;
		this.player2Discard = player2Discard;
		this.isPlayer1Protected = isPlayer1Protected;
		this.isPlayer2Protected = isPlayer2Protected;
		this.visibleDiscard = visibleDiscard;
		this.winner = winner;
		this.whoseTurn = whoseTurn;
		this.deckSize = deckSize;
	}
	
	public boolean isPlayerProtected(PlayerNumber playerNum) {
		return (playerNum == PlayerNumber.PLAYER_1) ? isPlayer1Protected : isPlayer2Protected;
	}
}