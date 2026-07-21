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
        totemParticlesToggleBtn, totemParticlesModeBtn,
        potionParticlesToggleBtn, potionParticlesModeBtn,
        weatherToggleBtn, weatherModeBtn,
        arrowsToggleBtn, arrowsModeBtn,
        fireworksToggleBtn, fireworksModeBtn,
        hologramsToggleBtn, hologramsModeBtn,
        namesToggleBtn, namesModeBtn,
        playersToggleBtn, playersModeBtn,
        handToggleBtn, handModeBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, BUTTON_GAP = 10, SPACING = 45;
    private static final int SCROLL_SPEED = 18, CLIP_TOP = 42, CLIP_BOTTOM = 55;

    private boolean changed = false;

    private int scrollOffset = 0, maxScroll = 0;

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
        totemParticlesToggleBtn = totemParticlesModeBtn = null;
        potionParticlesToggleBtn = potionParticlesModeBtn = null;
        weatherToggleBtn = weatherModeBtn = null;
        arrowsToggleBtn = arrowsModeBtn = null;
        fireworksToggleBtn = fireworksModeBtn = null;
        hologramsToggleBtn = hologramsModeBtn = null;
        namesToggleBtn = namesModeBtn = null;
        playersToggleBtn = playersModeBtn = null;
        handToggleBtn = handModeBtn = null;

        int rightX = width / 2 + 50;
        int modeX = rightX + BUTTON_WIDTH + BUTTON_GAP;
        int y = 60 - scrollOffset;

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            totemOverlayToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderTotemOverlayEnabled,
                config::setNoRenderTotemOverlay
            );

            totemOverlayModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderTotemOverlayEnabled,
                config::setNoRenderTotemOverlay,
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

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_particles")) {
            totemParticlesToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderTotemParticlesEnabled,
                config::setNoRenderTotemParticlesEnabled
            );

            totemParticlesModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderTotemParticlesEnabled,
                config::setNoRenderTotemParticlesEnabled,
                config::getNoRenderTotemParticles,
                config::setNoRenderTotemParticles,
                true
            );

            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_potion_particles")) {
            potionParticlesToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderPotionParticlesEnabled,
                config::setNoRenderPotionParticlesEnabled
            );

            potionParticlesModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderPotionParticlesEnabled,
                config::setNoRenderPotionParticlesEnabled,
                config::getNoRenderPotionParticles,
                config::setNoRenderPotionParticles,
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

        if (!LiteApiManager.isFeatureBlocked("no_render_arrows")) {
            arrowsToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderArrowsEnabled,
                config::setNoRenderArrowsEnabled
            );

            arrowsModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderArrowsEnabled,
                config::setNoRenderArrowsEnabled,
                config::getNoRenderArrows,
                config::setNoRenderArrows,
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

        if (!LiteApiManager.isFeatureBlocked("no_render_names")) {
            namesToggleBtn = createToggleButton(
                rightX,
                y,
                config::isNoRenderNamesEnabled,
                config::setNoRenderNamesEnabled
            );

            namesModeBtn = createModeButton(
                modeX,
                y,
                config::isNoRenderNamesEnabled,
                config::setNoRenderNamesEnabled,
                config::getNoRenderNames,
                config::setNoRenderNames,
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

        maxScroll = Math.max(0, y - (height - 100));

        updateAllButtons();
        updateScroll();
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

        if (totemParticlesToggleBtn != null) {
            updateToggleButton(totemParticlesToggleBtn, config.isNoRenderTotemParticlesEnabled());
            updateModeButton(totemParticlesModeBtn, config.getNoRenderTotemParticles());
        }

        if (potionParticlesToggleBtn != null) {
            updateToggleButton(potionParticlesToggleBtn, config.isNoRenderPotionParticlesEnabled());
            updateModeButton(potionParticlesModeBtn, config.getNoRenderPotionParticles());
        }

        if (weatherToggleBtn != null) {
            updateToggleButton(weatherToggleBtn, config.isNoRenderWeatherEnabled());
            updateModeButton(weatherModeBtn, config.getNoRenderWeather());
        }

        if (arrowsToggleBtn != null) {
            updateToggleButton(arrowsToggleBtn, config.isNoRenderArrowsEnabled());
            updateModeButton(arrowsModeBtn, config.getNoRenderArrows());
        }

        if (fireworksToggleBtn != null) {
            updateToggleButton(fireworksToggleBtn, config.isNoRenderFireworksEnabled());
            updateModeButton(fireworksModeBtn, config.getNoRenderFireworks());
        }

        if (namesToggleBtn != null) {
            updateToggleButton(namesToggleBtn, config.isNoRenderNamesEnabled());
            updateModeButton(namesModeBtn, config.getNoRenderNames());
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
        int y = 60 - scrollOffset;

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Overlay",
                "Hide or reduce the totem overlay.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fire_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fire Overlay",
                "Hide or reduce the fire overlay.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_particles")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Particles",
                "Hide or reduce the totem particles.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_potion_particles")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Potion Particles",
                "Hide or reduce the potion particles.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_weather")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Weather",
                "Hide rain, snow and water splashes.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_arrows")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Arrows",
                "Hide arrows on players.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fireworks")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fireworks",
                "Hide fireworks particles.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_names")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Names",
                "Hide players names.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_hand")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Hand",
                "Hide client player's hand.");
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_players")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Players",
                "Hide players.");
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int top = y;
        int bottom = y + 12 + textRenderer.fontHeight;

        if (bottom <= CLIP_TOP || top >= height - CLIP_BOTTOM) {
            return;
        }

        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x &&
                mouseX <= x + titleWidth &&
                mouseY >= y &&
                mouseY <= y + textRenderer.fontHeight;

        context.drawText(textRenderer, Text.literal(title), x, y, hovered ? ACCENT_COLOR : 0xFFFFFFFF, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    /// Скролл
    private void updateScroll() {
        int y = 60 - scrollOffset;

        if (totemOverlayToggleBtn != null) {
            totemOverlayToggleBtn.setY(y - 3);
            totemOverlayModeBtn.setY(y - 3);
            setVisible(totemOverlayToggleBtn, totemOverlayToggleBtn.getY());
            setVisible(totemOverlayModeBtn, totemOverlayModeBtn.getY());
            y += SPACING;
        }

        if (fireOverlayToggleBtn != null) {
            fireOverlayToggleBtn.setY(y - 3);
            fireOverlayModeBtn.setY(y - 3);
            setVisible(fireOverlayToggleBtn, fireOverlayToggleBtn.getY());
            setVisible(fireOverlayModeBtn, fireOverlayModeBtn.getY());
            y += SPACING;
        }

        if (totemParticlesToggleBtn != null) {
            totemParticlesToggleBtn.setY(y - 3);
            totemParticlesModeBtn.setY(y - 3);
            setVisible(totemParticlesToggleBtn, totemParticlesToggleBtn.getY());
            setVisible(totemParticlesModeBtn, totemParticlesModeBtn.getY());
            y += SPACING;
        }

        if (potionParticlesToggleBtn != null) {
            potionParticlesToggleBtn.setY(y - 3);
            potionParticlesModeBtn.setY(y - 3);
            setVisible(potionParticlesToggleBtn, potionParticlesToggleBtn.getY());
            setVisible(potionParticlesModeBtn, potionParticlesModeBtn.getY());
            y += SPACING;
        }

        if (weatherToggleBtn != null) {
            weatherToggleBtn.setY(y - 3);
            weatherModeBtn.setY(y - 3);
            setVisible(weatherToggleBtn, weatherToggleBtn.getY());
            setVisible(weatherModeBtn, weatherModeBtn.getY());
            y += SPACING;
        }

        if (arrowsToggleBtn != null) {
            arrowsToggleBtn.setY(y - 3);
            arrowsModeBtn.setY(y - 3);
            setVisible(arrowsToggleBtn, arrowsToggleBtn.getY());
            setVisible(arrowsModeBtn, arrowsModeBtn.getY());
            y += SPACING;
        }

        if (fireworksToggleBtn != null) {
            fireworksToggleBtn.setY(y - 3);
            fireworksModeBtn.setY(y - 3);
            setVisible(fireworksToggleBtn, fireworksToggleBtn.getY());
            setVisible(fireworksModeBtn, fireworksModeBtn.getY());
            y += SPACING;
        }

        if (hologramsToggleBtn != null) {
            hologramsToggleBtn.setY(y - 3);
            hologramsModeBtn.setY(y - 3);
            setVisible(hologramsToggleBtn, hologramsToggleBtn.getY());
            setVisible(hologramsModeBtn, hologramsModeBtn.getY());
            y += SPACING;
        }

        if (namesToggleBtn != null) {
            namesToggleBtn.setY(y - 3);
            namesModeBtn.setY(y - 3);
            setVisible(namesToggleBtn, namesToggleBtn.getY());
            setVisible(namesModeBtn, namesModeBtn.getY());
            y += SPACING;
        }

        if (handToggleBtn != null) {
            handToggleBtn.setY(y - 3);
            handModeBtn.setY(y - 3);
            setVisible(handToggleBtn, handToggleBtn.getY());
            setVisible(handModeBtn, handModeBtn.getY());
            y += SPACING;
        }

        if (playersToggleBtn != null) {
            playersToggleBtn.setY(y - 3);
            playersModeBtn.setY(y - 3);
            setVisible(playersToggleBtn, playersToggleBtn.getY());
            setVisible(playersModeBtn, playersModeBtn.getY());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int oldOffset = scrollOffset;

        scrollOffset -= (int) (amount * SCROLL_SPEED);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        if (oldOffset != scrollOffset) {
            updateScroll();
        }

        return true;
    }

    private void setVisible(ButtonWidget button, int y) {
        if (button == null) {
            return;
        }

        int top = button.getY();
        int bottom = top + button.getHeight();

        boolean visible = bottom > CLIP_TOP && top < height - CLIP_BOTTOM;

        button.visible = visible;
        button.active = visible;
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