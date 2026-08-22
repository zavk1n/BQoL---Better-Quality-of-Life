package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.features.BetterHolograms;
import com.zavk1n.bqol.features.BetterTnt;
import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HologramsConfigScreen extends MainConfigScreen{

    /// Виджеты
    private ButtonWidget visibleArmorStandBtn, antiHologramsBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    /// Конструктор
    public HologramsConfigScreen(Screen parent) {
        super(Text.literal("Better Holograms Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_holograms")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        visibleArmorStandBtn = null;
        antiHologramsBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_holograms_visible_armor_stand")) {
            visibleArmorStandBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterHologramsVisibleArmorStand() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterHologramsVisibleArmorStand();
                        config.setBetterHologramsVisibleArmorStand(state);
                        changed = true;
                        updateButton(visibleArmorStandBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(visibleArmorStandBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_holograms_anti_holograms")) {
            antiHologramsBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterHologramsAntiHolograms() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterHologramsAntiHolograms();
                        config.setBetterHologramsAntiHolograms(state);
                        changed = true;
                        updateButton(antiHologramsBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(antiHologramsBtn);
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

    /// Обновление состояния кнопок
    private void updateAllButtons() {
        if (visibleArmorStandBtn != null) {
            updateButton(visibleArmorStandBtn, config.isBetterHologramsVisibleArmorStand());
        }

        if (antiHologramsBtn != null) {
            updateButton(antiHologramsBtn, config.isBetterHologramsAntiHolograms());
        }
    }

    private void updateButton(ButtonWidget btn, boolean enabled) {
        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_holograms_visible_armor_stand")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Visible Armor Stand",
                "Allows you to see armor stands if they're hidden."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_holograms_anti_holograms")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Anti Holograms",
                "Cancels rendering of all holograms."
            );
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
        BetterHolograms.refreshBlockedStatus();

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