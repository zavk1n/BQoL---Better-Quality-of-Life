package com.zavk1n.bqol.client.screen;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class MainConfigScreen extends Screen {
    protected final Screen parent;
    protected final BQoLConfig config = BQoLConfig.getInstance();
    protected static final int ACCENT_COLOR = 0xFFA3A3FF;
    protected static final int BACKGROUND_COLOR = 0x99101010;

    protected MainConfigScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BACKGROUND_COLOR);
        int titleColor = (mouseX >= width/2-120 && mouseX <= width/2+120 && mouseY >= 20 && mouseY <= 40)
                ? ACCENT_COLOR : 0xFFFFFFFF;
        context.drawCenteredTextWithShadow(textRenderer, title, width/2, 25, titleColor);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}