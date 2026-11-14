package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;

import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.ParticlesMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherRendering.class)
public class NORENDERWeather1 {

    @Inject(method = "addParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void bqol$disableWeatherParticles(ClientWorld world, Camera camera, int ticks, ParticlesMode particlesMode, int weatherRadius, CallbackInfo ci) {
        if (world == null) {
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