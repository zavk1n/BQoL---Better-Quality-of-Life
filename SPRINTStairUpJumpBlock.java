package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class SPRINTStairUpJumpBlock {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void bqol$blockJumpOnStepBlocks(CallbackInfo ci) {
        if (!BetterSprint.isStairUpActive()) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;

        World world = player.getWorld();

        if (player.input == null
                || !player.input.jumping
                || !player.isOnGround()
                || !isMoving(player)) {
            return;
        }

        boolean currentStep = isStepBlockBelowMe(world, player);
        boolean nextStep = isStepBlockAhead(world, player);

        if (!currentStep && !nextStep) {
            return;
        }

        player.input.jumping = false;
    }

    private static boolean isMoving(ClientPlayerEntity player) {
        if (player == null || player.input == null) {
            return false;
        }

        boolean input = player.input.movementForward != 0.0F || player.input.movementSideways != 0.0F;

        var v = player.getVelocity();

        double speedSq = v.x * v.x + v.z * v.z;

        boolean velocity = speedSq > 0.0025;

        return input || velocity;
    }

    private boolean isStepBlockBelowMe(World world, ClientPlayerEntity player) {
        BlockPos pos = BlockPos.ofFloored(
                player.getX(),
                player.getY() - 0.05D,
                player.getZ()
        );

        return isStepBlock(world.getBlockState(pos));
    }

    private boolean isStepBlockAhead(World world, ClientPlayerEntity player) {
        if (player.input == null) {
            return false;
        }

        double forward = player.input.movementForward;
        double sideways = player.input.movementSideways;

        double length = Math.sqrt(forward * forward + sideways * sideways);

        if (length < 0.01D) {
            return false;
        }

        forward /= length;
        sideways /= length;

        float yaw = player.getYaw();

        double rad = Math.toRadians(yaw);

        double dirX = forward * -Math.sin(rad) + sideways * Math.cos(rad);
        double dirZ = forward *  Math.cos(rad) + sideways * Math.sin(rad);

        BlockPos target = BlockPos.ofFloored(
                player.getX() + dirX,
                player.getY(),
                player.getZ() + dirZ
        );

        return isStepBlock(world.getBlockState(target));
    }

    /// Список блоков "ступенек"
    private boolean isStepBlock(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof StairsBlock) {
            return true;
        }

        if (block instanceof SlabBlock) {
            return state.get(SlabBlock.TYPE) == SlabType.BOTTOM;
        }

        if (block instanceof TrapdoorBlock) {
            return !state.get(TrapdoorBlock.OPEN) && !state.get(TrapdoorBlock.WATERLOGGED)
                    && state.get(TrapdoorBlock.HALF)
                    == net.minecraft.block.enums.BlockHalf.BOTTOM;
        }

        if (block instanceof SnowBlock) {
            int layers = state.get(SnowBlock.LAYERS);

            return layers >= 2;
        }

        return block instanceof CakeBlock;
    }
}
// v1.0