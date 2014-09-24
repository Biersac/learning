package com.flighstats.analytics.tree.binary;

import com.flighstats.analytics.tree.TrainingStatistics;
import com.google.common.collect.Sets;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class BinaryRandomForestTrainer {
    private final BinaryDecisionTreeTrainer decisionTreeTrainer;
    private final SecureRandom secureRandom = new SecureRandom();

    public BinaryRandomForestTrainer(BinaryDecisionTreeTrainer decisionTreeTrainer) {
        this.decisionTreeTrainer = decisionTreeTrainer;
    }

    public TrainingResults train(String name, int numberOfTrees, List<BinaryLabeledItem> trainingData, List<String> attributes) {
        AtomicInteger totalTestCases = new AtomicInteger();
        AtomicInteger totalWrongCases = new AtomicInteger();

        List<BinaryDecisionTree> trees = times(numberOfTrees).parallel()
                .map(x -> {
                    List<BinaryLabeledItem> bootstrappedData = pickTrainingData(trainingData);
                    Sets.SetView<BinaryLabeledItem> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    BinaryDecisionTree tree = decisionTreeTrainer.train(name, bootstrappedData, attributes, (int) Math.sqrt(attributes.size()), false);
                    for (BinaryLabeledItem item : outOfBagItems) {
                        boolean result = tree.evaluate(item.getItem());
                        boolean label = item.getLabel();
                        if (result != label) {
                            totalWrongCases.incrementAndGet();
                        }
                        totalTestCases.incrementAndGet();
                    }
                    return tree;
                }).collect(toList());

        double errorEstimate = ((double) totalWrongCases.get()) / totalTestCases.get();
        return new TrainingResults(new BinaryRandomForest(trees), new TrainingStatistics(errorEstimate));
    }

    private List<BinaryLabeledItem> pickTrainingData(List<BinaryLabeledItem> trainingData) {
        int size = trainingData.size();
        List<BinaryLabeledItem> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

    @SuppressWarnings("RedundantCast")
    public static Stream<Void> times(int number) {
        return Stream.generate(() -> (Void) null).limit(number);
    }

}
