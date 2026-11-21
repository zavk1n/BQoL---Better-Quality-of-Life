package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterAntiInvisible;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class ANTIINVISIBLEPlayerRender {
    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void bqol$hasLabel(PlayerLikeEntity player, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (BetterAntiInvisible.isEnabled() && distance <= 4096.0) {
            cir.setReturnValue(true);
        }
    }
}