package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTnt;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntityRenderer.class)
public abstract class TNTEntityRenderer
    extends EntityRenderer<TntEntity> {

    protected TNTEntityRenderer() {
        super(null);
    }

    @Inject(method = "render(Lnet/minecraft/entity/TntEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void bqol$renderTimer(TntEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        BetterTnt.renderTimer(matrices, entity,
            net.minecraft.client.MinecraftClient
                .getInstance()
                .gameRenderer
                .getCamera(),
            net.minecraft.client.MinecraftClient
                .getInstance()
                .textRenderer, vertexConsumers, light, tickDelta
        );
    }
}