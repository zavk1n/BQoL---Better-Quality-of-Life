package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterMovement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MOVEMENTStairUpAllowFastMoving {

    @Inject(method = "autoJump", at = @At("HEAD"), cancellable = true)
    private void bqol$filterAutoJump(float dx, float dz, CallbackInfo ci) {
        if (!BetterMovement.isStairUpActive()) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (player.input == null || !player.isOnGround() || !player.isAlive()) {
            return;
        }

        if (!bqol$isAllowedBlock(player)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean bqol$isAllowedBlock(ClientPlayerEntity player) {
        World world = player.getEntityWorld();

        Vec3d velocity = player.getVelocity();

        double velocityX = velocity.x;
        double velocityZ = velocity.z;

        double lengthSquared = velocityX * velocityX + velocityZ * velocityZ;

        if (lengthSquared < 0.0001D) {
            return false;
        }

        double inverseLength = 1.0D / Math.sqrt(lengthSquared);

        velocityX *= inverseLength;
        velocityZ *= inverseLength;

        BlockPos currentPos = BlockPos.ofFloored(player.getX(), player.getY() - 0.05D, player.getZ());
        BlockPos targetPos = BlockPos.ofFloored(player.getX() + velocityX, player.getY(), player.getZ() + velocityZ);

        BlockState currentState = world.getBlockState(currentPos);
        BlockState targetState = world.getBlockState(targetPos);

        return bqol$isAllowedBlockState(currentPos, targetPos, currentState, targetState);
    }

    @Unique
    private boolean bqol$isAllowedBlockState(BlockPos currentPos, BlockPos targetPos, BlockState currentState, BlockState targetState) {
        boolean currentStep = bqol$isStepBlock(currentState);
        boolean targetStep = bqol$isStepBlock(targetState);

        if (!currentStep && !targetStep) {
            return false;
        }

        if (targetState.getBlock() instanceof StairsBlock) {
            return bqol$isValidStairsSide(currentPos, targetPos, targetState);
        }

        if (currentState.getBlock() instanceof StairsBlock) {
            return bqol$isValidStairsSide(currentPos, targetPos, currentState);
        }

        return true;
    }

    @Unique
    private boolean bqol$isStepBlock(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof StairsBlock) {
            return state.getFluidState().isEmpty();
        }

        if (block instanceof SlabBlock) {
            return state.getFluidState().isEmpty()
                && state.get(SlabBlock.TYPE) == SlabType.BOTTOM;
        }

        if (block instanceof TrapdoorBlock) {
            return !state.get(TrapdoorBlock.OPEN)
                && !state.get(TrapdoorBlock.WATERLOGGED)
                && state.get(TrapdoorBlock.HALF) == BlockHalf.BOTTOM;
        }

        if (block instanceof SnowBlock) {
            return state.get(SnowBlock.LAYERS) >= 2;
        }

        return block instanceof CakeBlock;
    }

    @Unique
    private boolean bqol$isValidStairsSide(BlockPos currentPos, BlockPos targetPos, BlockState stairState) {
        Direction facing = stairState.get(StairsBlock.FACING);

        int dx = targetPos.getX() - currentPos.getX();
        int dz = targetPos.getZ() - currentPos.getZ();

        return dx == facing.getOffsetX() && dz == facing.getOffsetZ();
    }
}