package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Tree;
import com.google.common.collect.Multimap;
import lombok.Value;
import org.la4j.matrix.Matrix;

import java.util.ArrayList;
import java.util.List;

@Value
public class TrainingResults {
    RandomForest forest;
    Multimap<LabeledItem<Integer>, Tree<Integer>> outOfBagTreesForItem;

    //these two items go together. consider squishing them together into a single class.
    Matrix itemProximities;
    List<LabeledItem<Integer>> trainingData;

    public float calculateOutOfBagError(Integer defaultLabel) {
        int total = 0;
        int totalWrong = 0;
        for (LabeledItem<Integer> item : outOfBagTreesForItem.keySet()) {
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

    public Matrix getItemProximities() {
        return itemProximities.divide(forest.size());
    }

}
