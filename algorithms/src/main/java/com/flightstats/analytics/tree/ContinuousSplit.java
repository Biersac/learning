package com.flightstats.analytics.tree;

import lombok.Value;

import java.util.List;

@Value
public class ContinuousSplit<T> implements Split<T> {
    String attribute;
    Double splitValue;
    List<LabeledMixedItem<T>> left;
    List<LabeledMixedItem<T>> right;
}
