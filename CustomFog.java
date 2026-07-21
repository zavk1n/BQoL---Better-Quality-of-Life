package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomFog {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private CustomFog() {}

    private static CustomFog instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private ActiveSource activeSource = ActiveSource.None;
    private enum ActiveSource { None, Custom, Biome }

    /// Состояния в классе
    private final TransitionState transitionState = new TransitionState();

    private static class TransitionState {
        float speed = 0.1f;
        boolean active;

        Vector3f currentColor = new Vector3f(1f, 1f, 1f);
        Vector3f targetColor = new Vector3f(1f, 1f, 1f);

        float currentFogEnd = 256f;
        float targetFogEnd = 256f;
    }

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean noFog;
        boolean nightVision;
        boolean biomeFog;
    }

    private static final Map<String, Integer> BIOME_COLORS = new HashMap<>();
    private static final Map<String, List<String>> BIOME_GROUP_MAPPING = new HashMap<>();
    private static final Map<String, String> BIOME_TO_GROUP = new HashMap<>();

    /// Остальные состояния
    private int chunksRange = 8;
    private int rgbColor = 0xFFFFFF;
    private String currentBiomeGroup = null;

    /// Таблица цветов биомов
    static {
        BIOME_COLORS.put("CrimsonForest", 0xFF8A8A);
        BIOME_COLORS.put("NetherWastes", 0xFF8A8A);
        BIOME_COLORS.put("BasaltDeltas", 0xADADAD);
        BIOME_COLORS.put("WarpedForest", 0x498F63);
        BIOME_COLORS.put("SoulSand", 0xAD968C);
        BIOME_COLORS.put("GrowthTaiga", 0xCFA386);
        BIOME_COLORS.put("Mushrooms", 0xB5A1A4);
        BIOME_COLORS.put("Snow", 0xE0FBFF);
        BIOME_COLORS.put("Desert", 0xFFFBD4);
        BIOME_COLORS.put("Savanna", 0xC9D461);
        BIOME_COLORS.put("Mesa", 0xFFD291);
        BIOME_COLORS.put("End", 0xE7DBFF);

        BIOME_GROUP_MAPPING.put("CrimsonForest", List.of("minecraft:crimson_forest"));
        BIOME_GROUP_MAPPING.put("NetherWastes", List.of("minecraft:nether_wastes"));
        BIOME_GROUP_MAPPING.put("BasaltDeltas", List.of("minecraft:basalt_deltas"));
        BIOME_GROUP_MAPPING.put("WarpedForest", List.of("minecraft:warped_forest"));
        BIOME_GROUP_MAPPING.put("SoulSand", List.of("minecraft:soul_sand_valley"));

        BIOME_GROUP_MAPPING.put("GrowthTaiga", List.of(
            "minecraft:old_growth_pine_taiga",
            "minecraft:old_growth_spruce_taiga"
        ));

        BIOME_GROUP_MAPPING.put("Mushrooms", List.of(
            "minecraft:mushroom_fields"
        ));

        BIOME_GROUP_MAPPING.put("Snow", List.of(
            "minecraft:snowy_plains",
            "minecraft:snowy_taiga",
            "minecraft:ice_spikes",
            "minecraft:frozen_river",
            "minecraft:snowy_beach",
            "minecraft:snowy_slopes",
            "minecraft:frozen_ocean",
            "minecraft:deep_frozen_ocean",
            "minecraft:grove",
            "minecraft:jagged_peaks"
        ));

        BIOME_GROUP_MAPPING.put("Desert", List.of(
            "minecraft:desert"
        ));

        BIOME_GROUP_MAPPING.put("Savanna", List.of(
            "minecraft:savanna",
            "minecraft:savanna_plateau",
            "minecraft:windswept_savanna"
        ));

        BIOME_GROUP_MAPPING.put("Mesa", List.of(
            "minecraft:badlands",
            "minecraft:wooded_badlands",
            "minecraft:eroded_badlands"
        ));

        BIOME_GROUP_MAPPING.put("End", List.of(
            "minecraft:end_barrens",
            "minecraft:end_highlands",
            "minecraft:end_midlands",
            "minecraft:small_end_islands"
        ));

        for (var entry : BIOME_GROUP_MAPPING.entrySet()) {
            for (String biome : entry.getValue()) {
                BIOME_TO_GROUP.put(biome, entry.getKey());
            }
        }
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new CustomFog();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            instance.init();
            BQoL.LOGGER.info("CustomFog initialized");
        }
    }

    public static CustomFog getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static void reloadFromConfig() {
        if (instance != null) instance.reloadFromConfigInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    public static boolean isNoFogEnabled() {
        return instance != null && instance.isNoFogEnabledInternal();
    }

    public static void setNoFogEnabled(boolean enabled) {
        if (instance != null) instance.setNoFogEnabledInternal(enabled);
    }

    public static boolean isNightVisionEnabled() {
        return instance != null && instance.isNightVisionEnabledInternal();
    }

    public static void setNightVisionEnabled(boolean enabled) {
        if (instance != null) instance.setNightVisionEnabledInternal(enabled);
    }

    public static boolean isBiomeFogEnabled() {
        return instance != null && instance.isBiomeFogEnabledInternal();
    }

    public static void setBiomeFogEnabled(boolean enabled) {
        if (instance != null) instance.setBiomeFogEnabledInternal(enabled);
    }

    public static int getRangeChunks() {
        return instance == null ? 8 : instance.chunksRange;
    }

    public static void setRangeChunks(int chunks) {
        if (instance != null) instance.setRangeChunksInternal(chunks);
    }

    public static int getColorRGB() {
        return instance == null ? 0xFFFFFF : instance.rgbColor;
    }

    public static void setColorRGB(int rgb) {
        if (instance != null) instance.setColorRGBInternal(rgb);
    }

    public static boolean isUsingCustomFog() {
        return instance != null && instance.isUsingCustomFogInternal();
    }

    public static float getCurrentRed() {
        return instance == null ? 1f : instance.transitionState.currentColor.x;
    }

    public static float getCurrentGreen() {
        return instance == null ? 1f : instance.transitionState.currentColor.y;
    }

    public static float getCurrentBlue() {
        return instance == null ? 1f : instance.transitionState.currentColor.z;
    }

    public static float getCurrentFogEnd() {
        return instance == null ? 256f : instance.transitionState.currentFogEnd;
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("custom_fog");
        blocked.noFog = LiteApiManager.isFeatureBlocked("custom_fog_no_fog");
        blocked.nightVision = LiteApiManager.isFeatureBlocked("custom_fog_night_vision");
        blocked.biomeFog = LiteApiManager.isFeatureBlocked("custom_fog_biome_fog");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();

        chunksRange = MathHelper.clamp(config.getCustomFogRange(), 1, 32);
        rgbColor = config.getCustomFogColor() & 0xFFFFFF;

        transitionState.targetColor.set(
            getRedFromColor(rgbColor),
            getGreenFromColor(rgbColor),
            getBlueFromColor(rgbColor)
        );

        transitionState.targetFogEnd = chunksRange * 16f;

        if (!transitionState.active) {
            transitionState.currentColor.set(transitionState.targetColor);
            transitionState.currentFogEnd = transitionState.targetFogEnd;
        }
    }

    private boolean isEnabledInternal() {
        return config.isCustomFogEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setCustomFogEnabled(enabled);

        reloadFromConfigInternal();

        if (!config.isCustomFogEnabled()) {
            activeSource = ActiveSource.None;
            transitionState.active = false;
        }
    }

    private boolean isNoFogEnabledInternal() {
        return config.isNoFog() && !blocked.noFog;
    }

    private void setNoFogEnabledInternal(boolean enabled) {
        config.setNoFog(enabled);
    }

    private boolean isNightVisionEnabledInternal() {
        return config.isNightVision() && !blocked.nightVision;
    }

    private void setNightVisionEnabledInternal(boolean enabled) {
        config.setNightVision(enabled);
    }

    private boolean isBiomeFogEnabledInternal() {
        return config.isBiomeFog() && !blocked.biomeFog;
    }

    private void setBiomeFogEnabledInternal(boolean enabled) {
        config.setBiomeFog(enabled);
    }

    private void setRangeChunksInternal(int chunks) {
        chunksRange = MathHelper.clamp(chunks, 1, 32);
        config.setCustomFogRange(chunksRange);

        if (activeSource == ActiveSource.Custom || activeSource == ActiveSource.Biome) {
            transitionState.targetFogEnd = chunksRange * 16.0f;
            transitionState.active = true;
        }
    }

    private void setColorRGBInternal(int rgb) {
        rgb &= 0xFFFFFF;

        rgbColor = rgb;
        config.setCustomFogColor(rgb);

        if (activeSource == ActiveSource.Custom) {
            transitionState.targetColor.set(
                getRedFromColor(rgb),
                getGreenFromColor(rgb),
                getBlueFromColor(rgb)
            );

            transitionState.active = true;
        }
    }

    private boolean isUsingCustomFogInternal() {
        return activeSource != ActiveSource.None && !isNoFogEnabledInternal();
    }

    /// Ядро логики
    private void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) {
                return;
            }

            updateActiveFogSource();

            if (transitionState.active)
                updateTransition();
        });
    }

    /// Работа с переходами тумана
    private void updateTransition() {
        boolean changed = false;

        if (transitionState.currentColor.distance(transitionState.targetColor) > 0.002f) {
            transitionState.currentColor.lerp(transitionState.targetColor, transitionState.speed);

            changed = true;
        } else {
            transitionState.currentColor.set(transitionState.targetColor);
        }

        if (Math.abs(
            transitionState.currentFogEnd - transitionState.targetFogEnd) > 0.5f) {
            transitionState.currentFogEnd += (transitionState.targetFogEnd - transitionState.currentFogEnd) * transitionState.speed;

            changed = true;
        } else {
            transitionState.currentFogEnd = transitionState.targetFogEnd;
        }

        transitionState.active = changed;
    }

    private void updateActiveFogSource() {
        MinecraftClient client = mc();

        if (client == null || client.player == null || client.world == null)
            return;

        if (isNoFogEnabledInternal()) {
            activeSource = ActiveSource.None;
            applyTargetFromSource();
            return;
        }

        ActiveSource newSource = ActiveSource.None;
        String newBiome = null;

        if (isBiomeFogEnabledInternal()) {
            newBiome = getCurrentBiomeGroup();

            if (newBiome != null && config.isBiomeGroupEnabled(newBiome)) {
                newSource = ActiveSource.Biome;
            }
        }

        if (newSource == ActiveSource.None && isEnabledInternal()) {
            newSource = ActiveSource.Custom;
        }

        if (newSource != activeSource || !Objects.equals(newBiome, currentBiomeGroup)) {
            activeSource = newSource;
            currentBiomeGroup = newBiome;

            applyTargetFromSource();
        }
    }

    private void applyTargetFromSource() {
        int color;

        switch (activeSource) {
            case Biome -> {
                color = BIOME_COLORS.getOrDefault(
                    currentBiomeGroup,
                    0xFFFFFF);
            }

            case Custom -> {
                color = rgbColor;
            }

            default -> {
                transitionState.targetColor.set(1f,1f,1f);
                transitionState.targetFogEnd = 256f;
                transitionState.active = true;
                currentBiomeGroup = null;
                return;
            }
        }

        transitionState.targetColor.set(
            getRedFromColor(color),
            getGreenFromColor(color),
            getBlueFromColor(color));

        transitionState.targetFogEnd = chunksRange * 16f;
        transitionState.active = true;
    }

    /// Работа с цветами
    private String getCurrentBiomeGroup() {
        MinecraftClient client = mc();

        if (client == null
            || client.world == null
            || client.player == null)
            return null;

        Biome biome = client.world
            .getBiome(client.player.getBlockPos())
            .value();

        var registry = client.world
            .getRegistryManager()
            .get(RegistryKeys.BIOME);

        Identifier id = registry.getId(biome);

        if (id == null)
            return null;

        return BIOME_TO_GROUP.get(id.toString());
    }

    private float getRedFromColor(int rgb) {
        rgb &= 0xFFFFFF;
        return ((rgb >> 16) & 0xFF) / 255.0f;
    }

    private float getBlueFromColor(int rgb) {
        rgb &= 0xFFFFFF;
        return (rgb & 0xFF) / 255.0f;
    }

    private float getGreenFromColor(int rgb) {
        rgb &= 0xFFFFFF;
        return ((rgb >> 8) & 0xFF) / 255.0f;
    }
}
// v1.0