package com.flightstats.analytics.tree;

import com.flightstats.analytics.tree.regression.LabeledMixedItem;

import java.util.List;

public interface Split {
    List<LabeledMixedItem> getLeft();

    List<LabeledMixedItem> getRight();
}
