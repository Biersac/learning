package com.flightstats.analytics.tree.decision;

import lombok.Value;

@Value
public class AttributeImportance implements Comparable<AttributeImportance> {
    String attribute;
    Double importance;

    @Override
    public int compareTo(AttributeImportance o) {
        return o.importance.compareTo(importance);
    }
}
