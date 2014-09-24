package com.flightstats.analytics.tree.binary;

import com.flightstats.analytics.tree.Item;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.*;

import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Humidity.HIGH;
import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Humidity.NORMAL;
import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Outlook.*;
import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Temp.*;
import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Wind.STRONG;
import static com.flightstats.analytics.tree.binary.BinaryDecisionTreeTrainerTest.Wind.WEAK;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BinaryDecisionTreeTrainerTest {

    private BinaryEntropyCalculator entropyCalculator = mock(BinaryEntropyCalculator.class);

    @Test
    public void testBestGain() throws Exception {
        List<BinaryLabeledItem> items = Arrays.asList();
        when(entropyCalculator.entropyGain(items, "bitter")).thenReturn(0.05);
        when(entropyCalculator.entropyGain(items, "sweet")).thenReturn(0.06);
        when(entropyCalculator.entropyGain(items, "sour")).thenReturn(0.07);

        BinaryDecisionTreeTrainer testClass = new BinaryDecisionTreeTrainer(entropyCalculator);
        assertEquals(Optional.of((Object) "sour"), testClass.bestEntropyGain(items, Arrays.asList("bitter", "sour", "sweet"), 3));
    }

    @Test
    public void testSimpleExample() throws Exception {
        BinaryLabeledItem one = new BinaryLabeledItem(new Item("1", fruitData(true, true, false)), true);
        BinaryLabeledItem two = new BinaryLabeledItem(new Item("2", fruitData(true, true, false)), true);

        BinaryLabeledItem three = new BinaryLabeledItem(new Item("3", fruitData(false, true, true)), false);
        BinaryLabeledItem four = new BinaryLabeledItem(new Item("4", fruitData(false, false, true)), false);
        BinaryLabeledItem five = new BinaryLabeledItem(new Item("5", fruitData(true, false, true)), false);

        BinaryLabeledItem six = new BinaryLabeledItem(new Item("6", fruitData(true, true, true)), true);

        //we put 'one' in the list twice, to unbalance the weights and make bitter the best entropy gain. (otherwise, the resulting tree isn't deterministic)
        List<BinaryLabeledItem> labeledItems = Arrays.asList(one, one, two, three, four, five, six);

        BinaryDecisionTree tree = new BinaryDecisionTreeTrainer(new BinaryEntropyCalculator()).train("fruits", labeledItems, Arrays.asList("sweet", "sour", "bitter"));

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

    private Map<String, Integer> fruitData(boolean sweet, boolean sour, boolean bitter) {
        Map<String, Integer> data = new HashMap<>();
        data.put("sour", sour ? 1 : 0);
        data.put("sweet", sweet ? 1 : 0);
        data.put("bitter", bitter ? 1 : 0);
        return data;
    }

    @Test
    public void testTennisExample() throws Exception {
        List<BinaryLabeledItem> trainingData = Arrays.asList(
                new BinaryLabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, WEAK)), false),
                new BinaryLabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, STRONG)), false),
                new BinaryLabeledItem(new Item("1", tennisData(OVERCAST, HOT, HIGH, WEAK)), true),
                new BinaryLabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, WEAK)), true),

                new BinaryLabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, WEAK)), true),
                new BinaryLabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, STRONG)), false),
                new BinaryLabeledItem(new Item("1", tennisData(OVERCAST, COOL, NORMAL, STRONG)), true),
                new BinaryLabeledItem(new Item("1", tennisData(SUNNY, MILD, HIGH, WEAK)), false),

                new BinaryLabeledItem(new Item("1", tennisData(SUNNY, COOL, NORMAL, WEAK)), true),
                new BinaryLabeledItem(new Item("1", tennisData(RAIN, MILD, NORMAL, WEAK)), true),
                new BinaryLabeledItem(new Item("1", tennisData(SUNNY, MILD, NORMAL, STRONG)), true),
                new BinaryLabeledItem(new Item("1", tennisData(OVERCAST, MILD, HIGH, STRONG)), true),

                new BinaryLabeledItem(new Item("1", tennisData(OVERCAST, HOT, NORMAL, WEAK)), true),
                new BinaryLabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, STRONG)), false)
        );

        BinaryDecisionTreeTrainer testClass = new BinaryDecisionTreeTrainer(new BinaryEntropyCalculator());
        List<String> attributes = Arrays.asList("outlook", "temp", "humidity", "wind");
        Optional<String> bestEntropyGain = testClass.bestEntropyGain(trainingData, attributes, 4);
        assertEquals(Optional.<Object>of("outlook"), bestEntropyGain);

        BinaryDecisionTree tennis = testClass.train("tennis", trainingData, attributes);
//        tennis.printStructure();

        assertFalse(tennis.evaluate(new Item("2", tennisData(RAIN, HOT, HIGH, STRONG))));
        //todo: figure out some assertions to make here?
    }

    private Map<String, Integer> tennisData(Outlook outlook, Temp temperature, Humidity humidity, Wind wind) {
        Map<String, Integer> data = new HashMap<>();
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