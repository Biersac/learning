package com.flightstats.analytics.tree.regression;

import com.flightstats.analytics.tree.LabeledMixedItem;
import com.flightstats.analytics.tree.Tree;
import com.google.common.collect.Multimap;
import lombok.Value;

import java.util.ArrayList;

@Value
public class TrainingResults {
    RegressionRandomForest forest;
    Multimap<LabeledMixedItem<Double>, Tree<Double>> outOfBagTreesForItem;

    public double calculateOutOfBagError() {
        int total = 0;
        double totalVar = 0;
        for (LabeledMixedItem<Double> item : outOfBagTreesForItem.keySet()) {
            RegressionRandomForest subForest = new RegressionRandomForest(new ArrayList<>(outOfBagTreesForItem.get(item)));
            Double response = subForest.evaluate(item.getItem());
            Double truth = item.getLabel();
            double var = Math.pow(response - truth, 2);
            totalVar += var;
            total++;
        }
        return Math.sqrt(totalVar) / total;
    }
}
