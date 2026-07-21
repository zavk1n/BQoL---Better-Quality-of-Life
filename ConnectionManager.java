package com.zavk1n.bqol.client.init;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.features.CustomHealth;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;

@Environment(EnvType.CLIENT)
public final class ConnectionManager {

    private ConnectionManager() {}

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onDisconnect(client));
    }

    private static void onJoin(MinecraftClient client) {
        CustomHealth.resetDisplay();

        LiteApiManager.sendRequest()
            .thenAcceptAsync(response -> {
                if (response == null) {
                    BQoL.LOGGER.warn("LiteApi returned a null response");
                    return;
                }

                if (response.isOk()) {
                    LiteApiManager.updateBlocklist(new HashSet<>(response.blocklist()));
                    refreshAllModulesBlockedStatus();
                } else {
                    BQoL.LOGGER.warn(
                        "LiteApi error response: {} ({})",
                        response.error(),
                        response.message()
                    );
                }
            }, client)
            .exceptionally(throwable -> {
                BQoL.LOGGER.error("Failed to check LiteApi features", throwable);
                return null;
            });
    }

    private static void onDisconnect(MinecraftClient client) {
        CustomHealth.resetDisplay();
        LiteApiManager.reset();
        refreshAllModulesBlockedStatus();
    }

    private static void refreshAllModulesBlockedStatus() {
        for (FeatureRegistry feature : FeatureRegistry.values()) {
            feature.refreshBlockedStatus();
        }
    }
}
// v1.0