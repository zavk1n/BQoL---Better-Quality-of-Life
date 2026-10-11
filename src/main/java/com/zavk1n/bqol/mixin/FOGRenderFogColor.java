package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public abstract class FOGRenderFogColor {

    @ModifyArgs(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"
        ),
        require = 1
    )
    private static void bqol$modifyFogColor(Args args) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || client.world == null
            || !BetterFog.isFogColorEnabled()) {
            return;
        }

        LivingEntity entity = client.player;

        if (entity.hasStatusEffect(StatusEffects.BLINDNESS) && !BetterFog.isNoBlindnessEnabled() || entity.hasStatusEffect(StatusEffects.DARKNESS)) {
            return;
        }

        int rgb = BetterFog.getFogColor();

        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        args.set(0, red);
        args.set(1, green);
        args.set(2, blue);
    }
}