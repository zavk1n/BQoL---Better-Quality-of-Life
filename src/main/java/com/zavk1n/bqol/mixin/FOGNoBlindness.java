package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.fog.BlindnessEffectFogModifier;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectFogModifier.class)
public abstract class FOGNoBlindness {

    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void bqol$ignoreBlindness(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        boolean isBlindness = (Object) this instanceof BlindnessEffectFogModifier;

        if (client.player != null
            && cameraEntity == client.player
            && isBlindness
            && BetterFog.isNoBlindnessEnabled()) {
            cir.setReturnValue(false);
        }
    }
}