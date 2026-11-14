package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.UUID;

public class BetterHealth {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterHealth() {}

    private static BetterHealth instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private final IndicatorState state = new IndicatorState();
    private static final class IndicatorState {
        UUID lastArmorPlayerUuid;

        UUID targetUuid;
        PlayerEntity target;

        PlayerEntity pendingTarget;
        UUID pendingTargetUuid;
        long pendingAttackTick;

        long lastDamageTick;
        long visibleUntilTick;

        Text cachedRenderText;

        float lastCachedHealth = -1f;
        float lastCachedAbsorption = -1f;

        float targetHealthBeforeHit = -1.0f;
        boolean damageConfirmed;

        int lastSettingsHash;

        void clearCache() {
            lastArmorPlayerUuid = null;

            cachedRenderText = null;

            lastCachedHealth = -1f;
            lastCachedAbsorption = -1f;

            lastSettingsHash = 0;
        }
    }

    private final IndicatorAnimation animation = new IndicatorAnimation();
    private static final class IndicatorAnimation {
        private static final float HIDDEN_SCALE = 0.90f;
        private static final float VISIBLE_SCALE = 1.1f;

        private static final float FADE_SPEED = 12.0f;
        private static final float SCALE_SPEED = 16.0f;

        float currentAlpha = 0.0f;
        float targetAlpha = 0.0f;

        float currentScale = HIDDEN_SCALE;
        float targetScale = HIDDEN_SCALE;

        void reset() {
            currentAlpha = 0.0f;
            targetAlpha = 0.0f;

            currentScale = HIDDEN_SCALE;
            targetScale = HIDDEN_SCALE;
        }

        void show(boolean scalingEnabled, boolean instant) {
            targetAlpha = 1.0f;

            if (!scalingEnabled) {
                currentScale = VISIBLE_SCALE;
                targetScale = VISIBLE_SCALE;

                if (instant) {
                    currentAlpha = 1.0f;
                }

                return;
            }

            targetScale = VISIBLE_SCALE;

            if (instant) {
                currentAlpha = 1.0f;
                currentScale = VISIBLE_SCALE;
            }
        }

        void hide(boolean scalingEnabled) {
            targetAlpha = 0.0f;

            if (scalingEnabled) {
                targetScale = HIDDEN_SCALE;
            } else {
                currentScale = VISIBLE_SCALE;
                targetScale = VISIBLE_SCALE;
            }
        }

        void update(float deltaSeconds, boolean scalingEnabled) {
            deltaSeconds = MathHelper.clamp(deltaSeconds, 0.0f, 0.05f);

            float alphaFactor = 1.0f - (float) Math.exp(-FADE_SPEED * deltaSeconds);

            currentAlpha = MathHelper.lerp(alphaFactor, currentAlpha, targetAlpha);

            if (Math.abs(currentAlpha - targetAlpha) < 0.001f) {
                currentAlpha = targetAlpha;
            }

            if (!scalingEnabled) {
                currentScale = VISIBLE_SCALE;
                targetScale = VISIBLE_SCALE;
                return;
            }

            float scaleFactor = 1.0f - (float) Math.exp(-SCALE_SPEED * deltaSeconds);

            currentScale = MathHelper.lerp(scaleFactor, currentScale, targetScale);

            if (Math.abs(currentScale - targetScale) < 0.001f) {
                currentScale = targetScale;
            }
        }

        boolean isFinished() {
            return targetAlpha == 0.0f && currentAlpha <= 0.001f;
        }
    }

    private record RenderData(
        Text text,
        float scale,
        float alpha
    ) {}

    private net.minecraft.world.World lastWorld = null;

    private long lastUpdateTick = -1;

    private int durationTicks = 100;
    private int location = 0;

