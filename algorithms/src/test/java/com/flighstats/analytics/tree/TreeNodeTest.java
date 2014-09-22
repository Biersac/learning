package com.flighstats.analytics.tree;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeNodeTest {

    @Test
    public void testLeafNode_true() throws Exception {
        TreeNode testClass = new TreeNode(true);
        boolean result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertTrue(result);
    }

    @Test
    public void testLeafNode_false() throws Exception {
        TreeNode testClass = new TreeNode(false);
        boolean result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertFalse(result);
    }

    @Test
    public void testTrueBranch() throws Exception {
        Map<Integer, TreeNode> branches = new HashMap<>();
        branches.put(0, new TreeNode(true));
        branches.put(1, new TreeNode(false));
        TreeNode testClass = new TreeNode("bitter", branches);

        Map<Object, Integer> values = new HashMap<>();
        values.put("bitter", 1);
        boolean result = testClass.evaluate(new Item("one", values));
        assertFalse(result);
    }

    @Test
    public void testFalseBranch() throws Exception {
        Map<Integer, TreeNode> branches = new HashMap<>();
        branches.put(0, new TreeNode(true));
        branches.put(1, new TreeNode(false));
        TreeNode testClass = new TreeNode("bitter", branches);
        Map<Object, Integer> values = new HashMap<>();
        values.put("bitter", 0);
        boolean result = testClass.evaluate(new Item("one", values));
        assertTrue(result);
    }


}