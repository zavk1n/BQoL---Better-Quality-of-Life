package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterLiquid {
    private BetterLiquid() {}

    private static BetterLiquid instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean waterColor;
        boolean lavaColor;
        boolean waterTransparency;
        boolean lavaTransparency;
        boolean waterColorIntensity;
        boolean lavaColorIntensity;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterLiquid();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Liquid initialized");
        }
    }

    public static BetterLiquid getInstance() {
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
        instance.setEnabledInternal(enabled);
    }

    public static boolean isWaterColorEnabled() {
        return instance != null && instance.isWaterColorEnabledInternal();
    }

    public static void setWaterColorEnabled(boolean enabled) {
        instance.setWaterColorEnabledInternal(enabled);
    }

    public static boolean isLavaColorEnabled() {
        return instance != null && instance.isLavaColorEnabledInternal();
    }

    public static void setLavaColorEnabled(boolean enabled) {
        instance.setLavaColorEnabledInternal(enabled);
    }

    public static boolean isWaterTransparencyEnabled() {
        return instance != null && instance.isWaterTransparencyEnabledInternal();
    }

    public static void setWaterTransparencyEnabled(boolean enabled) {
        instance.setWaterTransparencyEnabledInternal(enabled);
    }

    public static boolean isLavaTransparencyEnabled() {
        return instance != null && instance.isLavaTransparencyEnabledInternal();
    }

    public static void setLavaTransparencyEnabled(boolean enabled) {
        instance.setLavaTransparencyEnabledInternal(enabled);
    }

    public static boolean isWaterColorIntensityEnabled() {
        return instance != null && instance.isWaterColorIntensityEnabledInternal();
    }

    public static void setWaterColorIntensityEnabled(boolean enabled) {
        instance.setWaterColorIntensityEnabledInternal(enabled);
    }

    public static boolean isLavaColorIntensityEnabled() {
        return instance != null && instance.isLavaColorIntensityEnabledInternal();
    }

    public static void setLavaColorIntensityEnabled(boolean enabled) {
        instance.setLavaColorIntensityEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_liquid");
        blocked.waterColor = LiteApiManager.isFeatureBlocked("better_liquid_water_color");
        blocked.lavaColor = LiteApiManager.isFeatureBlocked("better_liquid_lava_color");
        blocked.waterTransparency = LiteApiManager.isFeatureBlocked("better_liquid_water_transparency");
        blocked.lavaTransparency = LiteApiManager.isFeatureBlocked("better_liquid_lava_transparency");
        blocked.waterColorIntensity = LiteApiManager.isFeatureBlocked("better_liquid_water_color_intensity");
        blocked.lavaColorIntensity = LiteApiManager.isFeatureBlocked("better_liquid_lava_color_intensity");
    }

    private boolean isEnabledInternal() {
        return config.isBetterLiquidEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterLiquidEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isWaterColorEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidWaterColorEnabled()
            && !blocked.waterColor;
    }

    private void setWaterColorEnabledInternal(boolean enabled) {
        config.setBetterLiquidWaterColorEnabled(enabled);
    }

    private boolean isLavaColorEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidLavaColorEnabled()
            && !blocked.lavaColor;
    }

    private void setLavaColorEnabledInternal(boolean enabled) {
        config.setBetterLiquidLavaColorEnabled(enabled);
    }

    private boolean isWaterTransparencyEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidWaterTransparencyEnabled()
            && !blocked.waterTransparency;
    }

    private void setWaterTransparencyEnabledInternal(boolean enabled) {
        config.setBetterLiquidWaterTransparencyEnabled(enabled);
    }

    private boolean isLavaTransparencyEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidLavaTransparencyEnabled()
            && !blocked.lavaTransparency;
    }

    private void setLavaTransparencyEnabledInternal(boolean enabled) {
        config.setBetterLiquidLavaTransparencyEnabled(enabled);
    }

    private boolean isWaterColorIntensityEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidWaterColorIntensityEnabled()
            && !blocked.waterColorIntensity;
    }

    private void setWaterColorIntensityEnabledInternal(boolean enabled) {
        config.setBetterLiquidWaterColorIntensityEnabled(enabled);
    }

    private boolean isLavaColorIntensityEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterLiquidLavaColorIntensityEnabled()
            && !blocked.lavaColorIntensity;
    }

    private void setLavaColorIntensityEnabledInternal(boolean enabled) {
        config.setBetterLiquidLavaColorIntensityEnabled(enabled);
    }

    public static int getWaterColor() {
        return instance.config.getBetterLiquidWaterColor() & 0xFFFFFF;
    }

    public static void setWaterColor(int rgb) {
        instance.config.setBetterLiquidWaterColor(rgb & 0xFFFFFF);
    }

    public static float getWaterRed() {
        return ((getWaterColor() >> 16) & 0xFF) / 255.0f;
    }

    public static float getWaterGreen() {
        return ((getWaterColor() >> 8) & 0xFF) / 255.0f;
    }

    public static float getWaterBlue() {
        return (getWaterColor() & 0xFF) / 255.0f;
    }


    public static int getLavaColor() {
        return instance.config.getBetterLiquidLavaColor() & 0xFFFFFF;
    }

    public static void setLavaColor(int rgb) {
        instance.config.setBetterLiquidLavaColor(rgb & 0xFFFFFF);
    }

    public static float getLavaRed() {
        return ((getLavaColor() >> 16) & 0xFF) / 255.0f;
    }

    public static float getLavaGreen() {
        return ((getLavaColor() >> 8) & 0xFF) / 255.0f;
    }

    public static float getLavaBlue() {
        return (getLavaColor() & 0xFF) / 255.0f;
    }

    public static int getWaterTransparency() {
        return instance.config.getBetterLiquidWaterTransparency();
    }

    public static void setWaterTransparency(int percentage) {
        instance.config.setBetterLiquidWaterTransparency(Math.max(0, Math.min(100, percentage)));
    }

    public static float getWaterAlpha() {
        return getWaterTransparency() / 100.0F;
    }

    public static int getLavaTransparency() {
        return instance.config.getBetterLiquidLavaTransparency();
    }

    public static void setLavaTransparency(int percentage) {
        instance.config.setBetterLiquidLavaTransparency(Math.max(0, Math.min(100, percentage)));
    }

    public static float getLavaAlpha() {
        return getLavaTransparency() / 100.0F;
    }

    public static int getWaterColorIntensity() {
        return instance.config.getBetterLiquidWaterColorIntensity();
    }

    public static void setWaterColorIntensity(int percentage) {
        instance.config.setBetterLiquidWaterColorIntensity(percentage);
    }

    public static int getLavaColorIntensity() {
        return instance.config.getBetterLiquidLavaColorIntensity();
    }

    public static void setLavaColorIntensity(int percentage) {
        instance.config.setBetterLiquidLavaColorIntensity(percentage);
    }
}