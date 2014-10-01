package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.LabeledMixedItem;
import com.flightstats.analytics.tree.MixedItem;
import com.flightstats.analytics.tree.Splitter;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Humidity.HIGH;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Humidity.NORMAL;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Outlook.*;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Temp.*;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Wind.STRONG;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Wind.WEAK;
import static org.junit.Assert.assertEquals;

public class RandomForestTrainerTest {

    @Test
    public void testTennisExample() throws Exception {
        List<LabeledMixedItem<Integer>> trainingData = buildTennisTrainingSet();

        RandomForestTrainer testClass = new RandomForestTrainer(new DecisionTreeTrainer(new Splitter<>()));
        List<String> attributes = tennisAttributes();

        TrainingResults result = testClass.train("tennis", 5, trainingData, attributes, -1);
        RandomForest tennis = result.getForest();
        //this is a nice case of where the random forest works probabilistically. It's not a clear-cut case,
        // so not all the trees return the same result. On average, though, these should be a good day for tennis.
        assertEquals((Integer) 1, tennis.evaluate(new MixedItem("1", tennisData(RAIN, HOT, NORMAL, WEAK), new HashMap<>())));
        assertEquals((Integer) 1, tennis.evaluate(new MixedItem("1", tennisData(RAIN, MILD, HIGH, WEAK), new HashMap<>())));
    }

    public static List<String> tennisAttributes() {
        return Arrays.asList("outlook", "temp", "humidity", "wind");
    }

    public static List<LabeledMixedItem<Integer>> buildTennisTrainingSet() {
        return Arrays.asList(
                new LabeledMixedItem<>(new MixedItem("1", tennisData(SUNNY, HOT, HIGH, WEAK), new HashMap<>()), 0),
                new LabeledMixedItem<>(new MixedItem("2", tennisData(SUNNY, HOT, HIGH, STRONG), new HashMap<>()), 0),
                new LabeledMixedItem<>(new MixedItem("3", tennisData(OVERCAST, HOT, HIGH, WEAK), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("4", tennisData(RAIN, MILD, HIGH, WEAK), new HashMap<>()), 1),

                new LabeledMixedItem<>(new MixedItem("5", tennisData(RAIN, COOL, NORMAL, WEAK), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("6", tennisData(RAIN, COOL, NORMAL, STRONG), new HashMap<>()), 0),
                new LabeledMixedItem<>(new MixedItem("7", tennisData(OVERCAST, COOL, NORMAL, STRONG), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("8", tennisData(SUNNY, MILD, HIGH, WEAK), new HashMap<>()), 0),

                new LabeledMixedItem<>(new MixedItem("9", tennisData(SUNNY, COOL, NORMAL, WEAK), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("10", tennisData(RAIN, MILD, NORMAL, WEAK), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("11", tennisData(SUNNY, MILD, NORMAL, STRONG), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("12", tennisData(OVERCAST, MILD, HIGH, STRONG), new HashMap<>()), 1),

                new LabeledMixedItem<>(new MixedItem("13", tennisData(OVERCAST, HOT, NORMAL, WEAK), new HashMap<>()), 1),
                new LabeledMixedItem<>(new MixedItem("14", tennisData(RAIN, MILD, HIGH, STRONG), new HashMap<>()), 0)
        );
    }

    public static Map<String, Integer> tennisData(Outlook outlook, Temp temperature, Humidity humidity, Wind wind) {
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