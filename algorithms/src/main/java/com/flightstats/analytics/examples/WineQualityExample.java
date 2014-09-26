package com.flightstats.analytics.examples;

import com.flightstats.analytics.tree.regression.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * RegressionRandomForest example, using the wine quality dataset from : http://archive.ics.uci.edu/ml/datasets/Wine+Quality
 */
public class WineQualityExample {

    public static void main(String[] args) throws IOException {
        Path dataFile = Paths.get("algorithms/testdata/wine-quality/winequality-white.csv");
        String headerRow = Files.lines(dataFile).findFirst().get();
        List<String> headers = Arrays.stream(headerRow.split(";")).map(s -> s.replaceAll("\"", "")).collect(toList());
        List<String> attributes = headers.stream().limit(headers.size() - 1).collect(toList());
        System.out.println("attributes = " + attributes);

        List<LabeledMixedItem> data = Files.lines(dataFile).skip(1).map(line -> {
            String[] pieces = line.split(";");

            Map<String, Double> values = new HashMap<>(pieces.length);
            for (int i = 0; i < pieces.length - 1; i++) {
                String piece = pieces[i];
                values.put(headers.get(i), Double.parseDouble(piece));
            }
            return new LabeledMixedItem(new MixedItem("dummy", new HashMap<>(), values), Double.parseDouble(pieces[pieces.length - 1]));
        }).collect(toList());

        Collections.shuffle(data);

        int totalNumberOfItems = data.size();
        int numberOfTestItems = totalNumberOfItems / 3;
        List<LabeledMixedItem> testSet = data.stream().limit(numberOfTestItems).collect(toList());
        List<LabeledMixedItem> trainingSet = data.stream().skip(numberOfTestItems).collect(toList());

        double average = data.stream().mapToDouble(LabeledMixedItem::getLabel).average().getAsDouble();
        System.out.println("average = " + average);

        RegressionRandomForestTrainer trainer = new RegressionRandomForestTrainer(new RegressionTreeTrainer());
        TrainingResults trainingResults = trainer.train("white wine", 50, trainingSet, attributes);
        double outOfBagError = trainingResults.calculateOutOfBagError();
        System.out.println("outOfBagError = " + outOfBagError);

        RegressionRandomForest forest = trainingResults.getForest();
        AtomicInteger closeCount = new AtomicInteger(0);
        double error = Math.sqrt(testSet.stream().map(i -> {
            Double evaluation = forest.evaluate(i.getItem());
//            System.out.println("eval:" + evaluation + " label: " + i.getLabel() + " i = " + i);
//            System.out.printf("%s\t%s%n", i.getLabel(), evaluation);
            double difference = evaluation - i.getLabel();
            int roundedResult = (int) Math.round(evaluation);
            if (roundedResult == (int) (double) i.getLabel()) {
                closeCount.incrementAndGet();
            }
            return Math.pow(difference, 2);
        }).mapToDouble(d -> d).sum()) / testSet.size();
        System.out.println("test set error = " + error);
        double meanGuessError = Math.sqrt(testSet.stream().map(i -> {
            Double evaluation = average;
            return Math.pow(evaluation - i.getLabel(), 2);
        }).mapToDouble(d -> d).sum()) / testSet.size();
        System.out.println("meanGuessError = " + meanGuessError);
        System.out.println("% within +/- 1/2 : " + (((double) closeCount.get()) / testSet.size()));
    }

    public static <T> T time(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        T t = supplier.get();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - start));
        return t;
    }
}
