package com.zavk1n.bqol.utils;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public final class NotificationManager {

    private static final LinkedList<Notification> activeNotifications = new LinkedList<>();
    private static final Queue<Notification> notificationQueue = new LinkedList<>();

    private static final java.util.Map<String, Long> lastNotificationTimes = new java.util.HashMap<>();

    private static final long SPAM_DELAY = 300L;
    private static final int MAX_ACTIVE = 4;

    private static final class Notification {
        private final String id;
        private Text message;
        private long createdTime;
        private int y;

        private Notification(
            String id,
            Text message,
            long createdTime,
            int y
        ) {
            this.id = id;
            this.message = message;
            this.createdTime = createdTime;
            this.y = y;
        }
    }

    /// Константы
    private static final int NOTIFICATION_DURATION_MS = 1800;
    private static final int FADE_DURATION_MS = 400;
    private static final int SLIDE_DURATION_MS = 400;

    private static final int NOTIFICATION_WIDTH = 180;
    private static final int NOTIFICATION_HEIGHT = 30;

    private static final int PADDING = 10;
    private static final int SPACING = 8;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ENABLED_COLOR = 0xFFBEB5FF;
    private static final int DISABLED_COLOR = 0xFF9E5EFF;

    private static boolean initialized;

    private NotificationManager() { }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
            renderNotifications(drawContext)
        );
    }

    /// Отображение
    public static void showNotification(String id, Text message) {
        long now = System.currentTimeMillis();

        Long last = lastNotificationTimes.get(id);

        if (last != null && now - last < SPAM_DELAY) {
            return;
        }

        lastNotificationTimes.put(id, now);

        for (Notification notification : activeNotifications) {
            if (notification.id.equals(id)) {
                notification.message = message;
                notification.createdTime = now;

                return;
            }
        }

        for (Notification notification : notificationQueue) {
            if (notification.id.equals(id)) {
                notification.message = message;
                notification.createdTime = now;

                return;
            }
        }

        Notification notification = new Notification(
            id,
            message,
            now,
            0
        );

        if (activeNotifications.size() < MAX_ACTIVE) {
            addActive(notification);
        } else {
            notificationQueue.offer(notification);
        }
    }

    private static void addActive(Notification notification) {
        notification.y = PADDING + activeNotifications.size() * (NOTIFICATION_HEIGHT + SPACING);

        activeNotifications.add(notification);
    }

    private static void showFeatureNotification(String featureName, boolean enabled) {
        MutableText message = Text.empty();

        message.append(
            Text.literal(featureName)
                .styled(style ->
                    style.withColor(0xFFFFFFFF)
                )
        );

        message.append(
            Text.literal(" • ")
                .styled(style ->
                    style.withColor(0xFFFFFFFF)
                )
        );

        message.append(
            Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(style ->
                    style.withColor(
                        enabled
                            ? ENABLED_COLOR
                            : DISABLED_COLOR
                    )
                )
        );

        showNotification(featureName, message);
    }

    /// Функции
    public static void showBetterSprintNotification(boolean enabled) {
        showFeatureNotification("Better Sprint", enabled);
    }


    public static void showBetterSoundsNotification(boolean enabled) {
        showFeatureNotification("Better Sounds", enabled);
    }


    public static void showBetterSpheresNotification(boolean enabled) {
        showFeatureNotification("Better Spheres", enabled);
    }


    public static void showShulkerParticlesNotification(boolean enabled) {
        showFeatureNotification("Shulker Particles", enabled);
    }


    public static void showCustomFogNotification(boolean enabled) {
        showFeatureNotification("Custom Fog", enabled);
    }


    public static void showCustomHealthNotification(boolean enabled) {
        showFeatureNotification("Custom Health", enabled);
    }

    /// Рендер
    private static void renderNotifications(DrawContext context) {
        if (activeNotifications.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        long currentTime = System.currentTimeMillis();

        Iterator<Notification> iterator = activeNotifications.iterator();

        boolean changed = false;

        while (iterator.hasNext()) {

            Notification notification = iterator.next();

            long age = currentTime - notification.createdTime;

            if (age >= NOTIFICATION_DURATION_MS) {
                iterator.remove();

                changed = true;

                if (!notificationQueue.isEmpty()) {
                    addActive(notificationQueue.poll());
                }

                continue;
            }

            int alpha;

            if (age < FADE_DURATION_MS) {
                alpha = MathHelper.clamp(
                    (int)(255F * age / FADE_DURATION_MS),
                    0,
                    255
                );

            } else if (age > NOTIFICATION_DURATION_MS - FADE_DURATION_MS) {
                float t = (float)(age - (NOTIFICATION_DURATION_MS - FADE_DURATION_MS)) / FADE_DURATION_MS;

                alpha = MathHelper.clamp(
                    (int)(255F * (1F - t)),
                    0,
                    255
                );
            } else {
                alpha = 255;
            }

            float slide;

            if (age < SLIDE_DURATION_MS) {
                slide = age / (float)SLIDE_DURATION_MS;
            } else if (age > NOTIFICATION_DURATION_MS - SLIDE_DURATION_MS) {
                float t = (float)(age - (NOTIFICATION_DURATION_MS - SLIDE_DURATION_MS)) / SLIDE_DURATION_MS;

                slide = 1F - t;
            } else {
                slide = 1F;
            }

            int x = screenWidth - NOTIFICATION_WIDTH - PADDING + (int)(NOTIFICATION_WIDTH * (1F - slide));

            drawNotification(context, textRenderer, notification, x, notification.y, alpha);
        }

        if (changed) {
            int y = PADDING;

            for (Notification notification : activeNotifications) {
                notification.y = y;

                y += NOTIFICATION_HEIGHT + SPACING;
            }
        }
    }

    private static void drawNotification(DrawContext context, TextRenderer textRenderer, Notification notification, int x, int y, int alpha) {
        int background = withAlpha(0xFF000000, (alpha * 166) / 255);
        int border = withAlpha(0xFFC7C0FA, alpha);

        context.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, background);

        context.drawBorder(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, border);

        int textWidth = textRenderer.getWidth(notification.message);

        int textX = x + (NOTIFICATION_WIDTH - textWidth) / 2;
        int textY = Math.round(y + (NOTIFICATION_HEIGHT - textRenderer.fontHeight) / 2f);


        context.drawText(textRenderer, notification.message, textX, textY, withAlpha(TEXT_COLOR, alpha), true);
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}