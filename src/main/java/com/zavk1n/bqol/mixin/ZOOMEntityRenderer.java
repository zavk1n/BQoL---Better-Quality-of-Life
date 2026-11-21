package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterZoom;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class ZOOMEntityRenderer {

    @ModifyVariable(method = "setRenderDistanceMultiplier(D)V", at = @At("HEAD"), argsOnly = true)
    private static double bqol$modifyRenderDistance(double renderDistWeight) {
        if (!BetterZoom.canSeeDistantEntities()) {
            return renderDistWeight;
        }

        return renderDistWeight * Math.max(1.0, BetterZoom.getZoomDivisor());
    }
}