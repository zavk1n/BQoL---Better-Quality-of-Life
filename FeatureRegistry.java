package com.zavk1n.bqol.client.init;

import com.zavk1n.bqol.features.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/// Реестр всех фич
@Environment(EnvType.CLIENT)
public enum FeatureRegistry {

    BETTER_SPRINT(
        BetterSprint::initialize,
        BetterSprint::update,
        BetterSprint::refreshBlockedStatus
    ),
    BETTER_SOUNDS(
        BetterSounds::initialize,
        () -> {},
        BetterSounds::refreshBlockedStatus
    ),
    BETTER_SPHERES(
        BetterSpheres::initialize,
        () -> {},
        BetterSpheres::refreshBlockedStatus
    ),
    BETTER_SKY(
        BetterSky::initialize,
        () -> {},
        BetterSky::refreshBlockedStatus
    ),
    SHULKER_PARTICLES(
        ShulkerParticles::initialize,
        ShulkerParticles::onTick,
        ShulkerParticles::refreshBlockedStatus
    ),
    CUSTOM_FOG(
        CustomFog::initialize,
        () -> {},
        CustomFog::refreshBlockedStatus
    ),
    CUSTOM_HEALTH(
        CustomHealth::initialize,
        () -> {},
        CustomHealth::refreshBlockedStatus
    ),
    NO_RENDER(
        NoRender::initialize,
        () -> {},
        NoRender::refreshBlockedStatus
    );

    private static final FeatureRegistry[] VALUES = values();

    private final Runnable initializer;
    private final Runnable tickHandler;
    private final Runnable blockedStatusRefresher;

    FeatureRegistry(
        Runnable initializer,
        Runnable tickHandler,
        Runnable blockedStatusRefresher
    ) {
        this.initializer = initializer;
        this.tickHandler = tickHandler;
        this.blockedStatusRefresher = blockedStatusRefresher;
    }

    public void init() {
        initializer.run();
    }

    public void tick() {
        tickHandler.run();
    }

    public void refreshBlockedStatus() {
        blockedStatusRefresher.run();
    }

    public static void initializeAll() {
        for (FeatureRegistry feature : VALUES) {
            feature.init();
        }

        for (FeatureRegistry feature : VALUES) {
            feature.refreshBlockedStatus();
        }
    }
}
// v1.0