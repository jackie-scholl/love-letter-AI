package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.*;

import org.inferred.freebuilder.FreeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

@FreeBuilder
public interface FullGameState3 extends GameState3 {
	public ImmutableMap<Player, Card> hands();
	public Optional<Card> drawnCard();
	
	//public Card initialSetAside();
	//public ImmutableList<Card> deck();
	//public ImmutableMultiset<Card> deck();
	public Optional<Multiset<Card>> optionalDeck();
	
	default public Multiset<Card> deck() {
		return optionalDeck().get();
	}
	/*public Optional<Multiset<Card>> optionalInverseDeck();
	
	default public Multiset<Card> deck() {
		Multiset<Card> temp = HashMultiset.<Card>create(Card.defaultDeckMultiset());
		Multisets.removeOccurrences(temp, optionalInverseDeck().get());
		return Multisets.unmodifiableMultiset(temp);
	}*/
	
	default public Card hand(Player player) {
		return hands().get(player);
	}
	
	default public Card player1Hand() {
		return hand(Player.ONE);
	}
	
	default public Card player2Hand() {
		return hand(Player.TWO);
	}
	
	default public GameState3.Builder toBuilder() {
		return new GameState3.Builder().mergeFrom(this);
	}
	
	default public Builder toFullBuilder() {
		return new Builder().mergeFrom(this);
	}
	
	class Builder extends FullGameState3_Builder {
		public Builder() {
			drawnCard(Optional.empty());
			optionalDeck(Card.defaultDeckMultiset());
			//optionalInverseDeck(ImmutableMultiset.of());
			//this.dec
		}
		
		/*public ImmutableMultiset<Card> deck() {
			return optionalDeck().get();
		}*/
		
		//public 
		
		public Multiset<Card> deck() {
			return optionalDeck().get();
			/*Multiset<Card> temp = HashMultiset.<Card>create(Card.defaultDeckMultiset());
			Multisets.removeOccurrences(temp, optionalInverseDeck().get());
			return Multisets.unmodifiableMultiset(temp);*/
		}
		
		@Override
		public FullGameState3 build() {
			//deckSize(deck().size() - 1);
			//deckSize(optionalDeck().get().size() - 1);
			deckSize(deck().size() - 1);
			
			hasJustDrawn(drawnCard().isPresent());
			FullGameState3 state = super.build();
			//Preconditions.checkState(state.deckSize() == state.deck().size());
			//Preconditions.checkState(state.cardDrawn() == state.justDrawn().isPresent());
			return state;
		}
		
		public Card getRandomFromDeck() {
			//List<Card> l = new ArrayList<>(optionalDeck().get());
			List<Card> l = new ArrayList<>(deck());
			int index = (new Random()).nextInt(l.size());
			return l.get(index);
		}
		
		/*public FullGameState3.Builder addAllDeck(ImmutableMultiset<Card> elements) {
			Preconditions.checkNotNull(elements);
			System.out.println("hello");
			this.mutateDeck(ms -> ms.addAll(elements));
			//return addAllDeck(elements.spliterator());
			return this;
		  }*/
		
		public FullGameState3.Builder removeCard(Card cardToRemove) {
			/*return this.mutateDeck(ms -> {
				ms.remove(cardToRemove);
			});*/
			return this.mapOptionalDeck(optDeck -> {
				Multiset<Card> ms = HashMultiset.create(optDeck);
				boolean b = ms.remove(cardToRemove);
				//System.out.println(b);
				return Multisets.unmodifiableMultiset(ms);
				//return ImmutableMultiset.copyOf(ms);
				}
			);
			/*return this.mapOptionalInverseDeck(invDeck -> {
				Multiset<Card> ms = HashMultiset.create(invDeck);
				ms.add(cardToRemove);
				//boolean b = ms.remove(cardToRemove);
				//System.out.println(b);
				return ImmutableMultiset.copyOf(ms);
				}
			);*/
		}
	}
	
	
	public static FullGameState3 createNewGame() { return FGS3Helper.createNewGame(); }
	
