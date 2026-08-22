package com.zavk1n.bqol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zavk1n.bqol.BQoL;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    public boolean betterSoundsSwim = false;
    public boolean betterSoundsFall = false;
    public boolean betterSoundsChargeCrossbow = false;
    public boolean betterSoundsFireworks = false;
    public boolean betterSoundsEnderman = false;
    public boolean betterSoundsBlaze = false;
    public boolean betterSoundsBee = false;

    public boolean betterSoundsFarm = false;
    public boolean betterSoundsMob = false;

    /// Better Interact
    public boolean betterInteractEnabled = false;
    public boolean betterInteractClickThrough = false;
    public boolean betterInteractAntiSigns = false;
    public boolean betterInteractAutoSigns = false;
    public String betterInteractAutoSignsText = "";
    public boolean betterInteractSafeHarvest = false;

    /// Better Tnt
    public boolean betterTntEnabled = false;
    public boolean betterTntTimer = false;
    public boolean betterTntAlert = false;
    public int betterTntAlertPosition = 0;
    public boolean betterTntAlertShowXYZ = false;

    /// Better Holograms
    public boolean betterHologramsEnabled = false;
    public boolean betterHologramsVisibleArmorStand = false;
    public boolean betterHologramsAntiHolograms = false;

    /// Better Spheres
    public boolean betterSpheresEnabled = false;

    // HolyWorld
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

    public boolean hwGoldenSpheres = false;

    // ReallyWorld
    public boolean reallyWorldSpheresEnabled = false;

    public boolean sphereAir = false;
    public boolean sphereFire = false;
    public boolean sphereShine = false;
    public boolean sphereChaos = false;
    public boolean sphereWater = false;
    public boolean sphereGround = false;
    public boolean sphereGOD = false;
    public boolean sphereCocaCola = false;
    public boolean spherePepsi = false;
    public boolean sphereRedBull = false;
    public boolean sphereSprite = false;
    public boolean sphereFanta = false;
    public boolean spherePoseidon = false;
    public boolean sphereHades = false;
    public boolean sphereArmadillo = false;
    public boolean sphereBUNNY = false;
    public boolean sphereDHELPER = false;
    public boolean sphereDiscipline = false;

    public boolean headBatman = false;
    public boolean headVampire = false;
    public boolean headJack = false;
    public boolean headGrinch = false;
    public boolean headHydra = false;
    public boolean headIronMan = false;
    public boolean headCobra = false;
    public boolean headBunny = false;
    public boolean headPegasus = false;
    public boolean headPenguin = false;
    public boolean headGingerbread = false;
    public boolean headRudolph = false;
    public boolean headSanta = false;
    public boolean headHulk = false;
    public boolean headThor = false;
    public boolean headNutcracker = false;
    public boolean headElf = false;

    public boolean easterEgg = false;

    public boolean rwGoldenSpheres = false;

    /// Better Sky
    public boolean betterSkyEnabled = false;

    public boolean betterSkyColorEnabled = false;
    public int betterSkyColor = 0xFFFFFF;
    public long betterSkyTime = 6000;

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
    public boolean betterFogEnabled = false;

    public boolean noFog = false;
    public boolean nightVision = false;

    /// Custom Health
    public boolean customHealthEnabled = false;

    public int customHealthDuration = 5000;
    public int customHealthPosition = 0;
    public boolean customHealthScaling = false;
    public boolean customHealthHovering = false;
    public boolean customHealthPvPMode = false;
    public boolean customHealthDecimal = false;
    public boolean customHealthGoldenHearts = false;
    public boolean customHealthGoldenHeartsPlus = false;

    /// No Render
    public enum RenderMode {
        NO_RENDER,
        SMALL,
        FULL
    }

    public boolean noRenderEnabled = false;

    public boolean noRenderTotemOverlayEnabled = false;
    public boolean noRenderFireOverlayEnabled = false;
    public boolean noRenderTotemParticlesEnabled = false;
    public boolean noRenderPotionParticlesEnabled = false;
    public boolean noRenderExplosionEnabled = false;
    public boolean noRenderSmokeEnabled = false;
    public boolean noRenderBubblesEnabled = false;
    public boolean noRenderWeatherEnabled = false;
    public boolean noRenderArrowsEnabled = false;
    public boolean noRenderFireworksEnabled = false;
    public boolean noRenderNamesEnabled = false;
    public boolean noRenderPlayersEnabled = false;
    public boolean noRenderHandEnabled = false;

    public RenderMode noRenderTotemOverlay = RenderMode.FULL;
    public RenderMode noRenderFireOverlay = RenderMode.FULL;
    public RenderMode noRenderTotemParticles = RenderMode.FULL;
    public RenderMode noRenderPotionParticles = RenderMode.FULL;
    public RenderMode noRenderExplosion = RenderMode.FULL;
    public RenderMode noRenderSmoke = RenderMode.FULL;
    public RenderMode noRenderBubbles = RenderMode.FULL;
    public RenderMode noRenderWeather = RenderMode.FULL;
    public RenderMode noRenderArrows = RenderMode.FULL;
    public RenderMode noRenderFireworks = RenderMode.FULL;
    public RenderMode noRenderNames = RenderMode.FULL;
    public RenderMode noRenderPlayers = RenderMode.FULL;
    public RenderMode noRenderHand = RenderMode.FULL;

    /// Конструктор и методы
    private BQoLConfig() {
        validateSettings();
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
        betterTntAlertPosition = Math.max(0, Math.min(4, betterTntAlertPosition));
        customHealthDuration = Math.max(3000, Math.min(30000, customHealthDuration));
        customHealthPosition = Math.max(0, Math.min(3, customHealthPosition));

        pvpTimerDuration = Math.max(5000, Math.min(60000, pvpTimerDuration));

        if (customHealthGoldenHeartsPlus && !customHealthGoldenHearts) {
            customHealthGoldenHeartsPlus = false;
        }
    }

    /// BetterSprint
    public boolean isBetterSprintEnabled() { return betterSprintEnabled; }
    public void setBetterSprintEnabled(boolean enabled) {
        this.betterSprintEnabled = enabled; save();
    }

    public boolean isBetterSprintDefaultMode() { return betterSprintDefaultMode; }
    public void setBetterSprintDefaultMode(boolean enabled) {
        this.betterSprintDefaultMode = enabled;
        if (enabled) { this.betterSprintPvPMode = false; save(); }
    }
    public boolean isBetterSprintPvPMode() { return betterSprintPvPMode; }
    public void setBetterSprintPvPMode(boolean enabled) {
        this.betterSprintPvPMode = enabled;
        if (enabled) { this.betterSprintDefaultMode = false; save(); }
    }

    public boolean isBetterSprintStairUp() { return betterSprintStairUp; }
    public void setBetterSprintStairUp(boolean enabled) {
        this.betterSprintStairUp = enabled; save();
    }
    public boolean isBetterSprintWaterSprint() { return betterSprintWaterSprint; }
    public void setBetterSprintWaterSprint(boolean enabled) {
        this.betterSprintWaterSprint = enabled; save();
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
    public boolean isBetterSoundsSwim() { return betterSoundsSwim; }
    public void setBetterSoundsSwim(boolean enabled) { this.betterSoundsSwim = enabled; save(); }
    public boolean isBetterSoundsFall() { return betterSoundsFall; }
    public void setBetterSoundsFall(boolean enabled) { this.betterSoundsFall = enabled; save(); }
    public boolean isBetterSoundsChargeCrossbow() { return betterSoundsChargeCrossbow; }
    public void setBetterSoundsChargeCrossbow(boolean enabled) { this.betterSoundsChargeCrossbow = enabled; save(); }
    public boolean isBetterSoundsFireworks() { return betterSoundsFireworks; }
    public void setBetterSoundsFireworks(boolean enabled) { this.betterSoundsFireworks = enabled; save(); }
    public boolean isBetterSoundsEnderman() { return betterSoundsEnderman; }
    public void setBetterSoundsEnderman(boolean enabled) { this.betterSoundsEnderman = enabled; save(); }
    public boolean isBetterSoundsBlaze() { return betterSoundsBlaze; }
    public void setBetterSoundsBlaze(boolean enabled) { this.betterSoundsBlaze = enabled; save(); }
    public boolean isBetterSoundsBee() { return betterSoundsBee; }
    public void setBetterSoundsBee(boolean enabled) { this.betterSoundsBee = enabled; save(); }

    public boolean isBetterSoundsFarm() { return betterSoundsFarm; }
    public void setBetterSoundsFarm(boolean enabled) { this.betterSoundsFarm = enabled; save(); }
    public boolean isBetterSoundsMob() { return betterSoundsMob; }
    public void setBetterSoundsMob(boolean enabled) { this.betterSoundsMob = enabled; save(); }

    /// Better Interact
    public boolean isBetterInteractEnabled() { return betterInteractEnabled; }
    public void setBetterInteractEnabled(boolean enabled) {
        this.betterInteractEnabled = enabled;
    }

    public boolean isBetterInteractClickThrough() { return betterInteractClickThrough; }
    public void setBetterInteractClickThrough(boolean enabled) {
        this.betterInteractClickThrough = enabled; save();
    }

    public boolean isBetterInteractAntiSigns() { return betterInteractAntiSigns; }
    public void setBetterInteractAntiSigns(boolean enabled) {
        this.betterInteractAntiSigns = enabled; save();
    }

    public boolean isBetterInteractAutoSigns() { return betterInteractAutoSigns; }
    public void setBetterInteractAutoSigns(boolean enabled) {
        this.betterInteractAutoSigns = enabled; save();
    }

    public String getBetterInteractAutoSignsText() {
        return betterInteractAutoSignsText;
    }

    public void setBetterInteractAutoSignsText(String text) {
        betterInteractAutoSignsText = text == null ? "" : text;
    }

    public boolean isBetterInteractSafeHarvest() { return betterInteractSafeHarvest; }
    public void setBetterInteractSafeHarvest(boolean enabled) {
        this.betterInteractSafeHarvest = enabled; save();
    }

    /// Better Tnt
    public boolean isBetterTntEnabled() { return betterTntEnabled; }
    public void setBetterTntEnabled(boolean enabled) {
        this.betterTntEnabled = enabled; save();
    }
    public boolean isBetterTntTimer() { return betterTntTimer; }
    public void setBetterTntTimer(boolean enabled) {
        this.betterTntTimer = enabled; save();
    }
    public boolean isBetterTntAlert() { return betterTntAlert; }
    public void setBetterTntAlert(boolean enabled) {
        this.betterTntAlert = enabled; save();
    }

    public int getBetterTntAlertPosition() { return betterTntAlertPosition; }
    public void setBetterTntAlertPosition(int loc) { this.betterTntAlertPosition = Math.max(0, Math.min(4, loc)); save(); }

    public boolean isBetterTntAlertShowXYZ() { return betterTntAlertShowXYZ; }
    public void setBetterTntAlertShowXYZ(boolean enabled) {
        this.betterTntAlertShowXYZ = enabled; save();
    }

    /// Better Holograms
    public boolean isBetterHologramsEnabled() { return betterHologramsEnabled; }
    public void setBetterHologramsEnabled(boolean enabled) {
        this.betterHologramsEnabled = enabled; save();
    }
    public boolean isBetterHologramsVisibleArmorStand() { return betterHologramsVisibleArmorStand; }
    public void setBetterHologramsVisibleArmorStand(boolean enabled) {
        this.betterHologramsVisibleArmorStand = enabled; save();
    }
    public boolean isBetterHologramsAntiHolograms() { return betterHologramsAntiHolograms; }
    public void setBetterHologramsAntiHolograms(boolean enabled) {
        this.betterHologramsAntiHolograms = enabled; save();
    }

    /// Better Spheres
    public boolean isBetterSpheresEnabled() { return betterSpheresEnabled; }
    public void setBetterSpheresEnabled(boolean enabled) { this.betterSpheresEnabled = enabled; save(); }

    // Holyworld
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

    public boolean isHWGoldenSpheres() { return hwGoldenSpheres; }
    public void setHWGoldenSpheres(boolean enabled) { this.hwGoldenSpheres = enabled; save(); }

    // ReallyWorld
    public boolean isReallyWorldSpheresEnabled() { return reallyWorldSpheresEnabled; }
    public void setReallyWorldSpheresEnabled(boolean enabled) { this.reallyWorldSpheresEnabled = enabled; save(); }

    public boolean isSphereAir() { return sphereAir; }
    public void setSphereAir(boolean enabled) { this.sphereAir = enabled; save(); }
    public boolean isSphereFire() { return sphereFire; }
    public void setSphereFire(boolean enabled) { this.sphereFire = enabled; save(); }
    public boolean isSphereWater() { return sphereWater; }
    public void setSphereWater(boolean enabled) { this.sphereWater = enabled; save(); }
    public boolean isSphereGround() { return sphereGround; }
    public void setSphereGround(boolean enabled) { this.sphereGround = enabled; save(); }
    public boolean isSphereGOD() { return sphereGOD; }
    public void setSphereGOD(boolean enabled) { this.sphereGOD = enabled; save(); }
    public boolean isSphereCocaCola() { return sphereCocaCola; }
    public void setSphereCocaCola(boolean enabled) { this.sphereCocaCola = enabled; save(); }
    public boolean isSpherePepsi() { return spherePepsi; }
    public void setSpherePepsi(boolean enabled) { this.spherePepsi = enabled; save(); }
    public boolean isSphereRedBull() { return sphereRedBull; }
    public void setSphereRedBull(boolean enabled) { this.sphereRedBull = enabled; save(); }
    public boolean isSphereSprite() { return sphereSprite; }
    public void setSphereSprite(boolean enabled) { this.sphereSprite = enabled; save(); }
    public boolean isSphereFanta() { return sphereFanta; }
    public void setSphereFanta(boolean enabled) { this.sphereFanta = enabled; save(); }
    public boolean isSphereShine() { return sphereShine; }
    public void setSphereShine(boolean enabled) { this.sphereShine = enabled; save(); }
    public boolean isSphereChaos() { return sphereChaos; }
    public void setSphereChaos(boolean enabled) { this.sphereChaos = enabled; save(); }
    public boolean isSpherePoseidon() { return spherePoseidon; }
    public void setSpherePoseidon(boolean enabled) { this.spherePoseidon = enabled; save(); }
    public boolean isSphereHades() { return sphereHades; }
    public void setSphereHades(boolean enabled) { this.sphereHades = enabled; save(); }
    public boolean isSphereArmadillo() { return sphereArmadillo; }
    public void setSphereArmadillo(boolean enabled) { this.sphereArmadillo = enabled; save(); }
    public boolean isSphereBUNNY() { return sphereBUNNY; }
    public void setSphereBUNNY(boolean enabled) { this.sphereBUNNY = enabled; save(); }
    public boolean isSphereDHELPER() { return sphereDHELPER; }
    public void setSphereDHELPER(boolean enabled) { this.sphereDHELPER = enabled; save(); }
    public boolean isSphereDiscipline() { return sphereDiscipline; }
    public void setSphereDiscipline(boolean enabled) { this.sphereDiscipline = enabled; save(); }
    public boolean isHeadBatman() { return headBatman; }
    public void setHeadBatman(boolean enabled) { this.headBatman = enabled; save(); }
    public boolean isHeadVampire() { return headVampire; }
    public void setHeadVampire(boolean enabled) { this.headVampire = enabled; save(); }
    public boolean isHeadJack() { return headJack; }
    public void setHeadJack(boolean enabled) { this.headJack = enabled; save(); }
    public boolean isHeadGrinch() { return headGrinch; }
    public void setHeadGrinch(boolean enabled) { this.headGrinch = enabled; save(); }
    public boolean isHeadHydra() { return headHydra; }
    public void setHeadHydra(boolean enabled) { this.headHydra = enabled; save(); }
    public boolean isHeadIronMan() { return headIronMan; }
    public void setHeadIronMan(boolean enabled) { this.headIronMan = enabled; save(); }
    public boolean isHeadCobra() { return headCobra; }
    public void setHeadCobra(boolean enabled) { this.headCobra = enabled; save(); }
    public boolean isHeadBunny() { return headBunny; }
    public void setHeadBunny(boolean enabled) { this.headBunny = enabled; save(); }
    public boolean isHeadPegasus() { return headPegasus; }
    public void setHeadPegasus(boolean enabled) { this.headPegasus = enabled; save(); }
    public boolean isHeadGingerbread() { return headGingerbread; }
    public void setHeadGingerbread(boolean enabled) { this.headGingerbread = enabled; save(); }
    public boolean isHeadRudolph() { return headRudolph; }
    public void setHeadRudolph(boolean enabled) { this.headRudolph = enabled; save(); }
    public boolean isHeadSanta() { return headSanta; }
    public void setHeadSanta(boolean enabled) { this.headSanta = enabled; save(); }
    public boolean isHeadHulk() { return headHulk; }
    public void setHeadHulk(boolean enabled) { this.headHulk = enabled; save(); }
    public boolean isHeadPenguin() { return headPenguin; }
    public void setHeadPenguin(boolean enabled) { this.headPenguin = enabled; save(); }
    public boolean isHeadThor() { return headThor; }
    public void setHeadThor(boolean enabled) { this.headThor = enabled; save(); }
    public boolean isHeadNutcracker() { return headNutcracker; }
    public void setHeadNutcracker(boolean enabled) { this.headNutcracker = enabled; save(); }
    public boolean isHeadElf() { return headElf; }
    public void setHeadElf(boolean enabled) { this.headElf = enabled; save(); }

    public boolean isEasterEgg() { return easterEgg; }
    public void setEasterEgg(boolean enabled) { this.easterEgg = enabled; save(); }

    /// Better Sky
    public boolean isBetterSkyEnabled() { return betterSkyEnabled; }
    public void setBetterSkyEnabled(boolean betterSkyEnabled) { this.betterSkyEnabled = betterSkyEnabled; }

    public boolean isBetterSkyColorEnabled() { return betterSkyColorEnabled; }
    public void setBetterSkyColorEnabled(boolean enabled) { this.betterSkyColorEnabled = enabled; save(); }
    public int getBetterSkyColor() { return betterSkyColor; }
    public void setBetterSkyColor(int betterSkyColor) { this.betterSkyColor = betterSkyColor; }
    public long getBetterSkyTime() { return betterSkyTime; }
    public void setBetterSkyTime(long betterSkyTime) { this.betterSkyTime = betterSkyTime; }

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

    /// Better Fog
    public boolean isBetterFogEnabled() { return betterFogEnabled; }
    public void setBetterFogEnabled(boolean enabled) { this.betterFogEnabled = enabled; save(); }

    public boolean isNoFog() { return noFog; }
    public void setNoFog(boolean enabled) { this.noFog = enabled; save(); }
    public boolean isNightVision() { return nightVision; }
    public void setNightVision(boolean enabled) { this.nightVision = enabled; save(); }

    /// Custom Health
    public boolean isCustomHealthEnabled() { return customHealthEnabled; }
    public void setCustomHealthEnabled(boolean enabled) { this.customHealthEnabled = enabled; save(); }

    public int getCustomHealthDuration() { return customHealthDuration; }
    public void setCustomHealthDuration(int duration) { this.customHealthDuration = Math.max(3000, Math.min(30000, duration)); save(); }
    public int getCustomHealthPosition() { return customHealthPosition; }
    public void setCustomHealthPosition(int loc) { this.customHealthPosition = Math.max(0, Math.min(3, loc)); save(); }
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

    public boolean isNoRenderTotemOverlayEnabled() { return noRenderTotemOverlayEnabled; }
    public void setNoRenderTotemOverlay(boolean enabled) {
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

    public boolean isNoRenderTotemParticlesEnabled() {
        return noRenderTotemParticlesEnabled;
    }
    public void setNoRenderTotemParticlesEnabled(boolean enabled) {
        this.noRenderTotemParticlesEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderTotemParticles() {
        return noRenderTotemParticles;
    }
    public void setNoRenderTotemParticles(RenderMode mode) {
        this.noRenderTotemParticles = mode;
        save();
    }

    public boolean isNoRenderPotionParticlesEnabled() {
        return noRenderPotionParticlesEnabled;
    }
    public void setNoRenderPotionParticlesEnabled(boolean enabled) {
        this.noRenderPotionParticlesEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderPotionParticles() {
        return noRenderPotionParticles;
    }
    public void setNoRenderPotionParticles(RenderMode mode) {
        this.noRenderPotionParticles = mode;
        save();
    }

    public boolean isNoRenderExplosionEnabled() {
        return noRenderExplosionEnabled;
    }
    public void setNoRenderExplosionEnabled(boolean enabled) {
        this.noRenderExplosionEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderExplosion() {
        return noRenderExplosion;
    }
    public void setNoRenderExplosion(RenderMode mode) {
        this.noRenderExplosion = mode;
        save();
    }

    public boolean isNoRenderSmokeEnabled() {
        return noRenderSmokeEnabled;
    }
    public void setNoRenderSmokeEnabled(boolean enabled) {
        this.noRenderSmokeEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderSmoke() {
        return noRenderSmoke;
    }
    public void setNoRenderSmoke(RenderMode mode) {
        this.noRenderSmoke = mode;
        save();
    }

    public boolean isNoRenderBubblesEnabled() {
        return noRenderBubblesEnabled;
    }
    public void setNoRenderBubblesEnabled(boolean enabled) {
        this.noRenderBubblesEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderBubbles() {
        return noRenderBubbles;
    }
    public void setNoRenderBubbles(RenderMode mode) {
        this.noRenderBubbles = mode;
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

    public boolean isNoRenderArrowsEnabled() {
        return noRenderArrowsEnabled;
    }
    public void setNoRenderArrowsEnabled(boolean enabled) {
        noRenderArrowsEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderArrows() {
        return noRenderArrows;
    }
    public void setNoRenderArrows(RenderMode mode) {
        noRenderArrows = mode;
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

    public boolean isNoRenderNamesEnabled() {
        return noRenderNamesEnabled;
    }
    public void setNoRenderNamesEnabled(boolean enabled) {
        this.noRenderNamesEnabled = enabled;
        save();
    }
    public RenderMode getNoRenderNames() {
        return noRenderNames;
    }
    public void setNoRenderNames(RenderMode mode) {
        this.noRenderNames = mode;
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
        this.betterSoundsSwim = defaults.betterSoundsSwim;
        this.betterSoundsFall = defaults.betterSoundsFall;
        this.betterSoundsChargeCrossbow = defaults.betterSoundsChargeCrossbow;
        this.betterSoundsFireworks = defaults.betterSoundsFireworks;
        this.betterSoundsEnderman = defaults.betterSoundsEnderman;
        this.betterSoundsBlaze = defaults.betterSoundsBlaze;
        this.betterSoundsBee = defaults.betterSoundsBee;

        this.betterSoundsFarm = defaults.betterSoundsFarm;
        this.betterSoundsMob = defaults.betterSoundsMob;

        /// Better Interact
        this.betterInteractEnabled = defaults.betterInteractEnabled;

        this.betterInteractClickThrough = defaults.betterInteractClickThrough;
        this.betterInteractAntiSigns = defaults.betterInteractAntiSigns;
        this.betterInteractAutoSigns = defaults.betterInteractAutoSigns;
        this.betterInteractSafeHarvest = defaults.betterInteractSafeHarvest;

        /// Better Tnt
        this.betterTntEnabled = defaults.betterTntEnabled;
        this.betterTntTimer = defaults.betterTntTimer;
        this.betterTntAlert = defaults.betterTntAlert;
        this.betterTntAlertPosition = defaults.betterTntAlertPosition;
        this.betterTntAlertShowXYZ = defaults.betterTntAlertShowXYZ;

        /// Better Holograms
        this.betterHologramsEnabled = defaults.betterHologramsEnabled;
        this.betterHologramsVisibleArmorStand = defaults.betterHologramsVisibleArmorStand;
        this.betterHologramsAntiHolograms = defaults.betterHologramsAntiHolograms;

        /// Better Spheres
        this.betterSpheresEnabled = defaults.betterSpheresEnabled;

        // HolyWorld
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

        this.hwGoldenSpheres = defaults.hwGoldenSpheres;

        // ReallyWorld
        this.sphereAir = defaults.sphereAir;
        this.sphereFire = defaults.sphereFire;
        this.sphereShine = defaults.sphereShine;
        this.sphereChaos = defaults.sphereChaos;
        this.sphereWater = defaults.sphereWater;
        this.sphereGround = defaults.sphereGround;
        this.sphereGOD = defaults.sphereGOD;
        this.sphereCocaCola = defaults.sphereCocaCola;
        this.spherePepsi = defaults.spherePepsi;
        this.sphereRedBull = defaults.sphereRedBull;
        this.sphereSprite = defaults.sphereSprite;
        this.sphereFanta = defaults.sphereFanta;
        this.spherePoseidon = defaults.spherePoseidon;
        this.sphereHades = defaults.sphereHades;
        this.sphereArmadillo = defaults.sphereArmadillo;
        this.sphereBUNNY = defaults.sphereBUNNY;
        this.sphereDHELPER = defaults.sphereDHELPER;
        this.sphereDiscipline = defaults.sphereDiscipline;

        this.headBatman = defaults.headBatman;
        this.headVampire = defaults.headVampire;
        this.headJack = defaults.headJack;
        this.headGrinch = defaults.headGrinch;
        this.headHydra = defaults.headHydra;
        this.headIronMan = defaults.headIronMan;
        this.headCobra = defaults.headCobra;
        this.headBunny = defaults.headBunny;
        this.headPegasus = defaults.headPegasus;
        this.headGingerbread = defaults.headGingerbread;
        this.headRudolph = defaults.headRudolph;
        this.headSanta = defaults.headSanta;
        this.headHulk = defaults.headHulk;
        this.headPenguin = defaults.headPenguin;
        this.headThor = defaults.headThor;
        this.headNutcracker = defaults.headNutcracker;
        this.headElf = defaults.headElf;
        this.easterEgg = defaults.easterEgg;

        this.rwGoldenSpheres = defaults.rwGoldenSpheres;

        /// BetterSky
        this.betterSkyEnabled = defaults.betterSkyEnabled;

        this.betterSkyColorEnabled = defaults.betterSkyColorEnabled;
        this.betterSkyColor = defaults.betterSkyColor;
        this.betterSkyTime = defaults.betterSkyTime;

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
        this.betterFogEnabled = defaults.betterFogEnabled;

        this.noFog = defaults.noFog;
        this.nightVision = defaults.nightVision;

        /// Custom Health
        this.customHealthEnabled = defaults.customHealthEnabled;

        this.customHealthDuration = defaults.customHealthDuration;
        this.customHealthPosition = defaults.customHealthPosition;
        this.customHealthScaling = defaults.customHealthScaling;
        this.customHealthHovering = defaults.customHealthHovering;
        this.customHealthPvPMode = defaults.customHealthPvPMode;
        this.customHealthDecimal = defaults.customHealthDecimal;
        this.customHealthGoldenHearts = defaults.customHealthGoldenHearts;
        this.customHealthGoldenHeartsPlus = defaults.customHealthGoldenHeartsPlus;

        /// No Render
        this.noRenderTotemOverlayEnabled = defaults.noRenderTotemOverlayEnabled;
        this.noRenderFireOverlayEnabled = defaults.noRenderFireOverlayEnabled;
        this.noRenderTotemParticlesEnabled = defaults.noRenderTotemParticlesEnabled;
        this.noRenderPotionParticlesEnabled = defaults.noRenderPotionParticlesEnabled;
        this.noRenderExplosionEnabled = defaults.noRenderExplosionEnabled;
        this.noRenderSmokeEnabled = defaults.noRenderSmokeEnabled;
        this.noRenderBubblesEnabled = defaults.noRenderBubblesEnabled;
        this.noRenderWeatherEnabled = defaults.noRenderWeatherEnabled;
        this.noRenderArrowsEnabled = defaults.noRenderArrowsEnabled;
        this.noRenderFireworksEnabled = defaults.noRenderFireworksEnabled;
        this.noRenderNamesEnabled = defaults.noRenderNamesEnabled;
        this.noRenderPlayersEnabled = defaults.noRenderPlayersEnabled;
        this.noRenderHandEnabled = defaults.noRenderHandEnabled;

        this.noRenderEnabled = defaults.noRenderEnabled;
        this.noRenderTotemOverlay = defaults.noRenderTotemOverlay;
        this.noRenderFireOverlay = defaults.noRenderFireOverlay;
        this.noRenderTotemParticles = defaults.noRenderTotemParticles;
        this.noRenderPotionParticles = defaults.noRenderPotionParticles;
        this.noRenderExplosion = defaults.noRenderExplosion;
        this.noRenderSmoke = defaults.noRenderSmoke;
        this.noRenderBubbles = defaults.noRenderBubbles;
        this.noRenderWeather = defaults.noRenderWeather;
        this.noRenderArrows = defaults.noRenderArrows;
        this.noRenderFireworks = defaults.noRenderFireworks;
        this.noRenderNames = defaults.noRenderNames;
        this.noRenderPlayers = defaults.noRenderPlayers;
        this.noRenderHand = defaults.noRenderHand;

        validateSettings();
        save();
    }
}