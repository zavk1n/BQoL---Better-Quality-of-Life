package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorSlider;
import com.zavk1n.bqol.features.BetterPrefixes;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PrefixesConfigScreen extends MainConfigScreen {

    private ButtonWidget nameProtectBtn, donateBtn, titleBtn, starBtn;
    private ButtonWidget nameColorBtn, donateColorBtn, titleColorBtn;
    private ButtonWidget nameBoldBtn, donateBoldBtn, titleBoldBtn;

    private ColorSlider nameColorSlider, donateColorSlider, titleColorSlider, starColorSlider;

    private TextFieldWidget protectedNameField, donateTextField, titleTextField;
    private TextFieldWidget nameHexField, donateHexField, titleHexField, starHexField;

    private int nameCurrentColor;
    private int donateCurrentColor;
    private int titleCurrentColor;
    private int starCurrentColor;

    private boolean updatingHexField;
    private boolean updatingColorControls;
    private boolean changed;

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BOLD_BUTTON_WIDTH = 115;

    private static final int LABEL_WIDTH = 220;
    private static final int TEXT_FIELD_WIDTH = 170;
    private static final int SLIDER_WIDTH = 150;
    private static final int HEX_WIDTH = 75;
    private static final int PREVIEW_SIZE = 18;

    private static final int START_Y = 48;
    private static final int BLOCK_HEIGHT = 82;
    private static final int COLOR_ROW_OFFSET = 39;

    public PrefixesConfigScreen(Screen parent) {
        super(Text.literal("Better Prefixes Settings"), parent);

        nameCurrentColor = config.betterPrefixesNameColorRgb & 0xFFFFFF;
        donateCurrentColor = config.betterPrefixesDonateColorRgb & 0xFFFFFF;
        titleCurrentColor = config.betterPrefixesTitleColorRgb & 0xFFFFFF;
        starCurrentColor = config.betterPrefixesStarColorRgb & 0xFFFFFF;
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_prefixes")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        nameProtectBtn = null;
        donateBtn = null;
        titleBtn = null;
        starBtn = null;

        nameColorBtn = null;
        donateColorBtn = null;
        titleColorBtn = null;

        nameBoldBtn = null;
        donateBoldBtn = null;
        titleBoldBtn = null;

        nameColorSlider = null;
        donateColorSlider = null;
        titleColorSlider = null;
        starColorSlider = null;

        protectedNameField = null;
        donateTextField = null;
        titleTextField = null;

        nameHexField = null;
        donateHexField = null;
        titleHexField = null;
        starHexField = null;

        int centerX = width / 2;
        int leftX = width / 6;

        int controlX = leftX + LABEL_WIDTH + 20;
        int textX = controlX + BUTTON_WIDTH + 15;
        int boldX = textX + TEXT_FIELD_WIDTH + 10;

        int sliderX = controlX + BUTTON_WIDTH + 10;
        int hexX = sliderX + SLIDER_WIDTH + 5;

        int block = 0;

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_name_protect")) {
            int y = START_Y + block * BLOCK_HEIGHT;
            int colorY = y + COLOR_ROW_OFFSET;

            nameProtectBtn = createButton(
                controlX,
                y,
                config::isBetterPrefixesNameProtect,
                value -> {
                    config.setBetterPrefixesNameProtect(value);
                    changed = true;
                }
            );

            protectedNameField = createTextField(
                textX,
                y,
                TEXT_FIELD_WIDTH,
                config.betterPrefixesProtectedName,
                value -> {
                    config.betterPrefixesProtectedName = value;
                    changed = true;
                }
            );

            nameBoldBtn = createBoldButton(
                boldX,
                y,
                () -> config.betterPrefixesNameBold,
                value -> {
                    config.betterPrefixesNameBold = value;
                    changed = true;
                }
            );

            nameColorBtn = createButton(
                controlX,
                colorY,
                () -> config.betterPrefixesNameColor,
                value -> {
                    config.betterPrefixesNameColor = value;
                    changed = true;
                }
            );

            nameColorSlider = createColorSlider(
                sliderX,
                colorY,
                nameCurrentColor,
                rgb -> {
                    nameCurrentColor = rgb & 0xFFFFFF;
                    config.betterPrefixesNameColorRgb = nameCurrentColor;

                    updateHexField(nameHexField, nameCurrentColor);
                    changed = true;
                }
            );

            nameHexField = createHexField(
                hexX,
                colorY,
                nameCurrentColor,
                hex -> applyHexColor(
                    hex,
                    nameColorSlider,
                    rgb -> {
                        nameCurrentColor = rgb & 0xFFFFFF;
                        config.betterPrefixesNameColorRgb = nameCurrentColor;
                    }
                )
            );

            addDrawableChild(nameProtectBtn);
            addDrawableChild(protectedNameField);
            addDrawableChild(nameBoldBtn);
            addDrawableChild(nameColorBtn);
            addDrawableChild(nameColorSlider);
            addDrawableChild(nameHexField);

            block++;
        }

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_donate")) {
            int y = START_Y + block * BLOCK_HEIGHT;
            int colorY = y + COLOR_ROW_OFFSET;

            donateBtn = createButton(
                controlX,
                y,
                config::isBetterPrefixesDonate,
                value -> {
                    config.setBetterPrefixesDonate(value);
                    changed = true;
                }
            );

            donateTextField = createTextField(
                textX,
                y,
                TEXT_FIELD_WIDTH,
                config.betterPrefixesDonateText,
                value -> {
                    config.betterPrefixesDonateText = value;
                    changed = true;
                }
            );

            donateBoldBtn = createBoldButton(
                boldX,
                y,
                () -> config.betterPrefixesDonateBold,
                value -> {
                    config.betterPrefixesDonateBold = value;
                    changed = true;
                }
            );

            donateColorBtn = createButton(
                controlX,
                colorY,
                () -> config.betterPrefixesDonateColor,
                value -> {
                    config.betterPrefixesDonateColor = value;
                    changed = true;
                }
            );

            donateColorSlider = createColorSlider(
                sliderX,
                colorY,
                donateCurrentColor,
                rgb -> {
                    donateCurrentColor = rgb & 0xFFFFFF;
                    config.betterPrefixesDonateColorRgb = donateCurrentColor;

                    updateHexField(donateHexField, donateCurrentColor);
                    changed = true;
                }
            );

            donateHexField = createHexField(
                hexX,
                colorY,
                donateCurrentColor,
                hex -> applyHexColor(
                    hex,
                    donateColorSlider,
                    rgb -> {
                        donateCurrentColor = rgb & 0xFFFFFF;
                        config.betterPrefixesDonateColorRgb = donateCurrentColor;
                    }
                )
            );

            addDrawableChild(donateBtn);
            addDrawableChild(donateTextField);
            addDrawableChild(donateBoldBtn);
            addDrawableChild(donateColorBtn);
            addDrawableChild(donateColorSlider);
            addDrawableChild(donateHexField);

            block++;
        }

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_title")) {
            int y = START_Y + block * BLOCK_HEIGHT;
            int colorY = y + COLOR_ROW_OFFSET;

            titleBtn = createButton(
                controlX,
                y,
                config::isBetterPrefixesTitle,
                value -> {
                    config.setBetterPrefixesTitle(value);
                    changed = true;
                }
            );

            titleTextField = createTextField(
                textX,
                y,
                TEXT_FIELD_WIDTH,
                config.betterPrefixesTitleText,
                value -> {
                    config.betterPrefixesTitleText = value;
                    changed = true;
                }
            );

            titleBoldBtn = createBoldButton(
                boldX,
                y,
                () -> config.betterPrefixesTitleBold,
                value -> {
                    config.betterPrefixesTitleBold = value;
                    changed = true;
                }
            );

            titleColorBtn = createButton(
                controlX,
                colorY,
                () -> config.betterPrefixesTitleColor,
                value -> {
                    config.betterPrefixesTitleColor = value;
                    changed = true;
                }
            );

            titleColorSlider = createColorSlider(
                sliderX,
                colorY,
                titleCurrentColor,
                rgb -> {
                    titleCurrentColor = rgb & 0xFFFFFF;
                    config.betterPrefixesTitleColorRgb = titleCurrentColor;

                    updateHexField(titleHexField, titleCurrentColor);
                    changed = true;
                }
            );

            titleHexField = createHexField(
                hexX,
                colorY,
                titleCurrentColor,
                hex -> applyHexColor(
                    hex,
                    titleColorSlider,
                    rgb -> {
                        titleCurrentColor = rgb & 0xFFFFFF;
                        config.betterPrefixesTitleColorRgb = titleCurrentColor;
                    }
                )
            );

            addDrawableChild(titleBtn);
            addDrawableChild(titleTextField);
            addDrawableChild(titleBoldBtn);
            addDrawableChild(titleColorBtn);
            addDrawableChild(titleColorSlider);
            addDrawableChild(titleHexField);

            block++;
        }

        int starY = START_Y + block * BLOCK_HEIGHT;
        int starColorY = starY + COLOR_ROW_OFFSET;

        starBtn = createButton(
            controlX,
            starY,
            () -> config.betterPrefixesStarEnabled,
            value -> {
                config.betterPrefixesStarEnabled = value;
                changed = true;
            }
        );

        starColorSlider = createColorSlider(
            sliderX,
            starColorY,
            starCurrentColor,
            rgb -> {
                starCurrentColor = rgb & 0xFFFFFF;
                config.betterPrefixesStarColorRgb = starCurrentColor;

                updateHexField(starHexField, starCurrentColor);
                changed = true;
            }
        );

        starHexField = createHexField(
            hexX,
            starColorY,
            starCurrentColor,
            hex -> applyHexColor(
                hex,
                starColorSlider,
                rgb -> {
                    starCurrentColor = rgb & 0xFFFFFF;
                    config.betterPrefixesStarColorRgb = starCurrentColor;
                }
            )
        );

        addDrawableChild(starBtn);
        addDrawableChild(starColorSlider);
        addDrawableChild(starHexField);

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(centerX - 50, height - 32, 100, 25)
                .build()
        );

        updateAllButtons();
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(getter.getAsBoolean() ? "Enabled" : "Disabled"),
                widget -> {
                    boolean enabled = !getter.getAsBoolean();

                    setter.accept(enabled);
                    updateButton(widget, enabled);
                }
            )
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());

        return button;
    }

    private ButtonWidget createBoldButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal("Bold: " + (getter.getAsBoolean() ? "Enabled" : "Disabled")),
                widget -> {
                    boolean enabled = !getter.getAsBoolean();

                    setter.accept(enabled);
                    updateBoldButton(widget, enabled);
                }
            )
            .dimensions(x, y - 3, BOLD_BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateBoldButton(button, getter.getAsBoolean());

        return button;
    }

    private void updateButton(ButtonWidget button, boolean enabled) {
        if (button == null) {
            return;
        }

        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private void updateBoldButton(ButtonWidget button, boolean enabled) {
        if (button == null) {
            return;
        }

        button.setMessage(Text.literal("Bold: " + (enabled ? "Enabled" : "Disabled"))
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF))
        );
    }

    private void updateAllButtons() {
        updateButton(nameProtectBtn, config.isBetterPrefixesNameProtect());
        updateButton(donateBtn, config.isBetterPrefixesDonate());
        updateButton(titleBtn, config.isBetterPrefixesTitle());
        updateButton(starBtn, config.betterPrefixesStarEnabled);

        updateButton(nameColorBtn, config.betterPrefixesNameColor);
        updateButton(donateColorBtn, config.betterPrefixesDonateColor);
        updateButton(titleColorBtn, config.betterPrefixesTitleColor);

        updateBoldButton(nameBoldBtn, config.betterPrefixesNameBold);
        updateBoldButton(donateBoldBtn, config.betterPrefixesDonateBold);
        updateBoldButton(titleBoldBtn, config.betterPrefixesTitleBold);
    }

    private TextFieldWidget createTextField(int x, int y, int fieldWidth, String initialValue, Consumer<String> onChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, fieldWidth, 20, Text.literal(""));

        field.setMaxLength(128);
        field.setText(initialValue == null ? "" : initialValue);
        field.setChangedListener(onChange);
        field.setEditable(true);
        field.setFocusUnlocked(true);

        return field;
    }

    private ColorSlider createColorSlider(int x, int y, int rgb, Consumer<Integer> onColorChange) {
        int hue = rgbToHue(rgb);

        return new ColorSlider(x, y, SLIDER_WIDTH, 20, Text.literal(""), hue / 360.0,
            value -> {
                if (updatingColorControls) {
                    return;
                }

                int newHue = (int) Math.round(value * 360);
                int newRgb = hslToRgb(newHue % 360, 100, 50);

                onColorChange.accept(newRgb & 0xFFFFFF);
            }
        );
    }

    private void updateColorSlider(ColorSlider slider, int rgb) {
        if (slider == null) {
            return;
        }

        updatingColorControls = true;

        try {
            slider.setValue(rgbToHue(rgb & 0xFFFFFF) / 360.0);
        } finally {
            updatingColorControls = false;
        }
    }

    private TextFieldWidget createHexField(int x, int y, int initialRgb, Consumer<String> onHexChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, HEX_WIDTH, 20, Text.literal("#RRGGBB"));

        field.setMaxLength(7);
        field.setText(String.format("#%06X", initialRgb & 0xFFFFFF));

        field.setChangedListener(text -> {
            if (updatingHexField) {
                return;
            }

            String value = text.trim();

            if (value.startsWith("#")) {
                value = value.substring(1);
            }

            if (value.length() != 6 || parseHex(value) == -1) {
                return;
            }

            onHexChange.accept(value);
        });

        field.setEditable(true);
        field.setFocusUnlocked(true);

        return field;
    }

    private void applyHexColor(String hex, ColorSlider slider, Consumer<Integer> onColorChange) {
        int rgb = parseHex(hex);

        if (rgb == -1) {
            return;
        }

        rgb &= 0xFFFFFF;

        onColorChange.accept(rgb);
        changed = true;

        updateColorSlider(slider, rgb);
    }

    private void updateHexField(TextFieldWidget field, int rgb) {
        if (field == null) {
            return;
        }

        String formatted = String.format("#%06X", rgb & 0xFFFFFF);

        if (formatted.equalsIgnoreCase(field.getText())) {
            return;
        }

        updatingHexField = true;

        try {
            field.setText(formatted);
        } finally {
            updatingHexField = false;
        }
    }

    private int parseHex(String hex) {
        if (hex == null) {
            return -1;
        }

        hex = hex.trim();

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (!hex.matches("[0-9a-fA-F]{6}")) {
            return -1;
        }

        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return -1;
        }
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

        return Math.round(hue) % 360;
    }

    private int hslToRgb(int h, int s, int l) {
        float hue = (h % 360) / 360.0f;
        float sat = s / 100.0f;
        float light = l / 100.0f;

        float c = (1 - Math.abs(2 * light - 1)) * sat;
        float x = c * (1 - Math.abs((hue * 6) % 2 - 1));
        float m = light - c / 2;

        float r = 0;
        float g = 0;
        float b = 0;

        if (hue < 1.0f / 6.0f) {
            r = c;
            g = x;
        } else if (hue < 2.0f / 6.0f) {
            r = x;
            g = c;
        } else if (hue < 3.0f / 6.0f) {
            g = c;
            b = x;
        } else if (hue < 4.0f / 6.0f) {
            g = x;
            b = c;
        } else if (hue < 5.0f / 6.0f) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        int red = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue = Math.round((b + m) * 255);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 6;
        int block = 0;

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_name_protect")) {
            int y = START_Y + block * BLOCK_HEIGHT;

            renderLabel(context, leftX, y,
                "Name Protection",
                "Replace your real name with a custom name.", mouseX, mouseY
            );

            renderLabel(context, leftX, y + COLOR_ROW_OFFSET,
                "Name Color",
                "Enable custom color for your name.", mouseX, mouseY
            );

            renderColorPreview(context, nameHexField, nameColorSlider, nameCurrentColor);

            block++;
        }

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_donate")) {
            int y = START_Y + block * BLOCK_HEIGHT;

            renderLabel(context, leftX, y,
                "Donate Prefix",
                "Text displayed before your name.", mouseX, mouseY
            );

            renderLabel(context, leftX, y + COLOR_ROW_OFFSET,
                "Donate Color",
                "Enable custom color for the donate prefix.", mouseX, mouseY
            );

            renderColorPreview(context, donateHexField, donateColorSlider, donateCurrentColor);

            block++;
        }

        if (!LiteApiManager.isFeatureBlocked("better_prefixes_title")) {
            int y = START_Y + block * BLOCK_HEIGHT;

            renderLabel(context, leftX, y,
                "Title",
                "Text displayed after your name.", mouseX, mouseY
            );

            renderLabel(context, leftX, y + COLOR_ROW_OFFSET,
                "Title Color",
                "Enable custom color for the title.", mouseX, mouseY
            );

            renderColorPreview(context, titleHexField, titleColorSlider, titleCurrentColor);

            block++;
        }

        int starY = START_Y + block * BLOCK_HEIGHT;

        renderLabel(context, leftX, starY,
            "Star",
            "Bold star before name, donate and title.", mouseX, mouseY
        );

        renderLabel(context, leftX, starY + COLOR_ROW_OFFSET,
            "Star Color",
            "Customize the star color using the slider or HEX.", mouseX, mouseY
        );

        renderColorPreview(context, starHexField, starColorSlider, starCurrentColor);
    }

    private void renderColorPreview(DrawContext context, TextFieldWidget hexField, ColorSlider slider, int rgb) {
        if (hexField == null || slider == null) {
            return;
        }

        int x = hexField.getX() + hexField.getWidth() + 5;
        int y = slider.getY() + 1;

        context.fill(x - 1, y - 1, x + PREVIEW_SIZE + 1, y + PREVIEW_SIZE + 1, 0xFFFFFFFF);
        context.fill(x, y, x + PREVIEW_SIZE, y + PREVIEW_SIZE, 0xFF000000 | (rgb & 0xFFFFFF));
    }

    private void renderLabel(DrawContext context, int x, int y, String title, String description, int mouseX, int mouseY) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x
            && mouseX <= x + titleWidth
            && mouseY >= y
            && mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(description), x, y + 12, 0xFF888888, false);
    }

    private void save() {
        config.betterPrefixesNameColorRgb = nameCurrentColor & 0xFFFFFF;
        config.betterPrefixesDonateColorRgb = donateCurrentColor & 0xFFFFFF;
        config.betterPrefixesTitleColorRgb = titleCurrentColor & 0xFFFFFF;
        config.betterPrefixesStarColorRgb = starCurrentColor & 0xFFFFFF;

        config.save();
        BetterPrefixes.refreshBlockedStatus();

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