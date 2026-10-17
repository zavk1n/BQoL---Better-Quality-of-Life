package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterRRLS;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class RRLSMinecraftClient {

    @Inject(method = "setOverlay", at = @At("TAIL"))
    private void bqol$onSetOverlay(Overlay overlay, CallbackInfo ci) {
        if (overlay == null) {
            BetterRRLS.getInstance().finishReload();
        }
    }
}