package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.LabeledItem;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.flightstats.util.Functional.hashSetOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A first attempt at finding clusters in random forest training data.
 */
public class ClusterFinder<T> {

    public Collection<Set<LabeledItem<T>>> findTrainingClusters(List<LabeledItem<T>> trainingData, Matrix itemProximities) {
        Matrix proximities = itemProximities.transform((i, j, value) -> (1 - value));
        Collection<Set<Integer>> clusters = Collections.emptyList();
        for (int k = 3; k < 100; k++) {
            clusters = findClustersOfSize(k, trainingData, proximities);
            double inClusterSumOfSquares = clusters.stream().mapToDouble(c -> clusterSumOfSquares(c, proximities)).sum();
            System.out.println(k + "\t" + inClusterSumOfSquares);
        }
        //currently returns the last cluster (which is way too big, almost certainly).
        // todo: figure out how to divine the right cluster size from the data generated above.
        return clusters.stream()
                .map(s -> new HashSet<>(Collections2.transform(s, trainingData::get)))
                .collect(toList());
    }

    private Collection<Set<Integer>> findClustersOfSize(int k, List<LabeledItem<T>> trainingData, Matrix proximities) {
        Collection<Set<Integer>> clusters = Collections.emptyList();
        Set<Integer> centroids = initialCentroids(k, trainingData.size());
        //to prevent possible oscillating clusters..stop it at 40 max iterations (what's a good number of max iterations?)
        int tries = 0;
        while (tries < 40) {
            clusters = buildClusters(centroids, proximities);
            Set<Integer> newCentroids = findNewCentroids(clusters, proximities);
            if (newCentroids.equals(centroids)) {
                break;
            }
            tries++;
            centroids = newCentroids;
        }
        return clusters;
    }

    private Collection<Set<Integer>> buildClusters(Set<Integer> centroids, Matrix proximities) {
        Map<Integer, Set<Integer>> clusters = new HashMap<>();
        centroids.forEach(i -> clusters.put(i, hashSetOf(i)));
        for (int i = 0; i < proximities.rows(); i++) {
            Vector row = proximities.getRow(i);
            Integer closest = null;
            for (Integer centroid : centroids) {
                //we already put the centroid into its cluster, so skip it...
                if (centroid == i) {
                    continue;
                }
                if (closest == null || row.get(centroid) < row.get(closest)) {
                    closest = centroid;
                }
            }
            clusters.get(closest).add(i);
        }

        return clusters.values();
    }

    @VisibleForTesting
    Integer findCentroid(Set<Integer> cluster, Matrix proximities) {
        return cluster.stream()
                .min(Comparator.comparing(value -> {
                    Vector row = proximities.getRow(value);
                    List<Double> values = cluster.stream().map(row::get).collect(Collectors.toList());
                    return values.stream().mapToDouble(d -> d).sum();
                }))
                .orElseThrow(() -> new IllegalStateException("no minimum?"));
    }

    private Set<Integer> findNewCentroids(Collection<Set<Integer>> clusters, Matrix proximities) {
        return clusters.stream().map(c -> findCentroid(c, proximities)).collect(toSet());
    }

    private Set<Integer> initialCentroids(int k, int totalNumberOfItems) {
        SecureRandom secureRandom = new SecureRandom();
        Set<Integer> results = new HashSet<>();
        while (results.size() < k) {
            results.add(secureRandom.nextInt(totalNumberOfItems));
        }
        return results;
    }

    private double clusterSumOfSquares(Set<Integer> cluster, Matrix proximities) {
        return cluster.stream().mapToDouble(i -> {
            Vector row = proximities.getRow(i);
            List<Double> values = cluster.stream().map(row::get).collect(Collectors.toList());
            return values.stream().mapToDouble(d -> d * d).sum();
        }).sum();
    }
}
