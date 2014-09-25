package com.flightstats.analytics.tree.multiclass;

import com.google.common.collect.Multimap;
import lombok.Value;

import java.util.ArrayList;

@Value
public class TrainingResults {
    RandomForest forest;
    Multimap<LabeledItem, DecisionTree> outOfBagTreesForItem;

    public float calculateOutOfBagError(Integer defaultLabel) {
        int total = 0;
        int totalWrong = 0;
        for (LabeledItem item : outOfBagTreesForItem.keySet()) {
            RandomForest subForest = new RandomForest(new ArrayList<>(outOfBagTreesForItem.get(item)), defaultLabel);
            Integer response = subForest.evaluate(item.getItem());
            Integer truth = item.getLabel();
            boolean subTreeIsCorrect = truth.equals(response);
            if (!subTreeIsCorrect) {
                totalWrong++;
            }

            total++;
        }
        return ((float) totalWrong) / total;
    }
}
