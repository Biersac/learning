package com.flightstats.analytics.examples;

import com.flightstats.analytics.tree.regression.LabeledMixedItem;
import com.flightstats.analytics.tree.regression.MixedItem;
import com.flightstats.analytics.tree.regression.RegressionTree;
import com.flightstats.analytics.tree.regression.RegressionTreeTrainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * An example of training a simple regression tree. The tree doesn't get pruned, and it's grown as far as it possibly can.
 * The data are from the R "cars93" dataset.
 */
public class Cars93Example {

    public static final String MANUAL_AVAIL = "manual.avail";
    public static final String HORSEPOWER = "horsepower";
    public static final String WHEELBASE = "wheelbase";
    public static final String DRIVETRAIN = "drivetrain";
    public static final String TYPE = "type";

    public static Map<String, Integer> typeMapping = new HashMap<>(5);

    static {
        typeMapping.put("\"Compact\"", 0);
        typeMapping.put("\"Large\"", 1);
        typeMapping.put("\"Midsize\"", 2);
        typeMapping.put("\"Small\"", 3);
        typeMapping.put("\"Sporty\"", 4);
        typeMapping.put("\"Van\"", 5);
    }

    public static void main(String[] args) throws IOException {
        Path dataDirectory = Paths.get("algorithms/testdata/cars93");
        Stream<String> lines = Files.lines(dataDirectory.resolve("Cars93.csv")).skip(1);
        List<LabeledMixedItem> data = lines.map(line -> {
            String[] fields = line.split(",");
            double price = Double.parseDouble(fields[5]);
            Map<String, Double> continuousValues = new HashMap<>();
            Map<String, Integer> discreteValues = new HashMap<>();

            addDrivetrain(fields[10], discreteValues);
            addHorsePower(fields[13], continuousValues);
            addManualAvailable(fields[16], discreteValues);
            addWheelBase(fields[20], continuousValues);
            addType(fields[3], discreteValues);

            return new LabeledMixedItem(new MixedItem(fields[0], discreteValues, continuousValues), price);
        }).collect(toList());

        RegressionTreeTrainer trainer = new RegressionTreeTrainer();
        RegressionTree tree = trainer.train("cars93", data, Arrays.asList(MANUAL_AVAIL, HORSEPOWER, WHEELBASE, DRIVETRAIN, TYPE), 5);

        data.forEach(i -> {
            double result = tree.evaluate(i.getItem());
            System.out.printf("label: %f\tresult: %f\tdiff: %f\n", i.getLabel(), result, i.getLabel() - result);
        });

        Map<String, Double> continuousValues = new HashMap<>();
        continuousValues.put(HORSEPOWER, 140d);
        continuousValues.put(WHEELBASE, 100d);

        Map<String, Integer> discreteValues = new HashMap<>();
        discreteValues.put(MANUAL_AVAIL, 1);
        discreteValues.put(DRIVETRAIN, 0);
        discreteValues.put(TYPE, typeMapping.get("\"Sporty\""));

        MixedItem test = new MixedItem("test", discreteValues, continuousValues);
        double result = tree.evaluate(test);
        System.out.println("result = " + result);
    }

    private static void addManualAvailable(String field, Map<String, Integer> discreteValues) {
        discreteValues.put(MANUAL_AVAIL, "\"Yes\"".equals(field) ? 1 : 0);
    }

    private static void addWheelBase(String field, Map<String, Double> continuousValues) {
        double wheelbase = Double.parseDouble(field);
        continuousValues.put(WHEELBASE, wheelbase);
    }

    private static void addHorsePower(String field, Map<String, Double> continuousValues) {
        double horsepower = Double.parseDouble(field);
        continuousValues.put(HORSEPOWER, horsepower);
    }

    private static void addDrivetrain(String field, Map<String, Integer> discreteValues) {
        //0: Front, 1: Rear, 2: 4WD
        discreteValues.put(DRIVETRAIN, "\"Front\"".equals(field) ? 0 : "\"Rear\"".equals(field) ? 1 : 2);
    }

    private static void addType(String field, Map<String, Integer> discreteValues) {
        discreteValues.put(TYPE, typeMapping.get(field));
    }
}
