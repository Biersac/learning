package com.flightstats.analytics.tree.multiclass;

import com.flightstats.analytics.tree.Item;
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
