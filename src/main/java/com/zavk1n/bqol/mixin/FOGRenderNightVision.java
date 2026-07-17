package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.CustomFog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FOGRenderNightVision {

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void bqol$hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || entity != client.player
            || !CustomFog.isNightVisionEnabled()
            || hasBlockingEffect(entity)) {
            return;
        }

        if (effect == StatusEffects.NIGHT_VISION) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    private void bqol$getStatusEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || entity != client.player
            || !CustomFog.isNightVisionEnabled()
            || hasBlockingEffect(entity)) {
            return;
        }

        if (effect == StatusEffects.NIGHT_VISION) {
            cir.setReturnValue(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                false,
                false
            ));
        }
    }

    private static boolean hasBlockingEffect(LivingEntity entity) {
        return entity.getActiveStatusEffects().containsKey(StatusEffects.BLINDNESS)
            || entity.getActiveStatusEffects().containsKey(StatusEffects.DARKNESS)
            || entity.getActiveStatusEffects().containsKey(StatusEffects.NAUSEA);
    }
}