package com.flightstats.analytics.tree.regression;

import lombok.Value;

@Value
public class LabeledMixedItem {
    MixedItem item;
    Double label;

    public Integer getDiscreteValue(String attribute) {
        return item.getDiscreteValue(attribute);
    }

    public Double getContinuousValue(String attribute) {
        return item.getContinuousValue(attribute);
    }
}
