package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.MixedItem;
import lombok.Value;

@Value
public class RegressionTree {
    String name;
    RegressionTreeNode rootNode;

    public double evaluate(MixedItem item) {
        return rootNode.evaluate(item);
    }

}
