package com.flighstats.analytics.tree;

import lombok.Value;

@Value
public class LabeledItem {
    Item item;
    boolean label;

    public boolean positive() {
        return label;
    }

    public boolean negative() {
        return !label;
    }

    public boolean getLabel() {
        return label;
    }

    public Integer evaluate(Object attribute) {
        return item.evaluate(attribute);
    }
}
