package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BetterSprint {
    private BetterSprint() {}

    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private static BetterSprint instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean defaultMode;
        boolean pvpMode;
        boolean stairUp;
        boolean waterSprint;
        boolean scaffold;
    }

    private interface DelayCalculator {
        long calc(long currentMs);
    }

    private static class ModesState {
        boolean sprint;
        boolean active;

        long delayUntilMs;
        long lastStoppedMs = -1000L;
    }

    private static class WaterSprintState {
        boolean active;
        boolean sprint;
        long shiftIgnoreUntilTick;
    }

    private static class ScaffoldState {
        boolean active;
        boolean sprint;

        long nextPlacementMs;
        long lastPlacementMs;

        BlockPos pendingTarget;
        Hand pendingHand = Hand.MAIN_HAND;

        int bridgeY;
        boolean bridgeYInitialized;

        BlockPos secondTarget;
        long secondPlacementMs;
    }

    private static class EndConcreteState {
        boolean blocked;

        long lastCheckTick = -1;

        int lastX;
        int lastY;
        int lastZ;
        int dimensionHash;
    }

    private final ModesState defaultState = new ModesState();
    private final ModesState pvpState = new ModesState();

    private final WaterSprintState waterSprintState = new WaterSprintState();
    private final ScaffoldState scaffoldState = new ScaffoldState();

    private final EndConcreteState endConcreteState = new EndConcreteState();

    private long scaffoldLastPlacementTick = -100L;
    private PlayerEntity pendingTarget;
    private long pvpExpireTick;
    private long pendingAttackTick;
    private boolean wasEnabledLastTick;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterSprint();
            instance.refreshBlockedStatusInternal();
            instance.reloadFromConfigInternal();
            BQoL.LOGGER.info("Better Sprint initialized");
        }
    }

    public static BetterSprint getInstance() {
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

    public static void setDefaultMode(boolean enabled) {
        if (instance != null) {
            instance.setDefaultModeInternal(enabled);
        }
    }

    public static void setPvPMode(boolean enabled) {
        if (instance != null) {
            instance.setPvPModeInternal(enabled);
        }
    }

    public static void setStairUp(boolean enabled) {
        if (instance != null) {
            instance.setStairUpInternal(enabled);
        }
    }

    public static void setWaterSprint(boolean enabled) {
        if (instance != null) {
            instance.setWaterSprintInternal(enabled);
        }
    }

    public static void setScaffold(boolean enabled) {
        if (instance != null) {
            instance.setScaffoldInternal(enabled);
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

    public static void update() {
        if (instance != null) {
            instance.updateInternal();
        }
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_sprint");
        blocked.defaultMode = LiteApiManager.isFeatureBlocked("better_sprint_default");
        blocked.pvpMode = LiteApiManager.isFeatureBlocked("better_sprint_pvp");
        blocked.stairUp = LiteApiManager.isFeatureBlocked("better_sprint_stair_up");
        blocked.waterSprint = LiteApiManager.isFeatureBlocked("better_sprint_water_sprint");
        blocked.scaffold = LiteApiManager.isFeatureBlocked("better_sprint_scaffold");
    }

    private void reloadFromConfigInternal() {
        refreshBlockedStatusInternal();

        if (!config.isBetterSprintDefaultMode()) {
            defaultState.sprint = false;
            defaultState.active = false;
            defaultState.delayUntilMs = 0L;
        }

        if (!config.isBetterSprintPvPMode()) {
            pvpState.sprint = false;
            pvpState.active = false;
            pvpState.delayUntilMs = 0L;
        }

        if (!config.isBetterSprintWaterSprint()) {
            waterSprintState.sprint = false;
            waterSprintState.active = false;
        }

        if (!config.isBetterSprintScaffold()) {
            resetScaffoldPlacement();
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
            defaultState.delayUntilMs = 0L;
        } else {
            defaultState.active = false;
            defaultState.sprint = false;
        }
    }

    private void setPvPModeInternal(boolean enabled) {
        config.setBetterSprintPvPMode(enabled);

        if (enabled) {
            pvpState.delayUntilMs = 0L;
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

    private void setScaffoldInternal(boolean enabled) {
        config.setBetterSprintScaffold(enabled);

        if (!enabled) {
            resetScaffoldPlacement();
        }

        scaffoldLastPlacementTick = -100L;
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

        if (client == null
            || client.player == null
            || client.world == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        long currentMs = System.currentTimeMillis();

        updatePendingAttack();

        if (!isEnabledInternal()) {
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
        scaffoldState.sprint = false;

        updateInternalDefault(currentMs, moving, hasFood, inWaterOrLava);
        updateInternalPvP(currentMs, moving, hasFood, inWaterOrLava);
        updateInternalStairUp(player);
        updateInternalWaterSprint(currentMs, player, moving, hasFood);
        updateInternalScaffold(currentMs, player);

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

    private void updateMode(ModesState state, boolean enabled, boolean canUse, boolean instantAllowed, int instantChance, long currentMs, DelayCalculator delayCalc) {
        if (!enabled || !canUse) {
            if (state.active) {
                state.lastStoppedMs = currentMs;
            }

            state.active = false;
            state.sprint = false;
            state.delayUntilMs = 0L;
            return;
        }

        if (!state.active) {
            boolean canInstant = instantAllowed && canInstantRestart(currentMs, state.lastStoppedMs, instantChance);

            if (canInstant) {
                state.sprint = true;
                state.active = true;
                state.delayUntilMs = 0L;
                return;
            }

            if (state.delayUntilMs == 0L) {
                state.delayUntilMs = delayCalc.calc(currentMs);
            } else if (currentMs >= state.delayUntilMs) {
                state.sprint = true;
                state.active = true;
                state.delayUntilMs = 0L;
            }

            return;
        }

        state.sprint = true;
    }

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

        if (client == null
            || client.options == null
            || player == null
            || client.world == null) {
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

        if (isWaterSprintEndConcreteNearby(player)) {
            disableStairUp();
            return;
        }

        if (!client.options.getAutoJump().getValue()) {
            client.options.getAutoJump().setValue(true);
        }
    }

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
            waterSprintState.shiftIgnoreUntilTick = player.age + 8L;
        }
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

    private void updateInternalScaffold(long currentMs, ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null
            || player == null
            || client.world == null
            || client.interactionManager == null
            || !isEnabledInternal()
            || blocked.scaffold
            || !config.isBetterSprintScaffold()) {
            resetScaffoldPlacement();
            return;
        }

        if (client.currentScreen != null
            || player.isSpectator()
            || player.isFallFlying()
            || player.isClimbing()
            || player.hasVehicle()
            || player.isTouchingWater()
            || player.isInLava()) {
            resetScaffoldPlacement();
            return;
        }

        if (!client.options.useKey.isPressed()) {
            resetScaffoldPlacement();
            return;
        }

        Hand hand;

        if (isScaffoldStack(player.getMainHandStack())) {
            hand = Hand.MAIN_HAND;
        } else if (isScaffoldStack(player.getOffHandStack())) {
            hand = Hand.OFF_HAND;
        } else {
            resetScaffoldPlacement();
            return;
        }

        if (!scaffoldState.bridgeYInitialized) {
            scaffoldState.bridgeY = net.minecraft.util.math.MathHelper.floor(player.getBoundingBox().minY - 0.05D);
            scaffoldState.bridgeYInitialized = true;
        } else if (player.isOnGround()) {
            scaffoldState.bridgeY = net.minecraft.util.math.MathHelper.floor(player.getBoundingBox().minY - 0.05D);
        }

        BlockPos currentBelow = BlockPos.ofFloored(
            player.getX(),
            scaffoldState.bridgeY,
            player.getZ()
        );

        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;

        double yawRad = Math.toRadians(player.getYaw());

        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        double moveX = forwardX * forward + Math.cos(yawRad) * sideways;
        double moveZ = forwardZ * forward + Math.sin(yawRad) * sideways;

        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);

        if (length > 0.0001D) {
            moveX /= length;
            moveZ /= length;
        }

        List<BlockPos> candidates = new ArrayList<>();
        candidates.add(currentBelow);

        if (length > 0.0001D) {
            for (int distance = 1; distance <= 2; distance++) {
                int extraDistance = ThreadLocalRandom.current().nextInt(100) < 35 ? 1 : 0;

                BlockPos target = BlockPos.ofFloored(
                    player.getX() + moveX * (distance + extraDistance),
                    scaffoldState.bridgeY,
                    player.getZ() + moveZ * (distance + extraDistance)
                );

                if (!candidates.contains(target)) {
                    candidates.add(target);
                }
            }

            if (forward > 0.0F && Math.abs(sideways) > 0.0F) {
                int offsetX = moveX > 0.15D ? 1 : moveX < -0.15D ? -1 : 0;
                int offsetZ = moveZ > 0.15D ? 1 : moveZ < -0.15D ? -1 : 0;

                if (offsetX != 0) {
                    BlockPos sideX = currentBelow.add(offsetX, 0, 0);

                    if (!candidates.contains(sideX)) {
                        candidates.add(sideX);
                    }
                }

                if (offsetZ != 0) {
                    BlockPos sideZ = currentBelow.add(0, 0, offsetZ);

                    if (!candidates.contains(sideZ)) {
                        candidates.add(sideZ);
                    }
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(player.getPos())));

        if (scaffoldLastPlacementTick == player.age) {
            return;
        }

        if (scaffoldState.secondTarget != null) {
            if (currentMs < scaffoldState.secondPlacementMs) {
                return;
            }

            BlockPos secondTarget = scaffoldState.secondTarget;

            scaffoldState.secondTarget = null;
            scaffoldState.secondPlacementMs = 0L;

            if (!canScaffoldPlaceAt(client.world, secondTarget) || !hasScaffoldDepth(client.world, secondTarget)) {
                return;
            }

            BlockHitResult secondHit = findScaffoldFace(client.world, secondTarget, player);

            if (secondHit == null) {
                return;
            }

            BlockPos secondNeighbor = secondHit.getBlockPos();

            if (!canScaffoldInteract(client, player, hand, secondTarget, secondNeighbor)) {
                return;
            }

            ActionResult secondResult = client.interactionManager.interactBlock(player, hand, secondHit);

            scaffoldLastPlacementTick = player.age;

            if (secondResult.isAccepted()) {
                player.swingHand(hand);

                scaffoldState.active = true;
                scaffoldState.lastPlacementMs = currentMs;
                scaffoldState.nextPlacementMs = currentMs + calculateScaffoldDelayMs();
            }

            return;
        }

        if (scaffoldState.nextPlacementMs > 0L && currentMs < scaffoldState.nextPlacementMs) {
            return;
        }

        for (BlockPos target : candidates) {
            if (!canScaffoldPlaceAt(client.world, target) || !hasScaffoldDepth(client.world, target)) {
                continue;
            }

            BlockHitResult hit = findScaffoldFace(client.world, target, player);

            if (hit == null) {
                continue;
            }

            BlockPos neighbor = hit.getBlockPos();

            if (!canScaffoldInteract(client, player, hand, target, neighbor)) {
                continue;
            }

            ActionResult result = client.interactionManager.interactBlock(player, hand, hit);

            if (!result.isAccepted()) {
                continue;
            }

            player.swingHand(hand);

            scaffoldState.active = true;
            scaffoldState.lastPlacementMs = currentMs;
            scaffoldState.nextPlacementMs = currentMs + calculateScaffoldDelayMs();
            scaffoldLastPlacementTick = player.age;

            if (forward > 0.0F && ThreadLocalRandom.current().nextInt(100) < 50) {
                for (BlockPos secondCandidate : candidates) {
                    if (secondCandidate.equals(target)
                        || !canScaffoldPlaceAt(client.world, secondCandidate)
                        || !hasScaffoldDepth(client.world, secondCandidate)) {
                        continue;
                    }

                    BlockHitResult secondCandidateHit = findScaffoldFace(client.world, secondCandidate, player);

                    if (secondCandidateHit == null || !canScaffoldInteract(client, player, hand, secondCandidate, secondCandidateHit.getBlockPos())) {
                        continue;
                    }

                    scaffoldState.secondTarget = secondCandidate.toImmutable();
                    scaffoldState.secondPlacementMs = currentMs + calculateScaffoldDelayMs();

                    break;
                }
            }

            return;
        }
    }

    private void resetScaffoldPlacement() {
        scaffoldState.active = false;
        scaffoldState.sprint = false;

        scaffoldState.pendingTarget = null;
        scaffoldState.pendingHand = Hand.MAIN_HAND;

        scaffoldState.nextPlacementMs = 0L;

        scaffoldState.bridgeYInitialized = false;
    }

    private boolean canScaffoldInteract(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockPos target, BlockPos currentBelow) {
        if (client == null
            || client.world == null
            || client.interactionManager == null
            || client.options == null
            || client.currentScreen != null
            || player == null
            || player.isRemoved()
            || player.isSpectator()
            || player.hasVehicle()
            || player.isFallFlying()
            || player.isClimbing()
            || player.isTouchingWater()
            || player.isInLava()
            || !isEnabledInternal()
            || blocked.scaffold
            || !config.isBetterSprintScaffold()
            || !client.options.useKey.isPressed()) {

            return false;
        }

        ItemStack stack = hand == Hand.MAIN_HAND ? player.getMainHandStack() : player.getOffHandStack();

        if (!isScaffoldStack(stack)
            || target == null
            || currentBelow == null
            || Math.abs(target.getX() - currentBelow.getX()) > 1
            || Math.abs(target.getZ() - currentBelow.getZ()) > 1
            || !canScaffoldPlaceAt(client.world, target)
            || !hasScaffoldDepth(client.world, target)) {

            return false;
        }

        return true;
    }

    private boolean canScaffoldPlaceAt(World world, BlockPos pos) {
        if (world == null
            || pos == null
            || pos.getY() < world.getBottomY()
            || pos.getY() >= world.getTopY()) {
            return false;
        }

        if (!world.isChunkLoaded(pos)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        if (!state.isReplaceable() || !state.getFluidState().isEmpty()) {
            return false;
        }

        MinecraftClient client = mc();

        if (client != null && client.player != null) {
            Box targetBox = new Box(pos).expand(-0.001D);
            Box playerBox = client.player.getBoundingBox().expand(-0.001D);

            if (targetBox.intersects(playerBox)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasScaffoldDepth(World world, BlockPos target) {
        if (world == null || target == null) {
            return false;
        }

        int targetY = target.getY();

        if (targetY < world.getBottomY() + 3 || targetY >= world.getTopY()) {
            return false;
        }

        final int minDepth = 3;
        final int maxDepth = 16;

        for (int depth = 1; depth <= maxDepth; depth++) {
            BlockPos below = target.down(depth);

            if (below.getY() < world.getBottomY() || !world.isChunkLoaded(below)) {
                return false;
            }

            BlockState state = world.getBlockState(below);

            if (state.isAir()
                || state.isReplaceable()
                || state.getCollisionShape(world, below).isEmpty()) {
                continue;
            }

            return depth >= minDepth;
        }

        return true;
    }

    private boolean isScaffoldStack(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) {
            return false;
        }

        BlockState state = item.getBlock().getDefaultState();

        return state.isFullCube(net.minecraft.world.EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    }

    private BlockHitResult findScaffoldFace(World world, BlockPos target, ClientPlayerEntity player) {
        if (world == null
            || target == null
            || player == null) {
            return null;
        }

        Vec3d eye = player.getEyePos();

        double maxReachSquared = 4.5D * 4.5D;

        BlockHitResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        Direction[] directions = {
            Direction.DOWN,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST,
            Direction.UP
        };

        for (Direction offset : directions) {
            BlockPos neighbor = target.offset(offset);

            BlockState state = world.getBlockState(neighbor);

            if (state.isAir() || state.isReplaceable()) {
                continue;
            }

            VoxelShape shape = state.getOutlineShape(world, neighbor);

            if (shape.isEmpty()) {
                shape = state.getCollisionShape(world, neighbor);
            }

            if (shape.isEmpty()) {
                continue;
            }

            Direction face = offset.getOpposite();

            for (Box box : shape.getBoundingBoxes()) {
                double minX = neighbor.getX() + box.minX;
                double minY = neighbor.getY() + box.minY;
                double minZ = neighbor.getZ() + box.minZ;

                double maxX = neighbor.getX() + box.maxX;
                double maxY = neighbor.getY() + box.maxY;
                double maxZ = neighbor.getZ() + box.maxZ;

                double[] samples = {0.2D, 0.5D, 0.8D};

                for (double u : samples) {
                    for (double v : samples) {
                        double x;
                        double y;
                        double z;

                        switch (face) {
                            case WEST -> {
                                x = minX;
                                y = minY + (maxY - minY) * u;
                                z = minZ + (maxZ - minZ) * v;
                            }

                            case EAST -> {
                                x = maxX;
                                y = minY + (maxY - minY) * u;
                                z = minZ + (maxZ - minZ) * v;
                            }

                            case NORTH -> {
                                x = minX + (maxX - minX) * u;
                                y = minY + (maxY - minY) * v;
                                z = minZ;
                            }

                            case SOUTH -> {
                                x = minX + (maxX - minX) * u;
                                y = minY + (maxY - minY) * v;
                                z = maxZ;
                            }

                            case DOWN -> {
                                x = minX + (maxX - minX) * u;
                                y = minY;
                                z = minZ + (maxZ - minZ) * v;
                            }

                            case UP -> {
                                x = minX + (maxX - minX) * u;
                                y = maxY;
                                z = minZ + (maxZ - minZ) * v;
                            }

                            default -> {
                                continue;
                            }
                        }

                        Vec3d point = new Vec3d(x, y, z);

                        double distance = eye.squaredDistanceTo(point);

                        if (distance > maxReachSquared || distance >= nearestDistance) {
                            continue;
                        }

                        Vec3d rayEnd = point.add(
                            -face.getOffsetX() * 0.001D,
                            -face.getOffsetY() * 0.001D,
                            -face.getOffsetZ() * 0.001D
                        );

                        HitResult ray = world.raycast(
                            new RaycastContext(
                                eye,
                                rayEnd,
                                RaycastContext.ShapeType.OUTLINE,
                                RaycastContext.FluidHandling.NONE,
                                player
                            )
                        );

                        if (!(ray instanceof BlockHitResult blockHit)
                            || !blockHit.getBlockPos().equals(neighbor)
                            || blockHit.getSide() != face) {
                            continue;
                        }

                        nearestDistance = distance;

                        nearest = new BlockHitResult(blockHit.getPos(), face, neighbor, false);
                    }
                }
            }
        }

        return nearest;
    }

    private boolean isScaffoldForwardOrDiagonal(ClientPlayerEntity player) {
        if (player == null || player.input == null) {
            return false;
        }

        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;

        return forward > 0.0F && (sideways == 0.0F || Math.abs(sideways) > 0.0F);
    }

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

    private long calculateScaffoldDelayMs() {
        int roll = ThreadLocalRandom.current().nextInt(100);

        if (roll < 60) {
            return ThreadLocalRandom.current().nextLong(7L, 12L);
        }

        if (roll < 90) {
            return ThreadLocalRandom.current().nextLong(12L, 16L);
        }

        return ThreadLocalRandom.current().nextLong(16L, 19L);
    }

    private long delayBooster(PlayerEntity player) {
        if (player == null) {
            return 0L;
        }

        long delay = 0L;

        StatusEffectInstance speed =
            player.getStatusEffect(StatusEffects.SPEED);

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

    private static boolean isMoving(ClientPlayerEntity player) {
        if (player == null || player.input == null) {
            return false;
        }

        boolean input = player.input.movementForward != 0.0F || player.input.movementSideways != 0.0F;

        Vec3d velocity = player.getVelocity();

        double speedSq = velocity.x * velocity.x + velocity.z * velocity.z;

        return input || speedSq > 0.0025D;
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
        if (lastStopMs < 0L || currentMs - lastStopMs < 500L) {
            return false;
        }

        return ThreadLocalRandom.current().nextInt(100) < chancePercent;
    }

    private boolean isWaterSprintEndConcreteNearby(ClientPlayerEntity player) {
        MinecraftClient client = mc();

        if (client == null
            || client.world == null
            || player == null) {
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

        int dimensionHash =
            client.world.getRegistryKey().hashCode();

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

                    if (client.world.getBlockState(mutablePos)
                        .isOf(Blocks.BLACK_CONCRETE_POWDER)) {

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
            player.getY() + 0.05D,
            player.getZ()
        );

        BlockState state = client.world.getBlockState(feet);

        return state.getFluidState().isStill() && player.isTouchingWater();
    }

    public static boolean isRestrictedServer() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getCurrentServerEntry() == null) {
            return false;
        }

        String address = client.getCurrentServerEntry().address.toLowerCase();

        return address.contains("holyworld") || address.contains("funtime");
    }
    private void resetAllMovementStates() {
        defaultState.delayUntilMs = 0L;
        pvpState.delayUntilMs = 0L;

        defaultState.active = false;
        pvpState.active = false;
        waterSprintState.active = false;

        defaultState.sprint = false;
        pvpState.sprint = false;
        waterSprintState.sprint = false;
    }

    private void resetRemainingStates() {
        pendingTarget = null;
        pendingAttackTick = 0L;

        pvpExpireTick = 0L;

        defaultState.lastStoppedMs = -1000L;
        pvpState.lastStoppedMs = -1000L;

        endConcreteState.blocked = false;
        endConcreteState.lastCheckTick = -1L;

        endConcreteState.dimensionHash = 0;
        endConcreteState.lastX = 0;
        endConcreteState.lastY = 0;
        endConcreteState.lastZ = 0;

        wasEnabledLastTick = false;

        waterSprintState.shiftIgnoreUntilTick = 0L;

        resetScaffoldPlacement();

        scaffoldState.lastPlacementMs = 0L;
        scaffoldLastPlacementTick = -100L;

        disableStairUp();

        resetAllMovementStates();
    }
}