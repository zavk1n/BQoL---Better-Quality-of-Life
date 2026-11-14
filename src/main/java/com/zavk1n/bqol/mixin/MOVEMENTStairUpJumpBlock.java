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
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MOVEMENTStairUpJumpBlock {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void bqol$blockJumpOnStepBlocks(CallbackInfo ci) {
        if (!BetterMovement.isStairUpActive()) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (!player.isOnGround() || !player.isAlive() || player.input == null) {
            return;
        }

        PlayerInput input = player.input.playerInput;

        if (!input.jump() || !bqol$isMoving(player, input)) {
            return;
        }

        World world = player.getEntityWorld();

        boolean currentStep = bqol$isStepBlockBelowMe(world, player);
        boolean nextStep = bqol$isStepBlockAhead(world, player, input);

        if (!currentStep && !nextStep) {
            return;
        }

        player.input.playerInput = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), false, input.sneak(), input.sprint());
    }

    @Unique
    private boolean bqol$isMoving(ClientPlayerEntity player, PlayerInput input) {
        if (input.forward() || input.backward() || input.left() || input.right()) {
            return true;
        }

        Vec3d velocity = player.getVelocity();

        double speedSquared = velocity.x * velocity.x + velocity.z * velocity.z;

        return speedSquared > 0.0025D;
    }

    @Unique
    private boolean bqol$isStepBlockBelowMe(World world, ClientPlayerEntity player) {
        BlockPos pos = BlockPos.ofFloored(player.getX(), player.getY() - 0.05D, player.getZ());

        return bqol$isStepBlock(world.getBlockState(pos));
    }

    @Unique
    private boolean bqol$isStepBlockAhead(World world, ClientPlayerEntity player, PlayerInput input) {
        double forward = (input.forward() ? 1.0D : 0.0D) - (input.backward() ? 1.0D : 0.0D);
        double sideways = (input.left() ? 1.0D : 0.0D) - (input.right() ? 1.0D : 0.0D);

        double lengthSquared = forward * forward + sideways * sideways;

        if (lengthSquared < 0.0001D) {
            return false;
        }

        double inverseLength = 1.0D / Math.sqrt(lengthSquared);

        forward *= inverseLength;
        sideways *= inverseLength;

        double radians = Math.toRadians(player.getYaw());

        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double dirX = forward * -sin + sideways * cos;
        double dirZ = forward * cos + sideways * sin;

        BlockPos target = BlockPos.ofFloored(player.getX() + dirX, player.getY(), player.getZ() + dirZ);

        return bqol$isStepBlock(world.getBlockState(target));
    }

    @Unique
    private boolean bqol$isStepBlock(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof StairsBlock) {
            return true;
        }

        if (block instanceof SlabBlock) {
            return state.get(SlabBlock.TYPE) == SlabType.BOTTOM;
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
}