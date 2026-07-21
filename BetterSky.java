package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;

public class BetterSky {

    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }
        return client;
    }

    private BetterSky() {}

    private static BetterSky instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterSky();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("No Render initialized");
        }
    }

    public static BetterSky getInstance() {
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

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_sky");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();
    }

    private boolean isEnabledInternal() {
        return config.isBetterSkyEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterSkyEnabled(enabled);

        reloadFromConfigInternal();
    }

    /// Работа с цветами
    public static int getSkyColor() {
        return getInstance().config.getBetterSkyColor();
    }

    public static void setSkyColor(int rgb) {
        rgb &= 0xFFFFFF;

        BetterSky sky = getInstance();
        sky.config.setBetterSkyColor(rgb);

        reloadFromConfig();
    }

    public static float getRed() {
        return ((getSkyColor() >> 16) & 255) / 255F;
    }

    public static float getGreen() {
        return ((getSkyColor() >> 8) & 255) / 255F;
    }

    public static float getBlue() {
        return (getSkyColor() & 255) / 255F;
    }

    /// Работа со временем
    public static long getTime() {
        return getInstance().config.getBetterSkyTime();
    }

    public static void setTime(long time) {
        time = Math.max(1000, Math.min(24000, time));

        time = Math.round(time / 1000f) * 1000L;

        BetterSky sky = getInstance();
        sky.config.setBetterSkyTime(time);

        reloadFromConfig();
    }
}
