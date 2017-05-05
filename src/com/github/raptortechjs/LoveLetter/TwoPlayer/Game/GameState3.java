package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

@FreeBuilder
public interface GameState3 {
	public ImmutableMap<Player, PlayerState3> players();
	
	public ImmutableList<Card> visibleDiscard();
	
	public Optional<Player> winner();
	
	public Player whoseTurn();
	public int turnNumber();
	public boolean hasJustDrawn();
	public int deckSize();


	default public PlayerState3 state(Player player) {
		return players().get(player);
	}
	
	default public PlayerState3 player1() {
		return state(Player.ONE);
	}
	
	default public PlayerState3 player2() {
		return state(Player.TWO);
	}
	
	public Builder toBuilder();
	class Builder extends GameState3_Builder {
		public Builder() {
			hasJustDrawn(false);
			winner(Optional.empty());
		}
		
		@Override
		public GameState3 build() {
			GameState3 state = super.build();
			Preconditions.checkState(state.players().keySet().equals(ImmutableSet.of(Player.ONE, Player.TWO)));
			Preconditions.checkState(state.visibleDiscard().size() == 3);
			//Preconditions.checkState(state.turnNumber() >= 0);
			//Preconditions.checkState(state.deckSize() >= 0);
			return state;
		}
	}
	
	default PublicGameState toPublicGameState() { return GS3Helper.toPublicGameState(this); }
	
	static GameState3 fromPublicGameState(PublicGameState state) { return GS3Helper.fromPublicGameState(state); }
	
	default ImmutableMultiset<Card> getCombinedDiscard() { return GS3Helper.getCombinedDiscard(this); }
	
	default ImmutableMultiset<Card> remainingCards() { return GS3Helper.remainingCards(this); }
	
	public default boolean isValid(Action action, Card inHand, Card drawnCard) {
		return GS3Helper.isValid(action, this, inHand, drawnCard);
	}
}



class GS3Helper {
	static PublicGameState toPublicGameState(GameState3 s) {
		return new PublicGameState(s.player1().discardPile(), s.player2().discardPile(),
				s.player1().isProtected(), s.player2().isProtected(),
				s.visibleDiscard(), s.winner(), s.whoseTurn(), s.deckSize());
	}
	
	static GameState3 fromPublicGameState(PublicGameState state) {
		return new GameState3.Builder()
				.putPlayers(Player.ONE, 
						new PlayerState3.Builder()
						.addAllDiscardPile(state.player1Discard)
						.isProtected(state.isPlayer1Protected).build())
				.putPlayers(Player.TWO, 
						new PlayerState3.Builder()
						.addAllDiscardPile(state.player2Discard)
						.isProtected(state.isPlayer2Protected).build())
				.addAllVisibleDiscard(state.visibleDiscard)
				.winner(state.winner)
				.whoseTurn(state.whoseTurn)
				.turnNumber(-1) // NOTE
				.hasJustDrawn(false)
				.deckSize(-1)
				.build();
	}
	
	
	static ImmutableMultiset<Card> getCombinedDiscard(GameState3 s) {
		return ImmutableMultiset.<Card>builder()
				.addAll(s.visibleDiscard())
				.addAll(s.player1().discardPile())
				.addAll(s.player2().discardPile())
				.build();
	}
	
	static ImmutableMultiset<Card> remainingCards(GameState3 s) {
		Multiset<Card> temp = HashMultiset.<Card>create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, s.getCombinedDiscard());
		return ImmutableMultiset.copyOf(temp);
	}
	
	public static <T> Map<T, Double> multisetToNormalizedFrequencyMap(Multiset<T> multiset) {
		ImmutableMap.Builder<T, Double> frequencyMap = ImmutableMap.<T, Double>builder();
		for (Multiset.Entry<T> e : multiset.entrySet()) {
			double frequency = e.getCount() / multiset.size();
			frequencyMap.put(e.getElement(), frequency);
		}
		//frequencyMap.
		return frequencyMap.build();
	}
	
	
	static boolean isValid(Action action, GameState3 state, Card inHand, Card drawnCard) {
		if (action == null || action.player != state.whoseTurn() || (action.card != drawnCard && action.card != inHand)) {
			return false;
		}
		// If Countess is caught with King or Prince, discard Countess
		if ((drawnCard == Card.COUNTESS || inHand == Card.COUNTESS) &&
				((drawnCard == Card.KING || inHand == Card.KING) ||
						(drawnCard == Card.PRINCE || inHand == Card.PRINCE))) {
			return action.card == Card.COUNTESS;
		}

		if (action.targetPlayer.isPresent() && state.state(action.targetPlayer.get()).isProtected()) {
			return false;
		}
		// TODO: Is there more to do here?
		return true;
	}
}
