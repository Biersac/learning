package com.flightstats.analytics.tree;

import lombok.Value;

@Value
public class ContinuousTreeNode<T> implements TreeNode<T> {
    String attribute;
    /**
     * Less than this goes right, otherwise goes right.
     */
    Double continuousSplitValue;
    TreeNode<T> left;
    TreeNode<T> right;

    @Override
    public T evaluate(Item item) {
        return choose(item).evaluate(item);
    }

    private TreeNode<T> choose(Item item) {
        Double value = item.getContinuousValue(attribute);
        boolean less = value < continuousSplitValue;
        return less ? left : right;
    }

}
