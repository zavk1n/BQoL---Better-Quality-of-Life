package com.zavk1n.bqol.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;

public interface ACCESSORHitColorOverlay {

    static void reload() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.gameRenderer == null) {
            return;
        }

        OverlayTexture overlay = client.gameRenderer.getOverlayTexture();

        if (overlay != null) {
            ((ACCESSORHitColorOverlay) (Object) overlay).bqol$reload();
        }
    }

    void bqol$reload();
}