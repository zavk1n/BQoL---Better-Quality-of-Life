package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterParticles;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlock.class)
public abstract class PARTICLESShulkerSpawnBreakingParticles {

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void bqol$spawnBreakingParticles(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (world instanceof ClientWorld) {
            BetterParticles.getInstance().onShulkerBroken(pos, state);
        }
    }
}