package com.zavk1n.bqol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zavk1n.bqol.BQoL;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BQoLConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "bqol_config.json"
    );

    private static BQoLConfig instance;

    /// ОБЩИЕ НАСТРОЙКИ
    public boolean debugMode = false;
    public int pvpTimerDuration = 30000;

    /// Better Sprint
    public boolean betterSprintEnabled = false;

    public boolean betterSprintDefault = false;
    public boolean betterSprintPvP = false;
    public boolean betterSprintTree = false;
    public boolean betterSprintStairUp = false;
    public boolean betterSprintWaterSprint = false;

    /// Better Sounds
    public boolean betterSoundsEnabled = false;

    public boolean explosionMode = false;
    public boolean enderDragonMode = false;
    public boolean pistonMode = false;
    public boolean iceMode = false;
    public boolean villagerMode = false;
    public boolean moodMode = false;
    public boolean thunderMode = false;
    public boolean fireMode = false;
    public boolean eatMode = false;
    public boolean drinkMode = false;
    public boolean hitsMode = false;
    public boolean storageMode = false;
    public boolean grassMode = false;
    public boolean totemMode = false;
    public boolean anvilMode = false;
    public boolean xpMode = false;
    public boolean miningMode = false;
    public boolean woodMode = false;
    public boolean lavawaterMode = false;
    public boolean enderPortalMode = false;
    public boolean achievementsMode = false;

    public boolean farmMode = false;
    public boolean mobMode = false;

    /// Better Spheres
    public boolean betterSpheresEnabled = false;

    public boolean holyWorldSpheresEnabled = false;

    public boolean sphereCerberusEnabled = false;
    public boolean sphereFlashEnabled = false;
    public boolean sphereImmortalityEnabled = false;
    public boolean sphereArmortalityEnabled = false;
    public boolean sphereEternityEnabled = false;
    public boolean sphereStingerEnabled = false;

    public boolean sphereDefaultEnabled = false;
    public boolean sphereEpicEnabled = false;
    public boolean sphereLegendaryEnabled = false;
    public boolean sphereMythicEnabled = false;

    public boolean sphereSpeedEnabled = false;
    public boolean sphereMinerEnabled = false;
    public boolean spherePvPEnabled = false;

    public boolean coloredParametersEnabled = false;
    public boolean coloredNamesEnabled = false;
    public boolean goldenSpheresEnabled = false;

    /// Shulker Particles
    public boolean shulkerParticlesEnabled = false;

    public boolean shulkerConstantEnabled = false;
    public boolean shulkerBreakingEnabled = false;
    public boolean shulkerVanillaBreakingEnabled = false;
    public boolean shulkerConstantDependence = false;
    public boolean shulkerBreakingDependence = false;
    public int shulkerConstantColor = 0xFFFFFF;
    public int shulkerBreakingColor = 0xFFFFFF;

    /// Custom Fog
    private boolean customFogEnabled = false;

    private int customFogRange = 16;
    private int customFogColor = 0xFFFFFF;
    private boolean noFogEnabled = false;
    public boolean biomeFogEnabled = false;
    public boolean nightVisionEnabled = false;
    public Map<String, Boolean> biomeFogGroups = new HashMap<>();

    /// Custom Health
    public boolean customHealthEnabled = false;

    public int customHealthDuration = 5000;
    private int customHealthLocation = 0;
    public boolean customHealthScaling = false;
    private boolean customHealthHovering = false;
    public boolean customHealthPvPMode = false;
    private boolean customHealthDecimal = false;
    private boolean customHealthGoldenHearts = false;
    private boolean customHealthGoldenHeartsPlus = false;

    /// Конструктор и методы
    private BQoLConfig() {
        validateSettings();
        initBiomeGroups();
    }

    public static BQoLConfig getInstance() {
        if (instance == null) instance = load();
        return instance;
    }

    public static BQoLConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                BQoLConfig config = GSON.fromJson(reader, BQoLConfig.class);
                config.validateSettings();
                config.initBiomeGroups();
                return config;
            } catch (IOException e) {
                BQoL.LOGGER.error("Failed to load BQoL config", e);
            }
        }
        BQoLConfig config = new BQoLConfig();
        config.save();
        return config;
    }

    public void save() {
        validateSettings();
        try {
            if (!CONFIG_FILE.getParentFile().exists()) CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
                BQoL.LOGGER.info("Saved BQoL config to {}", CONFIG_FILE.getAbsolutePath());
            }
        } catch (IOException e) {
            BQoL.LOGGER.error("Failed to save BQoL config", e);
        }
    }

    private void validateSettings() {
        customHealthDuration = Math.max(3000, Math.min(30000, customHealthDuration));
        customHealthLocation = Math.max(0, Math.min(3, customHealthLocation));

        pvpTimerDuration = Math.max(5000, Math.min(60000, pvpTimerDuration));


        if (customHealthGoldenHeartsPlus && !customHealthGoldenHearts) {
            customHealthGoldenHeartsPlus = false;
        }
    }

    private void initBiomeGroups() {
        if (biomeFogGroups.isEmpty()) {
            String[] groups = {"KrimsonForest", "NetherWastes", "BasaltDeltas", "WarpedForest",
                    "SoulSand", "GrowthTaiga", "Mushrooms", "Snow",
                    "Desert", "Savanna", "Mesa", "End"};
            for (String g : groups) {
                biomeFogGroups.put(g, true);
            }
        }
    }

    /// BetterSprint
    public boolean isBetterSprintEnabled() { return betterSprintEnabled; }
    public void setBetterSprintEnabled(boolean enabled) {
        this.betterSprintEnabled = enabled;
    }

    public boolean isBetterSprintDefault() { return betterSprintDefault; }
    public void setBetterSprintDefault(boolean enabled) {
        this.betterSprintDefault = enabled;
        if (enabled) { this.betterSprintTree = false; this.betterSprintPvP = false; }
    }
    public boolean isBetterSprintPvP() { return betterSprintPvP; }
    public void setBetterSprintPvP(boolean enabled) {
        this.betterSprintPvP = enabled;
        if (enabled) { this.betterSprintDefault = false; this.betterSprintTree = false; }
    }
    public boolean isBetterSprintTree() { return betterSprintTree; }
    public void setBetterSprintTree(boolean enabled) {
        this.betterSprintTree = enabled;
        if (enabled) { this.betterSprintDefault = false; this.betterSprintPvP = false; }
    }
    public boolean isBetterSprintStairUp() { return betterSprintStairUp; }
    public void setBetterSprintStairUp(boolean enabled) {
        this.betterSprintStairUp = enabled;
    }
    public boolean isBetterSprintWaterSprint() { return betterSprintWaterSprint; }
    public void setBetterSprintWaterSprint(boolean enabled) {
        this.betterSprintWaterSprint = enabled;
    }

    /// Better Sounds
    public boolean isBetterSoundsEnabled() { return betterSoundsEnabled; }
    public void setBetterSoundsEnabled(boolean enabled) { this.betterSoundsEnabled = enabled; save(); }

    public boolean isExplosionMode() { return explosionMode; }
    public void setExplosionMode(boolean enabled) { this.explosionMode = enabled; save(); }
    public boolean isEnderDragonMode() { return enderDragonMode; }
    public void setEnderDragonMode(boolean enabled) { this.enderDragonMode = enabled; save(); }
    public boolean isPistonMode() { return pistonMode; }
    public void setPistonMode(boolean enabled) { this.pistonMode = enabled; save(); }
    public boolean isIceMode() { return iceMode; }
    public void setIceMode(boolean enabled) { this.iceMode = enabled; save(); }
    public boolean isVillagerMode() { return villagerMode; }
    public void setVillagerMode(boolean enabled) { this.villagerMode = enabled; save(); }
    public boolean isMoodMode() { return moodMode; }
    public void setMoodMode(boolean enabled) { this.moodMode = enabled; save(); }
    public boolean isThunderMode() { return thunderMode; }
    public void setThunderMode(boolean enabled) { this.thunderMode = enabled; save(); }
    public boolean isFireMode() { return fireMode; }
    public void setFireMode(boolean enabled) { this.fireMode = enabled; save(); }
    public boolean isEatMode() { return eatMode; }
    public void setEatMode(boolean enabled) { this.eatMode = enabled; save(); }
    public boolean isDrinkMode() { return drinkMode; }
    public void setDrinkMode(boolean enabled) { this.drinkMode = enabled; save(); }
    public boolean isHitsMode() { return hitsMode; }
    public void setHitsMode(boolean enabled) { this.hitsMode = enabled; save(); }
    public boolean isStorageMode() { return storageMode; }
    public void setStorageMode(boolean enabled) { this.storageMode = enabled; save(); }
    public boolean isGrassMode() { return grassMode; }
    public void setGrassMode(boolean enabled) { this.grassMode = enabled; save(); }
    public boolean isTotemMode() { return totemMode; }
    public void setTotemMode(boolean enabled) { this.totemMode = enabled; save(); }
    public boolean isAnvilMode() { return anvilMode; }
    public void setAnvilMode(boolean enabled) { this.anvilMode = enabled; save(); }
    public boolean isXpMode() { return xpMode; }
    public void setXpMode(boolean enabled) { this.xpMode = enabled; save(); }
    public boolean isMiningMode() { return miningMode; }
    public void setMiningMode(boolean v) { this.miningMode = v; save(); }
    public boolean isWoodMode() { return woodMode; }
    public void setWoodMode(boolean v) { this.woodMode = v; save(); }
    public boolean isLavaWaterMode() { return lavawaterMode; }
    public void setLavaWaterMode(boolean v) { this.lavawaterMode = v; save(); }
    public boolean isEnderPortalMode() { return enderPortalMode; }
    public void setEnderPortalMode(boolean enabled) { this.enderPortalMode = enabled; save(); }
    public boolean isAchievementsMode() { return achievementsMode; }
    public void setAchievementsMode(boolean enabled) { this.achievementsMode = enabled; save(); }

    public boolean isFarmMode() { return farmMode; }
    public void setFarmMode(boolean enabled) { this.farmMode = enabled; save(); }
    public boolean isMobMode() { return mobMode; }
    public void setMobMode(boolean enabled) { this.mobMode = enabled; save(); }

    /// Better Spheres
    public boolean isBetterSpheresEnabled() { return betterSpheresEnabled; }
    public void setBetterSpheresEnabled(boolean enabled) { this.betterSpheresEnabled = enabled; save(); }

    public boolean isHolyWorldSpheresEnabled() { return holyWorldSpheresEnabled; }
    public void setHolyWorldSpheresEnabled(boolean enabled) { this.holyWorldSpheresEnabled = enabled; save(); }

    public boolean isSphereCerberusEnabled() { return sphereCerberusEnabled; }
    public void setSphereCerberusEnabled(boolean enabled) { this.sphereCerberusEnabled = enabled; save(); }
    public boolean isSphereFlashEnabled() { return sphereFlashEnabled; }
    public void setSphereFlashEnabled(boolean enabled) { this.sphereFlashEnabled = enabled; save(); }
    public boolean isSphereImmortalityEnabled() { return sphereImmortalityEnabled; }
    public void setSphereImmortalityEnabled(boolean enabled) { this.sphereImmortalityEnabled = enabled; save(); }
    public boolean isSphereArmortalityEnabled() { return sphereArmortalityEnabled; }
    public void setSphereArmortalityEnabled(boolean enabled) { this.sphereArmortalityEnabled = enabled; save(); }
    public boolean isSphereEternityEnabled() { return sphereEternityEnabled; }
    public void setSphereEternityEnabled(boolean enabled) { this.sphereEternityEnabled = enabled; save(); }
    public boolean isSphereStingerEnabled() { return sphereStingerEnabled; }
    public void setSphereStingerEnabled(boolean enabled) { this.sphereStingerEnabled = enabled; save(); }

    public boolean isSphereMythicEnabled() { return sphereMythicEnabled; }
    public void setSphereMythicEnabled(boolean enabled) { this.sphereMythicEnabled = enabled; save(); }
    public boolean isSphereLegendaryEnabled() { return sphereLegendaryEnabled; }
    public void setSphereLegendaryEnabled(boolean enabled) { this.sphereLegendaryEnabled = enabled; save(); }
    public boolean isSphereEpicEnabled() { return sphereEpicEnabled; }
    public void setSphereEpicEnabled(boolean enabled) { this.sphereEpicEnabled = enabled; save(); }
    public boolean isSphereDefaultEnabled() { return sphereDefaultEnabled; }
    public void setSphereDefaultEnabled(boolean enabled) { this.sphereDefaultEnabled = enabled; save(); }

    public boolean isSphereSpeedEnabled() { return sphereSpeedEnabled; }
    public void setSphereSpeedEnabled(boolean enabled) { this.sphereSpeedEnabled = enabled; save(); }
    public boolean isSphereMinerEnabled() { return sphereMinerEnabled; }
    public void setSphereMinerEnabled(boolean enabled) { this.sphereMinerEnabled = enabled; save(); }
    public boolean isSpherePvPEnabled() { return spherePvPEnabled; }
    public void setSpherePvPEnabled(boolean enabled) { this.spherePvPEnabled = enabled; save(); }

    public boolean isColoredParametersEnabled() { return coloredParametersEnabled; }
    public void setColoredParametersEnabled(boolean enabled) { this.coloredParametersEnabled = enabled; save(); }
    public boolean isColoredNamesEnabled() { return coloredNamesEnabled; }
    public void setColoredNamesEnabled(boolean enabled) { this.coloredNamesEnabled = enabled; save(); }
    public boolean isGoldenSpheresEnabled() { return goldenSpheresEnabled; }
    public void setGoldenSpheresEnabled(boolean enabled) { this.goldenSpheresEnabled = enabled; save(); }

    /// Shulker Paricles
    public boolean isShulkerParticlesEnabled() { return shulkerParticlesEnabled; }
    public void setShulkerParticlesEnabled(boolean enabled) { this.shulkerParticlesEnabled = enabled; save(); }

    public boolean isShulkerConstantEnabled() { return shulkerConstantEnabled; }
    public void setShulkerConstantEnabled(boolean enabled) { this.shulkerConstantEnabled = enabled; save(); }
    public boolean isShulkerBreakingEnabled() { return shulkerBreakingEnabled; }
    public void setShulkerBreakingEnabled(boolean enabled) { this.shulkerBreakingEnabled = enabled; save(); }
    public boolean isShulkerVanillaBreakingEnabled() { return shulkerVanillaBreakingEnabled; }
    public void setShulkerVanillaBreakingEnabled(boolean enabled) { this.shulkerVanillaBreakingEnabled = enabled; save(); }
    public boolean isShulkerConstantDependence() { return shulkerConstantDependence; }
    public void setShulkerConstantDependence(boolean enabled) { this.shulkerConstantDependence = enabled; save(); }
    public boolean isShulkerBreakingDependence() { return shulkerBreakingDependence; }
    public void setShulkerBreakingDependence(boolean enabled) { this.shulkerBreakingDependence = enabled; save(); }
    public int getShulkerConstantColor() { return shulkerConstantColor; }
    public void setShulkerConstantColor(int color) { this.shulkerConstantColor = color; save(); }
    public int getShulkerBreakingColor() { return shulkerBreakingColor; }
    public void setShulkerBreakingColor(int color) { this.shulkerBreakingColor = color; save(); }

    /// Custom Fog
    public boolean isCustomFogEnabled() { return customFogEnabled; }
    public void setCustomFogEnabled(boolean enabled) { this.customFogEnabled = enabled; save(); }

    public int getCustomFogRange() { return customFogRange; }
    public void setCustomFogRange(int range) { this.customFogRange = MathHelper.clamp(range, 0, 32); save(); }
    public int getCustomFogColor() { return customFogColor; }
    public void setCustomFogColor(int color) { this.customFogColor = color; save(); }
    public boolean isNoFogEnabled() { return noFogEnabled; }
    public void setNoFogEnabled(boolean enabled) { this.noFogEnabled = enabled; save(); }
    public boolean isNightVisionEnabled() { return nightVisionEnabled; }
    public void setNightVisionEnabled(boolean enabled) { this.nightVisionEnabled = enabled; save(); }
    public boolean isBiomeFogEnabled() { return biomeFogEnabled; }
    public void setBiomeFogEnabled(boolean enabled) { this.biomeFogEnabled = enabled; save(); }
    public boolean isBiomeGroupEnabled(String group) { return biomeFogGroups.getOrDefault(group, false); }
    public void setBiomeGroupEnabled(String group, boolean enabled) { biomeFogGroups.put(group, enabled); save(); }

    /// Custom Health
    public boolean isCustomHealthEnabled() { return customHealthEnabled; }
    public void setCustomHealthEnabled(boolean enabled) { this.customHealthEnabled = enabled; save(); }

    public int getCustomHealthDuration() { return customHealthDuration; }
    public void setCustomHealthDuration(int duration) { this.customHealthDuration = Math.max(3000, Math.min(30000, duration)); save(); }
    public int getCustomHealthLocation() { return customHealthLocation; }
    public void setCustomHealthLocation(int loc) { this.customHealthLocation = Math.max(0, Math.min(3, loc)); save(); }
    public boolean isCustomHealthScaling() { return customHealthScaling; }
    public void setCustomHealthScaling(boolean scaling) { this.customHealthScaling = scaling; save(); }
    public boolean isCustomHealthHovering() { return customHealthHovering; }
    public void setCustomHealthHovering(boolean hover) { this.customHealthHovering = hover; save(); }
    public boolean isCustomHealthPvPMode() { return customHealthPvPMode; }
    public void setCustomHealthPvPMode(boolean enabled) { this.customHealthPvPMode = enabled; save(); }
    public boolean isCustomHealthDecimal() { return customHealthDecimal; }
    public void setCustomHealthDecimal(boolean decimal) { this.customHealthDecimal = decimal; save(); }
    public boolean isCustomHealthGoldenHearts() { return customHealthGoldenHearts; }
    public void setCustomHealthGoldenHearts(boolean enabled) { this.customHealthGoldenHearts = enabled; if (!enabled) { this.customHealthGoldenHeartsPlus = false; } save(); }
    public boolean isCustomHealthGoldenHeartsPlus() { return customHealthGoldenHeartsPlus; }
    public void setCustomHealthGoldenHeartsPlus(boolean enabled) { this.customHealthGoldenHeartsPlus = enabled && this.customHealthGoldenHearts; save(); }

    public boolean isDebugMode() { return debugMode; }

    public int getPvpTimerDuration() { return pvpTimerDuration; }
    public void setPvpTimerDuration(int duration) { this.pvpTimerDuration = Math.max(5000, Math.min(60000, duration)); save(); }

    public String toJson() { return GSON.toJson(this); }

    public static BQoLConfig fromJson(String json) {
        BQoLConfig config = GSON.fromJson(json, BQoLConfig.class);

        if (config == null) {
            return new BQoLConfig();
        }

        config.validateSettings();
        config.initBiomeGroups();

        return config;
    }

    public BQoLConfig copy() { return fromJson(toJson()); }

    public void resetToDefaults() {
        BQoLConfig defaults = new BQoLConfig();

        this.debugMode = defaults.debugMode;
        this.pvpTimerDuration = defaults.pvpTimerDuration;

        /// BetterSprint
        this.betterSprintEnabled = defaults.betterSprintEnabled;

        this.betterSprintDefault = defaults.betterSprintDefault;
        this.betterSprintPvP = defaults.betterSprintPvP;
        this.betterSprintTree = defaults.betterSprintTree;
        this.betterSprintStairUp = defaults.betterSprintStairUp;
        this.betterSprintWaterSprint = defaults.betterSprintWaterSprint;

        /// Better Sounds
        this.betterSoundsEnabled = defaults.betterSoundsEnabled;

        this.explosionMode = defaults.explosionMode;
        this.enderDragonMode = defaults.enderDragonMode;
        this.pistonMode = defaults.pistonMode;
        this.iceMode = defaults.iceMode;
        this.villagerMode = defaults.villagerMode;
        this.moodMode = defaults.moodMode;
        this.thunderMode = defaults.thunderMode;
        this.fireMode = defaults.fireMode;
        this.eatMode = defaults.eatMode;
        this.drinkMode = defaults.drinkMode;
        this.hitsMode = defaults.hitsMode;
        this.storageMode = defaults.storageMode;
        this.grassMode = defaults.grassMode;
        this.totemMode = defaults.totemMode;
        this.anvilMode = defaults.anvilMode;
        this.xpMode = defaults.xpMode;
        this.miningMode = defaults.miningMode;
        this.woodMode = defaults.woodMode;
        this.lavawaterMode = defaults.lavawaterMode;
        this.enderPortalMode = defaults.enderPortalMode;
        this.achievementsMode = defaults.achievementsMode;

        this.farmMode = defaults.farmMode;
        this.mobMode = defaults.mobMode;

        /// Better Spheres
        this.betterSpheresEnabled = defaults.betterSpheresEnabled;

        this.sphereCerberusEnabled = defaults.sphereCerberusEnabled;
        this.sphereFlashEnabled = defaults.sphereFlashEnabled;
        this.sphereImmortalityEnabled = defaults.sphereImmortalityEnabled;
        this.sphereArmortalityEnabled = defaults.sphereArmortalityEnabled;
        this.sphereEternityEnabled = defaults.sphereEternityEnabled;
        this.sphereStingerEnabled = defaults.sphereStingerEnabled;

        this.sphereMythicEnabled = defaults.sphereMythicEnabled;
        this.sphereLegendaryEnabled = defaults.sphereLegendaryEnabled;
        this.sphereEpicEnabled = defaults.sphereEpicEnabled;
        this.sphereDefaultEnabled = defaults.sphereDefaultEnabled;
        this.holyWorldSpheresEnabled = defaults.holyWorldSpheresEnabled;

        this.sphereSpeedEnabled = defaults.sphereSpeedEnabled;
        this.sphereMinerEnabled = defaults.sphereMinerEnabled;
        this.spherePvPEnabled = defaults.spherePvPEnabled;

        this.coloredParametersEnabled = defaults.coloredParametersEnabled;
        this.coloredNamesEnabled = defaults.coloredNamesEnabled;
        this.goldenSpheresEnabled = defaults.goldenSpheresEnabled;

        /// Shulker Particles
        this.shulkerParticlesEnabled = defaults.shulkerParticlesEnabled;

        this.shulkerConstantEnabled = defaults.shulkerConstantEnabled;
        this.shulkerBreakingEnabled = defaults.shulkerBreakingEnabled;
        this.shulkerVanillaBreakingEnabled = defaults.shulkerVanillaBreakingEnabled;
        this.shulkerConstantDependence = defaults.shulkerConstantDependence;
        this.shulkerBreakingDependence = defaults.shulkerBreakingDependence;
        this.shulkerConstantColor = defaults.shulkerConstantColor;
        this.shulkerBreakingColor = defaults.shulkerBreakingColor;

        /// Custom Fog
        this.customFogEnabled = defaults.customFogEnabled;

        this.customFogRange = defaults.customFogRange;
        this.customFogColor = defaults.customFogColor;
        this.noFogEnabled = defaults.noFogEnabled;
        this.nightVisionEnabled = defaults.nightVisionEnabled;
        this.biomeFogEnabled = defaults.biomeFogEnabled;
        this.biomeFogGroups.clear();
        this.biomeFogGroups.putAll(defaults.biomeFogGroups);

        /// Custom Health
        this.customHealthEnabled = defaults.customHealthEnabled;

        this.customHealthDuration = defaults.customHealthDuration;
        this.customHealthLocation = defaults.customHealthLocation;
        this.customHealthScaling = defaults.customHealthScaling;
        this.customHealthHovering = defaults.customHealthHovering;
        this.customHealthPvPMode = defaults.customHealthPvPMode;
        this.customHealthDecimal = defaults.customHealthDecimal;
        this.customHealthGoldenHearts = defaults.customHealthGoldenHearts;
        this.customHealthGoldenHeartsPlus = defaults.customHealthGoldenHeartsPlus;

        validateSettings();
        save();
    }
}