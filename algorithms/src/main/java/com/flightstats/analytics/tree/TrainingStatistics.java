package com.flightstats.analytics.tree;

import lombok.Value;

@Value
public class TrainingStatistics {
    //todo: add variable importance measures, and possibly variable proximities.
    double errorEstimate;
}
