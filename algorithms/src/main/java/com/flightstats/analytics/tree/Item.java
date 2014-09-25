package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.Map;

@Value
public class Item {
    String id;
    Map<String, Integer> values;

    public Integer value(String key) {
        return values.get(key);
    }
}
