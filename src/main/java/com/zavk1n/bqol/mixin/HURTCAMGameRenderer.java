package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHurtCam;

import net.minecraft.client.render.GameRenderer;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class HURTCAMGameRenderer {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void bqol$disableHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (BetterHurtCam.isEnabled()) {
            ci.cancel();
        }
    }
}