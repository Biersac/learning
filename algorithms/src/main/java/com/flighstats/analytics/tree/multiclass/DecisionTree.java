package com.flighstats.analytics.tree.multiclass;

import com.flighstats.analytics.tree.Item;
import com.flighstats.analytics.tree.binary.BinaryTreeNode;
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
