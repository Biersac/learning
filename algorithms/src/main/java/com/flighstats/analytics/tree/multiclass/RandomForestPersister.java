package com.flighstats.analytics.tree.multiclass;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Will persist random forests, and reload them. Uses Gson as the serialization mechanism.
 * <p/>
 * We should consider not using json, as it's really inefficient, from a space perspective.
 */
public class RandomForestPersister {

    private Gson buildGson() {
        Type integerKeyedMapType = new TypeToken<Map<Integer, TreeNode>>() {
        }.getType();

        return new GsonBuilder()
                //this is necessary, since gson will always map json numbers into Doubles. We need Integer.
                .registerTypeAdapter(integerKeyedMapType, (JsonDeserializer<Map<Integer, TreeNode>>) (json, typeOfT, context) -> {
                    JsonObject map = json.getAsJsonObject();
                    Map<Integer, TreeNode> result = new HashMap<>();
                    Set<Map.Entry<String, JsonElement>> entries = map.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        result.put(Integer.valueOf(entry.getKey()), context.deserialize(entry.getValue(), TreeNode.class));
                    }
                    return result;
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
