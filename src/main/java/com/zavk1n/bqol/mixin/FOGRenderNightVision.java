package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FOGRenderNightVision {

    @Unique
    private static final StatusEffectInstance BQOL_NIGHT_VISION = new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false);

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void bqol$hasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect != StatusEffects.NIGHT_VISION) {
            return;
        }

        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != entity
            || !BetterFog.isNightVisionEnabled()
            || bqol$hasBlockingEffect(entity)) {
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    private void bqol$getStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (effect != StatusEffects.NIGHT_VISION) {
            return;
        }

        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != entity
            || !BetterFog.isNightVisionEnabled()
            || bqol$hasBlockingEffect(entity)) {
            return;
        }

        cir.setReturnValue(BQOL_NIGHT_VISION);
    }

    @Unique
    private static boolean bqol$hasBlockingEffect(LivingEntity entity) {
        var effects = entity.getActiveStatusEffects();

        return effects.containsKey(StatusEffects.BLINDNESS)
            || effects.containsKey(StatusEffects.DARKNESS)
            || effects.containsKey(StatusEffects.NAUSEA);
    }
}