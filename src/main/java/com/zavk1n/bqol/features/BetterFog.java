package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;

public class BetterFog {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterFog() {}

    private static BetterFog instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean noFog;
        boolean nightVision;
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterFog();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("BetterFog initialized");
        }
    }

    public static BetterFog getInstance() {
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

    public static boolean isNoFogEnabled() {
        return instance != null && instance.isNoFogEnabledInternal();
    }

    public static void setNoFogEnabled(boolean enabled) {
        if (instance != null) instance.setNoFogEnabledInternal(enabled);
    }

    public static boolean isNightVisionEnabled() {
        return instance != null && instance.isNightVisionEnabledInternal();
    }

    public static void setNightVisionEnabled(boolean enabled) {
        if (instance != null) instance.setNightVisionEnabledInternal(enabled);
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_fog");
        blocked.noFog = LiteApiManager.isFeatureBlocked("better_fog_no_fog");
        blocked.nightVision = LiteApiManager.isFeatureBlocked("better_fog_night_vision");
    }

    private boolean isEnabledInternal() {
        return config.isBetterFogEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterFogEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isNoFogEnabledInternal() {
        return isEnabledInternal() && config.isNoFog() && !blocked.noFog;
    }

    private void setNoFogEnabledInternal(boolean enabled) {
        config.setNoFog(enabled);
    }

    private boolean isNightVisionEnabledInternal() {
        return isEnabledInternal() && config.isNightVision() && !blocked.nightVision;
    }

    private void setNightVisionEnabledInternal(boolean enabled) {
        config.setNightVision(enabled);
    }
}
// v1.0