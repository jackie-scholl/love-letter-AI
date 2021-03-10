package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

@FreeBuilder
public abstract class FullGameState implements GameState {
	public static final boolean TARGET_CARD_CONTROLS_PRINCE_PICK = true;
	
	abstract public ImmutableMap<Player, Card> hands();

	abstract public Optional<Card> drawnCard();

	abstract public Optional<Multiset<Card>> optionalDeck();

	abstract public Optional<FullGameState> lastHalfStep();

	public Multiset<Card> deck() {
		return optionalDeck().get();
	}

	public Card hand(Player player) {
		return hands().get(player);
	}

	public Card player1Hand() {
		return hand(Player.ONE);
	}

	public Card player2Hand() {
		return hand(Player.TWO);
	}
	

	public boolean equals(Object obj) {
		if (!(obj instanceof FullGameState)) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		FullGameState other = (FullGameState) obj;
		
		if (this.hashCode != 0 && other.hashCode != 0 && this.hashCode != other.hashCode) {
			return false;
		}
		
		boolean equals1;
		if (!this.lastHalfStep().isPresent()) {
			equals1 = Objects.equals(players(), other.players())
					&& Objects.equals(visibleDiscard(), other.visibleDiscard())
					&& Objects.equals(hands(), other.hands())
					&& Objects.equals(drawnCard(), other.drawnCard())
					&& Objects.equals(thisAction(), other.thisAction())
					&& Objects.equals(lastHalfStep(), other.lastHalfStep());
		} else {
			equals1 = Objects.equals(lastHalfStep(), other.lastHalfStep())
					&& Objects.equals(drawnCard(), other.drawnCard())
					&& Objects.equals(thisAction(), other.thisAction());
		}
		
		return equals1;
	}
	
	private int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			if (lastHalfStep().isPresent()) {
				hashCode = Objects.hash(
						drawnCard(),
						thisAction(),
						lastHalfStep());
			} else {
				hashCode = Objects.hash(
						players(),
						visibleDiscard(),
						hands(),
						drawnCard(),
						thisAction(),
						lastHalfStep());
			}
		}
		return hashCode;
	}

	public GameState.Builder toBuilder() {
		return new GameState.Builder().mergeFrom(this);
	}

	public Builder toFullBuilder() {
		return new Builder().mergeFrom(this);
	}

	static class Builder extends FullGameState_Builder {
		public Builder() {
			drawnCard(Optional.empty());
		}

		public Multiset<Card> deck() {
			return optionalDeck().get();
		}

		@Override
		public FullGameState build() {
			deckSize(deck().size() - 1);

			hasJustDrawn(drawnCard().isPresent());
			FullGameState state = super.build();
			return state;
		}

		public Card getRandomFromDeck() {
			return FGS3Helper.getRandomFromDeck(deck());
		}

		public FullGameState.Builder removeCard(Card cardToRemove) {
			return this.mapOptionalDeck(optDeck -> {
				Multiset<Card> ms = optDeck;
				ms = HashMultiset.create(ms);
				boolean b = ms.remove(cardToRemove);
				return Multisets.unmodifiableMultiset(ms);
			});
		}

		// private
	}

	public static FullGameState createNewGame() {
		return FGS3Helper.createNewGame();
	}

	public static FullGameState createNewGame(Optional<List<Card>> visibleDiscardOpt) {
		return FGS3Helper.createNewGame(visibleDiscardOpt);
	}

	public static FullGameState createNewGame(List<Card> visibleDiscard, Card player1Hand, Card player2Hand) {
		return FGS3Helper.createNewGame(visibleDiscard, player1Hand, player2Hand);
	}

	public GameState getPublicState() {
		return toBuilder().lastHalfStep(lastHalfStep().map(FullGameState::getPublicState)).build();
	}

	public FullGameState startTurn() {
		return FGS3Helper.startTurn(this);
	}

	public FullGameState startTurn(Card drawnCard) {
		return FGS3Helper.startTurn(this, drawnCard);
	}

	public FullGameState endTurn(Action action) {
		return FGS3Helper.endTurn(this, action);
	}

	public boolean isValid(Action action) {
		return FGS3Helper.isValid(this, action);
	}

}

class FGS3Helper {
	private static final Random r = new Random();

