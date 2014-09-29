package com.flightstats.analytics.tree.regression;

import lombok.Value;

@Value
public class ContinuousTreeNode implements RegressionTreeNode {
    String attribute;
    /**
     * Less than this goes right, otherwise goes right.
     */
    Double continuousSplitValue;
    RegressionTreeNode left;
    RegressionTreeNode right;

    @Override
    public Double evaluate(MixedItem item) {
        return choose(item).evaluate(item);
    }

    private RegressionTreeNode choose(MixedItem item) {
        Double value = item.getContinuousValue(attribute);
        boolean less = value < continuousSplitValue;
//        if (less) {
//            System.out.println("" + attribute + " < " + continuousSplitValue + ". choosing left.");
//        } else {
//            System.out.println("" + attribute + " >= " + continuousSplitValue + ". choosing right.");
//        }

        return less ? left : right;
    }

}
