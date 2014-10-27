package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.Tree;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
public class RandomForest {
    private final Collection<Tree<Integer>> trees;
    private final Integer defaultLabel;

    public RandomForest(Collection<Tree<Integer>> trees, Integer defaultLabel) {
        this.trees = trees;
        this.defaultLabel = defaultLabel;
    }

    //todo: should this also return a confidence?
    public Integer evaluate(Item item) {
        Map<Integer, Integer> countsByLabel = trees.parallelStream().map(tree -> tree.evaluate(item)).collect(Collectors.toMap(t -> t, r -> 1, (integer, integer2) -> integer + integer2));
        Integer mostCommonLabel = null;
        int numberOfMostCommonLabel = 0;
        for (Map.Entry<Integer, Integer> entry : countsByLabel.entrySet()) {
            //don't let the default label vote.
            if (entry.getValue() > numberOfMostCommonLabel && !defaultLabel.equals(entry.getKey())) {
                numberOfMostCommonLabel = entry.getValue();
                mostCommonLabel = entry.getKey();
            }
        }
        return mostCommonLabel == null ? defaultLabel : mostCommonLabel;
    }

    public int size() {
        return trees.size();
    }
}
