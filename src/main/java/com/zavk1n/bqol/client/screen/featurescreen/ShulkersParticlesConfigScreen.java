
package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.BetterParticles;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ShulkersParticlesConfigScreen extends MainConfigScreen {

    private ButtonWidget constantBtn, constantDependenceBtn, breakingBtn, breakingDependenceBtn, vanillaBreakingBtn;
    private ColorSlider constantColorSlider, breakingColorSlider;
    private TextFieldWidget constantHexField, breakingHexField;

    private int constantCurrentColor, breakingCurrentColor;
    private int constantDepLabelX, constantDepLabelY, breakingDepLabelX, breakingDepLabelY;

    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;
    private static final int SPACING = 45;
    private static final int LABEL_WIDTH = 220;
    private static final int SLIDER_WIDTH = 150;
    private static final int HEX_WIDTH = 70;
    private static final int DEPENDENCE_BUTTONS_OFFSET = 165;
    private static final int COLOR_CONTROL_OFFSET = 20;

    private boolean changed = false;

    public ShulkersParticlesConfigScreen(Screen parent) {
        super(Text.literal("Better Particles Shulkers Settings"), parent);

        constantCurrentColor = config.getBetterParticlesShulkersConstantColor();
        breakingCurrentColor = config.getBetterParticlesShulkersBreakingColor();
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_particles_shulkers")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

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

        int centerX = this.width / 2;
        int leftX = this.width / 6;

        int baseControlX = leftX + LABEL_WIDTH + 10;
        int controlX = baseControlX + COLOR_CONTROL_OFFSET;
        int sliderX = baseControlX;

        int row = 0;
        int y = 60 + row * SPACING;

        constantBtn = createButton(controlX, y, config::isBetterParticlesShulkersConstantEnabled,
            value -> {
                config.setBetterParticlesShulkersConstantEnabled(value);
                changed = true;
            }
        );

        addDrawableChild(constantBtn);

        constantDepLabelX = controlX + BUTTON_WIDTH + 20;
        constantDepLabelY = y;

        int depBtnX = constantDepLabelX + textRenderer.getWidth("Dependence") + 10 + DEPENDENCE_BUTTONS_OFFSET;

        constantDependenceBtn = createButton(depBtnX, y, config::isBetterParticlesShulkersConstantDependenceEnabled,
            value -> {
                config.setBetterParticlesShulkersConstantDependenceEnabled(value);
                changed = true;
            }
        );

        addDrawableChild(constantDependenceBtn);

        row++;
        y = 60 + row * SPACING;

        breakingBtn = createButton(controlX, y, config::isBetterParticlesShulkersBreakingEnabled,
            value -> {
                config.setBetterParticlesShulkersBreakingEnabled(value);
                changed = true;
            }
        );

        addDrawableChild(breakingBtn);

        breakingDepLabelX = controlX + BUTTON_WIDTH + 20;
        breakingDepLabelY = y;

        depBtnX = breakingDepLabelX + textRenderer.getWidth("Dependence") + 10 + DEPENDENCE_BUTTONS_OFFSET;

        breakingDependenceBtn = createButton(depBtnX, y, config::isBetterParticlesShulkersBreakingDependenceEnabled,
            value -> {
                config.setBetterParticlesShulkersBreakingDependenceEnabled(value);
                changed = true;
            }
        );

        addDrawableChild(breakingDependenceBtn);

        row++;
        y = 60 + row * SPACING;

        vanillaBreakingBtn = createButton(controlX, y, config::isBetterParticlesShulkersVanillaBreakingEnabled,
            value -> {
                config.setBetterParticlesShulkersVanillaBreakingEnabled(value);
                changed = true;
            }
        );

        addDrawableChild(vanillaBreakingBtn);

        row++;
        y = 60 + row * SPACING;

        constantColorSlider = createColorSlider(sliderX, y, constantCurrentColor,
            newColor -> {
                constantCurrentColor = newColor;

                config.setBetterParticlesShulkersConstantColor(newColor);

                if (constantHexField != null) {
                    updateHexField(constantHexField, newColor);
                }

                changed = true;
            }
        );

        constantHexField = createHexField(sliderX + SLIDER_WIDTH + 5, y, constantCurrentColor,
            hex -> {
                int rgb = parseHex(hex);

                if (rgb != -1) {
                    constantCurrentColor = rgb;

                    config.setBetterParticlesShulkersConstantColor(rgb);
                    updateColorSlider(constantColorSlider, rgb);

                    changed = true;
                }
            }
        );

        addDrawableChild(constantColorSlider);
        addDrawableChild(constantHexField);

        row++;
        y = 60 + row * SPACING;

        breakingColorSlider = createColorSlider(sliderX, y, breakingCurrentColor,
            newColor -> {
                breakingCurrentColor = newColor;

                config.setBetterParticlesShulkersBreakingColor(newColor);

                if (breakingHexField != null) {
                    updateHexField(breakingHexField, newColor);
                }

                changed = true;
            }
        );

        breakingHexField = createHexField(sliderX + SLIDER_WIDTH + 5, y, breakingCurrentColor,
            hex -> {
                int rgb = parseHex(hex);

                if (rgb != -1) {
                    breakingCurrentColor = rgb;

                    config.setBetterParticlesShulkersBreakingColor(rgb);
                    updateColorSlider(breakingColorSlider, rgb);

                    changed = true;
                }
            }
        );

        addDrawableChild(breakingColorSlider);
        addDrawableChild(breakingHexField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, BUTTON_HEIGHT)
            .build()
        );
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(getter.getAsBoolean() ? "Enabled" : "Disabled"),
            btn -> {
                setter.accept(!getter.getAsBoolean());
                updateButton(btn, getter.getAsBoolean());
            })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());

        return button;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Integer> onColorChange) {
        int hue = rgbToHue(rgb);

        ColorSlider slider = new ColorSlider(x, y, SLIDER_WIDTH, 20, Text.literal(""),
            hue / 360.0,
            value -> {
                int newHue = (int) Math.round(value * 360.0);
                int newRgb = hslToRgb(newHue, 100, 50);

                onColorChange.accept(newRgb);
            }
        );

        slider.setY(y);

        return slider;
    }

    private void updateColorSlider(ColorSlider slider, int rgb) {
        if (slider != null) {
            slider.setValue(rgbToHue(rgb) / 360.0);
        }
    }

    private TextFieldWidget createHexField(int x, int y, int initialRgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, HEX_WIDTH, 20, Text.literal("#RRGGBB"));

        field.setMaxLength(7);
        field.setText(String.format("#%06X", initialRgb & 0xFFFFFF));
        field.setChangedListener(text -> {
            if (text.startsWith("#") && text.length() == 7) {
                onHexChange.accept(text);
            }
        });

        field.setEditable(true);
        field.setDrawsBackground(true);

        return field;
    }

    private void updateHexField(TextFieldWidget field, int rgb) {
        String formatted = String.format("#%06X", rgb & 0xFFFFFF);

        if (!formatted.equals(field.getText())) {
            field.setText(formatted);
        }
    }

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
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int delta = max - min;

        if (delta == 0) {
            return 0;
        }

        float hue;

        if (max == r) {
            hue = (float) (g - b) / delta;
        } else if (max == g) {
            hue = (float) (b - r) / delta + 2.0F;
        } else {
            hue = (float) (r - g) / delta + 4.0F;
        }

        hue *= 60.0F;

        if (hue < 0.0F) {
            hue += 360.0F;
        }

        return Math.round(hue);
    }

    private int hslToRgb(int h, int s, int l) {
        float hue = h / 360.0F;
        float sat = s / 100.0F;
        float light = l / 100.0F;

        float c = (1.0F - Math.abs(2.0F * light - 1.0F)) * sat;
        float x = c * (1.0F - Math.abs(hue * 6.0F % 2.0F - 1.0F));
        float m = light - c / 2.0F;

        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;

        if (hue < 1.0F / 6.0F) {
            r = c;
            g = x;
        } else if (hue < 2.0F / 6.0F) {
            r = x;
            g = c;
        } else if (hue < 3.0F / 6.0F) {
            g = c;
            b = x;
        } else if (hue < 4.0F / 6.0F) {
            g = x;
            b = c;
        } else if (hue < 5.0F / 6.0F) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        return Math.round((r + m) * 255.0F) << 16
            | Math.round((g + m) * 255.0F) << 8
            | Math.round((b + m) * 255.0F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 6;
        int row = 0;
        int y = 60 + row * SPACING;

        renderLabel(context, leftX, y,
            "Constant",
            "Spawns particles around the shulker.", mouseX, mouseY
        );

        renderDependenceLabel(context, constantDepLabelX, constantDepLabelY, mouseX, mouseY);
        row++;
        y = 60 + row * SPACING;

        renderLabel(context, leftX, y,
            "Breaking",
            "Spawns particles during a shulker breaking.", mouseX, mouseY
        );

        renderDependenceLabel(context, breakingDepLabelX, breakingDepLabelY, mouseX, mouseY);
        row++;

        renderLabel(context, leftX, 60 + row * SPACING,
            "Vanilla Breaking",
            "Removes vanilla break particles.", mouseX, mouseY
        );
        row++;

        renderLabel(context, leftX, 60 + row * SPACING,
            "Constant Color",
            "Custom color when Dependence is Off.", mouseX, mouseY
        );
        row++;

        renderLabel(context, leftX, 60 + row * SPACING,
            "Breaking Color",
            "Custom color when Dependence is Off.", mouseX, mouseY
        );

        if (constantHexField != null
            && constantColorSlider != null
            && constantHexField.isVisible()) {

            renderColorPreview(context, constantHexField.getX() + HEX_WIDTH + 5, constantColorSlider.getY(), constantCurrentColor);
        }

        if (breakingHexField != null
            && breakingColorSlider != null
            && breakingHexField.isVisible()) {

            renderColorPreview(context, breakingHexField.getX() + HEX_WIDTH + 5, breakingColorSlider.getY(), breakingCurrentColor);
        }
    }

    private void renderDependenceLabel(DrawContext context, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x
            && mouseX <= x + textRenderer.getWidth("Dependence")
            && mouseY >= y - 5
            && mouseY <= y + 10;

        context.drawText(textRenderer, Text.literal("Dependence"), x, y, hovered ? ACCENT_COLOR : 0xFFFFFF, false);
        context.drawText(textRenderer, Text.literal("Particles color depends on shulker color"), x, y + 12, 0x888888, false);
    }

    private void renderColorPreview(DrawContext context, int x, int y, int rgb) {
        int previewSize = 18;

        context.fill(x - 1, y - 1, x + previewSize + 1, y + previewSize + 1, 0xFF000000);
        context.fill(x, y, x + previewSize, y + previewSize, 0xFF000000 | (rgb & 0xFFFFFF));
    }

    private void renderLabel(DrawContext context, int x, int y, String title, String desc, int mouseX, int mouseY) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x
            && mouseX <= x + titleWidth
            && mouseY >= y
            && mouseY <= y + 9;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0x888888, false);
    }

    private void save() {
        config.save();

        BetterParticles.refreshBlockedStatus();

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