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
        explosionToggleBtn, explosionModeBtn,
        smokeToggleBtn, smokeModeBtn,
        bubblesToggleBtn, bubblesModeBtn,
        weatherToggleBtn, weatherModeBtn,
        arrowsToggleBtn, arrowsModeBtn,
        fireworksToggleBtn, fireworksModeBtn,
        namesToggleBtn, namesModeBtn,
        playersToggleBtn, playersModeBtn,
        handToggleBtn, handModeBtn;

    private ButtonWidget saveButton;

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
        resetScroll();
        rebuildUI();
    }

    /// Ядро
    private void rebuildUI() {
        clearChildren();

        totemOverlayToggleBtn = totemOverlayModeBtn = null;
        fireOverlayToggleBtn = fireOverlayModeBtn = null;
        totemParticlesToggleBtn = totemParticlesModeBtn = null;
        potionParticlesToggleBtn = potionParticlesModeBtn = null;
        explosionToggleBtn = explosionModeBtn = null;
        smokeToggleBtn = smokeModeBtn = null;
        bubblesToggleBtn = bubblesModeBtn = null;
        weatherToggleBtn = weatherModeBtn = null;
        arrowsToggleBtn = arrowsModeBtn = null;
        fireworksToggleBtn = fireworksModeBtn = null;
        namesToggleBtn = namesModeBtn = null;
        playersToggleBtn = playersModeBtn = null;
        handToggleBtn = handModeBtn = null;

        saveButton = null;

        int rightX = width / 2 + 50;
        int modeX = rightX + BUTTON_WIDTH + BUTTON_GAP;

        int contentY = 60;

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            totemOverlayToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderTotemOverlayEnabled, config::setNoRenderTotemOverlay);
            totemOverlayModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderTotemOverlayEnabled,
                config::setNoRenderTotemOverlay,
                config::getNoRenderTotemOverlay,
                config::setNoRenderTotemOverlay,
                true
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fire_overlay")) {
            fireOverlayToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderFireOverlayEnabled, config::setNoRenderFireOverlayEnabled);
            fireOverlayModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderFireOverlayEnabled,
                config::setNoRenderFireOverlayEnabled,
                config::getNoRenderFireOverlay,
                config::setNoRenderFireOverlay,
                true
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_particles")) {
            totemParticlesToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderTotemParticlesEnabled, config::setNoRenderTotemParticlesEnabled);
            totemParticlesModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderTotemParticlesEnabled,
                config::setNoRenderTotemParticlesEnabled,
                config::getNoRenderTotemParticles,
                config::setNoRenderTotemParticles,
                true
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_potion_particles")) {
            potionParticlesToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderPotionParticlesEnabled, config::setNoRenderPotionParticlesEnabled);
            potionParticlesModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderPotionParticlesEnabled,
                config::setNoRenderPotionParticlesEnabled,
                config::getNoRenderPotionParticles,
                config::setNoRenderPotionParticles,
                true
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_explosion")) {
            explosionToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderExplosionEnabled, config::setNoRenderExplosionEnabled);
            explosionModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderExplosionEnabled,
                config::setNoRenderExplosionEnabled,
                config::getNoRenderExplosion,
                config::setNoRenderExplosion,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_smoke")) {
            smokeToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderSmokeEnabled, config::setNoRenderSmokeEnabled);
            smokeModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderSmokeEnabled,
                config::setNoRenderSmokeEnabled,
                config::getNoRenderSmoke,
                config::setNoRenderSmoke,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_bubbles")) {
            bubblesToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderBubblesEnabled, config::setNoRenderBubblesEnabled);
            bubblesModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderBubblesEnabled,
                config::setNoRenderBubblesEnabled,
                config::getNoRenderBubbles,
                config::setNoRenderBubbles,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_weather")) {
            weatherToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderWeatherEnabled, config::setNoRenderWeatherEnabled);
            weatherModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderWeatherEnabled,
                config::setNoRenderWeatherEnabled,
                config::getNoRenderWeather,
                config::setNoRenderWeather,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_arrows")) {
            arrowsToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderArrowsEnabled, config::setNoRenderArrowsEnabled);
            arrowsModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderArrowsEnabled,
                config::setNoRenderArrowsEnabled,
                config::getNoRenderArrows,
                config::setNoRenderArrows,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fireworks")) {
            fireworksToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderFireworksEnabled, config::setNoRenderFireworksEnabled);
            fireworksModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderFireworksEnabled,
                config::setNoRenderFireworksEnabled,
                config::getNoRenderFireworks,
                config::setNoRenderFireworks,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_names")) {
            namesToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderNamesEnabled, config::setNoRenderNamesEnabled);
            namesModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderNamesEnabled,
                config::setNoRenderNamesEnabled,
                config::getNoRenderNames,
                config::setNoRenderNames,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_hand")) {
            handToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderHandEnabled, config::setNoRenderHandEnabled);
            handModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderHandEnabled,
                config::setNoRenderHandEnabled,
                config::getNoRenderHand,
                config::setNoRenderHand,
                false
            );
            contentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_players")) {
            playersToggleBtn = createToggleButton(rightX, contentY, config::isNoRenderPlayersEnabled, config::setNoRenderPlayersEnabled);
            playersModeBtn = createModeButton(modeX, contentY,
                config::isNoRenderPlayersEnabled,
                config::setNoRenderPlayersEnabled,
                config::getNoRenderPlayers,
                config::setNoRenderPlayers,
                false
            );
            contentY += SPACING;
        }

        int contentBottom = contentY + 70;

        setMaxScroll(Math.max(0F, contentBottom - getContentBottom()));

        saveButton = ButtonWidget.builder(Text.literal("Save & Back"),
                button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build();

        addDrawableChild(saveButton);

        updateAllButtons();
        updateScroll();
    }

    /// Создание элементов
    private ButtonWidget createToggleButton(int x, int y, Supplier<Boolean> enabledGetter, Consumer<Boolean> enabledSetter) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(""),
                b -> {
                    enabledSetter.accept(!enabledGetter.get());
                    changed = true;
                    updateAllButtons();
                    save();
                }
            )
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(button);

        return button;
    }

    private ButtonWidget createModeButton(int x, int y, Supplier<Boolean> enabledGetter, Consumer<Boolean> enabledSetter, Supplier<RenderMode> getter, Consumer<RenderMode> setter, boolean allowSmall) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(""),
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
                }
            )
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(button);

        return button;
    }

    /// Обновление элементов
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
            updateToggleButton(totemParticlesToggleBtn, config.isNoRenderTotemParticlesEnabled());updateModeButton(totemParticlesModeBtn, config.getNoRenderTotemParticles()
            );
        }

        if (potionParticlesToggleBtn != null) {
            updateToggleButton(potionParticlesToggleBtn, config.isNoRenderPotionParticlesEnabled());
            updateModeButton(potionParticlesModeBtn, config.getNoRenderPotionParticles());
        }

        if (explosionToggleBtn != null) {
            updateToggleButton(explosionToggleBtn, config.isNoRenderExplosionEnabled());
            updateModeButton(explosionModeBtn, config.getNoRenderExplosion());
        }

        if (smokeToggleBtn != null) {
            updateToggleButton(smokeToggleBtn, config.isNoRenderSmokeEnabled());
            updateModeButton(smokeModeBtn, config.getNoRenderSmoke());
        }

        if (bubblesToggleBtn != null) {
            updateToggleButton(bubblesToggleBtn, config.isNoRenderBubblesEnabled());
            updateModeButton(bubblesModeBtn, config.getNoRenderBubbles());
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
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style ->
                style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFFFF)
            )
        );
    }

    private void updateModeButton(ButtonWidget button, RenderMode mode) {
        if (button == null) {
            return;
        }

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

        button.setMessage(Text.literal(text)
            .styled(style -> style.withColor(color))
        );
    }

    private void updateButtonPosition(ButtonWidget toggleButton, ButtonWidget modeButton, int y) {
        if (toggleButton != null) {
            toggleButton.setY(y - 3);
        }

        if (modeButton != null) {
            modeButton.setY(y - 3);
        }
    }

    /// Скролл
    @Override
    protected void updateScroll() {
        super.updateScroll();

        updateButtonPositions();
    }

    private void updateButtonPositions() {
        int y = 60 - Math.round(scrollOffset);

        if (totemOverlayToggleBtn != null) {
            updateButtonPosition(totemOverlayToggleBtn, totemOverlayModeBtn, y);
            y += SPACING;
        }

        if (fireOverlayToggleBtn != null) {
            updateButtonPosition(fireOverlayToggleBtn, fireOverlayModeBtn, y);
            y += SPACING;
        }

        if (totemParticlesToggleBtn != null) {
            updateButtonPosition(totemParticlesToggleBtn, totemParticlesModeBtn, y);
            y += SPACING;
        }

        if (potionParticlesToggleBtn != null) {
            updateButtonPosition(potionParticlesToggleBtn, potionParticlesModeBtn, y);
            y += SPACING;
        }

        if (explosionToggleBtn != null) {
            updateButtonPosition(explosionToggleBtn, explosionModeBtn, y);
            y += SPACING;
        }

        if (smokeToggleBtn != null) {
            updateButtonPosition(smokeToggleBtn, smokeModeBtn, y);
            y += SPACING;
        }

        if (bubblesToggleBtn != null) {
            updateButtonPosition(bubblesToggleBtn, bubblesModeBtn, y);
            y += SPACING;
        }

        if (weatherToggleBtn != null) {
            updateButtonPosition(weatherToggleBtn, weatherModeBtn, y);
            y += SPACING;
        }

        if (arrowsToggleBtn != null) {
            updateButtonPosition(arrowsToggleBtn, arrowsModeBtn, y);
            y += SPACING;
        }

        if (fireworksToggleBtn != null) {
            updateButtonPosition(fireworksToggleBtn, fireworksModeBtn, y);
            y += SPACING;
        }

        if (namesToggleBtn != null) {
            updateButtonPosition(namesToggleBtn, namesModeBtn, y);
            y += SPACING;
        }

        if (handToggleBtn != null) {
            updateButtonPosition(handToggleBtn, handModeBtn, y);
            y += SPACING;
        }

        if (playersToggleBtn != null) {
            updateButtonPosition(playersToggleBtn, playersModeBtn, y);
        }
    }

    /// Рендер
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateButtonPositions();
        updateButtonInteraction();

        setFeatureButtonsVisible(false);

        super.render(context, mouseX, mouseY, delta);

        updateButtonPositions();
        updateButtonInteraction();

        setFeatureButtonsVisible(true);

        context.enableScissor(0, getContentTop(), width, getContentBottom());

        renderAllFeatureButtons(context, mouseX, mouseY, delta);
        renderLabels(context, mouseX, mouseY);

        context.disableScissor();

        if (saveButton != null) {
            saveButton.render(context, mouseX, mouseY, delta);
        }
    }

    private void renderAllFeatureButtons(DrawContext context, int mouseX, int mouseY, float delta) {
        renderButton(context, totemOverlayToggleBtn, mouseX, mouseY, delta);
        renderButton(context, totemOverlayModeBtn, mouseX, mouseY, delta);

        renderButton(context, fireOverlayToggleBtn, mouseX, mouseY, delta);
        renderButton(context, fireOverlayModeBtn, mouseX, mouseY, delta);

        renderButton(context, totemParticlesToggleBtn, mouseX, mouseY, delta);
        renderButton(context, totemParticlesModeBtn, mouseX, mouseY, delta);

        renderButton(context, potionParticlesToggleBtn, mouseX, mouseY, delta);
        renderButton(context, potionParticlesModeBtn, mouseX, mouseY, delta);

        renderButton(context, explosionToggleBtn, mouseX, mouseY, delta);
        renderButton(context, explosionModeBtn, mouseX, mouseY, delta);

        renderButton(context, smokeToggleBtn, mouseX, mouseY, delta);
        renderButton(context, smokeModeBtn, mouseX, mouseY, delta);

        renderButton(context, bubblesToggleBtn, mouseX, mouseY, delta);
        renderButton(context, bubblesModeBtn, mouseX, mouseY, delta);

        renderButton(context, weatherToggleBtn, mouseX, mouseY, delta);
        renderButton(context, weatherModeBtn, mouseX, mouseY, delta);

        renderButton(context, arrowsToggleBtn, mouseX, mouseY, delta);
        renderButton(context, arrowsModeBtn, mouseX, mouseY, delta);

        renderButton(context, fireworksToggleBtn, mouseX, mouseY, delta);
        renderButton(context, fireworksModeBtn, mouseX, mouseY, delta);

        renderButton(context, namesToggleBtn, mouseX, mouseY, delta);
        renderButton(context, namesModeBtn, mouseX, mouseY, delta);

        renderButton(context, handToggleBtn, mouseX, mouseY, delta);
        renderButton(context, handModeBtn, mouseX, mouseY, delta);

        renderButton(context, playersToggleBtn, mouseX, mouseY, delta);
        renderButton(context, playersModeBtn, mouseX, mouseY, delta);
    }

    private void renderButton(DrawContext context, ButtonWidget button, int mouseX, int mouseY, float delta) {
        if (button == null) {
            return;
        }

        button.render(context, mouseX, mouseY, delta);
    }

    private void renderLabels(DrawContext context, int mouseX, int mouseY) {
        int leftX = width / 4;
        int y = 60 - Math.round(scrollOffset);

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Overlay",
                "Hide or reduce the totem overlay."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fire_overlay")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fire Overlay",
                "Hide or reduce the fire overlay."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_totem_particles")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Totem Particles",
                "Hide or reduce the totem particles."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_potion_particles")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Potion Particles",
                "Hide or reduce the potion particles."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_explosion")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Explosion",
                "Hide explosion."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_smoke")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Smoke",
                "Hide smoke."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_bubbles")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Bubbles",
                "Hide bubbles particles."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_weather")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Weather",
                "Hide rain, snow and water splashes."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_arrows")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Arrows",
                "Hide arrows on players."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_fireworks")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Fireworks",
                "Hide fireworks particles."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_names")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Names",
                "Hide players names."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_hand")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Hand",
                "Hide client player's hand."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("no_render_players")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Players",
                "Hide players."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int descBottom = y + 12 + textRenderer.fontHeight;

        if (descBottom <= getContentTop() || y >= getContentBottom()) {
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

    private void setFeatureButtonsVisible(boolean visible) {
        setButtonVisible(totemOverlayToggleBtn, visible);
        setButtonVisible(totemOverlayModeBtn, visible);

        setButtonVisible(fireOverlayToggleBtn, visible);
        setButtonVisible(fireOverlayModeBtn, visible);

        setButtonVisible(totemParticlesToggleBtn, visible);
        setButtonVisible(totemParticlesModeBtn, visible);

        setButtonVisible(potionParticlesToggleBtn, visible);
        setButtonVisible(potionParticlesModeBtn, visible);

        setButtonVisible(explosionToggleBtn, visible);
        setButtonVisible(explosionModeBtn, visible);

        setButtonVisible(smokeToggleBtn, visible);
        setButtonVisible(smokeModeBtn, visible);

        setButtonVisible(bubblesToggleBtn, visible);
        setButtonVisible(bubblesModeBtn, visible);

        setButtonVisible(weatherToggleBtn, visible);
        setButtonVisible(weatherModeBtn, visible);

        setButtonVisible(arrowsToggleBtn, visible);
        setButtonVisible(arrowsModeBtn, visible);

        setButtonVisible(fireworksToggleBtn, visible);
        setButtonVisible(fireworksModeBtn, visible);

        setButtonVisible(namesToggleBtn, visible);
        setButtonVisible(namesModeBtn, visible);

        setButtonVisible(handToggleBtn, visible);
        setButtonVisible(handModeBtn, visible);

        setButtonVisible(playersToggleBtn, visible);
        setButtonVisible(playersModeBtn, visible);
    }

    private void updateButtonInteraction(ButtonWidget button) {
        if (button == null) {
            return;
        }

        int top = button.getY();
        int bottom = top + button.getHeight();

        boolean inside =
            bottom > getContentTop() &&
                top < getContentBottom();

        button.active = inside;
    }

    private void updateButtonInteraction() {
        updateButtonInteraction(totemOverlayToggleBtn);
        updateButtonInteraction(totemOverlayModeBtn);

        updateButtonInteraction(fireOverlayToggleBtn);
        updateButtonInteraction(fireOverlayModeBtn);

        updateButtonInteraction(totemParticlesToggleBtn);
        updateButtonInteraction(totemParticlesModeBtn);

        updateButtonInteraction(potionParticlesToggleBtn);
        updateButtonInteraction(potionParticlesModeBtn);

        updateButtonInteraction(explosionToggleBtn);
        updateButtonInteraction(explosionModeBtn);

        updateButtonInteraction(smokeToggleBtn);
        updateButtonInteraction(smokeModeBtn);

        updateButtonInteraction(bubblesToggleBtn);
        updateButtonInteraction(bubblesModeBtn);

        updateButtonInteraction(weatherToggleBtn);
        updateButtonInteraction(weatherModeBtn);

        updateButtonInteraction(arrowsToggleBtn);
        updateButtonInteraction(arrowsModeBtn);

        updateButtonInteraction(fireworksToggleBtn);
        updateButtonInteraction(fireworksModeBtn);

        updateButtonInteraction(namesToggleBtn);
        updateButtonInteraction(namesModeBtn);

        updateButtonInteraction(handToggleBtn);
        updateButtonInteraction(handModeBtn);

        updateButtonInteraction(playersToggleBtn);
        updateButtonInteraction(playersModeBtn);
    }

    private void setButtonVisible(ButtonWidget button, boolean visible) {
        if (button == null) {
            return;
        }

        button.visible = visible;
    }

    /// Сохранение
    private void save() {
        config.save();

        NoRender.refreshBlockedStatus();
        NoRender.reloadFromConfig();

        changed = false;
    }

    /// Закрытие
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