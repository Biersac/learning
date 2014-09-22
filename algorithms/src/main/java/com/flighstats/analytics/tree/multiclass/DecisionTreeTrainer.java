package com.flighstats.analytics.tree.multiclass;

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
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<Object> attributesToConsider, int defaultLabel) {
        return new DecisionTree(name, train(labeledItems, attributesToConsider, attributesToConsider.size(), true, defaultLabel));
    }

    /**
     * For use in training random forests.
     * Suggestion is to use sqrt(# of features) as the maxFeaturesToUse.
     */
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<Object> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode, int defaultLabel) {
        return new DecisionTree(name, train(labeledItems, attributesToConsider, maxFeaturesToUse, removeFeaturesAtNode, defaultLabel));
    }

    private TreeNode train(List<LabeledItem> labeledItems, List<Object> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode, int defaultLabel) {
        if (allAreSameLabel(labeledItems)) {
            return new TreeNode(labeledItems.get(0).getLabel());
        }
        if (attributesToConsider.isEmpty()) {
            return new TreeNode(defaultLabel);
        }
        Object bestAttribute = bestEntropyGain(labeledItems, attributesToConsider, maxFeaturesToUse).get();
        List<Object> newAttributes = new ArrayList<>(attributesToConsider);
        if (removeFeaturesAtNode) {
            newAttributes.remove(bestAttribute);
        }

        Set<Integer> valuesForAttribute = new HashSet<>(transform(labeledItems, li -> li.evaluate(bestAttribute)));
        Map<Integer, TreeNode> children = new HashMap<>();
        for (Integer integer : valuesForAttribute) {
            children.put(integer, train(labeledItems.stream().filter(li -> integer.equals(li.evaluate(bestAttribute))).collect(toList()), newAttributes, maxFeaturesToUse, removeFeaturesAtNode, defaultLabel));
        }

        return new TreeNode(bestAttribute, children);
    }

    private boolean allAreSameLabel(List<LabeledItem> labeledItems) {
        return labeledItems.stream().map(LabeledItem::getLabel).distinct().count() == 1;
    }

    @VisibleForTesting
    Optional<Object> bestEntropyGain(List<LabeledItem> items, List<Object> attributes, int maxFeaturesToUse) {
        attributes = new ArrayList<>(attributes);
        Collections.shuffle(attributes);
        return attributes.stream().limit(maxFeaturesToUse).max(comparing(o -> entropyCalculator.entropyGain(items, o)));
    }
}
