package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.binds.FeatureKeybindManager;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

public class BetterZoom {
    private BetterZoom() {}

    private static BetterZoom instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
    }

    private static final double DEFAULT_DIVISOR = 2.0;
    private static final double MIN_DIVISOR = 1.0;
    private static final double MAX_DIVISOR = 50.0;

    private static final double SCROLL_FACTOR = 1.25;

    private static final float SMOOTH_FACTOR = 0.5F;
    private static final float EPSILON = 0.0001F;

    private static boolean zooming;

    private static double zoomDivisor = DEFAULT_DIVISOR;

    private static float multiplier = 1.0F;
    private static float lastMultiplier = 1.0F;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterZoom();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("Better Zoom Invisible initialized");
        }
    }

    public static BetterZoom getInstance() {
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
        blocked.main = LiteApiManager.isFeatureBlocked("better_zoom");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();
    }

    private boolean isEnabledInternal() {
        return config.isBetterZoomEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterZoomEnabled(enabled);

        reloadFromConfigInternal();
    }

    public static void tick() {
        if (!isEnabled()) {
            reset();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        boolean canZoom = client.player != null
            && client.world != null
            && client.currentScreen == null
            && client.isWindowFocused()
            && FeatureKeybindManager.isFeatureKeyPressed("better_zoom");

        setZooming(canZoom);

        lastMultiplier = multiplier;

        float target = isZoomActive() ? (float) (1.0 / zoomDivisor) : 1.0F;

        multiplier += (target - multiplier) * SMOOTH_FACTOR;

        if (Math.abs(multiplier - target) < EPSILON) {
            multiplier = target;
        }
    }

    public static boolean isZooming() {
        return zooming;
    }

    public static boolean isZoomActive() {
        return zooming && !isUsingSpyglass();
    }

    public static boolean canSeeDistantEntities() {
        return isZoomActive();
    }

    private static void setZooming(boolean active) {
        if (zooming == active) {
            return;
        }

        zooming = active;

        if (!active) {
            zoomDivisor = DEFAULT_DIVISOR;
        }
    }

    public static double getZoomDivisor() {
        return zoomDivisor;
    }

    public static float getMultiplier(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastMultiplier, multiplier);
    }

    public static boolean isTransitionActive() {
        return multiplier != 1.0F || lastMultiplier != 1.0F;
    }

    public static void scroll(boolean increase) {
        if (!isZoomActive()) {
            return;
        }

        double factor = increase ? SCROLL_FACTOR : 1.0 / SCROLL_FACTOR;

        zoomDivisor = MathHelper.clamp(zoomDivisor * factor, MIN_DIVISOR, MAX_DIVISOR);
    }

    public static boolean isUsingSpyglass() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        return player != null
            && player.isUsingItem()
            && player.getActiveItem().isOf(Items.SPYGLASS);
    }

    public static void reset() {
        zooming = false;
        zoomDivisor = DEFAULT_DIVISOR;

        multiplier = 1.0F;
        lastMultiplier = 1.0F;
    }
}