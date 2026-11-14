
package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTnt;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.TallSeagrassBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.KelpPlantBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.PressurePlateBlock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.entity.TntEntity;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(WorldRenderer.class)
public abstract class TNTExplosionAreaRenderer {

    @Unique
    private static final float BQOL_RED = 1.0F;

    @Unique
    private static final float BQOL_GREEN = 0.0F;

    @Unique
    private static final float BQOL_BLUE = 0.0F;

    @Unique
    private static final float BQOL_FILL_ALPHA = 0.08F;

    @Unique
    private static final double BQOL_SURFACE_OFFSET = 0.002D;

    @Unique
    private static final double BQOL_TNT_SEARCH_RADIUS = 100.0D;

    @Unique
    private static final long BQOL_CACHE_INTERVAL_TICKS = 5L;

    @Unique
    private static final long BQOL_EXPLOSION_HOLD_TICKS = 5L;

    @Unique
    private static final double BQOL_CAMERA_MOVE_THRESHOLD_SQUARED = 0.25D;

    @Unique
    private static final float BQOL_CAMERA_ROTATION_THRESHOLD = 3.0F;

    @Unique
    private static final int BQOL_MAX_NORMAL_TNT = 10000;

    @Unique
    private static final int BQOL_MAX_TNT_A = 50;

    @Unique
    private static final int BQOL_MAX_TNT_B = 30;

    @Unique
    private static final int BQOL_MAX_TNT_B2 = 20;

    @Unique
    private static final Direction[] BQOL_DIRECTIONS = Direction.values();


    @Unique
    private static boolean bqol$renderEventRegistered;

    @Unique
    private static World bqol$cachedWorld;

    @Unique
    private static long bqol$cachedWorldTime = Long.MIN_VALUE;

    @Unique
    private static long bqol$lastTntSeenTime = Long.MIN_VALUE;

    @Unique
    private static ChunkPos bqol$cachedChunk;

    @Unique
    private static List<BqolRenderFace> bqol$cachedFaces = Collections.emptyList();

    @Unique
    private static List<BqolRenderFace> bqol$visibleFaces = Collections.emptyList();

    @Unique
    private static Vec3d bqol$lastCameraPos;

    @Unique
    private static float bqol$lastCameraYaw;

    @Unique
    private static float bqol$lastCameraPitch;

    @Unique
    private static boolean bqol$cameraInitialized;

    @Unique
    private static boolean bqol$visibilityDirty = true;

    @Unique
    private static final BlockPos.Mutable BQOL_NEIGHBOR = new BlockPos.Mutable();

    @Unique
    private static final class BqolRenderFace {
        private final BlockPos pos;
        private final Direction direction;

        private BqolRenderFace(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bqol$registerRenderEvent(net.minecraft.client.util.ObjectAllocator allocator, net.minecraft.client.render.RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix, com.mojang.blaze3d.buffers.GpuBufferSlice fogBuffer, org.joml.Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (bqol$renderEventRegistered) {
            return;
        }

        bqol$renderEventRegistered = true;

        WorldRenderEvents.BEFORE_ENTITIES.register(TNTExplosionAreaRenderer::bqol$renderExplosionArea);
    }

    @Unique
    private static void bqol$renderExplosionArea(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!BetterTnt.isExplosionAreaEnabled()
            || mc.world == null
            || mc.player == null) {
            bqol$invalidateCache();
            return;
        }

        List<BqolRenderFace> faces = bqol$getCachedFaces(mc);

        if (faces.isEmpty()) {
            return;
        }

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        MatrixStack.Entry matrix = context.matrices().peek();

        VertexConsumer consumer = context.consumers().getBuffer(RenderLayers.debugFilledBox());

        for (BqolRenderFace face : faces) {
            bqol$renderFace(matrix, consumer, face, cameraX, cameraY, cameraZ);
        }
    }

