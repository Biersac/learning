package com.flightstats.analytics.tree.regression;

public interface RegressionTreeNode {
    Double evaluate(MixedItem item);
}
