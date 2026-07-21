package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.CustomHealth;

import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CustomHealthConfigScreen extends MainConfigScreen {

    /// Виджеты
    private SliderWidget durationSlider;
    private ButtonWidget locationButton, hoveringBtn, scalingBtn, pvpModeBtn, decimalBtn, goldenHeartsBtn;
    private ColorCheckbox goldenHeartsPlusCheck;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45, LABEL_WIDTH = 220;
    private boolean changed = false;

    private static final String[] LOCATION_NAMES = {"Over", "Left", "Right", "Under"};

    /// Конструктор
    public CustomHealthConfigScreen(Screen parent) {
        super(Text.literal("Custom Health Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("custom_health")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        durationSlider = null;
        locationButton = null;
        hoveringBtn = null;
        scalingBtn = null;
        pvpModeBtn = null;
        decimalBtn = null;
        goldenHeartsBtn = null;
        goldenHeartsPlusCheck = null;

        int centerX = width / 2;
        int startY = 60;
        int controlX = centerX + 50;

        int durationSec = config.customHealthDuration / 1000;

        durationSlider = new SliderWidget(controlX, startY, 180, 20, Text.literal(durationSec + " s"), (durationSec - 3) / 27.0) {
            @Override
            protected void updateMessage() {
                int value = (int) Math.round(3 + this.value * 27);
                setMessage(Text.literal(value + " s"));
                config.customHealthDuration = value * 1000;
            }

            @Override
            protected void applyValue() {
                int value = (int) Math.round(3 + this.value * 27);
                config.customHealthDuration = value * 1000;
                changed = true;
            }
        };

        addDrawableChild(durationSlider);

        locationButton = ButtonWidget.builder(
                Text.literal(LOCATION_NAMES[config.getCustomHealthLocation()])
                    .styled(s -> s.withColor(ACCENT_COLOR)),
                button -> {
                    int location = (config.getCustomHealthLocation() + 1) % LOCATION_NAMES.length;
                    config.setCustomHealthLocation(location);
                    changed = true;
                    locationButton.setMessage(
                        Text.literal(LOCATION_NAMES[location])
                            .styled(s -> s.withColor(ACCENT_COLOR))
                    );
                })
            .dimensions(controlX, startY + SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(locationButton);

        if (!LiteApiManager.isFeatureBlocked("custom_health_hovering")) {
            hoveringBtn = createButton(
                controlX,
                startY + SPACING * 2,
                config::isCustomHealthHovering,
                config::setCustomHealthHovering
            );
        }

        if (!LiteApiManager.isFeatureBlocked("custom_health_scaling")) {
            scalingBtn = createButton(
                controlX,
                startY + SPACING * 3,
                config::isCustomHealthScaling,
                config::setCustomHealthScaling
            );
        }

        if (!LiteApiManager.isFeatureBlocked("custom_health_pvp")) {
            pvpModeBtn = createButton(
                controlX,
                startY + SPACING * 4,
                config::isCustomHealthPvPMode,
                config::setCustomHealthPvPMode
            );
        }

        if (!LiteApiManager.isFeatureBlocked("custom_health_decimal")) {
            decimalBtn = createButton(
                controlX,
                startY + SPACING * 5,
                config::isCustomHealthDecimal,
                config::setCustomHealthDecimal
            );
        }

        if (!LiteApiManager.isFeatureBlocked("custom_health_golden_hearts")) {
            goldenHeartsBtn = createButton(
                controlX,
                startY + SPACING * 6,
                config::isCustomHealthGoldenHearts,
                value -> {
                    config.setCustomHealthGoldenHearts(value);
                    changed = true;

                    updateGoldenHeartsPlusState();

                    if (goldenHeartsPlusCheck != null) {
                        goldenHeartsPlusCheck.setChecked(config.isCustomHealthGoldenHeartsPlus());
                    }
                }
            );
        }

        if (!LiteApiManager.isFeatureBlocked("custom_health_golden_plus") && goldenHeartsBtn != null) {
            goldenHeartsPlusCheck = new ColorCheckbox(
                controlX,
                startY + SPACING * 7 + 6,
                Text.empty(),
                config.isCustomHealthGoldenHeartsPlus(),
                button -> {
                    boolean enabled = !config.isCustomHealthGoldenHeartsPlus();
                    config.setCustomHealthGoldenHeartsPlus(enabled);
                    ((ColorCheckbox) button).setChecked(enabled);
                    changed = true;
                }
            );

            addDrawableChild(goldenHeartsPlusCheck);
            updateGoldenHeartsPlusState();
        }

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
                .dimensions(width / 2 - 50, height - 40, 100, BUTTON_HEIGHT)
                .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.empty(),
                widget -> {
                    boolean enabled = !getter.getAsBoolean();
                    setter.accept(enabled);
                    changed = true;
                    updateButton(widget, enabled);
                })
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());
        addDrawableChild(button);

        return button;
    }

    /// Обновление состояния кнопок
    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        if (hoveringBtn != null) updateButton(hoveringBtn, config.isCustomHealthHovering());
        if (scalingBtn != null) updateButton(scalingBtn, config.isCustomHealthScaling());
        if (pvpModeBtn != null) updateButton(pvpModeBtn, config.isCustomHealthPvPMode());
        if (decimalBtn != null) updateButton(decimalBtn, config.isCustomHealthDecimal());
        if (goldenHeartsBtn != null) updateButton(goldenHeartsBtn, config.isCustomHealthGoldenHearts());
    }

    private void updateGoldenHeartsPlusState() {
        if (goldenHeartsPlusCheck != null) {
            goldenHeartsPlusCheck.active = config.isCustomHealthGoldenHearts();
        }
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int labelX = width / 4;
        int startY = 60;

        renderLabel(context, labelX, startY, mouseX, mouseY,
            "Duration", "Indicator display duration."
        );

        renderLabel(context, labelX, startY + SPACING, mouseX, mouseY,
            "Location", "Position of the indicator on the screen."
        );

        if (!LiteApiManager.isFeatureBlocked("custom_health_hovering"))
            renderLabel(context, labelX, startY + SPACING * 2, mouseX, mouseY,
                "Hovering", "Displaying indicator when aiming at player."
            );

        if (!LiteApiManager.isFeatureBlocked("custom_health_scaling"))
            renderLabel(context, labelX, startY + SPACING * 3, mouseX, mouseY,
                "Scaling", "Displaying indicator with scaling effect."
            );

        if (!LiteApiManager.isFeatureBlocked("custom_health_pvp"))
            renderLabel(context, labelX, startY + SPACING * 4, mouseX, mouseY,
                "PvP Mode", "Displaying indicator only in PvP."
            );

        if (!LiteApiManager.isFeatureBlocked("custom_health_decimal"))
            renderLabel(context, labelX, startY + SPACING * 5, mouseX, mouseY,
                "Decimal Format", "Displaying indicator with float part."
            );

        if (!LiteApiManager.isFeatureBlocked("custom_health_golden_hearts"))
            renderLabel(context, labelX, startY + SPACING * 6, mouseX, mouseY,
                "Golden Hearts", "Displaying indicator shows additional hearts."
            );

        if (!LiteApiManager.isFeatureBlocked("custom_health_golden_plus"))
            renderLabel(context, labelX, startY + SPACING * 7, mouseX, mouseY,
                "Golden Hearts Plus", "Show golden hearts with a '+' sign."
            );
    }

    private void renderLabel(DrawContext ctx, int x, int y, int mousex, int mousey, String title, String desc) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered =
            mousex >= x &&
                mousex <= x + titleWidth &&
                mousey >= y &&
                mousey <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        ctx.drawTextWithShadow(textRenderer, Text.literal(title), x, y, color);
        ctx.drawTextWithShadow(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();

        CustomHealth.refreshBlockedStatus();
        CustomHealth.reloadFromConfig();
        CustomHealth.resetDisplay();

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