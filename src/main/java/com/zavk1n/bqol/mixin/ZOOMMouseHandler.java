package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterZoom;

import net.minecraft.client.Mouse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class ZOOMMouseHandler {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void bqol$handleZoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!BetterZoom.isZoomActive() || vertical == 0.0) {
            return;
        }

        BetterZoom.scroll(vertical > 0.0);

        ci.cancel();
    }
}