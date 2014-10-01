package com.flightstats.analytics.tree;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SplitterTest {

    @Test
    public void testSimpleDiscrete() throws Exception {
        Splitter<Integer> testClass = new Splitter<>();
        Map<String, Integer> discreteValues1 = new HashMap<>();
        discreteValues1.put("cheese", 1);
        Map<String, Integer> discreteValues2 = new HashMap<>();
        discreteValues2.put("cheese", 2);
        LabeledItem<Integer> item1 = new LabeledItem<>(new Item("1", discreteValues1, new HashMap<>()), 1);
        LabeledItem<Integer> item2 = new LabeledItem<>(new Item("2", discreteValues2, new HashMap<>()), 1);
        List<DiscreteSplit<Integer>> splits = testClass.possibleDiscreteSplits(Arrays.asList(item1, item2), "cheese");
        assertEquals(Arrays.asList(
                new DiscreteSplit<>("cheese", 1, Arrays.asList(item1), Arrays.asList(item2)),
                new DiscreteSplit<>("cheese", 2, Arrays.asList(item2), Arrays.asList(item1))
        ), splits);
    }

    @Test
    public void testSimpleContinuous() throws Exception {
        Splitter<Integer> testClass = new Splitter<>();
        Map<String, Double> continuousValues1 = new HashMap<>();
        continuousValues1.put("cheese", 1.0);
        Map<String, Double> continuousValues2 = new HashMap<>();
        continuousValues2.put("cheese", 2.0);
        LabeledItem<Integer> item1 = new LabeledItem<>(new Item("1", new HashMap<>(), continuousValues1), 1);
        LabeledItem<Integer> item2 = new LabeledItem<>(new Item("2", new HashMap<>(), continuousValues2), 1);
        List<ContinuousSplit<Integer>> splits = testClass.possibleContinuousSplits(Arrays.asList(item1, item2), "cheese");
        assertEquals(Arrays.asList(
                new ContinuousSplit<>("cheese", 1.5, Arrays.asList(item1), Arrays.asList(item2))
        ), splits);
    }

}