package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.*;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class RegressionTreeTrainer {
    private final Splitter<Double> splitter;

    public RegressionTreeTrainer(Splitter<Double> splitter) {
        this.splitter = splitter;
    }

    public Tree<Double> train(String name, List<LabeledItem<Double>> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose) {
        return new Tree<>(name, buildNode(trainingData, attributes, numberOfFeaturesToChoose));
    }

    private TreeNode<Double> buildNode(List<LabeledItem<Double>> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose) {
//        System.out.println("initial S: " + (variance(trainingData) * trainingData.size()));
        if (allTrainingDataIsTheSame(trainingData, attributes)) {
            double averageResponse = averageResponse(trainingData);
//            System.out.println("leaf node value = " + averageResponse);
            return new LeafNode<>(averageResponse);
        }
        Split split = bestSplit(trainingData, attributes, numberOfFeaturesToChoose);
        if (split == null) {
            return new LeafNode<>(averageResponse(trainingData));
        }
//        System.out.println("split = " + split);
//        System.out.println("l = " + split.getLeft().size());
//        System.out.println("r = " + split.getRight().size());
//        System.out.println("splitS(split) = " + splitError(split));
        //todo: check to see if the change in S is too small, or if there aren't enough things to do a split. maybe. [probably not needed for random forests, though...]
        if (split instanceof ContinuousSplit) {
            ContinuousSplit continuousSplit = (ContinuousSplit) split;
            return new ContinuousTreeNode<>(continuousSplit.getAttribute(), continuousSplit.getSplitValue(), buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose));
        } else {
            DiscreteSplit discreteSplit = (DiscreteSplit) split;
            return new DiscreteTreeNode<>(discreteSplit.getAttribute(), discreteSplit.getLeftChoice(), buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose));
        }
    }

    private boolean allTrainingDataIsTheSame(List<LabeledItem<Double>> trainingData, Collection<String> attributes) {
        Stream<List<Object>> objectStream = trainingData.stream()
                .map(lmi -> attributes.stream().map(a -> Arrays.asList(lmi.getContinuousValue(a), lmi.getDiscreteValue(a))).collect(toList()));
        return objectStream.distinct().count() == 1;
    }

    private double averageResponse(List<LabeledItem<Double>> items) {
        return items.stream().mapToDouble(LabeledItem::getLabel).average().orElse(Double.NaN);
    }

    private double variance(List<LabeledItem<Double>> items) {
        double average = averageResponse(items);
        return items.stream().mapToDouble(LabeledItem::getLabel).map(value -> square(value - average)).sum() / items.size();
    }

    private double square(double value) {
        return value * value;
    }

    private Split bestSplit(List<LabeledItem<Double>> items, Collection<String> attributes, int numberOfFeaturesToChoose) {
        if (numberOfFeaturesToChoose < attributes.size()) {
            List<String> newAttributes = new ArrayList<>(attributes);
            Collections.shuffle(newAttributes);
            attributes = newAttributes.stream().limit(numberOfFeaturesToChoose).collect(toList());
        }
        return attributes.stream().flatMap(attribute -> {
            List<ContinuousSplit<Double>> continuousSplits = splitter.possibleContinuousSplits(items, attribute);
            List<DiscreteSplit<Double>> discreteSplits = splitter.possibleDiscreteSplits(items, attribute);
            List<Split<Double>> splits = newArrayList(concat(continuousSplits, discreteSplits));
//            splits.forEach(split -> System.out.println("splitS: " + splitError(split)));
            return splits.stream();
        }).min(Comparator.comparing(this::splitError)).orElse(null);
    }

    private double splitError(Split<Double> split) {
        return splitError(split.getLeft(), split.getRight());
    }

    private double splitError(List<LabeledItem<Double>> left, List<LabeledItem<Double>> right) {
        return variance(left) * left.size() + variance(right) * right.size();
    }

}
