package com.flightstats.analytics.tree.decision;

import com.flightstats.analytics.tree.ContinuousTreeNode;
import com.flightstats.analytics.tree.DiscreteTreeNode;
import com.flightstats.analytics.tree.LeafNode;
import com.flightstats.analytics.tree.TreeNode;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Will persist random forests, and reload them. Uses Gson as the serialization mechanism.
 * <p>
 * We should consider not using json, as it's really inefficient, from a space perspective.
 */
public class RandomForestPersister {

    private Gson buildGson() {

        //gah! This is horrible!
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<TreeNode<Integer>>() {
                }.getType(), (JsonSerializer<TreeNode>) (instance, type, context) -> {
                    JsonElement element = context.serialize(instance, instance.getClass());
                    JsonObject o = element.getAsJsonObject();
                    return o;
                })
                .registerTypeAdapter(DiscreteTreeNode.class, (JsonSerializer<DiscreteTreeNode>) (instance, type, context) -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("className", "DiscreteTreeNode");
                    o.addProperty("attribute", instance.getAttribute());
                    o.addProperty("discreteSplitValue", instance.getDiscreteSplitValue());
                    o.add("left", context.serialize(instance.getLeft()));
                    o.add("right", context.serialize(instance.getRight()));
                    return o;
                })
                .registerTypeAdapter(LeafNode.class, (JsonSerializer<LeafNode>) (instance, type, context) -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("className", "LeafNode");
                    o.addProperty("responseType", instance.getResponseValue().getClass().getSimpleName());
                    o.addProperty("responseValue", (Integer) instance.getResponseValue());
                    return o;
                })
                .registerTypeAdapter(ContinuousTreeNode.class, (JsonSerializer<ContinuousTreeNode>) (instance, type, context) -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("className", "ContinuousTreeNode");
                    o.addProperty("attribute", instance.getAttribute());
                    o.addProperty("discreteSplitValue", instance.getContinuousSplitValue());
                    o.add("left", context.serialize(instance.getLeft()));
                    o.add("right", context.serialize(instance.getRight()));
                    return o;
                })
                .registerTypeAdapter(TreeNode.class, (JsonDeserializer<TreeNode>) (json, type, context) -> {
                    String className = json.getAsJsonObject().get("className").getAsString();
                    switch (className) {
                        case "DiscreteTreeNode":
                            return context.deserialize(json, DiscreteTreeNode.class);
                        case "ContinuousTreeNode":
                            return context.deserialize(json, ContinuousTreeNode.class);
                        default:
                            return context.deserialize(json, LeafNode.class);
                    }
                })
                .registerTypeAdapter(LeafNode.class, (JsonDeserializer<LeafNode>) (json, type, context) -> {
                    JsonObject o = json.getAsJsonObject();
                    String responseType = o.getAsJsonPrimitive("responseType").getAsString();
                    if ("Integer".equals(responseType)) {
                        return new LeafNode<>(o.getAsJsonPrimitive("responseValue").getAsInt());
                    } else {
                        return new LeafNode<>(o.getAsJsonPrimitive("responseValue").getAsDouble());
                    }
                })
                .create();
    }

    @SneakyThrows
    public RandomForest load(InputStream inputStream) {
        return buildGson().fromJson(new InputStreamReader(inputStream), RandomForest.class);
    }

    @SneakyThrows
    public void save(RandomForest forest, OutputStream outputStream) {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        buildGson().toJson(forest, writer);
        writer.flush();
    }
}
