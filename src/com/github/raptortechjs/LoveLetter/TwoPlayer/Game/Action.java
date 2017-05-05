package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.Optional;

public class Action {
	public final PlayerNumber player;
	public final Card card;
	public final Optional<PlayerNumber> targetPlayer;
	public final Optional<Card> targetCard;
	
	public Action(PlayerNumber player, Card card, Optional<PlayerNumber> targetPlayer, Optional<Card> targetCard) {
		this.player = player;
		this.card = card;
		this.targetPlayer = targetPlayer;
		this.targetCard = targetCard;
	}
	
	public Action(PlayerNumber player, Card card, Optional<PlayerNumber> targetPlayer) {
		this(player, card, targetPlayer, Optional.empty());
	}
	
	public Action(PlayerNumber player, Card card) {
		this(player, card, Optional.empty(), Optional.empty());
	}
	
	public Action normalize() {
		if (card.numberOfArguments == 0) {
			return new Action(player, card);
		} else if (card.numberOfArguments == 1) {
			return new Action(player, card, targetPlayer);
		} else if (card.numberOfArguments == 2) {
			return this;
		}
		throw new RuntimeException();
	}
	
	public String toString() {
		String s = String.format("%s plays %s", player, card);
		if (targetPlayer.isPresent()) {
			s = s + String.format(" on %s", targetPlayer.get());
		}
		if (targetCard.isPresent()) {
			s = s + String.format(" targeting %s", targetCard.get());
		}
		return s;
	}

	// autogenerated
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((card == null) ? 0 : card.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + ((targetCard == null) ? 0 : targetCard.hashCode());
		result = prime * result + ((targetPlayer == null) ? 0 : targetPlayer.hashCode());
		return result;
	}

	// autogenerated
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Action other = (Action) obj;
		if (card != other.card) return false;
		if (player != other.player) return false;
		if (targetCard == null) {
			if (other.targetCard != null) return false;
		} else if (!targetCard.equals(other.targetCard)) return false;
		if (targetPlayer == null) {
			if (other.targetPlayer != null) return false;
		} else if (!targetPlayer.equals(other.targetPlayer)) return false;
		return true;
	}
}