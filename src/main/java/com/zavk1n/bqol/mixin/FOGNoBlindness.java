package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class FOGNoBlindness {

    @Redirect(
        method = "applyFog",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"
        )
    )
    private static boolean bqol$ignoreBlindness(LivingEntity entity, StatusEffect effect) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null
            && entity == client.player
            && (effect == StatusEffects.BLINDNESS || effect == StatusEffects.DARKNESS)
            && BetterFog.isNoBlindnessEnabled()) {
            return false;
        }

        return entity.hasStatusEffect(effect);
    }
}