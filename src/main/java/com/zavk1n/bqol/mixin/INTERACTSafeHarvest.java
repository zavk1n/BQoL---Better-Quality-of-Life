package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterInteract;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class INTERACTSafeHarvest {

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void bqol$preventImmatureCropBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!BetterInteract.isSafeHarvestEnabled() || client.world == null) {
            return;
        }

        BlockState state = client.world.getBlockState(pos);

        if (bqol$isImmatureCrop(state)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void bqol$preventImmatureCropBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!BetterInteract.isSafeHarvestEnabled() || client.world == null) {
            return;
        }

        BlockState state = client.world.getBlockState(pos);

        if (bqol$isImmatureCrop(state)) {
            cir.setReturnValue(false);
        }
    }

    /// Проверка культуры
    private static boolean bqol$isImmatureCrop(BlockState state) {
        if (!(state.getBlock() instanceof CropBlock crop)) {
            return false;
        }

        return !crop.isMature(state);
    }
}
// v1.0