package com.flightstats.analytics.tree.decision;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Map;

@Value
@AllArgsConstructor
public class TreeNode {
    String key;
    Integer label;
    Map<Integer, TreeNode> branches;

    public TreeNode(int label) {
        this(null, label, null);
    }

    public TreeNode(String key, Map<Integer, TreeNode> branches) {
        this(key, null, branches);
    }

    public Integer evaluate(Item item) {
        if (key == null) {
            return label;
        }
        Integer evaluation = item.value(key);
        TreeNode branch = branches.get(evaluation);
        if (branch == null) {
            //todo probably the default would be better here...
            return null;
        }
        return branch.evaluate(item);
    }

    public void printStructure(int depth) {
        if (key == null) {
            System.out.println(spaces(depth) + "label = " + label);
            return;
        }
        System.out.println(spaces(depth) + " " + key);
        for (Map.Entry<Integer, TreeNode> entry : branches.entrySet()) {
            System.out.println(spaces(depth) + "  " + entry.getKey() + " : ");
            entry.getValue().printStructure(depth + 1);
        }
    }

    private String spaces(int depth) {
        char[] chars = new char[2 + depth * 2];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
