package com.flightstats.analytics.examples;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Splitter;
import com.flightstats.analytics.tree.decision.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.util.stream.Collectors.toList;

/**
 * This is an example of how one could train and test a RandomForest. It is doing recognition of handwritten digits,
 * using data from http://archive.ics.uci.edu/ml/datasets (The "Optical Recognition of Handwritten Digits Data Set").
 * <p>
 * The OOB error estimate & the test error should be about 4-5%, using this code.
 * <p>
 * This should be run from the root of the project (not from within the algorithms directory),
 * unless you want to edit the code and change the location of the training/test data.
 */
public class OptDigitsExample {

    public static void main(String[] args) throws IOException {
        Path dataDirectory = Paths.get("algorithms/testdata/optdigits");

        RandomForest forest = train(dataDirectory);

        Path modelDirectory = Paths.get("models");
        if (!Files.isDirectory(modelDirectory)) {
            Files.createDirectory(modelDirectory);
        }
        Path modelFile = modelDirectory.resolve("optdigits.rf.model.gz");

//        here's an example of how you to persist a model to re-use at a later time.
        saveForest(forest, modelFile);
        RandomForest loadedForest = loadForestFromDisk(modelFile);

        test(dataDirectory, loadedForest);
    }

    private static RandomForest loadForestFromDisk(Path modelFile) throws IOException {
        try (InputStream modelReader = new GZIPInputStream(Files.newInputStream(modelFile))) {
            return new RandomForestPersister().load(modelReader);
        }
    }

    private static void saveForest(RandomForest forest, Path modelFile) throws IOException {
        try (OutputStream modelWriter = new GZIPOutputStream(Files.newOutputStream(modelFile))) {
            new RandomForestPersister().save(forest, modelWriter);
        }
    }

    private static void test(Path dir, RandomForest forest) throws IOException {
        List<LabeledItem<Integer>> testData = extractLabeledItems(Files.lines(dir.resolve("optdigits.tes")));
        AtomicInteger totalItems = new AtomicInteger();
        AtomicInteger totalRight = new AtomicInteger();
        AtomicInteger totalWrong = new AtomicInteger();
        AtomicInteger totalUnknown = new AtomicInteger();
        testData.forEach(item -> {
            Integer result = forest.evaluate(item.getItem());
            Integer truth = item.getLabel();

            totalItems.incrementAndGet();
            if (result < 0) {
                totalUnknown.incrementAndGet();
            }
            if (truth.equals(result)) {
                totalRight.incrementAndGet();
            } else {
                totalWrong.incrementAndGet();
            }
//            System.out.println("real: " + truth + "; guess: " + result);
        });
        float accuracy = (float) (totalRight.get()) / totalItems.get();
        float error = (float) (totalWrong.get()) / totalItems.get();
        float accuracyOnNonUnknown = (float) (totalRight.get()) / (totalItems.get() - totalUnknown.get());
        System.out.println("test-set error = " + error);
        System.out.println("totalItems = " + totalItems);
        System.out.println("totalUnknown = " + totalUnknown);
        System.out.println("totalRight = " + totalRight);
        System.out.println("totalWrong = " + totalWrong);
        System.out.println("accuracy = " + accuracy);
        System.out.println("accuracyOnNonUnknown = " + accuracyOnNonUnknown);
    }

    private static RandomForest train(Path dir) throws IOException {
        RandomForestTrainer trainer = new RandomForestTrainer(new DecisionTreeTrainer(new Splitter<>()));

        List<LabeledItem<Integer>> trainingData = extractLabeledItems(Files.lines(dir.resolve("optdigits.tra")));
        List<String> attributes = trainingData.get(0).attributes();

        TrainingResults trainingResults = trainer.train("digits", 200, trainingData, attributes, -1);
        System.out.println("\noob error est. = " + trainingResults.calculateOutOfBagError(-1));

        ClusterFinder<Integer> clusterFinder = new ClusterFinder<>();
        clusterFinder.exploreTrainingClusters(trainingResults.getTrainingData(), trainingResults.getItemProximities());
        //from the output of above, looking for the 'elbow' in the graph, it looks like about 12-14 clusters is about optimal.
        Collection<Set<LabeledItem<Integer>>> clusters = clusterFinder.findTrainingClusters(12, trainingResults.getTrainingData(), trainingResults.getItemProximities());
        //further analysis could be done on the clusters that are found.

        System.out.println();
        return trainingResults.getForest();
    }

    private static List<LabeledItem<Integer>> extractLabeledItems(Stream<String> lines) {
        AtomicInteger lineNumber = new AtomicInteger();
        return lines.map(line -> line.split(",")).map(array -> {
            lineNumber.incrementAndGet();
            int length = array.length;
            Map<String, Integer> data = new HashMap<>();
            for (int i = 0; i < length - 1; i++) {
                Integer value = Integer.valueOf(array[i]);
                data.put(String.valueOf(i), value / 4);
            }
            String label = array[array.length - 1];
            return new LabeledItem<>(new Item(String.valueOf(lineNumber.get()), data, new HashMap<>()), Integer.valueOf(label));
        }).collect(toList());
    }
}
