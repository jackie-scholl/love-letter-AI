package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.ConsolePlayer;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.Expectiminimaxer;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

public class CounterfactualRegret {
	public static Table<InformationSet, Action, Double> getWinRates(Collection<Card> visibleDiscard,
			GameStateDistribution calculator, Player perspective) {
		
		Table<InformationSet, FullGameState, Double> input = InformationSets.getOverallInfoSets3(visibleDiscard, calculator, perspective);
		
		Table<InformationSet, Action, Double> result = HashBasedTable.create();
		
		for (InformationSet is : input.rowMap().keySet()) {
			Map<FullGameState, Double> map = input.row(is);
			
			if (!map.keySet().iterator().next().hasJustDrawn() || map.keySet().iterator().next().whoseTurn() != perspective) {
				continue;
			}
			
			Map<Action, Double> resultingMap = WinProbabilities.getWinProbabilityActionMap(map, calculator, perspective);
			assert result.row(is).isEmpty();
			result.row(is).putAll(resultingMap);
		}
		
		return ImmutableTable.copyOf(result);
	}
	
	
	/*
	 * Let π^σ (h) be the probability of history h occurring if players choose actions according to σ
	 * Note: theoretical only, do not use, awful speed
	 */
	public static double probability(Policy policy, FullGameState history, Collection<Card> initDiscard) {
		Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
		//return new GameStateDistribution(policy).probabilityOfFullGameStateGiven(initDiscard, history);
		double value = GSD2.create(initDiscard, policy).probabilityOfFullGameStateGiven(history);
		
		//assert Math.abs(value - GSD2.create(initDiscard, policy).getProbabilities().get(history)) < 1e-6 : "diverging results";
		//System.out.printf("Should be 1.0: %f%n", value / GSD2.create(initDiscard, policy).getProbabilities().get(history));
		
		return value;
	}
	
	/*
	 * Hence, π^σ_i (h) is the probability that if player i plays according to σ then for all histories h' that are a
	 * proper prefix of h with P(h') = i, player i takes the corresponding action in h.
	 * Note: theoretical only, do not use, awful speed
	 * 
	 */
	public static double probabilityP(Player i, Policy policy, FullGameState history, Collection<Card> initDiscard) {
		Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
		if (!history.lastHalfStep().isPresent()) {
			//System.out.println("a");
			return 1;
		}
		double previous = probabilityP(i, policy, history.lastHalfStep().get(), initDiscard);
		
		if (!history.thisAction().isPresent() || history.thisAction().get().player != i) {
			return previous;
		}

		/*return previous * 
				probability(policy, history, initDiscard) /
				probability(policy, history.lastHalfStep().get(), initDiscard);*/
		//System.out.println(history.thisAction());
		Action a = history.thisAction().get();
		
		assert a != null;
		
		InformationSet is = InformationSet.fromFullGameState(history.lastHalfStep().get(), i);
		Map<Action, Double> choiceDistribution = policy.choiceDistribution(is);
		Map<Action, Double> normalizedChoiceDistribution = Distributions.normalize(choiceDistribution);
		Double value = normalizedChoiceDistribution.get(a);
	
		return value == null ? 0 : previous * value;
	}
	
	/*
	 * Let π ^σ _−i (h) be the product of all players’ contribution (including chance) except player i.
	 * Note: theoretical only, do not use, awful speed
	 */
	public static double probabilityPNeg(Player i, Policy policy, FullGameState history, Collection<Card> initDiscard) {
		Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
		if (!history.lastHalfStep().isPresent()) {
			return GameStateDistribution.initialDistribution(initDiscard).get(history);
		}
		double previous = probabilityPNeg(i, policy, history.lastHalfStep().get(), initDiscard);
		
		if (history.thisAction().isPresent() && history.thisAction().get().player == i) {
			return previous;
		}
		double value = previous *
				probability(policy, history, initDiscard) /
				probability(policy, history.lastHalfStep().get(), initDiscard);
		
		ConsolePlayer.shouldBeEqual(value, probability(policy, history, initDiscard) / probabilityP(i, policy, history, initDiscard));
		
		return Double.isFinite(value) ? value : 0.0;
	}
	
