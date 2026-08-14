package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public class BetterSprint {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterSprint() {}

    private static BetterSprint instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private interface DelayCalculator {
        long calc(long currentMs);
    }

    /// Состояния в классах
    private final ModesState defaultState = new ModesState();
    private final ModesState pvpState = new ModesState();

    private static class ModesState {
        boolean sprint;
        boolean active;

        long delayUntilMs;
        long lastStoppedMs = -1000;
    }

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean defaultMode;
        boolean pvpMode;
        boolean stairUp;
        boolean waterSprint;
    }

    private final WaterSprintState waterSprintState = new WaterSprintState();

    private static class WaterSprintState {
        boolean active;
        boolean sprint;

        long shiftIgnoreUntilTick;
    }

    private final EndConcreteState endConcreteState = new EndConcreteState();

    private static class EndConcreteState {
        boolean blocked;
        long lastCheckTick = -1;

        int lastX;
        int lastY;
        int lastZ;

        int dimensionHash;
    }

    /// Остальные состояния
    private PlayerEntity pendingTarget; // Для PvP таймера
    private long pvpExpireTick = 0; // Для PvP таймера
    private long pendingAttackTick; // Для PvP таймера
    private boolean wasEnabledLastTick = false; // Для сброса

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterSprint();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("Better Sprint initialized");
        }
    }

    public static BetterSprint getInstance() {
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

    public static void setDefaultMode(boolean enabled) {
        if (instance != null) instance.setDefaultModeInternal(enabled);
    }

    public static void setPvPMode(boolean enabled) {
        if (instance != null) instance.setPvPModeInternal(enabled);
    }

    public static void setStairUp(boolean enabled) {
        if (instance != null) instance.setStairUpInternal(enabled);
    }

    public static void setWaterSprint(boolean enabled) {
        if (instance != null) instance.setWaterSprintInternal(enabled);
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

    public static void update() {
        if (instance != null) instance.updateInternal();
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_sprint");
        blocked.defaultMode = LiteApiManager.isFeatureBlocked("better_sprint_default");
        blocked.pvpMode = LiteApiManager.isFeatureBlocked("better_sprint_pvp");
        blocked.stairUp = LiteApiManager.isFeatureBlocked("better_sprint_stair_up");
        blocked.waterSprint = LiteApiManager.isFeatureBlocked("better_sprint_water_sprint");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();

        if (!config.isBetterSprintDefaultMode()) {
            defaultState.sprint = false;
            defaultState.delayUntilMs = 0;
            defaultState.active = false;
        }

        if (!config.isBetterSprintPvPMode()) {
            pvpState.delayUntilMs = 0;
            pvpState.sprint = false;
            pvpState.active = false;
        }

        if (!config.isBetterSprintWaterSprint()) {
            waterSprintState.sprint = false;
            waterSprintState.active = false;
        }

        if (!config.isBetterSprintStairUp()) {
            disableStairUp();
        }
    }

    private boolean isEnabledInternal() {
        return config.isBetterSprintEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        boolean wasEnabled = config.isBetterSprintEnabled();

        config.setBetterSprintEnabled(enabled);

        if (wasEnabled && !enabled) {
            disableStairUp();
            resetRemainingStates();
        }

        reloadFromConfigInternal();
    }

    private void setDefaultModeInternal(boolean enabled) {
        config.setBetterSprintDefaultMode(enabled);

        if (enabled) {
            defaultState.delayUntilMs = 0;
        } else {
            defaultState.active = false;
            defaultState.sprint = false;
        }
    }

    private void setPvPModeInternal(boolean enabled) {
        config.setBetterSprintPvPMode(enabled);

        if (enabled) {
            pvpState.delayUntilMs = 0;
        } else {
            pvpState.active = false;
            pvpState.sprint = false;
        }
    }

    private void setStairUpInternal(boolean enabled) {
        config.setBetterSprintStairUp(enabled);

        if (!enabled) {
            disableStairUp();
        }
    }

    private void setWaterSprintInternal(boolean enabled) {
        config.setBetterSprintWaterSprint(enabled);

        if (!enabled) {
            waterSprintState.active = false;
            waterSprintState.sprint = false;
        }
    }

    private void onAttackInternal(PlayerEntity target) {
        MinecraftClient client = mc();

        if (client == null
            || client.player == null
            || client.world == null
            || target == null
            || target == client.player
            || target.isRemoved()
            || target.isDead()
            || !config.isBetterSprintPvPMode()
            || blocked.pvpMode) {
            return;
        }

        pendingTarget = target;
        pendingAttackTick = client.world.getTime();
    }

    private void onDamagedInternal() {
        MinecraftClient client = mc();

        if (client == null
            || client.player == null
            || !config.isBetterSprintPvPMode()
            || blocked.pvpMode) {
            return;
        }

        pvpExpireTick = client.player.age + Math.max(1, config.getPvpTimerDuration() / 50L);
    }

    private void updatePendingAttack() {
        MinecraftClient client = mc();

        if (client == null
            || client.player == null
            || client.world == null
            || pendingTarget == null) {
            return;
        }

        if (pendingTarget.isRemoved() || pendingTarget.isDead()) {
            pendingTarget = null;
            return;
        }

        long tick = client.world.getTime();

        if (tick - pendingAttackTick > 10) {
            pendingTarget = null;
            return;
        }

        if (pendingTarget.hurtTime <= 0 || pendingTarget.getVelocity().horizontalLengthSquared() < 0.001D) {
            return;
        }

        pvpExpireTick = client.player.age + Math.max(1, config.getPvpTimerDuration() / 50L);

        pendingTarget = null;
    }

    private void updateInternal() {
        MinecraftClient client = mc();

        if (client == null || client.player == null || client.world == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        long currentMs = System.currentTimeMillis();

        updatePendingAttack();

        boolean enabled = isEnabledInternal();

        if (!enabled) {
            if (wasEnabledLastTick) {
                resetRemainingStates();
            }

            wasEnabledLastTick = false;
            return;
        }

        wasEnabledLastTick = true;

        boolean moving = isMoving(player);
        boolean forwardOnly = isMovingOnlyForward(player);

        if (isRestrictedServer()) {
            moving = moving && forwardOnly;
        }

        boolean hasFood = hasEnoughFood(player);
        boolean inWaterOrLava = player.isTouchingWater() || player.isInLava();

        defaultState.sprint = false;
        pvpState.sprint = false;
        waterSprintState.sprint = false;

        updateInternalDefault(currentMs, moving, hasFood, inWaterOrLava);
        updateInternalPvP(currentMs, moving, hasFood, inWaterOrLava);
        updateInternalStairUp(player);
        updateInternalWaterSprint(currentMs, player, moving, hasFood);

        if (!hasFood) {
            if (player.isSprinting()) {
                player.setSprinting(false);
                defaultState.lastStoppedMs = currentMs;
                pvpState.lastStoppedMs = currentMs;
            }

            resetAllMovementStates();
            return;
        }

        boolean anySprintModeEnabled = (!blocked.defaultMode && config.isBetterSprintDefaultMode())
            || (!blocked.pvpMode && config.isBetterSprintPvPMode())
            || (!blocked.waterSprint && config.isBetterSprintWaterSprint());

        if (!anySprintModeEnabled) {
            return;
        }

        boolean finalSprint = defaultState.sprint || pvpState.sprint || waterSprintState.sprint;

        if (finalSprint) {
            if (!player.isSprinting()) {
                player.setSprinting(true);
            }
        } else {
            boolean shouldControlSprint = defaultState.active || pvpState.active || waterSprintState.active;

            if (shouldControlSprint && player.isSprinting()) {
                player.setSprinting(false);

                defaultState.lastStoppedMs = currentMs;
                pvpState.lastStoppedMs = currentMs;
            }
        }
    }

    /// Единый хелпер для режимов
    private void updateMode(ModesState state, boolean enabled, boolean canUse, boolean instantAllowed, int instantChance, long currentMs, DelayCalculator delayCalc) {
        if (!enabled || !canUse) {
            if (state.active) {
                state.lastStoppedMs = currentMs;
            }

            state.active = false;
            state.sprint = false;
            state.delayUntilMs = 0;
            return;
        }

        if (!state.active) {
            boolean canInstant = instantAllowed && canInstantRestart(currentMs, state.lastStoppedMs, instantChance);

            if (canInstant) {
                state.sprint = true;
                state.active = true;
                state.delayUntilMs = 0;
                return;
            }

            if (state.delayUntilMs == 0) {
                state.delayUntilMs = delayCalc.calc(currentMs);
            } else if (currentMs >= state.delayUntilMs) {
                state.sprint = true;
                state.active = true;
                state.delayUntilMs = 0;
            }

            return;
        }

        state.sprint = true;
    }

    /// Обновление самих режимов
    private void updateInternalDefault(long currentMs, boolean moving, boolean hasFood, boolean inWaterOrLava) {
        updateMode(
            defaultState,
            config.isBetterSprintDefaultMode()
                && !blocked.defaultMode
                && !inWaterOrLava,
            moving && hasFood,
            true,
            33,
            currentMs,
            this::calculateDefaultDelayMs
        );
    }

    private void updateInternalPvP(long currentMs, boolean moving, boolean hasFood, boolean inWaterOrLava) {
        updateMode(
            pvpState,
            config.isBetterSprintPvPMode()
                && !blocked.pvpMode
                && !inWaterOrLava
                && mc().player.age < pvpExpireTick,
            moving && hasFood,
            true,
            25,
            currentMs,
            this::calculatePvPDelayMs
        );
    }

    private void updateInternalStairUp(ClientPlayerEntity player) {
        MinecraftClient client = mc();

        if (client == null || client.options == null || player == null || client.world == null) {
            return;
        }

        if (!config.isBetterSprintEnabled()
            || blocked.main
            || blocked.stairUp
            || !config.isBetterSprintStairUp()) {

            disableStairUp();
            endConcreteState.blocked = false;
            return;
        }

        boolean blockedByEndConcrete = isWaterSprintEndConcreteNearby(player);

        if (blockedByEndConcrete) {
            disableStairUp();
            return;
        }

        if (!client.options.getAutoJump().getValue()) {
            client.options.getAutoJump().setValue(true);
        }
    }

    private void updateInternalWaterSprint(long currentTick, ClientPlayerEntity player, boolean moving, boolean hasFood) {
        boolean wasActive = waterSprintState.active;

        boolean shouldBeActive = !blocked.waterSprint
            && config.isBetterSprintWaterSprint()
            && player.isTouchingWater()
            && hasFood
            && moving
            && isInFullWaterBlock(player);

        waterSprintState.active = shouldBeActive;
        waterSprintState.sprint = shouldBeActive;

        if (!wasActive && shouldBeActive) {
            waterSprintState.shiftIgnoreUntilTick = currentTick + 8;
        }
    }

    /// Расчет задержек для режимов
    private long calculateDefaultDelayMs(long currentMs) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        int delayMs;

        if (roll < 73) {
            delayMs = ThreadLocalRandom.current().nextInt(32, 145);
        } else if (roll < 94) {
            delayMs = ThreadLocalRandom.current().nextInt(133, 199);
        } else {
            delayMs = ThreadLocalRandom.current().nextInt(174, 269);
        }

        PlayerEntity player = mc().player;

        delayMs += (int) delayBooster(player);
        delayMs -= (int) delayNerfer(player);

        return Math.max(1L, currentMs + delayMs);
    }

    private long calculatePvPDelayMs(long currentMs) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        int delayMs;

        if (roll < 88) {
            delayMs = ThreadLocalRandom.current().nextInt(55, 140);
        } else if (roll < 93) {
            delayMs = ThreadLocalRandom.current().nextInt(134, 190);
        } else {
            delayMs = ThreadLocalRandom.current().nextInt(184, 256);
        }

        PlayerEntity player = mc().player;

        delayMs += (int) delayBooster(player);
        delayMs -= (int) delayNerfer(player);

        return Math.max(1L, currentMs + delayMs);
    }

    private long delayBooster(PlayerEntity player) {
        if (player == null) {
            return 0L;
        }

        long delay = 0L;

        StatusEffectInstance speed = player.getStatusEffect(StatusEffects.SPEED);

        if (speed != null) {
            switch (speed.getAmplifier()) {
                case 0 -> delay += randomDelayMs(1, 4);
                case 1 -> delay += randomDelayMs(2, 6);
                case 2 -> delay += randomDelayMs(5, 8);
                default -> delay += randomDelayMs(5, 8);
            }
        }

        if (player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            delay += randomDelayMs(6, 10);
        }

        if (player.hasStatusEffect(StatusEffects.DARKNESS)) {
            delay += randomDelayMs(7, 12);
        }

        if (player.isOnFire()) {
            delay += randomDelayMs(4, 13);
        }

        return delay;
    }

    private long delayNerfer(PlayerEntity player) {
        if (player == null) {
            return 0L;
        }

        long delay = 0L;

        StatusEffectInstance slowness =
            player.getStatusEffect(StatusEffects.SLOWNESS);

        if (slowness != null) {
            switch (slowness.getAmplifier()) {
                case 0 -> delay += randomDelayMs(3, 7);
                case 1 -> delay += randomDelayMs(4, 9);
                case 2 -> delay += randomDelayMs(2, 11);
                default -> delay += randomDelayMs(2, 11);
            }
        }

        return delay;
    }

    private int randomDelayMs(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /// Дополнительные проверки и методы для режимов
    private static boolean isMoving(ClientPlayerEntity player) {
        if (player == null || player.input == null) {
            return false;
        }

        boolean input = player.input.movementForward != 0.0F || player.input.movementSideways != 0.0F;

        var v = player.getVelocity();

        double speedSq = v.x * v.x + v.z * v.z;

        boolean velocity = speedSq > 0.0025;

        return input || velocity;
    }

    private boolean isMovingOnlyForward(ClientPlayerEntity player) {
        if (player == null || player.input == null) {
            return false;
        }

        return player.input.movementForward > 0.0F && player.input.movementSideways == 0.0F;
    }

    private boolean hasEnoughFood(PlayerEntity player) {
        return player != null && player.getHungerManager().getFoodLevel() > 6;
    }

    private boolean canInstantRestart(long currentMs, long lastStopMs, int chancePercent) {
        if (lastStopMs < 0) {
            return false;
        }

        long stoppedMs = currentMs - lastStopMs;

        if (stoppedMs < 500L) {
            return false;
        }

        return ThreadLocalRandom.current().nextInt(100) < chancePercent;
    }

    /// Дополнительные методы для Stair Up
    public static boolean isStairUpActive() {
        return instance != null
            && instance.isEnabledInternal()
            && !instance.blocked.stairUp
            && instance.config.isBetterSprintStairUp()
            && !instance.endConcreteState.blocked;
    }

    private void disableStairUp() {
        MinecraftClient client = mc();

        if (client != null && client.options != null) {
            client.options.getAutoJump().setValue(false);
        }
    }

    /// Дополнительные методы для Water Sprint
    private boolean isWaterSprintEndConcreteNearby(ClientPlayerEntity player) {
        MinecraftClient client = mc();

        if (client == null || client.world == null || player == null) {
            return false;
        }

        if (client.world.getRegistryKey() != World.END) {
            endConcreteState.blocked = false;
            return false;
        }

        long currentTick = player.age;

        int playerX = player.getBlockPos().getX();
        int playerY = player.getBlockPos().getY();
        int playerZ = player.getBlockPos().getZ();

        int dimensionHash = client.world.getRegistryKey().hashCode();

        boolean samePosition = endConcreteState.lastX == playerX
                && endConcreteState.lastY == playerY
                && endConcreteState.lastZ == playerZ
                && endConcreteState.dimensionHash == dimensionHash;

        if (samePosition && currentTick - endConcreteState.lastCheckTick < 2) {
            return endConcreteState.blocked;
        }

        endConcreteState.lastX = playerX;
        endConcreteState.lastY = playerY;
        endConcreteState.lastZ = playerZ;
        endConcreteState.dimensionHash = dimensionHash;
        endConcreteState.lastCheckTick = currentTick;

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    mutablePos.set(
                        playerX + x,
                        playerY + y,
                        playerZ + z
                    );

                    if (client.world.getBlockState(mutablePos).isOf(Blocks.BLACK_CONCRETE_POWDER)) {
                        endConcreteState.blocked = true;
                        return true;
                    }
                }
            }
        }

        endConcreteState.blocked = false;
        return false;
    }

    private boolean isInFullWaterBlock(PlayerEntity player) {
        MinecraftClient client = mc();

        if (player == null || client.world == null) {
            return false;
        }

        BlockPos feet = BlockPos.ofFloored(
            player.getX(),
            player.getY() + 0.05,
            player.getZ()
        );

        BlockState state = client.world.getBlockState(feet);

        return state.getFluidState().isStill() && player.isTouchingWater();
    }

    public static boolean shouldDisableWaterSprintShiftStops(PlayerEntity player) {
        if (instance == null || player == null) {
            return false;
        }

        return instance.isEnabledInternal()
            && !instance.blocked.waterSprint
            && instance.config.isBetterSprintWaterSprint()
            && player.isTouchingWater()
            && !player.isInLava();
    }

    public static boolean shouldDisableWaterSprintShiftStop() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (instance == null
                || client == null
                || client.player == null) {
            return false;
        }

        return instance.waterSprintState.active && client.player.age < instance.waterSprintState.shiftIgnoreUntilTick;
    }

    /// Метод для определения сервера
    public static boolean isRestrictedServer() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getCurrentServerEntry() == null) {
            return false;
        }

        String address = client.getCurrentServerEntry().address.toLowerCase();

        return address.contains("holyworld") || address.contains("funtime");
    }

    /// Утилиты сброса состояния
    private void resetAllMovementStates() {
        defaultState.delayUntilMs = 0;
        pvpState.delayUntilMs = 0;

        defaultState.active = false;
        pvpState.active = false;
        waterSprintState.active = false;

        defaultState.sprint = false;
        pvpState.sprint = false;
        waterSprintState.sprint = false;
    }

    private void resetRemainingStates() {
        pendingTarget = null;
        pendingAttackTick = 0;

        pvpExpireTick = 0;

        defaultState.lastStoppedMs = -1000;
        pvpState.lastStoppedMs = -1000;

        endConcreteState.blocked = false;

        endConcreteState.lastCheckTick = -1;
        endConcreteState.dimensionHash = 0;
        endConcreteState.lastX = 0;
        endConcreteState.lastY = 0;
        endConcreteState.lastZ = 0;

        wasEnabledLastTick = false;

        waterSprintState.shiftIgnoreUntilTick = 0;

        disableStairUp();

        resetAllMovementStates();
    }
}
// v1.0