package com.flighstats.analytics.tree;

import lombok.Value;

@Value
public class DecisionTree {
    String name;
    TreeNode rootNode;

    boolean evaluate(Item item) {
        return rootNode.evaluate(item);
    }

    public void printStructure() {
        System.out.println(name + " : ");
        rootNode.printStructure(0);
    }
}
