package com.flightstats.analytics.tree;

import lombok.Value;

@Value
public class DiscreteTreeNode<T> implements TreeNode<T> {
    String attribute;
    /**
     * Equal to this goes left, otherwise goes right.
     */
    Integer discreteSplitValue;
    TreeNode<T> left;
    TreeNode<T> right;

    @Override
    public T evaluate(MixedItem item) {
        return choose(item).evaluate(item);
    }

    private TreeNode<T> choose(MixedItem item) {
        Integer value = item.getDiscreteValue(attribute);

        boolean equals = discreteSplitValue.equals(value);
        return equals ? left : right;
    }

}
