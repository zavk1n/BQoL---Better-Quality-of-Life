package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.config.BQoLConfig;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.FrameGraphBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class NORENDERWeather {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void bqol$renderWeather(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        BQoLConfig config = BQoLConfig.getInstance();

        if (!config.isNoRenderWeatherEnabled()) {
            return;
        }

        if (config.getNoRenderWeather() == BQoLConfig.RenderMode.NO_RENDER) {
            ci.cancel();
        }
    }
}