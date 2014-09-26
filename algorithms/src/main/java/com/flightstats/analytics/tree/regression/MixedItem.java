package com.flightstats.analytics.tree.regression;

import lombok.Value;

import java.util.Map;

@Value
public class MixedItem {
    String id;
    Map<String, Integer> discreteValues;
    Map<String, Double> continuousValues;

    public Integer getDiscreteValue(String attribute) {
        return discreteValues.get(attribute);
    }

    public Double getContinuousValue(String attribute) {
        return continuousValues.get(attribute);
    }
}
