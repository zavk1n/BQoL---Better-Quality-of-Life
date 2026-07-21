package com.zavk1n.bqol.client.screen.featurescreen.utils;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Consumer;

public class ColorSlider extends SliderWidget {

    private final Consumer<Double> onValueChange;

    public ColorSlider(int x, int y, int width, int height, Text msg, double value, Consumer<Double> onChange) {
        super(x, y, width, height, msg, Math.max(0.0, Math.min(1.0, value)));
        this.onValueChange = Objects.requireNonNull(onChange, "onChange");
    }

    @Override
    protected void updateMessage() {}

    @Override
    protected void applyValue() {
        onValueChange.accept(value);
    }

    public void setValue(double value) {
        this.value = Math.max(0.0, Math.min(1.0, value));
        applyValue();
        updateMessage();
    }
}
// v1.0