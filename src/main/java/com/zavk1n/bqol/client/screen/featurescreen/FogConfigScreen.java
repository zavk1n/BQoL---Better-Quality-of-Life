package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterFog;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FogConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget noFogBtn, nightVisionBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45, CONTROL_WIDTH = 180;
    private boolean changed = false;

    /// Конструктор
    public FogConfigScreen(Screen parent) {
        super(Text.literal("Better Fog Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_fog")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        noFogBtn = null;
        nightVisionBtn = null;

        int leftX = width / 4;
        int buttonX = width / 2 + 20;
        int currentY = 60;

        if (!LiteApiManager.isFeatureBlocked("custom_fog_no_fog")) {
            noFogBtn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean state = !config.isNoFog();
                    config.setNoFog(state);
                    changed = true;
                    updateButton(noFogBtn, state);
                })
                .dimensions(buttonX, currentY - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(noFogBtn);

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("custom_fog_night_vision")) {
            nightVisionBtn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean state = !config.isNightVision();
                    config.setNightVision(state);
                    changed = true;
                    updateButton(nightVisionBtn, state);
                })
                .dimensions(buttonX, currentY - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(nightVisionBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build());

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (noFogBtn != null) {
            updateButton(noFogBtn, config.isNoFog());
        }

        if (nightVisionBtn != null) {
            updateButton(nightVisionBtn, config.isNightVision());
        }
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int currentY = 60;

        if (!LiteApiManager.isFeatureBlocked("custom_fog_no_fog")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "No Fog",
                "Disables fog completely." );

            currentY += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("custom_fog_night_vision")) {
            renderLabel(context, leftX, currentY, mouseX, mouseY,
                "Night Vision",
                "Adds night vision effect." );
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
        BetterFog.refreshBlockedStatus();

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
// v1.0