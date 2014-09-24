package com.flighstats.analytics.tree.binary;

import com.flighstats.analytics.tree.Item;
import lombok.Value;

@Value
public class BinaryDecisionTree {
    String name;
    BinaryTreeNode rootNode;

    boolean evaluate(Item item) {
        return rootNode.evaluate(item);
    }

    public void printStructure() {
        System.out.println(name + " : ");
        rootNode.printStructure(0);
    }
}
