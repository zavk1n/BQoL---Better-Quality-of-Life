package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CustomHealth {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private CustomHealth() {}

    private static CustomHealth instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private static final Set<String> IGNORED_NAMES = Set.of(
        "Путеводитель",
        "Сферомант",
        "Хранитель яиц",
        "Барыга",
        "Скупщик",
        "Опытный шахтер",
        "Опытный Тыпо",
        "Опытный тыпо",
        "Хранитель опыта",
        "Квестовик",
        "Джейкоб",
        "Деревенский Джейкоб",
        "Деревенский джейкоб",
        "Зажиточный Джейкоб",
        "Зажиточный джейкоб",
        "Садовод",
        "Снеговик",
        "Пират",
        "Капитан"
    );

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    /// Состояния в классах
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

        boolean cachedArmorResult;
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
        float currentAlpha = 0f;
        float targetAlpha = 0f;

        float currentScale = 0.85f;
        float targetScale = 0.85f;

        long lastAnimationTick = -1;

        void update(long currentTick, boolean scalingEnabled) {
            if (lastAnimationTick == -1) {
                lastAnimationTick = currentTick;
            }

            long passed = Math.max(1L, currentTick - lastAnimationTick);
            lastAnimationTick = currentTick;

            float speed = Math.min(1.0f, passed * 0.22f);

            currentAlpha += (targetAlpha - currentAlpha) * speed;
            currentAlpha = MathHelper.clamp(currentAlpha, 0f, 1f);

            if (scalingEnabled) {
                currentScale = MathHelper.lerp(currentAlpha, 0.85f, 1.0f);
            } else {
                currentScale = 1.0f;
            }

            if (Math.abs(targetAlpha - currentAlpha) < 0.001f) {
                currentAlpha = targetAlpha;
            }

            if (scalingEnabled) {
                float targetScaleValue = targetAlpha > 0f ? 1.0f : 0.85f;

                if (Math.abs(targetScaleValue - currentScale) < 0.001f) {
                    currentScale = targetScaleValue;
                }
            }
        }
    }

    private record RenderData(Text text, float scale, float alpha) {}
    private record Position(int x, int y) {}

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean scaling;
        boolean hovering;
        boolean decimal;
        boolean goldenHearts;
        boolean goldenPlus;
    }


    /// Остальные состояния
    private net.minecraft.world.World lastWorld = null;
    private long lastUpdateTick = -1;
    private long lastHoverTick = 0;
    private int durationTicks = 100;
    private int location = 0;
    private boolean wasFirstPerson = true;
    private boolean hidingIndicator = false;

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new CustomHealth();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            instance.init();
            BQoL.LOGGER.info("CustomHealth initialized");
        }
    }

    public static CustomHealth getInstance() {
        if (instance == null)
            initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static void reloadFromConfig() {
        if (instance != null) instance.reloadFromConfigInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    public static void resetDisplay() {
        if (instance != null) instance.resetDisplayInternal();
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

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("custom_health");
        blocked.scaling = LiteApiManager.isFeatureBlocked("custom_health_scaling");
        blocked.hovering = LiteApiManager.isFeatureBlocked("custom_health_hovering");
        blocked.decimal = LiteApiManager.isFeatureBlocked("custom_health_decimal");
        blocked.goldenHearts = LiteApiManager.isFeatureBlocked("custom_health_golden_hearts");
        blocked.goldenPlus = LiteApiManager.isFeatureBlocked("custom_health_golden_plus");
    }

    private void reloadFromConfigInternal() {
        durationTicks = Math.max(1, (int) (config.getCustomHealthDuration() / 50));
        location = config.getCustomHealthLocation();

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

        animation.currentAlpha = 0f;
        animation.targetAlpha = 0f;

        animation.currentScale = 0.85f;
        animation.targetScale = 0.85f;

        animation.lastAnimationTick = -1;

        state.target = null;
        state.targetUuid = null;

        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;

        state.lastDamageTick = 0;

        state.clearCache();

        lastWorld = null;
        wasFirstPerson = true;
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
            || target.isDead()) {
            return;
        }

        String name = target.getName().getString();

        if (IGNORED_NAMES.contains(name)) {
            return;
        }

        state.pendingTarget = target;
        state.pendingTargetUuid = target.getUuid();
        state.pendingAttackTick = client.world.getTime();
    }

    private void onDamagedInternal() {
        MinecraftClient client = mc();

        if (client == null
            || client.player == null
            || client.world == null
            || !isEnabledInternal()
            || blocked.main
            || state.target == null
            || state.target.isRemoved()
            || state.target.isDead()) {
            return;
        }

        state.lastDamageTick = client.world.getTime();

        hidingIndicator = false;

        animation.targetAlpha = 1f;
        animation.targetScale = 1f;
    }

    /// Основная логика
    private void init() {
        reloadFromConfigInternal();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || !isEnabled()) return;
            renderIndicator(client, drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;
            update(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetDisplayInternal());
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
        updatePendingAttack(client);

        boolean firstPerson = client.options.getPerspective().isFirstPerson();

        if (firstPerson && !wasFirstPerson
            && state.target != null
            && !hidingIndicator) {
            animation.currentAlpha = 0f;
            animation.currentScale = 0.85f;

            animation.targetAlpha = 1f;
            animation.targetScale = 1f;
        }

        wasFirstPerson = firstPerson;

        if (!hidingIndicator && state.targetUuid != null
            && worldTick - state.lastDamageTick <= durationTicks) {
            updateTargetReference(client);
        }

        boolean expired = state.target != null && worldTick - state.lastDamageTick > durationTicks;

        if (expired) {
            if (!hidingIndicator) {
                hidingIndicator = true;
                animation.targetAlpha = 0f;
                animation.targetScale = 0.85f;
            }

        } else if (!hidingIndicator) {
            boolean visible = shouldShowIndicator(client, worldTick) && !isGuiHidden(client);

            animation.targetAlpha = visible ? 1f : 0f;
            animation.targetScale = visible ? 1f : 0.85f;
        }

        boolean scalingEnabled = config.isCustomHealthScaling() && !blocked.scaling;

        animation.update(worldTick, scalingEnabled);

        if (hidingIndicator && animation.currentAlpha <= 0.01f) {
            state.target = null;
            state.targetUuid = null;

            state.pendingTarget = null;
            state.pendingTargetUuid = null;
            state.pendingAttackTick = 0;
            state.lastDamageTick = 0;

            state.clearCache();

            animation.currentAlpha = 0f;
            animation.targetAlpha = 0f;

            animation.currentScale = 0.85f;
            animation.targetScale = 0.85f;

            hidingIndicator = false;

            return;
        }
    }

    /// Рендер индикатора
    private RenderData prepareRenderData(PlayerEntity player) {
        float normalHealth = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float totalHealth = normalHealth + absorption;

        int settingsHash = computeSettingsHash();

        boolean rebuild = state.cachedRenderText == null || Math.abs(state.lastCachedHealth - normalHealth) > 0.01f
            || Math.abs(state.lastCachedAbsorption - absorption) > 0.01f
            || state.lastSettingsHash != settingsHash;

        if (rebuild) {
            boolean useGoldenHearts = config.isCustomHealthGoldenHearts() && !blocked.goldenHearts;
            boolean useGoldenPlus = config.isCustomHealthGoldenHeartsPlus() && !blocked.goldenPlus;
            boolean useDecimal = config.isCustomHealthDecimal() && !blocked.decimal;

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

        float healthPercent = MathHelper.clamp(
            totalHealth / maxHealth,
            0f,
            1f
        );

        float danger = 1f - healthPercent;
        float baseScale = MathHelper.lerp(danger,
            1.35f,
            1.80f
        );

        float finalScale;

        if (config.isCustomHealthScaling() && !blocked.scaling) {
            finalScale = baseScale * animation.currentScale;
        } else {
            finalScale = baseScale;
        }

        return new RenderData(
            state.cachedRenderText,
            finalScale,
            animation.currentAlpha
        );
    }

    private Position calculateIndicatorPosition(MinecraftClient client, float scale, int textWidth) {
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

        int drawX = (int) (absX / scale) - textWidth / 2;
        int drawY = (int) (absY / scale);

        return new Position(drawX, drawY);
    }

    private int textColor(Text text, float alpha) {
        int baseColor = text.getStyle().getColor() != null ? text.getStyle().getColor().getRgb() : 0xFFFFFF;

        if (alpha >= 1f) {
            return baseColor;
        }

        int alphaInt = (int) (alpha * 255);

        return (alphaInt << 24) | (baseColor & 0xFFFFFF);
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

        boolean useDecimal = config.isCustomHealthDecimal() && !blocked.decimal;

        String healthValue = formatIndicatorString(totalHealth, useDecimal);

        return color + healthValue;
    }

    private String formatIndicatorString(float health, boolean useDecimal) {
        if (useDecimal) {
            return String.format(
                Locale.ROOT,
                "%.1f",
                health
            );
        }

        return String.valueOf((int) Math.ceil(health));
    }

    private Formatting getIndicatorFormatting(float health) {
        if (health <= 5f) {
            return Formatting.RED;
        }

        if (health <= 10f) {
            return Formatting.GOLD;
        }

        if (health <= 15f) {
            return Formatting.YELLOW;
        }

        return Formatting.GREEN;
    }

    private void drawIndicator(DrawContext context, TextRenderer renderer, RenderData data, Position pos) {
        context.getMatrices().push();
        context.getMatrices().scale(data.scale(), data.scale(), 1);

        int color = textColor(data.text(), data.alpha());
        context.drawTextWithShadow(renderer, data.text(), pos.x(), pos.y(), color);

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

    private void renderIndicator(MinecraftClient client, DrawContext context) {
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

        RenderData data = prepareRenderData(player);

        if (data.alpha() <= 0.01f || data.scale() <= 0.01f) {
            return;
        }

        TextRenderer renderer = client.textRenderer;

        if (renderer == null) {
            return;
        }

        int textWidth = renderer.getWidth(data.text());

        Position pos = calculateIndicatorPosition(
            client,
            data.scale(),
            textWidth
        );

        drawIndicator(
            context,
            renderer,
            data,
            pos
        );
    }

    /// Условия показа индикатора
    private boolean shouldShowIndicator(MinecraftClient client, long tick) {
        if (blocked.main
            || state.target == null
            || client.player == null
            || state.target.isRemoved()
            || state.target.isDead()
            || tick - state.lastDamageTick > durationTicks
            || client.player.squaredDistanceTo(state.target) > 128 * 128
            || (config.isCustomHealthPvPMode()
            && shouldHideIndicator(state.target, tick))) {
            return false;
        }

        if (config.isCustomHealthHovering() && !blocked.hovering) {
            return isHoverOnTarget(client) || tick - lastHoverTick <= 10;
        }

        return true;
    }

    private boolean shouldHideIndicator(PlayerEntity player, long currentTick) {
        if (player == null) {
            return true;
        }

        long sample = computeArmorSample(player);

        if (state.hasCachedArmorResult
            && state.lastArmorPlayerUuid != null
            && state.lastArmorPlayerUuid.equals(player.getUuid())
            && currentTick - state.lastArmorCheckTick < 20
            && state.lastArmorSample == sample) {
            return state.cachedArmorResult;
        }

        boolean hasArmor = false;
        boolean hide = false;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);

            if (stack.isEmpty()) {
                continue;
            }

            hasArmor = true;

            if (!(stack.getItem() instanceof ArmorItem armorItem)) {
                continue;
            }

            String material = armorItem.getMaterial().getName().toLowerCase(Locale.ROOT);

            if (material.contains("chainmail")) {
                hide = true;
                break;
            }

            if (armorItem instanceof DyeableArmorItem dyeableArmor) {
                int color = dyeableArmor.getColor(stack);
                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;

                boolean exactRed = color == 0xB02E26;
                boolean rangeRed = r > 120 && g < 100 && b < 100;

                if (exactRed || rangeRed) {
                    hide = true;
                    break;
                }
            }
        }

        boolean result = !hasArmor || hide;

        state.lastArmorPlayerUuid = player.getUuid();
        state.lastArmorCheckTick = currentTick;
        state.cachedArmorResult = result;
        state.hasCachedArmorResult = true;
        state.lastArmorSample = sample;

        return result;
    }

    /// Дополнительные методы настроек
    private void updatePendingAttack(MinecraftClient client) {
        if (state.pendingTarget == null) {
            return;
        }

        if (state.pendingTarget.isRemoved() || state.pendingTarget.isDead()) {
            state.pendingTarget = null;
            state.pendingTargetUuid = null;
            return;
        }

        long tick = client.world.getTime();

        if (tick - state.pendingAttackTick > 10) {
            state.pendingTarget = null;
            state.pendingTargetUuid = null;
            return;
        }

        if (state.pendingTarget.hurtTime <= 0 || state.pendingTarget.getVelocity().horizontalLengthSquared() < 0.001D) {
            return;
        }

        boolean newTarget = state.target != state.pendingTarget;

        state.target = state.pendingTarget;
        state.targetUuid = state.pendingTargetUuid;
        state.lastDamageTick = tick;

        hidingIndicator = false;

        if (newTarget || animation.currentAlpha <= 0.05f) {
            animation.currentAlpha = 0f;
            animation.currentScale = 0.85f;
        }

        animation.targetAlpha = 1f;
        animation.targetScale = 1f;

        lastHoverTick = 0;

        state.pendingTarget = null;
        state.pendingTargetUuid = null;
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

            if (refreshed != null && !refreshed.isRemoved() && !refreshed.isDead()) {
                state.target = refreshed;
            } else {
                animation.targetAlpha = 0f;
                animation.targetScale = 0.85f;
            }
        }
    }

    private boolean isHoverOnTarget(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof PlayerEntity hoveredPlayer)) {
            return false;
        }

        if (hoveredPlayer.equals(state.target)) {
            if (client.world == null) {
                return false;
            }

            lastHoverTick = client.world.getTime();

            return true;
        }

        return false;
    }

    private boolean isGuiHidden(MinecraftClient client) {
        return client.currentScreen != null && !(client.currentScreen instanceof ChatScreen);
    }

    private long computeArmorSample(PlayerEntity player) {
        long hash = 1L;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);

            if (stack.isEmpty()) {
                hash = 31L * hash;
                continue;
            }

            hash = 31L * hash + System.identityHashCode(stack.getItem());
            hash = 31L * hash + stack.getCount();

            if (stack.getItem() instanceof DyeableArmorItem dyeableArmor) {
                hash = 31L * hash + dyeableArmor.getColor(stack);
            }

            if (stack.hasNbt()) {
                hash = 31L * hash + stack.getNbt().hashCode();
            }
        }

        return hash;
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

            animation.currentAlpha = 0f;
            animation.targetAlpha = 0f;

            animation.currentScale = 0.85f;
            animation.targetScale = 0.85f;

            animation.lastAnimationTick = -1;

            return;
        }

        lastWorld = client.world;
    }

    private void checkDeaths(MinecraftClient client) {
        if (client.player != null && (client.player.isDead() || client.player.getHealth() <= 0)) {
            hidingIndicator = true;

            animation.targetAlpha = 0f;
            animation.targetScale = 0.85f;

            return;
        }

        if (state.target != null && (state.target.isDead() || state.target.getHealth() <= 0)) {
            hidingIndicator = true;

            animation.targetAlpha = 0f;
            animation.targetScale = 0.85f;
        }
    }

    /// Утилиты сброса состояния
    private void resetAllStates() {
        lastHoverTick = 0;
        durationTicks = 100;
        location = 0;

        lastWorld = null;
        wasFirstPerson = true;
        hidingIndicator = false;

        state.target = null;
        state.targetUuid = null;

        state.pendingTarget = null;
        state.pendingTargetUuid = null;
        state.pendingAttackTick = 0;

        state.lastDamageTick = 0;

        state.clearCache();

        animation.currentAlpha = 0f;
        animation.targetAlpha = 0f;

        animation.currentScale = 0.85f;
        animation.targetScale = 0.85f;

        animation.lastAnimationTick = -1;
    }
}
// v1.0