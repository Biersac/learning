package com.flighstats.analytics.tree;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Map;

@Value
@AllArgsConstructor
public class TreeNode {
    Object key;
    boolean label;
    Map<Integer, TreeNode> branches;

    public TreeNode(boolean label) {
        this(null, label, null);
    }

    public TreeNode(Object key, Map<Integer, TreeNode> branches) {
        this(key, false, branches);
    }

    public boolean evaluate(Item item) {
        if (key == null) {
            return label;
        }
        Integer evaluation = item.evaluate(key);
        TreeNode branch = branches.get(evaluation);
        if (branch == null) {
            /*
            TODO: what is the *proper* response, when we didn't seen this value in this branch during training?

                The "right" answer is that we should return the more common label, from the training data, according to:
                    http://www.cs.princeton.edu/courses/archive/spr07/cos424/papers/mitchell-dectrees.pdf

                However, it seems to me that this assumes that the training data is somehow representative, in
                proportion, to the true population. This seems like a flawed assumption to me, especially, on hand-
                selected, hand-labeled training data.  More thought needs to go into what should happen here.
                An assumption of negativity doesn't seem all that bad to me in many problems. However, this seems
                to be a case where it greatly depends on the specific problem at hand. Are we trying to find positive needles
                in haystacks, or are we trying to sort roughly equally-prevalent classes? Or are we trying to only
                find the outliers (which might be considered "negative" in some sense, depending on how the training
                regime and data were assembled.
            */
            return false;
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