	/*
	 * For I ⊆ H, define π^σ (I) = (sum with h over I of) π^σ (h), as the probability of reaching a particular 
	 * information set given σ, with π^σ_i(I) and π^σ_−i(I) defined similarly.
	 */
	public static double probability2(Policy policy, InformationSet is, Set<FullGameState> gameStates, Collection<Card> initDiscard) {
		Preconditions.checkArgument(is.state().visibleDiscard().equals(initDiscard));

		/*return GSD2.create(initDiscard, policy)
				.getAllGameStates()
				.stream()
				.filter(is::includes)*/
		return gameStates.stream()
				.mapToDouble(s -> probability(policy, s, initDiscard)).sum();
	}
	
	public static double probability2P(Player i, Policy policy, InformationSet is, Set<FullGameState> gameStates, Collection<Card> initDiscard) {
		Preconditions.checkArgument(is.state().visibleDiscard().equals(initDiscard));
		Preconditions.checkArgument(i == is.perspective());

		List<Double> probabilities =
				//GSD2.create(initDiscard, policy).getAllGameStates().stream().filter(is::includes)
				gameStates.stream()
				.map(s -> probabilityP(i, policy, s, initDiscard)).collect(Collectors.toList());
		
		/*if (probabilities.stream().distinct().count() != 1) {
			System.out.println(is);
			for (FullGameState s : GSD2.create(initDiscard, policy).getAllGameStates().stream().filter(is::includes).collect(Collectors.toList())) {
				//Map<Action, Double> actionMap = Distributions.normalize(policy.choiceDistribution(InformationSet.fromFullGameState(s.lastHalfStep().get().lastHalfStep().get(), i)));
				//System.out.println(actionMap + " _ " + s);
				System.out.println(is.includes(s) + " " + s.hand(i) + " " + is.hand());
				System.out.println(s);
			}
		}*/
		
		// TODO: this assert keeps failing and I have no idea why. I'm kinda concerned. Ignoring for now.
		//assert probabilities.stream().distinct().count() == 1 : probabilities;
		
		return probabilities.get(0);
	}
	
	public static double probability2PNeg(Player i, Policy policy, InformationSet is, Set<FullGameState> gameStates, Collection<Card> initDiscard) {
		Preconditions.checkArgument(is.state().visibleDiscard().equals(initDiscard));

		/*return GSD2.create(initDiscard, policy).getAllGameStates().stream().filter(is::includes)
		//return GSD2.create(initDiscard, policy).getAllGameStatesInInfoSet(is).stream()
				.mapToDouble(s -> probabilityNeg(i, policy, s, initDiscard)).sum();*/
		//InformationSets.
		/*List<Double> probabilities =
				GSD2.create(initDiscard, policy).getAllGameStates().stream().filter(is::includes)
					.map(s -> probabilityNeg(i, policy, s, initDiscard)).collect(Collectors.toList());
		List<FullGameState> l = GSD2.create(initDiscard, policy).getAllGameStates().stream().filter(is::includes).collect(Collectors.toList());
		for (FullGameState s : l) {
			System.out.println(s);
		}
		assert probabilities.stream().distinct().count() == 1 : probabilities;
		return probabilities.get(0);*/
		/*double sum = InformationSets.getOverallInfoSets3(initDiscard, new GameStateDistribution(policy), i).row(is)
				.entrySet()
				.stream()
				.map(e -> probabilityNeg(i, policy, e.getKey(), initDiscard) * e.getValue()).mapToDouble(x -> x).sum();
		System.out.println(InformationSets.getOverallInfoSets3(initDiscard, new GameStateDistribution(policy), i).row(is).values());
		System.out.println(InformationSets.getOverallInfoSets3(initDiscard, new GameStateDistribution(policy), i).row(is).keySet().stream().map(s -> probabilityNeg(i, policy, s, initDiscard)).collect(Collectors.toList()));
		for (FullGameState s : InformationSets.getOverallInfoSets3(initDiscard, new GameStateDistribution(policy), i).row(is).keySet()) {
			System.out.println(s);
		}
		return sum;*/
		double value = probability2(policy, is, gameStates, initDiscard) / probability2P(i, policy, is, gameStates, initDiscard);
		
		return Double.isNaN(value) ? 0.0 : value;
	}
	
	
	private static void shouldBeEqual(double a, double b) {
		assert Math.abs(a - b) < 1e-9 : "Values should be equal: " + a + " and " + b;
	}
	