    @Unique
    private static List<BqolRenderFace> bqol$getCachedFaces(MinecraftClient mc) {
        World world = mc.world;
        Camera camera = mc.gameRenderer.getCamera();

        long worldTime = world.getTime();

        ChunkPos playerChunk = mc.player.getChunkPos();

        boolean worldChanged = bqol$cachedWorld != world;

        if (worldChanged) {
            bqol$invalidateCache();
            bqol$cachedWorld = world;
        }

        boolean chunkChanged = bqol$cachedChunk == null || !bqol$cachedChunk.equals(playerChunk);
        boolean timeExpired = bqol$cachedWorldTime == Long.MIN_VALUE
            || worldTime < bqol$cachedWorldTime
            || worldTime - bqol$cachedWorldTime >= BQOL_CACHE_INTERVAL_TICKS;

        if (worldChanged || chunkChanged || timeExpired) {
            bqol$refreshAreaCache(mc, worldTime, playerChunk);
        }

        if (bqol$hasCameraChanged(camera)) {
            bqol$visibilityDirty = true;
        }

        if (bqol$visibilityDirty) {
            bqol$visibleFaces = bqol$filterVisibleFaces(mc, camera, bqol$cachedFaces);

            bqol$saveCameraState(camera);

            bqol$visibilityDirty = false;
        }

        return bqol$visibleFaces;
    }

    @Unique
    private static void bqol$refreshAreaCache(MinecraftClient mc, long worldTime, ChunkPos playerChunk) {
        List<TntEntity> nearbyTnts = bqol$getNearbyTnts(mc);

        if (!nearbyTnts.isEmpty()) {
            List<BqolRenderFace> newFaces =
                bqol$calculateExplosionFaces(mc, nearbyTnts);

            bqol$cachedFaces = newFaces;
            bqol$lastTntSeenTime = worldTime;
        } else {
            boolean holdExpired = bqol$lastTntSeenTime == Long.MIN_VALUE || worldTime - bqol$lastTntSeenTime > BQOL_EXPLOSION_HOLD_TICKS;

            if (holdExpired) {
                bqol$cachedFaces = Collections.emptyList();
            }
        }

        bqol$cachedWorldTime = worldTime;
        bqol$cachedChunk = playerChunk;

        bqol$visibilityDirty = true;
    }

    @Unique
    private static boolean bqol$hasCameraChanged(Camera camera) {
        if (!bqol$cameraInitialized || bqol$lastCameraPos == null) {
            return true;
        }

        Vec3d currentPos = camera.getCameraPos();

        if (bqol$lastCameraPos.squaredDistanceTo(currentPos)
            >= BQOL_CAMERA_MOVE_THRESHOLD_SQUARED) {
            return true;
        }

        return bqol$angleDifference(camera.getYaw(), bqol$lastCameraYaw) >= BQOL_CAMERA_ROTATION_THRESHOLD
            || Math.abs(camera.getPitch() - bqol$lastCameraPitch) >= BQOL_CAMERA_ROTATION_THRESHOLD;
    }

    @Unique
    private static float bqol$angleDifference(float first, float second) {
        float difference = (first - second) % 360.0F;

        if (difference > 180.0F) {
            difference -= 360.0F;
        } else if (difference < -180.0F) {
            difference += 360.0F;
        }

        return Math.abs(difference);
    }

    @Unique
    private static void bqol$saveCameraState(Camera camera) {
        bqol$lastCameraPos = camera.getCameraPos();
        bqol$lastCameraYaw = camera.getYaw();
        bqol$lastCameraPitch = camera.getPitch();
        bqol$cameraInitialized = true;
    }

    @Unique
    private static void bqol$invalidateCache() {
        bqol$cachedWorld = null;
        bqol$cachedWorldTime = Long.MIN_VALUE;
        bqol$lastTntSeenTime = Long.MIN_VALUE;
        bqol$cachedChunk = null;

        bqol$cachedFaces = Collections.emptyList();
        bqol$visibleFaces = Collections.emptyList();

        bqol$lastCameraPos = null;
        bqol$cameraInitialized = false;
        bqol$visibilityDirty = true;
    }

    @Unique
    private static List<TntEntity> bqol$getNearbyTnts(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return Collections.emptyList();
        }

        Box searchBox = mc.player.getBoundingBox().expand(BQOL_TNT_SEARCH_RADIUS);

        return mc.world.getEntitiesByClass(TntEntity.class, searchBox, entity -> entity.isAlive() && entity.getFuse() > 0);
    }

    @Unique
    private static List<BqolRenderFace> bqol$calculateExplosionFaces(MinecraftClient mc, List<TntEntity> nearbyTnts) {
        Map<BetterTnt.TntType, Integer> counts = new EnumMap<>(BetterTnt.TntType.class);

        Set<BlockPos> blocks = new HashSet<>();

        for (TntEntity tnt : nearbyTnts) {
            BetterTnt.TntType type = BetterTnt.getTntType(tnt);

            int count = counts.getOrDefault(type, 0);

            if (count >= bqol$getMaxTntForType(type)) {
                continue;
            }

            counts.put(type, count + 1);

            bqol$collectBlocksInExplosionRadius(blocks, mc.world, tnt);
        }

        if (blocks.isEmpty()) {
            return Collections.emptyList();
        }

        return bqol$buildRenderFaces(blocks);
    }

