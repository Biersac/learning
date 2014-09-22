package com.flighstats.analytics.tree;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode
@ToString
public class RandomForest {
    private final List<DecisionTree> trees;

    public RandomForest(List<DecisionTree> trees) {
        this.trees = trees;
    }

    //todo: should this also return a confidence?
    boolean evaluate(Item item) {
        AtomicInteger positives = new AtomicInteger(0);
        AtomicInteger negatives = new AtomicInteger(0);
        trees.parallelStream().forEach(dt -> {
            if (dt.evaluate(item)) {
                positives.incrementAndGet();
            } else {
                negatives.incrementAndGet();
            }
        });
        return positives.get() > negatives.get();
    }
}