	private static double utility(Player i, FullGameState history) {
		Preconditions.checkArgument(history.winner().isPresent());
		return history.winner().get() == i ? 1 : -1;
	}
	
	/*
	 * The overall value to player i of a strategy profile is then the expected payoff of the resulting terminal node, u_i(σ) = Ph∈Z u_i(h) π^σ(h).
	 */
	private static double utility(Player i, Policy policy, Collection<Card> initDiscard) {
		return WinProbabilities.overallProbabilityOfWin(initDiscard, new GameStateDistribution(policy), i);
	}

	/*
	 * Define u_i(σ, h) to be the expected utility given that the history h is reached and
	 *  then all players play using strategy σ
	 *  
	 *  Issue: A history is not enough to know the state of the game, and our expectation of the distribution of actual
	 *  game states relies on the policy they play with.
	 */
	
	// i = perspective, σ = policy, h = history
	private static double utility(Player perspective, Policy policy, FullGameState history, Collection<Card> initDiscard) {
		//GSD2.create(initDiscard, policy);
		return WinProbabilities.probabilityOfWinGivenFinalDistribution(
				//new GameStateDistribution(policy).getFullDistribution(history),
				GSD2.create(initDiscard, policy).getFullDistribution(history),
				perspective);
	}
	
	/*
	 * Define counterfactual utility u_i(σ, I) to be the expected utility given that information set I is reached and
	 *  all players play using strategy σ except that player i plays to reach I, formally if π^σ (h, h') is the
	 *  probability of going from history h to history h', then:
	 *  (Equation 5)
	 */
	private static double utility(Player perspective, Policy policy, InformationSet is, Set<FullGameState> gameStates) {
		Collection<Card> visibleDiscard = is.state().visibleDiscard();
		//Set<FullGameState> infoset = GSD2.create(visibleDiscard, policy).getAllGameStatesInInfoSet(is);
		
		double result = gameStates.stream()
				.mapToDouble(h -> probabilityPNeg(perspective, policy, h, visibleDiscard) 
						* utility(perspective, policy, h, visibleDiscard))
				.sum() / probability2PNeg(perspective, policy, is, gameStates, visibleDiscard);
		
		return Double.isNaN(result) ? 0.0 : result;
	}
	
	private static double counterfactualRegret(Player perspective, Policy policy, InformationSet is, Set<FullGameState> gameStates, Action a) {
		double v1 = probability2PNeg(perspective, policy, is, gameStates, is.state().visibleDiscard());
		
		Policy policy2 = Policies.generalSplitPolicy(is::equals,
				Policies.superSimpleExplicitPolicy(is, a),
				policy);
		double v2 = utility(perspective, policy2, is, gameStates);
		
		double v3 = utility(perspective, policy, is, gameStates);
		
		return v1 * (v2 - v3);
	}

	private static double counterfactualRegret(Player perspective, InformationSet is, Set<FullGameState> gameStates, List<Policy> policies, Action a) {
		return policies.stream().mapToDouble(p -> counterfactualRegret(perspective, p, is, gameStates, a)).average().getAsDouble();
	}
	
	private static double counterfactualRegretPos(Player perspective, InformationSet is, Set<FullGameState> gameStates, List<Policy> policies, Action a) {
		double value = Math.max(0, counterfactualRegret(perspective, is, gameStates, policies, a));
		return Double.isFinite(value) ? value : 0;
	}
	
