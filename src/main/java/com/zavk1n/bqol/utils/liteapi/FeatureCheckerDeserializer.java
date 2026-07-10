package com.zavk1n.bqol.utils.liteapi;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class FeatureCheckerDeserializer
    implements JsonDeserializer<CheckFeaturesResponse> {

    @Override
    public CheckFeaturesResponse deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {

        if (json == null || json.isJsonNull()) {
            throw new JsonParseException("Response is null");
        }

        if (!json.isJsonObject()) {
            throw new JsonParseException("Response must be a JSON object");
        }

        JsonObject root = json.getAsJsonObject();

        JsonElement idElement = root.get("id");

        if (idElement == null || idElement.isJsonNull()) {
            throw new JsonParseException("Missing required field 'id'");
        }

        if (!idElement.isJsonPrimitive()
            || !idElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("'id' must be a string");
        }

        String id = idElement.getAsString();

        JsonElement okElement = root.get("ok");

        if (okElement == null || okElement.isJsonNull()) {
            throw new JsonParseException("Missing required field 'ok'");
        }

        if (!okElement.isJsonPrimitive()
            || !okElement.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("'ok' must be a boolean");
        }

        boolean ok = okElement.getAsBoolean();

        if (!ok) {
            JsonElement errorElement = root.get("error");

            if (errorElement == null || errorElement.isJsonNull()) {
                throw new JsonParseException("Missing required field 'error'");
            }

            if (!errorElement.isJsonPrimitive()
                || !errorElement.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'error' must be a string");
            }

            String error = errorElement.getAsString();

            JsonElement messageElement = root.get("message");

            String message = null;

            if (messageElement != null && !messageElement.isJsonNull()) {
                if (!messageElement.isJsonPrimitive()
                    || !messageElement.getAsJsonPrimitive().isString()) {
                    throw new JsonParseException("'message' must be a string");
                }

                message = messageElement.getAsString();
            }

            return CheckFeaturesResponse.failure(id, error, message);
        }

        JsonElement payloadElement = root.get("payload");

        if (payloadElement == null || payloadElement.isJsonNull()) {
            throw new JsonParseException("Missing required field 'payload'");
        }

        if (!payloadElement.isJsonObject()) {
            throw new JsonParseException("'payload' must be an object");
        }

        JsonObject payload = payloadElement.getAsJsonObject();

        JsonElement blocklistElement = payload.get("blocklist");

        if (blocklistElement == null || blocklistElement.isJsonNull()) {
            throw new JsonParseException("Missing required field 'blocklist'");
        }

        if (!blocklistElement.isJsonArray()) {
            throw new JsonParseException("'blocklist' must be an array");
        }

        JsonArray array = blocklistElement.getAsJsonArray();

        List<String> blocklist = new ArrayList<>(array.size());

        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException(
                    "Every blocklist entry must be a string"
                );
            }

            blocklist.add(element.getAsString());
        }

        return CheckFeaturesResponse.success(id, blocklist);
    }
}
// v1.0