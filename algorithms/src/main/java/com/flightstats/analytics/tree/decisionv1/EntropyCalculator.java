package com.flightstats.analytics.tree.decisionv1;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class EntropyCalculator {
    public static final double LOG_2 = Math.log(2);

    double entropy(List<LabeledItem> items, String attribute, Integer attributeValue) {
        List<LabeledItem> matchingItems = items.stream().filter(i -> attributeValue.equals(i.value(attribute))).collect(toList());

        Double[] cs = getCountsByCategory(matchingItems);

        double totalItems = matchingItems.size();
        return entropy(totalItems, cs);
    }

    private Double[] getCountsByCategory(List<LabeledItem> items) {
        //I'm sure there's a clever functional way to do this. todo: figure it out.
        Map<Integer, Double> countsByCategory = new HashMap<>();
        for (LabeledItem item : items) {
            Double count = countsByCategory.getOrDefault(item.getLabel(), 0d);
            count = count + 1;
            countsByCategory.put(item.getLabel(), count);
        }
        Collection<Double> counts = countsByCategory.values();
        return counts.toArray(new Double[counts.size()]);
    }

    private double entropy(double totalItems, Double[] numbersInEachClass) {
        double totalEntropy = 0;
        for (Double number : numbersInEachClass) {
            double fractionInClassI = number / totalItems;
            totalEntropy -= fractionInClassI * log2(fractionInClassI);
        }
        return totalEntropy;
    }

    double labelEntropy(List<LabeledItem> items) {
        return entropy(items.size(), getCountsByCategory(items));
    }

    Double entropyGain(List<LabeledItem> items, String attribute) {
        Set<Integer> values = items.stream().map(li -> li.value(attribute)).collect(toSet());

        double labelEntropy = labelEntropy(items);
        int totalItems = items.size();
        for (Integer value : values) {
            double numberMatching = items.stream().filter(i -> value.equals(i.value(attribute))).count();
            double fractionMatching = numberMatching / totalItems;
            labelEntropy = labelEntropy - entropy(items, attribute, value) * fractionMatching;
        }
        return labelEntropy;
    }

    private double log2(double number) {
        //not actually true, but for this algorithm, it works fine.
        if (number == 0) {
            return 0;
        }
        return Math.log(number) / LOG_2;
    }


}
