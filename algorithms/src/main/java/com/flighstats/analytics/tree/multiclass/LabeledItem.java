package com.flighstats.analytics.tree.multiclass;

import com.flighstats.analytics.tree.Item;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class LabeledItem {
    Item item;
    Integer label;

    public Integer evaluate(String attribute) {
        return item.evaluate(attribute);
    }

    public List<String> attributes() {
        return new ArrayList<>(item.getValues().keySet());
    }
}
