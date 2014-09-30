package com.flightstats.analytics.tree;

import com.flightstats.analytics.tree.regression.LabeledMixedItem;
import lombok.Value;

import java.util.List;

@Value
public class DiscreteSplit implements Split {
    String attribute;
    Integer leftChoice;
    List<LabeledMixedItem> left;
    List<LabeledMixedItem> right;
}
