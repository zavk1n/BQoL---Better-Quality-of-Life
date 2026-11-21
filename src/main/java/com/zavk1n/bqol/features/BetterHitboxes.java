package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterHitboxes {
    private BetterHitboxes() {}

    private static BetterHitboxes instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHitboxes();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Hitboxes initialized");
        }
    }

    public static BetterHitboxes getInstance() {
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

    public static boolean isHitboxesColorEnabled() {
        return instance != null && instance.isHitboxesColorEnabledInternal();
    }

    public static void setHitboxesColorEnabled(boolean enabled) {
        getInstance().setHitboxesColorEnabledInternal(enabled);
    }

    public static int getHitboxesColor() {
        return getInstance().config.getBetterHitboxesColor() & 0xFFFFFF;
    }

    public static void setHitboxesColor(int rgb) {
        getInstance().config.setBetterHitboxesColor(rgb & 0xFFFFFF);
    }

    public static float getHitboxesRed() {
        return ((getHitboxesColor() >> 16) & 0xFF) / 255.0f;
    }

    public static float getHitboxesGreen() {
        return ((getHitboxesColor() >> 8) & 0xFF) / 255.0f;
    }

    public static float getHitboxesBlue() {
        return (getHitboxesColor() & 0xFF) / 255.0f;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_hitboxes");
    }

    private boolean isEnabledInternal() {
        return config.isBetterHitboxesEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHitboxesEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isHitboxesColorEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterHitboxesColorEnabled();
    }

    private void setHitboxesColorEnabledInternal(boolean enabled) {
        config.setBetterHitboxesColorEnabled(enabled);
    }
}