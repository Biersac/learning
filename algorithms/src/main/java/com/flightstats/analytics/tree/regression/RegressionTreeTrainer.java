package com.flightstats.analytics.tree.regression;

import lombok.Value;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class RegressionTreeTrainer {
    public RegressionTree train(String name, List<LabeledMixedItem> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose) {
        return new RegressionTree(name, buildNode(trainingData, attributes, numberOfFeaturesToChoose));
    }

    private RegressionTreeNode buildNode(List<LabeledMixedItem> trainingData, Collection<String> attributes, int numberOfFeaturesToChoose) {
//        System.out.println("initial S: " + (variance(trainingData) * trainingData.size()));
        if (allTrainingDataIsTheSame(trainingData, attributes)) {
            double averageResponse = averageResponse(trainingData);
//            System.out.println("leaf node value = " + averageResponse);
            return new LeafNode(averageResponse);
        }
        Split split = bestSplit(trainingData, attributes, numberOfFeaturesToChoose);
        if (split == null) {
            return new LeafNode(averageResponse(trainingData));
        }
//        System.out.println("split = " + split);
//        System.out.println("l = " + split.getLeft().size());
//        System.out.println("r = " + split.getRight().size());
//        System.out.println("splitS(split) = " + splitError(split));
        //todo: check to see if the change in S is too small, or if there aren't enough things to do a split. maybe. [probably not needed for random forests, though...]
        if (split instanceof ContinuousSplit) {
            ContinuousSplit continuousSplit = (ContinuousSplit) split;
            return new ContinuousTreeNode(continuousSplit.attribute, continuousSplit.splitValue, buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose));
        } else {
            DiscreteSplit discreteSplit = (DiscreteSplit) split;
            return new DiscreteTreeNode(discreteSplit.attribute, discreteSplit.getLeftChoice(), buildNode(split.getLeft(), attributes, numberOfFeaturesToChoose), buildNode(split.getRight(), attributes, numberOfFeaturesToChoose));
        }
    }

    private boolean allTrainingDataIsTheSame(List<LabeledMixedItem> trainingData, Collection<String> attributes) {
        Stream<List<Object>> objectStream = trainingData.stream()
                .map(lmi -> attributes.stream().map(a -> Arrays.asList(lmi.getContinuousValue(a), lmi.getDiscreteValue(a))).collect(toList()));
        return objectStream.distinct().count() == 1;
    }

    private double averageResponse(List<LabeledMixedItem> items) {
        return items.stream().mapToDouble(LabeledMixedItem::getLabel).average().orElse(Double.NaN);
    }

    private double variance(List<LabeledMixedItem> items) {
        double average = averageResponse(items);
        return items.stream().mapToDouble(LabeledMixedItem::getLabel).map(value -> square(value - average)).sum() / items.size();
    }

    private double square(double value) {
        return value * value;
    }

    private Split bestSplit(List<LabeledMixedItem> items, Collection<String> attributes, int numberOfFeaturesToChoose) {
        if (numberOfFeaturesToChoose < attributes.size()) {
            List<String> newAttributes = new ArrayList<>(attributes);
            Collections.shuffle(newAttributes);
            attributes = newAttributes.stream().limit(numberOfFeaturesToChoose).collect(toList());
        }
        return attributes.stream().flatMap(attribute -> {
            List<ContinuousSplit> continuousSplits = possibleContinuousSplits(items, attribute);
            List<DiscreteSplit> discreteSplits = possibleDiscreteSplits(items, attribute);
            List<Split> splits = newArrayList(concat(continuousSplits, discreteSplits));
//            splits.forEach(split -> System.out.println("splitS: " + splitError(split)));
            return splits.stream();
        }).min(Comparator.comparing(this::splitError)).orElse(null);
    }

    private List<ContinuousSplit> possibleContinuousSplits(List<LabeledMixedItem> items, String attribute) {
        return splitPoints(items, attribute).stream().map(v -> split(items, attribute, v)).collect(toList());
    }

    private ContinuousSplit split(List<LabeledMixedItem> items, String attribute, Double value) {
        List<LabeledMixedItem> left = items.stream().filter(i -> i.getContinuousValue(attribute) < value).collect(toList());
        List<LabeledMixedItem> right = items.stream().filter(i -> i.getContinuousValue(attribute) >= value).collect(toList());
        return new ContinuousSplit(attribute, value, left, right);
    }

    private List<Double> splitPoints(List<LabeledMixedItem> items, String attribute) {
        double[] values = items.stream()
                .map(i -> i.getContinuousValue(attribute))
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .distinct()
                .mapToDouble(d -> d)
                .toArray();
        List<Double> results = new ArrayList<>();
        for (int i = 1; i < values.length; i++) {
            double value1 = values[i - 1];
            double value2 = values[i];
            results.add((value2 + value1) / 2);
        }
        return results;
    }

    private List<DiscreteSplit> possibleDiscreteSplits(List<LabeledMixedItem> items, String attribute) {
        Set<Integer> attributeValues = items.stream().map(i -> i.getDiscreteValue(attribute)).filter(Objects::nonNull).collect(toSet());
        return attributeValues.stream().map(av -> split(items, attribute, av)).collect(toList());
    }

    private DiscreteSplit split(List<LabeledMixedItem> items, String attribute, Integer attributeValue) {
        List<LabeledMixedItem> left = items.stream().filter(i -> attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        List<LabeledMixedItem> right = items.stream().filter(i -> !attributeValue.equals(i.getDiscreteValue(attribute))).collect(toList());
        return new DiscreteSplit(attribute, attributeValue, left, right);
    }

    private double splitError(Split split) {
        return splitError(split.getLeft(), split.getRight());
    }

    private double splitError(List<LabeledMixedItem> left, List<LabeledMixedItem> right) {
        return variance(left) * left.size() + variance(right) * right.size();
    }

    private static interface Split {
        List<LabeledMixedItem> getLeft();

        List<LabeledMixedItem> getRight();
    }

    @Value
    private static class ContinuousSplit implements Split {
        String attribute;
        Double splitValue;
        List<LabeledMixedItem> left;
        List<LabeledMixedItem> right;
    }

    @Value
    private static class DiscreteSplit implements Split {
        String attribute;
        Integer leftChoice;
        List<LabeledMixedItem> left;
        List<LabeledMixedItem> right;
    }
}
