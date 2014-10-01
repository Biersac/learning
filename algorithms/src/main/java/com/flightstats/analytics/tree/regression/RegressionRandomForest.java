package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.Tree;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class RegressionRandomForest {
    private final List<Tree<Double>> trees;

    public RegressionRandomForest(List<Tree<Double>> trees) {
        this.trees = trees;
    }

    //todo: should this also return a confidence?
    public Double evaluate(Item item) {
        return trees.parallelStream()
                .map(tree -> tree.evaluate(item))
                .mapToDouble(d -> d)
                .average().getAsDouble();
    }
}
