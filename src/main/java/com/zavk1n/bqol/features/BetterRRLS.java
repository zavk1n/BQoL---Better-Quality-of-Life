package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class BetterRRLS {
    private BetterRRLS() {}

    private static BetterRRLS instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    private boolean reloading;
    private boolean initialLoading;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterRRLS();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better RRLS initialized");
        }
    }

    public static BetterRRLS getInstance() {
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
        blocked.main = LiteApiManager.isFeatureBlocked("better_rrls");
    }

    private boolean isEnabledInternal() {
        return config.isBetterRRLSEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterRRLSEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    public void beginReload(boolean initial) {
        if (!isEnabledInternal()) {
            resetState();
            return;
        }

        reloading = true;
        initialLoading = initial;
    }

    public void finishReload() {
        resetState();
    }

    public boolean isReloading() {
        return isEnabledInternal() && reloading;
    }

    public boolean isInitialLoading() {
        return isReloading() && initialLoading;
    }

    public boolean shouldHideOverlay() {
        return isReloading() && !initialLoading;
    }

    public boolean shouldAllowInteraction() {
        return shouldHideOverlay();
    }

    private void resetState() {
        reloading = false;
        initialLoading = false;
    }
}