package com.zavk1n.bqol.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zavk1n.bqol.features.BetterTnt;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(WorldRenderer.class)
public abstract class TNTExplosionAreaRenderer {

    @Unique private static final float RED = 1.0F;
    @Unique private static final float GREEN = 0.0F;
    @Unique private static final float BLUE = 0.0F;

    @Unique private static final float FILL_ALPHA = 0.08F;

    @Unique private static final double SURFACE_OFFSET = 0.002D;

    @Unique private static final int MAX_NORMAL_TNT_IN_NEARBY_CHUNKS = 100;
    @Unique private static final int MAX_TNT_A_IN_NEARBY_CHUNKS = 20;
    @Unique private static final int MAX_TNT_B_IN_NEARBY_CHUNKS = 8;
    @Unique private static final int MAX_TNT_B2_IN_NEARBY_CHUNKS = 8;

    @Unique private static final int CHUNK_CHECK_RADIUS = 1;

    @Inject(method = "render", at = @At("TAIL"))
    private void bqol$renderExplosionArea(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!BetterTnt.isExplosionAreaEnabled()
            || mc.world == null
            || mc.player == null) {
            return;
        }

        List<TntEntity> nearbyTnts = getNearbyTnts(mc);

        if (nearbyTnts.isEmpty()) {
            return;
        }

        Map<BetterTnt.TntType, Integer> renderedCounts = new EnumMap<>(BetterTnt.TntType.class);

        Set<BlockPos> blocksToRender = new HashSet<>();

        for (TntEntity tnt : nearbyTnts) {
            BetterTnt.TntType type = BetterTnt.getTntType(tnt);

            if (type == null) {
                continue;
            }

            int rendered = renderedCounts.getOrDefault(type, 0);

            if (rendered >= getMaxTntForType(type)) {
                continue;
            }

            renderedCounts.put(type, rendered + 1);

            collectBlocksInExplosionRadius(blocksToRender, tnt);
        }

        if (blocksToRender.isEmpty()) {
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

            for (BlockPos pos : blocksToRender) {
                renderBlockFaces(matrix, buffer, pos, blocksToRender, cameraX, cameraY, cameraZ);
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
    private static List<TntEntity> getNearbyTnts(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return Collections.emptyList();
        }

        ChunkPos chunk = mc.player.getChunkPos();

        int minX = (chunk.x - CHUNK_CHECK_RADIUS) << 4;
        int minZ = (chunk.z - CHUNK_CHECK_RADIUS) << 4;

        int maxX = (chunk.x + CHUNK_CHECK_RADIUS + 1) << 4;
        int maxZ = (chunk.z + CHUNK_CHECK_RADIUS + 1) << 4;

        Box box = new Box(minX, mc.world.getBottomY(), minZ, maxX, mc.world.getTopY(), maxZ);

        return mc.world.getEntitiesByClass(TntEntity.class, box, entity -> entity.isAlive() && entity.getFuse() > 0);
    }

    @Unique
    private static int getMaxTntForType(BetterTnt.TntType type) {
        return switch (type) {
            case A -> MAX_TNT_A_IN_NEARBY_CHUNKS;
            case B -> MAX_TNT_B_IN_NEARBY_CHUNKS;
            case B2 -> MAX_TNT_B2_IN_NEARBY_CHUNKS;
            case NORMAL, C4, ICE_WAVE, EXPLOSIVE_WAVE -> MAX_NORMAL_TNT_IN_NEARBY_CHUNKS;
        };
    }

    @Unique
    private static void collectBlocksInExplosionRadius(Set<BlockPos> blocks, TntEntity tnt) {
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
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    double dx = x + 0.5D - cx;
                    double dy = y + 0.5D - cy;
                    double dz = z + 0.5D - cz;

                    if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                        continue;
                    }

                    mutable.set(x, y, z);

                    if (blocks.contains(mutable)) {
                        continue;
                    }

                    BlockState state = mc.world.getBlockState(mutable);

                    if (state.isAir()
                        || !state.getFluidState().isEmpty()
                        || isIgnoredBlock(state)) {
                        continue;
                    }

                    blocks.add(mutable.toImmutable());
                }
            }
        }
    }

    @Unique
    private static void renderBlockFaces(Matrix4f matrix, VertexConsumer consumer, BlockPos pos, Set<BlockPos> blocks, double cameraX, double cameraY, double cameraZ) {
        double x1 = pos.getX() - cameraX;
        double y1 = pos.getY() - cameraY;
        double z1 = pos.getZ() - cameraZ;

        double x2 = x1 + 1.0D;
        double y2 = y1 + 1.0D;
        double z2 = z1 + 1.0D;

        for (Direction direction : Direction.values()) {
            if (blocks.contains(pos.offset(direction))) {
                continue;
            }

            double o = SURFACE_OFFSET;

            switch (direction) {
                case UP -> quad(matrix, consumer, x1, y2 + o, z1, x1, y2 + o, z2, x2, y2 + o, z2, x2, y2 + o, z1);
                case DOWN -> quad(matrix, consumer, x1, y1 - o, z2, x1, y1 - o, z1, x2, y1 - o, z1, x2, y1 - o, z2);
                case NORTH -> quad(matrix, consumer, x2, y1, z1 - o, x1, y1, z1 - o, x1, y2, z1 - o, x2, y2, z1 - o);
                case SOUTH -> quad(matrix, consumer, x1, y1, z2 + o, x2, y1, z2 + o, x2, y2, z2 + o, x1, y2, z2 + o);
                case WEST -> quad(matrix, consumer, x1 - o, y1, z1, x1 - o, y1, z2, x1 - o, y2, z2, x1 - o, y2, z1);
                case EAST -> quad(matrix, consumer, x2 + o, y1, z2, x2 + o, y1, z1, x2 + o, y2, z1, x2 + o, y2, z2);
            }
        }
    }

    @Unique
    private static void quad(Matrix4f matrix, VertexConsumer consumer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        vertex(matrix, consumer, x1, y1, z1);
        vertex(matrix, consumer, x2, y2, z2);
        vertex(matrix, consumer, x3, y3, z3);
        vertex(matrix, consumer, x4, y4, z4);
    }

    @Unique
    private static void vertex(Matrix4f matrix, VertexConsumer consumer, double x, double y, double z) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
            .color(RED, GREEN, BLUE, FILL_ALPHA)
            .next();
    }

    @Unique
    private static boolean isIgnoredBlock(BlockState state) {
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