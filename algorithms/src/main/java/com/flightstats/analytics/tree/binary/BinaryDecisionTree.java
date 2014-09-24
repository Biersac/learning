package com.flightstats.analytics.tree.binary;

import com.flightstats.analytics.tree.Item;
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
