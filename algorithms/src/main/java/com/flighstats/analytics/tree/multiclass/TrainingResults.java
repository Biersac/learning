package com.flighstats.analytics.tree.multiclass;

import com.flighstats.analytics.tree.TrainingStatistics;
import lombok.Value;

@Value
public class TrainingResults {
    RandomForest forest;
    TrainingStatistics statistics;
}
