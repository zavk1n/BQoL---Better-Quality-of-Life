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

public class CustomHealth {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private CustomHealth() {}

    private static CustomHealth instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean scaling;
        boolean hovering;
        boolean pvp;
        boolean decimal;
        boolean goldenHearts;
        boolean goldenPlus;
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

        long lastArmorSample;
        long lastArmorCheckTick = -10;

        boolean hasCachedArmorResult = false;

        Text cachedRenderText;

        float lastCachedHealth = -1f;
        float lastCachedAbsorption = -1f;

        int lastSettingsHash;

        void clearCache() {
            lastArmorPlayerUuid = null;

            hasCachedArmorResult = false;

            cachedRenderText = null;

            lastCachedHealth = -1f;
            lastCachedAbsorption = -1f;

            lastArmorCheckTick = -10;
            lastArmorSample = 0;

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

    private record Position(
        int x,
        int y
    ) {}

    private net.minecraft.world.World lastWorld = null;

    private long lastUpdateTick = -1;
    private long lastHoverTick = 0;

    private int durationTicks = 100;
    private int location = 0;

    private boolean wasFirstPerson = true;
    private boolean hidingIndicator = false;

    public static void initialize() {
        if (instance == null) {
            instance = new CustomHealth();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            instance.init();
            BQoL.LOGGER.info("Custom Health initialized");
        }
    }

    public static CustomHealth getInstance() {
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
        if (instance != null) {
            instance.onAttackInternal(target);
        }
    }

    public static void onDamaged() {
        if (instance != null) {
            instance.onDamagedInternal();
        }
    }

    public static PlayerEntity getLastAttackedPlayer() {
        return instance != null ? instance.state.target : null;
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("custom_health");
        blocked.scaling = LiteApiManager.isFeatureBlocked("custom_health_scaling");
        blocked.hovering = LiteApiManager.isFeatureBlocked("custom_health_hovering");
        blocked.pvp = LiteApiManager.isFeatureBlocked("custom_health_pvp");
        blocked.decimal = LiteApiManager.isFeatureBlocked("custom_health_decimal");
        blocked.goldenHearts = LiteApiManager.isFeatureBlocked("custom_health_golden_hearts");
        blocked.goldenPlus = LiteApiManager.isFeatureBlocked("custom_health_golden_plus");
    }

    private void reloadFromConfigInternal() {
        durationTicks = Math.max(1, (int) (config.getCustomHealthDuration() / 50));

        location = config.getCustomHealthPosition();

        refreshBlockedStatusInternal();

        state.clearCache();
    }

    private boolean isEnabledInternal() {
        return config.isCustomHealthEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setCustomHealthEnabled(enabled);

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

        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;

        state.lastDamageTick = 0;

        state.clearCache();

        lastWorld = null;
        wasFirstPerson = true;
        lastUpdateTick = -1;
    }

    private void clearTargetAfterFade() {
        state.target = null;
        state.targetUuid = null;

        cancelPendingAttack();

        state.lastDamageTick = 0;

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

        hidingIndicator = true;

        boolean scalingEnabled = isEnabledInternal()
                && config.isCustomHealthScaling()
                && !blocked.scaling;

        animation.hide(scalingEnabled);
    }

    private void onAttackInternal(PlayerEntity target) {
        MinecraftClient client = mc();

        if (client == null
            || client.player == null
            || client.world == null
            || !isEnabledInternal()
            || blocked.main
            || target == null
            || target == client.player
            || target.isRemoved()
            || target.isDead()
            || hidingIndicator) {
            return;
        }

        state.pendingTarget = target;
        state.pendingTargetUuid = target.getUuid();
        state.pendingAttackTick = client.world.getTime();
    }

    private void onDamagedInternal() {
        MinecraftClient client = mc();

        if (client == null
            || client.world == null
            || !isEnabledInternal()
            || state.target == null
            || state.target.isRemoved()
            || state.target.isDead()
            || hidingIndicator) {

            return;
        }

        state.lastDamageTick = client.world.getTime();

        boolean scalingEnabled = config.isCustomHealthScaling() && !blocked.scaling;

        animation.show(scalingEnabled, false);
    }

    private void init() {
        reloadFromConfigInternal();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client == null || !isEnabled()) {
                return;
            }

            renderIndicator(client, drawContext, tickDelta);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) {
                return;
            }

            update(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register(
            (handler, client) -> resetDisplayInternal()
        );
    }

