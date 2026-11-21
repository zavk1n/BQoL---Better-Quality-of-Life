package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.accessors.ACCESSORHitColorOverlay;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterHitColor {
    private BetterHitColor() {}

    private static BetterHitColor instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHitColor();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Hit Color initialized");
        }
    }

    public static BetterHitColor getInstance() {
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

    public static boolean isHitColorColorEnabled() {
        return instance != null && instance.isHitColorColorEnabledInternal();
    }

    public static void setHitColorColorEnabled(boolean enabled) {
        getInstance().setHitColorColorEnabledInternal(enabled);
    }

    public static int getHitColorColor() {
        return getInstance().config.getBetterHitColor() & 0xFFFFFF;
    }

    public static void setHitColorColor(int rgb) {
        getInstance().config.setBetterHitColor(rgb & 0xFFFFFF);
        ACCESSORHitColorOverlay.reload();
    }

    public static float getHitColorRed() {
        return ((getHitColorColor() >> 16) & 0xFF) / 255.0f;
    }

    public static float getHitColorGreen() {
        return ((getHitColorColor() >> 8) & 0xFF) / 255.0f;
    }

    public static float getHitColorBlue() {
        return (getHitColorColor() & 0xFF) / 255.0f;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_hit_color");
    }

    private boolean isEnabledInternal() {
        return config.isBetterHitColorEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHitColorEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isHitColorColorEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterHitColorColorEnabled();
    }

    private void setHitColorColorEnabledInternal(boolean enabled) {
        config.setBetterHitColorColorEnabled(enabled);
    }
}