package com.zavk1n.bqol.client.screen.featurescreen.utils;

import  net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ColorCheckbox extends ButtonWidget {

    private boolean checked;
    private static final int BORDER = 2;
    private int customColor = 0xFF8A8AFF;

    public ColorCheckbox(int x, int y, Text msg, boolean checked, PressAction onPress) {
        super(x, y, 15, 15, msg, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setColor(int rgb) {
        this.customColor = (rgb & 0x00FFFFFF) | 0xFF000000;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);

        if (checked) {
            context.fill(getX() + BORDER, getY() + BORDER, getX() + width - BORDER, getY() + height - BORDER, customColor);
        }
    }
}
// v1.0