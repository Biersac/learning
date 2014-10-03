package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

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
    public T evaluate(Item item) {
        return choose(item).evaluate(item);
    }

    @Override
    public List<TreeNode<T>> putDown(Item item, List<TreeNode<T>> accumulator) {
        accumulator.add(0, this);
        return choose(item).putDown(item, accumulator);
    }

    private TreeNode<T> choose(Item item) {
        Integer value = item.getDiscreteValue(attribute);

        boolean equals = discreteSplitValue.equals(value);
        return equals ? left : right;
    }

}
