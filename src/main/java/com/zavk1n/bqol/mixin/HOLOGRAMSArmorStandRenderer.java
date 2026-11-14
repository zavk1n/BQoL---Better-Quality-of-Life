package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.accessors.ACCESSORHologramRenderState;
import com.zavk1n.bqol.features.BetterHolograms;

import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.decoration.ArmorStandEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntityRenderer.class)
public abstract class HOLOGRAMSArmorStandRenderer {

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void bqol$updateRenderState(ArmorStandEntity armorStand, ArmorStandEntityRenderState state, float tickDelta, CallbackInfo ci) {
        ACCESSORHologramRenderState hologramState = (ACCESSORHologramRenderState) state;

        hologramState.bqol$setHologram(BetterHolograms.isVisibleArmorStandEnabled() && BetterHolograms.isHologram(armorStand));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$beforeRender(ArmorStandEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        BetterHolograms.end();

        if (((ACCESSORHologramRenderState) state).bqol$isHologram() && BetterHolograms.isVisibleArmorStandEnabled()) {
            BetterHolograms.begin();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bqol$afterRender(ArmorStandEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        BetterHolograms.end();
    }
}