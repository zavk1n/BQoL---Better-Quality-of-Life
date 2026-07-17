package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class NORENDERWeather {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void bqol$renderWeather(LightmapTextureManager lightmapTextureManager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (config.isNoRenderEnabled() && config.getNoRenderWeather() == RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}