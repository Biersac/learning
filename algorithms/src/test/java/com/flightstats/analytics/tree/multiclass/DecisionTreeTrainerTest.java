package com.flightstats.analytics.tree.multiclass;

import com.flightstats.analytics.tree.Item;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.*;

import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Humidity.HIGH;
import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Humidity.NORMAL;
import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Outlook.*;
import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Temp.*;
import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Wind.STRONG;
import static com.flightstats.analytics.tree.multiclass.DecisionTreeTrainerTest.Wind.WEAK;
import static org.junit.Assert.assertEquals;
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
        assertEquals(Optional.of("sour"), testClass.bestEntropyGain(items, Arrays.asList("bitter", "sour", "sweet"), 3));
    }

    @Test
    public void testSimpleExample() throws Exception {
        LabeledItem one = new LabeledItem(new Item("1", fruitData(true, true, false)), 1);
        LabeledItem two = new LabeledItem(new Item("2", fruitData(true, true, false)), 1);

        LabeledItem three = new LabeledItem(new Item("3", fruitData(false, true, true)), 0);
        LabeledItem four = new LabeledItem(new Item("4", fruitData(false, false, true)), 0);
        LabeledItem five = new LabeledItem(new Item("5", fruitData(true, false, true)), 0);

        LabeledItem six = new LabeledItem(new Item("6", fruitData(true, true, true)), 1);

        //we put 'one' in the list twice, to unbalance the weights and make bitter the best entropy gain. (otherwise, the resulting tree isn't deterministic)
        List<LabeledItem> labeledItems = Arrays.asList(one, one, two, three, four, five, six);

        DecisionTree tree = new DecisionTreeTrainer(new EntropyCalculator()).train("fruits", labeledItems, Arrays.asList("sweet", "sour", "bitter"), 0);

        assertEquals((Integer) 1, tree.evaluate(one.getItem()));
        assertEquals((Integer) 1, tree.evaluate(two.getItem()));
        assertEquals((Integer) 0, tree.evaluate(three.getItem()));
        assertEquals((Integer) 0, tree.evaluate(four.getItem()));
        assertEquals((Integer) 0, tree.evaluate(five.getItem()));
        assertEquals((Integer) 1, tree.evaluate(six.getItem()));

        //we like these, because they aren't bitter (which is the strongest signal in the tree above).
        assertEquals((Integer) 1, tree.evaluate(new Item("bland", fruitData(false, false, false))));
        assertEquals((Integer) 1, tree.evaluate(new Item("sour", fruitData(false, true, false))));
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
        List<LabeledItem> trainingData = Arrays.asList(
                new LabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, WEAK)), 0),
                new LabeledItem(new Item("1", tennisData(SUNNY, HOT, HIGH, STRONG)), 0),
                new LabeledItem(new Item("1", tennisData(OVERCAST, HOT, HIGH, WEAK)), 1),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, WEAK)), 1),

                new LabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, WEAK)), 1),
                new LabeledItem(new Item("1", tennisData(RAIN, COOL, NORMAL, STRONG)), 0),
                new LabeledItem(new Item("1", tennisData(OVERCAST, COOL, NORMAL, STRONG)), 1),
                new LabeledItem(new Item("1", tennisData(SUNNY, MILD, HIGH, WEAK)), 0),

                new LabeledItem(new Item("1", tennisData(SUNNY, COOL, NORMAL, WEAK)), 1),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, NORMAL, WEAK)), 1),
                new LabeledItem(new Item("1", tennisData(SUNNY, MILD, NORMAL, STRONG)), 1),
                new LabeledItem(new Item("1", tennisData(OVERCAST, MILD, HIGH, STRONG)), 1),

                new LabeledItem(new Item("1", tennisData(OVERCAST, HOT, NORMAL, WEAK)), 1),
                new LabeledItem(new Item("1", tennisData(RAIN, MILD, HIGH, STRONG)), 0)
        );

        DecisionTreeTrainer testClass = new DecisionTreeTrainer(new EntropyCalculator());
        List<String> attributes = Arrays.asList("outlook", "temp", "humidity", "wind");
        Optional<String> bestEntropyGain = testClass.bestEntropyGain(trainingData, attributes, 4);
        assertEquals(Optional.<Object>of("outlook"), bestEntropyGain);

        DecisionTree tennis = testClass.train("tennis", trainingData, attributes, 0);

        assertEquals((Integer) 0, tennis.evaluate(new Item("2", tennisData(RAIN, HOT, HIGH, STRONG))));
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