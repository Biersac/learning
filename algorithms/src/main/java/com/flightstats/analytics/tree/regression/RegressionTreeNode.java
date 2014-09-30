package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.MixedItem;

public interface RegressionTreeNode {
    Double evaluate(MixedItem item);
}
