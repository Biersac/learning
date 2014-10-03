package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Tree<T> {
    String name;
    TreeNode<T> rootNode;

    public T evaluate(Item item) {
        return rootNode.evaluate(item);
    }

    /**
     * Returns the path of nodes that an item went down as it was put down the tree.
     */
    public List<TreeNode<T>> putDown(Item item) {
        return rootNode.putDown(item, new ArrayList<>());
    }

}
