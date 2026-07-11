package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.CustomFog;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CustomFogConfigScreen extends MainConfigScreen {

    /// Виджеты
    private ButtonWidget noFogBtn, nightVisionBtn, biomeFogBtn, biomeSettingsBtn;
    private SliderWidget rangeSlider;
    private ColorSlider colorSlider;
    private TextFieldWidget hexField;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45, CONTROL_WIDTH = 180;
    private boolean changed = false;

    /// Конструктор
    public CustomFogConfigScreen(Screen parent) {
        super(Text.literal("CustomFog Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("custom_fog")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        rangeSlider = null;
        colorSlider = null;
        hexField = null;
        noFogBtn = null;
        biomeFogBtn = null;
        biomeSettingsBtn = null;
        nightVisionBtn = null;

        int centerX = width / 2;
        int currentY = 50;

        rangeSlider = createRangeSlider(centerX);
        rangeSlider.setY(currentY);
        addDrawableChild(rangeSlider);

        currentY += SPACING;

        colorSlider = createColorSlider(centerX - 40, currentY, config.getCustomFogColor(), value -> {
            int hue = (int) Math.round(value * 360);
            int rgb = hslToRgb(hue, 100, 50);

            config.setCustomFogColor(rgb);
            changed = true;
            updateHexField();
        });

        hexField = createHexField(centerX + 115, currentY, config.getCustomFogColor(), text -> {
            int rgb = parseHex(text);

            if (rgb != -1) {
                config.setCustomFogColor(rgb);
                changed = true;
                updateHexField();

                if (colorSlider != null) {
                    colorSlider.setValue(rgbToHue(rgb) / 360.0);
                }
            }
        });

        addDrawableChild(colorSlider);
        addDrawableChild(hexField);

        currentY += SPACING;

        if (!LiteApiManager.isFeatureBlocked("custom_fog_no_fog")) {
            noFogBtn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean state = !config.isNoFogEnabled();
                    config.setNoFogEnabled(state);
                    changed = true;
                    updateButton(noFogBtn, state);
                })
                .dimensions(centerX + 20, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(noFogBtn);
        }

        currentY += SPACING;

        if (!LiteApiManager.isFeatureBlocked("custom_fog_biome_fog")) {

            biomeFogBtn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean state = !config.isBiomeFogEnabled();
                    config.setBiomeFogEnabled(state);
                    changed = true;
                    updateButton(biomeFogBtn, state);
                })
                .dimensions(centerX + 20, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            biomeSettingsBtn = ButtonWidget.builder(Text.literal("Settings"), button -> {
                    if (client != null) {
                        client.setScreen(new BiomeFogConfigScreen(this));
                    }
                })
                .dimensions(centerX + 130, currentY, 80, BUTTON_HEIGHT)
                .build();

            addDrawableChild(biomeFogBtn);
            addDrawableChild(biomeSettingsBtn);
        }

        currentY += SPACING;

        if (!LiteApiManager.isFeatureBlocked("custom_fog_night_vision")) {
            nightVisionBtn = ButtonWidget.builder(Text.literal(""), button -> {
                    boolean state = !config.isNightVisionEnabled();
                    config.setNightVisionEnabled(state);
                    changed = true;
                    updateButton(nightVisionBtn, state);
                })
                .dimensions(centerX + 20, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(nightVisionBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 25)
            .build());

        updateAllButtons();
    }

    private SliderWidget createRangeSlider(int centerX) {
        int range = config.getCustomFogRange();

        return new SliderWidget(centerX - 40, 0, CONTROL_WIDTH, 20, Text.literal(range + " chunks"), (range - 1) / 31.0) {
            @Override
            protected void updateMessage() {
                int value = (int) Math.round(1 + this.value * 31);
                setMessage(Text.literal(value + " chunks"));
            }

            @Override
            protected void applyValue() {
                int value = (int) Math.round(1 + this.value * 31);
                config.setCustomFogRange(value);
                changed = true;
                save();
            }
        };
    }

    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Double> onValueChange) {
        return new ColorSlider(x, y, 150, 20, Text.literal(""), rgbToHue(rgb) / 360.0, onValueChange);
    }

    private TextFieldWidget createHexField(int x, int y, int rgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, 70, 20, Text.literal("#RRGGBB"));

        field.setText(String.format("#%06X", rgb));
        field.setChangedListener(onHexChange::accept);
        field.setFocusUnlocked(true);
        field.setEditable(true);

        return field;
    }

    /// Обновление состояния элементов
    private void updateHexField() {
        if (hexField == null) {
            return;
        }

        String hex = String.format("#%06X", config.getCustomFogColor());

        if (!hex.equalsIgnoreCase(hexField.getText())) {
            hexField.setText(hex);
        }
    }

    private void updateAllButtons() {
        if (noFogBtn != null) {
            updateButton(noFogBtn, config.isNoFogEnabled());
        }

        if (biomeFogBtn != null) {
            updateButton(biomeFogBtn, config.isBiomeFogEnabled());
        }

        if (nightVisionBtn != null) {
            updateButton(nightVisionBtn, config.isNightVisionEnabled());
        }
    }


    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
                .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
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

        float r = 0;
        float g = 0;
        float b = 0;

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

        int leftX = width / 4;
        int startY = 60;

        renderLabel(context, leftX, startY, mouseX, mouseY,
                "Range",
                "Fog range in chunks.");

        renderLabel(context, leftX, startY + SPACING, mouseX, mouseY,
                "Color",
                "Customizable fog color.");

        if (!LiteApiManager.isFeatureBlocked("custom_fog_no_fog")) {
            renderLabel(context, leftX, startY + SPACING * 2, mouseX, mouseY,
                    "NoFog",
                    "Disables fog completely.");
        }

        if (!LiteApiManager.isFeatureBlocked("custom_fog_biome_fog")) {
            renderLabel(context, leftX, startY + SPACING * 3, mouseX, mouseY,
                    "Biome Fog",
                    "Fog color changes depending on biome.");
        }

        if (!LiteApiManager.isFeatureBlocked("custom_fog_night_vision")) {
            renderLabel(context, leftX, startY + SPACING * 4, mouseX, mouseY,
                    "Night Vision",
                    "Adds night vision effect.");
        }

        int previewSize = 18;
        int previewX = hexField.getX() + 75;
        int previewY = colorSlider.getY();

        context.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
        context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | config.getCustomFogColor());
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
        CustomFog.refreshBlockedStatus();
        CustomFog.reloadFromConfig();

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