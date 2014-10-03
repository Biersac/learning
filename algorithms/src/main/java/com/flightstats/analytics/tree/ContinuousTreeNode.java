package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

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

    @Override
    public List<TreeNode<T>> putDown(Item item, List<TreeNode<T>> accumulator) {
        accumulator.add(0, this);
        return choose(item).putDown(item, accumulator);
    }

    private TreeNode<T> choose(Item item) {
        Double value = item.getContinuousValue(attribute);
        boolean less = value < continuousSplitValue;
        return less ? left : right;
    }

}
