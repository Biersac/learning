package com.flighstats.analytics.tree;

import lombok.Value;

@Value
public class TrainingResults {
    RandomForest forest;
    TrainingStatistics statistics;
}
