package com.flightstats.analytics.tree;

public interface TreeNode<T> {
    T evaluate(Item item);
}
