package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterLiquid;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FluidRenderer.class)
public abstract class LIQUIDVanillaFluidRenderer {

    @ModifyArgs(
        method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/FluidRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFI)V"
        )
    )
    private void bqol$removeBiomeTint(Args args, BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        boolean customColor = (fluidState.isIn(FluidTags.WATER) && BetterLiquid.isWaterColorEnabled()) || (fluidState.isIn(FluidTags.LAVA) && BetterLiquid.isLavaColorEnabled());

        if (!customColor) {
            return;
        }

        args.set(4, 1.0F);
        args.set(5, 1.0F);
        args.set(6, 1.0F);
    }
}