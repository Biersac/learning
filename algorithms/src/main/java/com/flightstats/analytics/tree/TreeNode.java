package com.flightstats.analytics.tree;

import java.util.List;

public interface TreeNode<T> {
    T evaluate(Item item);

    List<TreeNode<T>> putDown(Item item, List<TreeNode<T>> accumulator);
}
