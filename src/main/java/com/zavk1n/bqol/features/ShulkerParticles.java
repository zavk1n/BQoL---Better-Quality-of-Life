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

import java.util.*;

public class ShulkerParticles {

    private static MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private ShulkerParticles() {}

    private static ShulkerParticles instance;
    private static MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();
    private static final Random random = new Random();

    /// Кэш
    private final Map<Vector3f, DustParticleEffect> EFFECT_CACHE = new HashMap<>();

    /// Таблица цветовой палитры
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

    /// Блокировки
    private static final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean constant;
        boolean breaking;
        boolean vanillaBreaking;
        boolean constantDependence;
        boolean breakingDependence;
    }

    /// Остальные состояния
    private static final Vector3f DEFAULT_DYE_COLOR = rgb(0xB363BA);
    private static int tickCounter = 0;

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new ShulkerParticles();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("ShulkerParticles initialized");
        }
    }

    public static ShulkerParticles getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static ClientWorld getClientWorld() {
        return mc().world;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    public static boolean shouldCancelVanillaBreaking() {
        return instance != null && instance.shouldCancelVanillaBreakingInternal();
    }

    public static boolean canSpawnConstantParticles() {
        return instance != null && instance.canSpawnConstantParticlesInternal();
    }

    public static boolean canSpawnBreakingParticles() {
        return instance != null && instance.canSpawnBreakingParticlesInternal();
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("shulker_particles");
        blocked.constant = LiteApiManager.isFeatureBlocked("shulker_particles_constant");
        blocked.breaking = LiteApiManager.isFeatureBlocked("shulker_particles_breaking");
        blocked.vanillaBreaking = LiteApiManager.isFeatureBlocked("shulker_particles_vanilla_breaking");
        blocked.constantDependence = LiteApiManager.isFeatureBlocked("shulker_particles_constant_dependence");
        blocked.breakingDependence = LiteApiManager.isFeatureBlocked("shulker_particles_breaking_dependence");
    }

    private boolean isEnabledInternal() {
        return config.isShulkerParticlesEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setShulkerParticlesEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean shouldCancelVanillaBreakingInternal() {
        return isEnabledInternal()
                && config.isShulkerVanillaBreakingEnabled()
                && !blocked.vanillaBreaking;
    }

    private boolean canSpawnConstantParticlesInternal() {
        return isEnabledInternal()
                && config.isShulkerConstantEnabled()
                && !blocked.constant;
    }

    private boolean canSpawnBreakingParticlesInternal() {
        return isEnabledInternal()
                && config.isShulkerBreakingEnabled()
                && !blocked.breaking;
    }

    /// Основная логика
    public static void onTick() {
        if (instance == null || !isEnabled() || !canSpawnConstantParticles()) {
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

        Vector3f defaultColor = instance.rgb(instance.config.getShulkerConstantColor());

        boolean useDependence = instance.config.isShulkerConstantDependence() && !blocked.constantDependence;

        instance.scanChunks(client, client.world, defaultColor, useDependence);
    }

    /// Сканирование чанков
    private void scanChunks(MinecraftClient client, ClientWorld world, Vector3f defaultColor, boolean useDependence) {
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

                    Vector3f color = resolveConstantColor(
                            state,
                            defaultColor,
                            useDependence
                    );

                    spawnConstantParticles(
                            world,
                            pos,
                            color,
                            multiplier
                    );
                }
            }
        }
    }

    /// Получение цветов
    private Vector3f resolveConstantColor(BlockState state, Vector3f defaultColor, boolean useDependence) {
        if (!useDependence || !(state.getBlock() instanceof ShulkerBoxBlock box)) {
            return defaultColor;
        }

        return dyeColorToVector(box.getColor());
    }

    private Vector3f resolveBreakingColor(BlockState state) {
        boolean useDependence = config.isShulkerBreakingDependence() && !blocked.breakingDependence;

        if (!useDependence || !(state.getBlock() instanceof ShulkerBoxBlock box)) {
            return rgb(config.getShulkerBreakingColor());
        }

        return dyeColorToVector(box.getColor());
    }

    /// Спавн частиц
    private void spawnConstantParticles(ClientWorld world, BlockPos pos, Vector3f color, float multiplier) {
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
        Vector3f look = new Vector3f(0.0F, 0.0F, 1.0F).rotate(client.gameRenderer.getCamera().getRotation());

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

    private void spawnBreakingParticles(ClientWorld world, Vec3d center, Vector3f color) {
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

    /// Событийный метод для миксина
    public void onShulkerBroken(BlockPos pos, BlockState state) {
        if (!canSpawnBreakingParticles()) {
            return;
        }

        ClientWorld world = getClientWorld();

        if (world == null) {
            return;
        }

        spawnBreakingParticles(
                world,
                Vec3d.ofCenter(pos),
                resolveBreakingColor(state)
        );
    }

    /// Получение информации шалкеров
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
        return EFFECT_CACHE.computeIfAbsent(
                color,
                c -> new DustParticleEffect(new Vector3f(c), 1.2F)
        );
    }

    /// Множитель для производительности
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

    /// Методы для преобразования цветов
    private static Vector3f rgb(int rgb) {
        return new Vector3f(
                ((rgb >> 16) & 255) / 255F,
                ((rgb >> 8) & 255) / 255F,
                (rgb & 255) / 255F
        );
    }

    private Vector3f dyeColorToVector(DyeColor color) {
        if (color == null) {
            return DEFAULT_DYE_COLOR;
        }

        return PARTICLE_COLORS.getOrDefault(color, DEFAULT_DYE_COLOR);
    }
}
// v1.0