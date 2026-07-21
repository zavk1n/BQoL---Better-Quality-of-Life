package com.zavk1n.bqol.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zavk1n.bqol.features.CustomHealth;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class HEALTHRenderIndicator {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"
        ),
        require = 0
    )
    private void onRenderLabel(LivingEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci,
                               @Local(argsOnly = true) LocalRef<Text> localText) {
        if (!CustomHealth.isEnabled()
            || !(entity instanceof PlayerEntity player)) {
            return;
        }

        String indicator = CustomHealth.getColoredIndicatorString(player);

        if (indicator.isEmpty()) {
            return;
        }

        localText.set(Text.literal(indicator + " ").append(text));
    }
}
// v1.0