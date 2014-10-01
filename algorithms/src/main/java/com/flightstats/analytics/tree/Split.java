package com.flightstats.analytics.tree;

import java.util.List;

public interface Split<T> {
    List<LabeledItem<T>> getLeft();

    List<LabeledItem<T>> getRight();

    default int totalNumberOfItems() {
        return getLeft().size() + getRight().size();
    }

    default int numberOnLeft() {
        return getLeft().size();
    }

    default int numberOnRight() {
        return getRight().size();
    }
}
