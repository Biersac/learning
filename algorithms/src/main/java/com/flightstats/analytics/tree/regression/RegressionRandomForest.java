package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.MixedItem;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class RegressionRandomForest {
    private final List<RegressionTree> trees;

    public RegressionRandomForest(List<RegressionTree> trees) {
        this.trees = trees;
    }

    //todo: should this also return a confidence?
    public Double evaluate(MixedItem item) {
        return trees.parallelStream()
                .map(tree -> tree.evaluate(item))
                .mapToDouble(d -> d)
                .average().getAsDouble();
    }
}
