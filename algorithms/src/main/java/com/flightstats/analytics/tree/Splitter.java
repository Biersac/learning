package com.flightstats.analytics.tree;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Splitter<T> {
    private ContinuousSplit<T> split(List<LabeledItem<T>> items, String attribute, Double value) {
        List<LabeledItem<T>> left = items.stream().filter(i -> i.getContinuousValue(attribute) < value).collect(toList());
        List<LabeledItem<T>> right = items.stream().filter(i -> i.getContinuousValue(attribute) >= value).collect(toList());
        return new ContinuousSplit<>(attribute, value, left, right);
    }

    private DiscreteSplit<T> split(List<LabeledItem<T>> items, String attribute, Integer attributeValue) {
        List<LabeledItem<T>> left = items.stream().filter(i -> attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        List<LabeledItem<T>> right = items.stream().filter(i -> !attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        return new DiscreteSplit<>(attribute, attributeValue, left, right);
    }

    public List<DiscreteSplit<T>> possibleDiscreteSplits(List<LabeledItem<T>> items, String attribute) {
        Set<Integer> attributeValues = items.stream().map(i -> i.getDiscreteValue(attribute)).filter(Objects::nonNull).collect(toSet());
        return attributeValues.stream().map(av -> split(items, attribute, av)).collect(toList());
    }

    private List<Double> splitPoints(List<LabeledItem<T>> items, String attribute) {
        double[] values = items.stream()
                .map(i -> i.getContinuousValue(attribute))
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .distinct()
                .mapToDouble(d -> d)
                .toArray();
        List<Double> results = new ArrayList<>();
        for (int i = 1; i < values.length; i++) {
            double value1 = values[i - 1];
            double value2 = values[i];
            results.add((value2 + value1) / 2);
        }
        return results;
    }

    public List<ContinuousSplit<T>> possibleContinuousSplits(List<LabeledItem<T>> items, String attribute) {
        return splitPoints(items, attribute).stream().map(v -> split(items, attribute, v)).collect(toList());
    }
}