    private boolean wasFirstPerson = true;
    private boolean hidingIndicator = false;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterHealth();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            instance.init();
            BQoL.LOGGER.info("Better Health initialized");
        }
    }

    public static BetterHealth getInstance() {
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

    public static void resetDisplay() {
        if (instance != null) {
            instance.resetDisplayInternal();
        }
    }

    public static void onAttack(PlayerEntity target) {
        getInstance().onAttackInternal(target);
    }

    public static PlayerEntity getLastAttackedPlayer() {
        return instance != null ? instance.state.target : null;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_health");
    }

    private void reloadFromConfigInternal() {
        durationTicks = Math.max(1, (int) Math.ceil(config.getBetterHealthDuration() / 50.0));

        location = config.getBetterHealthPosition();

        refreshBlockedStatusInternal();

        state.clearCache();
    }

    private boolean isEnabledInternal() {
        return config.isBetterHealthEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterHealthEnabled(enabled);

        if (!enabled) {
            resetAllStates();
        }

        reloadFromConfigInternal();
    }

    private void resetDisplayInternal() {
        hidingIndicator = false;

        animation.reset();

        state.target = null;
        state.targetUuid = null;

        cancelPendingAttack();

        state.lastDamageTick = 0;
        state.visibleUntilTick = 0;

        state.clearCache();

        lastWorld = null;
        wasFirstPerson = true;
        lastUpdateTick = -1;

        state.targetHealthBeforeHit = -1.0f;
        state.damageConfirmed = false;
    }

    private void clearTargetAfterFade() {
        if (!hidingIndicator || !animation.isFinished()) {
            return;
        }

        state.target = null;
        state.targetUuid = null;

        cancelPendingAttack();

        state.lastDamageTick = 0;
        state.visibleUntilTick = 0;

        state.clearCache();

        animation.reset();

        hidingIndicator = false;
    }

    private void startHideAnimation() {
        if (state.target == null) {
            hidingIndicator = false;
            animation.reset();
            return;
        }

        if (hidingIndicator) {
            return;
        }

        hidingIndicator = true;

        animation.hide(config.isBetterHealthScaling());
    }

    private void onAttackInternal(PlayerEntity target) {
        MinecraftClient minecraft = mc();

        if (minecraft.player == null
            || minecraft.world == null
            || !isEnabledInternal()
            || target == null
            || target == minecraft.player
            || target.isRemoved()
            || target.isDead()
            || target.getHealth() <= 0) {
            return;
        }

        boolean newTarget = state.targetUuid == null || !state.targetUuid.equals(target.getUuid());

        if (newTarget) {
            state.targetHealthBeforeHit = target.getHealth();
            state.damageConfirmed = false;
        }

        state.target = target;
        state.targetUuid = target.getUuid();

        long tick = minecraft.world.getTime();

        state.lastDamageTick = tick;
        state.visibleUntilTick = tick + durationTicks;

        hidingIndicator = false;

        cancelPendingAttack();

        if (newTarget) {
            state.clearCache();
            animation.reset();
        }

        if (config.isBetterHealthHovering() && !isHoverOnTarget(minecraft)) {
            animation.hide(config.isBetterHealthScaling());
            return;
        }

        animation.show(config.isBetterHealthScaling(), false);
    }

    private boolean hasDamageBeenConfirmed(PlayerEntity target) {
        if (target == null) {
            return false;
        }

        if (state.damageConfirmed) {
            return true;
        }

        float currentHealth = target.getHealth();

        if (state.targetHealthBeforeHit > 0.0f && currentHealth < state.targetHealthBeforeHit) {
            state.damageConfirmed = true;
        }

        return state.damageConfirmed;
    }

    private void updatePendingAttack(MinecraftClient client) {
        PlayerEntity pending = state.pendingTarget;

        if (pending == null || client.world == null) {
            return;
        }

        long tick = client.world.getTime();

        if (tick - state.pendingAttackTick > 10
            || pending.isRemoved()
            || pending.isDead()
            || pending.getHealth() <= 0) {
            cancelPendingAttack();
            return;
        }

        boolean newTarget = state.targetUuid == null
            || !state.targetUuid.equals(pending.getUuid());

        state.target = pending;
        state.targetUuid = pending.getUuid();

        state.lastDamageTick = tick;
        state.visibleUntilTick = tick + durationTicks;

        hidingIndicator = false;

        if (newTarget) {
            state.clearCache();
            animation.reset();
        }

        animation.show(config.isBetterHealthScaling(), false);

        cancelPendingAttack();
    }

    private void updateTargetReference(MinecraftClient client) {
        if (state.targetUuid == null || client.world == null) {
            return;
        }

        if (state.target != null && !state.target.isRemoved()) {
            return;
        }

        PlayerEntity refreshed = client.world.getPlayerByUuid(state.targetUuid);

        if (refreshed != null && !refreshed.isDead() && refreshed.getHealth() > 0) {
            state.target = refreshed;
        }
    }

    private void init() {
        reloadFromConfigInternal();

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (!isEnabledInternal()) {
                return;
            }

            float deltaSeconds = tickCounter.getDynamicDeltaTicks() / 20.0f;

            renderIndicator(client, drawContext, deltaSeconds);
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::update);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetDisplayInternal());
    }

    private void update(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        long worldTick = client.world.getTime();

        if (worldTick == lastUpdateTick) {
            return;
        }

        lastUpdateTick = worldTick;

        checkWorldChange(client);
        checkDeaths(client);

        if (client.player.isDead() || client.player.getHealth() <= 0) {
            cancelPendingAttack();
            startHideAnimation();
            return;
        }

        updatePendingAttack(client);

        boolean firstPerson = client.options.getPerspective().isFirstPerson();

        if (firstPerson && !wasFirstPerson
            && state.target != null
            && !hidingIndicator
            && worldTick < state.visibleUntilTick) {
            animation.show(config.isBetterHealthScaling(), false);
        }

        wasFirstPerson = firstPerson;

        if (state.target == null || hidingIndicator) {
            return;
        }

        updateTargetReference(client);

        PlayerEntity target = state.target;

        if (target == null) {
            return;
        }

        if (target.isDead()
            || target.getHealth() <= 0
            || target.isRemoved()) {
            resetDisplayInternal();
            return;
        }

        if (worldTick >= state.visibleUntilTick) {
            startHideAnimation();
            return;
        }

        boolean shouldShow = true;

        if (client.player.squaredDistanceTo(target) > 128.0 * 128.0) {
            shouldShow = false;
        }

        if (shouldShow && config.isBetterHealthPvPMode()) {
            if (!hasDamageBeenConfirmed(target) || shouldHideIndicator(target)) {
                shouldShow = false;
            }
        }

        if (shouldShow
            && config.isBetterHealthHovering()
            && !isHoverOnTarget(client)) {
            shouldShow = false;
        }

        if (shouldShow) {
            animation.show(config.isBetterHealthScaling(), false);
        } else {
            animation.hide(config.isBetterHealthScaling());
        }
    }


    private RenderData prepareRenderData(PlayerEntity player) {
        if (player == null || player.isRemoved()) {
            return null;
        }

        float normalHealth = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float totalHealth = normalHealth + absorption;

        int settingsHash = computeSettingsHash();

        boolean rebuild = state.cachedRenderText == null
            || Math.abs(state.lastCachedHealth - normalHealth) > 0.01f
            || Math.abs(state.lastCachedAbsorption - absorption) > 0.01f
            || state.lastSettingsHash != settingsHash;

        if (rebuild) {
            state.cachedRenderText = buildDisplayText(normalHealth, absorption, totalHealth, config.isBetterHealthDecimal(), config.isBetterHealthGoldenHearts(), config.isBetterHealthGoldenHeartsPlus());

            state.lastCachedHealth = normalHealth;
            state.lastCachedAbsorption = absorption;
            state.lastSettingsHash = settingsHash;
        }

        float healthScale;

        if (normalHealth > 15.0f) {
            healthScale = 1.1f;
        } else if (normalHealth > 10.0f) {
            healthScale = 1.25f;
        } else if (normalHealth > 5.0f) {
            healthScale = 1.35f;
        } else {
            healthScale = 1.5f;
        }

        float animationScale = config.isBetterHealthScaling() ? animation.currentScale : 1.0f;
        float finalScale = healthScale * animationScale;

        return new RenderData(state.cachedRenderText, finalScale, animation.currentAlpha);
    }

    private Text buildDisplayText(float normalHealth, float absorption, float totalHealth, boolean useDecimal, boolean goldenHearts, boolean goldenPlus) {
        if (goldenHearts && absorption > 0) {
            String normalStr = formatIndicatorString(normalHealth, useDecimal);
            String goldenStr = formatIndicatorString(absorption, useDecimal);

            Formatting normalColor = getIndicatorFormatting(totalHealth);

            return Text.empty()
                .append(Text.literal(normalStr).setStyle(Style.EMPTY.withColor(normalColor)))
                .append(Text.literal(goldenPlus ? " + " : " ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                .append(Text.literal("(").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(Text.literal(goldenStr).setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(Text.literal(")").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));

        } else {
            String healthStr = formatIndicatorString(totalHealth, useDecimal);
            Formatting color = getIndicatorFormatting(totalHealth);

            return Text.literal(healthStr).setStyle(Style.EMPTY.withColor(color));
        }
    }

    private String formatIndicatorString(float health, boolean useDecimal) {
        if (useDecimal) {
            return String.format(Locale.ROOT, "%.1f", health);
        }

        return String.valueOf((int) Math.ceil(health));
    }

    private Formatting getIndicatorFormatting(float health) {
        if (health <= 5.0f) {
            return Formatting.RED;
        }

        if (health <= 10.0f) {
            return Formatting.GOLD;
        }

        if (health <= 15.0f) {
            return Formatting.YELLOW;
        }

        return Formatting.GREEN;
    }

    private void renderIndicator(MinecraftClient client, DrawContext context, float deltaSeconds) {
        if (client == null || context == null) {
            return;
        }

        boolean scalingEnabled = config.isBetterHealthScaling();

        animation.update(deltaSeconds, scalingEnabled);

        if (hidingIndicator && animation.isFinished()) {
            clearTargetAfterFade();
        }

        if (client.player == null || client.world == null) {
            return;
        }

        if (!client.options.getPerspective().isFirstPerson()) {
            return;
        }

        if (isGuiHidden(client)) {
            return;
        }

        PlayerEntity target = state.target;

        if (target == null || target.isRemoved() || target.isDead() || target.getHealth() <= 0) {
            return;
        }

        if (animation.currentAlpha <= 0.001f) {
            return;
        }

        RenderData data = prepareRenderData(target);

        if (data == null || data.text() == null) {
            return;
        }

        int color = data.text().getStyle().getColor() != null ? data.text().getStyle().getColor().getRgb() : 0xFFFFFF;

        render(client, context, data.text(), animation.currentAlpha, data.scale(), location, scalingEnabled, color);
    }

    public static void render(MinecraftClient client, DrawContext context, Text text, float alpha, float scale, int location, boolean scalingEnabled, int color) {
        if (client == null
            || client.player == null
            || client.world == null
            || context == null
            || text == null) {
            return;
        }

        if (!client.options.getPerspective().isFirstPerson()) {
            return;
        }

        if (alpha <= 0.001f) {
            return;
        }

        TextRenderer renderer = client.textRenderer;

        if (renderer == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = screenWidth / 2;
        int y = screenHeight / 2 - 25;

        switch (location) {
            case 1 -> {
                x -= 25;
                y += 20;
            }
            case 2 -> {
                x += 35;
                y += 20;
            }
            case 3 -> {
                y += 41;
            }
            default -> {
                x += 2;
            }
        }

        float finalScale = scalingEnabled ? MathHelper.clamp(scale, 0.90F, 1.10F * 1.5f) : 1.0f;

        int finalAlpha = MathHelper.clamp((int) (alpha * 255.0f), 0, 255);
        int finalColor = (finalAlpha << 24) | (color & 0xFFFFFF);

        int textWidth = renderer.getWidth(text);

        context.getMatrices().pushMatrix();

        context.getMatrices().translate((float) x, (float) y);
        context.getMatrices().scale(finalScale, finalScale);

        context.drawTextWithShadow(renderer, text, -textWidth / 2, 0, finalColor);

        context.getMatrices().popMatrix();
    }

    private boolean shouldHideIndicator(PlayerEntity player) {
        if (player == null) {
            return true;
        }

        boolean hasAnyArmor = false;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);

            if (stack.isEmpty()) {
                continue;
            }

            hasAnyArmor = true;

            if (stack.isOf(Items.LEATHER_HELMET)
                || stack.isOf(Items.LEATHER_CHESTPLATE)
                || stack.isOf(Items.LEATHER_LEGGINGS)
                || stack.isOf(Items.LEATHER_BOOTS)
                || stack.isOf(Items.CHAINMAIL_HELMET)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_LEGGINGS)
                || stack.isOf(Items.CHAINMAIL_BOOTS)) {
                return true;
            }
        }

        return !hasAnyArmor;
    }

    private boolean isHoverOnTarget(MinecraftClient client) {
        if (state.target == null
            || !(client.crosshairTarget instanceof EntityHitResult entityHit)
            || !(entityHit.getEntity() instanceof PlayerEntity hoveredPlayer)) {
            return false;
        }

        return hoveredPlayer.getUuid().equals(state.target.getUuid());
    }

    private boolean isGuiHidden(MinecraftClient client) {
        return client.currentScreen != null && !(client.currentScreen instanceof ChatScreen);
    }

    private int computeSettingsHash() {
        int hash = 1;

        hash = 31 * hash + (config.isBetterHealthDecimal() ? 1 : 0);
        hash = 31 * hash + (config.isBetterHealthGoldenHearts() ? 1 : 0);
        hash = 31 * hash + (config.isBetterHealthGoldenHeartsPlus() ? 1 : 0);

        return hash;
    }

    private void checkWorldChange(MinecraftClient client) {
        if (client.world == null) {
            lastWorld = null;
            return;
        }

        if (lastWorld != null && lastWorld != client.world) {
            lastWorld = client.world;

            hidingIndicator = false;

            state.target = null;
            state.targetUuid = null;

            state.pendingTarget = null;
            state.pendingTargetUuid = null;
            state.pendingAttackTick = 0;

            state.lastDamageTick = 0;
            state.visibleUntilTick = 0;

            state.clearCache();

            animation.reset();

            return;
        }

        lastWorld = client.world;
    }

    private void checkDeaths(MinecraftClient client) {
        if (client.player != null && (client.player.isDead() || client.player.getHealth() <= 0)) {
            startHideAnimation();
            return;
        }

        if (state.target != null && (state.target.isDead()
            || state.target.getHealth() <= 0
            || state.target.isRemoved())) {
            resetDisplayInternal();
        }
    }

    private void cancelPendingAttack() {
        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;
    }

    private void resetAllStates() {
        durationTicks = 100;
        location = 0;

        lastWorld = null;
        lastUpdateTick = -1;

        wasFirstPerson = true;

        hidingIndicator = false;

        state.target = null;
        state.targetUuid = null;

        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;

        state.lastDamageTick = 0;
        state.visibleUntilTick = 0;

        state.targetHealthBeforeHit = -1.0f;
        state.damageConfirmed = false;

        state.clearCache();

        animation.reset();
    }
}