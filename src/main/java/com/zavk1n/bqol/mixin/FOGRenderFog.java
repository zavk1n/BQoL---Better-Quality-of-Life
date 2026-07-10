package com.zavk1n.bqol.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zavk1n.bqol.features.CustomFog;
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
public class FOGRenderFog {

    private static final float NO_FOG_DISTANCE = Float.MAX_VALUE;

    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
                || hasBlockingEffect(client.player)
                || !CustomFog.isUsingCustomFog()) {
            return;
        }

        if (CustomFog.isNoFogEnabled()) {
            RenderSystem.setShaderFogColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderFogStart(0F);
            RenderSystem.setShaderFogEnd(NO_FOG_DISTANCE);
            return;
        }

        RenderSystem.setShaderFogColor(
            CustomFog.getCurrentRed(),
            CustomFog.getCurrentGreen(),
            CustomFog.getCurrentBlue(),
            1F
        );

        RenderSystem.setShaderFogStart(0F);
        RenderSystem.setShaderFogEnd(CustomFog.getCurrentFogEnd());
    }

    /// Список эффектов
    private static boolean hasBlockingEffect(LivingEntity entity) {
        return entity.hasStatusEffect(StatusEffects.BLINDNESS)
            || entity.hasStatusEffect(StatusEffects.DARKNESS)
            || entity.hasStatusEffect(StatusEffects.NAUSEA);
    }
}
// v1.0