package com.zavk1n.bqol.client.screen;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.Random;

public abstract class MainConfigScreen extends Screen {

    protected final Screen parent;
    protected final BQoLConfig config = BQoLConfig.getInstance();

    protected static final int ACCENT_COLOR = 0xFFA3A3FF;
    protected static final int BACKGROUND_COLOR = 0xA6000000;

    private static final int MAX_FRAGMENTS = 35;

    private final Random random = new Random();
    private final Fragment[] fragments = new Fragment[MAX_FRAGMENTS];

    private boolean fragmentsInitialized = false;

    private static final class Fragment {
        float x;
        float y;

        float speed;

        float waveOffset;
        float waveSpeed;

        Fragment(
            float x,
            float y,
            float speed,
            float waveOffset,
            float waveSpeed
        ) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.waveOffset = waveOffset;
            this.waveSpeed = waveSpeed;
        }
    }

    /// Конструктор
    protected MainConfigScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        if (!fragmentsInitialized) {
            initFragments();
            fragmentsInitialized = true;
        }
    }

    /// Отрисовка
    private void initFragments() {
        int columns = 5;
        int rows = 7;

        float cellWidth = (float) width / columns;
        float cellHeight = (float) height / rows;

        for (int i = 0; i < fragments.length; i++) {
            int column = i % columns;
            int row = i / columns;

            float x = column * cellWidth + random.nextFloat() * cellWidth;
            float y = row * cellHeight + random.nextFloat() * cellHeight;

            fragments[i] = new Fragment(
                x,
                y,
                0.18F + random.nextFloat() * 0.10F,
                random.nextFloat() * ((float) Math.PI * 2F),
                0.008F + random.nextFloat() * 0.008F
            );
        }
    }

    private Fragment createFragment(boolean randomY) {
        return new Fragment(
            random.nextFloat() * width,

            randomY
                ? random.nextFloat() * height
                : -10F,

            0.18F + random.nextFloat() * 0.10F,

            random.nextFloat() * ((float)Math.PI * 2F),

            0.02F + random.nextFloat() * 0.015F
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BACKGROUND_COLOR);

        drawFragments(context);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 30, 0xFFFFFFFF);

        int titleColor = mouseX >= width / 2 - 100 &&
                mouseX <= width / 2 + 100 &&
                mouseY >= 25 &&
                mouseY <= 45
                ? ACCENT_COLOR
                : 0xFFFFFFFF;

        context.drawCenteredTextWithShadow(
            textRenderer,
            title,
            width / 2,
            30,
            titleColor
        );
    }

    private void drawFragments(DrawContext context) {
        if (!fragmentsInitialized) {
            return;
        }

        float time = Util.getMeasuringTimeMs() * 0.001F;

        for (int i = 0; i < fragments.length; i++) {
            Fragment fragment = fragments[i];

            fragment.y += fragment.speed;

            float drawX = fragment.x + (float) Math.sin(time * (fragment.waveSpeed * 10F) + fragment.waveOffset) * 2F;

            if (fragment.y >= height + 10) {
                fragments[i] = createFragment(false);
                continue;
            }

            drawFragment(context, (int) drawX, (int) fragment.y, 3, 0xAAFFFFFF);
        }
    }

    private static void drawFragment(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x * x + y * y > radius * radius) {
                    continue;
                }

                context.fill(
                    centerX + x,
                    centerY + y,
                    centerX + x + 1,
                    centerY + y + 1,
                    color
                );
            }
        }
    }

    /// Закрытие
    @Override
    public void close() {
        config.save();

        if (client != null) {
            client.setScreen(parent);
        }
    }
}