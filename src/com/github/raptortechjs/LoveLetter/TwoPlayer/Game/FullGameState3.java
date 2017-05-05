package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.base.Preconditions;

@FreeBuilder
interface FullGameState3 extends GameState3 {
	//public Card player1Hand();
	//public Card player2Hand();
	public ImmutableMap<Player, Card> hands();
	public Optional<Card> drawnCard();
	
	public Card initialSetAside();
	public ImmutableList<Card> deck();
	
	default public Card hand(Player player) {
		return hands().get(player);
	}
	
	default public Card player1Hand() {
		return hand(Player.ONE);
	}
	
	default public Card player2Hand() {
		return hand(Player.TWO);
	}
	
	
	
	public Builder toFullBuilder();
	class Builder extends FullGameState3_Builder {
		public Builder() {
			drawnCard(Optional.empty());
		}
		
		@Override
		public FullGameState3 build() {
			deckSize(deck().size());
			hasJustDrawn(drawnCard().isPresent());
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
		builder.putHands(Player.ONE, drawDeck.remove(drawDeck.size()-1));
		builder.putHands(Player.TWO, drawDeck.remove(drawDeck.size()-1));
		//builder.player2Hand(drawDeck.remove(drawDeck.size()-1));
		//builder.setPlayer2(PlayerState.create(drawDeck.remove(drawDeck.size()-1)));
		
		builder.clearWinner(); // at the beginning of the game, nobody has won
		
		builder.whoseTurn(Player.ONE);
		builder.turnNumber(0);

		return builder.build();
	}
	
	private static void shuffle(List<Card> deck) {
		Collections.shuffle(deck);
	}
	
	
	public static FullGameState3 startTurn(FullGameState3 state) {
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		FullGameState3.Builder builder = state.toFullBuilder();
		endProtection(builder);
		drawCard(builder);
		
		return builder.build();
	}
	
	private static FullGameState3.Builder endProtection(FullGameState3.Builder builder) {
		if (builder.players().get(builder.whoseTurn()).isProtected()) {
			return builder.mutatePlayers(m -> m.compute(builder.whoseTurn(), (p, ps) -> ps.toBuilder().isProtected(false).build()));
		} else {
			return builder;
		}
	}
	
	private static FullGameState3.Builder drawCard(FullGameState3.Builder builder) {
		Card drawnCard = builder.deck().get(builder.deck().size() - 1);
		builder.drawnCard(drawnCard);
		builder.mutateDeck(l -> l.remove(builder.deck().size() - 1));
		return builder;
	}
	
	
	
	public static FullGameState3 endTurn(FullGameState3 state, Action action) {
		Preconditions.checkArgument(state.hasJustDrawn());
		
		Preconditions.checkArgument(isValid(action, state, state.hand(state.whoseTurn()), state.drawnCard().get()));
		
		FullGameState3.Builder builder = new FullGameState3.Builder();
		
		discardCard(builder, action);
		applyAction(builder, action);
		checkWin(builder);
		incrementTurn(builder);
		
		return builder.build();
	}
	
	private static FullGameState3.Builder discardCard(FullGameState3.Builder builder, Action action) {
		//Card hand = (action.player == Player.ONE) ? builder.player1Hand() : builder.player2Hand();
		if (action.card == builder.hands().get(action.player)) {
			builder.mutateHands(m -> m.put(action.player, builder.drawnCard().get()));//(c -> builder.drawnCard().get());
//			if (action.player == Player.ONE) {
//				builder.mutateHands(m -> m.put(action.player, builder.drawnCard().get()));//(c -> builder.drawnCard().get());
//			} else {
//				builder.mapPlayer2Hand(c -> builder.drawnCard().get());
//			}
		}
		
		builder.mutatePlayers(m -> m.compute(builder.whoseTurn(),
				(p, s) -> s.toBuilder().addDiscardPile(action.card).build()));
		
		return builder;
	}
	
	/* 
	 * Meaty; this contains the bulk of the per-card logic.
	 */
	private static FullGameState3.Builder applyAction(FullGameState3.Builder builder, Action action) {
		switch (action.card) {
		case PRINCESS: 	return builder.winner(action.player.other());
		case COUNTESS:	return builder;
		case KING:		Card temp = builder.hands().get(Player.ONE);
						return builder
							.putHands(Player.ONE, builder.hands().get(Player.TWO))
							.putHands(Player.TWO, temp);
							//.player2Hand(temp);
		case PRINCE:	return builder.winner(action.targetPlayer.get().other());
		case HANDMAID:	return builder.mutatePlayers(m -> m.compute(action.targetPlayer.get(),
							(p, s) -> s.toBuilder().isProtected(true).build()));
		case BARON:		int result = builder.hands().get(Player.ONE).compareTo(builder.hands().get(Player.TWO));
						return builder.winner(
								(result == 0) ? Optional.empty() :
								Optional.of((result > 0) ? Player.ONE : Player.TWO));
		case PRIEST:	return builder; // TODO I don't know how to handle this
		case GUARD:		//Card hand = (action.targetPlayer.get() == Player.ONE) ?
						//		builder.player1Hand() :
						//		builder.player2Hand();
						if (builder.hands().get(action.player) == action.targetCard.get()) {
							return builder.winner(action.targetPlayer.get().other());
						} else {
							return builder;
						}
		//default:		return builder;
		}
		return null;
	}
	
	private static FullGameState3.Builder checkWin(FullGameState3.Builder builder) {
		if (builder.deckSize() == 0) { // "a round ends if the deck is empty at the end of a turn"
			Comparator<Player> comparator =
					// "player with the highest ranked person wins the round"
					Comparator.<Player, Card>comparing(builder.hands()::get)
					
					// "In case of a tie, the player who discarded the highest total value of cards wins"
					.thenComparing(builder.players()::get,
							Comparator.comparingInt(s -> s.discardPile().stream().mapToInt(c -> c.value).sum()))
					
					 // last resort, compare by player number
					.thenComparing(Comparator.naturalOrder());
					
					//Comparator.comparing(p -> builder.players().get(p),

					// "player with highest ranked person wins the round"
							//Comparator.<PlayerState3, Card> comparing(s -> s.hand)

					// "In case of a tie, the player who discarded the highest total value of cards wins"
							//		.thenComparing(s -> s.discardPile().stream().mapToInt(c -> c.value).sum()))
							//.thenComparing(Comparator.naturalOrder()); // last resort, compare by player number
			/*Comparator<Player> comparator = Comparator.comparing(p -> builder.players().get(p),

			// "player with highest ranked person wins the round"
					Comparator.<PlayerState3, Card> comparing(s -> s.hand)

			// "In case of a tie, the player who discarded the highest total value of cards wins"
							.thenComparing(s -> s.discardPile().stream().mapToInt(c -> c.value).sum()))
					.thenComparing(Comparator.naturalOrder()); // last resort, compare by player number*/

			Optional<Player> winner = Arrays.stream(Player.values()).max(comparator);
			builder.winner(winner);
			return builder;
			//state = state.toBuilder().setWinner(winner).build();
			//return state;
		} else {
			return builder;
		}
	}
	
	private static FullGameState3.Builder incrementTurn(FullGameState3.Builder builder) {
		return builder;
	}
	
	public static boolean isValid(Action action, GameState3 state, Card inHand, Card drawnCard) {
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