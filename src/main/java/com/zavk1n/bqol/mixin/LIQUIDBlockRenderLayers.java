package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterLiquid;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayers;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderLayers.class)
public abstract class LIQUIDBlockRenderLayers {

    @Inject(method = "getFluidLayer(Lnet/minecraft/fluid/FluidState;)Lnet/minecraft/client/render/BlockRenderLayer;", at = @At("RETURN"), cancellable = true)
    private static void bqol$makeLavaTranslucent(FluidState state, CallbackInfoReturnable<BlockRenderLayer> cir) {
        if (state.isIn(FluidTags.LAVA) && BetterLiquid.isLavaTransparencyEnabled()) {
            cir.setReturnValue(BlockRenderLayer.TRANSLUCENT);
        }
    }
}