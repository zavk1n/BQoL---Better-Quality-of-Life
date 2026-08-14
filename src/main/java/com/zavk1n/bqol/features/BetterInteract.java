package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;

public class BetterInteract {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterInteract() {}

    private static BetterInteract instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean clickThrough;
        boolean autoSigns;
        boolean antiSigns;
        boolean safeHarvest;
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterInteract();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Interact initialized");
        }
    }

    public static BetterInteract getInstance() {
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

    public static boolean isClickThroughEnabled() {
        return instance != null && instance.isClickThroughEnabledInternal();
    }

    public static void setClickThroughEnabled(boolean enabled) {
        if (instance != null) instance.setClickThroughEnabledInternal(enabled);
    }

    public static boolean isAntiSignsEnabled() {
        return instance != null && instance.isAntiSignsEnabledInternal();
    }

    public static void setAntiSignsEnabled(boolean enabled) {
        if (instance != null) instance.setAntiSignsEnabledInternal(enabled);
    }

    public static boolean isAutoSignsEnabled() {
        return instance != null && instance.isAutoSignsEnabledInternal();
    }

    public static void setAutoSignsEnabled(boolean enabled) {
        if (instance != null) instance.setAutoSignsEnabledInternal(enabled);
    }

    public static boolean isSafeHarvestEnabled() {
        return instance != null && instance.isSafeHarvestEnabledInternal();
    }

    public static void setSafeHarvestEnabled(boolean enabled) {
        if (instance != null) instance.setSafeHarvestEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_interact");
        blocked.clickThrough = LiteApiManager.isFeatureBlocked("better_interact_click_through");
        blocked.antiSigns = LiteApiManager.isFeatureBlocked("better_interact_anti_signs");
        blocked.autoSigns = LiteApiManager.isFeatureBlocked("better_interact_auto_signs");
        blocked.safeHarvest = LiteApiManager.isFeatureBlocked("better_interact_safe_harvest");
    }

    private boolean isEnabledInternal() {
        return config.isBetterInteractEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterInteractEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isClickThroughEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterInteractClickThrough()
            && !blocked.clickThrough;
    }

    private void setClickThroughEnabledInternal(boolean enabled) {
        config.setBetterInteractClickThrough(enabled);
    }

    private boolean isAntiSignsEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterInteractAntiSigns()
            && !blocked.antiSigns;
    }

    private void setAntiSignsEnabledInternal(boolean enabled) {
        config.setBetterInteractAntiSigns(enabled);
    }

    private boolean isAutoSignsEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterInteractAutoSigns()
            && !blocked.autoSigns;
    }

    private void setAutoSignsEnabledInternal(boolean enabled) {
        config.setBetterInteractAutoSigns(enabled);
    }

    private boolean isSafeHarvestEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterInteractSafeHarvest()
            && !blocked.safeHarvest;
    }

    private void setSafeHarvestEnabledInternal(boolean enabled) {
        config.setBetterInteractSafeHarvest(enabled);
    }
}
// v1.0