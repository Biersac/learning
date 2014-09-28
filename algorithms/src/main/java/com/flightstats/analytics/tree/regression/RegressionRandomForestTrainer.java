package com.flightstats.analytics.tree.regression;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RegressionRandomForestTrainer {
    private final RegressionTreeTrainer regressionTreeTrainer;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegressionRandomForestTrainer(RegressionTreeTrainer regressionTreeTrainer) {
        this.regressionTreeTrainer = regressionTreeTrainer;
    }

    public TrainingResults train(String name, int numberOfTrees, List<LabeledMixedItem> trainingData, List<String> attributes) {
        int featuresToUse = (int) Math.sqrt(attributes.size());

        Multimap<LabeledMixedItem, RegressionTree> treesForItem = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        List<RegressionTree> trees = times(numberOfTrees)
//                .parallel()
                .map(x -> {
                    List<LabeledMixedItem> bootstrappedData = pickTrainingData(trainingData);
                    Sets.SetView<LabeledMixedItem> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    RegressionTree tree = regressionTreeTrainer.train(name, bootstrappedData, attributes, featuresToUse);
                    for (LabeledMixedItem item : outOfBagItems) {
                        treesForItem.put(item, tree);
                    }
                    System.out.print(".");
                    return tree;
                }).collect(toList());

        return new TrainingResults(new RegressionRandomForest(trees), treesForItem);
    }

    private List<LabeledMixedItem> pickTrainingData(List<LabeledMixedItem> trainingData) {
        int size = trainingData.size();
        List<LabeledMixedItem> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

    @SuppressWarnings("RedundantCast")
    public static Stream<Void> times(int number) {
        return Stream.generate(() -> (Void) null).limit(number);
    }

}
