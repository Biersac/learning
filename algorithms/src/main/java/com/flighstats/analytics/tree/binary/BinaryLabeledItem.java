package com.flighstats.analytics.tree.binary;

import com.flighstats.analytics.tree.Item;
import lombok.Value;

@Value
public class BinaryLabeledItem {
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

    public Integer evaluate(String attribute) {
        return item.evaluate(attribute);
    }
}
