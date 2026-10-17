package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterParticles;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class PARTICLESShulkerBlockVanillaParticles {

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void bqol$cancelBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!BetterParticles.shouldCancelShulkersVanillaBreaking()) {
            return;
        }

        if (state.getBlock() instanceof ShulkerBoxBlock) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void bqol$cancelHitParticles(BlockPos pos, net.minecraft.util.math.Direction direction, CallbackInfo ci) {
        if (!BetterParticles.shouldCancelShulkersVanillaBreaking()) {
            return;
        }

        ClientWorld world = BetterParticles.getClientWorld();

        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
            ci.cancel();
        }
    }
}