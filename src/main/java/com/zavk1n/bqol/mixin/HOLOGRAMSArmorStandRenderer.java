package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterHolograms;
import com.zavk1n.bqol.utils.render.HologramRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class HOLOGRAMSArmorStandRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$beforeRender(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        HologramRenderState.end();

        if (!BetterHolograms.isVisibleArmorStandEnabled()
                || !(entity instanceof ArmorStandEntity armorStand)
                || !BetterHolograms.isHologram(armorStand)) {
            return;
        }

        HologramRenderState.begin(armorStand);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bqol$afterRender(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        HologramRenderState.end();
    }
}