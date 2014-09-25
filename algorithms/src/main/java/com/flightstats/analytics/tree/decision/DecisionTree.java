package com.flightstats.analytics.tree.decision;

import lombok.Value;

@Value
public class DecisionTree {
    String name;
    TreeNode rootNode;

    Integer evaluate(Item item) {
        return rootNode.evaluate(item);
    }

    public void printStructure() {
        System.out.println(name + " : ");
        rootNode.printStructure(0);
    }
}
