package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.List;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.collect.ImmutableMap;

@FreeBuilder
public abstract class InformationSet {
	abstract public Player perspective();
	
	abstract public GameState state();
	
	abstract public Card hand();
	
	abstract public Optional<Card> drawnCard();
	
	abstract public Builder toBuilder();
	
	public static Builder builder() {
		return new Builder();
	}
	
	static class Builder extends InformationSet_Builder { }
	
	public static InformationSet fromFullGameState(FullGameState state, Player perspective) {
		InformationSet is = InfoSetHelper.fromFullGameState(state, perspective);
		return is;
	}
	
	public boolean includes(FullGameState s) {
		return this.equals(fromFullGameState(s, perspective()));
	}
	
	public String toString() {
		return ImmutableMap.of("hand", hand(), "drawnCard", drawnCard()).toString();
	}
}

class InfoSetHelper {
	static InformationSet fromFullGameState(FullGameState state, Player perspective) {
		InformationSet.Builder b  = InformationSet.builder()
				.hand(state.hand(perspective))
				.state(state.getPublicState())
				.perspective(perspective);
		if (state.whoseTurn() == perspective) {
			b.drawnCard(state.drawnCard());
		}
		return b.build();
	}
}



//assert !temp.isEmpty() : curState;
//if (temp.isEmpty()) {
//	throw new RuntimeException();
//}

//double resultSum3 = temp.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
//if (resultSum3 < 0.99) {
//	System.out.println("temp1 from distribution: " + resultSum3);
//}
//if (resultSum3 < 0.99) { throw new RuntimeException(); }

//temp.replaceAll((s, v) -> v * curVal);

//double resultSum4 = temp.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
//if (resultSum4 < 0.99) {
//	System.out.println("temp2 from distribution: " + resultSum4 + "; curVal: " + curVal);
//}
//results.putAll(temp);

//ImmutableMap<T, Double> resultMap = frequencyMap.build();
//double result = resultMap.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
//if (result < 0.99) {
//System.out.println("normalized from multiset: " + result);
//}



//double resultSum = starting.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
//if (resultSum < 0.99) {
//System.out.println("initial from distribution: " + resultSum + " " + expander);
//}

//double resultSum2 = results.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
//if (resultSum2 < 0.99) {
//System.out.println("expanded from distribution: " + resultSum2 + " " + expander);
//}