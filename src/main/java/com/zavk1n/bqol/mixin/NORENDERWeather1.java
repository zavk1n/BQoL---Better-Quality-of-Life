package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class NORENDERWeather1 {

    @Inject(method = "tickRainSplashing", at = @At("HEAD"), cancellable = true)
    private void bqol$disableWeatherParticles(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderWeatherEnabled()) {
            return;
        }

        if (config.getNoRenderWeather() == BQoLConfig.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}
// v1.0