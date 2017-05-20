package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Player;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.Expectiminimaxer;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class Policies {
	
	private static final Policy uniformRandom2 = (set) -> {
		Preconditions.checkArgument(set.drawnCard().isPresent());
		return Expectiminimaxer.validActions(set.state(), set.hand(), set.drawnCard().get())
				.collect(Collectors.toMap(Functions.identity(), x -> 1.0));
	};
			
	
	//private static final Policy uniformRandom = new ReasonableUniformPolicy();
	public static Policy uniformRandom() {
		return uniformRandom2;
	}
	
	public static Policy splitPolicy(Policy player1, Policy player2) {
		return SplitPolicy.create(player1, player2);
	}
	
	public static Policy generalSplitPolicy(Predicate<InformationSet> predicate, Policy ifTrue, Policy ifFalse) {
		return (is) -> predicate.test(is) ? ifTrue.choiceDistribution(is) : ifFalse.choiceDistribution(is);
	}
	
	public static Policy explicitPolicy(Table<InformationSet, Action, Double> policyMap) {
		return ExplicitPolicy.create(policyMap);
	}
	
	public static Policy simpleExplicitPolicy(InformationSet is, Map<Action, Double> actionMap) {
		return explicitPolicy(InformationSets.table(ImmutableMap.of(is, actionMap)));
	}
	
	public static Policy superSimpleExplicitPolicy(InformationSet is, Action action) {
		return simpleExplicitPolicy(is, ImmutableMap.of(action, 1.0));
	}
	
	public static Table<InformationSet, Action, Double> explicitTable(Policy p, Set<InformationSet> informationSets) {
		return ExplicitPolicy.explicitTable(p, informationSets);
	}
	
	public static Policy aimingTowards(InformationSet goal) {
		return AimingTowardsIS.create(goal);
	}
	
	private static class SplitPolicy implements Policy {
		private final Map<Player, Policy> playerMap;
		
		private SplitPolicy(Map<Player, Policy> playerMap) {
			this.playerMap = ImmutableMap.copyOf(playerMap);
		}
		
		public static SplitPolicy create(Map<Player, Policy> playerMap) {
			Preconditions.checkNotNull(playerMap);
			Preconditions.checkArgument(playerMap.containsKey(Player.ONE));
			Preconditions.checkArgument(playerMap.containsKey(Player.TWO));
			return new SplitPolicy(playerMap);
		}
		
		public static SplitPolicy create(Policy player1, Policy player2) {
			return create(ImmutableMap.of(Player.ONE, player1, Player.TWO, player2));
		}

		public Map<Action, Double> choiceDistribution(InformationSet set) {
			return playerMap.get(set.perspective()).choiceDistribution(set);
		}
	}
	
	/*private static class GeneralSplitPolicy implements Policy {
		private final Predicate<InformationSet> ch
	}*/
	
	static class ExplicitPolicy implements Policy {
		private final ImmutableTable<InformationSet, Action, Double> policyMap;
		
		private ExplicitPolicy(Table<InformationSet, Action, Double> policyMap) {
			this.policyMap = ImmutableTable.copyOf(policyMap);
		}
		
		public static ExplicitPolicy create(Table<InformationSet, Action, Double> policyMap) {
			Preconditions.checkNotNull(policyMap);
			return new ExplicitPolicy(policyMap);
		}
		
		public static ExplicitPolicy copyOf(Policy p, Set<InformationSet> informationSetsToUse) {
			return create(explicitTable(p, informationSetsToUse));
		}
		
		public static Table<InformationSet, Action, Double> explicitTable(Policy p, Set<InformationSet> informationSets) {
			Map<InformationSet, Map<Action, Double>> map =
					informationSets.stream()
					.filter(i -> i.state().hasJustDrawn()) // we should only need to care about infosets where an action needs to be taken
					.collect(Collectors.toMap(Functions.identity(), p::choiceDistribution));
			return InformationSets.table(map);
		}
		
		public Table<InformationSet, Action, Double> getTable() {
			return policyMap;
		}
		
		public Map<Action, Double> choiceDistribution(InformationSet set) {
			Preconditions.checkNotNull(set);
			Preconditions.checkArgument(set.state().hasJustDrawn());
			Preconditions.checkArgument(policyMap.containsRow(set),
					"Policy map did not containg the given info set: %s", set);
			return policyMap.row(set);
		}

		@Override
		public String toString() {
			//return "ExplicitPolicy [policyMap=" + policyMap + "]";
			//return "ExplicitPolicy [policyMap=" + String.format("%8X", policyMap.hashCode()) + ": " + policyMap.cellSet().stream().map(x -> x.getValue()).collect(Collectors.toList()) + "]";
			return "ExplicitPolicy [policyMap=" + String.format("%8X", policyMap.hashCode()) + "]";
		}
	}

	private static class AimingTowardsIS implements Policy {
		private final InformationSet aimingTowards;
		
		private AimingTowardsIS(InformationSet informationSet) {
			this.aimingTowards = informationSet;
		}
		
		public static AimingTowardsIS create(InformationSet informationSet) {
			Preconditions.checkNotNull(informationSet);
			return new AimingTowardsIS(informationSet);
		}
		
		public Map<Action, Double> choiceDistribution(InformationSet set) {
			Preconditions.checkArgument(!set.equals(aimingTowards));
			Preconditions.checkArgument(set.state().turnNumber() < aimingTowards.state().turnNumber());
			Preconditions.checkArgument(ImmutableSet.copyOf(set.state().visibleDiscard()).equals(
					ImmutableSet.copyOf(aimingTowards.state().visibleDiscard())));
			
			List<Action> intendedActionHistory = aimingTowards.state().history();
			List<Action> currentActionHistory = set.state().history();
			
			// Up to now, action histories must be same
			Preconditions.checkArgument(
					intendedActionHistory.subList(0, currentActionHistory.size()).equals(currentActionHistory));
			
			Action intent = intendedActionHistory.get(currentActionHistory.size());
			
			return ImmutableMap.of(intent, 1.0);
		}
	}

}