    @Unique
    private static int bqol$getMaxTntForType(BetterTnt.TntType type) {
        return switch (type) {
            case A -> BQOL_MAX_TNT_A;
            case B -> BQOL_MAX_TNT_B;
            case B2 -> BQOL_MAX_TNT_B2;
            case NORMAL, C4, ICE_WAVE, EXPLOSIVE_WAVE -> BQOL_MAX_NORMAL_TNT;
        };
    }

    @Unique
    private static void bqol$collectBlocksInExplosionRadius(Set<BlockPos> blocks, World world, TntEntity tnt) {
        if (world == null || tnt == null) {
            return;
        }

        double cx = tnt.getX();
        double cy = tnt.getY();
        double cz = tnt.getZ();

        double radius = BetterTnt.getExplosionBlockRadius(tnt);

        if (!Double.isFinite(radius) || radius <= 0.0D) {
            return;
        }

        double radiusSquared = radius * radius;

        int minX = (int) Math.floor(cx - radius);
        int maxX = (int) Math.floor(cx + radius);

        int minY = Math.max(world.getBottomY(), (int) Math.floor(cy - radius));
        int maxY = Math.min(world.getTopYInclusive(), (int) Math.floor(cy + radius));

        int minZ = (int) Math.floor(cz - radius);
        int maxZ = (int) Math.floor(cz + radius);

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            double dx = x + 0.5D - cx;
            double dxSquared = dx * dx;

            if (dxSquared > radiusSquared) {
                continue;
            }

            for (int y = minY; y <= maxY; y++) {
                double dy = y + 0.5D - cy;
                double horizontalSquared = dxSquared + dy * dy;

                if (horizontalSquared > radiusSquared) {
                    continue;
                }

                for (int z = minZ; z <= maxZ; z++) {
                    double dz = z + 0.5D - cz;

                    if (horizontalSquared + dz * dz > radiusSquared) {
                        continue;
                    }

                    mutable.set(x, y, z);

                    if (blocks.contains(mutable)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(mutable);

                    if (state.isAir()
                        || !state.getFluidState().isEmpty()
                        || bqol$isIgnoredBlock(state)) {
                        continue;
                    }

                    blocks.add(mutable.toImmutable());
                }
            }
        }
    }

    @Unique
    private static List<BqolRenderFace> bqol$buildRenderFaces(Set<BlockPos> blocks) {
        List<BqolRenderFace> faces = new ArrayList<>();

        for (BlockPos pos : blocks) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (Direction direction : BQOL_DIRECTIONS) {
                BQOL_NEIGHBOR.set(
                    x + direction.getOffsetX(),
                    y + direction.getOffsetY(),
                    z + direction.getOffsetZ()
                );

                if (blocks.contains(BQOL_NEIGHBOR)) {
                    continue;
                }

                faces.add(new BqolRenderFace(pos, direction));
            }
        }

