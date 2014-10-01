package com.flightstats.analytics.examples;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Splitter;
import com.flightstats.analytics.tree.decision.DecisionTreeTrainer;
import com.flightstats.analytics.tree.decision.RandomForest;
import com.flightstats.analytics.tree.decision.RandomForestTrainer;
import com.flightstats.analytics.tree.decision.TrainingResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * Decision RandomForest example, using the wine quality dataset from : http://archive.ics.uci.edu/ml/datasets/Wine+Quality
 */
public class WineQualityDecisionExample {

    public static void main(String[] args) throws IOException {
        Path dataFile = Paths.get("algorithms/testdata/wine-quality/winequality-white.csv");
        String headerRow = Files.lines(dataFile).findFirst().get();
        List<String> headers = Arrays.stream(headerRow.split(";")).map(s -> s.replaceAll("\"", "")).collect(toList());
        List<String> attributes = headers.stream().limit(headers.size() - 1).collect(toList());
        System.out.println("attributes = " + attributes);

        List<LabeledItem<Integer>> data = Files.lines(dataFile).skip(1).map(line -> {
            String[] pieces = line.split(";");

            Map<String, Double> values = new HashMap<>(pieces.length);
            for (int i = 0; i < pieces.length - 1; i++) {
                String piece = pieces[i];
                values.put(headers.get(i), Double.parseDouble(piece));
            }
            return new LabeledItem<>(new Item("dummy", new HashMap<>(), values), Integer.parseInt(pieces[pieces.length - 1]));
        }).collect(toList());

        Collections.shuffle(data);

        int totalNumberOfItems = data.size();
        int numberOfTestItems = totalNumberOfItems / 3;
        List<LabeledItem<Integer>> testSet = data.stream().limit(numberOfTestItems).collect(toList());
        List<LabeledItem<Integer>> trainingSet = data.stream().skip(numberOfTestItems).collect(toList());

        double average = data.stream().mapToDouble(LabeledItem::getLabel).average().getAsDouble();
        System.out.println("average value = " + average);

        RandomForestTrainer trainer = new RandomForestTrainer(new DecisionTreeTrainer(new Splitter<>()));
        TrainingResults trainingResults = trainer.train("white wine", 100, trainingSet, attributes, -1);
        double outOfBagError = trainingResults.calculateOutOfBagError(-1);
        System.out.println("\noutOfBagError = " + outOfBagError);

        RandomForest forest = trainingResults.getForest();
        AtomicInteger totalWrong = testSetTest(testSet, forest);
        int numberCorrect = testSet.size() - totalWrong.get();
        System.out.println("test set % correct = " + ((double) numberCorrect / testSet.size()));
        System.out.println("test set % wrong   = " + (totalWrong.doubleValue() / testSet.size()));

        testWithGuessingMeanScore(testSet, (int) average);
    }

    private static void testWithGuessingMeanScore(List<LabeledItem<Integer>> testSet, int average) {
        AtomicInteger totalWrongByAverage = new AtomicInteger(0);
        testSet.forEach(i -> {
            Integer evaluation = average;
            Integer truth = i.getLabel();
            if (!truth.equals(evaluation)) {
                totalWrongByAverage.incrementAndGet();
            }
        });
        System.out.println("meanGuessError = " + (totalWrongByAverage.doubleValue() / testSet.size()));
    }

    private static AtomicInteger testSetTest(List<LabeledItem<Integer>> testSet, RandomForest forest) {
        AtomicInteger totalWrong = new AtomicInteger(0);
        testSet.forEach(i -> {
            Integer evaluation = forest.evaluate(i.getItem());
//            System.out.println("eval:" + evaluation + " label: " + i.getLabel() + " i = " + i);
//            System.out.printf("%s\t%s%n", i.getLabel(), evaluation);
            Integer truth = i.getLabel();
            if (!truth.equals(evaluation)) {
                totalWrong.incrementAndGet();
            }
        });
        return totalWrong;
    }

    public static <T> T time(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        T t = supplier.get();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - start));
        return t;
    }
}
