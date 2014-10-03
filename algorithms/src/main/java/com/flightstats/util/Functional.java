package com.flightstats.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Functional {
    public static Stream<Void> times(int number) {
        return Arrays.stream(new Void[number]);
    }

    public static void times(int n, Runnable r) {
        times(n).forEach(x -> r.run());
    }

    public static void timesParallel(int n, Runnable r) {
        times(n).parallel().forEach(x -> r.run());
    }

    public static <T> Set<T> hashSetOf(T... ts) {
        Set<T> results = new HashSet<>();
        Collections.addAll(results, ts);
        return results;
    }
}
