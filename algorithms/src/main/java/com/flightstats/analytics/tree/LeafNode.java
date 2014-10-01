package com.flightstats.analytics.tree;

import lombok.Value;

@Value
public class LeafNode<T> implements TreeNode<T> {
    T responseValue;

    @Override
    public T evaluate(MixedItem item) {
        return responseValue;
    }

}
