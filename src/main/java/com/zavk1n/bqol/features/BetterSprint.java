package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
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
        long calc (long currentTick);
    }

    /// Состояния в классах
    private final ModesState defaultState = new ModesState();
    private final ModesState pvpState = new ModesState();
    private final ModesState treeState = new ModesState();

    private static class ModesState {
        boolean sprint;
        boolean active;

        long delayUntilTick;
        long lastStoppedTick = -1000;
    }

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean defaultMode;
        boolean pvpMode;
        boolean treeMode;
        boolean stairUp;
        boolean waterSprint;
    }

    private final LeavesState leavesState = new LeavesState();

    private static class LeavesState {
        boolean cachedResult;
        long lastCheckTick;
        int dimensionHash;
    }

    private final WaterSprintState waterSprintState = new WaterSprintState();

    private static class WaterSprintState {
        boolean active;
        boolean sprint;
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
            BQoL.LOGGER.info("BetterSprint initialized");
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

    public static void setTreeMode(boolean enabled) {
        if (instance != null) instance.setTreeModeInternal(enabled);
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
        blocked.treeMode = LiteApiManager.isFeatureBlocked("better_sprint_tree");
        blocked.stairUp = LiteApiManager.isFeatureBlocked("better_sprint_stair_up");
        blocked.waterSprint = LiteApiManager.isFeatureBlocked("better_sprint_water_sprint");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();

        if (!config.isBetterSprintDefaultModeEnabled()) {
            defaultState.delayUntilTick = 0;
            defaultState.sprint = false;
            defaultState.active = false;
        }

        if (!config.isBetterSprintPvPModeEnabled()) {
            pvpState.delayUntilTick = 0;
            pvpState.sprint = false;
            pvpState.active = false;
        }

        if (!config.isBetterSprintTreeModeEnabled()) {
            treeState.delayUntilTick = 0;
            treeState.sprint = false;
            treeState.active = false;
        }

        if (!config.isBetterSprintWaterSprintEnabled()) {
            waterSprintState.sprint = false;
            waterSprintState.active = false;
        }

        if (!config.isBetterSprintStairUpEnabled()) {
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
        config.setBetterSprintDefaultModeEnabled(enabled);

        if (enabled) {
            defaultState.delayUntilTick = 0;
        } else {
            defaultState.active = false;
            defaultState.sprint = false;
        }
    }

    private void setPvPModeInternal(boolean enabled) {
        config.setBetterSprintPvPModeEnabled(enabled);

        if (enabled) {
            pvpState.delayUntilTick = 0;
        } else {
            pvpState.active = false;
            pvpState.sprint = false;
        }
    }

    private void setTreeModeInternal(boolean enabled) {
        config.setBetterSprintTreeModeEnabled(enabled);

        if (enabled) {
            treeState.delayUntilTick = 0;
        } else {
            treeState.active = false;
            treeState.sprint = false;
        }
    }

    private void setStairUpInternal(boolean enabled) {
        config.setBetterSprintStairUpEnabled(enabled);

        if (!enabled) {
            disableStairUp();
        }
    }

    private void setWaterSprintInternal(boolean enabled) {
        config.setBetterSprintWaterSprintEnabled(enabled);

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
            || !config.isBetterSprintPvPModeEnabled()
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
            || !config.isBetterSprintPvPModeEnabled()
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
        long currentTick = player.age;

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
        treeState.sprint = false;
        waterSprintState.sprint = false;

        updateInternalDefault(currentTick, moving, hasFood, inWaterOrLava);
        updateInternalPvP(currentTick, moving, hasFood, inWaterOrLava);
        updateInternalTree(currentTick, moving, hasFood, inWaterOrLava);
        updateInternalStairUp(player);
        updateInternalWaterSprint(player, moving, hasFood);

        if (!hasFood) {
            if (player.isSprinting()) {
                player.setSprinting(false);
                defaultState.lastStoppedTick = currentTick;
                pvpState.lastStoppedTick = currentTick;
                treeState.lastStoppedTick = currentTick;
            }

            resetAllMovementStates();
            return;
        }

        boolean anySprintModeEnabled = (!blocked.defaultMode && config.isBetterSprintDefaultModeEnabled())
            || (!blocked.pvpMode && config.isBetterSprintPvPModeEnabled())
            || (!blocked.treeMode && config.isBetterSprintTreeModeEnabled())
            || (!blocked.waterSprint && config.isBetterSprintWaterSprintEnabled());

        if (!anySprintModeEnabled) {
            return;
        }

        boolean finalSprint = defaultState.sprint || pvpState.sprint || treeState.sprint || waterSprintState.sprint;

        if (finalSprint) {
            if (!player.isSprinting()) {
                player.setSprinting(true);
            }
        } else {
            boolean shouldControlSprint = defaultState.active || pvpState.active || treeState.active || waterSprintState.active;

            if (shouldControlSprint && player.isSprinting()) {
                player.setSprinting(false);

                defaultState.lastStoppedTick = currentTick;
                pvpState.lastStoppedTick = currentTick;
                treeState.lastStoppedTick = currentTick;
            }
        }
    }

    /// Единый хелпер для режимов
    private void updateMode(
        ModesState state,
        boolean enabled,
        boolean canUse,
        boolean instantAllowed,
        int instantChance,
        long currentTick,
        DelayCalculator delayCalc) {

        if (!enabled || !canUse) {
            if (state.active) {
                state.lastStoppedTick = currentTick;
            }

            state.active = false;
            state.sprint = false;
            state.delayUntilTick = 0;
            return;
        }

        if (!state.active) {
            boolean canInstant = canInstantRestart(currentTick, state.lastStoppedTick, instantChance);

            if (canInstant) {
                state.sprint = true;
                state.active = true;
                state.delayUntilTick = 0;
                return;
            }

            if (state.delayUntilTick == 0) {
                state.delayUntilTick = delayCalc.calc(currentTick);
            } else if (currentTick >= state.delayUntilTick) {
                state.sprint = true;
                state.active = true;
                state.delayUntilTick = 0;
            }

            return;
        }

        state.sprint = true;
    }

    /// Обновление самих режимов
    private void updateInternalDefault(long currentTick, boolean moving, boolean hasFood, boolean inWaterOrLava) {
        updateMode(
            defaultState,
            config.isBetterSprintDefaultModeEnabled() && !blocked.defaultMode && !inWaterOrLava,
            moving && hasFood,
            true,
            33,
            currentTick,
            this::calculateDefaultDelayTick
        );
    }

    private void updateInternalPvP(long currentTick, boolean moving, boolean hasFood, boolean inWaterOrLava) {
        updateMode(
            pvpState,
            config.isBetterSprintPvPModeEnabled() && !blocked.pvpMode && !inWaterOrLava && currentTick < pvpExpireTick,
            moving && hasFood,
            true,
            25,
            currentTick,
            this::calculatePvPDelayTick
        );
    }

    private void updateInternalTree(long currentTick, boolean moving, boolean hasFood, boolean inWaterOrLava) {
        updateMode(
            treeState,
            config.isBetterSprintTreeModeEnabled() && !blocked.treeMode && !inWaterOrLava && isFoliageAboveHead(mc(), currentTick),
            moving && hasFood,
            true,
            25,
            currentTick,
            this::calculateTreeDelayTick
        );
    }

    private void updateInternalStairUp(ClientPlayerEntity player) {
        MinecraftClient client = mc();

        if (client == null || client.options == null) {
            return;
        }

        if (!config.isBetterSprintEnabled()
            || blocked.main
            || blocked.stairUp
            || !config.isBetterSprintStairUpEnabled()) {

            disableStairUp();
            return;
        }

        if (!client.options.getAutoJump().getValue()) {
            client.options.getAutoJump().setValue(true);
        }
    }

    private void updateInternalWaterSprint(ClientPlayerEntity player, boolean moving, boolean hasFood) {
        waterSprintState.active = !blocked.waterSprint
            && config.isBetterSprintWaterSprintEnabled()
            && player.isTouchingWater()
            && !player.isInLava()
            && hasFood
            && moving
            && isInFullWaterBlock(player);

        waterSprintState.sprint = waterSprintState.active;
    }

    /// Расчет задержек для режимов
    private long calculateDefaultDelayTick(long currentTick) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        int delayMs;

        if (roll < 73) {
            delayMs = ThreadLocalRandom.current().nextInt(32, 145);
        } else if (roll < 94) {
            delayMs = ThreadLocalRandom.current().nextInt(133, 199);
        } else {
            delayMs = ThreadLocalRandom.current().nextInt(174, 269);
        }

        return currentTick + Math.max(1, delayMs / 50);
    }

    private long calculatePvPDelayTick(long currentTick) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        int delayMs;

        if (roll < 88) {
            delayMs = ThreadLocalRandom.current().nextInt(55, 140);
        } else if (roll < 93) {
            delayMs = ThreadLocalRandom.current().nextInt(134, 190);
        } else {
            delayMs = ThreadLocalRandom.current().nextInt(184, 256);
        }

        return currentTick + Math.max(1, delayMs / 50);
    }

    private long calculateTreeDelayTick(long currentTick) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        int delayMs;

        if (roll < 77) {
            delayMs = ThreadLocalRandom.current().nextInt(41, 159);
        } else if (roll < 93) {
            delayMs = ThreadLocalRandom.current().nextInt(144, 164);
        } else {
            delayMs = ThreadLocalRandom.current().nextInt(162, 234);
        }

        return currentTick + Math.max(1, delayMs / 50);
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

    private boolean canInstantRestart(long currentTick, long lastStopTick, int chancePercent) {
        if (lastStopTick < 0) {
            return false;
        }

        long stoppedTicks = currentTick - lastStopTick;

        if (stoppedTicks < 10) {
            return false;
        }

        return ThreadLocalRandom.current().nextInt(100) < chancePercent;
    }

    /// Дополнительный метод Tree mode (Листва над головой)
    private boolean isFoliageAboveHead(MinecraftClient client, long currentTick) {
        if (client.world == null) {
            leavesState.cachedResult = false;
            leavesState.lastCheckTick = currentTick;
            return false;
        }

        int dimensionHash = client.world.getRegistryKey().hashCode();

        if (dimensionHash != leavesState.dimensionHash) {
            leavesState.dimensionHash = dimensionHash;
            leavesState.cachedResult = false;
            leavesState.lastCheckTick = 0;
        }

        if (currentTick - leavesState.lastCheckTick < 2) {
            return leavesState.cachedResult;
        }

        ClientPlayerEntity player = client.player;

        if (player == null) {
            leavesState.cachedResult = false;
            leavesState.lastCheckTick = currentTick;
            return false;
        }

        BlockPos aboveHead = BlockPos.ofFloored(
            player.getX(),
            player.getY() + player.getHeight() + 1.0D,
            player.getZ()
        );

        leavesState.cachedResult = client.world.getBlockState(aboveHead).isIn(BlockTags.LEAVES);
        leavesState.lastCheckTick = currentTick;

        return leavesState.cachedResult;
    }

    /// Дополнительные методы для Stair Up (Активация/Отключение)
    public static boolean isStairUpActive() {
        return instance != null && instance.isEnabledInternal()
            && !instance.blocked.stairUp
            && instance.config.isBetterSprintStairUpEnabled();
    }

    private void disableStairUp() {
        MinecraftClient client = mc();

        if (client != null && client.options != null) {
            client.options.getAutoJump().setValue(false);
        }
    }

    /// Дополнительный метод для Water Sprint (в полном ли блоке воды я)
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
        defaultState.delayUntilTick = 0;
        pvpState.delayUntilTick = 0;
        treeState.delayUntilTick = 0;

        defaultState.active = false;
        pvpState.active = false;
        treeState.active = false;
        waterSprintState.active = false;

        defaultState.sprint = false;
        pvpState.sprint = false;
        treeState.sprint = false;
        waterSprintState.sprint = false;
    }

    private void resetRemainingStates() {
        pendingTarget = null;
        pendingAttackTick = 0;

        pvpExpireTick = 0;

        defaultState.lastStoppedTick = -1000;
        pvpState.lastStoppedTick = -1000;
        treeState.lastStoppedTick = -1000;

        leavesState.lastCheckTick = 0;
        leavesState.dimensionHash = 0;

        leavesState.cachedResult = false;

        wasEnabledLastTick = false;

        disableStairUp();

        resetAllMovementStates();
    }
}
// v1.0