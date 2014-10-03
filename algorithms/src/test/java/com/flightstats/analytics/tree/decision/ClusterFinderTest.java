package com.flightstats.analytics.tree.decision;

import com.flightstats.util.Functional;
import org.junit.Test;
import org.la4j.matrix.dense.Basic2DMatrix;

import static org.junit.Assert.assertEquals;

public class ClusterFinderTest {
    @Test
    public void testCentroidFinding() throws Exception {
        ClusterFinder<Integer> testClass = new ClusterFinder<>();

        Basic2DMatrix proximities = new Basic2DMatrix(new double[][]{
                {0.0, 0.8, 0.5, 0.0},
                {0.8, 0.0, 0.5, 0.0},
                {0.5, 0.5, 0.0, 1.0},
                {0.0, 0.0, 1.0, 0.0},
        });

        Integer centroid = testClass.findCentroid(Functional.hashSetOf(0, 1, 2), proximities);
        assertEquals((Integer) 2, centroid);
    }

}