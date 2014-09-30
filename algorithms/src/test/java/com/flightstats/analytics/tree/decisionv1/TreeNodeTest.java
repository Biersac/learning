package com.flightstats.analytics.tree.decisionv1;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TreeNodeTest {

    @Test
    public void testLeafNode_true() throws Exception {
        TreeNode testClass = new TreeNode(1);
        int result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertEquals(1, result);
    }

    @Test
    public void testLeafNode_false() throws Exception {
        TreeNode testClass = new TreeNode(0);
        int result = testClass.evaluate(new Item("one", new HashMap<>()));
        assertEquals(0, result);
    }

    @Test
    public void testTrueBranch() throws Exception {
        Map<Integer, TreeNode> branches = new HashMap<>();
        branches.put(0, new TreeNode(1));
        branches.put(1, new TreeNode(0));
        TreeNode testClass = new TreeNode("bitter", branches);

        Map<String, Integer> values = new HashMap<>();
        values.put("bitter", 1);
        int result = testClass.evaluate(new Item("one", values));
        assertEquals(0, result);
    }

    @Test
    public void testFalseBranch() throws Exception {
        Map<Integer, TreeNode> branches = new HashMap<>();
        branches.put(0, new TreeNode(1));
        branches.put(1, new TreeNode(0));
        TreeNode testClass = new TreeNode("bitter", branches);
        Map<String, Integer> values = new HashMap<>();
        values.put("bitter", 0);
        int result = testClass.evaluate(new Item("one", values));
        assertEquals(1, result);
    }

    @Test
    public void testMultiBranch() throws Exception {
        Map<Integer, TreeNode> branches = new HashMap<>();
        branches.put(0, new TreeNode(1));
        branches.put(2, new TreeNode(4));
        branches.put(1, new TreeNode(0));
        TreeNode testClass = new TreeNode("bitter", branches);
        Map<String, Integer> values = new HashMap<>();
        values.put("bitter", 2);
        int result = testClass.evaluate(new Item("one", values));
        assertEquals(4, result);
    }

}