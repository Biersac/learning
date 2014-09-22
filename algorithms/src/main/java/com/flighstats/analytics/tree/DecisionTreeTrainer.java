package com.flighstats.analytics.tree;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;

import static com.google.common.collect.Lists.transform;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class DecisionTreeTrainer {

    private final EntropyCalculator entropyCalculator;

    public DecisionTreeTrainer(EntropyCalculator entropyCalculator) {
        this.entropyCalculator = entropyCalculator;
    }

    /**
     * Classic ID3 decision tree.
     * http://www.cs.princeton.edu/courses/archive/spr07/cos424/papers/mitchell-dectrees.pdf
     */
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<Object> attributesToConsider) {
        return new DecisionTree(name, train(labeledItems, attributesToConsider, attributesToConsider.size(), true));
    }

    /**
     * For use in training random forests.
     * Suggestion is to use sqrt(# of features) as the maxFeaturesToUse.
     */
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<Object> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode) {
        return new DecisionTree(name, train(labeledItems, attributesToConsider, maxFeaturesToUse, removeFeaturesAtNode));
    }

    private TreeNode train(List<LabeledItem> labeledItems, List<Object> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode) {
        if (allArePositive(labeledItems)) {
            return new TreeNode(true);
        }
        if (allAreNegative(labeledItems)) {
            return new TreeNode(false);
        }
        if (attributesToConsider.isEmpty()) {
            //this most common label thing seems wonky to me. More extensive comment along these lines in the TreeNode class.
            return new TreeNode(mostCommonLabel(labeledItems));
        }
        Object bestAttribute = bestEntropyGain(labeledItems, attributesToConsider, maxFeaturesToUse).get();
        List<Object> newAttributes = new ArrayList<>(attributesToConsider);
        if (removeFeaturesAtNode) {
            newAttributes.remove(bestAttribute);
        }

        Set<Integer> valuesForAttribute = new HashSet<>(transform(labeledItems, li -> li.evaluate(bestAttribute)));
        Map<Integer, TreeNode> children = new HashMap<>();
        for (Integer integer : valuesForAttribute) {
            children.put(integer, train(labeledItems.stream().filter(li -> integer.equals(li.evaluate(bestAttribute))).collect(toList()), newAttributes, maxFeaturesToUse, removeFeaturesAtNode));
        }

        return new TreeNode(bestAttribute, children);
    }

    private boolean mostCommonLabel(List<LabeledItem> labeledItems) {
        int totalItems = labeledItems.size();
        long positives = labeledItems.stream().filter(LabeledItem::positive).count();
        return positives >= totalItems / 2;
    }

    private boolean allAreNegative(List<LabeledItem> labeledItems) {
        return labeledItems.stream().allMatch(LabeledItem::negative);
    }

    private boolean allArePositive(List<LabeledItem> labeledItems) {
        return labeledItems.stream().allMatch(LabeledItem::positive);
    }

    @VisibleForTesting
    Optional<Object> bestEntropyGain(List<LabeledItem> items, List<Object> attributes, int maxFeaturesToUse) {
        attributes = new ArrayList<>(attributes);
        Collections.shuffle(attributes);
        return attributes.stream().limit(maxFeaturesToUse).max(comparing(o -> entropyCalculator.entropyGain(items, o)));
    }
}
