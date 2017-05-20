package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.inferred.freebuilder.shaded.com.google.common.collect.Maps;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.*;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;

public class GameStateDistribution {
	
	private static class PolicyFGSTuple {
		public final Policy p;
		public final FullGameState s;
		public PolicyFGSTuple(Policy p, FullGameState s) {
			this.p = p;
			this.s = s;
		}
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			result = prime * result + ((s == null) ? 0 : s.hashCode());
			return result;
		}
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PolicyFGSTuple other = (PolicyFGSTuple) obj;
			if (p == null) {
				if (other.p != null) return false;
			} else if (!p.equals(other.p)) return false;
			if (s == null) {
				if (other.s != null) return false;
			} else if (!s.equals(other.s)) return false;
			return true;
		}
	}
	
	private static final LoadingCache<PolicyFGSTuple, Map<FullGameState, Double>> halfStepDistributionCache2 = CacheBuilder.newBuilder()
			.maximumSize(AI.MAX_CACHE_SIZE)
			.concurrencyLevel(40)
			.build(
	           new CacheLoader<PolicyFGSTuple, Map<FullGameState, Double>>() {
	             public Map<FullGameState, Double> load(PolicyFGSTuple key)  {
	               return calculateHalfStepDistribution(key.s, key.p);
	             }
	           });
	
	//private final LoadingCache<FullGameState, Map<FullGameState, Double>> halfStepDistributionCache;
	private final Policy policy;
	
	public GameStateDistribution(Policy p) {
		this(p, Optional.empty());
	}
	
	public GameStateDistribution(Policy p, Optional<Integer> maxCacheSize) {
		policy = p;
		/*halfStepDistributionCache = CacheBuilder.newBuilder()
			.maximumSize(maxCacheSize.orElse(AI.MAX_CACHE_SIZE))
			//.maximumSize(10000)
			.concurrencyLevel(40)
			.build(
	           new CacheLoader<FullGameState, Map<FullGameState, Double>>() {
	             public Map<FullGameState, Double> load(FullGameState key)  {
	               return calculateHalfStepDistribution(key, p);
	             }
	           });*/
	}
	
	private Map<FullGameState, Double> getHalfStepDistribution(FullGameState state) {
		//return halfStepDistributionCache.getUnchecked(state);
		return halfStepDistributionCache2.getUnchecked(new PolicyFGSTuple(policy, state));
	}
	
	public Map<FullGameState, Double> getFullDistribution(FullGameState state) {
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		return Distributions.expand(getHalfStepDistribution(state), s -> this.getFullDistribution(s));
	}
	
	public Map<FullGameState, Double> getFullDistributionFromDistribution(Map<FullGameState, Double> initialDistribution) {
		return Distributions.expand(initialDistribution, this::getFullDistribution);
	}
	
	private void getProbabilities(FullGameState current, double currentMultiplier, Map<FullGameState, Double> distribution) {
		distribution.put(current, currentMultiplier);
		
		if (!current.winner().isPresent()) {
			getHalfStepDistribution(current).entrySet().stream()
				.forEach(e -> getProbabilities(e.getKey(), currentMultiplier * e.getValue(), distribution));
		}
	}
	
	private Map<FullGameState, Double> getProbabilities(FullGameState start, double initProbability) {
		Map<FullGameState, Double> probabilities = new HashMap<>();
		getProbabilities(start, initProbability, probabilities);
		return Collections.unmodifiableMap(probabilities);
	}
	
	private Map<FullGameState, Double> getProbabilities(Map<FullGameState, Double> distribution) {
		Map<FullGameState, Double> map = new HashMap<>();
		
		for (FullGameState state : distribution.keySet()) {
			double initProbability = distribution.get(state);
			Map<FullGameState, Double> temp = getProbabilities(state, initProbability);
			Distributions.mergeInByAdding(map, temp);
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	public Map<FullGameState, Double> getProbabilities(Collection<Card> visibleDiscard) {
		return getProbabilities(initialDistribution(visibleDiscard));
	}
	
	/*public double probabilityOfFullGameStateGiven(FullGameState start, FullGameState end) {
		if (start.equals(end)) {
			return 1;
		} else if (start.history().size() > end.history().size() ||
				!end.history().subList(0, start.history().size()).equals(start.history()) ||
				start.winner().isPresent()) {
			return 0;
		}
		return getHalfStepDistribution(start).entrySet().stream()
				.mapToDouble(e -> this.probabilityOfFullGameStateGiven(e.getKey(), end) * e.getValue()).sum();
	}
	
	public double probabilityOfFullGameStateGiven(Map<FullGameState, Double> distribution, FullGameState end) {
		return distribution.entrySet().stream()
				.mapToDouble(e -> probabilityOfFullGameStateGiven(e.getKey(), end) * e.getValue()).sum();
	}
	
	public double probabilityOfFullGameStateGiven(Collection<Card> visibleDiscard, FullGameState end) {
		return probabilityOfFullGameStateGiven(initialDistribution(visibleDiscard), end);
	}*/
	
	
	
	/*public double probabilityOfFullGameStateGiven(FullGameState start, FullGameState end) {
		return probabilityOfStateConditionGiven(start, (end) -> start.history().size() > end.history().size() ||
				!end.history().subList(0, start.history().size()).equals(start.history()) ||
				start.winner().isPresent(), )
	}
	
	public double probabilityOfFullGameStateGiven(Map<FullGameState, Double> distribution, FullGameState end) {
		return distribution.entrySet().stream()
				.mapToDouble(e -> probabilityOfFullGameStateGiven(e.getKey(), end) * e.getValue()).sum();
	}*/
	
	public double probabilityOfFullGameStateGiven(Collection<Card> visibleDiscard, FullGameState end) {
		List<Action> endHistory = end.history();
		return probabilityOfStateConditionGiven(initialDistribution(visibleDiscard),
				(start) ->
					//start.history().size() > end.history().size() ||
					start.turnNumber() > end.turnNumber() ||
					//!end.history().subList(0, start.history().size()).equals(start.history()) ||
					//!end.history().subList(0, start.turnNumber()).equals(start.history()) ||
					!endHistory.subList(0, start.turnNumber()).equals(start.history()) ||
					start.winner().isPresent(),
				end::equals);
	}
	
	private double probabilityOfStateConditionGiven(FullGameState start, Predicate<FullGameState> shouldQuit, Predicate<FullGameState> shouldInclude) {
		if (shouldInclude.test(start)) {
			//System.out.println("E:" + start);
			return 1;
		} else if (shouldQuit.test(start)) {
			return 0;
		}
		
		return getHalfStepDistribution(start).entrySet().stream()
				.mapToDouble(e -> this.probabilityOfStateConditionGiven(e.getKey(), shouldQuit, shouldInclude) * e.getValue()).sum();
	}
	
	private double probabilityOfStateConditionGiven(Map<FullGameState, Double> distribution, Predicate<FullGameState> shouldQuit, Predicate<FullGameState> shouldInclude) {
		return distribution.entrySet().stream()
				.mapToDouble(e -> probabilityOfStateConditionGiven(e.getKey(), shouldQuit, shouldInclude) * e.getValue()).sum();
	}
	
	public double probabilityOfStateConditionGiven(Collection<Card> visibleDiscard, Predicate<FullGameState> shouldQuit, Predicate<FullGameState> shouldInclude) {
		return probabilityOfStateConditionGiven(initialDistribution(visibleDiscard), shouldQuit, shouldInclude);
	}
	

	// broken, I think
	/*public double probabilityOfHistoryGiven(FullGameState start, GameState end) {
		if (start.history().equals(end.history())) {
			return 1;
		} else if (start.history().size() >= end.history().size() ||
				!end.history().subList(0, start.history().size()).equals(start.history()) ||
				start.winner().isPresent()) {
			return 0;
		}
		
		return getHalfStepDistribution(start).entrySet().stream()
				.mapToDouble(e -> this.probabilityOfHistoryGiven(e.getKey(), end) * e.getValue()).sum();
	}
	
	public double probabilityOfHistoryGiven(Map<FullGameState, Double> distribution, GameState history) {
		return distribution.entrySet().stream()
				.mapToDouble(e -> probabilityOfHistoryGiven(e.getKey(), history) * e.getValue()).sum();
	}
	
	public double probabilityOfHistoryGiven(Collection<Card> visibleDiscard, GameState history) {
		return probabilityOfHistoryGiven(initialDistribution(visibleDiscard), history);
	}*/
	
	
	private Set<FullGameState> getHalfTurnGameStates(FullGameState state) {
		return getHalfStepDistribution(state).keySet();
	}
	
	private Set<FullGameState> getAllGameStates(FullGameState state) {
		if (state.winner().isPresent()) {
			return ImmutableSet.of(state);
		}
		Set<FullGameState> set = getHalfTurnGameStates(state).stream()
				.flatMap(s -> getAllGameStates(s).stream())
				.collect(Collectors.toCollection(HashSet::new));
		set.add(state);
		return Collections.unmodifiableSet(set);
		
		//return ImmutableSet.<FullGameState>builder().addAll(set).add(state).build();
	}
	
	public Set<FullGameState> getAllGameStates(Collection<Card> visibleDiscard) {
		Set<FullGameState> initialSet = 
				ImmutableSet.copyOf(AI.getStartingStates(ImmutableList.copyOf(visibleDiscard)));
		
		return initialSet.stream()
				.flatMap(s -> getAllGameStates(s).stream())
				.collect(Collectors.toSet());
	}
	
	private Set<FullGameState> getAllGameStates(FullGameState state, Predicate<FullGameState> exclude) {
		if (exclude.test(state)) {
			return ImmutableSet.of();
		}
		if (state.winner().isPresent()) {
			return ImmutableSet.of(state);
		}
		Set<FullGameState> set = getHalfTurnGameStates(state).stream()
				.flatMap(s -> getAllGameStates(s).stream())
				.collect(Collectors.toCollection(HashSet::new));
		set.add(state);
		return Collections.unmodifiableSet(set);
		
		//return ImmutableSet.<FullGameState>builder().addAll(set).add(state).build();
	}
	
	public Set<FullGameState> getAllGameStates(Collection<Card> visibleDiscard, Predicate<FullGameState> exclude) {
		Set<FullGameState> initialSet = 
				ImmutableSet.copyOf(AI.getStartingStates(ImmutableList.copyOf(visibleDiscard)));
		
		return initialSet.stream()
				.flatMap(s -> getAllGameStates(s, exclude).stream())
				.collect(Collectors.toSet());
	}
	
	public  Set<FullGameState> getAllGameStatesInInfoSet(Collection<Card> visibleDiscard, InformationSet infoSet) {
		return getAllGameStates(visibleDiscard,
				(start) ->
					start.turnNumber() > infoSet.state().turnNumber() ||
					!infoSet.state().history().subList(0, start.turnNumber()).equals(start.history()))
				.stream().filter(infoSet::includes).collect(Collectors.toSet());
	}
	
	public static Map<FullGameState, Double> initialDistribution(Collection<Card> visibleDiscard) {
		return Distributions.fromMultiset(AI.getStartingStates(ImmutableList.copyOf(visibleDiscard)));
	}
	
	public Map<FullGameState, Double> getFullDistribution2(Collection<Card> visibleDiscard) {
		Map<FullGameState, Double> initialDistribution = initialDistribution(visibleDiscard);
		
		Map<FullGameState, Double> map = Distributions.expand(initialDistribution, this::getFullDistribution);
		
		return Collections.unmodifiableMap(map);
	}
	
	public static Map<FullGameState, Double> getFinalDistribution(Collection<Card> visibleDiscard, Policy p) {
		Map<FullGameState, Double> initialDistribution = initialDistribution(visibleDiscard);
		
		Map<FullGameState, Double> map =
				Distributions.expand(initialDistribution,
				s -> GameStateDistribution.getFinalDistribution(s, p));
		
		return Collections.unmodifiableMap(map);
	}
	
	public static Map<FullGameState, Double> getFinalDistribution(FullGameState state, Policy p) {
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
	
		Preconditions.checkArgument(state.deckSize() > 0);
		Preconditions.checkArgument(!state.hasJustDrawn());
		
		return Distributions.expand(calculateFullStepDistribution(state, p), s -> getFinalDistribution(s, p));
	}
	
	private static Map<FullGameState, Double> calculateHalfStepDistribution(FullGameState state, Policy p) {
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		if (state.hasJustDrawn()) {
			return calculateEndTurnDistribution(state, p);
		} else {
			return calculateStartTurnDistribution(state);
		}
	}

	private static Map<FullGameState, Double> calculateFullStepDistribution(FullGameState state, Policy p) {
		Preconditions.checkNotNull(state);
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkArgument(state.deckSize() > 0);
		
		Map<FullGameState, Double> map = Distributions.expand(calculateStartTurnDistribution(state),
				s -> calculateEndTurnDistribution(s, p));
		
		assert map.keySet().stream().allMatch(s -> !s.hasJustDrawn());
		
		return map;
	}

	private static Map<FullGameState, Double> calculateEndTurnDistribution(FullGameState state, Policy p) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(state.hasJustDrawn());
		
		if (state.winner().isPresent()) {
			return ImmutableMap.of(state, 1.0);
		}
		
		Map<FullGameState, Double> map = new HashMap<>();
		
		Map<Action, Double> choiceDistribution = 
				p.choiceDistribution(InformationSet.fromFullGameState(state, state.whoseTurn()));
		
		choiceDistribution = Distributions.normalize(choiceDistribution);
		
		for (Map.Entry<Action, Double> e : choiceDistribution.entrySet()) {
			/*if (e.getKey().card != Card.PRINCE || !FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK) {
				FullGameState s = state.endTurn(e.getKey());
				map.put(s, e.getValue());
			} else {
				Map<FullGameState, Double> gameDistribution = calculatePrinceDistribution(state, e.getKey());
				//System.out.println(gameDistribution);
				Distributions.mergeInByAdding(map, Maps.transformValues(gameDistribution, v -> v * e.getValue()));
			}*/
			Map<FullGameState, Double> endTurnDistribution = calculateEndTurnDistribution(state, e.getKey());

			Distributions.mergeInByAdding(map, Maps.transformValues(endTurnDistribution, v -> v * e.getValue()));
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	public static Map<FullGameState, Double> calculateEndTurnDistribution(FullGameState state, Action a) {
		if (a.card != Card.PRINCE || !FullGameState.TARGET_CARD_CONTROLS_PRINCE_PICK) {
			return ImmutableMap.of(state.endTurn(a), 1.0);
		} else {
			return calculatePrinceDistribution(state, a);
		}
	}
	
	private static Map<FullGameState, Double> calculatePrinceDistribution(FullGameState state, Action a) {
		Preconditions.checkArgument(a.card == Card.PRINCE);
		Preconditions.checkArgument(a.targetPlayer.isPresent());
		Preconditions.checkArgument(!a.targetCard.isPresent());
		
		Map<Card, Double> deck = Distributions.fromMultiset(state.deck());
		Map<FullGameState, Double> stateDistribution = new HashMap<>();
		
		for (Card c : deck.keySet()) {
			double value = deck.get(c);
			Action newAction = new Action(a.player, a.card, a.targetPlayer, Optional.of(c));
			FullGameState newState = state.endTurn(newAction);
			stateDistribution.put(newState, value);
		}
		
		return Collections.unmodifiableMap(stateDistribution);
	}

	private static Map<FullGameState, Double> calculateStartTurnDistribution(FullGameState state) {
		Preconditions.checkNotNull(state);
		Preconditions.checkArgument(!state.hasJustDrawn());
		Preconditions.checkArgument(!state.winner().isPresent());
	
		Preconditions.checkArgument(state.deckSize() > 0, state);
		
		Multiset<FullGameState> states = HashMultiset.create();
		
		for (Card c : state.deck()) {
			FullGameState s = state.startTurn(c);
			assert (s.hasJustDrawn());
			states.add(s);
		}
		
		assert !states.isEmpty();
		
		return Distributions.fromMultiset(states);
	}
}

class GSD2 {
	/*private static final LoadingCache<Policy, GameStateDistribution> gsdInstanceCache = 
			CacheBuilder.newBuilder()
		       //.maximumSize(3)
				.maximumSize(200)
		       .concurrencyLevel(40)
		       .build(
		           new CacheLoader<Policy, GameStateDistribution>() {
		             public GameStateDistribution load(Policy key)  {
		               return new GameStateDistribution(key);
		             }
		           });*/
	
	public final Collection<Card> visibleDiscard;
	public final GameStateDistribution distribution;
	
	private GSD2(Collection<Card> initDiscard, GameStateDistribution distribution) {
		this.visibleDiscard = ImmutableList.copyOf(initDiscard);
		this.distribution = distribution;
	}
	
	public static GSD2 create(Collection<Card> initDiscard, GameStateDistribution distribution) {
		return new GSD2(initDiscard, distribution);
	}
	
	public static GSD2 create(Collection<Card> initDiscard, Policy policy) {
		//return create(initDiscard, gsdInstanceCache.getUnchecked(policy));
		return create(initDiscard, new GameStateDistribution(policy));
	}
	
	public Set<FullGameState> getAllGameStates() {
		return distribution.getAllGameStates(visibleDiscard);
	}
	
	public Set<FullGameState> getAllGameStates(Predicate<FullGameState> exclude) {
		return distribution.getAllGameStates(visibleDiscard, exclude);
	}
	
	public Set<FullGameState> getAllGameStatesInInfoSet(InformationSet infoSet) {
		return distribution.getAllGameStatesInInfoSet(visibleDiscard, infoSet);
	}
	
	public double probabilityOfStateConditionGiven(Predicate<FullGameState> shouldQuit, Predicate<FullGameState> shouldInclude) {
		return distribution.probabilityOfStateConditionGiven(visibleDiscard, shouldQuit, shouldInclude);
	}
	
	public double probabilityOfFullGameStateGiven(FullGameState end) {
		return distribution.probabilityOfFullGameStateGiven(visibleDiscard, end);
	}
	
	public Map<FullGameState, Double> getProbabilities() {
		return distribution.getProbabilities(visibleDiscard);
	}
	
	public Map<FullGameState, Double> getFullDistribution(FullGameState history) {
		return distribution.getFullDistribution(history);
	}
	
	public Map<FullGameState, Double> getFullDistribution2() {
		return distribution.getFullDistribution2(visibleDiscard);
	}
	
	public Table<InformationSet, FullGameState, Double> overallInfoSets(Player perspective) {
		return InformationSets.getOverallInfoSets3(visibleDiscard, distribution, perspective);
	}
}
