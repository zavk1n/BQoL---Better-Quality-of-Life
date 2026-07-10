package com.zavk1n.bqol.utils;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NotificationManager {

    private static final Map<String, Notification> notifications = new LinkedHashMap<>();

    private static final class Notification {
        private final Text message;
        private final long createdTime;

        private Notification(Text message, long createdTime) {
            this.message = message;
            this.createdTime = createdTime;
        }
    }

    /// Константы
    private static final int NOTIFICATION_DURATION_MS = 1500;
    private static final int FADE_DURATION_MS = 300;
    private static final int SLIDE_DURATION_MS = 200;

    private static final int NOTIFICATION_WIDTH = 160;
    private static final int NOTIFICATION_HEIGHT = 20;
    private static final int PADDING = 8;
    private static final int SPACING = 4;

    private static final int BACKGROUND_COLOR = 0xFF1E1F29;
    private static final int BACKGROUND_HIGHLIGHT = 0xFF2B2D3A;
    private static final int ACCENT_COLOR = 0xFF7C5CFF;
    private static final int TEXT_COLOR = 0xFFF5F5F5;
    private static final int FEATURE_COLOR = 0xFF8B5CF6;

    private static boolean initialized;

    private NotificationManager() { }

    /// Публичные статические методы
    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
            renderNotifications(drawContext)
        );
    }

    /// Показ уведомления
    public static void showNotification(String id, Text message) {
        Notification existing = notifications.get(id);

        if (existing == null) {
            notifications.put(id, new Notification(message, System.currentTimeMillis()));
            return;
        }

        notifications.put(id, new Notification(message, System.currentTimeMillis()));
    }

    /// Рендер
    private static void renderNotifications(DrawContext context) {
        if (notifications.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, Notification>> iterator = notifications.entrySet().iterator();

        int index = 0;

        while (iterator.hasNext()) {
            Map.Entry<String, Notification> entry = iterator.next();
            Notification notification = entry.getValue();

            long age = currentTime - notification.createdTime;

            if (age >= NOTIFICATION_DURATION_MS) {
                iterator.remove();
                continue;
            }

            int alpha;

            if (age < FADE_DURATION_MS) {
                alpha = MathHelper.clamp(
                    (int) (255F * age / FADE_DURATION_MS),
                    0,
                    255
                );
            } else if (age > NOTIFICATION_DURATION_MS - FADE_DURATION_MS) {
                float t = (float) (
                    age - (NOTIFICATION_DURATION_MS - FADE_DURATION_MS)
                ) / FADE_DURATION_MS;

                alpha = MathHelper.clamp(
                    (int) (255F * (1F - t)),
                    0,
                    255
                );
            } else {
                alpha = 255;
            }

            float slideProgress;

            if (age < SLIDE_DURATION_MS) {
                slideProgress = (float) age / SLIDE_DURATION_MS;
            } else if (age > NOTIFICATION_DURATION_MS - SLIDE_DURATION_MS) {
                float t = (float) (
                    age - (NOTIFICATION_DURATION_MS - SLIDE_DURATION_MS)
                ) / SLIDE_DURATION_MS;

                slideProgress = 1F - t;
            } else {
                slideProgress = 1F;
            }

            int slideOffset = (int) (
                NOTIFICATION_WIDTH * (1F - slideProgress)
            );

            int x = screenWidth - NOTIFICATION_WIDTH - PADDING + slideOffset;
            int y = PADDING + index * (NOTIFICATION_HEIGHT + SPACING);

            drawNotification(context, textRenderer, notification, x, y, alpha);

            index++;
        }
    }

    private static void drawNotification(DrawContext context, TextRenderer textRenderer, Notification notification, int x, int y, int alpha) {
        context.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, withAlpha(BACKGROUND_COLOR, alpha));
        context.fill(x, y, x + 3, y + NOTIFICATION_HEIGHT, withAlpha(ACCENT_COLOR, alpha));

        context.fillGradient(x, y, x + NOTIFICATION_WIDTH, y + 2, withAlpha(ACCENT_COLOR, alpha), withAlpha(BACKGROUND_HIGHLIGHT, alpha));
        context.fillGradient(x, y + NOTIFICATION_HEIGHT - 2, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, 0x00000000, withAlpha(0xFF000000, alpha / 2));

        int textX = x + (NOTIFICATION_WIDTH - textRenderer.getWidth(notification.message)) / 2;
        int textY = y + (NOTIFICATION_HEIGHT - textRenderer.fontHeight) / 2;

        context.drawText(textRenderer, notification.message, textX, textY, withAlpha(TEXT_COLOR, alpha), true);
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    /// BetterSprint
    public static void showBetterSprintNotification(boolean enabled) {
        String statusText = enabled ? "Enabled" : "Disabled";
        Formatting statusColor = enabled
            ? Formatting.GREEN
            : Formatting.RED;

        Text feature = Text.literal("BetterSprint")
            .styled(style -> style.withColor(FEATURE_COLOR));

        Text status = Text.literal(statusText)
            .styled(style -> style.withColor(statusColor));

        showNotification(
            "bettersprint",
            feature.copy().append(" ").append(status)
        );
    }
}