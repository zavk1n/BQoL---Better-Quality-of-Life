package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.features.BetterZoom;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class ZOOMGameRenderer {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void bqol$applyZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (!BetterZoom.isZoomActive() && !BetterZoom.isTransitionActive()) {
            return;
        }

        if (BetterZoom.isUsingSpyglass()) {
            return;
        }

        float originalFov = cir.getReturnValue();
        float multiplier = BetterZoom.getMultiplier(tickDelta);
        float zoomedFov = originalFov * multiplier;

        cir.setReturnValue(zoomedFov);
    }
}