package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.config.BQoLConfig.RenderMode;
import com.zavk1n.bqol.features.NoRender;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NoRenderConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget totemOverlayToggleBtn, totemOverlayModeBtn,
        fireOverlayToggleBtn, fireOverlayModeBtn,
        weatherToggleBtn, weatherModeBtn,
        fireworksToggleBtn, fireworksModeBtn,
        playersToggleBtn, playersModeBtn,
        handToggleBtn, handModeBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, BUTTON_GAP = 10, SPACING = 45;
    private boolean changed = false;

    /// Конструктор
    public NoRenderConfigScreen(Screen parent) {
        super(Text.literal("No Render Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("no_render")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        totemOverlayToggleBtn = totemOverlayModeBtn = null;
        fireOverlayToggleBtn = fireOverlayModeBtn = null;
        weatherToggleBtn = weatherModeBtn = null;
        fireworksToggleBtn = fireworksModeBtn = null;
        playersToggleBtn = playersModeBtn = null;

        int rightX = width / 2 + 50;
        int modeX = rightX + BUTTON_WIDTH + BUTTON_GAP;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            totemOverlayToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderTotemOverlayEnabled,
                config::setNoRenderTotemOverlayEnabled
            );

            totemOverlayModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderTotemOverlayEnabled,
                config::setNoRenderTotemOverlayEnabled,
                config::getNoRenderTotemOverlay,
                config::setNoRenderTotemOverlay,
                true
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fire_overlay")) {
            fireOverlayToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderFireOverlayEnabled,
                config::setNoRenderFireOverlayEnabled
            );

            fireOverlayModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderFireOverlayEnabled,
                config::setNoRenderFireOverlayEnabled,
                config::getNoRenderFireOverlay,
                config::setNoRenderFireOverlay,
                true
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_weather")) {
            weatherToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderWeatherEnabled,
                config::setNoRenderWeatherEnabled
            );

            weatherModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderWeatherEnabled,
                config::setNoRenderWeatherEnabled,
                config::getNoRenderWeather,
                config::setNoRenderWeather,
                false
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fireworks")) {
            fireworksToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderFireworksEnabled,
                config::setNoRenderFireworksEnabled
            );

            fireworksModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderFireworksEnabled,
                config::setNoRenderFireworksEnabled,
                config::getNoRenderFireworks,
                config::setNoRenderFireworks,
                false
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_hand")) {
            handToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderHandEnabled,
                config::setNoRenderHandEnabled
            );

            handModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderHandEnabled,
                config::setNoRenderHandEnabled,
                config::getNoRenderHand,
                config::setNoRenderHand,
                false
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_players")) {
            playersToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderPlayersEnabled,
                config::setNoRenderPlayersEnabled
            );

            playersModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderPlayersEnabled,
                config::setNoRenderPlayersEnabled,
                config::getNoRenderPlayers,
                config::setNoRenderPlayers,
                false
            );
        }

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createToggleButton(int x, int y, Supplier<Boolean> enabledGetter, Consumer<Boolean> enabledSetter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(""),
                b -> {
                    enabledSetter.accept(!enabledGetter.get());

                    changed = true;
                    updateAllButtons();
                    save();
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(button);
        return button;
    }

    private ButtonWidget createModeButton(int x, int y, Supplier<Boolean> enabledGetter, Consumer<Boolean> enabledSetter, Supplier<RenderMode> getter, Consumer<RenderMode> setter, boolean allowSmall) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(""),
                b -> {

                    RenderMode current = getter.get();
                    RenderMode next;

                    if (allowSmall) {
                        next = switch (current) {
                            case FULL -> RenderMode.SMALL;
                            case SMALL -> RenderMode.NO_RENDER;
                            case NO_RENDER -> RenderMode.FULL;
                        };
                    } else {
                        next = switch (current) {
                            case FULL -> RenderMode.NO_RENDER;
                            case NO_RENDER -> RenderMode.FULL;
                            case SMALL -> RenderMode.FULL;
                        };
                    }

                    setter.accept(next);

                    changed = true;
                    updateAllButtons();
                    save();
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(button);
        return button;
    }

    /// Обновление состояния кнопок
    private void updateAllButtons() {
        if (totemOverlayToggleBtn != null) {
            updateToggleButton(totemOverlayToggleBtn, config.isNoRenderTotemOverlayEnabled());
            updateModeButton(totemOverlayModeBtn, config.getNoRenderTotemOverlay());
        }

        if (fireOverlayToggleBtn != null) {
            updateToggleButton(fireOverlayToggleBtn, config.isNoRenderFireOverlayEnabled());
            updateModeButton(fireOverlayModeBtn, config.getNoRenderFireOverlay());
        }

        if (weatherToggleBtn != null) {
            updateToggleButton(weatherToggleBtn, config.isNoRenderWeatherEnabled());
            updateModeButton(weatherModeBtn, config.getNoRenderWeather());
        }

        if (fireworksToggleBtn != null) {
            updateToggleButton(fireworksToggleBtn, config.isNoRenderFireworksEnabled());
            updateModeButton(fireworksModeBtn, config.getNoRenderFireworks());
        }

        if (handToggleBtn != null) {
            updateToggleButton(handToggleBtn, config.isNoRenderHandEnabled());
            updateModeButton(handModeBtn, config.getNoRenderHand());
        }

        if (playersToggleBtn != null) {
            updateToggleButton(playersToggleBtn, config.isNoRenderPlayersEnabled());
            updateModeButton(playersModeBtn, config.getNoRenderPlayers());
        }
    }

    private void updateToggleButton(ButtonWidget button, boolean enabled) {
        button.setMessage(
            Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(style ->
                    style.withColor(
                        enabled
                            ? ACCENT_COLOR
                            : 0xFFFFFFFF
                    )
                )
        );
    }

    private void updateModeButton(ButtonWidget button, RenderMode mode) {
        int color = switch (mode) {
            case FULL -> 0xFFFFFFFF;
            case SMALL -> ACCENT_COLOR;
            case NO_RENDER -> 0xFF6565DB;
        };

        String text = switch (mode) {
            case FULL -> "Full";
            case SMALL -> "Small";
            case NO_RENDER -> "No Render";
        };

        button.setMessage(
            Text.literal(text)
                .styled(style -> style.withColor(color))
        );
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Overlay",
                "Hide or reduce the totem animation.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fire_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fire Overlay",
                "Hide or reduce the fire overlay.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_weather")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Weather",
                "Hide rain, snow and water splashes.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fireworks")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fireworks",
                "Hide fireworks particles.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_hand")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Hand",
                "Hide hand.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_players")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Players",
                "Hide players.");
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x &&
                mouseX <= x + titleWidth &&
                mouseY >= y &&
                mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();

        NoRender.refreshBlockedStatus();
        NoRender.reloadFromConfig();

        changed = false;
    }

    @Override
    public void close() {
        if (changed) {
            save();
        }

        if (client != null) {
            client.setScreen(parent);
        }
    }
}