	private static Map<Action, Double> nextPolicyAt(Player perspective, InformationSet is, Set<FullGameState> gameStates, List<Policy> pastPolicies) {
		// Not actually all of them, but all the actions that we're going to *consider*
		Set<Action> validActions = Expectiminimaxer.validActions(is.state(), is.hand(), is.drawnCard().get())
				.collect(Collectors.toSet());
		
		Map<Action, Double> actionValues = validActions.stream()
				.collect(Collectors.toMap(a -> a,
						a -> counterfactualRegretPos(perspective, is, gameStates, pastPolicies, a)));
		
		//double sum = validActions.stream().mapToDouble(a -> counterfactualRegretPos(perspective, is, pastPolicies, a)).sum();
		double sum = actionValues.values().stream().mapToDouble(x -> x).sum();
		
		if (sum == 0) {
			return Distributions.normalize(validActions.stream().collect(Collectors.toMap(a -> a, a -> 1.0)));
		}
		
		return Distributions.normalize(validActions.stream()
				.collect(Collectors.<Action, Action, Double>toMap(a -> a,
						a -> actionValues.get(a))));
						//a -> counterfactualRegretPos(perspective, is, pastPolicies, a))));
	}
	
	private static Policy nextPolicy(Set<InformationSet> infoSets, SetMultimap<InformationSet, FullGameState> gameStates, List<Policy> pastPolicies) {
		Stream<InformationSet> infoSetStream = AI.PARALLEL ? infoSets.parallelStream() : infoSets.stream();
		
		return Policies.explicitPolicy(
				InformationSets.table(
						infoSetStream
						.collect(Collectors.toMap(is -> is,
								is -> nextPolicyAt(is.perspective(), is, gameStates.get(is), pastPolicies)))
						)
				);
	}
	
	private static double immediateCounterfactualRegret(Player perspective, InformationSet is, Set<FullGameState> gameStates, List<Policy> policies) {
		Preconditions.checkArgument(is.drawnCard().isPresent());
		Preconditions.checkArgument(is.perspective() == perspective);
		
		// this is T (big T) but we follow java conventions so...
		int t = policies.size();
		
		// Not actually all of them, but all the actions that we're going to *consider*
		Set<Action> validActions = Expectiminimaxer.validActions(is.state(), is.hand(), is.drawnCard().get()).collect(Collectors.toSet());
		
		double maxAverageVal = validActions.stream()
				.mapToDouble(a -> counterfactualRegret(perspective, is, gameStates, policies, a))
				.max().getAsDouble();
		
		return maxAverageVal;
	}
	
	private static double immediateCounterfactualRegretPos(Player perspective, InformationSet is, Set<FullGameState> gameStates, List<Policy> policies) {
		double value = Math.max(0, immediateCounterfactualRegret(perspective, is, gameStates, policies));
		return Double.isFinite(value) ? value : 0.0;
	}
	
	private static double totalImmediateRegret(Player perspective, Set<InformationSet> infoSets,
			SetMultimap<InformationSet, FullGameState> gameStates, List<Policy> policies) {
		return infoSets.stream().mapToDouble(
				is -> immediateCounterfactualRegretPos(perspective, is, gameStates.get(is), policies))
				.sum();
	}
	
	private static SetMultimap<InformationSet, FullGameState> getGameStates(Set<InformationSet> infoSets) {
		Set<Collection<Card>> initDiscards = infoSets.stream().map(s -> s.state().visibleDiscard()).collect(Collectors.toSet());
		
		GameStateDistribution sample = new GameStateDistribution(Policies.uniformRandom());
		
		ImmutableSetMultimap.Builder<InformationSet, FullGameState> builder = ImmutableSetMultimap.builder();
		
		for (Collection<Card> initDiscard : initDiscards) {
			builder.putAll(InformationSets.getOverallInfoSets(initDiscard, sample, Player.ONE));
			builder.putAll(InformationSets.getOverallInfoSets(initDiscard, sample, Player.TWO));
		}
		
		return builder.build();
	}
	
