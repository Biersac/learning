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

        boolean equals = discreteSplitValue.equals(value);
//        if (equals) {
//            System.out.println("" + attribute + " = " + discreteSplitValue + ". choosing left.");
//        } else {
//            System.out.println("" + attribute + " != " + discreteSplitValue + ". choosing right.");
//        }
        return equals ? left : right;
    }

}
