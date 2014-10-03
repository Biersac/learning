package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

@Value
public class LeafNode<T> implements TreeNode<T> {
    T responseValue;

    @Override
    public T evaluate(Item item) {
        return responseValue;
    }

    @Override
    public List<TreeNode<T>> putDown(Item item, List<TreeNode<T>> accumulator) {
        accumulator.add(0, this);
        return accumulator;
    }

}
