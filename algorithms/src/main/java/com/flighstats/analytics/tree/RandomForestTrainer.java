package com.flighstats.analytics.tree;

import com.google.common.collect.Sets;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RandomForestTrainer {
    private final DecisionTreeTrainer decisionTreeTrainer;
    private final SecureRandom secureRandom = new SecureRandom();

    public RandomForestTrainer(DecisionTreeTrainer decisionTreeTrainer) {
        this.decisionTreeTrainer = decisionTreeTrainer;
    }

    public TrainingResults train(String name, int numberOfTrees, List<LabeledItem> trainingData, List<Object> attributes) {
        AtomicInteger totalTestCases = new AtomicInteger();
        AtomicInteger totalWrongCases = new AtomicInteger();

        List<DecisionTree> trees = times(numberOfTrees).parallel()
                .map(x -> {
                    List<LabeledItem> bootstrappedData = pickTrainingData(trainingData);
                    Sets.SetView<LabeledItem> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    DecisionTree tree = decisionTreeTrainer.train(name, bootstrappedData, attributes, (int) Math.sqrt(attributes.size()), false);
                    for (LabeledItem item : outOfBagItems) {
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
        return new TrainingResults(new RandomForest(trees), new TrainingStatistics(errorEstimate));
    }

    private List<LabeledItem> pickTrainingData(List<LabeledItem> trainingData) {
        int size = trainingData.size();
        List<LabeledItem> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

    @SuppressWarnings("RedundantCast")
    public static Stream<Void> times(int number) {
        return Stream.generate(() -> (Void) null).limit(number);
    }

}
