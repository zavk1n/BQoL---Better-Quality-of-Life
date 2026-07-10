package com.zavk1n.bqol.utils.liteapi;

import java.util.List;
import java.util.Objects;

public final class CheckFeaturesRequest extends Request {

    private final String client;
    private final List<String> features;

    public CheckFeaturesRequest(String client, List<String> features) {
        super("checkFeatures");

        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(features, "features");

        if (client.isBlank()) {
            throw new IllegalArgumentException("client must not be blank");
        }

        for (String feature : features) {
            Objects.requireNonNull(feature, "feature");
        }

        this.client = client;
        this.features = List.copyOf(features);
    }

    public String client() {
        return client;
    }

    public List<String> features() {
        return features;
    }
}
// v1.0