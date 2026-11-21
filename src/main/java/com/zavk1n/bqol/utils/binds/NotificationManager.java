package com.zavk1n.bqol.utils.binds;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class NotificationManager {
    private NotificationManager() {}

    private static final Deque<Notification> ACTIVE_NOTIFICATIONS = new ArrayDeque<>();
    private static final Deque<Notification> NOTIFICATION_QUEUE = new ArrayDeque<>();
    private static final Map<String, Long> LAST_NOTIFICATION_TIMES = new HashMap<>();

    private static final long SPAM_DELAY_NANOS = 300_000_000L;

    private static final int MAX_ACTIVE = 4;

    private static final int NOTIFICATION_DURATION_MS = 1800;
    private static final int FADE_DURATION_MS = 400;
    private static final int SLIDE_DURATION_MS = 400;

    private static final long NOTIFICATION_DURATION_NANOS = NOTIFICATION_DURATION_MS * 1_000_000L;
    private static final long FADE_DURATION_NANOS = FADE_DURATION_MS * 1_000_000L;
    private static final long SLIDE_DURATION_NANOS = SLIDE_DURATION_MS * 1_000_000L;

    private static final int NOTIFICATION_WIDTH = 180;
    private static final int NOTIFICATION_HEIGHT = 30;

    private static final int PADDING = 10;
    private static final int SPACING = 8;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ENABLED_COLOR = 0xFFBEB5FF;
    private static final int DISABLED_COLOR = 0xFF9E5EFF;

    private static final class Notification {
        private final String id;
        private Text message;
        private long createdTime;

        private Notification(
            String id,
            Text message,
            long createdTime
        ) {
            this.id = id;
            this.message = message;
            this.createdTime = createdTime;
        }
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> renderNotifications(drawContext));

        initialized = true;
    }

    public static void showNotification(String id, Text message) {
        if (id == null || message == null) {
            return;
        }

        long now = System.nanoTime();

        Long lastTime = LAST_NOTIFICATION_TIMES.get(id);

        if (lastTime != null && now - lastTime < SPAM_DELAY_NANOS) {
            return;
        }

        LAST_NOTIFICATION_TIMES.put(id, now);

        for (Notification notification : ACTIVE_NOTIFICATIONS) {
            if (notification.id.equals(id)) {
                updateNotification(notification, message, now);
                return;
            }
        }

        for (Notification notification : NOTIFICATION_QUEUE) {
            if (notification.id.equals(id)) {
                notification.message = message;
                notification.createdTime = now;
                return;
            }
        }

        Notification notification = new Notification(id, message, now);

        if (ACTIVE_NOTIFICATIONS.size() < MAX_ACTIVE) {
            addActive(notification, now);
        } else {
            NOTIFICATION_QUEUE.addLast(notification);
        }

        if (LAST_NOTIFICATION_TIMES.size() > 64) {
            LAST_NOTIFICATION_TIMES.entrySet().removeIf(entry -> now - entry.getValue() > SPAM_DELAY_NANOS);
        }
    }

    private static void updateNotification(Notification notification, Text message, long now) {
        notification.message = message;
        notification.createdTime = now;
    }

    private static void addActive(Notification notification, long now) {
        notification.createdTime = now;
        ACTIVE_NOTIFICATIONS.addLast(notification);
    }

    private static void showFeatureNotification(String featureName, boolean enabled) {
        MutableText message = Text.empty();

        message.append(Text.literal(featureName).styled(style -> style.withColor(TEXT_COLOR)));
        message.append(Text.literal(" • ").styled(style -> style.withColor(TEXT_COLOR)));
        message.append(Text.literal(enabled ? "Enabled" : "Disabled").styled(style -> style.withColor(enabled ? ENABLED_COLOR : DISABLED_COLOR)));

        showNotification(featureName, message);
    }

    public static void showBetterMovementNotification(boolean enabled) {
        showFeatureNotification("Better Movement", enabled);
    }

    public static void showBetterRCTNotification(boolean enabled) {
        showFeatureNotification("Better RCT", enabled);
    }

    public static void showBetterSoundsNotification(boolean enabled) {
        showFeatureNotification("Better Sounds", enabled);
    }

    public static void showBetterInteractNotification(boolean enabled) {
        showFeatureNotification("Better Interact", enabled);
    }

    public static void showBetterGammaNotification(boolean enabled) {
        showFeatureNotification("Better Gamma", enabled);
    }

    public static void showBetterHurtCamNotification(boolean enabled) {
        showFeatureNotification("Better Hurt Cam", enabled);
    }

    public static void showBetterThirdPersonNotification(boolean enabled) {
        showFeatureNotification("Better Third Person", enabled);
    }

    public static void showBetterTntNotification(boolean enabled) {
        showFeatureNotification("Better Tnt", enabled);
    }

    public static void showBetterChatNotification(boolean enabled) {
        showFeatureNotification("Better Chat", enabled);
    }

    public static void showBetterTPNotification(boolean enabled) {
        showFeatureNotification("Better TP", enabled);
    }

    public static void showBetterRRLSNotification(boolean enabled) {
        showFeatureNotification("Better RRLS", enabled);
    }

    public static void showBetterSkyNotification(boolean enabled) {
        showFeatureNotification("Better Sky", enabled);
    }

    public static void showBetterPrefixesNotification(boolean enabled) {
        showFeatureNotification("Better Prefixes", enabled);
    }

    public static void showBetterHitColorNotification(boolean enabled) {
        showFeatureNotification("Better Hit Color", enabled);
    }

    public static void showBetterHitboxesNotification(boolean enabled) {
        showFeatureNotification("Better Hitboxes", enabled);
    }

    public static void showBetterHologramsNotification(boolean enabled) {
        showFeatureNotification("Better Holograms", enabled);
    }

    public static void showBetterZoomNotification(boolean enabled) {
        showFeatureNotification("Better Zoom", enabled);
    }

    public static void showBetterTotemsNotification(boolean enabled) {
        showFeatureNotification("Better Totems", enabled);
    }

    public static void showBetterSpheresNotification(boolean enabled) {
        showFeatureNotification("Better Spheres", enabled);
    }

    public static void showBetterParticlesNotification(boolean enabled) {
        showFeatureNotification("Better Particles", enabled);
    }

    public static void showBetterLiquidNotification(boolean enabled) {
        showFeatureNotification("Better Liquid", enabled);
    }

    public static void showBetterFogNotification(boolean enabled) {
        showFeatureNotification("Better Fog", enabled);
    }

    public static void showBetterHealthNotification(boolean enabled) {
        showFeatureNotification("Better Health", enabled);
    }

    public static void showNoRenderNotification(boolean enabled) {
        showFeatureNotification("No Render", enabled);
    }

    public static void showBetterMineNotification(boolean enabled) {
        showFeatureNotification("Better Mine", enabled);
    }

    public static void showBetterAntiInvisibleNotification(boolean enabled) {
        showFeatureNotification("Better Anti Invisible ", enabled);
    }

    public static void showBetterHitCounterNotification(boolean enabled) {
        showFeatureNotification("Better Hit Counter", enabled);
    }

    private static void renderNotifications(DrawContext context) {
        if (ACTIVE_NOTIFICATIONS.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();

        long now = System.nanoTime();

        Iterator<Notification> iterator = ACTIVE_NOTIFICATIONS.iterator();

        int expiredCount = 0;
        int index = 0;

        while (iterator.hasNext()) {
            Notification notification = iterator.next();

            long age = now - notification.createdTime;

            if (age >= NOTIFICATION_DURATION_NANOS) {
                iterator.remove();
                expiredCount++;
                continue;
            }

            int alpha = calculateAlpha(age);

            float slide = calculateSlide(age);

            int x = screenWidth - NOTIFICATION_WIDTH - PADDING + (int) (NOTIFICATION_WIDTH * (1F - slide));
            int y = PADDING + index * (NOTIFICATION_HEIGHT + SPACING);

            drawNotification(context, textRenderer, notification, x, y, alpha);

            index++;
        }

        while (expiredCount > 0 && !NOTIFICATION_QUEUE.isEmpty()) {
            Notification next = NOTIFICATION_QUEUE.pollFirst();

            addActive(next, now);

            expiredCount--;
        }
    }

    private static int calculateAlpha(long age) {
        if (age < FADE_DURATION_NANOS) {
            return MathHelper.clamp((int) (255F * age / FADE_DURATION_NANOS), 0, 255);
        }

        long fadeOutStart = NOTIFICATION_DURATION_NANOS - FADE_DURATION_NANOS;

        if (age > fadeOutStart) {
            float progress = (float) (age - fadeOutStart) / FADE_DURATION_NANOS;

            return MathHelper.clamp((int) (255F * (1F - progress)), 0, 255);
        }

        return 255;
    }

    private static float calculateSlide(long age) {
        if (age < SLIDE_DURATION_NANOS) {
            return age / (float) SLIDE_DURATION_NANOS;
        }

        long slideOutStart = NOTIFICATION_DURATION_NANOS - SLIDE_DURATION_NANOS;

        if (age > slideOutStart) {
            float progress = (float) (age - slideOutStart) / SLIDE_DURATION_NANOS;

            return 1F - progress;
        }

        return 1F;
    }

    private static void drawNotification(DrawContext context, TextRenderer textRenderer, Notification notification, int x, int y, int alpha) {
        int background = withAlpha(0xFF000000, (alpha * 166) / 255);
        int border = withAlpha(0xFFC7C0FA, alpha);

        context.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, background);

        context.drawStrokedRectangle(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, border);

        int textWidth = textRenderer.getWidth(notification.message);

        int textX = x + (NOTIFICATION_WIDTH - textWidth) / 2;
        int textY = Math.round(y + (NOTIFICATION_HEIGHT - textRenderer.fontHeight) / 2F);

        context.drawText(textRenderer, notification.message, textX, textY, withAlpha(TEXT_COLOR, alpha), true);
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}