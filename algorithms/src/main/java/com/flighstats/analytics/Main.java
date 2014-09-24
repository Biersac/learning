package com.flighstats.analytics;

import com.flighstats.analytics.tree.Item;
import com.flighstats.analytics.tree.multiclass.*;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        RandomForestTrainer trainer = new RandomForestTrainer(new DecisionTreeTrainer(new EntropyCalculator()));

        Path dir = Paths.get("algorithms/testdata");

        List<LabeledItem> trainingData = extractLabeledItems(Files.lines(dir.resolve("optdigits.tra")));
        List<LabeledItem> testData = extractLabeledItems(Files.lines(dir.resolve("optdigits.tes")));

        List<Object> attributes = trainingData.stream().findFirst().get().attributes();

        TrainingResults trainingResults = trainer.train("digits", 200, trainingData, attributes, -1);
        RandomForest forest = trainingResults.getForest();
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
        float accuracyOnGuesses = (float) (totalRight.get()) / (totalItems.get() - totalUnknown.get());
        System.out.println("oob error est = " + trainingResults.calculateOutOfBagError(-1));
        System.out.println("testset error = " + error);
        System.out.println("totalItems = " + totalItems);
        System.out.println("totalUnknown = " + totalUnknown);
        System.out.println("totalRight = " + totalRight);
        System.out.println("totalWrong = " + totalWrong);
        System.out.println("accuracy = " + accuracy);
        System.out.println("accuracyOnGuesses = " + accuracyOnGuesses);

    }

    private static List<LabeledItem> extractLabeledItems(Stream<String> lines) {
        AtomicInteger lineNumber = new AtomicInteger();
        return lines.map(line -> line.split(",")).map(array -> {
            lineNumber.incrementAndGet();
            int length = array.length;
            Map<Object, Integer> data = new HashMap<>();
            for (int i = 0; i < length - 1; i++) {
                Integer value = Integer.valueOf(array[i]);
                data.put(i, value / 4);
            }
            String label = array[array.length - 1];
            return new LabeledItem(new Item(String.valueOf(lineNumber.get()), data), Integer.valueOf(label));
        }).collect(toList());
    }
}
