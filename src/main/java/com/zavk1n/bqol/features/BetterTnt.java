package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.accessors.ACCESSORTNTRenderState;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class BetterTnt {

    private BetterTnt() {}

    private static BetterTnt instance;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static final class BlockedFeatures {
        private boolean main;
        private boolean timer;
        private boolean alert;
        private boolean showXYZ;
        private boolean showBlocks;
        private boolean explosionArea;
    }

    private static final TntType[] TNT_TYPES = TntType.values();
    private final Map<UUID, TrackedTnt> trackedAlerts = new HashMap<>();

    public enum TntType {
        NORMAL,
        A,
        B,
        B2,
        C4,
        ICE_WAVE,
        EXPLOSIVE_WAVE
    }

    public static final class AlertInfo {

        private final TntType type;
        private final double seconds;
        private final int x;
        private final int y;
        private final int z;

        public AlertInfo(TntType type, double seconds, int x, int y, int z) {
            this.type = type;
            this.seconds = seconds;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public TntType getType() {
            return type;
        }

        public double getSeconds() {
            return seconds;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public String getFuseText() {
            return formatSeconds(seconds);
        }

        public String getText(Vec3d playerPos) {
            StringBuilder text = new StringBuilder(64)
                .append("Время до взрыва ")
                .append(getAlertTypeName(type))
                .append(" - ")
                .append(getFuseText());

            if (isAlertShowXYZEnabled()) {
                text.append(" (X = ").append(x)
                    .append(", Y = ").append(y)
                    .append(", Z = ").append(z)
                    .append(')');
            } else if (isAlertShowBlocksEnabled()) {
                text.append(" (в ")
                    .append(String.format(Locale.ROOT, "%.1f", getDistanceBlocks(playerPos)))
                    .append(" блоках)");
            }

            return text.toString();
        }

        public double getDistanceBlocks(Vec3d playerPos) {
            double dx = x + 0.5D - playerPos.x;
            double dy = y + 0.5D - playerPos.y;
            double dz = z + 0.5D - playerPos.z;

            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        private double getDistanceSquared(Vec3d playerPos) {
            double dx = x + 0.5D - playerPos.x;
            double dy = y + 0.5D - playerPos.y;
            double dz = z + 0.5D - playerPos.z;

            return dx * dx + dy * dy + dz * dz;
        }
    }

    private static final class TrackedTnt {
        private TntType type;

        private int x;
        private int y;
        private int z;

        private int lastServerFuse;

        private long lastSeenTick;
        private long endTimeNanos;

        private TrackedTnt(
            TntEntity entity,
            TntType type,
            int fuse,
            long now,
            long tick
        ) {
            this.type = type;
            this.lastServerFuse = fuse;
            this.lastSeenTick = tick;
            this.endTimeNanos = now + fuse * 50_000_000L;

            updatePosition(entity);
        }

        private void updatePosition(TntEntity entity) {
            this.x = entity.getBlockX();
            this.y = entity.getBlockY();
            this.z = entity.getBlockZ();
        }

        private double getSeconds(long now) {
            return Math.max(0.0D, (endTimeNanos - now) / 1_000_000_000.0D);
        }
    }

    public static void initialize() {
        if (instance != null) {
            return;
        }
        instance = new BetterTnt();
        instance.refreshBlockedStatusInternal();
        BQoL.LOGGER.info("Better Tnt initialized");
    }

    public static BetterTnt getInstance() {
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
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) {
            instance.setEnabledInternal(enabled);
        }
    }

    public static boolean isTimerEnabled() {
        return instance != null && instance.isTimerEnabledInternal();
    }

    public static void setTimerEnabled(boolean enabled) {
        if (instance != null) {
            instance.setTimerEnabledInternal(enabled);
        }
    }

    public static boolean isAlertEnabled() {
        return instance != null && instance.isAlertEnabledInternal();
    }

    public static void setAlertEnabled(boolean enabled) {
        if (instance != null) {
            instance.setAlertEnabledInternal(enabled);
        }
    }

    public static int getAlertPosition() {
        return getInstance().config.getBetterTntAlertPosition();
    }

    public static boolean isAlertShowXYZEnabled() {
        return instance != null && instance.isAlertShowXYZEnabledInternal();
    }

    public static void setAlertShowXYZEnabled(boolean enabled) {
        if (instance != null) {
            instance.setAlertShowXYZEnabledInternal(enabled);
        }
    }

    public static boolean isAlertShowBlocksEnabled() {
        return instance != null && instance.isAlertShowBlocksEnabledInternal();
    }

    public static void setAlertShowBlocksEnabled(boolean enabled) {
        if (instance != null) {
            instance.setAlertShowBlocksEnabledInternal(enabled);
        }
    }

    public static boolean isExplosionAreaEnabled() {
        return instance != null && instance.isExplosionAreaEnabledInternal();
    }

    public static void setExplosionAreaEnabled(boolean enabled) {
        if (instance != null) {
            instance.setExplosionAreaEnabledInternal(enabled);
        }
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_tnt");
        blocked.timer = LiteApiManager.isFeatureBlocked("better_tnt_timer_tnt");
        blocked.alert = LiteApiManager.isFeatureBlocked("better_tnt_alert");
        blocked.showXYZ = LiteApiManager.isFeatureBlocked("better_tnt_alert_show_xyz");
        blocked.showBlocks = LiteApiManager.isFeatureBlocked("better_tnt_alert_show_blocks");
        blocked.explosionArea = LiteApiManager.isFeatureBlocked("better_tnt_explosion_area");
    }

    private boolean isEnabledInternal() {
        return config.isBetterTntEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterTntEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isTimerEnabledInternal() {
        return config.isBetterTntTimer()
            && !blocked.timer
            && isEnabledInternal();
    }

    private void setTimerEnabledInternal(boolean enabled) {
        config.setBetterTntTimer(enabled);
    }

    private boolean isAlertEnabledInternal() {
        return config.isBetterTntAlert()
            && !blocked.alert
            && isEnabledInternal();
    }

    private void setAlertEnabledInternal(boolean enabled) {
        config.setBetterTntAlert(enabled);
    }

    private boolean isAlertShowXYZEnabledInternal() {
        return config.isBetterTntAlertShowXYZ()
            && !blocked.showXYZ
            && isEnabledInternal();
    }

    private void setAlertShowXYZEnabledInternal(boolean enabled) {
        config.setBetterTntAlertShowXYZ(enabled);
    }

    private boolean isAlertShowBlocksEnabledInternal() {
        return config.isBetterTntAlertShowBlocks()
            && !blocked.showBlocks
            && isEnabledInternal();
    }

    private void setAlertShowBlocksEnabledInternal(boolean enabled) {
        config.setBetterTntAlertShowBlocks(enabled);
    }

    private boolean isExplosionAreaEnabledInternal() {
        return config.isBetterTntExplosionArea()
            && !blocked.explosionArea
            && isEnabledInternal();
    }

    private void setExplosionAreaEnabledInternal(boolean enabled) {
        config.setBetterTntExplosionArea(enabled);
    }

    public void tickTracking() {
        if (client.world == null
            || client.player == null
            || (!isTimerEnabled() && !isAlertEnabled())) {
            trackedAlerts.clear();
            return;
        }

        long now = System.nanoTime();
        long tick = client.world.getTime();

        Box searchBox = client.player.getBoundingBox().expand(32.0D);

        List<TntEntity> nearbyTnts = client.world.getEntitiesByClass(TntEntity.class, searchBox, TntEntity::isAlive);

        for (TntEntity tnt : nearbyTnts) {
            trackTnt(tnt, now, tick);
        }

        trackedAlerts.values().removeIf(tracked -> tracked.lastSeenTick != tick || tracked.endTimeNanos <= now);
    }

    private void trackTnt(TntEntity tnt, long now, long tick) {
        if (tnt == null || !tnt.isAlive()) {
            return;
        }

        UUID uuid = tnt.getUuid();

        int fuse = Math.max(0, tnt.getFuse());

        TrackedTnt tracked = trackedAlerts.get(uuid);

        if (tracked == null) {
            trackedAlerts.put(uuid, new TrackedTnt(tnt, getTntType(tnt), fuse, now, tick));
            return;
        }

        tracked.lastSeenTick = tick;
        tracked.updatePosition(tnt);

        TntType type = getTntType(tnt);

        if (tracked.type != type) {
            tracked.type = type;
        }

        if (fuse != tracked.lastServerFuse) {
            tracked.lastServerFuse = fuse;
            tracked.endTimeNanos = now + fuse * 50_000_000L;
        }
    }

    public static void registerTnt(TntEntity entity) {
        if (entity != null) {
            getTntType(entity);
        }
    }

    public static TntType getTntType(TntEntity entity) {
        if (entity == null) {
            return TntType.NORMAL;
        }

        return detectType(entity.getName().getString());
    }

    private static TntType detectType(String name) {
        if (name == null || name.isEmpty()) {
            return TntType.NORMAL;
        }

        String normalized = name.toLowerCase(Locale.ROOT);

        if (normalized.contains("динамит b2") || normalized.contains("динамит б2")) {
            return TntType.B2;
        }

        if (normalized.contains("динамит b") || normalized.contains("динамит в")) {
            return TntType.B;
        }

        if (normalized.contains("динамит a") || normalized.contains("динамит а")) {
            return TntType.A;
        }

        if (normalized.contains("c4 взрывчатка") || normalized.contains("с4 взрывчатка")) {
            return TntType.C4;
        }

        if (normalized.contains("ледяная волна")) {
            return TntType.ICE_WAVE;
        }

        if (normalized.contains("разрывная волна")) {
            return TntType.EXPLOSIVE_WAVE;
        }

        return TntType.NORMAL;
    }

    public static int getTimerColor(TntType type, double seconds) {
        /*
         * A / B / B2:
         *
         * 0.00 - 2.00  Красный
         * 2.01 - 5.00  Желтый
         * 5.01 - 7.00  Светло-зеленый
         * 7.01 - 10.00 Зеленый
         */

        if (type == TntType.A || type == TntType.B || type == TntType.B2) {
            if (seconds <= 2.0D) {
                return 0xFFFF5555;
            }

            if (seconds <= 5.0D) {
                return 0xFFFFFF55;
            }

            return 0xFF55FF55;
        }

        /*
         * C4 / Ледяная волна / Разрыва / Обычный:
         *
         * 0.00 - 1.00 Красный
         * 1.01 - 3.00 Желтый
         * 3.01 - 4.00 Светло-зеленый
         * 4.01 - 5.00 Зеленый
         */

        if (seconds <= 1.0D) {
            return 0xFFFF5555;
        }

        if (seconds <= 3.0D) {
            return 0xFFFFFF55;
        }

        return 0xFF55FF55;
    }

    public static void prepareRenderTimer(TntEntity entity, TntEntityRenderState state) {
        if (entity == null || state == null) {
            return;
        }

        ACCESSORTNTRenderState access = (ACCESSORTNTRenderState) state;
        access.bqol$setTntTypeOrdinal(getTntType(entity).ordinal());
    }

    public static void renderTimer(MatrixStack matrices, TntEntityRenderState state, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (!isTimerEnabled()
            || state == null
            || queue == null
            || cameraState == null) {
            return;
        }

        if (state.fuse <= 0) {
            return;
        }

        ACCESSORTNTRenderState access = (ACCESSORTNTRenderState) state;

        int ordinal = access.bqol$getTntTypeOrdinal();

        TntType type = ordinal >= 0 && ordinal < TNT_TYPES.length ? TNT_TYPES[ordinal] : TntType.NORMAL;

        double seconds = state.fuse / 20.0D;

        Text label = Text.literal(formatSeconds(seconds))
            .styled(style -> style.withColor(TextColor.fromRgb(getTimerColor(type, seconds) & 0xFFFFFF)));

        queue.submitLabel(matrices, new Vec3d(0.0D, state.height + 0.30D, 0.0D), 0, label, true, state.light, state.squaredDistanceToCamera, cameraState);
    }

    private static String formatSeconds(double seconds) {
        long hundredths = Math.max(0L, Math.round(seconds * 100.0D));
        long whole = hundredths / 100L;
        long fraction = hundredths % 100L;

        return whole + "." + (fraction < 10L ? "0" : "") + fraction;
    }


    public static List<AlertInfo> getAlertTnts(float tickDelta) {
        if (!isAlertEnabled()) {
            return Collections.emptyList();
        }

        BetterTnt self = getInstance();

        if (self.client.world == null || self.client.player == null) {
            return Collections.emptyList();
        }

        long now = System.nanoTime();

        Vec3d playerPos = self.client.player.getEntityPos();

        self.trackedAlerts.values().removeIf(tracked -> tracked.endTimeNanos <= now);

        if (self.trackedAlerts.isEmpty()) {
            return Collections.emptyList();
        }

        List<AlertInfo> result = new ArrayList<>(self.trackedAlerts.size());

        for (TrackedTnt tracked : self.trackedAlerts.values()) {
            double seconds = tracked.getSeconds(now);

            if (seconds <= 0.0D) {
                continue;
            }

            result.add(new AlertInfo(tracked.type, seconds, tracked.x, tracked.y, tracked.z));
        }

        result.sort(Comparator.comparingDouble(alert -> alert.getDistanceSquared(playerPos)));

        return result;
    }

    public static String getAlertTypeName(TntType type) {
        if (type == null) {
            return "Обычного динамита";
        }

        return switch (type) {
            case A -> "Динамита A";
            case B -> "Динамита B";
            case B2 -> "Динамита Б2";
            case C4 -> "C4 взрывчатки";
            case ICE_WAVE -> "Ледяной волны";
            case EXPLOSIVE_WAVE -> "Разрывной волны";
            case NORMAL -> "Обычного динамита";
        };
    }

    public static double getExplosionBlockRadius(TntEntity tnt) {
        if (tnt == null) {
            return 8.0D;
        }

        return switch (getTntType(tnt)) {
            case A -> 20.0D;
            case B -> 40.0D;
            case B2 -> 24.0D;
            case NORMAL, C4, ICE_WAVE, EXPLOSIVE_WAVE -> 8.0D;
        };
    }
}