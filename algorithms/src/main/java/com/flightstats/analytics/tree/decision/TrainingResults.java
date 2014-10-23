package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.Item;
import com.flightstats.analytics.tree.LabeledItem;
import com.flightstats.analytics.tree.Tree;
import com.google.common.collect.Multimap;
import lombok.Value;
import org.la4j.matrix.Matrix;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Value
public class TrainingResults {
    RandomForest forest;
    Multimap<LabeledItem<Integer>, Tree<Integer>> outOfBagTreesForItem;

    //these two items go together. consider squishing them together into a single class.
    Matrix itemProximities;
    List<LabeledItem<Integer>> trainingData;
    IdentityHashMap<Tree<Integer>, Set<LabeledItem<Integer>>> outOfBagItemsByTree;

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

    /**
     * This is currently quite slow. You have been warned.
     */
    public TreeSet<AttributeImportance> calculateAttributeImportance() {
        System.out.println("Calculating attribute importance");
        List<String> attributes = trainingData.get(0).attributes();
        TreeSet<AttributeImportance> results = new TreeSet<>();
        for (String attribute : attributes) {
            System.out.print(".");
            results.add(new AttributeImportance(attribute, calculateAttributeImportance(attribute)));
        }
        System.out.println();
        return results;
    }

    //todo: this is a lot of duplicated code...figure out how to unify it (and speed it up!).
    private Double calculateAttributeImportance(String attribute) {
        if (attributeIsDiscrete(attribute)) {
            double totalAcrossTrees = 0;
            for (Map.Entry<Tree<Integer>, Set<LabeledItem<Integer>>> entry : outOfBagItemsByTree.entrySet()) {
                Tree<Integer> tree = entry.getKey();
                Set<LabeledItem<Integer>> outOfBagItems = entry.getValue();
                List<Integer> possibleAttributeValues = outOfBagItems.stream().map(item -> item.getDiscreteValue(attribute)).collect(toList());
                Collections.shuffle(possibleAttributeValues);
                int numberCorrectMinusNumberRandomCorrect = 0;
                for (LabeledItem<Integer> item : outOfBagItems) {
                    Item randomizedItem = item.getItem().withDiscreteValue(attribute, possibleAttributeValues.remove(0));
                    Integer realAnswer = tree.evaluate(item.getItem());
                    Integer randomAnswer = tree.evaluate(randomizedItem);
                    Integer labeledAnswer = item.getLabel();
                    if (realAnswer.equals(labeledAnswer)) {
                        numberCorrectMinusNumberRandomCorrect++;
                    }
                    if (randomAnswer.equals(labeledAnswer)) {
                        numberCorrectMinusNumberRandomCorrect--;
                    }
                }
                totalAcrossTrees += numberCorrectMinusNumberRandomCorrect;
            }
            return totalAcrossTrees / forest.size();
        } else {
            double totalAcrossTrees = 0;
            for (Map.Entry<Tree<Integer>, Set<LabeledItem<Integer>>> entry : outOfBagItemsByTree.entrySet()) {
                Tree<Integer> tree = entry.getKey();
                Set<LabeledItem<Integer>> outOfBagItems = entry.getValue();
                List<Double> possibleAttributeValues = outOfBagItems.stream().map(item -> item.getContinuousValue(attribute)).collect(toList());
                Collections.shuffle(possibleAttributeValues);
                int numberCorrectMinusNumberRandomCorrect = 0;
                for (LabeledItem<Integer> item : outOfBagItems) {
                    Item randomizedItem = item.getItem().withContinuousValue(attribute, possibleAttributeValues.remove(0));
                    Integer realAnswer = tree.evaluate(item.getItem());
                    Integer randomAnswer = tree.evaluate(randomizedItem);
                    Integer labeledAnswer = item.getLabel();
                    if (realAnswer.equals(labeledAnswer)) {
                        numberCorrectMinusNumberRandomCorrect++;
                    }
                    if (randomAnswer.equals(labeledAnswer)) {
                        numberCorrectMinusNumberRandomCorrect--;
                    }
                }
                totalAcrossTrees += numberCorrectMinusNumberRandomCorrect;
            }
            return totalAcrossTrees / forest.size();
        }
    }

    private boolean attributeIsDiscrete(String attribute) {
        return trainingData.stream().map(item -> item.getDiscreteValue(attribute)).filter(Objects::nonNull).anyMatch(i -> true);
    }

    public Matrix getItemProximities() {
        return itemProximities.divide(forest.size());
    }

}
