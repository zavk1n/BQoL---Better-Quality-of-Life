package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;

public class BetterHitCounter {
    private BetterHitCounter() {}

    private static BetterHitCounter instance;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    private static final long RESET_DELAY_MS = 10_000L;
    private static final long FADE_DURATION_MS = 180L;

    private int combo = 0;

    private long lastHitTime = 0L;
    private long fadeStartTime = -1L;

    private Entity trackedTarget;

    private boolean eventsRegistered = false;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHitCounter();
            instance.refreshBlockedStatusInternal();
            instance.registerEvents();
            BQoL.LOGGER.info("Better Hit Counter initialized");
        }
    }

    public static BetterHitCounter getInstance() {
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

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_hit_counter");
    }

    private boolean isEnabledInternal() {
        return config.isBetterHitCounterEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHitCounterEnabled(enabled);

        refreshBlockedStatusInternal();

        if (!isEnabledInternal()) {
            resetComboInternal();
        }
    }

    private void registerEvents() {
        if (eventsRegistered) return;

        eventsRegistered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!isEnabledInternal()
                || client.player == null
                || client.world == null) {

                resetComboInternal();
                return;
            }

            if (combo <= 0) return;

            long now = System.currentTimeMillis();

            if (trackedTarget == null
                || !trackedTarget.isAlive()
                || trackedTarget.isRemoved()
                || trackedTarget.getWorld() != client.world) {

                beginFade(now);
            }

            if (fadeStartTime < 0L && now - lastHitTime >= RESET_DELAY_MS) {
                beginFade(now);
            }

            if (fadeStartTime >= 0L && now - fadeStartTime >= FADE_DURATION_MS) {
                resetComboInternal();
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (!isEnabledInternal()
                || client.player == null
                || client.world == null
                || client.options.hudHidden
                || combo <= 0) {
                return;
            }

            long now = System.currentTimeMillis();

            float alpha = getAlpha(now);

            if (alpha <= 0.0F) {
                return;
            }

            int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));

            if (alphaByte < 4) {
                return;
            }

            int color = (alphaByte << 24) | 0x00C7C2FF;

            String text = "Combo: " + combo;

            int x = client.getWindow().getScaledWidth() / 2;
            int y = client.getWindow().getScaledHeight() - 77;

            context.drawCenteredTextWithShadow(client.textRenderer, text, x, y, color);
        });
    }

    private void registerHit(Entity target) {
        if (target == null || !target.isAlive()) return;

        long now = System.currentTimeMillis();

        if ((combo > 0 && (fadeStartTime >= 0L || now - lastHitTime >= RESET_DELAY_MS)) || trackedTarget != null && trackedTarget != target) {
            resetComboInternal();
        }

        trackedTarget = target;

        combo++;
        lastHitTime = now;

        fadeStartTime = -1L;
    }

    private void beginFade(long now) {
        if (combo <= 0 || fadeStartTime >= 0L) {
            return;
        }

        fadeStartTime = now;
    }

    private float getAlpha(long now) {
        if (fadeStartTime < 0L) {
            return 1.0F;
        }

        float progress = (float) (now - fadeStartTime) / (float) FADE_DURATION_MS;

        progress = Math.max(0.0F, Math.min(1.0F, progress));

        float inverse = 1.0F - progress;

        return inverse * inverse;
    }

    public static void onMissedAttack() {
        if (instance == null || !instance.isEnabledInternal()) {
            return;
        }

        instance.resetComboInternal();
    }

    public static void onDeath() {
        if (instance == null || !instance.isEnabledInternal()) {
            return;
        }

        instance.resetComboInternal();
    }

    public static void onAttack(Entity target) {
        if (instance == null || !instance.isEnabledInternal()) {
            return;
        }

        instance.registerHit(target);
    }

    private void resetComboInternal() {
        combo = 0;

        lastHitTime = 0L;
        fadeStartTime = -1L;

        trackedTarget = null;
    }
}