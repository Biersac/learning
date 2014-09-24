package com.flightstats.analytics.tree.binary;

import com.flightstats.analytics.tree.TrainingStatistics;
import lombok.Value;

@Value
public class TrainingResults {
    BinaryRandomForest forest;
    TrainingStatistics statistics;
}
