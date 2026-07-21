package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class SPRINTStairUpAllowFastMoving {

    @Inject(method = "autoJump", at = @At("HEAD"), cancellable = true)
    private void bqol$filterAutoJump(float dx, float dz, CallbackInfo ci) {
        if (!BetterSprint.isStairUpActive()) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;

        if (player.input == null || !player.isOnGround() || !player.isAlive()) {
            return;
        }

        if (!isAllowedBlock(player)) {
            ci.cancel();
        }
    }

    private boolean isAllowedBlock(ClientPlayerEntity player) {
        World world = player.getWorld();

        if (world == null) {
            return false;
        }

        double velocityX = player.getVelocity().x;
        double velocityZ = player.getVelocity().z;

        double length = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);

        if (length < 0.01D) {
            return false;
        }

        velocityX /= length;
        velocityZ /= length;

        BlockPos currentPos = BlockPos.ofFloored(
                player.getX(),
                player.getY() - 0.05D,
                player.getZ()
        );

        BlockPos targetPos = BlockPos.ofFloored(
                player.getX() + velocityX,
                player.getY(),
                player.getZ() + velocityZ
        );

        if (!isHeightAllowed(player, targetPos)) {
            return false;
        }

        return isAllowedBlockState(world, currentPos, targetPos);
    }

    private boolean isAllowedBlockState(World world, BlockPos currentPos, BlockPos targetPos) {
        BlockState currentState = world.getBlockState(currentPos);
        BlockState targetState = world.getBlockState(targetPos);

        boolean currentStep = isStepBlock(currentState);
        boolean targetStep = isStepBlock(targetState);

        if (!currentStep && !targetStep) {
            return false;
        }

        if (targetState.getBlock() instanceof StairsBlock) {
            return isValidStairsSide(currentPos, targetPos, targetState);
        }

        if (currentState.getBlock() instanceof StairsBlock) {
            return isValidStairsSide(currentPos, targetPos, currentState);
        }

        return true;
    }

    /// Список блоков "ступенек"
    private boolean isStepBlock(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof StairsBlock) {
            if (!state.getFluidState().isEmpty()) {
                return false;
            }

            return true;
        }

        if (block instanceof SlabBlock) {
            if (!state.getFluidState().isEmpty()) {
                return false;
            }

            return state.get(SlabBlock.TYPE) == SlabType.BOTTOM;
        }

        if (block instanceof TrapdoorBlock) {
            if (state.get(TrapdoorBlock.OPEN) || state.get(TrapdoorBlock.WATERLOGGED)) {
                return false;
            }

            return state.get(TrapdoorBlock.HALF) == net.minecraft.block.enums.BlockHalf.BOTTOM;
        }

        if (block instanceof SnowBlock) {
            int layers = state.get(SnowBlock.LAYERS);

            return layers >= 2;
        }

        return block instanceof CakeBlock;
    }

    /// Проврека нужной стороны у лестницы
    private boolean isValidStairsSide(BlockPos currentPos, BlockPos targetPos, BlockState stairState) {
        Direction facing = stairState.get(StairsBlock.FACING);

        int dx = targetPos.getX() - currentPos.getX();
        int dz = targetPos.getZ() - currentPos.getZ();

        return dx == facing.getOffsetX() && dz == facing.getOffsetZ();
    }

    /// Проверка корректной высоты подъема
    private boolean isHeightAllowed(ClientPlayerEntity player, BlockPos targetPos) {
        double dy = targetPos.getY() - player.getY();

        return dy <= 1.0 && dy >= -1.0;
    }
}
// v1.0