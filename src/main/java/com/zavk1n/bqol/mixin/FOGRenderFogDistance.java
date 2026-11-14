package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterFog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FogRenderer.class)
public abstract class FOGRenderFogDistance {

    @ModifyArgs(
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
        )
    )
    private void bqol$modifyFog(Args args, Camera camera, int viewDistance, RenderTickCounter tickCounter, float tickDelta, ClientWorld world) {
        if (!BetterFog.isEnabled()) {
            return;
        }

        CameraSubmersionType submersion = camera.getSubmersionType();

        if (submersion == CameraSubmersionType.WATER
            || submersion == CameraSubmersionType.LAVA
            || submersion == CameraSubmersionType.POWDER_SNOW) {
            return;
        }

        if (BetterFog.isNoFogEnabled()) {
            float noFog = Float.MAX_VALUE;

            args.set(3, noFog);
            args.set(4, noFog);
            args.set(5, noFog);
            args.set(6, noFog);
            args.set(7, noFog);
            args.set(8, noFog);

            return;
        }

        if (!BetterFog.isFogDistanceEnabled()) {
            return;
        }

        float distance = BetterFog.getFogDistance() * 16.0F;

        float start = distance * 0.75F;
        float end = distance;

        args.set(3, start);
        args.set(4, end);
        args.set(5, start);
        args.set(6, end);
    }
}