package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.CustomFog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class FOGRenderNightVision {
    private static final float FULL_NIGHT_VISION = 1.0F;

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void onGetNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || entity != client.player
            || !CustomFog.isNightVisionEnabled()
            || hasBlockingEffect(entity)) {
            return;
        }

        cir.setReturnValue(FULL_NIGHT_VISION);
    }

    /// Список эффектов
    private static boolean hasBlockingEffect(LivingEntity entity) {
        return entity.hasStatusEffect(StatusEffects.BLINDNESS)
            || entity.hasStatusEffect(StatusEffects.DARKNESS)
            || entity.hasStatusEffect(StatusEffects.NAUSEA);
    }
}
// v1.0