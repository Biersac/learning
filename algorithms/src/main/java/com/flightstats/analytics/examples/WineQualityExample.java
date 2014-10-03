package com.flightstats.analytics.examples;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Splitter;
import com.flightstats.analytics.tree.decision.RandomForestPersister;
import com.flightstats.analytics.tree.regression.RegressionRandomForest;
import com.flightstats.analytics.tree.regression.RegressionRandomForestTrainer;
import com.flightstats.analytics.tree.regression.RegressionTreeTrainer;
import com.flightstats.analytics.tree.regression.TrainingResults;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.util.stream.Collectors.toList;

/**
 * RegressionRandomForest example, using the wine quality dataset from : http://archive.ics.uci.edu/ml/datasets/Wine+Quality
 */
public class WineQualityExample {

    public static void main(String[] args) throws IOException {
        Path dataFile = Paths.get("algorithms/testdata/wine-quality/winequality-white.csv");
        List<String> attributes = readAttributes(dataFile);

        System.out.println("attributes = " + attributes);

        List<LabeledItem<Double>> data = loadData(dataFile, attributes);

        int totalNumberOfItems = data.size();
        int numberOfTestItems = totalNumberOfItems / 3;

        List<LabeledItem<Double>> testSet = data.stream().limit(numberOfTestItems).collect(toList());
        List<LabeledItem<Double>> trainingSet = data.stream().skip(numberOfTestItems).collect(toList());

        double average = data.stream().mapToDouble(LabeledItem::getLabel).average().getAsDouble();
        System.out.println("average value = " + average);

        RegressionRandomForest forest = train(attributes, trainingSet);

        //an example of how to persist the model
        Path modelFile = constructModelFile();
        saveModel(forest, modelFile);
        RegressionRandomForest loadedForest = loadModel(modelFile);

        test(testSet, average, loadedForest);
    }

    @SneakyThrows
    private static Path constructModelFile() {
        Path modelDirectory = Paths.get("models");
        if (!Files.isDirectory(modelDirectory)) {
            Files.createDirectory(modelDirectory);
        }
        return modelDirectory.resolve("winequality.rf.model.gz");
    }

    @SneakyThrows
    private static List<LabeledItem<Double>> loadData(Path dataFile, List<String> attributes) {
        List<LabeledItem<Double>> data = Files.lines(dataFile).skip(1).map(line -> {
            String[] pieces = line.split(";");

            Map<String, Double> values = new HashMap<>(pieces.length);
            for (int i = 0; i < pieces.length - 1; i++) {
                String piece = pieces[i];
                values.put(attributes.get(i), Double.parseDouble(piece));
            }
            return new LabeledItem<>(new Item("dummy", new HashMap<>(), values), Double.parseDouble(pieces[pieces.length - 1]));
        }).collect(toList());

        Collections.shuffle(data);
        return data;
    }

    @SneakyThrows
    private static List<String> readAttributes(Path dataFile) {
        String headerRow = Files.lines(dataFile).findFirst().get();
        List<String> headers = Arrays.stream(headerRow.split(";")).map(s -> s.replaceAll("\"", "")).collect(toList());
        return headers.stream().limit(headers.size() - 1).collect(toList());
    }

    private static RegressionRandomForest train(List<String> attributes, List<LabeledItem<Double>> trainingSet) {
        RegressionRandomForestTrainer trainer = new RegressionRandomForestTrainer(new RegressionTreeTrainer(new Splitter<>()));
        TrainingResults trainingResults = trainer.train("white wine", 100, trainingSet, attributes);
        double outOfBagError = trainingResults.calculateOutOfBagError();
        System.out.println("\noutOfBagError = " + outOfBagError);

        return trainingResults.getForest();
    }

    @SneakyThrows
    private static RegressionRandomForest loadModel(Path modelFile) {
        RegressionRandomForest loadedForest;
        try (InputStream modelReader = new GZIPInputStream(Files.newInputStream(modelFile))) {
            loadedForest = new RandomForestPersister().loadRegression(modelReader);
        }
        return loadedForest;
    }

    @SneakyThrows
    private static void saveModel(RegressionRandomForest forest, Path modelFile) {
        try (OutputStream modelWriter = new GZIPOutputStream(Files.newOutputStream(modelFile))) {
            new RandomForestPersister().save(forest, modelWriter);
        }
    }

    private static void test(List<LabeledItem<Double>> testSet, double average, RegressionRandomForest forest) {
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
