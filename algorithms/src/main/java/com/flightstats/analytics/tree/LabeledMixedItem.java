package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

@Value
public class LabeledMixedItem<T> {
    MixedItem item;
    T label;

    public Integer getDiscreteValue(String attribute) {
        return item.getDiscreteValue(attribute);
    }

    public Double getContinuousValue(String attribute) {
        return item.getContinuousValue(attribute);
    }

    public List<String> attributes() {
        return item.getAttributes();
    }
}
