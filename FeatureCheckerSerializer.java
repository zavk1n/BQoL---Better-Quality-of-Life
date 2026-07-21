package com.zavk1n.bqol.utils.liteapi;

import com.google.gson.*;

import java.lang.reflect.Type;

public final class FeatureCheckerSerializer implements JsonSerializer<CheckFeaturesRequest> {

    @Override
    public JsonElement serialize(CheckFeaturesRequest src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray features = new JsonArray();

        for (String feature : src.features()) {
            features.add(feature);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("client", src.client());
        payload.add("features", features);

        JsonObject root = new JsonObject();
        root.addProperty("id", src.id());
        root.addProperty("method", src.method());
        root.add("payload", payload);

        return root;
    }
}
// v1.0