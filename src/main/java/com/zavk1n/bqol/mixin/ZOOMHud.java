package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterZoom;
import net.minecraft.client.gui.hud.InGameHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class ZOOMHud {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void bqol$hideCrosshair(CallbackInfo ci) {
        if (BetterZoom.isZoomActive() || (BetterZoom.isTransitionActive() && BetterZoom.getMultiplier(1.0F) < 0.999F)) {
            ci.cancel();
        }
    }
}