    private void update(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        long worldTick = client.world.getTime();

        if (worldTick == lastUpdateTick) {
            return;
        }

        lastUpdateTick = worldTick;

        checkWorldChange(client);
        checkDeaths(client);

        if (hidingIndicator) {
            cancelPendingAttack();
        } else {
            updatePendingAttack(client);
        }

        boolean firstPerson = client.options.getPerspective().isFirstPerson();

        if (firstPerson && !wasFirstPerson
            && state.target != null
            && !hidingIndicator) {

            boolean scalingEnabled = isEnabledInternal()
                    && config.isCustomHealthScaling()
                    && !blocked.scaling;

            animation.show(scalingEnabled, false);
        }

        wasFirstPerson = firstPerson;

        if (!hidingIndicator && state.targetUuid != null && worldTick - state.lastDamageTick <= durationTicks) {
            updateTargetReference(client);
        }

        boolean expired = state.target != null
                && state.lastDamageTick > 0
                && worldTick - state.lastDamageTick >= durationTicks;

        if (expired && !hidingIndicator) {
            startHideAnimation();
        }

        boolean scalingEnabled = isEnabledInternal()
                && config.isCustomHealthScaling()
                && !blocked.scaling;

        if (hidingIndicator) {
            cancelPendingAttack();
            animation.hide(scalingEnabled);

        } else {
            boolean visible = shouldShowIndicator(client, worldTick) && !isGuiHidden(client);

            if (visible) {
                animation.show(scalingEnabled, false);
            } else if (!hidingIndicator) {
                animation.hide(scalingEnabled);
            }
        }
    }

    private RenderData prepareRenderData(PlayerEntity player) {
        float normalHealth = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float totalHealth = normalHealth + absorption;

        int settingsHash = computeSettingsHash();

        boolean rebuild = state.cachedRenderText == null
            || Math.abs(state.lastCachedHealth - normalHealth) > 0.01f
            || Math.abs(state.lastCachedAbsorption - absorption) > 0.01f
            || state.lastSettingsHash != settingsHash;

        if (rebuild) {
            boolean useGoldenHearts = isEnabledInternal()
                    && config.isCustomHealthGoldenHearts()
                    && !blocked.goldenHearts;

            boolean useGoldenPlus = isEnabledInternal()
                    && config.isCustomHealthGoldenHeartsPlus()
                    && !blocked.goldenPlus;

            boolean useDecimal = isEnabledInternal()
                    && config.isCustomHealthDecimal()
                    && !blocked.decimal;

            state.cachedRenderText = buildDisplayText(
                    normalHealth,
                    absorption,
                    totalHealth,
                    useDecimal,
                    useGoldenHearts,
                    useGoldenPlus
                );

            state.lastCachedHealth = normalHealth;
            state.lastCachedAbsorption = absorption;
            state.lastSettingsHash = settingsHash;
        }

        float maxHealth = Math.max(player.getMaxHealth(), 20f);
        float healthPercent = MathHelper.clamp(totalHealth / maxHealth, 0.0f, 1.0f);
        float danger = 1.0f - healthPercent;
        float baseScale = MathHelper.lerp(danger, 1.0f, 1.20f);
        float animationScale = 1.0f;

        if (config.isCustomHealthScaling() && !blocked.scaling) {
            animationScale = animation.currentScale;
        }

        float finalScale = baseScale * animationScale;

        return new RenderData(state.cachedRenderText, finalScale, animation.currentAlpha);
    }

    private Position calculateIndicatorPosition(MinecraftClient client, int textWidth) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int absX = screenWidth / 2 + 2;
        int absY = screenHeight / 2 - 25;

        switch (location) {
            case 1 -> {
                // Left
                absX = screenWidth / 2 - 25;
                absY = screenHeight / 2 - 5;
            }

            case 2 -> {
                // Right
                absX = screenWidth / 2 + 35;
                absY = screenHeight / 2 - 5;
            }

            case 3 -> {
                // Under
                absX = screenWidth / 2;
                absY = screenHeight / 2 + 16;
            }
        }

