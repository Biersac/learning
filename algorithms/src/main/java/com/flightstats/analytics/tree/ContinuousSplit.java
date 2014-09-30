package com.flightstats.analytics.tree;

import com.flightstats.analytics.tree.regression.LabeledMixedItem;
import lombok.Value;

import java.util.List;

@Value
public class ContinuousSplit implements Split {
    String attribute;
    Double splitValue;
    List<LabeledMixedItem> left;
    List<LabeledMixedItem> right;
}
