package com.zavk1n.bqol.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class FOGRenderFogDistance {

    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void bqol$customFogDistance(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || client.world == null
            || fogType == BackgroundRenderer.FogType.FOG_SKY
            || !BetterFog.isEnabled()
            || BetterFog.isNoFogEnabled()
            || !BetterFog.isFogDistanceEnabled()
            || bqol$hasBlockingEffect(client.player)) {
            return;
        }

        int chunks = Math.max(1, Math.min(32, BetterFog.getFogDistance()));

        float distance = chunks * 16.0F;

        RenderSystem.setShaderFogStart(distance * 0.75F);
        RenderSystem.setShaderFogEnd(distance);
    }

    private static boolean bqol$hasBlockingEffect(LivingEntity entity) {
        return (entity.hasStatusEffect(StatusEffects.BLINDNESS) && !BetterFog.isNoBlindnessEnabled()) || entity.hasStatusEffect(StatusEffects.DARKNESS);
    }
}