package com.flighstats.analytics.tree.multiclass;

import com.flighstats.analytics.tree.Item;
import lombok.Value;

@Value
public class LabeledItem {
    Item item;
    Integer label;

    public Integer evaluate(Object attribute) {
        return item.evaluate(attribute);
    }
}
