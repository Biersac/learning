package com.flighstats.analytics.tree;

import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.*;

import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Humidity.HIGH;
import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Humidity.NORMAL;
import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Outlook.*;
import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Temp.*;
import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Wind.STRONG;
import static com.flighstats.analytics.tree.DecisionTreeTrainerTest.Wind.WEAK;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DecisionTreeTrainerTest {

    private EntropyCalculator entropyCalculator = mock(EntropyCalculator.class);

    @Test
    public void testBestGain() throws Exception {
        List<LabeledItem> items = Arrays.asList();
        when(entropyCalculator.entropyGain(items, "bitter")).thenReturn(0.05);
        when(entropyCalculator.entropyGain(items, "sweet")).thenReturn(0.06);
        when(entropyCalculator.entropyGain(items, "sour")).thenReturn(0.07);

        DecisionTreeTrainer testClass = new DecisionTreeTrainer(entropyCalculator);
        assertEquals(Optional.of((Object) "sour"), testClass.bestEntropyGain(items, Arrays.asList("bitter", "sour", "sweet"), 3));
    }

    @Test
    public void testSimpleExample() throws Exception {
        LabeledItem one = new LabeledItem(new Item("1", fruitData(true, true, false)), true);
        LabeledItem two = new LabeledItem(new Item("2", fruitData(true, true, false)), true);

        LabeledItem three = new LabeledItem(new Item("3", fruitData(false, true, true)), false);
        LabeledItem four = new LabeledItem(new Item("4", fruitData(false, false, true)), false);
        LabeledItem five = new LabeledItem(new Item("5", fruitData(true, false, true)), false);

        LabeledItem six = new LabeledItem(new Item("6", fruitData(true, true, true)), true);

        //we put 'one' in the list twice, to unbalance the weights and make bitter the best entropy gain. (otherwise, the resulting tree isn't deterministic)
        List<LabeledItem> labeledItems = Arrays.asList(one, one, two, three, four, five, six);

        DecisionTree tree = new DecisionTreeTrainer(new EntropyCalculator()).train("fruits", labeledItems, Arrays.asList("sweet", "sour", "bitter"));

        assertTrue(tree.evaluate(one.getItem()));
        assertTrue(tree.evaluate(two.getItem()));
        assertFalse(tree.evaluate(three.getItem()));
        assertFalse(tree.evaluate(four.getItem()));
        assertFalse(tree.evaluate(five.getItem()));
        assertTrue(tree.evaluate(six.getItem()));

        //we like these, because they aren't bitter (which is the strongest signal in the tree above).
        assertTrue(tree.evaluate(new Item("bland", fruitData(false, false, false))));
        assertTrue(tree.evaluate(new Item("sour", fruitData(false, true, false))));
    }

    private Map<Object, Integer> fruitData(boolean sweet, boolean sour, boolean bitter) {
        Map<Object, Integer> data = new HashMap<>();
        data.put("sour", sour ? 1 : 0);
        data.put("sweet", sweet ? 1 : 0);
        data.put("bitter", bitter ? 1 : 0);
        return data;
    }

    @Test
    public void testTennisExample() throws Exception {
        List<LabeledItem> trainingData = Arrays.asList(
                new LabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, WEAK)), false),
                new LabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, STRONG)), false),
                new LabeledItem(new Item("1", tennisData(OVERCAST, HOT, HIGH, WEAK)), true),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, WEAK)), true),

                new LabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, WEAK)), true),
                new LabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, STRONG)), false),
                new LabeledItem(new Item("1", tennisData(OVERCAST, COOL, NORMAL, STRONG)), true),
                new LabeledItem(new Item("1", tennisData(SUNNY, MILD, HIGH, WEAK)), false),

                new LabeledItem(new Item("1", tennisData(SUNNY, COOL, NORMAL, WEAK)), true),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, NORMAL, WEAK)), true),
                new LabeledItem(new Item("1", tennisData(SUNNY, MILD, NORMAL, STRONG)), true),
                new LabeledItem(new Item("1", tennisData(OVERCAST, MILD, HIGH, STRONG)), true),

                new LabeledItem(new Item("1", tennisData(OVERCAST, HOT, NORMAL, WEAK)), true),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, STRONG)), false)
        );

        DecisionTreeTrainer testClass = new DecisionTreeTrainer(new EntropyCalculator());
        List<Object> attributes = Arrays.asList("outlook", "temp", "humidity", "wind");
        Optional<Object> bestEntropyGain = testClass.bestEntropyGain(trainingData, attributes, 4);
        assertEquals(Optional.<Object>of("outlook"), bestEntropyGain);

        DecisionTree tennis = testClass.train("tennis", trainingData, attributes);
//        tennis.printStructure();

        assertFalse(tennis.evaluate(new Item("2", tennisData(RAIN, HOT, HIGH, STRONG))));
        //todo: figure out some assertions to make here?
    }

    private Map<Object, Integer> tennisData(Outlook outlook, Temp temperature, Humidity humidity, Wind wind) {
        Map<Object, Integer> data = new HashMap<>();
        data.put("outlook", outlook.value);
        data.put("temp", temperature.value);
        data.put("humidity", humidity.value);
        data.put("wind", wind.value);
        return data;
    }

    @AllArgsConstructor
    static enum Outlook {
        SUNNY(1), OVERCAST(2), RAIN(3);
        int value;
    }

    @AllArgsConstructor
    static enum Temp {
        HOT(1), MILD(2), COOL(3);
        int value;
    }

    @AllArgsConstructor
    static enum Humidity {
        HIGH(1), NORMAL(2);
        int value;
    }

    @AllArgsConstructor
    static enum Wind {
        WEAK(1), STRONG(2);
        int value;
    }

}