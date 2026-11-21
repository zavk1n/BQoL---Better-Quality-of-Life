package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterAntiInvisible {
    private BetterAntiInvisible() {}

    private static BetterAntiInvisible instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterAntiInvisible();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("Better Anti Invisible initialized");
        }
    }

    public static BetterAntiInvisible getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) {
            instance.refreshBlockedStatusInternal();
        }
    }

    public static void reloadFromConfig() {
        if (instance != null) {
            instance.reloadFromConfigInternal();
        }
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) {
            instance.setEnabledInternal(enabled);
        }
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_anti_invisible");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();
    }

    private boolean isEnabledInternal() {
        return config.isBetterAntiInvisibleEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterAntiInvisibleEnabled(enabled);

        reloadFromConfigInternal();
    }
}