	default public GameState3 getPublicState() { return toBuilder().build(); }
	
	default public FullGameState3 startTurn() { return FGS3Helper.startTurn(this); }
	
	default public FullGameState3 startTurn(Card drawnCard) { return FGS3Helper.startTurn(this, drawnCard); }
	
	default public FullGameState3 endTurn(Action action) { return FGS3Helper.endTurn(this, action); }
}



class FGS3Helper {
	public static FullGameState3 createNewGame() {
		// following rules from "Setup"
		
		FullGameState3.Builder builder = new FullGameState3.Builder();

		//List<Card> drawDeck = new ArrayList<>(Card.defaultDeckList());
		//shuffle(drawDeck); // "shuffle the 16 cards"
		
		//System.out.println(drawDeck);

		//builder.initialSetAside(drawDeck.remove(drawDeck.size()-1)); // "remove the top card of deck" (and keep it for later)

		//List<Card> subList = drawDeck.subList(drawDeck.size()-3, drawDeck.size()); // "in two-player games, take 3 more cards from the deck"
		//builder.addAllVisibleDiscard(ImmutableList.copyOf(subList)); // "and place them to the side, face up"
		//subList.clear(); // actually remove the cards from the draw deck
		for (int i=0; i<3; i++) {
			Card c = builder.getRandomFromDeck();
			builder.addVisibleDiscard(c);
			builder.removeCard(c);
		}
		
		//builder.addAllDeck(drawDeck);
		//builder.optionalDeck(ImmutableMultiset.copyOf(drawDeck));
		//builder.removeCard(cardToRemove)
		/*for (Card c : subList) {
			builder.removeCard(c);
		}
		subList.clear();*/

		builder.putPlayers(Player.ONE, PlayerState3.defaultPlayerState());
		builder.putPlayers(Player.TWO, PlayerState3.defaultPlayerState());
		
		for (Player p : Player.values()) {
			Card c = builder.getRandomFromDeck();
			builder.putHands(p, c);
			builder.removeCard(c);
		}
		

		// "Each player draws one card from the deck"
		//builder.putHands(Player.ONE, drawDeck.remove(drawDeck.size()-1));
		//builder.putHands(Player.TWO, drawDeck.remove(drawDeck.size()-1));
		
		//builder.putHands(Player.ONE, drawDeck.remove(drawDeck.size()-1));
		//builder.putHands(Player.TWO, drawDeck.remove(drawDeck.size()-1));
		
		builder.clearWinner(); // at the beginning of the game, nobody has won
		
		builder.whoseTurn(Player.ONE);
		builder.turnNumber(0);

		FullGameState3 state = builder.build();
		//System.out.println(state.deckSize());
		//state.hash
		return state;
	}
	
	public static FullGameState3 startTurn(FullGameState3 state) {
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		FullGameState3.Builder builder = state.toFullBuilder();
		endProtection(builder);
		drawCard(builder);
		
		return builder.build();
	}
	
	public static FullGameState3 startTurn(FullGameState3 state, Card chosenCard) {
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		FullGameState3.Builder builder = state.toFullBuilder();
		endProtection(builder);
		drawCard(builder, chosenCard);
		
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
		Preconditions.checkArgument(builder.deckSize() > 0);
		//Card drawnCard = builder.deck().get(builder.deck().size() - 1);
		
		Card drawnCard = builder.getRandomFromDeck();
		builder.drawnCard(drawnCard);
		//builder.mutateDeck(l -> l.remove(builder.deck().size() - 1));
		//builder.mutateDeck(ms -> {boolean b = ms.remove(drawnCard); System.out.println(b);});
		/*builder.mapOptionalDeck(optDeck -> {
			Multiset<Card> ms = HashMultiset.create(optDeck);
			boolean b = ms.remove(drawnCard);
			System.out.println(b);
			return ImmutableMultiset.copyOf(ms);
			}
		);*/
		builder.removeCard(drawnCard);
		return builder;
	}
	
