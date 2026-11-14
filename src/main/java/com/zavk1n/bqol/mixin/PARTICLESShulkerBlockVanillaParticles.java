package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterParticles;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class PARTICLESShulkerBlockVanillaParticles {

    @Inject(method = "spawnBlockBreakingParticle", at = @At("HEAD"), cancellable = true)
    private void bqol$cancelHitParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (BetterParticles.shouldCancelShulkersVanillaBreaking() && ((ClientWorld) (Object) this).getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
            ci.cancel();
        }
    }
}