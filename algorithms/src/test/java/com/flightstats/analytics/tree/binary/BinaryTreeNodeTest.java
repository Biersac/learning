package com.flightstats.analytics.tree.binary;

import com.flightstats.analytics.tree.Item;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryTreeNodeTest {

    @Test
    public void testLeafNode_true() throws Exception {
        BinaryTreeNode testClass = new BinaryTreeNode(true);
        boolean result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertTrue(result);
    }

    @Test
    public void testLeafNode_false() throws Exception {
        BinaryTreeNode testClass = new BinaryTreeNode(false);
        boolean result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertFalse(result);
    }

    @Test
    public void testTrueBranch() throws Exception {
        Map<Integer, BinaryTreeNode> branches = new HashMap<>();
        branches.put(0, new BinaryTreeNode(true));
        branches.put(1, new BinaryTreeNode(false));
        BinaryTreeNode testClass = new BinaryTreeNode("bitter", branches);

        Map<String, Integer> values = new HashMap<>();
        values.put("bitter", 1);
        boolean result = testClass.evaluate(new Item("one", values));
        assertFalse(result);
    }

    @Test
    public void testFalseBranch() throws Exception {
        Map<Integer, BinaryTreeNode> branches = new HashMap<>();
        branches.put(0, new BinaryTreeNode(true));
        branches.put(1, new BinaryTreeNode(false));
        BinaryTreeNode testClass = new BinaryTreeNode("bitter", branches);
        Map<String, Integer> values = new HashMap<>();
        values.put("bitter", 0);
        boolean result = testClass.evaluate(new Item("one", values));
        assertTrue(result);
    }


}