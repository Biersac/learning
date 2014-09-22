package com.flighstats.analytics.tree.binary;

import com.flighstats.analytics.tree.TrainingStatistics;
import lombok.Value;

@Value
public class TrainingResults {
    BinaryRandomForest forest;
    TrainingStatistics statistics;
}
