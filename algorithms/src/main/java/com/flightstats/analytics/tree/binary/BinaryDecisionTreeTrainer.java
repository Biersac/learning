package com.flightstats.analytics.tree.binary;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;

import static com.google.common.collect.Lists.transform;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class BinaryDecisionTreeTrainer {

    private final BinaryEntropyCalculator entropyCalculator;

    public BinaryDecisionTreeTrainer(BinaryEntropyCalculator entropyCalculator) {
        this.entropyCalculator = entropyCalculator;
    }

    /**
     * Classic ID3 decision tree.
     * http://www.cs.princeton.edu/courses/archive/spr07/cos424/papers/mitchell-dectrees.pdf
     */
    public BinaryDecisionTree train(String name, List<BinaryLabeledItem> labeledItems, List<String> attributesToConsider) {
        return new BinaryDecisionTree(name, train(labeledItems, attributesToConsider, attributesToConsider.size(), true));
    }

    /**
     * For use in training random forests.
     * Suggestion is to use sqrt(# of features) as the maxFeaturesToUse.
     */
    public BinaryDecisionTree train(String name, List<BinaryLabeledItem> labeledItems, List<String> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode) {
        return new BinaryDecisionTree(name, train(labeledItems, attributesToConsider, maxFeaturesToUse, removeFeaturesAtNode));
    }

    private BinaryTreeNode train(List<BinaryLabeledItem> labeledItems, List<String> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode) {
        if (allArePositive(labeledItems)) {
            return new BinaryTreeNode(true);
        }
        if (allAreNegative(labeledItems)) {
            return new BinaryTreeNode(false);
        }
        if (attributesToConsider.isEmpty()) {
            //this most common label thing seems wonky to me. More extensive comment along these lines in the TreeNode class.
            //todo: replace this "default" value with something that can be passed in.
            return new BinaryTreeNode(mostCommonLabel(labeledItems));
        }
        String bestAttribute = bestEntropyGain(labeledItems, attributesToConsider, maxFeaturesToUse).get();
        List<String> newAttributes = new ArrayList<>(attributesToConsider);
        if (removeFeaturesAtNode) {
            newAttributes.remove(bestAttribute);
        }

        Set<Integer> valuesForAttribute = new HashSet<>(transform(labeledItems, li -> li.evaluate(bestAttribute)));
        Map<Integer, BinaryTreeNode> children = new HashMap<>();
        for (Integer integer : valuesForAttribute) {
            children.put(integer, train(labeledItems.stream().filter(li -> integer.equals(li.evaluate(bestAttribute))).collect(toList()), newAttributes, maxFeaturesToUse, removeFeaturesAtNode));
        }

        return new BinaryTreeNode(bestAttribute, children);
    }

    private boolean mostCommonLabel(List<BinaryLabeledItem> labeledItems) {
        int totalItems = labeledItems.size();
        long positives = labeledItems.stream().filter(BinaryLabeledItem::positive).count();
        return positives >= totalItems / 2;
    }

    private boolean allAreNegative(List<BinaryLabeledItem> labeledItems) {
        return labeledItems.stream().allMatch(BinaryLabeledItem::negative);
    }

    private boolean allArePositive(List<BinaryLabeledItem> labeledItems) {
        return labeledItems.stream().allMatch(BinaryLabeledItem::positive);
    }

    @VisibleForTesting
    Optional<String> bestEntropyGain(List<BinaryLabeledItem> items, List<String> attributes, int maxFeaturesToUse) {
        attributes = new ArrayList<>(attributes);
        Collections.shuffle(attributes);
        return attributes.stream().limit(maxFeaturesToUse).max(comparing(o -> entropyCalculator.entropyGain(items, o)));
    }
}
