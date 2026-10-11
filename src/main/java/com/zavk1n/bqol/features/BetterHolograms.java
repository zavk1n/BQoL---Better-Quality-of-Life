package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.entity.decoration.ArmorStandEntity;

public class BetterHolograms {
    private BetterHolograms() {}

    private static BetterHolograms instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean visibleArmorStand;
        boolean antiHolograms;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHolograms();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Holograms initialized");
        }
    }

    public static BetterHolograms getInstance() {
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

    public static boolean isVisibleArmorStandEnabled() {
        return instance != null && instance.isVisibleArmorStandEnabledInternal();
    }

    public static void setVisibleArmorStandEnabled(boolean enabled) {
        if (instance != null) instance.setVisibleArmorStandEnabledInternal(enabled);
    }

    public static boolean isAntiHologramsEnabled() {
        return instance != null && instance.isAntiHologramsEnabledInternal();
    }

    public static void setAntiHologramsEnabled(boolean enabled) {
        if (instance != null) instance.setAntiHologramsEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_holograms");
        blocked.visibleArmorStand = LiteApiManager.isFeatureBlocked("better_holograms_visible_armor_stand");
        blocked.antiHolograms = LiteApiManager.isFeatureBlocked("better_holograms_anti_holograms");
    }

    private boolean isEnabledInternal() {
        return config.isBetterHologramsEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHologramsEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isVisibleArmorStandEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterHologramsVisibleArmorStand()
            && !blocked.visibleArmorStand;
    }

    private void setVisibleArmorStandEnabledInternal(boolean enabled) {
        config.setBetterHologramsVisibleArmorStand(enabled);
    }

    private boolean isAntiHologramsEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterHologramsAntiHolograms()
            && !blocked.antiHolograms;
    }

    private void setAntiHologramsEnabledInternal(boolean enabled) {
        config.setBetterHologramsAntiHolograms(enabled);
    }

    public static boolean isHologram(ArmorStandEntity stand) {
        return stand.hasCustomName();
    }
}