package com.flightstats.analytics.tree;

import lombok.Value;

@Value
public class Tree<T> {
    String name;
    TreeNode<T> rootNode;

    public T evaluate(Item item) {
        return rootNode.evaluate(item);
    }

}
