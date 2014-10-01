package com.flightstats.analytics.tree;

import com.google.common.collect.Sets;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> getAttributes() {
        return new ArrayList<>(Sets.union(discreteValues.keySet(), continuousValues.keySet()));
    }
}
