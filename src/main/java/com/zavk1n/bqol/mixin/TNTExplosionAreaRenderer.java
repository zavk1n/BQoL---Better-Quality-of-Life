package com.zavk1n.bqol.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zavk1n.bqol.features.BetterTnt;

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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private static final int BQOL_MAX_NORMAL_TNT = 100;

    @Unique
    private static final int BQOL_MAX_TNT_A = 20;

    @Unique
    private static final int BQOL_MAX_TNT_B = 8;

    @Unique
    private static final int BQOL_MAX_TNT_B2 = 8;

    @Unique
    private static final int BQOL_CHUNK_CHECK_RADIUS = 1;

    @Unique
    private static World bqol$cachedWorld;

    @Unique
    private static long bqol$cachedWorldTime = Long.MIN_VALUE;

    @Unique
    private static ChunkPos bqol$cachedChunk;

    @Unique
    private static Set<BlockPos> bqol$cachedBlocks = Collections.emptySet();

    @Unique
    private static final BlockPos.Mutable BQOL_NEIGHBOR =
        new BlockPos.Mutable();

    @Inject(method = "render", at = @At("TAIL"))
    private void bqol$renderExplosionArea(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!BetterTnt.isExplosionAreaEnabled()
            || mc.world == null
            || mc.player == null) {
            return;
        }

        Set<BlockPos> blocks = bqol$getCachedBlocks(mc);

        if (blocks.isEmpty()) {
            return;
        }

        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        matrices.push();

        try {
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            for (BlockPos pos : blocks) {
                bqol$renderBlockFaces(matrix, buffer, pos, blocks, cameraX, cameraY, cameraZ);
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());

        } finally {
            matrices.pop();

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    @Unique
    private static Set<BlockPos> bqol$getCachedBlocks(MinecraftClient mc) {
        World world = mc.world;

        if (world == null || mc.player == null) {
            bqol$invalidateCache();
            return Collections.emptySet();
        }

        long worldTime = world.getTime();
        ChunkPos playerChunk = mc.player.getChunkPos();

        boolean cacheInvalid = bqol$cachedWorld != world
            || bqol$cachedWorldTime != worldTime
            || bqol$cachedChunk == null
            || !bqol$cachedChunk.equals(playerChunk);

        if (!cacheInvalid) {
            return bqol$cachedBlocks;
        }

        bqol$cachedWorld = world;
        bqol$cachedWorldTime = worldTime;
        bqol$cachedChunk = playerChunk;

        bqol$cachedBlocks = bqol$calculateExplosionBlocks(mc);

        return bqol$cachedBlocks;
    }

    @Unique
    private static void bqol$invalidateCache() {
        bqol$cachedWorld = null;
        bqol$cachedWorldTime = Long.MIN_VALUE;
        bqol$cachedChunk = null;
        bqol$cachedBlocks = Collections.emptySet();
    }

    @Unique
    private static Set<BlockPos> bqol$calculateExplosionBlocks(MinecraftClient mc) {
        List<TntEntity> nearbyTnts = bqol$getNearbyTnts(mc);

        if (nearbyTnts.isEmpty()) {
            return Collections.emptySet();
        }

        Map<BetterTnt.TntType, Integer> counts = new EnumMap<>(BetterTnt.TntType.class);

        Set<BlockPos> blocks = new HashSet<>();

        for (TntEntity tnt : nearbyTnts) {
            BetterTnt.TntType type = BetterTnt.getTntType(tnt);

            if (type == null) {
                continue;
            }

            int count = counts.getOrDefault(type, 0);

            if (count >= bqol$getMaxTntForType(type)) {
                continue;
            }

            counts.put(type, count + 1);

            bqol$collectBlocksInExplosionRadius(blocks, tnt);
        }

        return blocks.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(blocks);
    }

    @Unique
    private static List<TntEntity> bqol$getNearbyTnts(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return Collections.emptyList();
        }

        ChunkPos chunk = mc.player.getChunkPos();

        int minX = (chunk.x - BQOL_CHUNK_CHECK_RADIUS) << 4;
        int minZ = (chunk.z - BQOL_CHUNK_CHECK_RADIUS) << 4;

        int maxX = (chunk.x + BQOL_CHUNK_CHECK_RADIUS + 1) << 4;
        int maxZ = (chunk.z + BQOL_CHUNK_CHECK_RADIUS + 1) << 4;

        Box searchBox = new Box(minX, mc.world.getBottomY(), minZ, maxX, mc.world.getTopY(), maxZ);

        return mc.world.getEntitiesByClass(TntEntity.class, searchBox, entity -> entity.isAlive() && entity.getFuse() > 0);
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
    private static void bqol$collectBlocksInExplosionRadius(Set<BlockPos> blocks, TntEntity tnt) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null) {
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

        int minY = Math.max(mc.world.getBottomY(), (int) Math.floor(cy - radius));
        int maxY = Math.min(mc.world.getTopY() - 1, (int) Math.floor(cy + radius));

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

                    BlockState state = mc.world.getBlockState(mutable);

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
    private static void bqol$renderBlockFaces(Matrix4f matrix, VertexConsumer consumer, BlockPos pos, Set<BlockPos> blocks, double cameraX, double cameraY, double cameraZ) {
        double x1 = pos.getX() - cameraX;
        double y1 = pos.getY() - cameraY;
        double z1 = pos.getZ() - cameraZ;

        double x2 = x1 + 1.0D;
        double y2 = y1 + 1.0D;
        double z2 = z1 + 1.0D;

        double offset = BQOL_SURFACE_OFFSET;

        for (Direction direction : Direction.values()) {
            BQOL_NEIGHBOR.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());

            if (blocks.contains(BQOL_NEIGHBOR)) {
                continue;
            }

            switch (direction) {
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
    }

    @Unique
    private static void bqol$quad(Matrix4f matrix, VertexConsumer consumer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        bqol$vertex(matrix, consumer, x1, y1, z1);
        bqol$vertex(matrix, consumer, x2, y2, z2);
        bqol$vertex(matrix, consumer, x3, y3, z3);
        bqol$vertex(matrix, consumer, x4, y4, z4);
    }

    @Unique
    private static void bqol$vertex(Matrix4f matrix, VertexConsumer consumer, double x, double y, double z) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
            .color(BQOL_RED, BQOL_GREEN, BQOL_BLUE, BQOL_FILL_ALPHA)
            .next();
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