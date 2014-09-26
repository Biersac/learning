package com.flightstats.analytics.tree.regression;

import lombok.Value;

@Value
public class DiscreteTreeNode implements RegressionTreeNode {
    String attribute;
    /**
     * Equal to this goes left, otherwise goes right.
     */
    Integer discreteSplitValue;
    RegressionTreeNode left;
    RegressionTreeNode right;

    @Override
    public Double evaluate(MixedItem item) {
        return choose(item).evaluate(item);
    }

    private RegressionTreeNode choose(MixedItem item) {
        Integer value = item.getDiscreteValue(attribute);
        return discreteSplitValue.equals(value) ? left : right;
    }

}
