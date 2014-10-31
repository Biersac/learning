package com.flightstats.analytics.tree;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Item {
    @Getter
    private final String id;
    private final Map<String, Integer> discreteValues;
    private final Map<String, Double> continuousValues;

    public Integer getDiscreteValue(String attribute) {
        return discreteValues.get(attribute);
    }

    public Double getContinuousValue(String attribute) {
        return continuousValues.get(attribute);
    }

    public List<String> getAttributes() {
        return new ArrayList<>(Sets.union(discreteValues.keySet(), continuousValues.keySet()));
    }

    public Item withContinuousValue(String attribute, Double value) {
        Map<String, Double> newValues = new HashMap<>(continuousValues);
        newValues.put(attribute, value);
        return new Item(id, discreteValues, newValues);
    }

    public Item withDiscreteValue(String attribute, Integer value) {
        Map<String, Integer> newValues = new HashMap<>(discreteValues);
        newValues.put(attribute, value);
        return new Item(id, newValues, continuousValues);
    }
}
