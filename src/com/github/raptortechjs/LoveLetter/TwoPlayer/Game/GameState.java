package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;
import java.util.stream.Stream;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

@FreeBuilder
public interface GameState {
	public ImmutableMap<Player, PlayerState3> players();
	
	public ImmutableList<Card> visibleDiscard();
	
	public Optional<Player> winner();
	
	public Player whoseTurn();
	public int turnNumber();
	public boolean hasJustDrawn();
	public int deckSize();
	
	//public List<Action> history();
	public Optional<Action> thisAction();
	
	public Optional<? extends GameState> lastHalfStep();
	//public GameState lastHalfStep();


	default public PlayerState3 playerState(Player player) {
		return players().get(player);
	}
	
	default public PlayerState3 player1() {
		return playerState(Player.ONE);
	}
	
	default public PlayerState3 player2() {
		return playerState(Player.TWO);
	}
	
	public Builder toBuilder();
	class Builder extends GameState_Builder {
		public Builder() {
			hasJustDrawn(false);
			winner(Optional.empty());
		}
		
		@Override
		public GameState build() {
			GameState state = super.build();
			Preconditions.checkState(state.players().keySet().equals(ImmutableSet.of(Player.ONE, Player.TWO)));
			//Preconditions.checkState(state.visibleDiscard().size() == 3);
			Preconditions.checkState(state.turnNumber() >= 0);
			//Preconditions.checkState(state.deckSize() >= 0);
			//assert state.turnNumber() == state.history().size();
			return state;
		}
	}
	
	
	default ImmutableMultiset<Card> getCombinedDiscard() { return GS3Helper.getCombinedDiscard(this); }
	
	default ImmutableMultiset<Card> remainingCards() { return GS3Helper.remainingCards(this); }
	
	public default boolean isValid(Action action, Card inHand, Card drawnCard) {
		return GS3Helper.isValid(action, this, inHand, drawnCard);
	}
	
	public default List<Action> history() {
		/*List<Action> currentHistory = thisAction().isPresent() ?
				ImmutableList.of(thisAction().get()) : ImmutableList.of();
		if (!lastHalfStep().isPresent()) {
			return currentHistory;
		}
		return ImmutableList.<Action>builder().addAll(lastHalfStep().get().history()).addAll(currentHistory).build();*/
		return historyBuilder().build();
	}
	
	default ImmutableList.Builder<Action> historyBuilder() {
		ImmutableList.Builder<Action> builder;
		if (lastHalfStep().isPresent()) {
			builder = lastHalfStep().get().historyBuilder();
		} else {
			builder = ImmutableList.builder();
		}
		if (thisAction().isPresent()) {
			builder.add(thisAction().get());
		}
		return builder;
	}
	
	/*default GameState constructPrevious() {
		if (hasJustDrawn()) {
			return this.toBuilder().hasJustDrawn(false).mapDeckSize(i -> i + 1).clearWinner().build();
		} else {
			Preconditions.checkState(!history().isEmpty());
			
		}
	}*/
	
	public static ImmutableMultiset<Card> remaining(ImmutableMultiset<Card> ms) {
		Multiset<Card> temp = HashMultiset.<Card>create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, ms);
		return ImmutableMultiset.copyOf(temp);
	}
}



class GS3Helper {
	static ImmutableMultiset<Card> getCombinedDiscard(GameState s) {
		return ImmutableMultiset.<Card>builder()
				.addAll(s.visibleDiscard())
				.addAll(s.player1().discardPile())
				.addAll(s.player2().discardPile())
				.build();
	}
	
	static ImmutableMultiset<Card> remainingCards(GameState s) {
		Multiset<Card> temp = HashMultiset.<Card>create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, s.getCombinedDiscard());
		return ImmutableMultiset.copyOf(temp);
	}
	
	
	
	
	static boolean isValid(Action action, GameState state, Card inHand, Card drawnCard) {
		if (action == null || action.player != state.whoseTurn() || (action.card != drawnCard && action.card != inHand)) {
			return false;
		}
		// If Countess is caught with King or Prince, discard Countess
		if ((drawnCard == Card.COUNTESS || inHand == Card.COUNTESS) &&
				((drawnCard == Card.KING || inHand == Card.KING) ||
						(drawnCard == Card.PRINCE || inHand == Card.PRINCE))) {
			return action.card == Card.COUNTESS;
		}

		// If the targeted player is protected, invalid action
		if (action.targetPlayer.isPresent() && state.playerState(action.targetPlayer.get()).isProtected()) {
			return false;
		}
		
		if (action.card == Card.GUARD && action.targetCard.get() == Card.GUARD) {
			return false;
		}
		
		// TODO: Is there more to do here?
		return true;
	}
	
	
}
