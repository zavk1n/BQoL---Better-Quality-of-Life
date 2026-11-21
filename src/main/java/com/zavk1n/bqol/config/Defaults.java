package com.zavk1n.bqol.config;

public abstract class Defaults extends Validate {

    public void resetToDefaults() {
        MainConfig defaults = new MainConfig();

        this.debug = defaults.debug;

        /// Better Movement
        this.betterMovementEnabled = defaults.betterMovementEnabled;

        this.betterMovementDefault = defaults.betterMovementDefault;
        this.betterMovementStairUp = defaults.betterMovementStairUp;
        this.betterMovementWaterSprint = defaults.betterMovementWaterSprint;
        this.betterMovementScaffold = defaults.betterMovementScaffold;

        /// Better RCT
        this.betterRCTEnabled = defaults.betterRCTEnabled;

        this.betterRCTHolyWorld = defaults.betterRCTHolyWorld;
        this.betterRCTReallyWorld = defaults.betterRCTReallyWorld;
        this.betterRCTFakeTime = defaults.betterRCTFakeTime;

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
        this.betterInteractAutoSignsText = defaults.betterInteractAutoSignsText;
        this.betterInteractSafeHarvest = defaults.betterInteractSafeHarvest;

        /// Better Gamma
        this.betterGammaEnabled = defaults.betterGammaEnabled;

        /// Better Hurt Cam
        this.betterHurtCamEnabled = defaults.betterHurtCamEnabled;

        /// Better Third Person
        this.betterThirdPersonEnabled = defaults.betterThirdPersonEnabled;

        /// Better Tnt
        this.betterTntEnabled = defaults.betterTntEnabled;

        this.betterTntTimer = defaults.betterTntTimer;
        this.betterTntAlert = defaults.betterTntAlert;
        this.betterTntAlertPosition = defaults.betterTntAlertPosition;
        this.betterTntAlertShowXYZ = defaults.betterTntAlertShowXYZ;
        this.betterTntAlertShowBlocks = defaults.betterTntAlertShowBlocks;
        this.betterTntExplosionArea = defaults.betterTntExplosionArea;

        /// Better Chat
        this.betterChatEnabled = defaults.betterChatEnabled;

        this.betterChatStackMessages = defaults.betterChatStackMessages;
        this.betterChatAnimation = defaults.betterChatAnimation;
        this.betterChatMoreHistory = defaults.betterChatMoreHistory;
        this.betterChatHeads = defaults.betterChatHeads;
        this.betterChatPing = defaults.betterChatPing;

        this.betterChatTransliteration = defaults.betterChatTransliteration;
        this.betterChatKillMessage = defaults.betterChatKillMessage;
        this.betterChatKillMessageText = defaults.betterChatKillMessageText;

        /// Better Zoom
        this.betterZoomEnabled = defaults.betterZoomEnabled;

        /// Better TP
        this.betterTPEnabled = defaults.betterTPEnabled;

        this.betterTPAutoAccept = defaults.betterTPAutoAccept;
        this.betterTPAutoDeny = defaults.betterTPAutoDeny;

        /// Better RRLS
        this.betterRRLSEnabled = defaults.betterRRLSEnabled;

        /// Better Prefixes
        this.betterPrefixesEnabled = defaults.betterPrefixesEnabled;

        this.betterPrefixesNameProtect = defaults.betterPrefixesNameProtect;
        this.betterPrefixesDonate = defaults.betterPrefixesDonate;
        this.betterPrefixesTitle = defaults.betterPrefixesTitle;

        this.betterPrefixesProtectedName = defaults.betterPrefixesProtectedName;
        this.betterPrefixesDonateText = defaults.betterPrefixesDonateText;
        this.betterPrefixesTitleText = defaults.betterPrefixesTitleText;

        this.betterPrefixesNameColorRgb = defaults.betterPrefixesNameColorRgb;
        this.betterPrefixesDonateColorRgb = defaults.betterPrefixesDonateColorRgb;
        this.betterPrefixesTitleColorRgb = defaults.betterPrefixesTitleColorRgb;

        this.betterPrefixesNameColor = defaults.betterPrefixesNameColor;
        this.betterPrefixesDonateColor = defaults.betterPrefixesDonateColor;
        this.betterPrefixesTitleColor = defaults.betterPrefixesTitleColor;

        this.betterPrefixesNameBold = defaults.betterPrefixesNameBold;
        this.betterPrefixesDonateBold = defaults.betterPrefixesDonateBold;
        this.betterPrefixesTitleBold = defaults.betterPrefixesTitleBold;

        this.betterPrefixesStar = defaults.betterPrefixesStar;
        this.betterPrefixesStarColorRgb = defaults.betterPrefixesStarColorRgb;

        this.betterPrefixesLightning = defaults.betterPrefixesLightning;
        this.betterPrefixesLightningColorRgb = defaults.betterPrefixesLightningColorRgb;

        /// Better Hit Color
        this.betterHitColorEnabled = defaults.betterHitColorEnabled;

        this.betterHitColorColorEnabled = defaults.betterHitColorColorEnabled;
        this.betterHitColor = defaults.betterHitColor;

        /// Better Hitboxes
        this.betterHitboxesEnabled = defaults.betterHitboxesEnabled;

        this.betterHitboxesColorEnabled = defaults.betterHitboxesColorEnabled;
        this.betterHitboxesColor = defaults.betterHitboxesColor;

        /// Better Holograms
        this.betterHologramsEnabled = defaults.betterHologramsEnabled;

        this.betterHologramsVisibleArmorStand = defaults.betterHologramsVisibleArmorStand;
        this.betterHologramsAntiHolograms = defaults.betterHologramsAntiHolograms;

        /// Better Totems
        this.betterTotemsEnabled = defaults.betterTotemsEnabled;
        this.betterTotemsCounterEnabled = defaults.betterTotemsCounterEnabled;

        // HolyWorld
        this.holyWorldTotemsEnabled = defaults.holyWorldTotemsEnabled;

        this.totemInfinity = defaults.totemInfinity;
        this.totemEternity = defaults.totemEternity;
        this.totemStinger = defaults.totemStinger;
        this.totemSatires = defaults.totemSatires;

        // ReallyWorld
        this.reallyWorldTotemsEnabled = defaults.reallyWorldTotemsEnabled;

        this.totemDrakonid = defaults.totemDrakonid;
        this.totemWitch = defaults.totemWitch;

        /// Better Spheres
        this.betterSpheresEnabled = defaults.betterSpheresEnabled;

        // HolyWorld
        this.holyWorldSpheresEnabled = defaults.holyWorldSpheresEnabled;

        this.sphereCerberusHW = defaults.sphereCerberusHW;
        this.sphereFlashHW = defaults.sphereFlashHW;
        this.sphereImmortalityHW = defaults.sphereImmortalityHW;
        this.sphereArmortalityHW = defaults.sphereArmortalityHW;
        this.sphereEternityHW = defaults.sphereEternityHW;
        this.sphereStingerHW = defaults.sphereStingerHW;

        this.sphereMythicHW = defaults.sphereMythicHW;
        this.sphereLegendaryHW = defaults.sphereLegendaryHW;
        this.sphereEpicHW = defaults.sphereEpicHW;
        this.sphereDefaultHW = defaults.sphereDefaultHW;

        this.sphereSpeedHW = defaults.sphereSpeedHW;
        this.sphereMinerHW = defaults.sphereMinerHW;
        this.spherePvPHW = defaults.spherePvPHW;

        this.hwGoldenSpheres = defaults.hwGoldenSpheres;

        // ReallyWorld
        this.reallyWorldSpheresEnabled = defaults.reallyWorldSpheresEnabled;

        this.sphereAirRW = defaults.sphereAirRW;
        this.sphereFireRW = defaults.sphereFireRW;
        this.sphereShineRW = defaults.sphereShineRW;
        this.sphereChaosRW = defaults.sphereChaosRW;
        this.sphereWaterRW = defaults.sphereWaterRW;
        this.sphereGroundRW = defaults.sphereGroundRW;
        this.sphereGODRW = defaults.sphereGODRW;
        this.sphereCocaColaRW = defaults.sphereCocaColaRW;
        this.spherePepsiRW = defaults.spherePepsiRW;
        this.sphereRedBullRW = defaults.sphereRedBullRW;
        this.sphereSpriteRW = defaults.sphereSpriteRW;
        this.sphereFantaRW = defaults.sphereFantaRW;
        this.spherePoseidonRW = defaults.spherePoseidonRW;
        this.sphereHadesRW = defaults.sphereHadesRW;
        this.sphereArmadilloRW = defaults.sphereArmadilloRW;
        this.sphereBUNNYRW = defaults.sphereBUNNYRW;
        this.sphereDHELPERRW = defaults.sphereDHELPERRW;
        this.sphereDisciplineRW = defaults.sphereDisciplineRW;
        this.sphereDarkRW = defaults.sphereDarkRW;
        this.sphereBloodRW = defaults.sphereBloodRW;
        this.sphereLightningRW = defaults.sphereLightningRW;
        this.sphereIceRW = defaults.sphereIceRW;
        this.sphereDiabloRW = defaults.sphereDiabloRW;
        this.sphereWardenRW = defaults.sphereWardenRW;
        this.sphereModerRW = defaults.sphereModerRW;
        this.sphereCheaterRW = defaults.sphereCheaterRW;
        this.sphereAdminRW = defaults.sphereAdminRW;
        this.sphereTeacherRW = defaults.sphereTeacherRW;

        this.headBatmanRW = defaults.headBatmanRW;
        this.headVampireRW = defaults.headVampireRW;
        this.headJackRW = defaults.headJackRW;
        this.headGrinchRW = defaults.headGrinchRW;
        this.headHydraRW = defaults.headHydraRW;
        this.headIronManRW = defaults.headIronManRW;
        this.headCobraRW = defaults.headCobraRW;
        this.headBunnyRW = defaults.headBunnyRW;
        this.headPegasusRW = defaults.headPegasusRW;
        this.headGingerbreadRW = defaults.headGingerbreadRW;
        this.headRudolphRW = defaults.headRudolphRW;
        this.headSantaRW = defaults.headSantaRW;
        this.headHulkRW = defaults.headHulkRW;
        this.headPenguinRW = defaults.headPenguinRW;
        this.headThorRW = defaults.headThorRW;
        this.headNutcrackerRW = defaults.headNutcrackerRW;
        this.headElfRW = defaults.headElfRW;
        this.headTigerRW = defaults.headTigerRW;
        this.easterEggRW = defaults.easterEggRW;

        /// Better Sky
        this.betterSkyEnabled = defaults.betterSkyEnabled;

        this.betterSkyColorEnabled = defaults.betterSkyColorEnabled;
        this.betterSkyColor = defaults.betterSkyColor;
        this.betterSkyTime = defaults.betterSkyTime;

        /// Shulker Particles
        this.betterParticlesEnabled = defaults.betterParticlesEnabled;

        this.betterParticlesShulkersEnabled = defaults.betterParticlesShulkersEnabled;

        this.betterParticlesShulkersConstantEnabled = defaults.betterParticlesShulkersConstantEnabled;
        this.betterParticlesShulkersBreakingEnabled = defaults.betterParticlesShulkersBreakingEnabled;
        this.betterParticlesShulkersVanillaBreakingEnabled = defaults.betterParticlesShulkersVanillaBreakingEnabled;
        this.betterParticlesShulkersConstantDependenceEnabled = defaults.betterParticlesShulkersConstantDependenceEnabled;
        this.betterParticlesShulkersBreakingDependenceEnabled = defaults.betterParticlesShulkersBreakingDependenceEnabled;
        this.betterParticlesShulkersConstantColor = defaults.betterParticlesShulkersConstantColor;
        this.betterParticlesShulkersBreakingColor = defaults.betterParticlesShulkersBreakingColor;

        this.betterParticlesTotemsColorEnabled = defaults.betterParticlesTotemsColorEnabled;

        this.betterParticlesTotemsColor = defaults.betterParticlesTotemsColor;
        this.betterParticlesTotemsCountEnabled = defaults.betterParticlesTotemsCountEnabled;
        this.betterParticlesTotemsCountMode = defaults.betterParticlesTotemsCountMode;

        /// Better Liquid
        this.betterLiquidEnabled = defaults.betterLiquidEnabled;

        this.betterLiquidWaterColorEnabled = defaults.betterLiquidWaterColorEnabled;
        this.betterLiquidWaterColor = defaults.betterLiquidWaterColor;
        this.betterLiquidLavaColorEnabled = defaults.betterLiquidLavaColorEnabled;
        this.betterLiquidLavaColor = defaults.betterLiquidLavaColor;

        this.betterLiquidWaterTransparencyEnabled = defaults.betterLiquidWaterTransparencyEnabled;
        this.betterLiquidWaterTransparency = defaults.betterLiquidWaterTransparency;
        this.betterLiquidLavaTransparencyEnabled = defaults.betterLiquidLavaTransparencyEnabled;
        this.betterLiquidLavaTransparency = defaults.betterLiquidLavaTransparency;

        this.betterLiquidWaterColorIntensityEnabled = defaults.betterLiquidWaterColorIntensityEnabled;
        this.betterLiquidLavaColorIntensityEnabled = defaults.betterLiquidLavaColorIntensityEnabled;
        this.betterLiquidWaterColorIntensity = defaults.betterLiquidWaterColorIntensity;
        this.betterLiquidLavaColorIntensity = defaults.betterLiquidLavaColorIntensity;

        /// Better Fog
        this.betterFogEnabled = defaults.betterFogEnabled;

        this.betterFogColorEnabled = defaults.betterFogColorEnabled;
        this.betterFogColor = defaults.betterFogColor;
        this.betterFogDistance = defaults.betterFogDistance;
        this.betterFognightVision = defaults.betterFognightVision;
        this.betterFognoFog = defaults.betterFognoFog;
        this.betterFognoBlindness = defaults.betterFognoBlindness;

        /// Better Health
        this.betterHealthEnabled = defaults.betterHealthEnabled;

        this.betterHealthDuration = defaults.betterHealthDuration;
        this.betterHealthPosition = defaults.betterHealthPosition;
        this.betterHealthScaling = defaults.betterHealthScaling;
        this.betterHealthHovering = defaults.betterHealthHovering;
        this.betterHealthPvPMode = defaults.betterHealthPvPMode;
        this.betterHealthDecimal = defaults.betterHealthDecimal;
        this.betterHealthGoldenHearts = defaults.betterHealthGoldenHearts;
        this.betterHealthGoldenHeartsPlus = defaults.betterHealthGoldenHeartsPlus;

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

        /// Better Mine
        this.betterMineEnabled = defaults.betterMineEnabled;

        this.betterMineHolyWorld = defaults.betterMineHolyWorld;
        this.betterMineHolyWorldMineHelper = defaults.betterMineHolyWorldMineHelper;
        this.betterMineFunTime = defaults.betterMineFunTime;
        this.betterMineFunTimeMineHelper = defaults.betterMineFunTimeMineHelper;

        /// Better Anti Invisible
        this.betterAntiInvisibleEnabled = defaults.betterAntiInvisibleEnabled;

        /// Better Hit Counter
        this.betterHitCounterEnabled = defaults.betterHitCounterEnabled;

        validateSettings();
        save();
    }
}
