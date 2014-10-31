package com.flightstats.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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

    public static <T> Collection<T> times(int n, NoThrowCallable<T> callable) {
        return times(n).map(x -> callable.call()).collect(toList());
    }

    public static <T> Collection<T> timesParallel(int n, NoThrowCallable<T> callable) {
        return times(n).parallel().map(x -> callable.call()).collect(toList());
    }

    public static interface NoThrowCallable<T> {
        T call();
    }
}
