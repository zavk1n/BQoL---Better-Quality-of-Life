package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterHurtCam {
    private BetterHurtCam() {}

    private static BetterHurtCam instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHurtCam();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better HurtCam initialized");
        }
    }

    public static BetterHurtCam getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_hurt_cam");
    }

    private boolean isEnabledInternal() {
        return config.isBetterHurtCamEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHurtCamEnabled(enabled);

        refreshBlockedStatusInternal();
    }
}