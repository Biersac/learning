package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.*;
import com.flightstats.analytics.tree.regression.RegressionRandomForest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Humidity.HIGH;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Humidity.NORMAL;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Outlook.RAIN;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Temp.HOT;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Temp.MILD;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.Wind.WEAK;
import static com.flightstats.analytics.tree.decision.RandomForestTrainerTest.*;
import static org.junit.Assert.assertEquals;

public class RandomForestPersisterTest {

    @Test
    public void testSaveAndLoad() throws Exception {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();

        TrainingResults trainingResults = train();
        RandomForest forest = trainingResults.getForest();

        RandomForestPersister testClass = new RandomForestPersister();
        testClass.save(forest, writer);
        writer.flush();

        RandomForest loaded = testClass.load(new ByteArrayInputStream(writer.toByteArray()));

        assertEquals((Integer) 1, loaded.evaluate(new Item("a", tennisData(RAIN, HOT, NORMAL, WEAK), new HashMap<>())));
        assertEquals((Integer) 1, loaded.evaluate(new Item("b", tennisData(RAIN, MILD, HIGH, WEAK), new HashMap<>())));

        assertEquals(forest, loaded);
    }

    private TrainingResults train() {
        List<LabeledItem<Integer>> labeledItems = buildTennisTrainingSet();
        RandomForestTrainer trainer = new RandomForestTrainer(new DecisionTreeTrainer(new Splitter<>()));
        return trainer.train("test", 50, labeledItems, tennisAttributes(), -1);
    }

    @Test
    public void testSaveAndLoad_leafNodes() throws Exception {
        RandomForestPersister testClass = new RandomForestPersister();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        RegressionRandomForest forest = new RegressionRandomForest(Arrays.asList(new Tree<>("fred", new LeafNode<>(0.05d)), new Tree<>("joe", new LeafNode<>(1d))));
        testClass.save(forest, outputStream);
        RegressionRandomForest loaded = testClass.loadRegression(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(forest, loaded);

        Double result = loaded.evaluate(new Item("test", new HashMap<>(), new HashMap<>()));
        assertEquals(0.525d, result, 0.00001);
    }

    @Test
    public void testSaveAndLoad_continuousNodes() throws Exception {
        RandomForestPersister testClass = new RandomForestPersister();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        RegressionRandomForest forest = new RegressionRandomForest(Arrays.asList(new Tree<>("fred", new ContinuousTreeNode<>("coffee", 0.1d, new LeafNode<>(0.05d), new LeafNode<>(1.0d)))));
        testClass.save(forest, outputStream);
        RegressionRandomForest loaded = testClass.loadRegression(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(forest, loaded);

        HashMap<String, Double> continuousValues = new HashMap<>();
        continuousValues.put("coffee", 0.4d);
        Double result = loaded.evaluate(new Item("test", new HashMap<>(), continuousValues));
        assertEquals(1.0d, result, 0.00001);
    }

    @Test
    public void testSaveAndLoad_discreteNodes() throws Exception {
        RandomForestPersister testClass = new RandomForestPersister();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        RegressionRandomForest forest = new RegressionRandomForest(Arrays.asList(new Tree<>("fred", new DiscreteTreeNode<>("coffee", 1, new LeafNode<>(0.05d), new LeafNode<>(1.0d)))));
        testClass.save(forest, outputStream);
        RegressionRandomForest loaded = testClass.loadRegression(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(forest, loaded);

        Map<String, Integer> discreteValues = new HashMap<>();
        discreteValues.put("coffee", 2);
        Double result = loaded.evaluate(new Item("test", discreteValues, new HashMap<>()));
        assertEquals(1.0d, result, 0.00001);
    }


}