	public static Card getRandomFromDeck(Multiset<Card> deck) {
		List<Card> l = new ArrayList<>(deck);
		int index = r.nextInt(l.size());
		return l.get(index);
	}

	public static FullGameState createNewGame() {
		return createNewGame(Optional.empty());
	}

	public static FullGameState createNewGame(Optional<List<Card>> visibleDiscardOpt) {
		Multiset<Card> deck = HashMultiset.create(Card.defaultDeckMultiset());
		List<Card> visibleDiscard;
		if (visibleDiscardOpt.isPresent()) {
			visibleDiscard = visibleDiscardOpt.get();
			for (Card c : visibleDiscard) {
				deck.remove(c);
			}
		} else {
			visibleDiscard = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				Card c = getRandomFromDeck(deck);
				visibleDiscard.add(c);
				deck.remove(c);
			}
		}
		visibleDiscard = Collections.unmodifiableList(visibleDiscard);

		Card player1Hand = getRandomFromDeck(deck);
		deck.remove(player1Hand);

		Card player2Hand = getRandomFromDeck(deck);
		deck.remove(player2Hand);

		return createNewGame(visibleDiscard, player1Hand, player2Hand);
	}


	// following rules from "Setup"
	public static FullGameState createNewGame(List<Card> visibleDiscard, Card player1Hand, Card player2Hand) {
		Preconditions.checkNotNull(visibleDiscard);
		// Preconditions.checkArgument(visibleDiscard.size() == 3);
		Preconditions.checkNotNull(player1Hand);
		Preconditions.checkNotNull(player2Hand);

		visibleDiscard = ImmutableList.copyOf(visibleDiscard);

		FullGameState.Builder builder = new FullGameState.Builder();

		HashMultiset<Card> tempDeck = HashMultiset.create(Card.defaultDeckMultiset());

		// "in two-player games, take 3 more cards from the deck and place them to the side, face up"
		for (Card c : visibleDiscard) {
			tempDeck.remove(c);
		}
		builder.addAllVisibleDiscard(visibleDiscard);

		builder.putPlayers(Player.ONE, PlayerState3.defaultPlayerState());
		builder.putPlayers(Player.TWO, PlayerState3.defaultPlayerState());

		// "Each player draws one card from the deck"
		builder.putHands(Player.ONE, player1Hand);
		builder.putHands(Player.TWO, player2Hand);

		tempDeck.remove(player1Hand);
		tempDeck.remove(player2Hand);

		builder.optionalDeck(Multisets.unmodifiableMultiset(tempDeck));

		builder.clearWinner(); // at the beginning of the game, nobody has won

		builder.whoseTurn(Player.ONE);
		builder.turnNumber(0);
		
		builder.clearLastHalfStep();
		
		builder = checkWin(builder);

		FullGameState state = builder.build();
		
		return state;
	}

	public static FullGameState startTurn(FullGameState state) {
		Preconditions.checkArgument(!state.hasJustDrawn());

		FullGameState.Builder builder = state.toFullBuilder();
		endProtection(builder);
		drawCard(builder);
		
		builder.thisAction(Optional.empty());
		builder.lastHalfStep(state);

		return builder.build();
	}

	public static FullGameState startTurn(FullGameState state, Card chosenCard) {
		Preconditions.checkArgument(!state.hasJustDrawn());

		FullGameState.Builder builder = state.toFullBuilder();
		endProtection(builder);
		drawCard(builder, chosenCard);
		
		builder.thisAction(Optional.empty());
		builder.lastHalfStep(state);

		return builder.build();
	}

	private static FullGameState.Builder endProtection(FullGameState.Builder builder) {
		if (builder.players().get(builder.whoseTurn()).isProtected()) {
			return builder.mutatePlayers(
					m -> m.compute(builder.whoseTurn(), (p, ps) -> ps.toBuilder().isProtected(false).build()));
		} else {
			return builder;
		}
	}

	private static FullGameState.Builder drawCard(FullGameState.Builder builder) {
		Preconditions.checkArgument(builder.deckSize() > 0);
		// Card drawnCard = builder.deck().get(builder.deck().size() - 1);

		Card drawnCard = builder.getRandomFromDeck();
		builder.drawnCard(drawnCard);
		builder.removeCard(drawnCard);
		return builder;
	}

	private static FullGameState.Builder drawCard(FullGameState.Builder builder, Card drawnCard) {
		builder.drawnCard(drawnCard);
		builder.removeCard(drawnCard);
		return builder;
	}

	public static FullGameState endTurn(FullGameState state, Action action) {
		Preconditions.checkArgument(state.hasJustDrawn());

		Preconditions.checkArgument(isValid(state, action/*, state.hand(state.whoseTurn()), state.drawnCard().get()*/));

		FullGameState.Builder builder = state.toFullBuilder();

		discardCard(builder, action);
		applyAction(builder, action);

		builder.thisAction(action);
		builder.lastHalfStep(state);

		checkWin(builder);
		incrementTurn(builder);

		return builder.build();
	}

	private static FullGameState.Builder discardCard(FullGameState.Builder builder, Action action) {
		if (action.card == builder.hands().get(action.player)) {
			builder.mutateHands(m -> m.put(action.player, builder.drawnCard().get()));
		}

		builder.mutatePlayers(m -> m.compute(builder.whoseTurn(),
				(p, s) -> s.toBuilder().addDiscardPile(action.card).build()));

		builder.drawnCard(Optional.empty());

		return builder;
	}

	/* 
	 * Meaty; this contains the bulk of the per-card logic.
	 */
	private static FullGameState.Builder applyAction(FullGameState.Builder builder, Action action) {
		switch (action.card) {
		case PRINCESS:
			return builder.winner(action.player.other());
		case COUNTESS:
			return builder;
		case KING:
			Card temp = builder.hands().get(Player.ONE);
			return builder
					.putHands(Player.ONE, builder.hands().get(Player.TWO))
					.putHands(Player.TWO, temp);
		case PRINCE:
			Card drawn;
			if (FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK) {
				drawn = action.targetCard.get();
				Preconditions.checkArgument(builder.deck().contains(drawn));
			} else {
				drawn = builder.getRandomFromDeck();
			}
			builder.removeCard(drawn);
			Card inHand = builder.hands().get(action.targetPlayer.get());
			builder.mutatePlayers(m -> m.compute(action.targetPlayer.get(),
					(p, s) -> s.toBuilder().addDiscardPile(inHand).build()));
			if (inHand == Card.PRINCESS) {
				builder.winner(action.targetPlayer.map(Player::other));
			} else {
				builder.mutateHands(m -> m.put(action.targetPlayer.get(), drawn));
			}
		case HANDMAID:
			return builder.mutatePlayers(m -> m.compute(action.player,
					(p, s) -> s.toBuilder().isProtected(true).build()));
		case BARON:
			int result = builder.hands().get(action.player).compareTo(builder.hands().get(action.targetPlayer.get()));
			return builder.winner(
					(result == 0) ? Optional.empty()
							: Optional.of((result < 0) ? action.player : action.targetPlayer.get()));
		case PRIEST:
			return builder; // TODO I don't know how to handle this
		case GUARD:
			if (builder.hands().get(action.targetPlayer.get()) == action.targetCard.get()) {
				return builder.winner(action.targetPlayer.get().other());
			} else {
				return builder;
			}
		}
		return null;
	}

	private static FullGameState.Builder checkWin(FullGameState.Builder builder) {
		if (builder.winner().isPresent()) {
			return builder;
		}
		if (builder.deck().size() <= 1) { // "a round ends if the deck is empty at the end of a turn"
			Comparator<Player> comparator =
			// "player with the highest ranked person wins the round"
			Comparator.<Player, Card> comparing(builder.hands()::get).reversed()

			// "In case of a tie, the player who discarded the highest total value of cards wins"
					.thenComparing(builder.players()::get,
							Comparator.comparingInt(s -> s.discardPile().stream().mapToInt(c -> c.value).sum()))

			// last resort, compare by player number
					.thenComparing(Comparator.naturalOrder());

			Player winner = Arrays.stream(Player.values()).max(comparator).get();
			builder.winner(winner);
			return builder;
		} else {
			return builder;
		}
	}

	private static FullGameState.Builder incrementTurn(FullGameState.Builder builder) {
		return builder.mapWhoseTurn(Player::other).mapTurnNumber(i -> i + 1);
	}

	public static boolean isValid(FullGameState state, Action action) {
		return state.isValid(action, state.hand(action.player), state.drawnCard().get());
	}
}
