package com.flightstats.analytics.tree;

import com.flightstats.analytics.tree.regression.LabeledMixedItem;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Splitter {
    private ContinuousSplit split(List<LabeledMixedItem> items, String attribute, Double value) {
        List<LabeledMixedItem> left = items.stream().filter(i -> i.getContinuousValue(attribute) < value).collect(toList());
        List<LabeledMixedItem> right = items.stream().filter(i -> i.getContinuousValue(attribute) >= value).collect(toList());
        return new ContinuousSplit(attribute, value, left, right);
    }

    private DiscreteSplit split(List<LabeledMixedItem> items, String attribute, Integer attributeValue) {
        List<LabeledMixedItem> left = items.stream().filter(i -> attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        List<LabeledMixedItem> right = items.stream().filter(i -> !attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        return new DiscreteSplit(attribute, attributeValue, left, right);
    }

    public List<DiscreteSplit> possibleDiscreteSplits(List<LabeledMixedItem> items, String attribute) {
        Set<Integer> attributeValues = items.stream().map(i -> i.getDiscreteValue(attribute)).filter(Objects::nonNull).collect(toSet());
        return attributeValues.stream().map(av -> split(items, attribute, av)).collect(toList());
    }

    private List<Double> splitPoints(List<LabeledMixedItem> items, String attribute) {
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

    public List<ContinuousSplit> possibleContinuousSplits(List<LabeledMixedItem> items, String attribute) {
        return splitPoints(items, attribute).stream().map(v -> split(items, attribute, v)).collect(toList());
    }
}
