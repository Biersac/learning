package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.*;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.*;

public class DecisionTreeTrainer {
    private final Splitter<Integer> splitter;

    public DecisionTreeTrainer(Splitter<Integer> splitter) {
        this.splitter = splitter;
    }

    public Tree<Integer> train(String name, List<LabeledMixedItem<Integer>> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose, Integer defaultValue) {
        return new Tree<>(name, buildNode(trainingData, attributes, numberOfFeaturesToChoose, defaultValue));
    }

    private TreeNode<Integer> buildNode(List<LabeledMixedItem<Integer>> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose, Integer defaultValue) {
        if (trainingData.isEmpty()) {
            return new LeafNode<>(defaultValue);
        }
        if (allAreSameLabel(trainingData)) {
            return new LeafNode<>(trainingData.get(0).getLabel());
        }
        if (allTrainingDataIsTheSame(trainingData, attributes)) {
            return new LeafNode<>(findMostCommonLabel(trainingData));
        }

        Split<Integer> split = bestSplit(trainingData, attributes, numberOfFeaturesToChoose);
        if (split == null) {
            return new LeafNode<>(defaultValue);
        }
        //todo: check to see if the change in S is too small, or if there aren't enough things to do a split. maybe. [probably not needed for random forests, though...]
        if (split instanceof ContinuousSplit) {
            ContinuousSplit continuousSplit = (ContinuousSplit) split;
            return new ContinuousTreeNode<>(continuousSplit.getAttribute(), continuousSplit.getSplitValue(), buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose, defaultValue), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose, defaultValue));
        } else {
            DiscreteSplit<Integer> discreteSplit = (DiscreteSplit) split;
            return new DiscreteTreeNode<>(discreteSplit.getAttribute(), discreteSplit.getLeftChoice(), buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose, defaultValue), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose, defaultValue));
        }
    }

    private Integer findMostCommonLabel(List<LabeledMixedItem<Integer>> trainingData) {
        Map<Integer, Integer> countsByLabel = trainingData.stream().collect(groupingBy(LabeledMixedItem::getLabel, reducing(0, LabeledMixedItem::getLabel, Integer::sum)));
        return countsByLabel.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
    }

    private boolean allAreSameLabel(List<LabeledMixedItem<Integer>> labeledItems) {
        return labeledItems.stream().map(LabeledMixedItem::getLabel).distinct().count() == 1;
    }

    private boolean allTrainingDataIsTheSame(List<LabeledMixedItem<Integer>> trainingData, Collection<String> attributes) {
        Stream<List<Object>> objectStream = trainingData.stream()
                .map(lmi -> attributes.stream().map(a -> Arrays.asList(lmi.getContinuousValue(a), lmi.getDiscreteValue(a))).collect(toList()));
        return objectStream.distinct().count() == 1;
    }

    private Split<Integer> bestSplit(List<LabeledMixedItem<Integer>> items, Collection<String> attributes, int numberOfFeaturesToChoose) {
        if (numberOfFeaturesToChoose < attributes.size()) {
            List<String> newAttributes = new ArrayList<>(attributes);
            Collections.shuffle(newAttributes);
            attributes = newAttributes.stream().limit(numberOfFeaturesToChoose).collect(toList());
        }
        return attributes.stream().flatMap(attribute -> {
            List<ContinuousSplit<Integer>> continuousSplits = splitter.possibleContinuousSplits(items, attribute);
            List<DiscreteSplit<Integer>> discreteSplits = splitter.possibleDiscreteSplits(items, attribute);
            List<Split<Integer>> splits = newArrayList(concat(continuousSplits, discreteSplits));
//            splits.forEach(split -> System.out.println("splitS: " + splitError(split)));
            return splits.stream();
        }).min(Comparator.comparing(this::impurity)).orElse(null);
    }

    private double impurity(Split<Integer> split) {
        int total = split.totalNumberOfItems();
        int left = split.numberOnLeft();
        int right = split.numberOnRight();
        return ((double) left / total) * impurity(split.getLeft()) + ((double) right / total) * impurity(split.getRight());
    }

    //visible for testing
    double impurity(List<LabeledMixedItem<Integer>> items) {
        int total = items.size();
        Collection<Integer> counts = items.stream().collect(groupingBy(LabeledMixedItem::getLabel, reducing(0, LabeledMixedItem::getLabel, Integer::sum))).values();
        return counts.stream().mapToDouble(c -> ((double) c / total)).map(n -> n * (1 - n)).sum();
    }


}
