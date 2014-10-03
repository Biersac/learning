package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Tree;
import com.flightstats.analytics.tree.TreeNode;
import com.flightstats.util.Functional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.security.SecureRandom;
import java.util.*;

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
        Matrix itemProximities = new Basic2DMatrix(trainingData.size(), trainingData.size());

        List<Tree<Integer>> trees = Functional.times(numberOfTrees)
                .parallel()
                .map(x -> {
                    List<LabeledItem<Integer>> bootstrappedData = pickTrainingData(trainingData);
                    Sets.SetView<LabeledItem<Integer>> outOfBagItems = Sets.difference(new HashSet<>(trainingData), new HashSet<>(bootstrappedData));
                    Tree<Integer> tree = decisionTreeTrainer.train(name, bootstrappedData, attributes, featuresToUse, defaultLabel);
                    outOfBagItems.forEach(item -> treesForItem.put(item, tree));

                    System.out.print(".");
                    return tree;
                }).collect(toList());

        System.out.println("\n calculating proximities...");
        calculateProximities(trainingData, itemProximities, trees);

        return new TrainingResults(new RandomForest(trees, defaultLabel), treesForItem, itemProximities, trainingData);
    }

    private void calculateProximities(List<LabeledItem<Integer>> trainingData, Matrix itemProximities, List<Tree<Integer>> trees) {
        trees.forEach(tree -> {
            Map<List<TreeNode<Integer>>, List<Integer>> proximityMap = new HashMap<>();
            for (int i = 0; i < trainingData.size(); i++) {
                Integer location = i;
                LabeledItem<Integer> item = trainingData.get(i);
                List<TreeNode<Integer>> path = tree.putDown(item.getItem());
                proximityMap.compute(path, (key, locations) -> {
                    if (locations == null) {
                        locations = new ArrayList<>();
                    }
                    locations.add(location);
                    return locations;
                });
            }
            updateProximities(itemProximities, proximityMap);
            System.out.print(".");
        });
        System.out.println();
    }

    private void updateProximities(Matrix itemProximities, Map<List<TreeNode<Integer>>, List<Integer>> proximityMap) {
        for (List<Integer> locations : proximityMap.values()) {
            for (int i = 0; i < locations.size(); i++) {
                Integer location = locations.get(i);
                for (int j = i; j < locations.size(); j++) {
                    Integer location2 = locations.get(j);
                    double current = itemProximities.get(location, location2);
                    itemProximities.set(location, location2, current + 1);
                    itemProximities.set(location2, location, current + 1);
                }
            }
        }
    }

    private List<LabeledItem<Integer>> pickTrainingData(List<LabeledItem<Integer>> trainingData) {
        int size = trainingData.size();
        List<LabeledItem<Integer>> dataToUse = new ArrayList<>(size);
        //by the algorithm, we pick <size> items, "with replacement" [i.e. we can pick the same thing more than once], from the training data.
        Functional.times(size).forEach(x -> dataToUse.add(trainingData.get(secureRandom.nextInt(size))));
        return dataToUse;
    }

}
