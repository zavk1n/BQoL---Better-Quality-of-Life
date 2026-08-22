package com.zavk1n.bqol.client.screen.featurescreen;

import net.minecraft.client.gui.widget.TextFieldWidget;
import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterInteract;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class InteractConfigScreen extends MainConfigScreen{

    /// Виджеты
    private ButtonWidget clickThroughBtn, antiSignsBtn, autoSignsBtn, safeHarvestBtn;
    private TextFieldWidget autoSignsTextField;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    /// Конструктор
    public InteractConfigScreen(Screen parent) {
        super(Text.literal("Better Interact Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_interact")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        clickThroughBtn = null;
        antiSignsBtn = null;
        autoSignsBtn = null;
        autoSignsTextField = null;
        safeHarvestBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_interact_click_through")) {
            clickThroughBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterInteractClickThrough() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterInteractClickThrough();
                        config.setBetterInteractClickThrough(state);
                        changed = true;
                        updateButton(clickThroughBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(clickThroughBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_anti_signs")) {
            antiSignsBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterInteractAntiSigns() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterInteractAntiSigns();
                        config.setBetterInteractAntiSigns(state);
                        changed = true;
                        updateButton(antiSignsBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(antiSignsBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_auto_signs")) {
            autoSignsBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterInteractAutoSigns() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterInteractAutoSigns();
                        config.setBetterInteractAutoSigns(state);
                        changed = true;
                        updateButton(autoSignsBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(autoSignsBtn);

            autoSignsTextField = new TextFieldWidget(
                textRenderer,
                rightX + BUTTON_WIDTH + 8,
                y - 3,
                150,
                BUTTON_HEIGHT,
                Text.literal("Sign text")
            );

            autoSignsTextField.setMaxLength(200);
            autoSignsTextField.setText(config.getBetterInteractAutoSignsText());

            autoSignsTextField.setChangedListener(text -> {
                config.setBetterInteractAutoSignsText(text);
                changed = true;
            });

            addDrawableChild(autoSignsTextField);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_safe_harvest")) {
            safeHarvestBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterInteractSafeHarvest() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterInteractSafeHarvest();
                        config.setBetterInteractSafeHarvest(state);
                        changed = true;
                        updateButton(safeHarvestBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(safeHarvestBtn);
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
        if (clickThroughBtn != null) {
            updateButton(clickThroughBtn, config.isBetterInteractClickThrough());
        }

        if (antiSignsBtn != null) {
            updateButton(antiSignsBtn, config.isBetterInteractAntiSigns());
        }

        if (autoSignsBtn != null) {
            updateButton(autoSignsBtn, config.isBetterInteractAutoSigns());
        }

        if (safeHarvestBtn != null) {
            updateButton(safeHarvestBtn, config.isBetterInteractSafeHarvest());
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

        if (!LiteApiManager.isFeatureBlocked("better_interact_click_through")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Click Through",
                "Interaction with storages through signs, etc.."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_anti_signs")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Anti Signs",
                "Putting signs is MANY times faster."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_auto_signs")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Auto Signs",
                "Automatically fills placed signs with specified text."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_interact_safe_harvest")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Safe Harvest",
                "Eliminates possibility of crop breaking."
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
        BetterInteract.refreshBlockedStatus();

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