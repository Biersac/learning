package com.flighstats.analytics.tree;

import lombok.Value;

import java.util.Map;

@Value
public class Item {
    String id;
    Map<Object, Integer> values;

    public Integer evaluate(Object key) {
        return values.get(key);
    }
}
