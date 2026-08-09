package com.zavk1n.bqol.mixin;

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

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(BackgroundRenderer.class)
public class FOGNoFog {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void bqol$customFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
                || client.world == null
                || fogType == BackgroundRenderer.FogType.FOG_SKY
                || hasBlockingEffect(client.player)) {
            return;
        }

        if (BetterFog.isNoFogEnabled()) {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            ci.cancel();
            return;
        }
    }

    private static boolean hasBlockingEffect(LivingEntity entity) {
        return entity.hasStatusEffect(StatusEffects.BLINDNESS)
            || entity.hasStatusEffect(StatusEffects.DARKNESS)
            || entity.hasStatusEffect(StatusEffects.NAUSEA);
    }
}