        return faces.isEmpty() ? Collections.emptyList() : faces;
    }

    @Unique
    private static List<BqolRenderFace> bqol$filterVisibleFaces(MinecraftClient mc, Camera camera, List<BqolRenderFace> faces) {
        if (faces.isEmpty() || mc.world == null || mc.player == null) {
            return Collections.emptyList();
        }

        World world = mc.world;

        Vec3d cameraPos = camera.getCameraPos();

        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        double yaw = Math.toRadians(camera.getYaw());
        double pitch = Math.toRadians(camera.getPitch());

        double cosPitch = Math.cos(pitch);

        double forwardX = -Math.sin(yaw) * cosPitch;
        double forwardY = -Math.sin(pitch);
        double forwardZ = Math.cos(yaw) * cosPitch;

        List<BqolRenderFace> visible = new ArrayList<>();

        for (BqolRenderFace face : faces) {
            BlockPos pos = face.pos;
            Direction direction = face.direction;

            int nx = direction.getOffsetX();
            int ny = direction.getOffsetY();
            int nz = direction.getOffsetZ();

            double faceX = pos.getX() + 0.5D + nx * 0.499D;
            double faceY = pos.getY() + 0.5D + ny * 0.499D;
            double faceZ = pos.getZ() + 0.5D + nz * 0.499D;

            double dx = faceX - cameraX;
            double dy = faceY - cameraY;
            double dz = faceZ - cameraZ;

            double cameraSide = -(dx * nx + dy * ny + dz * nz);

            if (cameraSide <= 0.001D) {
                continue;
            }

            double viewDot = dx * forwardX + dy * forwardY + dz * forwardZ;

            if (viewDot <= 0.0D) {
                continue;
            }

            Vec3d target = new Vec3d(faceX, faceY, faceZ);

            BlockHitResult hit = world.raycast(new RaycastContext(cameraPos, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

            if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(pos)) {
                continue;
            }

            visible.add(face);
        }

        return visible.isEmpty() ? Collections.emptyList() : visible;
    }

    @Unique
    private static void bqol$renderFace(MatrixStack.Entry matrix, VertexConsumer consumer, BqolRenderFace face, double cameraX, double cameraY, double cameraZ) {
        BlockPos pos = face.pos;

        double x1 = pos.getX() - cameraX;
        double y1 = pos.getY() - cameraY;
        double z1 = pos.getZ() - cameraZ;

        double x2 = x1 + 1.0D;
        double y2 = y1 + 1.0D;
        double z2 = z1 + 1.0D;

        double offset = BQOL_SURFACE_OFFSET;

        switch (face.direction) {
            case UP -> bqol$quad(
                matrix, consumer,
                x1, y2 + offset, z1,
                x1, y2 + offset, z2,
                x2, y2 + offset, z2,
                x2, y2 + offset, z1
            );

            case DOWN -> bqol$quad(
                matrix, consumer,
                x1, y1 - offset, z2,
                x1, y1 - offset, z1,
                x2, y1 - offset, z1,
                x2, y1 - offset, z2
            );

            case NORTH -> bqol$quad(
                matrix, consumer,
                x2, y1, z1 - offset,
                x1, y1, z1 - offset,
                x1, y2, z1 - offset,
                x2, y2, z1 - offset
            );

            case SOUTH -> bqol$quad(
                matrix, consumer,
                x1, y1, z2 + offset,
                x2, y1, z2 + offset,
                x2, y2, z2 + offset,
                x1, y2, z2 + offset
            );

            case WEST -> bqol$quad(
                matrix, consumer,
                x1 - offset, y1, z1,
                x1 - offset, y1, z2,
                x1 - offset, y2, z2,
                x1 - offset, y2, z1
            );

            case EAST -> bqol$quad(
                matrix, consumer,
                x2 + offset, y1, z2,
                x2 + offset, y1, z1,
                x2 + offset, y2, z1,
                x2 + offset, y2, z2
            );
        }
    }

    @Unique
    private static void bqol$quad(MatrixStack.Entry matrix, VertexConsumer consumer,
        double x1, double y1, double z1,
        double x2, double y2, double z2,
        double x3, double y3, double z3,
        double x4, double y4, double z4
    ) {
        bqol$vertex(matrix, consumer, x1, y1, z1);
        bqol$vertex(matrix, consumer, x2, y2, z2);
        bqol$vertex(matrix, consumer, x3, y3, z3);
        bqol$vertex(matrix, consumer, x4, y4, z4);
    }

    @Unique
    private static void bqol$vertex(MatrixStack.Entry matrix, VertexConsumer consumer, double x, double y, double z) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
            .color(BQOL_RED, BQOL_GREEN, BQOL_BLUE, BQOL_FILL_ALPHA);
    }


    @Unique
    private static boolean bqol$isIgnoredBlock(BlockState state) {
        Block block = state.getBlock();

        return block instanceof PlantBlock
            || block instanceof TallPlantBlock
            || block instanceof FlowerBlock
            || block instanceof SeagrassBlock
            || block instanceof TallSeagrassBlock
            || block instanceof KelpBlock
            || block instanceof KelpPlantBlock
            || block instanceof LadderBlock
            || block instanceof CarpetBlock
            || block instanceof PressurePlateBlock
            || block == Blocks.VINE
            || block == Blocks.IRON_BARS
            || block == Blocks.COCOA
            || block == Blocks.SUGAR_CANE
            || block == Blocks.BAMBOO
            || block == Blocks.BAMBOO_SAPLING;
    }
}