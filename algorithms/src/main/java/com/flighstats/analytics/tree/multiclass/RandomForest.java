package com.flighstats.analytics.tree.multiclass;

import com.flighstats.analytics.tree.Item;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
public class RandomForest {
    private final List<DecisionTree> trees;

    public RandomForest(List<DecisionTree> trees) {
        this.trees = trees;
    }

    //todo: should this also return a confidence?
    Integer evaluate(Item item) {
        Map<Integer, Integer> countsByLabel = trees.parallelStream().map(tree -> tree.evaluate(item)).collect(Collectors.toMap(t -> t, r -> 1, (integer, integer2) -> integer + integer2));
        Integer mostCommonLabel = null;
        int numberOfMostCommonLabel = 0;
        for (Map.Entry<Integer, Integer> entry : countsByLabel.entrySet()) {
            if (entry.getValue() > numberOfMostCommonLabel) {
                numberOfMostCommonLabel = entry.getValue();
                mostCommonLabel = entry.getKey();
            }
        }
        return mostCommonLabel;
    }
}