	private static FullGameState3.Builder drawCard(FullGameState3.Builder builder, Card drawnCard) {
		//Card drawnCard = builder.deck().get(builder.deck().size() - 1);
		builder.drawnCard(drawnCard);
		//System.out.println(x);
		//builder.mutateDeck(l -> l.remove(l.indexOf(drawnCard)));
		//builder.mutateDeck(ms -> {boolean b = ms.remove(drawnCard); /*System.out.println(b);*/});
		/*builder.mapOptionalDeck(optDeck -> {
			Multiset<Card> ms = HashMultiset.create(optDeck);
			boolean b = ms.remove(drawnCard);
			//System.out.println(b);
			return ImmutableMultiset.copyOf(ms);
			}
		);*/
		builder.removeCard(drawnCard);
		return builder;
	}
	
	
	public static FullGameState3 endTurn(FullGameState3 state, Action action) {
		Preconditions.checkArgument(state.hasJustDrawn());
		
		Preconditions.checkArgument(isValid(state, action/*, state.hand(state.whoseTurn()), state.drawnCard().get()*/));
		
		FullGameState3.Builder builder = state.toFullBuilder();
		
		discardCard(builder, action);
		applyAction(builder, action);
		checkWin(builder);
		incrementTurn(builder);
		
		return builder.build();
	}
	
	private static FullGameState3.Builder discardCard(FullGameState3.Builder builder, Action action) {
		if (action.card == builder.hands().get(action.player)) {
			builder.mutateHands(m -> m.put(action.player, builder.drawnCard().get()));//(c -> builder.drawnCard().get());
		}
		
		builder.mutatePlayers(m -> m.compute(builder.whoseTurn(),
				(p, s) -> s.toBuilder().addDiscardPile(action.card).build()));
		
		builder.drawnCard(Optional.empty());
		
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
		case PRINCE:	Card drawn;
						//if (builder.deckSize() > 0) {
						drawn = builder.getRandomFromDeck();
						//builder.mutateDeck(ms -> ms.remove(drawn));
						builder.removeCard(drawn);
						//} else {
						//	drawn = builder.initialSetAside();
						//}
						Card inHand = builder.hands().get(action.targetPlayer.get());
						builder.mutatePlayers(m -> m.compute(action.targetPlayer.get(),
								(p, s) -> s.toBuilder().addDiscardPile(inHand).build()));
						if (inHand == Card.PRINCESS) {
							builder.winner(action.targetPlayer.map(Player::other));
						} else {
							builder.mutateHands(m -> m.put(action.targetPlayer.get(), drawn));
						}
		case HANDMAID:	//System.out.println("Handmaiding " + action.player);
						return builder.mutatePlayers(m -> m.compute(action.player,
							(p, s) -> s.toBuilder().isProtected(true).build()));
		case BARON:		int result = builder.hands().get(Player.ONE).compareTo(builder.hands().get(Player.TWO));
						//System.out.println("baron: " + result);
						return builder.winner(
								(result == 0) ? Optional.empty() :
								Optional.of((result < 0) ? Player.ONE : Player.TWO));
		case PRIEST:	//System.out.printf("Result of Priest: %s has %s%n",
						//		action.targetPlayer.get(), builder.hands().get(action.targetPlayer.get()));
						return builder; // TODO I don't know how to handle this
		case GUARD:		if (builder.hands().get(action.targetPlayer.get()) == action.targetCard.get()) {
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

			Optional<Player> winner = Arrays.stream(Player.values()).max(comparator);
			builder.winner(winner);
			return builder;
		} else {
			return builder;
		}
	}
	
	private static FullGameState3.Builder incrementTurn(FullGameState3.Builder builder) {
		return builder.mapWhoseTurn(Player::other).mapTurnNumber(i -> i+1);
	}
	
	private static boolean isValid(FullGameState3 state, Action action) {
		return state.isValid(action, state.hand(action.player), state.drawnCard().get());
	}
}