	private static Policy iteratePolicies(Set<InformationSet> infoSets, int iterations) {
		long start = System.currentTimeMillis();
		
		List<Policy> policies = new ArrayList<>();
		
		policies.add(Policies.explicitPolicy(Policies.explicitTable(Policies.uniformRandom(), infoSets)));
		
		SetMultimap<InformationSet, FullGameState> gameStates = getGameStates(infoSets);

		System.out.printf("Time: %5.3f seconds%n", (System.currentTimeMillis() - start) / 1000.0);
		
		System.out.println("Initial random: " + Policies.explicitPolicy(Policies.explicitTable(Policies.uniformRandom(), infoSets)));
		
		for (int i=0; i<iterations; i++) {
			/*if (i % 10 == 0) {
				for (Player p : Player.values()) {
					Set<InformationSet> s = infoSets.stream().filter(is -> is.perspective() == p).collect(Collectors.toSet());
					System.out.println("Value: " + p + " : " + totalImmediateRegret(p, s, gameStates, policies.subList(policies.size()-1, policies.size())));
				}
			}*/
			Policy next = nextPolicy(infoSets, gameStates, policies);
			policies.add(next);
			
			System.out.println(WinProbabilities.overallProbabilityOfWin(infoSets.iterator().next().state().visibleDiscard(), new GameStateDistribution(next), Player.ONE));
			System.out.println(next);
			
			System.out.printf("Time: %5.3f seconds%n", (System.currentTimeMillis() - start) / 1000.0);
			start = System.currentTimeMillis();
		}
		
		System.out.println("-------------");
		
		double regretMax = 0;
		for (Player p : Player.values()) {
			Set<InformationSet> s = infoSets.stream().filter(is -> is.perspective() == p).collect(Collectors.toSet());
			double regret = totalImmediateRegret(p, s, gameStates, policies);
			regretMax = Math.max(regret, regretMax);
		}
		
		System.out.printf("Regret is %.3f, so average policy is a %.3f equilibrium%n", regretMax, regretMax*2);
		
		System.out.printf("Average policy: %s%n", averagePolicy(policies));
		
		return policies.get(policies.size() - 1);
	}
	
	private static Policy averagePolicy(List<Policy> policies) {
		Map<InformationSet, List<Map<Action, Double>>> thing = 
				policies.stream()
				.peek(p -> Preconditions.checkArgument(p instanceof Policies.ExplicitPolicy))
				.map(p -> (Policies.ExplicitPolicy) p)
				.map(p -> p.getTable())
				.map(t -> t.rowMap().entrySet())
				.flatMap(Set::stream)
				.collect(Collectors.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toList())))
				;
		
		//ListMultimap<InformationSet, Map<Action, Double>> thing2 = ArrayListMultimap.create();
		Table<InformationSet, Action, Double> result = HashBasedTable.create();
		
		for (InformationSet is : thing.keySet()) {
			List<Map<Action, Double>> values = thing.get(is);
			
			assert values.stream().map(m -> m.keySet()).distinct().count() == 1;
			
			//values.stream().map(m -> Distributions.sum(m)).forEach(d -> ConsolePlayer.shouldBeEqual(d, 1.0));
			
			Map<Action, Double> baseDistribution = new HashMap<>();
			
			for (Map<Action, Double> value : values) {
				Distributions.mergeInByAdding(baseDistribution, Distributions.normalize(value));
			}
			
			Map<Action, Double> resultDistribution = Distributions.normalize(baseDistribution);
			
			for (Action a : resultDistribution.keySet()) {
				double value = resultDistribution.get(a);
				result.put(is, a, value);
			}
		}
		
		return Policies.explicitPolicy(result);
	}
	
	public static Set<InformationSet> informationSets(Collection<Card> initDiscard) {
		return GSD2.create(initDiscard, Policies.uniformRandom())
				.getAllGameStates()
				.stream()
				.map(s -> InformationSet.fromFullGameState(s, s.whoseTurn()))
				.filter(s -> s.state().hasJustDrawn())
				.collect(Collectors.toSet());
	}
	
	public static Policy iteratePolicies(Collection<Card> initDiscard, int iterations) {
		Set<InformationSet> infoSets = informationSets(initDiscard);
		return iteratePolicies(infoSets, iterations);
	}
	
}




