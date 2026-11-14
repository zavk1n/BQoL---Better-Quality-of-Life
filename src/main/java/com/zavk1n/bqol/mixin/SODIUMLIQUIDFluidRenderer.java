package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterLiquid;

import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer", remap = false)
public abstract class SODIUMLIQUIDFluidRenderer {

    @Shadow
    @Final
    private int[] quadColors;

    @Inject(method = "updateQuad", at = @At("TAIL"), remap = false)
    private void bqol$applyLiquidColors(@Coerce Object quad, @Coerce Object level, BlockPos pos, @Coerce Object lighter, Direction direction, @Coerce Object facing, float brightness, @Coerce Object colorProvider, FluidState fluidState, CallbackInfo ci) {
        if (fluidState == null || quadColors == null) {
            return;
        }

        boolean water = fluidState.isIn(FluidTags.WATER);
        boolean lava = fluidState.isIn(FluidTags.LAVA);

        if (!water && !lava) {
            return;
        }

        boolean customColor = water ? BetterLiquid.isWaterColorEnabled() : BetterLiquid.isLavaColorEnabled();

        if (!customColor) {
            return;
        }

        for (int i = 0; i < quadColors.length; i++) {
            quadColors[i] = 0xFFFFFFFF;
        }
    }
}
