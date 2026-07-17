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
    private static final File config_file = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "bqol_config.json"
    );

    private static BQoLConfig instance;

    /// ОБЩИЕ НАСТРОЙКИ
    public boolean debug = false;
    public int pvpTimerDuration = 30000;

    /// Better Sprint
    public boolean betterSprintEnabled = false;

    public boolean betterSprintDefaultMode = false;
    public boolean betterSprintPvPMode = false;
    public boolean betterSprintTreeMode = false;
    public boolean betterSprintStairUp = false;
    public boolean betterSprintWaterSprint = false;

    /// Better Sounds
    public boolean betterSoundsEnabled = false;

    public boolean betterSoundsExplosion = false;
    public boolean betterSoundsEnderDragon = false;
    public boolean betterSoundsPiston = false;
    public boolean betterSoundsIce = false;
    public boolean betterSoundsVillager = false;
    public boolean betterSoundsMood = false;
    public boolean betterSoundsThunder = false;
    public boolean betterSoundsFire = false;
    public boolean betterSoundsEat = false;
    public boolean betterSoundsDrink = false;
    public boolean betterSoundsHit = false;
    public boolean betterSoundsStorage = false;
    public boolean betterSoundsGrass = false;
    public boolean betterSoundsTotem = false;
    public boolean betterSoundsAnvil = false;
    public boolean betterSoundsXp = false;
    public boolean betterSoundsMining = false;
    public boolean betterSoundsWood = false;
    public boolean betterSoundsLavaWater = false;
    public boolean betterSoundsEnderPortal = false;
    public boolean betterSoundsAchievements = false;

    public boolean betterSoundsFarm = false;
    public boolean betterSoundsMob = false;

    /// Better Spheres
    public boolean betterSpheresEnabled = false;

    public boolean holyWorldSpheresEnabled = false;

    public boolean sphereCerberus = false;
    public boolean sphereFlash = false;
    public boolean sphereImmortality = false;
    public boolean sphereArmortality = false;
    public boolean sphereEternity = false;
    public boolean sphereStinger = false;

    public boolean holyWorldSphereDefault = false;
    public boolean holyWorldSphereEpic = false;
    public boolean holyWorldSphereLegendary = false;
    public boolean holyWorldSphereMythic = false;

    public boolean holyWorldSphereSpeed = false;
    public boolean holyWorldSphereMiner = false;
    public boolean holyWorldSpherePvP = false;

    public boolean coloredParameters = false;
    public boolean coloredNames = false;
    public boolean goldenSpheres = false;

    /// Shulker Particles
    public boolean shulkerParticlesEnabled = false;

    public boolean shulkerParticlesConstant = false;
    public boolean shulkerParticlesBreaking = false;
    public boolean shulkerParticlesVanillaBreaking = false;
    public boolean shulkerParticlesConstantDependence = false;
    public boolean shulkerParticlesBreakingDependence = false;
    public int shulkerParticlesConstantColor = 0xFFFFFF;
    public int shulkerParticlesBreakingColor = 0xFFFFFF;

    /// Custom Fog
    private boolean customFogEnabled = false;

    private int customFogRange = 16;
    private int customFogColor = 0xFFFFFF;
    private boolean noFog = false;
    public boolean biomeFog = false;
    public boolean nightVision = false;
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

    /// No Render
    public enum RenderMode {
        NO_RENDER,
        SMALL,
        FULL
    }

    public boolean noRenderEnabled = false;

    public boolean noRenderTotemOverlayEnabled = false;
    public boolean noRenderFireOverlayEnabled = false;
    public boolean noRenderWeatherEnabled = false;
    public boolean noRenderFireworksEnabled = false;
    public boolean noRenderPlayersEnabled = false;
    public boolean noRenderHandEnabled = false;

    public RenderMode noRenderTotemOverlay = RenderMode.FULL;
    public RenderMode noRenderFireOverlay = RenderMode.FULL;
    public RenderMode noRenderWeather = RenderMode.FULL;
    public RenderMode noRenderFireworks = RenderMode.FULL;
    public RenderMode noRenderPlayers = RenderMode.FULL;
    public RenderMode noRenderHand = RenderMode.FULL;

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
        if (config_file.exists()) {
            try (FileReader reader = new FileReader(config_file)) {

                BQoLConfig config = GSON.fromJson(reader, BQoLConfig.class);

                if (config == null) {
                    config = new BQoLConfig();
                } else {
                    config.validateSettings();
                    config.initBiomeGroups();
                }

                return config;

            } catch (Exception e) {
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
            if (!config_file.getParentFile().exists()) {
                config_file.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(config_file)) {
                GSON.toJson(this, writer);
                BQoL.LOGGER.info("Saved BQoL config to {}", config_file.getAbsolutePath());
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

    public boolean isBetterSprintDefaultModeEnabled() { return betterSprintDefaultMode; }
    public void setBetterSprintDefaultModeEnabled(boolean enabled) {
        this.betterSprintDefaultMode = enabled;
        if (enabled) { this.betterSprintTreeMode = false; this.betterSprintPvPMode = false; }
    }
    public boolean isBetterSprintPvPModeEnabled() { return betterSprintPvPMode; }
    public void setBetterSprintPvPModeEnabled(boolean enabled) {
        this.betterSprintPvPMode = enabled;
        if (enabled) { this.betterSprintDefaultMode = false; this.betterSprintTreeMode = false; }
    }
    public boolean isBetterSprintTreeModeEnabled() { return betterSprintTreeMode; }
    public void setBetterSprintTreeModeEnabled(boolean enabled) {
        this.betterSprintTreeMode = enabled;
        if (enabled) { this.betterSprintDefaultMode = false; this.betterSprintPvPMode = false; }
    }
    public boolean isBetterSprintStairUpEnabled() { return betterSprintStairUp; }
    public void setBetterSprintStairUpEnabled(boolean enabled) {
        this.betterSprintStairUp = enabled;
    }
    public boolean isBetterSprintWaterSprintEnabled() { return betterSprintWaterSprint; }
    public void setBetterSprintWaterSprintEnabled(boolean enabled) {
        this.betterSprintWaterSprint = enabled;
    }

    /// Better Sounds
    public boolean isBetterSoundsEnabled() { return betterSoundsEnabled; }
    public void setBetterSoundsEnabled(boolean enabled) { this.betterSoundsEnabled = enabled; save(); }

    public boolean isBetterSoundsExplosion() { return betterSoundsExplosion; }
    public void setBetterSoundsExplosion(boolean enabled) { this.betterSoundsExplosion = enabled; save(); }
    public boolean isBetterSoundsEnderDragon() { return betterSoundsEnderDragon; }
    public void setBetterSoundsEnderDragon(boolean enabled) { this.betterSoundsEnderDragon = enabled; save(); }
    public boolean isBetterSoundsPiston() { return betterSoundsPiston; }
    public void setBetterSoundsPiston(boolean enabled) { this.betterSoundsPiston = enabled; save(); }
    public boolean isBetterSoundsIce() { return betterSoundsIce; }
    public void setBetterSoundsIce(boolean enabled) { this.betterSoundsIce = enabled; save(); }
    public boolean isBetterSoundsVillager() { return betterSoundsVillager; }
    public void setBetterSoundsVillager(boolean enabled) { this.betterSoundsVillager = enabled; save(); }
    public boolean isBetterSoundsMood() { return betterSoundsMood; }
    public void setBetterSoundsMood(boolean enabled) { this.betterSoundsMood = enabled; save(); }
    public boolean isBetterSoundsThunder() { return betterSoundsThunder; }
    public void setBetterSoundsThunder(boolean enabled) { this.betterSoundsThunder = enabled; save(); }
    public boolean isBetterSoundsFire() { return betterSoundsFire; }
    public void setBetterSoundsFire(boolean enabled) { this.betterSoundsFire = enabled; save(); }
    public boolean isBetterSoundsEat() { return betterSoundsEat; }
    public void setBetterSoundsEat(boolean enabled) { this.betterSoundsEat = enabled; save(); }
    public boolean isBetterSoundsDrink() { return betterSoundsDrink; }
    public void setBetterSoundsDrink(boolean enabled) { this.betterSoundsDrink = enabled; save(); }
    public boolean isBetterSoundsHit() { return betterSoundsHit; }
    public void setBetterSoundsHit(boolean enabled) { this.betterSoundsHit = enabled; save(); }
    public boolean isBetterSoundsStorage() { return betterSoundsStorage; }
    public void setBetterSoundsStorage(boolean enabled) { this.betterSoundsStorage = enabled; save(); }
    public boolean isBetterSoundsGrass() { return betterSoundsGrass; }
    public void setBetterSoundsGrass(boolean enabled) { this.betterSoundsGrass = enabled; save(); }
    public boolean isBetterSoundsTotem() { return betterSoundsTotem; }
    public void setBetterSoundsTotem(boolean enabled) { this.betterSoundsTotem = enabled; save(); }
    public boolean isBetterSoundsAnvil() { return betterSoundsAnvil; }
    public void setBetterSoundsAnvil(boolean enabled) { this.betterSoundsAnvil = enabled; save(); }
    public boolean isBetterSoundsXp() { return betterSoundsXp; }
    public void setBetterSoundsXp(boolean enabled) { this.betterSoundsXp = enabled; save(); }
    public boolean isBetterSoundsMining() { return betterSoundsMining; }
    public void setBetterSoundsMining(boolean v) { this.betterSoundsMining = v; save(); }
    public boolean isBetterSoundsWood() { return betterSoundsWood; }
    public void setBetterSoundsWood(boolean v) { this.betterSoundsWood = v; save(); }
    public boolean isBetterSoundsLavaWater() { return betterSoundsLavaWater; }
    public void setBetterSoundsLavaWater(boolean v) { this.betterSoundsLavaWater = v; save(); }
    public boolean isBetterSoundsEnderPortal() { return betterSoundsEnderPortal; }
    public void setBetterSoundsEnderPortal(boolean enabled) { this.betterSoundsEnderPortal = enabled; save(); }
    public boolean isBetterSoundsAchievements() { return betterSoundsAchievements; }
    public void setBetterSoundsAchievements(boolean enabled) { this.betterSoundsAchievements = enabled; save(); }

    public boolean isBetterSoundsFarm() { return betterSoundsFarm; }
    public void setBetterSoundsFarm(boolean enabled) { this.betterSoundsFarm = enabled; save(); }
    public boolean isBetterSoundsMob() { return betterSoundsMob; }
    public void setBetterSoundsMob(boolean enabled) { this.betterSoundsMob = enabled; save(); }

    /// Better Spheres
    public boolean isBetterSpheresEnabled() { return betterSpheresEnabled; }
    public void setBetterSpheresEnabled(boolean enabled) { this.betterSpheresEnabled = enabled; save(); }

    public boolean isHolyWorldSpheresEnabled() { return holyWorldSpheresEnabled; }
    public void setHolyWorldSpheresEnabled(boolean enabled) { this.holyWorldSpheresEnabled = enabled; save(); }

    public boolean isSphereCerberus() { return sphereCerberus; }
    public void setSphereCerberus(boolean enabled) { this.sphereCerberus = enabled; save(); }
    public boolean isSphereFlash() { return sphereFlash; }
    public void setSphereFlash(boolean enabled) { this.sphereFlash = enabled; save(); }
    public boolean isSphereImmortality() { return sphereImmortality; }
    public void setSphereImmortality(boolean enabled) { this.sphereImmortality = enabled; save(); }
    public boolean isSphereArmortality() { return sphereArmortality; }
    public void setSphereArmortality(boolean enabled) { this.sphereArmortality = enabled; save(); }
    public boolean isSphereEternity() { return sphereEternity; }
    public void setSphereEternity(boolean enabled) { this.sphereEternity = enabled; save(); }
    public boolean isSphereStinger() { return sphereStinger; }
    public void setSphereStinger(boolean enabled) { this.sphereStinger = enabled; save(); }

    public boolean isHolyWorldSphereMythic() { return holyWorldSphereMythic; }
    public void setHolyWorldSphereMythic(boolean enabled) { this.holyWorldSphereMythic = enabled; save(); }
    public boolean isHolyWorldSphereLegendary() { return holyWorldSphereLegendary; }
    public void setHolyWorldSphereLegendary(boolean enabled) { this.holyWorldSphereLegendary = enabled; save(); }
    public boolean isHolyWorldSphereEpic() { return holyWorldSphereEpic; }
    public void setHolyWorldSphereEpic(boolean enabled) { this.holyWorldSphereEpic = enabled; save(); }
    public boolean isHolyWorldSphereDefault() { return holyWorldSphereDefault; }
    public void setHolyWorldSphereDefault(boolean enabled) { this.holyWorldSphereDefault = enabled; save(); }

    public boolean isHolyWorldSphereSpeed() { return holyWorldSphereSpeed; }
    public void setHolyWorldSphereSpeed(boolean enabled) { this.holyWorldSphereSpeed = enabled; save(); }
    public boolean isHolyWorldSphereMiner() { return holyWorldSphereMiner; }
    public void setHolyWorldSphereMiner(boolean enabled) { this.holyWorldSphereMiner = enabled; save(); }
    public boolean isHolyWorldSpherePvP() { return holyWorldSpherePvP; }
    public void setHolyWorldSpherePvP(boolean enabled) { this.holyWorldSpherePvP = enabled; save(); }

    public boolean isColoredParameters() { return coloredParameters; }
    public void setColoredParameters(boolean enabled) { this.coloredParameters = enabled; save(); }
    public boolean isColoredNames() { return coloredNames; }
    public void setColoredNames(boolean enabled) { this.coloredNames = enabled; save(); }
    public boolean isGoldenSpheres() { return goldenSpheres; }
    public void setGoldenSpheres(boolean enabled) { this.goldenSpheres = enabled; save(); }

    /// Shulker Paricles
    public boolean isShulkerParticlesEnabled() { return shulkerParticlesEnabled; }
    public void setShulkerParticlesEnabled(boolean enabled) { this.shulkerParticlesEnabled = enabled; save(); }

    public boolean isShulkerParticlesConstant() { return shulkerParticlesConstant; }
    public void setShulkerParticlesConstant(boolean enabled) { this.shulkerParticlesConstant = enabled; save(); }
    public boolean isShulkerParticlesBreaking() { return shulkerParticlesBreaking; }
    public void setShulkerParticlesBreaking(boolean enabled) { this.shulkerParticlesBreaking = enabled; save(); }
    public boolean isShulkerParticlesVanillaBreaking() { return shulkerParticlesVanillaBreaking; }
    public void setShulkerParticlesVanillaBreaking(boolean enabled) { this.shulkerParticlesVanillaBreaking = enabled; save(); }
    public boolean isShulkerParticlesConstantDependence() { return shulkerParticlesConstantDependence; }
    public void setShulkerParticlesConstantDependence(boolean enabled) { this.shulkerParticlesConstantDependence = enabled; save(); }
    public boolean isShulkerParticlesBreakingDependence() { return shulkerParticlesBreakingDependence; }
    public void setShulkerParticlesBreakingDependence(boolean enabled) { this.shulkerParticlesBreakingDependence = enabled; save(); }
    public int getShulkerParticlesConstantColor() { return shulkerParticlesConstantColor; }
    public void setShulkerParticlesConstantColor(int color) { this.shulkerParticlesConstantColor = color; save(); }
    public int getShulkerParticlesBreakingColor() { return shulkerParticlesBreakingColor; }
    public void setShulkerParticlesBreakingColor(int color) { this.shulkerParticlesBreakingColor = color; save(); }

    /// Custom Fog
    public boolean isCustomFogEnabled() { return customFogEnabled; }
    public void setCustomFogEnabled(boolean enabled) { this.customFogEnabled = enabled; save(); }

    public int getCustomFogRange() { return customFogRange; }
    public void setCustomFogRange(int range) { this.customFogRange = MathHelper.clamp(range, 0, 32); save(); }
    public int getCustomFogColor() { return customFogColor; }
    public void setCustomFogColor(int color) { this.customFogColor = color; save(); }
    public boolean isNoFog() { return noFog; }
    public void setNoFog(boolean enabled) { this.noFog = enabled; save(); }
    public boolean isNightVision() { return nightVision; }
    public void setNightVision(boolean enabled) { this.nightVision = enabled; save(); }
    public boolean isBiomeFog() { return biomeFog; }
    public void setBiomeFog(boolean enabled) { this.biomeFog = enabled; save(); }
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

    /// No Render
    public boolean isNoRenderEnabled() { return noRenderEnabled; }
    public void setNoRenderEnabled(boolean enabled) { this.noRenderEnabled = enabled; save(); }
    public boolean isNoRenderTotemOverlayEnabled() {
        return noRenderTotemOverlayEnabled;
    }

    public void setNoRenderTotemOverlayEnabled(boolean enabled) {
        this.noRenderTotemOverlayEnabled = enabled;
        save();
    }

    public RenderMode getNoRenderTotemOverlay() {
        return noRenderTotemOverlay;
    }

    public void setNoRenderTotemOverlay(RenderMode mode) {
        this.noRenderTotemOverlay = mode;
        save();
    }
    public boolean isNoRenderFireOverlayEnabled() {
        return noRenderFireOverlayEnabled;
    }

    public void setNoRenderFireOverlayEnabled(boolean enabled) {
        this.noRenderFireOverlayEnabled = enabled;
        save();
    }

    public RenderMode getNoRenderFireOverlay() {
        return noRenderFireOverlay;
    }

    public void setNoRenderFireOverlay(RenderMode mode) {
        this.noRenderFireOverlay = mode;
        save();
    }

    public boolean isNoRenderWeatherEnabled() {
        return noRenderWeatherEnabled;
    }

    public void setNoRenderWeatherEnabled(boolean enabled) {
        noRenderWeatherEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderWeather() {
        return noRenderWeather;
    }

    public void setNoRenderWeather(RenderMode mode) {
        noRenderWeather = mode;
        save();
    }
    public boolean isNoRenderFireworksEnabled() {
        return noRenderFireworksEnabled;
    }

    public void setNoRenderFireworksEnabled(boolean enabled) {
        this.noRenderFireworksEnabled = enabled;
        save();
    }

    public RenderMode getNoRenderFireworks() {
        return noRenderFireworks;
    }

    public void setNoRenderFireworks(RenderMode mode) {
        this.noRenderFireworks = mode;
        save();
    }
    public boolean isNoRenderPlayersEnabled() {
        return noRenderPlayersEnabled;
    }

    public void setNoRenderPlayersEnabled(boolean enabled) {
        this.noRenderPlayersEnabled = enabled;
        save();
    }

    public RenderMode getNoRenderPlayers() {
        return noRenderPlayers;
    }

    public void setNoRenderPlayers(RenderMode mode) {
        this.noRenderPlayers = mode;
        save();
    }

    public boolean isNoRenderHandEnabled() {
        return noRenderHandEnabled;
    }

    public void setNoRenderHandEnabled(boolean enabled) {
        this.noRenderHandEnabled = enabled;
        save();
    }

    public RenderMode getNoRenderHand() {
        return noRenderHand;
    }

    public void setNoRenderHand(RenderMode mode) {
        this.noRenderHand = mode;
        save();
    }

    public boolean isDebugMode() { return debug; }

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

        this.debug = defaults.debug;
        this.pvpTimerDuration = defaults.pvpTimerDuration;

        /// BetterSprint
        this.betterSprintEnabled = defaults.betterSprintEnabled;

        this.betterSprintDefaultMode = defaults.betterSprintDefaultMode;
        this.betterSprintPvPMode = defaults.betterSprintPvPMode;
        this.betterSprintTreeMode = defaults.betterSprintTreeMode;
        this.betterSprintStairUp = defaults.betterSprintStairUp;
        this.betterSprintWaterSprint = defaults.betterSprintWaterSprint;

        /// Better Sounds
        this.betterSoundsEnabled = defaults.betterSoundsEnabled;

        this.betterSoundsExplosion = defaults.betterSoundsExplosion;
        this.betterSoundsEnderDragon = defaults.betterSoundsEnderDragon;
        this.betterSoundsPiston = defaults.betterSoundsPiston;
        this.betterSoundsIce = defaults.betterSoundsIce;
        this.betterSoundsVillager = defaults.betterSoundsVillager;
        this.betterSoundsMood = defaults.betterSoundsMood;
        this.betterSoundsThunder = defaults.betterSoundsThunder;
        this.betterSoundsFire = defaults.betterSoundsFire;
        this.betterSoundsEat = defaults.betterSoundsEat;
        this.betterSoundsDrink = defaults.betterSoundsDrink;
        this.betterSoundsHit = defaults.betterSoundsHit;
        this.betterSoundsStorage = defaults.betterSoundsStorage;
        this.betterSoundsGrass = defaults.betterSoundsGrass;
        this.betterSoundsTotem = defaults.betterSoundsTotem;
        this.betterSoundsAnvil = defaults.betterSoundsAnvil;
        this.betterSoundsXp = defaults.betterSoundsXp;
        this.betterSoundsMining = defaults.betterSoundsMining;
        this.betterSoundsWood = defaults.betterSoundsWood;
        this.betterSoundsLavaWater = defaults.betterSoundsLavaWater;
        this.betterSoundsEnderPortal = defaults.betterSoundsEnderPortal;
        this.betterSoundsAchievements = defaults.betterSoundsAchievements;

        this.betterSoundsFarm = defaults.betterSoundsFarm;
        this.betterSoundsMob = defaults.betterSoundsMob;

        /// Better Spheres
        this.betterSpheresEnabled = defaults.betterSpheresEnabled;

        this.sphereCerberus = defaults.sphereCerberus;
        this.sphereFlash = defaults.sphereFlash;
        this.sphereImmortality = defaults.sphereImmortality;
        this.sphereArmortality = defaults.sphereArmortality;
        this.sphereEternity = defaults.sphereEternity;
        this.sphereStinger = defaults.sphereStinger;

        this.holyWorldSphereMythic = defaults.holyWorldSphereMythic;
        this.holyWorldSphereLegendary = defaults.holyWorldSphereLegendary;
        this.holyWorldSphereEpic = defaults.holyWorldSphereEpic;
        this.holyWorldSphereDefault = defaults.holyWorldSphereDefault;
        this.holyWorldSpheresEnabled = defaults.holyWorldSpheresEnabled;

        this.holyWorldSphereSpeed = defaults.holyWorldSphereSpeed;
        this.holyWorldSphereMiner = defaults.holyWorldSphereMiner;
        this.holyWorldSpherePvP = defaults.holyWorldSpherePvP;

        this.coloredParameters = defaults.coloredParameters;
        this.coloredNames = defaults.coloredNames;
        this.goldenSpheres = defaults.goldenSpheres;

        /// Shulker Particles
        this.shulkerParticlesEnabled = defaults.shulkerParticlesEnabled;

        this.shulkerParticlesConstant = defaults.shulkerParticlesConstant;
        this.shulkerParticlesBreaking = defaults.shulkerParticlesBreaking;
        this.shulkerParticlesVanillaBreaking = defaults.shulkerParticlesVanillaBreaking;
        this.shulkerParticlesConstantDependence = defaults.shulkerParticlesConstantDependence;
        this.shulkerParticlesBreakingDependence = defaults.shulkerParticlesBreakingDependence;
        this.shulkerParticlesConstantColor = defaults.shulkerParticlesConstantColor;
        this.shulkerParticlesBreakingColor = defaults.shulkerParticlesBreakingColor;

        /// Custom Fog
        this.customFogEnabled = defaults.customFogEnabled;

        this.customFogRange = defaults.customFogRange;
        this.customFogColor = defaults.customFogColor;
        this.noFog = defaults.noFog;
        this.nightVision = defaults.nightVision;
        this.biomeFog = defaults.biomeFog;
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

        /// No Render
        this.noRenderTotemOverlayEnabled = defaults.noRenderTotemOverlayEnabled;
        this.noRenderFireOverlayEnabled = defaults.noRenderFireOverlayEnabled;
        this.noRenderWeatherEnabled = defaults.noRenderWeatherEnabled;
        this.noRenderFireworksEnabled = defaults.noRenderFireworksEnabled;
        this.noRenderPlayersEnabled = defaults.noRenderPlayersEnabled;
        this.noRenderHandEnabled = defaults.noRenderHandEnabled;

        this.noRenderEnabled = defaults.noRenderEnabled;
        this.noRenderTotemOverlay = defaults.noRenderTotemOverlay;
        this.noRenderFireOverlay = defaults.noRenderFireOverlay;
        this.noRenderWeather = defaults.noRenderWeather;
        this.noRenderFireworks = defaults.noRenderFireworks;
        this.noRenderPlayers = defaults.noRenderPlayers;
        this.noRenderHand = defaults.noRenderHand;

        validateSettings();
        save();
    }
}