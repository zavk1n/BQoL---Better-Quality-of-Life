package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.ShulkerParticles;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ShulkerParticlesConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget constantBtn, constantDependenceBtn, breakingBtn, breakingDependenceBtn, vanillaBreakingBtn;
    private ColorSlider constantColorSlider, breakingColorSlider;
    private TextFieldWidget constantHexField, breakingHexField;

    private int constantCurrentColor, breakingCurrentColor, constantDepLabelX, constantDepLabelY, breakingDepLabelX, breakingDepLabelY;
    private boolean constantDepVisible = false, breakingDepVisible = false;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45,
        LABEL_WIDTH = 220, SLIDER_WIDTH = 150, HEX_WIDTH = 70,
        DEPENDENCE_BUTTONS_OFFSET = 165, COLOR_CONTROL_OFFSET = 20;

    private boolean changed = false;

    /// Конструктор
    public ShulkerParticlesConfigScreen(Screen parent) {
        super(Text.literal("Shulker Particles Settings"), parent);

        constantCurrentColor = config.getShulkerConstantColor();
        breakingCurrentColor = config.getShulkerBreakingColor();
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("shulker_particles")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        constantBtn = null;
        constantDependenceBtn = null;
        breakingBtn = null;
        breakingDependenceBtn = null;
        vanillaBreakingBtn = null;

        constantColorSlider = null;
        breakingColorSlider = null;
        constantHexField = null;
        breakingHexField = null;

        constantDepVisible = false;
        breakingDepVisible = false;

        int centerX = width / 2;
        int leftX = width / 6;

        int baseControlX = leftX + LABEL_WIDTH + 10;
        int controlX = baseControlX + 20;
        int sliderX = baseControlX - 20 + COLOR_CONTROL_OFFSET;

        int row = 0;

        boolean constantBlocked = LiteApiManager.isFeatureBlocked("shulker_particles_constant");
        boolean constantDependenceBlocked = LiteApiManager.isFeatureBlocked("shulker_particles_constant_dependence");

        if (!constantBlocked) {
            int y = 50 + row * SPACING;

            constantBtn = createButton(
                controlX,
                y,
                config::isShulkerConstantEnabled,
                value -> {
                    config.setShulkerConstantEnabled(value);
                    changed = true;
                }
            );
            addDrawableChild(constantBtn);

            constantDepLabelX = controlX + BUTTON_WIDTH + 20;
            constantDepLabelY = y;

            if (!constantDependenceBlocked) {
                int depBtnX = constantDepLabelX + textRenderer.getWidth("Dependence") + 10 + DEPENDENCE_BUTTONS_OFFSET;

                constantDependenceBtn = createButton(
                    depBtnX,
                    y,
                    config::isShulkerConstantDependence,
                    value -> {
                        config.setShulkerConstantDependence(value);
                        changed = true;
                    }
                );

                addDrawableChild(constantDependenceBtn);
                constantDepVisible = true;
            }

            row++;
        }

        boolean breakingBlocked = LiteApiManager.isFeatureBlocked("shulker_particles_breaking");
        boolean breakingDependenceBlocked = LiteApiManager.isFeatureBlocked("shulker_particles_breaking_dependence");

        if (!breakingBlocked) {
            int y = 50 + row * SPACING;

            breakingBtn = createButton(
                controlX,
                y,
                config::isShulkerBreakingEnabled,
                value -> {
                    config.setShulkerBreakingEnabled(value);
                    changed = true;
                }
            );
            addDrawableChild(breakingBtn);

            breakingDepLabelX = controlX + BUTTON_WIDTH + 20;
            breakingDepLabelY = y;

            if (!breakingDependenceBlocked) {
                int depBtnX = breakingDepLabelX + textRenderer.getWidth("Dependence") + 10 + DEPENDENCE_BUTTONS_OFFSET;

                breakingDependenceBtn = createButton(
                    depBtnX,
                    y,
                    config::isShulkerBreakingDependence,
                    value -> {
                        config.setShulkerBreakingDependence(value);
                        changed = true;
                    }
                );

                addDrawableChild(breakingDependenceBtn);
                breakingDepVisible = true;
            }

            row++;
        }

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_vanilla_breaking")) {
            int y = 50 + row * SPACING;

            vanillaBreakingBtn = createButton(
                controlX,
                y,
                config::isShulkerVanillaBreakingEnabled,
                value -> {
                    config.setShulkerVanillaBreakingEnabled(value);
                    changed = true;
                }
            );

            addDrawableChild(vanillaBreakingBtn);

            row++;
        }

        if (!constantBlocked) {
            int y = 50 + row * SPACING;

            constantColorSlider = createColorSlider(
                sliderX,
                y,
                constantCurrentColor,
                newColor -> {
                    constantCurrentColor = newColor;
                    config.setShulkerConstantColor(newColor);

                    if (constantHexField != null) {
                        updateHexField(constantHexField, newColor);
                    }

                    changed = true;
                }
            );

            constantHexField = createHexField(
                sliderX + SLIDER_WIDTH + 5,
                y,
                constantCurrentColor,
                hex -> {
                    int rgb = parseHex(hex);

                    if (rgb != -1) {
                        constantCurrentColor = rgb;
                        config.setShulkerConstantColor(rgb);

                        updateColorSlider(constantColorSlider, rgb);

                        changed = true;
                    }
                }
            );

            addDrawableChild(constantColorSlider);
            addDrawableChild(constantHexField);

            row++;
        }

        if (!breakingBlocked) {
            int y = 50 + row * SPACING;

            breakingColorSlider = createColorSlider(
                sliderX,
                y,
                breakingCurrentColor,
                newColor -> {
                    breakingCurrentColor = newColor;
                    config.setShulkerBreakingColor(newColor);

                    if (breakingHexField != null) {
                        updateHexField(breakingHexField, newColor);
                    }

                    changed = true;
                }
            );

            breakingHexField = createHexField(
                sliderX + SLIDER_WIDTH + 5,
                y,
                breakingCurrentColor,
                hex -> {
                    int rgb = parseHex(hex);

                    if (rgb != -1) {
                        breakingCurrentColor = rgb;
                        config.setShulkerBreakingColor(rgb);

                        updateColorSlider(breakingColorSlider, rgb);

                        changed = true;
                    }
                }
            );

            addDrawableChild(breakingColorSlider);
            addDrawableChild(breakingHexField);

            row++;
        }

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(centerX - 50, height - 40, 100, 25)
                .build()
        );
    }

    /// Работа с кнопками
    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget btn = ButtonWidget.builder(
                        Text.literal(getter.getAsBoolean() ? "Enabled" : "Disabled"),
                        button -> {
                            setter.accept(!getter.getAsBoolean());
                            updateButton(button, getter.getAsBoolean());
                        })
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        updateButton(btn, getter.getAsBoolean());
        return btn;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    /// Работа со слайдерами
    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Integer> onColorChange) {
        int hue = rgbToHue(rgb);
        ColorSlider slider = new ColorSlider(x, y, SLIDER_WIDTH, 20,
                Text.literal(""), hue / 360.0,
                value -> {
                    int newHue = (int) Math.round(value * 360);
                    int newRgb = hslToRgb(newHue, 100, 50);
                    onColorChange.accept(newRgb);
                });

        slider.setY(y);
        return slider;
    }

    private void updateColorSlider(ColorSlider slider, int rgb) {
        if (slider != null) {
            slider.setValue(rgbToHue(rgb) / 360.0);
        }
    }

    /// Работа с полем цвета
    private TextFieldWidget createHexField(int x, int y, int initialRgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, HEX_WIDTH, 20, Text.literal("#RRGGBB"));
        field.setText(String.format("#%06X", initialRgb));
        field.setChangedListener(text -> {
            if (text.startsWith("#") && text.length() == 7) {
                onHexChange.accept(text);
            }
        });
        field.setEditable(true);
        field.setFocusUnlocked(true);
        return field;
    }

    private void updateHexField(TextFieldWidget field, int rgb) {
        String formatted = String.format("#%06X", rgb);
        if (!formatted.equals(field.getText())) {
            field.setText(formatted);
        }
    }

    /// Работа с цветами
    private int parseHex(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                return Integer.parseInt(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    private int rgbToHue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int delta = max - min;

        if (delta == 0) {
            return 0;
        }

        float hue;
        if (max == r) {
            hue = (g - b) / (float) delta;
        } else if (max == g) {
            hue = (b - r) / (float) delta + 2;
        } else {
            hue = (r - g) / (float) delta + 4;
        }

        hue *= 60;
        if (hue < 0) {
            hue += 360;
        }

        return Math.round(hue);
    }

    private int hslToRgb(int h, int s, int l) {
        float hue = h / 360f;
        float sat = s / 100f;
        float light = l / 100f;

        float c = (1 - Math.abs(2 * light - 1)) * sat;
        float x = c * (1 - Math.abs((hue * 6) % 2 - 1));
        float m = light - c / 2;

        float r = 0, g = 0, b = 0;

        if (hue < 1 / 6f) {
            r = c;
            g = x;
        } else if (hue < 2 / 6f) {
            r = x;
            g = c;
        } else if (hue < 3 / 6f) {
            g = c;
            b = x;
        } else if (hue < 4 / 6f) {
            g = x;
            b = c;
        } else if (hue < 5 / 6f) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        return (Math.round((r + m) * 255) << 16) | (Math.round((g + m) * 255) << 8) | Math.round((b + m) * 255);
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 6;
        int startY = 50;
        int row = 0;

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_constant")) {
            int y = startY + row * SPACING;

            renderLabel(context, leftX, y,
                "Constant",
                "Spawning particles around the shulker.",
                mouseX,
                mouseY
            );

            if (constantDepVisible) {
                boolean hover = mouseX >= constantDepLabelX && mouseX <= constantDepLabelX + textRenderer.getWidth("Dependence") && mouseY >= constantDepLabelY - 5 && mouseY <= constantDepLabelY + 10;

                context.drawText(
                    textRenderer,
                    Text.literal("Dependence"),
                    constantDepLabelX,
                    constantDepLabelY,
                    hover ? ACCENT_COLOR : 0xFFFFFFFF,
                    false
                );

                context.drawText(
                    textRenderer,
                    Text.literal("Particles color depends on shulker color"),
                    constantDepLabelX,
                    constantDepLabelY + 12,
                    0xFF888888,
                    false
                );
            }

            row++;
        }

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_breaking")) {
            int y = startY + row * SPACING;

            renderLabel(context, leftX, y,
                "Breaking",
                "Spawns particles during a shulker breaking.",
                mouseX,
                mouseY
            );

            if (breakingDepVisible) {
                boolean hover = mouseX >= breakingDepLabelX && mouseX <= breakingDepLabelX + textRenderer.getWidth("Dependence") && mouseY >= breakingDepLabelY - 5 && mouseY <= breakingDepLabelY + 10;

                context.drawText(
                    textRenderer,
                    Text.literal("Dependence"),
                    breakingDepLabelX,
                    breakingDepLabelY,
                    hover ? ACCENT_COLOR : 0xFFFFFFFF,
                    false
                );

                context.drawText(
                    textRenderer,
                    Text.literal("Particles color depends on shulker color"),
                    breakingDepLabelX,
                    breakingDepLabelY + 12,
                    0xFF888888,
                    false
                );
            }

            row++;
        }

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_vanilla_breaking")) {
            renderLabel(context, leftX, startY + row * SPACING,
                "Vanilla Breaking",
                "Remove vanilla break particles.",
                mouseX,
                mouseY
            );
            row++;
        }

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_constant")) {
            renderLabel(context, leftX, startY + row * SPACING,
                "Constant Color",
                "Custom color when Dependence is Off.",
                mouseX,
                mouseY
            );
            row++;
        }

        if (!LiteApiManager.isFeatureBlocked("shulker_particles_breaking")) {
            renderLabel(context, leftX, startY + row * SPACING,
                "Breaking Color",
                "Custom color when Dependence is Off.",
                mouseX,
                mouseY
            );
        }

        if (constantHexField != null && constantHexField.isVisible()) {
            int previewSize = 18;
            int previewX = constantHexField.getX() + HEX_WIDTH + 5;
            int previewY = constantColorSlider.getY();

            context.fill(previewX - 1, previewY - 1,
                previewX + previewSize + 1,
                previewY + previewSize + 1,
                0xFF000000);

            context.fill(previewX, previewY,
                previewX + previewSize,
                previewY + previewSize,
                0xFF000000 | constantCurrentColor);
        }

        if (breakingHexField != null && breakingHexField.isVisible()) {
            int previewSize = 18;
            int previewX = breakingHexField.getX() + HEX_WIDTH + 5;
            int previewY = breakingColorSlider.getY();

            context.fill(previewX - 1, previewY - 1,
                previewX + previewSize + 1,
                previewY + previewSize + 1,
                0xFF000000);

            context.fill(previewX, previewY,
                previewX + previewSize,
                previewY + previewSize,
                0xFF000000 | breakingCurrentColor);
        }
    }

    private void renderLabel(DrawContext context, int x, int y, String title, String desc, int mouseX, int mouseY) {
        int titleColor = (mouseX >= x && mouseX <= x + textRenderer.getWidth(title) && mouseY >= y - 5 && mouseY <= y + 10)
                ? ACCENT_COLOR
                : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, titleColor, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();
        ShulkerParticles.refreshBlockedStatus();

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