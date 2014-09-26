package com.flightstats.analytics.tree.regression;

import lombok.Value;

@Value
public class LeafNode implements RegressionTreeNode {
    Double responseValue;

    @Override
    public Double evaluate(MixedItem item) {
        return responseValue;
    }

}