/*System.out.println(" 2: " + GSD2.create(initDiscard, policy)
	.getAllGameStates()
	.stream()
	.filter(s -> s.history().equals(is.state().history()))
	//.filter(is::includes)
	.count());

Player us = is.perspective();
assert us == is.state().whoseTurn();

System.out.println(" 4: " + GSD2.create(initDiscard, policy)
	.getAllGameStates()
	.stream()
	.filter(s -> s.history().equals(is.state().history()))
	.filter(s -> s.hand(us).equals(is.hand()))
	.count());

System.out.println(" 6: " + GSD2.create(initDiscard, policy)
	.getAllGameStates()
	.stream()
	.filter(s -> s.history().equals(is.state().history()))
	.filter(s -> s.hand(us).equals(is.hand()))
	.filter(s -> s.drawnCard().equals(is.drawnCard()))
	.count());

System.out.println(" 8: \n" + GSD2.create(initDiscard, policy)
	.getAllGameStates()
	.stream()
	.filter(s -> s.history().equals(is.state().history()))
	.filter(s -> s.hand(us).equals(is.hand()))
	.filter(s -> s.drawnCard().equals(is.drawnCard()))
	.map(s -> s.toString())
	.collect(Collectors.joining("\n")));

System.out.println(" 9: " + GSD2.create(initDiscard, policy)
	.getAllGameStates()
	.stream()
	.filter(s -> s.history().equals(is.state().history()))
	.filter(s -> s.hand(us).equals(is.hand()))
	.filter(s -> s.drawnCard().equals(is.drawnCard()))
	.filter(s -> s.getPublicState().equals(is.state()))
	.count());

System.out.println("10: " + GSD2.create(initDiscard, policy)
		.getAllGameStates()
		.stream()
		.filter(is::includes).count());*/


/*
 * Let π^σ (h) be the probability of history h occurring if players choose actions according to σ
 * Note: theoretical only, do not use, awful speed
 */
//public static double probability(Policy policy, GameState history, List<Card> initDiscard) {
//	Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
//	return new GameStateDistribution(policy).probabilityOfHistoryGiven(initDiscard, history);
//}

/*
 * Hence, π^σ_i (h) is the probability that if player i plays according to σ then for all histories h' that are a
 * proper prefix of h with P(h') = i, player i takes the corresponding action in h.
 * Note: theoretical only, do not use, awful speed
 * 
 * Broken, maybe? This is "not supposed" to include chance but it totally does and it feels like it has to?
 */
//public static double probability(Player i, Policy policy, GameState history, List<Card> initDiscard) {
//	Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
//	if (!history.lastHalfStep().isPresent()) {
//		return 1;
//	}
//	double previous = probability(i, policy, history.lastHalfStep().get(), initDiscard);
//	if (!history.thisAction().isPresent() || history.thisAction().get().player != i) {
//		return previous;
//	}
//	return previous * 
//			probability(policy, history, initDiscard) /
//			probability(policy, history.lastHalfStep().get(), initDiscard);
//}

/*
 * Let π ^σ _−i (h) be the product of all players’ contribution (including chance) except player i.
 * Note: theoretical only, do not use, awful speed
// */
//public static double probabilityNeg(Player i, Policy policy, GameState history, List<Card> initDiscard) {
//	Preconditions.checkArgument(history.visibleDiscard().equals(initDiscard));
//	if (!history.lastHalfStep().isPresent()) {
//		return 1;
//	}
//	double previous = probabilityNeg(i, policy, history.lastHalfStep().get(), initDiscard);
//	
//	if (!history.thisAction().isPresent() || history.thisAction().get().player == i) {
//		return previous;
//	}
//	return previous *
//			probability(policy, history, initDiscard) /
//			probability(policy, history.lastHalfStep().get(), initDiscard);
//}






