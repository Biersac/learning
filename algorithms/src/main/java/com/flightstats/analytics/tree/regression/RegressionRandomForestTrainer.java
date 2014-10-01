package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Tree;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Value;
import lombok.extern.java.Log;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.transform;
import static java.util.stream.Collectors.toList;

@Log
public class RegressionRandomForestTrainer {
    private final RegressionTreeTrainer regressionTreeTrainer;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegressionRandomForestTrainer(RegressionTreeTrainer regressionTreeTrainer) {
        this.regressionTreeTrainer = regressionTreeTrainer;
    }

    public TrainingResults train(String name, int numberOfTrees, List<LabeledItem<Double>> trainingData, List<String> attributes) {
        int featuresToUse = (int) Math.sqrt(attributes.size());

        List<TrainingPair> pairs = times(numberOfTrees)
                .parallel()
                .map(x -> {
                    List<LabeledItem<Double>> bootstrappedData = pickTrainingData(trainingData);
                    Set<LabeledItem<Double>> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    Tree<Double> tree = regressionTreeTrainer.train(name, bootstrappedData, attributes, featuresToUse);
                    System.out.print(".");
                    return new TrainingPair(tree, outOfBagItems);
                }).collect(toList());

        Multimap<LabeledItem<Double>, Tree<Double>> treesForItem = ArrayListMultimap.create();
        for (TrainingPair pair : pairs) {
            pair.getOutOfBagItems().forEach(item -> treesForItem.put(item, pair.getTree()));
        }
        return new TrainingResults(new RegressionRandomForest(transform(pairs, TrainingPair::getTree)), treesForItem);
    }

    private List<LabeledItem<Double>> pickTrainingData(List<LabeledItem<Double>> trainingData) {
        int size = trainingData.size();
        List<LabeledItem<Double>> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

    private static Stream<Void> times(int number) {
        return Arrays.stream(new Void[number]);
    }

    @Value
    private static class TrainingPair {
        Tree<Double> tree;
        Set<LabeledItem<Double>> outOfBagItems;
    }
}
