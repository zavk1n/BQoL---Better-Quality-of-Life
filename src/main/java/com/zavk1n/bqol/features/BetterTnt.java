package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class BetterTnt {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterTnt() {}

    private static BetterTnt instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    public enum TntType {
        NORMAL,
        A,
        B,
        B2,
        C4,
        ICE_WAVE,
        EXPLOSIVE_WAVE
    }

    public static class AlertInfo {
        private final TntType type;
        private final double seconds;
        private final int x;
        private final int y;
        private final int z;

        public AlertInfo(
            TntType type,
            double seconds,
            int x,
            int y,
            int z
        ) {
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
            return String.format(Locale.ROOT, "%.2f", seconds);
        }

        public String getText() {
            String text = "Время до взрыва " + getAlertTypeName(type) + " - " + getFuseText();

            if (BetterTnt.isAlertShowXYZ()) {
                text += " (X = " + x + ", Y = " + y + ", Z = " + z + ")";
            }

            return text;
        }
    }

    private static class TrackedTnt {
        private final UUID uuid;

        private TntType type;

        private int x;
        private int y;
        private int z;

        private boolean wasDetected;
        private boolean exploding;
        private int lastServerFuse;
        private long lastSyncNanos;
        private long endTimeNanos;

        private TrackedTnt(TntEntity entity, TntType type, long endTimeNanos, int fuse) {
            this.uuid = entity.getUuid();
            this.type = type;
            this.endTimeNanos = endTimeNanos;
            this.lastServerFuse = fuse;
            this.lastSyncNanos = System.nanoTime();
            this.wasDetected = true;

            updatePosition(entity);
        }

        private void updatePosition(TntEntity entity) {
            if (entity == null) {
                return;
            }

            this.x = entity.getBlockPos().getX();
            this.y = entity.getBlockPos().getY();
            this.z = entity.getBlockPos().getZ();
        }

        private double getSeconds() {
            return Math.max(0.0D, (endTimeNanos - System.nanoTime()) / 1_000_000_000.0D);
        }
    }

    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean timer;
        boolean alert;
        boolean showXYZ;
    }

    /// Мапы
    private final Map<UUID, TntType> tntTypes = new HashMap<>();
    private final Map<UUID, TrackedTnt> trackedAlerts = new HashMap<>();

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterTnt();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Tnt initialized");
        }
    }

    public static BetterTnt getInstance() {
        if (instance == null) {
            initialize();
        }

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

    public static boolean isTimerEnabled() {
        return instance != null && instance.isTimerEnabledInternal();
    }

    public static void setTimerEnabled(boolean enabled) {
        if (instance != null) instance.setTimerEnabledInternal(enabled);
    }

    public static boolean isAlertEnabled() {
        return instance != null && instance.isAlertEnabledInternal();
    }

    public static void setAlertEnabled(boolean enabled) {
        if (instance != null) instance.setAlertEnabledInternal(enabled);
    }

    public static int getAlertPosition() {
        if (instance == null) {
            initialize();
        }

        return instance.config.getBetterTntAlertPosition();
    }

    public static boolean isAlertShowXYZ() {
        return instance != null && instance.config.isBetterTntAlertShowXYZ();
    }

    public static void setAlertShowXYZ(boolean enabled) {
        if (instance != null) {
            instance.config.setBetterTntAlertShowXYZ(enabled);
        }
    }

    /// Внутренние динамические методы
    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_tnt");
        blocked.timer = LiteApiManager.isFeatureBlocked("better_tnt_timer");
        blocked.alert = LiteApiManager.isFeatureBlocked("better_tnt_alert");
        blocked.showXYZ = LiteApiManager.isFeatureBlocked("better_tnt_show_xyz");
    }

    private boolean isEnabledInternal() {
        return config.isBetterTntEnabled()
            && !blocked.main;
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

    /// Каждый тик
    public static void tickTracking() {
        if (!isEnabled()) {
            return;
        }

        BetterTnt self = getInstance();
        MinecraftClient mc = self.mc();

        if (mc.world == null || mc.player == null) {
            self.trackedAlerts.clear();
            return;
        }

        Vec3d playerPos = mc.player.getPos();

        double radius = 32.0D;

        Box searchBox = new Box(
            playerPos.x - radius,
            playerPos.y - radius,
            playerPos.z - radius,

            playerPos.x + radius,
            playerPos.y + radius,
            playerPos.z + radius
        );

        List<TntEntity> nearby = mc.world.getEntitiesByClass(TntEntity.class, searchBox, BetterTnt::isValidTnt);

        for (TntEntity tnt : nearby) {
            self.trackTnt(tnt);
        }

        self.trackedAlerts.entrySet().removeIf(entry -> entry.getValue().getSeconds() <= 0.0D);
    }

    /// Регистрация ТнТ
    public static void registerTnt(TntEntity entity) {
        if (entity == null) {
            return;
        }

        BetterTnt self = getInstance();

        TntType type = detectTypeFromText(entity.getName().getString());

        self.tntTypes.put(entity.getUuid(), type);
    }

    public static TntType getTntType(TntEntity entity) {
        if (entity == null) {
            return TntType.NORMAL;
        }

        BetterTnt self = getInstance();

        TntType detectedType = detectTypeFromText(entity.getName().getString());

        self.tntTypes.put(entity.getUuid(), detectedType);

        return detectedType;
    }

    /// Получение информации о Тнт
    private static TntType detectTypeFromText(String name) {
        if (name == null) {
            return TntType.NORMAL;
        }

        return detectType(name);
    }

    private static TntType detectType(String name) {
        if (name == null) {
            return TntType.NORMAL;
        }

        if ((name.contains("Динамит B2") || name.contains("Динамит Б2"))) {
            return TntType.B2;
        }

        if ((name.contains("Динамит B") || name.contains("Динамит В"))) {
            return TntType.B;
        }

        if ((name.contains("Динамит A") || name.contains("Динамит А"))) {
            return TntType.A;
        }

        if ((name.contains("C4 ВзРыВчАтКа") || name.contains("С4 ВзРыВчАтКа"))) {
            return TntType.C4;
        }

        if (name.contains("Ледяная волна")) {
            return TntType.ICE_WAVE;
        }

        if (name.contains("Разрывная волна")) {
            return TntType.EXPLOSIVE_WAVE;
        }

        if (name.contains("Динамит")) {
            return TntType.NORMAL;
        }

        return TntType.NORMAL;
    }

    /// Таймер
    public static double getFuseSeconds(TntEntity tnt, float tickDelta) {
        if (tnt == null) {
            return 0.0D;
        }

        return Math.max(0.0D, (tnt.getFuse() - tickDelta) / 20.0D);
    }

    private double getTrackedFuseSeconds(TntEntity entity) {
        if (entity == null) {
            return 0.0D;
        }

        TrackedTnt tracked = trackedAlerts.get(entity.getUuid());

        if (tracked == null) {
            return 0.0D;
        }

        return tracked.getSeconds();
    }

    public static String getFuseText(TntEntity tnt, float tickDelta) {
        return String.format(Locale.ROOT, "%.2f", getFuseSeconds(tnt, tickDelta));
    }

    /// Цвета
    public static int getTimerColor(TntEntity tnt, float tickDelta) {
        return getTimerColor(getTntType(tnt), getFuseSeconds(tnt, tickDelta));
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

        if (type == TntType.A
            || type == TntType.B
            || type == TntType.B2) {
            if (seconds <= 2.00D) {
                return 0xFFFF5555;
            }

            if (seconds <= 5.00D) {
                return 0xFFFFFF55;
            }

            if (seconds <= 7.00D) {
                return 0xFF55FF55;
            }

            return 0xFF55FF55;
        }

        /*
         * C4 / ICE / EXPLOSIVE / NORMAL:
         *
         * 0.00 - 1.00 RED
         * 1.01 - 3.00 YELLOW
         * 3.01 - 4.00 LIGHT GREEN
         * 4.01 - 5.00 GREEN
         */

        if (seconds <= 1.00D) {
            return 0xFFFF5555;
        }

        if (seconds <= 3.00D) {
            return 0xFFFFFF55;
        }

        if (seconds <= 4.00D) {
            return 0xFF55FF55;
        }

        return 0xFF55FF55;
    }

    public static void renderTimer(MatrixStack matrices, TntEntity tnt, Camera camera, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        if (!isTimerEnabled()
                || tnt == null
                || !tnt.isAlive()) {
            return;
        }

        BetterTnt self = getInstance();

        double seconds = self.getTrackedFuseSeconds(tnt);

        if (seconds <= 0.0D) {
            return;
        }

        String text = String.format(Locale.ROOT, "%.2f", seconds);
        TntType type = getTntType(tnt);

        int color = getTimerColor(type, seconds);

        matrices.push();

        double timerHeight = self.isHolyWorldServer() ? tnt.getHeight() + 0.8D : tnt.getHeight() + 0.5D;

        matrices.translate(0.0D, timerHeight, 0.0D);
        matrices.multiply(camera.getRotation());

        float scale = 0.025F;

        matrices.scale(-scale, -scale, scale);

        float width = textRenderer.getWidth(text);

        textRenderer.draw(text, -width / 2.0F, 0.0F, color, false,
            matrices.peek().getPositionMatrix(),
            vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH, 0x4D000000, light
        );

        matrices.pop();
    }

    /// Отслеживание
    private void trackTnt(TntEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return;
        }

        int fuse = entity.getFuse();

        if (fuse <= 0) {
            trackedAlerts.remove(entity.getUuid());
            return;
        }

        UUID uuid = entity.getUuid();
        TntType type = getTntType(entity);

        TrackedTnt tracked = trackedAlerts.get(uuid);

        long now = System.nanoTime();

        if (tracked == null) {
            long endTimeNanos = now + fuse * 50_000_000L;

            tracked = new TrackedTnt(entity, type, endTimeNanos, fuse);

            trackedAlerts.put(uuid, tracked);
            return;
        }

        tracked.type = type;
        tracked.updatePosition(entity);
        tracked.lastServerFuse = fuse;
        tracked.lastSyncNanos = now;
    }

    /// Alert
    public static List<AlertInfo> getAlertTnts(float tickDelta) {
        if (!isAlertEnabled()) {
            return Collections.emptyList();
        }

        BetterTnt self = getInstance();
        MinecraftClient mc = self.mc();

        if (mc.world == null || mc.player == null) {
            return Collections.emptyList();
        }

        Vec3d playerPos = mc.player.getPos();

        self.trackedAlerts.entrySet().removeIf(entry -> {TrackedTnt tracked = entry.getValue();
            return tracked.getSeconds() <= 0.0D;
        });

        List<AlertInfo> result = new ArrayList<>(self.trackedAlerts.size());

        for (TrackedTnt tracked : self.trackedAlerts.values()) {
            double seconds = tracked.getSeconds();

            if (seconds <= 0.0D) {
                continue;
            }

            result.add(new AlertInfo(tracked.type, seconds, tracked.x, tracked.y, tracked.z));
        }

        result.sort(Comparator.comparingDouble(alert -> {
                double dx = alert.getX() + 0.5D - playerPos.x;
                double dy = alert.getY() + 0.5D - playerPos.y;
                double dz = alert.getZ() + 0.5D - playerPos.z;

                return dx * dx + dy * dy + dz * dz;
            })
        );

        return result;
    }

    private static boolean isValidTnt(TntEntity entity) {
        return entity != null && entity.isAlive() && entity.getFuse() >= 0;
    }

    public static String getAlertTypeName(TntType type) {
        switch (type) {
            case A: return "Динамита A";

            case B: return "Динамита B";

            case B2: return "Динамита Б2";

            case C4: return "C4 Взрывчатки";

            case ICE_WAVE: return "Ледяной волны";

            case EXPLOSIVE_WAVE: return "Разрывной волны";

            case NORMAL:

            default: return "Обычного динамита";
        }
    }

    /// Проверка сервера
    private boolean isHolyWorldServer() {
        MinecraftClient mc = mc();

        ServerInfo server = mc.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address.toLowerCase(Locale.ROOT).contains("holyworld");
    }
}