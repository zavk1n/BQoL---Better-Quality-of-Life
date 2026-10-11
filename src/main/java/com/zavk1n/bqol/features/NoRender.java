package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

public class NoRender {
    private NoRender() {}

    private static NoRender instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

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

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("no_render");
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
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getTotemMode() {
        return config.getNoRenderTotemOverlay();
    }

    public boolean isFireOverlayEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getFireMode() {
        return config.getNoRenderFireOverlay();
    }

    public boolean isTotemParticlesEnabled() { return isEnabledInternal(); }

    public BQoLConfig.RenderMode getTotemParticlesMode() {
        return config.getNoRenderTotemParticles();
    }

    public boolean isPotionParticlesEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getPotionParticlesMode() {
        return config.getNoRenderPotionParticles();
    }

    public boolean isExplosionEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getExplosionMode() {
        return config.getNoRenderExplosion();
    }

    public boolean isSmokeEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getSmokeMode() {
        return config.getNoRenderSmoke();
    }

    public boolean isBubblesEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getBubblesMode() {
        return config.getNoRenderBubbles();
    }

    public boolean isWeatherEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getWeatherMode() {
        return config.getNoRenderWeather();
    }

    public boolean isArrowsEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getArrowsMode() {
        return config.getNoRenderArrows();
    }

    public boolean isFireworksEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getFireworksMode() {
        return config.getNoRenderFireworks();
    }

    public boolean isNamesEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getNamesMode() {
        return config.getNoRenderNames();
    }

    public boolean isPlayersEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getPlayersMode() {
        return config.getNoRenderPlayers();
    }

    public boolean isHandEnabled() {
        return isEnabledInternal();
    }

    public BQoLConfig.RenderMode getHandMode() {
        return config.getNoRenderHand();
    }
}