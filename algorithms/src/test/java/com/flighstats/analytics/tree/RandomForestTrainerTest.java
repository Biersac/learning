package com.flighstats.analytics.tree;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flighstats.analytics.tree.RandomForestTrainerTest.Humidity.HIGH;
import static com.flighstats.analytics.tree.RandomForestTrainerTest.Humidity.NORMAL;
import static com.flighstats.analytics.tree.RandomForestTrainerTest.Outlook.*;
import static com.flighstats.analytics.tree.RandomForestTrainerTest.Temp.*;
import static com.flighstats.analytics.tree.RandomForestTrainerTest.Wind.STRONG;
import static com.flighstats.analytics.tree.RandomForestTrainerTest.Wind.WEAK;
import static org.junit.Assert.*;

public class RandomForestTrainerTest {

    @Test
    public void testTennisExample() throws Exception {
        List<LabeledItem> trainingData = buildTennisTrainingSet();

        RandomForestTrainer testClass = new RandomForestTrainer(new DecisionTreeTrainer(new EntropyCalculator()));
        List<Object> attributes = tennisAttributes();

        TrainingResults result = testClass.train("tennis", 500, trainingData, attributes);
        RandomForest tennis = result.getForest();
        TrainingStatistics statistics = result.getStatistics();
        assertNotNull(statistics);
        //this is a nice case of where the random forest works probabilistically. It's not a clear-cut case,
        // so not all the trees return the same result. On average, though, these should be a good day for tennis.
        assertTrue(tennis.evaluate(new Item("1", tennisData(RAIN, HOT, NORMAL, WEAK))));
        assertTrue(tennis.evaluate(new Item("1", tennisData(RAIN, MILD, HIGH, WEAK))));
    }

    @Test
    public void testGsonSerialization() throws Exception {
        RandomForest randomForest = new RandomForestTrainer(new DecisionTreeTrainer(new EntropyCalculator())).train("test", 50, buildTennisTrainingSet(), tennisAttributes()).getForest();
        Gson gson = new Gson();
        String json = gson.toJson(randomForest);

        RandomForest deserialized = gson.fromJson(json, RandomForest.class);
        assertEquals(randomForest, deserialized);
    }

    private List<Object> tennisAttributes() {
        return Arrays.asList("outlook", "temp", "humidity", "wind");
    }


    private List<LabeledItem> buildTennisTrainingSet() {
        return Arrays.asList(
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