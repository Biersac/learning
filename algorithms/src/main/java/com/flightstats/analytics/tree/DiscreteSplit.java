package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

@Value
public class DiscreteSplit<T> implements Split<T> {
    String attribute;
    Integer leftChoice;
    List<LabeledItem<T>> left;
    List<LabeledItem<T>> right;
}
