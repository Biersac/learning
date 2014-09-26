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

public class Cars93Example {
    public static void main(String[] args) throws IOException {
        Path dataDirectory = Paths.get("algorithms/testdata/cars93");
        Stream<String> lines = Files.lines(dataDirectory.resolve("Cars93.csv")).skip(1);
        List<LabeledMixedItem> data = lines.map(line -> {
            String[] fields = line.split(",");
            double price = Double.parseDouble(fields[5]);
            double horsepower = Double.parseDouble(fields[13]);
            double wheelbase = Double.parseDouble(fields[20]);
            Map<String, Double> continuousValues = new HashMap<>();
            continuousValues.put("horsepower", horsepower);
            continuousValues.put("wheelbase", wheelbase);
            return new LabeledMixedItem(new MixedItem(fields[0], new HashMap<>(), continuousValues), price);
        }).collect(toList());

        RegressionTreeTrainer trainer = new RegressionTreeTrainer();
        RegressionTree tree = trainer.train("cars93", data, Arrays.asList("horsepower", "wheelbase"));

        data.forEach(i -> {
            double result = tree.evaluate(i.getItem());
            System.out.printf("%f\t%f\t%f\n", i.getLabel(), result, i.getLabel() - result);
        });

        Map<String, Double> continuousValues = new HashMap<>();
        continuousValues.put("horsepower", 100d);
        continuousValues.put("wheelbase", 100d);
        MixedItem test = new MixedItem("test", new HashMap<>(), continuousValues);
        double result = tree.evaluate(test);
        System.out.println("result = " + result);
    }
}
