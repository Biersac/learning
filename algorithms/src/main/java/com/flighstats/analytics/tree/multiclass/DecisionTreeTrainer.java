package com.flighstats.analytics.tree.multiclass;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;

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
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<String> attributesToConsider, int defaultLabel) {
        Map<String, Set<Integer>> validValuesForAttributes = validValuesForAttributes(labeledItems, attributesToConsider);
        return new DecisionTree(name, trainRecursively(labeledItems, attributesToConsider, attributesToConsider.size(), true, defaultLabel, validValuesForAttributes));
    }

    /**
     * For use in training random forests.
     * Suggestion is to use sqrt(# of features) as the maxFeaturesToUse (although Breiman says 2 works fine).
     */
    public DecisionTree train(String name, List<LabeledItem> labeledItems, List<String> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode, int defaultLabel, Map<String, Set<Integer>> validValuesForAttributes) {
        return new DecisionTree(name, trainRecursively(labeledItems, attributesToConsider, maxFeaturesToUse, removeFeaturesAtNode, defaultLabel, validValuesForAttributes));
    }

    Map<String, Set<Integer>> validValuesForAttributes(List<LabeledItem> items, List<String> attributes) {
        Map<String, Set<Integer>> results = new HashMap<>();
        for (String attribute : attributes) {
            results.put(attribute, new HashSet<>());
        }
        for (String attribute : attributes) {
            for (LabeledItem item : items) {
                results.get(attribute).add(item.evaluate(attribute));
            }
        }
        return results;
    }

    private TreeNode trainRecursively(List<LabeledItem> labeledItems, List<String> attributesToConsider, int maxFeaturesToUse, boolean removeFeaturesAtNode, int defaultLabel, Map<String, Set<Integer>> validValuesForAttributes) {
        if (labeledItems.isEmpty()) {
            return new TreeNode(defaultLabel);
        }
        if (allAreSameLabel(labeledItems)) {
            return new TreeNode(labeledItems.get(0).getLabel());
        }
        if (attributesToConsider.isEmpty()) {
            return new TreeNode(defaultLabel);
        }
        String bestAttribute = bestEntropyGain(labeledItems, attributesToConsider, maxFeaturesToUse).get();
        List<String> newAttributes = new ArrayList<>(attributesToConsider);
        if (removeFeaturesAtNode) {
            newAttributes.remove(bestAttribute);
        }

        Set<Integer> valuesForAttribute = validValuesForAttributes.get(bestAttribute);
        Map<Integer, TreeNode> children = new HashMap<>();
        for (Integer integer : valuesForAttribute) {
            children.put(integer, trainRecursively(labeledItems.stream().filter(li -> integer.equals(li.evaluate(bestAttribute))).collect(toList()), newAttributes, maxFeaturesToUse, removeFeaturesAtNode, defaultLabel, validValuesForAttributes));
        }

        return new TreeNode(bestAttribute, children);
    }

    private boolean allAreSameLabel(List<LabeledItem> labeledItems) {
        return labeledItems.stream().map(LabeledItem::getLabel).distinct().count() == 1;
    }

    @VisibleForTesting
    Optional<String> bestEntropyGain(List<LabeledItem> items, List<String> attributes, int maxFeaturesToUse) {
        attributes = new ArrayList<>(attributes);
        Collections.shuffle(attributes);
        return attributes.stream().limit(maxFeaturesToUse).max(comparing(o -> entropyCalculator.entropyGain(items, o)));
    }
}
