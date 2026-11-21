package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.mixin.ACCESSORGammaOption;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public class BetterGamma {
    private BetterGamma() {}

    private static BetterGamma instance;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
    }

    private boolean gammaApplied;
    private double initialGamma = -1.0D;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterGamma();
            instance.refreshBlockedStatusInternal();

            BQoL.LOGGER.info("Better Gamma initialized");
        }
    }

    public static BetterGamma getInstance() {
        if (instance == null) {
            initialize();
        }

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
        boolean wasBlocked = blocked.main;

        blocked.main = LiteApiManager.isFeatureBlocked("better_gamma");

        if (!wasBlocked && blocked.main) {
            restoreGamma();
        } else if (wasBlocked && !blocked.main && isEnabledInternal()) {
            applyGamma();
        }
    }

    private boolean isEnabledInternal() {
        return config.isBetterGammaEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterGammaEnabled(enabled);

        if (enabled && !blocked.main) {
            applyGamma();
        } else {
            restoreGamma();
        }

        refreshBlockedStatusInternal();
    }

    public void tick(MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }

        refreshBlockedStatusInternal();

        if (isEnabledInternal()) {
            applyGamma();
        } else {
            restoreGamma();
        }
    }

    private void syncGamma() {
        if (isEnabledInternal()) {
            applyGamma();
        } else {
            restoreGamma();
        }
    }

    private void applyGamma() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc == null || mc.options == null) {
            return;
        }

        SimpleOption<Double> gammaOption = mc.options.getGamma();

        if (gammaApplied) {
            ((ACCESSORGammaOption) (Object) gammaOption).bqol$setValue(15.0D);
            return;
        }

        initialGamma = gammaOption.getValue();

        ((ACCESSORGammaOption) (Object) gammaOption).bqol$setValue(15.0D);

        gammaApplied = true;
    }

    private void restoreGamma() {
        if (!gammaApplied) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc != null && mc.options != null) {
            SimpleOption<Double> gammaOption = mc.options.getGamma();

            ((ACCESSORGammaOption) (Object) gammaOption).bqol$setValue(initialGamma);
        }

        initialGamma = -1.0D;
        gammaApplied = false;
    }
}