package com.zavk1n.bqol.client.init;

import com.zavk1n.bqol.BQoL;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class TickManager {
    private static final FeatureRegistry[] FEATURES = FeatureRegistry.values();

    private TickManager() { }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (FeatureRegistry feature : FEATURES) {
                try {
                    feature.tick();
                } catch (Exception e) {
                    BQoL.LOGGER.error(
                        "Error in feature tick handler: {}",
                        feature.name(),
                        e
                    );
                }
            }
        });

        BQoL.LOGGER.info("Registered all feature tick handlers");
    }
}
// v1.0