        return new Position(absX, absY);
    }

    private int textColor(Text text, float alpha) {
        int baseColor = text.getStyle().getColor() != null ? text.getStyle().getColor().getRgb() : 0xFFFFFF;

        if (alpha >= 1f) {
            return baseColor;
        }

        int alphaInt = (int) (MathHelper.clamp(alpha, 0f, 1f) * 255f);

        return (alphaInt << 24) | (baseColor & 0xFFFFFF);
    }

    private void drawIndicator(DrawContext context, TextRenderer renderer, RenderData data, Position pos) {
        int textWidth = renderer.getWidth(data.text());

        context.getMatrices().push();
        context.getMatrices().translate(pos.x(), pos.y(), 0);
        context.getMatrices().scale(data.scale(), data.scale(), 1.0f);

        int color = textColor(data.text(), data.alpha());

        context.drawTextWithShadow(renderer, data.text(), -textWidth / 2, 0, color);
        context.getMatrices().pop();
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

    public static String getColoredIndicatorString(PlayerEntity player) {
        return instance != null ? instance.getColoredIndicatorStringInternal(player) : "";
    }

    private String getColoredIndicatorStringInternal(PlayerEntity player) {
        if (player == null || blocked.main) {
            return "";
        }

        float totalHealth = player.getHealth() + player.getAbsorptionAmount();

        Formatting color = getIndicatorFormatting(totalHealth);

        boolean useDecimal = isEnabledInternal()
                && config.isCustomHealthDecimal()
                && !blocked.decimal;

        String healthValue = formatIndicatorString(totalHealth, useDecimal);

        return color + healthValue;
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

    private void renderIndicator(MinecraftClient client, DrawContext context, float tickDelta) {
        if (client.player == null
            || client.world == null
            || !client.options.getPerspective().isFirstPerson()
            || isGuiHidden(client)) {

            return;
        }

        PlayerEntity player = state.target;

        if (player == null) {
            return;
        }

        boolean scalingEnabled = isEnabledInternal()
                && config.isCustomHealthScaling()
                && !blocked.scaling;

        animation.update(tickDelta, scalingEnabled);

        if (hidingIndicator && animation.isFinished()) {
            clearTargetAfterFade();
            return;
        }

        RenderData data = prepareRenderData(player);

        if (data.alpha() <= 0.001f) {
            return;
        }

        TextRenderer renderer = client.textRenderer;

        if (renderer == null) {
            return;
        }

        int textWidth = renderer.getWidth(data.text());

        Position pos = calculateIndicatorPosition(client, textWidth);

        drawIndicator(context, renderer, data, pos);
    }

    private boolean shouldShowIndicator(MinecraftClient client, long tick) {
        if (blocked.main
            || hidingIndicator
            || state.target == null
            || client.player == null
            || state.target.isRemoved()
            || state.target.isDead()
            || tick - state.lastDamageTick >= durationTicks
            || client.player.squaredDistanceTo(state.target) > 128.0 * 128.0) {

            return false;
        }

        if (config.isCustomHealthPvPMode() && !blocked.pvp && shouldHideIndicator(state.target)) {
            return false;
        }

        if (config.isCustomHealthHovering() && !blocked.hovering) {
            return isHoverOnTarget(client);
        }

        return true;
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

            if (stack.getItem() instanceof ArmorItem armorItemLeather && armorItemLeather.getMaterial() == ArmorMaterials.LEATHER
                || stack.getItem() instanceof ArmorItem armorItemChain && armorItemChain.getMaterial() == ArmorMaterials.CHAIN) {
                return true;
            }
        }

        return !hasAnyArmor;
    }

    private void cancelPendingAttack() {
        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;
    }

    private void updatePendingAttack(MinecraftClient client) {
        if (hidingIndicator) {
            cancelPendingAttack();
            return;
        }

        PlayerEntity pending = state.pendingTarget;

        if (pending == null) {
            return;
        }

        if (pending.isRemoved() || pending.isDead()) {
            cancelPendingAttack();
            return;
        }

        long tick = client.world.getTime();

        if (tick - state.pendingAttackTick > 10) {
            cancelPendingAttack();
            return;
        }

        if (pending.hurtTime <= 0) {
            return;
        }

        boolean newTarget = state.target == null || !state.target.getUuid().equals(pending.getUuid());

        state.target = pending;
        state.targetUuid = pending.getUuid();

        state.lastDamageTick = tick;

        hidingIndicator = false;

        boolean scalingEnabled = config.isCustomHealthScaling() && !blocked.scaling;

        if (newTarget || animation.currentAlpha <= 0.05f) {
            animation.show(scalingEnabled, false);
        } else {
            animation.targetAlpha = 1.0f;
        }

        cancelPendingAttack();
    }

    private void updateTargetReference(MinecraftClient client) {
        if (hidingIndicator
            || state.targetUuid == null
            || client.world == null
            || client.world.getTime() - state.lastDamageTick > durationTicks) {
            return;
        }

        if (state.target == null || state.target.isRemoved()) {
            PlayerEntity refreshed = client.world.getPlayerByUuid(state.targetUuid);

            if (refreshed != null
                && !refreshed.isRemoved()
                && !refreshed.isDead()) {

                state.target = refreshed;

            } else {
                startHideAnimation();
            }
        }
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

        hash = 31 * hash + (config.isCustomHealthDecimal() ? 1 : 0);
        hash = 31 * hash + (config.isCustomHealthGoldenHearts() ? 1 : 0);
        hash = 31 * hash + (config.isCustomHealthGoldenHeartsPlus() ? 1 : 0);
        hash = 31 * hash + (blocked.decimal ? 1 : 0);
        hash = 31 * hash + (blocked.goldenHearts ? 1 : 0);
        hash = 31 * hash + (blocked.goldenPlus ? 1 : 0);

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

        if (state.target != null && (state.target.isDead() || state.target.getHealth() <= 0)) {
            startHideAnimation();
        }
    }

    private void resetAllStates() {
        lastHoverTick = 0;

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

        state.clearCache();

        animation.reset();
    }
}