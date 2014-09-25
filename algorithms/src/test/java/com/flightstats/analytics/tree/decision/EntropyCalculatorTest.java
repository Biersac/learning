package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.Item;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EntropyCalculatorTest {

    @Test
    public void testEntropy_50_50() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> positiveValues = new HashMap<>();
        positiveValues.put("sweet", 1);
        Map<String, Integer> negativeValues = new HashMap<>();
        negativeValues.put("sweet", 0);

        List<LabeledItem> input = Arrays.asList(
                //first 3 don't match, so should be ignored
                new LabeledItem(new Item("one", negativeValues), 1),
                new LabeledItem(new Item("one", negativeValues), 1),
                new LabeledItem(new Item("one", negativeValues), 1),
                new LabeledItem(new Item("one", positiveValues), 1),
                new LabeledItem(new Item("one", positiveValues), 1),
                new LabeledItem(new Item("one", positiveValues), 1),
                new LabeledItem(new Item("one", positiveValues), 0),
                new LabeledItem(new Item("one", positiveValues), 0),
                new LabeledItem(new Item("one", positiveValues), 0)
        );
        double entropy = testClass.entropy(input, "sweet", 1);
        assertEquals(1, entropy, 0.0001);
    }

    @Test
    public void testEntropy_allPositive() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> positiveValues = new HashMap<>();
        positiveValues.put("sweet", 1);
        Map<String, Integer> negativeValues = new HashMap<>();
        negativeValues.put("sweet", 0);

        double entropy = testClass.entropy(Arrays.asList(
                        //first 3 don't match, so should be ignored
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),

                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1)
                ),
                "sweet", 1);
        assertEquals(0, entropy, 0.0001);
    }

    @Test
    public void testEntropy_allNegative() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> positiveValues = new HashMap<>();
        positiveValues.put("sweet", 1);
        Map<String, Integer> negativeValues = new HashMap<>();
        negativeValues.put("sweet", 0);

        double entropy = testClass.entropy(Arrays.asList(
                        //first 3 don't match, so should be ignored
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),

                        new LabeledItem(new Item("one", negativeValues), 0),
                        new LabeledItem(new Item("one", negativeValues), 0),
                        new LabeledItem(new Item("one", negativeValues), 0)
                ),
                "sweet", 0);
        assertEquals(0, entropy, 0.0001);
    }

    @Test
    public void testEntropy_9_5() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> negativeValues = new HashMap<>();
        negativeValues.put("sweet", 0);
        Map<String, Integer> positiveValues = new HashMap<>();
        positiveValues.put("sweet", 1);

        double entropy = testClass.entropy(Arrays.asList(
                        //first 3 don't match, so should be ignored
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),

                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1)
                ),
                "sweet", 1);
        assertEquals(0.9403, entropy, 0.0001);
    }

    @Test
    public void testLabelEntropy_9_5() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> values = new HashMap<>();

        double entropy = testClass.labelEntropy(Arrays.asList(
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 1),
                new LabeledItem(new Item("one", values), 0),
                new LabeledItem(new Item("one", values), 0),
                new LabeledItem(new Item("one", values), 0),
                new LabeledItem(new Item("one", values), 0),
                new LabeledItem(new Item("one", values), 0)
        ));
        assertEquals(0.9403, entropy, 0.0001);
    }


    @Test
    public void testEntropyGain_9_5() throws Exception {
        EntropyCalculator testClass = new EntropyCalculator();
        Map<String, Integer> negativeValues = new HashMap<>();
        negativeValues.put("sweet", 0);
        Map<String, Integer> positiveValues = new HashMap<>();
        positiveValues.put("sweet", 1);

        double gain = testClass.entropyGain(Arrays.asList(
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", positiveValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),
                        new LabeledItem(new Item("one", negativeValues), 1),

                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", positiveValues), 0),
                        new LabeledItem(new Item("one", negativeValues), 0),
                        new LabeledItem(new Item("one", negativeValues), 0),
                        new LabeledItem(new Item("one", negativeValues), 0)
                ),
                "sweet");
        assertEquals(0.0481, gain, 0.0001);
    }


}