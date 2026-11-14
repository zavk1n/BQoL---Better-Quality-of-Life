package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterInteract;

import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(GameRenderer.class)
public abstract class INTERACTClickThrough {

    private static final double RAYCAST_EPSILON = 0.05D;
    private static final int MAX_RAYCAST_ITERATIONS = 128;

    private static final Set<Block> PASS_THROUGH_BLOCKS = Set.of(
        Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN,
        Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN,
        Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN,
        Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN,
        Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN,
        Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN,
        Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN,
        Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN,
        Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN,
        Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN,
        Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN,
        Blocks.SHORT_GRASS, Blocks.SHORT_DRY_GRASS,
        Blocks.TALL_GRASS, Blocks.TALL_DRY_GRASS,
        Blocks.FERN,
        Blocks.LARGE_FERN
    );

    private static final Set<Block> CONTAINER_BLOCKS = Set.of(Blocks.CHEST,
        Blocks.COPPER_CHEST, Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST,
        Blocks.WAXED_WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.BARREL);

    @Shadow
    private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void bqol$clickThrough(float tickDelta, CallbackInfo ci) {
        if (!BetterInteract.isClickThroughEnabled()) {
            return;
        }

        if (client.world == null || client.player == null) {
            return;
        }

        Entity cameraEntity = client.getCameraEntity();

        if (cameraEntity == null) {
            return;
        }

        HitResult currentTarget = client.crosshairTarget;

        Vec3d firstHit;

        if (currentTarget instanceof BlockHitResult blockHit) {
            if (!bqol$isPassThroughBlock(
                client.world.getBlockState(blockHit.getBlockPos()))) {
                return;
            }

            firstHit = blockHit.getPos();

        } else if (currentTarget instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof ItemFrameEntity) {
            firstHit = entityHit.getPos();
        } else {
            return;
        }

        BlockHitResult nextTarget = bqol$findNextContainer(client, cameraEntity, firstHit, tickDelta);

        if (nextTarget != null) {
            client.crosshairTarget = nextTarget;
        }
    }

    private static BlockHitResult bqol$findNextContainer(MinecraftClient client, Entity cameraEntity, Vec3d firstHit, float tickDelta) {
        if (client.world == null || client.player == null) {
            return null;
        }

        double range = client.player.getBlockInteractionRange();
        double maxDistanceSquared = range * range;

        Vec3d cameraPos = cameraEntity.getCameraPosVec(tickDelta);
        Vec3d direction = cameraEntity.getRotationVec(tickDelta).normalize();

        Vec3d end = cameraPos.add(direction.multiply(range));
        Vec3d start = firstHit.add(direction.multiply(RAYCAST_EPSILON));

        for (int i = 0; i < MAX_RAYCAST_ITERATIONS; i++) {
            if (start.squaredDistanceTo(cameraPos) >= maxDistanceSquared) {
                return null;
            }

            BlockHitResult hit = client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, cameraEntity));

            if (hit.getType() != HitResult.Type.BLOCK) {
                return null;
            }

            BlockState state = client.world.getBlockState(hit.getBlockPos());

            if (bqol$isContainer(state)) {
                return hit;
            }

            if (!bqol$isPassThroughBlock(state)) {
                return null;
            }

            start = hit.getPos().add(direction.multiply(RAYCAST_EPSILON));
        }

        return null;
    }

    private static boolean bqol$isContainer(BlockState state) {
        Block block = state.getBlock();

        return CONTAINER_BLOCKS.contains(block)
            || block instanceof ShulkerBoxBlock
            || block instanceof ChestBlock;
    }

    private static boolean bqol$isPassThroughBlock(BlockState state) {
        return PASS_THROUGH_BLOCKS.contains(state.getBlock());
    }
}