package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterFog {
    private BetterFog() {}

    private static BetterFog instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean noFog;
        boolean nightVision;
        boolean color;
        boolean fogDistance;
        boolean noBlindness;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterFog();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Fog initialized");
        }
    }

    public static BetterFog getInstance() {
        if (instance == null) {
            initialize();
        }

        return instance;
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

    public static boolean isFogColorEnabled() {
        return instance != null && instance.isFogColorEnabledInternal();
    }

    public static void setFogColorEnabled(boolean enabled) {
        getInstance().setFogColorEnabledInternal(enabled);
    }

    public static boolean isFogDistanceEnabled() {
        return instance != null && instance.isFogDistanceEnabledInternal();
    }

    public static void setFogDistanceEnabled(boolean enabled) {
        getInstance().setFogDistanceEnabledInternal(enabled);
    }

    public static boolean isNoFogEnabled() {
        return instance != null && instance.isNoFogEnabledInternal();
    }

    public static void setNoFogEnabled(boolean enabled) {
        getInstance().setNoFogEnabledInternal(enabled);
    }

    public static boolean isNightVisionEnabled() {
        return instance != null && instance.isNightVisionEnabledInternal();
    }

    public static void setNightVisionEnabled(boolean enabled) {
        getInstance().setNightVisionEnabledInternal(enabled);
    }

    public static boolean isNoBlindnessEnabled() {
        return instance != null && instance.isNoBlindnessEnabledInternal();
    }

    public static void setNoBlindnessEnabled(boolean enabled) {
        getInstance().setNoBlindnessEnabledInternal(enabled);
    }

    public static int getFogColor() {
        return getInstance().config.getBetterFogColor() & 0xFFFFFF;
    }

    public static void setFogColor(int rgb) {
        getInstance().config.setBetterFogColor(rgb & 0xFFFFFF);
    }

    public static float getFogRed() {
        return ((getFogColor() >> 16) & 0xFF) / 255.0f;
    }

    public static float getFogGreen() {
        return ((getFogColor() >> 8) & 0xFF) / 255.0f;
    }

    public static float getFogBlue() {
        return (getFogColor() & 0xFF) / 255.0f;
    }

    public static int getFogDistance() {
        return Math.max(1, Math.min(32, getInstance().config.getBetterFogDistance()));
    }

    public static void setFogDistance(int chunks) {
        getInstance().config.setBetterFogDistance(Math.max(1, Math.min(32, chunks)));
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_fog");
        blocked.color = LiteApiManager.isFeatureBlocked("better_fog_color");
        blocked.fogDistance = LiteApiManager.isFeatureBlocked("better_fog_distance");
        blocked.noFog = LiteApiManager.isFeatureBlocked("better_fog_no_fog");
        blocked.nightVision = LiteApiManager.isFeatureBlocked("better_fog_night_vision");
        blocked.noBlindness = LiteApiManager.isFeatureBlocked("better_fog_no_blindness");
    }

    private boolean isEnabledInternal() {
        return config.isBetterFogEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterFogEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isFogColorEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterFogColorEnabled()
            && !blocked.color;
    }

    private void setFogColorEnabledInternal(boolean enabled) {
        config.setBetterFogColorEnabled(enabled);
    }

    private boolean isFogDistanceEnabledInternal() {
        return isEnabledInternal()
            && !blocked.fogDistance;
    }

    private void setFogDistanceEnabledInternal(boolean enabled) { }

    private boolean isNoBlindnessEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterFogNoBlindness()
            && !blocked.noBlindness;
    }

    private void setNoBlindnessEnabledInternal(boolean enabled) {
        config.setBetterFogNoBlindness(enabled);
    }

    private boolean isNoFogEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterFogNoFog()
            && !blocked.noFog;
    }

    private void setNoFogEnabledInternal(boolean enabled) {
        config.setBetterFogNoFog(enabled);
    }

    private boolean isNightVisionEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterFogNightVision()
            && !blocked.nightVision;
    }

    private void setNightVisionEnabledInternal(boolean enabled) {
        config.setBetterFogNightVision(enabled);
    }
}