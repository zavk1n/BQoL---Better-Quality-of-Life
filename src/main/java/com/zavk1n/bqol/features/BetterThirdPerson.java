package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public class BetterThirdPerson {

    private static BetterThirdPerson instance;

    private final BQoLConfig config = BQoLConfig.getInstance();
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
    }

    private static final float MAX_PITCH = 90.0F;

    private boolean initialized;

    private float cameraYaw;
    private float cameraPitch;

    private float previousCameraYaw;
    private float previousCameraPitch;

    private BetterThirdPerson() {}

    public static void initialize() {
        if (instance == null) {
            instance = new BetterThirdPerson();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Third Person initialized");
        }
    }

    public static BetterThirdPerson getInstance() {
        if (instance == null) {
            initialize();
        }

        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) {
            instance.refreshBlockedStatusInternal();
        }
    }

    public static boolean isEnabled() {
        return instance != null
            && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_third_person");
    }

    private boolean isEnabledInternal() {
        return config.isBetterThirdPersonEnabled()
            && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        BetterThirdPerson feature = getInstance();

        feature.refreshBlockedStatusInternal();

        if (feature.blocked.main) {
            feature.config.setBetterThirdPersonEnabled(false);
            feature.initialized = false;
            return;
        }

        feature.config.setBetterThirdPersonEnabled(enabled);
        feature.reset();
    }

    public boolean shouldControlCamera() {
        MinecraftClient client = MinecraftClient.getInstance();

        return isEnabled()
            && client.player != null
            && client.world != null
            && client.options.getPerspective() != Perspective.FIRST_PERSON;
    }

    public void reset() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            initialized = false;
            return;
        }

        cameraYaw = player.getYaw();
        cameraPitch = player.getPitch();

        previousCameraYaw = cameraYaw;
        previousCameraPitch = cameraPitch;

        initialized = true;
    }

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!shouldControlCamera()) {
            initialized = false;
            return;
        }

        ClientPlayerEntity player = client.player;

        if (player == null) {
            initialized = false;
            return;
        }

        if (!initialized) {
            reset();
            return;
        }

        previousCameraYaw = cameraYaw;
        previousCameraPitch = cameraPitch;

        cameraYaw = player.getYaw();
        cameraPitch = MathHelper.clamp(player.getPitch(), -MAX_PITCH, MAX_PITCH);
    }

    public float getCameraYaw(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return cameraYaw;
        }

        return client.player.getYaw(tickDelta);
    }

    public float getCameraPitch(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return cameraPitch;
        }

        return MathHelper.clamp(client.player.getPitch(tickDelta), -MAX_PITCH, MAX_PITCH);
    }
}