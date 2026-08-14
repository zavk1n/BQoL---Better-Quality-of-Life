package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterInteract;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
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

@Mixin(GameRenderer.class)
public abstract class INTERACTClickThrough {

    @Shadow
    private MinecraftClient client;

    @Inject(method = "updateTargetedEntity", at = @At("TAIL"))
    private void bqol$clickThrough(float tickDelta, CallbackInfo ci) {
        if (!BetterInteract.isClickThroughEnabled()) {
            return;
        }

        if (client.world == null
            || client.cameraEntity == null
            || client.interactionManager == null) {
            return;
        }

        HitResult currentTarget = client.crosshairTarget;

        if (currentTarget instanceof BlockHitResult blockHit) {
            BlockState state = client.world.getBlockState(blockHit.getBlockPos());

            if (!bqol$isPassThroughBlock(state)) {
                return;
            }

            BlockHitResult nextTarget = bqol$findNextContainer(
                client.cameraEntity,
                blockHit.getPos(),
                tickDelta
            );

            if (nextTarget != null) {
                client.crosshairTarget = nextTarget;
            }

            return;
        }

        if (currentTarget instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();

            if (!(entity instanceof ItemFrameEntity) && !(entity instanceof GlowItemFrameEntity)) {
                return;
            }

            BlockHitResult nextTarget = bqol$findNextContainer(
                client.cameraEntity,
                entityHit.getPos(),
                tickDelta
            );

            if (nextTarget != null) {
                client.crosshairTarget = nextTarget;
            }
        }
    }

    /// Ищет следующий блок, но разрешает выбрать только контейнеры.
    private static BlockHitResult bqol$findNextContainer(Entity cameraEntity, Vec3d firstHit, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.interactionManager == null) {
            return null;
        }

        double range = client.interactionManager.getReachDistance();

        Vec3d cameraPos = cameraEntity.getCameraPosVec(tickDelta);
        Vec3d direction = cameraEntity.getRotationVec(tickDelta).normalize();

        Vec3d end = cameraPos.add(
            direction.x * range,
            direction.y * range,
            direction.z * range
        );

        Vec3d start = firstHit.add(
            direction.x * 0.05D,
            direction.y * 0.05D,
            direction.z * 0.05D
        );

        for (int i = 0; i < 128; i++) {
            if (start.squaredDistanceTo(cameraPos) >= end.squaredDistanceTo(cameraPos)) {
                return null;
            }

            BlockHitResult hit = client.world.raycast(
                new RaycastContext(
                    start,
                    end,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    cameraEntity
                )
            );

            if (hit.getType() != HitResult.Type.BLOCK) {
                return null;
            }

            BlockState state = client.world.getBlockState(hit.getBlockPos());

            if (bqol$isContainer(state)) {
                return hit;
            }

            if (bqol$isPassThroughBlock(state)) {
                Vec3d hitPos = hit.getPos();

                start = hitPos.add(
                    direction.x * 0.05D,
                    direction.y * 0.05D,
                    direction.z * 0.05D
                );

                continue;
            }

            return null;
        }

        return null;
    }

    /// Только контейнеры, которые разрешено открывать
    private static boolean bqol$isContainer(BlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST
                || block == Blocks.ENDER_CHEST
                || block == Blocks.BARREL) {
            return true;
        }

        return block instanceof ShulkerBoxBlock;
    }

    /// Блоки, через которые разрешено пройти лучу.
    private static boolean bqol$isPassThroughBlock(BlockState state) {
        return state.isOf(Blocks.OAK_SIGN)
            || state.isOf(Blocks.OAK_WALL_SIGN)

            || state.isOf(Blocks.SPRUCE_SIGN)
            || state.isOf(Blocks.SPRUCE_WALL_SIGN)

            || state.isOf(Blocks.BIRCH_SIGN)
            || state.isOf(Blocks.BIRCH_WALL_SIGN)

            || state.isOf(Blocks.JUNGLE_SIGN)
            || state.isOf(Blocks.JUNGLE_WALL_SIGN)

            || state.isOf(Blocks.ACACIA_SIGN)
            || state.isOf(Blocks.ACACIA_WALL_SIGN)

            || state.isOf(Blocks.DARK_OAK_SIGN)
            || state.isOf(Blocks.DARK_OAK_WALL_SIGN)

            || state.isOf(Blocks.CRIMSON_SIGN)
            || state.isOf(Blocks.CRIMSON_WALL_SIGN)

            || state.isOf(Blocks.WARPED_SIGN)
            || state.isOf(Blocks.WARPED_WALL_SIGN)

            || state.isOf(Blocks.MANGROVE_SIGN)
            || state.isOf(Blocks.MANGROVE_WALL_SIGN)

            || state.isOf(Blocks.BAMBOO_SIGN)
            || state.isOf(Blocks.BAMBOO_WALL_SIGN)

            || state.isOf(Blocks.CHERRY_SIGN)
            || state.isOf(Blocks.CHERRY_WALL_SIGN)

            || state.isOf(Blocks.GRASS)
            || state.isOf(Blocks.TALL_GRASS)
            || state.isOf(Blocks.FERN)
            || state.isOf(Blocks.LARGE_FERN);
    }
}
// v1.0