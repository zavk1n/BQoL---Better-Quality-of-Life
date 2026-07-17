package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;

public class NoRender {

    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }
        return client;
    }

    private NoRender() {}

    private static NoRender instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean totemOverlay;
        boolean fireOverlay;
        boolean portalOverlay;
        boolean weather;
        boolean fireworks;
        boolean players;
        boolean hand;
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new NoRender();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("No Render initialized");
        }
    }

    public static NoRender getInstance() {
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
        blocked.main = LiteApiManager.isFeatureBlocked("no_render");
        blocked.totemOverlay = LiteApiManager.isFeatureBlocked("no_render_totem");
        blocked.fireOverlay = LiteApiManager.isFeatureBlocked("no_render_fire");
        blocked.weather = LiteApiManager.isFeatureBlocked("no_render_weather");
        blocked.fireworks = LiteApiManager.isFeatureBlocked("no_render_fireworks");
        blocked.players = LiteApiManager.isFeatureBlocked("no_render_players");
        blocked.hand = LiteApiManager.isFeatureBlocked("no_render_hand");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();
    }

    private boolean isEnabledInternal() {
        return config.isNoRenderEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setNoRenderEnabled(enabled);

        reloadFromConfigInternal();
    }

    public boolean isTotemOverlayEnabled() {
        return isEnabledInternal() && !blocked.totemOverlay;
    }

    public BQoLConfig.RenderMode getTotemMode() {
        return config.getNoRenderTotemOverlay();
    }

    public boolean isFireOverlayEnabled() {
        return isEnabledInternal() && !blocked.fireOverlay;
    }

    public BQoLConfig.RenderMode getFireMode() {
        return config.getNoRenderFireOverlay();
    }

    public boolean isWeatherEnabled() {
        return isEnabledInternal() && !blocked.weather;
    }

    public BQoLConfig.RenderMode getWeatherMode() {
        return config.getNoRenderWeather();
    }

    public boolean isFireworksEnabled() {
        return isEnabledInternal() && !blocked.fireworks;
    }

    public BQoLConfig.RenderMode getFireworksMode() {
        return config.getNoRenderFireworks();
    }

    public boolean isPlayersEnabled() {
        return isEnabledInternal() && !blocked.players;
    }

    public BQoLConfig.RenderMode getPlayersMode() {
        return config.getNoRenderPlayers();
    }

    public boolean isHandEnabled() {
        return isEnabledInternal() && !blocked.hand;
    }

    public BQoLConfig.RenderMode getHandMode() {
        return config.getNoRenderHand();
    }
}