package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FOGRenderFogColor {

    @Inject(method = "getFogColor(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)Lorg/joml/Vector4f;", at = @At("RETURN"), cancellable = true)
    private void bqol$modifyFogColor(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || client.world == null
            || !BetterFog.isFogColorEnabled()) {
            return;
        }

        LivingEntity entity = client.player;

        if ((entity.hasStatusEffect(StatusEffects.BLINDNESS) && !BetterFog.isNoBlindnessEnabled()) || entity.hasStatusEffect(StatusEffects.DARKNESS)) {
            return;
        }

        CameraSubmersionType submersion = camera.getSubmersionType();

        if (submersion == CameraSubmersionType.WATER
            || submersion == CameraSubmersionType.LAVA
            || submersion == CameraSubmersionType.POWDER_SNOW) {
            return;
        }

        int rgb = BetterFog.getFogColor();

        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        Vector4f color = cir.getReturnValue();

        if (color != null) {
            color.set(red, green, blue, color.w());
        }
    }
}