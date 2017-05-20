package com.github.raptortechjs.LoveLetter.TwoPlayer.AI;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

public class Distributions {
	public static <T> Map<T, Double> expand(Map<T, Double> initialDistribution, Function<T, Map<T, Double>> expander) {
		return expand2(initialDistribution, expander);
	}
	
	public static <K, V> Map<V, Double> expand2(Map<K, Double> initialDistribution, Function<K, Map<V, Double>> expander) {
		Map<K, Double> starting = initialDistribution;
		
		Map<V, Double> results = new HashMap<>();
		
		for (Map.Entry<K, Double> e : starting.entrySet()) {
			K curState = e.getKey();
			double curVal = e.getValue();
			
			Map<V, Double> temp1 = expander.apply(curState);
			Map<V, Double> temp2 = Maps.transformValues(temp1, v -> v * curVal);
	
			for (Map.Entry<V, Double> e2 : temp2.entrySet()) {
				results.merge(e2.getKey(), e2.getValue(), Double::sum);
			}
		}
		
		return Collections.unmodifiableMap(results);
	}

	public static <T> Map<T, Double> expandParallel(Map<T, Double> initialDistribution, Function<T, Map<T, Double>> expander) {
		return initialDistribution
			.entrySet()
			.stream()
			.parallel()
			.map(e -> Maps.transformValues(expander.apply(e.getKey()), (Double v) -> v * e.getValue()))
			.reduce(ImmutableMap.of(), (accumulator, result) -> {
				Map<T, Double> temp = new HashMap<>(accumulator);
				for (Map.Entry<T, Double> e2 : result.entrySet()) {
					temp.merge(e2.getKey(), e2.getValue(), Double::sum);
				}
				return Collections.unmodifiableMap(temp);
			});
	}

	public static <T> Map<T, Double> fromMultiset(Multiset<T> multiset) {
		Preconditions.checkNotNull(multiset);
		Preconditions.checkArgument(multiset.size() > 0);
		
		ImmutableMap.Builder<T, Double> frequencyMap = ImmutableMap.<T, Double>builder();
		for (Multiset.Entry<T> e : multiset.entrySet()) {
			double frequency = e.getCount() * 1.0 / multiset.size();
			frequencyMap.put(e.getElement(), frequency);
		}
		
		return frequencyMap.build();
	}
	
	public static <T> Set<Multiset<T>> multiPowerSet(Multiset<T> multiset) {
		//System.out.println("elementset: " + ImmutableList.copyOf(multiset.elementSet()));
		return multiPowerSet(multiset, ImmutableList.copyOf(multiset.elementSet()), ImmutableMultiset.of());
	}
	
	private static <T> Set<Multiset<T>> multiPowerSet(Multiset<T> origin, List<T> elementsToConsider, Multiset<T> soFar) {
		if (elementsToConsider.isEmpty()) {
			return ImmutableSet.of(soFar);
		}
		
		T element = elementsToConsider.get(0);
		List<T> remaining = elementsToConsider.subList(1, elementsToConsider.size());

		//System.out.println(element + " : " + remaining);
		
		Set<Multiset<T>> result = new LinkedHashSet<>();
		
		for (int i=0; i <= origin.count(element); i++) {
			Multiset<T> current = ImmutableMultiset.<T>builder().addAll(soFar).addCopies(element, i).build();
			Set<Multiset<T>> temp = multiPowerSet(origin, remaining, current);
			result.addAll(temp);
		}
		
		return result;
	}

	public static <T> Map<T, Double> normalize(Map<T, Double> distribution) {
		Preconditions.checkNotNull(distribution);
		//System.out.println(distribution);
		assert distribution.size() != 0;
		double sum = sum(distribution);
		Map<T, Double> newDistribution = Maps.transformValues(distribution, v -> v / sum);
		return Collections.unmodifiableMap(newDistribution);
	}
	
	public static <T> double sum(Map<T, Double> distribution) {
		return distribution.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
	}
	
	public static <T> Map<T, Double> mergeByAdding(Map<T, Double> distribution1, Map<T, Double> distribution2) {
		Map<T, Double> result = new HashMap<>(distribution1);
		mergeInByAdding(result, distribution2);
		return Collections.unmodifiableMap(result);
	}
	
	public static <T> void mergeInByAdding(Map<T, Double> distributionToModify, Map<T, Double> distributionToAdd) {
		for (Map.Entry<T, Double> e : distributionToAdd.entrySet()) {
			distributionToModify.merge(e.getKey(), e.getValue(), Double::sum);
		}
	}

}
