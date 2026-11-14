package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTnt;

import net.minecraft.entity.TntEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntityRenderer.class)
public abstract class TNTEntityRenderer {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/TntEntity;Lnet/minecraft/client/render/entity/state/TntEntityRenderState;F)V", at = @At("TAIL"), require = 1)
    private void bqol$prepareTimer(TntEntity entity, TntEntityRenderState state, float tickProgress, CallbackInfo ci) {
        BetterTnt.prepareRenderTimer(entity, state);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/TntEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("TAIL"), require = 1)
    private void bqol$renderTimer(TntEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        BetterTnt.renderTimer(matrices, state, queue, cameraState);
    }
}