package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BetterParticles {
    private static MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }
        return client;
    }

    private BetterParticles() {}

    private static MinecraftClient client;
    private static BetterParticles instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private static final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean shulkers;
        boolean totems;
    }

    private static final Random random = new Random();
    private final Map<Vector3f, DustParticleEffect> EFFECT_CACHE = new HashMap<>();
    private static final EnumMap<DyeColor, Vector3f> PARTICLE_COLORS = new EnumMap<>(DyeColor.class);

    static {
        PARTICLE_COLORS.put(DyeColor.WHITE, rgb(0xF9FFFE));
        PARTICLE_COLORS.put(DyeColor.ORANGE, rgb(0xF9801D));
        PARTICLE_COLORS.put(DyeColor.MAGENTA, rgb(0xC74EBD));
        PARTICLE_COLORS.put(DyeColor.LIGHT_BLUE, rgb(0x3AB3DA));
        PARTICLE_COLORS.put(DyeColor.YELLOW, rgb(0xFED83D));
        PARTICLE_COLORS.put(DyeColor.LIME, rgb(0x80C71F));
        PARTICLE_COLORS.put(DyeColor.PINK, rgb(0xF38BAA));
        PARTICLE_COLORS.put(DyeColor.GRAY, rgb(0x474F52));
        PARTICLE_COLORS.put(DyeColor.LIGHT_GRAY, rgb(0x9D9D97));
        PARTICLE_COLORS.put(DyeColor.CYAN, rgb(0x169C9C));
        PARTICLE_COLORS.put(DyeColor.PURPLE, rgb(0x7627B0));
        PARTICLE_COLORS.put(DyeColor.BLUE, rgb(0x3C44AA));
        PARTICLE_COLORS.put(DyeColor.BROWN, rgb(0x835432));
        PARTICLE_COLORS.put(DyeColor.GREEN, rgb(0x5E7C16));
        PARTICLE_COLORS.put(DyeColor.RED, rgb(0xB02E26));
        PARTICLE_COLORS.put(DyeColor.BLACK, rgb(0x1D1D21));
    }

    private static final Vector3f DEFAULT_DYE_COLOR = rgb(0xB363BA);

    private static int tickCounter = 0;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterParticles();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Particles initialized");
        }
    }

    public static BetterParticles getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    public static ClientWorld getClientWorld() {
        return mc().world;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) {
            instance.refreshBlockedStatusInternal();
        }
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        getInstance().setEnabledInternal(enabled);
    }

    public static boolean isShulkersEnabled() {
        return instance != null && instance.isShulkersEnabledInternal();
    }

    public static void setShulkersEnabled(boolean enabled) {
        getInstance().setShulkersEnabledInternal(enabled);
    }

    public static boolean shouldCancelShulkersVanillaBreaking() {
        return instance != null
            && instance.shouldCancelShulkersVanillaBreakingInternal();
    }

    public static boolean canSpawnShulkersConstantParticles() {
        return instance != null
            && instance.spawnShulkersConstantParticlesInternal();
    }

    public static boolean canSpawnShulkersBreakingParticles() {
        return instance != null
            && instance.spawnShulkersBreakingParticlesInternal();
    }

    public static boolean isTotemsEnabled() {
        return instance != null && instance.isTotemsEnabledInternal();
    }

    public static void setTotemsEnabled(boolean enabled) {
        getInstance().setTotemsEnabledInternal(enabled);
    }

    public static boolean isTotemsColorEnabled() {
        return instance != null
            && instance.config.isBetterParticlesTotemsColorEnabled();
    }

    public static void setTotemsColorEnabled(boolean enabled) {
        getInstance().setTotemsColorEnabledInternal(enabled);
    }

    public static boolean isTotemsCountEnabled() {
        return instance != null
            && instance.config.isBetterParticlesTotemsCountEnabled();
    }

    public static void setTotemsCountEnabled(boolean enabled) {
        getInstance().setTotemsCountEnabledInternal(enabled);
    }

    public static int getTotemsColor() {
        return instance != null ? instance.config.getBetterParticlesTotemsColor() : 0xB363BA;
    }

    public static int getTotemsLightColor() {
        return adjustBrightness(getTotemsColor(), 1.35F);
    }

    public static int getTotemsDarkColor() {
        return adjustBrightness(getTotemsColor(), 0.60F);
    }

    public static int getTotemsCountMode() {
        return instance != null ? instance.config.getBetterParticlesTotemsCountMode() : 1;
    }

    public static float getTotemsParticleMultiplier() {
        if (!isTotemsCountEnabled()) {
            return 1.0F;
        }

        return switch (getTotemsCountMode()) {
            case 0 -> 0.5F;
            case 2 -> 1.5F;
            default -> 1.0F;
        };
    }

    public static int getRandomTotemsColor() {
        if (!isTotemsColorEnabled()) {
            return 0xB363BA;
        }

        return switch (random.nextInt(3)) {
            case 0 -> getTotemsLightColor();
            case 1 -> getTotemsDarkColor();
            default -> getTotemsColor();
        };
    }

    private static int adjustBrightness(int color, float multiplier) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Math.min(255, Math.max(0, Math.round(red * multiplier)));
        green = Math.min(255, Math.max(0, Math.round(green * multiplier)));
        blue = Math.min(255, Math.max(0, Math.round(blue * multiplier)));

        return (red << 16) | (green << 8) | blue;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_particles");
        blocked.shulkers = LiteApiManager.isFeatureBlocked("better_particles_shulkers");
        blocked.totems = LiteApiManager.isFeatureBlocked("better_particles_totems");
    }

    private boolean isEnabledInternal() {
        return config.isBetterParticlesEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterParticlesEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isShulkersEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterParticlesShulkersEnabled()
            && !blocked.shulkers;
    }

    private void setShulkersEnabledInternal(boolean enabled) {
        config.setBetterParticlesShulkersEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean shouldCancelShulkersVanillaBreakingInternal() {
        return isShulkersEnabledInternal()
            && config.isBetterParticlesShulkersVanillaBreakingEnabled();
    }

    private boolean spawnShulkersConstantParticlesInternal() {
        return isShulkersEnabledInternal()
            && config.isBetterParticlesShulkersConstantEnabled();
    }

    private boolean spawnShulkersBreakingParticlesInternal() {
        return isShulkersEnabledInternal()
            && config.isBetterParticlesShulkersBreakingEnabled();
    }

    private boolean isTotemsEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterParticlesTotemsEnabled()
            && !blocked.totems;
    }

    private void setTotemsEnabledInternal(boolean enabled) {
        config.setBetterParticlesTotemsEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private void setTotemsColorEnabledInternal(boolean enabled) {
        config.setBetterParticlesTotemsColorEnabled(enabled);
    }

    private void setTotemsCountEnabledInternal(boolean enabled) {
        config.setBetterParticlesTotemsCountEnabled(enabled);
    }

    public static void onTick() {
        if (instance == null
            || !isEnabled()
            || !canSpawnShulkersConstantParticles()) {
            return;
        }

        MinecraftClient client = mc();

        if (client.world == null || client.player == null) {
            return;
        }

        tickCounter++;

        if (tickCounter % 3 != 0) {
            return;
        }

        Vector3f defaultColor = instance.rgb(instance.config.getBetterParticlesShulkersConstantColor());

        boolean useDependence = instance.config.isBetterParticlesShulkersConstantDependenceEnabled();

        instance.scanChunksforShulkers(client, client.world, defaultColor, useDependence);
    }

    private void scanChunksforShulkers(MinecraftClient client, ClientWorld world, Vector3f defaultColor, boolean useDependence) {
        if (client.player == null || !client.gameRenderer.getCamera().isReady()) {
            return;
        }

        int playerChunkX = client.player.getBlockX() >> 4;
        int playerChunkZ = client.player.getBlockZ() >> 4;

        for (int chunkX = playerChunkX - 1; chunkX <= playerChunkX + 1; chunkX++) {
            for (int chunkZ = playerChunkZ - 1; chunkZ <= playerChunkZ + 1; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                List<BlockPos> shulkers = getShulkers(chunk);

                if (shulkers.isEmpty()) {
                    continue;
                }

                float multiplier = getParticleMultiplier(shulkers.size());

                Iterator<BlockPos> iterator = shulkers.iterator();

                while (iterator.hasNext()) {
                    BlockPos pos = iterator.next();
                    BlockState state = world.getBlockState(pos);

                    if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
                        iterator.remove();
                        continue;
                    }

                    Vector3f color = resolveShulkersConstantColor(state, defaultColor, useDependence);

                    spawnShulkersConstantParticles(world, pos, color, multiplier);
                }
            }
        }
    }

    private Vector3f resolveShulkersConstantColor(BlockState state, Vector3f defaultColor, boolean useDependence) {
        if (!useDependence || !(state.getBlock() instanceof ShulkerBoxBlock box)) {
            return defaultColor;
        }

        return dyeColorToVector(box.getColor());
    }

    private Vector3f resolveShulkersBreakingColor(BlockState state) {
        boolean useDependence =
            config.isBetterParticlesShulkersBreakingDependenceEnabled();

        if (!useDependence || !(state.getBlock() instanceof ShulkerBoxBlock box)) {
            return rgb(config.getBetterParticlesShulkersBreakingColor());
        }

        return dyeColorToVector(box.getColor());
    }

    private void spawnShulkersConstantParticles(ClientWorld world, BlockPos pos, Vector3f color, float multiplier) {
        MinecraftClient client = mc();

        if (world != client.world) {
            return;
        }

        Vec3d center = Vec3d.ofCenter(pos);
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        if (center.squaredDistanceTo(cameraPos) > 256.0D * 256.0D) {
            return;
        }

        Vec3d toShulker = center.subtract(cameraPos).normalize();
        Vector3f look = new Vector3f(0.0F, 0.0F, 1.0F)
            .rotate(client.gameRenderer.getCamera().getRotation());

        double dot = toShulker.x * look.x() + toShulker.y * look.y() + toShulker.z * look.z();

        if (dot <= 0.0D) {
            return;
        }

        DustParticleEffect effect = getEffect(color);

        int baseCount = 9 + random.nextInt(4);
        int count = Math.max(1, Math.round(baseCount * multiplier));

        for (int i = 0; i < count; i++) {
            world.addParticle(
                effect,
                center.x + (random.nextDouble() - 0.5) * 1.2,
                center.y + (random.nextDouble() - 0.5) * 1.2,
                center.z + (random.nextDouble() - 0.5) * 1.2,
                (random.nextDouble() - 0.5) * 0.04,
                (random.nextDouble() - 0.5) * 0.04,
                (random.nextDouble() - 0.5) * 0.04
            );
        }
    }

    private void spawnShulkersBreakingParticles(ClientWorld world, Vec3d center, Vector3f color) {
        DustParticleEffect effect = getEffect(color);

        int count = 30 + random.nextInt(20);

        for (int i = 0; i < count; i++) {
            world.addParticle(
                effect,
                center.x + (random.nextDouble() - 0.5) * 1.5,
                center.y + (random.nextDouble() - 0.5) * 1.5 + 0.5,
                center.z + (random.nextDouble() - 0.5) * 1.5,
                (random.nextDouble() - 0.5) * 0.2,
                random.nextDouble() * 0.3,
                (random.nextDouble() - 0.5) * 0.2
            );
        }
    }

    public void onShulkerBroken(BlockPos pos, BlockState state) {
        if (!canSpawnShulkersBreakingParticles()) {
            return;
        }

        ClientWorld world = getClientWorld();

        if (world == null) {
            return;
        }

        spawnShulkersBreakingParticles(world, Vec3d.ofCenter(pos), resolveShulkersBreakingColor(state));
    }

    private List<BlockPos> getShulkers(WorldChunk chunk) {
        List<BlockPos> result = new ArrayList<>();

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof ShulkerBoxBlockEntity) {
                result.add(blockEntity.getPos().toImmutable());
            }
        }

        return result;
    }

    private DustParticleEffect getEffect(Vector3f color) {
        Vector3f key = new Vector3f(color);

        return EFFECT_CACHE.computeIfAbsent(key, c -> new DustParticleEffect(new Vector3f(c), 1.2F));
    }

    private float getParticleMultiplier(int shulkerCount) {
        if (shulkerCount > 100) {
            return 1F / 1.75F;
        }

        if (shulkerCount > 64) {
            return 1F / 1.55F;
        }

        if (shulkerCount > 48) {
            return 1F / 1.35F;
        }

        if (shulkerCount > 24) {
            return 1F / 1.15F;
        }

        return 1F;
    }

    private static Vector3f rgb(int rgb) {
        return new Vector3f(
            ((rgb >> 16) & 255) / 255F,
            ((rgb >> 8) & 255) / 255F,
            (rgb & 255) / 255F
        );
    }

    private Vector3f dyeColorToVector(DyeColor color) {
        if (color == null) {
            return new Vector3f(DEFAULT_DYE_COLOR);
        }

        Vector3f result = PARTICLE_COLORS.get(color);
        return result != null
            ? new Vector3f(result)
            : new Vector3f(DEFAULT_DYE_COLOR);
    }
}