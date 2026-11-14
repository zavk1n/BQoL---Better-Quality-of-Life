package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterInteract;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class INTERACTSafeHarvest {

    @Shadow
    private MinecraftClient client;

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void bqol$preventImmatureCropBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (bqol$shouldPreventBreaking(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void bqol$preventImmatureCropBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (bqol$shouldPreventBreaking(pos)) {
            cir.setReturnValue(false);
        }
    }

    private boolean bqol$shouldPreventBreaking(BlockPos pos) {
        if (!BetterInteract.isSafeHarvestEnabled() || client.world == null) {
            return false;
        }

        BlockState state = client.world.getBlockState(pos);

        return state.getBlock() instanceof CropBlock crop && !crop.isMature(state);
    }
}