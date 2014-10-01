package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Tree;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RandomForestTrainer {
    private final DecisionTreeTrainer decisionTreeTrainer;
    private final SecureRandom secureRandom = new SecureRandom();

    public RandomForestTrainer(DecisionTreeTrainer decisionTreeTrainer) {
        this.decisionTreeTrainer = decisionTreeTrainer;
    }

    public TrainingResults train(String name, int numberOfTrees, List<LabeledItem<Integer>> trainingData, List<String> attributes, int defaultLabel) {
        int featuresToUse = (int) Math.sqrt(attributes.size());

        Multimap<LabeledItem<Integer>, Tree<Integer>> treesForItem = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        List<Tree<Integer>> trees = times(numberOfTrees)
                .parallel()
                .map(x -> {
                    List<LabeledItem<Integer>> bootstrappedData = pickTrainingData(trainingData);
                    Sets.SetView<LabeledItem<Integer>> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    Tree<Integer> tree = decisionTreeTrainer.train(name, bootstrappedData, attributes, featuresToUse, defaultLabel);
                    for (LabeledItem<Integer> item : outOfBagItems) {
                        treesForItem.put(item, tree);
                    }
                    System.out.print(".");
                    return tree;
                }).collect(toList());

        return new TrainingResults(new RandomForest(trees, defaultLabel), treesForItem);
    }

    private List<LabeledItem<Integer>> pickTrainingData(List<LabeledItem<Integer>> trainingData) {
        int size = trainingData.size();
        List<LabeledItem<Integer>> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

    public static Stream<Void> times(int number) {
        return Arrays.stream(new Void[number]);
    }

}
