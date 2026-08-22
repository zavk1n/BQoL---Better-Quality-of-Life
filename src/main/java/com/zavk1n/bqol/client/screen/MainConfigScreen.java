package com.zavk1n.bqol.client.screen;

import com.zavk1n.bqol.config.BQoLConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public abstract class MainConfigScreen extends Screen {

    protected final Screen parent;
    protected final BQoLConfig config = BQoLConfig.getInstance();

    protected static final int ACCENT_COLOR = 0xFFA3A3FF;
    protected static final int BACKGROUND_COLOR = 0xA6000000;

    protected static final int CONTENT_TOP = 50;
    protected static final int CONTENT_BOTTOM_PADDING = 55;

    private static final float SCROLL_SPEED = 15F;
    private static final float SCROLL_SMOOTHING = 0.18F;

    protected float scrollOffset = 0F;
    protected float targetScrollOffset = 0F;
    protected float maxScroll = 0F;

    private boolean draggingScrollbar = false;
    private float scrollbarGrabOffset = 0F;

    private final Random random = new Random();
    private final Fragment[] fragments = new Fragment[35];

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

        clampScroll();
    }

    /// Фрагменты
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

            fragments[i] = new Fragment(x, y,
                0.18F + random.nextFloat() * 0.10F,
                random.nextFloat() * ((float) Math.PI * 2F),
                0.008F + random.nextFloat() * 0.008F
            );
        }
    }

    private Fragment createFragment(boolean randomY) {
        return new Fragment(
            random.nextFloat() * width,

            randomY ? random.nextFloat() * height : -10F,

            0.18F + random.nextFloat() * 0.10F,

            random.nextFloat() * ((float) Math.PI * 2F),

            0.02F + random.nextFloat() * 0.015F
        );
    }

    private static void drawFragment(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x * x + y * y > radius * radius) {
                    continue;
                }

                context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
            }
        }
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

    /// Рендер
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BACKGROUND_COLOR);

        syncMovementKeys();

        drawFragments(context);

        updateScroll();

        super.render(context, mouseX, mouseY, delta);

        int titleColor = mouseX >= width / 2 - 100 &&
            mouseX <= width / 2 + 100 &&
            mouseY >= 20 &&
            mouseY <= 40 ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 30, titleColor);

        renderScrollbar(context);
    }

    protected int getContentTop() {
        return CONTENT_TOP;
    }

    protected int getContentBottom() {
        return height - CONTENT_BOTTOM_PADDING;
    }

    protected int getContentHeight() {
        return getContentBottom() - getContentTop();
    }

    /// Скролл
    protected void updateScroll() {
        float difference = targetScrollOffset - scrollOffset;

        if (Math.abs(difference) < 0.05F) {
            scrollOffset = targetScrollOffset;
            return;
        }

        scrollOffset += difference * SCROLL_SMOOTHING;
        scrollOffset = MathHelper.clamp(scrollOffset, 0F, maxScroll);
    }

    protected void setMaxScroll(float maxScroll) {
        this.maxScroll = Math.max(0F, maxScroll);

        clampScroll();
    }

    protected void clampScroll() {
        scrollOffset = MathHelper.clamp(scrollOffset, 0F, maxScroll);
        targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0F, maxScroll);
    }

    protected void resetScroll() {
        scrollOffset = 0F;
        targetScrollOffset = 0F;
    }

    /// СкроллБар
    protected int getScrollbarX() {
        int buttonsStartX = width / 2 + 50;

        int buttonWidth = 80;
        int buttonGap = 10;

        int configRight = buttonsStartX + buttonWidth + buttonGap + buttonWidth;

        return configRight + 12;
    }

    protected int getScrollbarThumbHeight() {
        int trackHeight = getContentBottom() - getContentTop();

        if (trackHeight <= 0) {
            return 8;
        }

        int contentHeight = trackHeight + (int) maxScroll;

        if (contentHeight <= 0) {
            return trackHeight / 4;
        }

        int normalHeight = (int) ((float) trackHeight / contentHeight * trackHeight);

        return Math.max(7, normalHeight / 4);
    }

    protected int getScrollbarThumbY() {
        int top = getContentTop();
        int bottom = getContentBottom();

        int thumbHeight = getScrollbarThumbHeight();
        int maxThumbY = bottom - thumbHeight;

        if (maxScroll <= 0F || maxThumbY <= top) {
            return top;
        }

        float progress = scrollOffset / maxScroll;

        return top + (int) (progress * (maxThumbY - top));
    }

    protected void renderScrollbar(DrawContext context) {
        if (maxScroll <= 0F) {
            return;
        }

        int top = getContentTop();
        int bottom = getContentBottom();

        int scrollbarX = getScrollbarX();

        int thumbHeight = getScrollbarThumbHeight();
        int thumbY = getScrollbarThumbY();

        context.fill(scrollbarX, top, scrollbarX + 10, bottom, 0x99000000);
        context.fill(scrollbarX, thumbY, scrollbarX + 10, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScroll <= 0F) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        targetScrollOffset -= (float) amount * SCROLL_SPEED;
        targetScrollOffset =MathHelper.clamp(targetScrollOffset, 0F, maxScroll);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll > 0F) {
            int scrollbarX = getScrollbarX();
            int thumbY = getScrollbarThumbY();
            int thumbHeight = getScrollbarThumbHeight();

            if (mouseX >= scrollbarX &&
                    mouseX <= scrollbarX + 10 &&
                    mouseY >= thumbY &&
                    mouseY <= thumbY + thumbHeight) {
                draggingScrollbar = true;
                scrollbarGrabOffset = (float) mouseY - thumbY;

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar && button == 0 && maxScroll > 0F) {
            int top = getContentTop();
            int bottom = getContentBottom();

            int thumbHeight = getScrollbarThumbHeight();
            int maxThumbY = bottom - thumbHeight;

            float newThumbY = (float) mouseY - scrollbarGrabOffset;

            newThumbY = MathHelper.clamp(newThumbY, top, maxThumbY);

            float progress;

            if (maxThumbY <= top) {
                progress = 0F;
            } else {
                progress = (newThumbY - top) / (float) (maxThumbY - top);
            }

            targetScrollOffset = progress * maxScroll;

            scrollOffset = targetScrollOffset;

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            scrollbarGrabOffset = 0F;

            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /// Отслеживание клавиш
    private void syncMovementKeys() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        long handle = client.getWindow().getHandle();

        syncKey(client.options.forwardKey, GLFW.GLFW_KEY_W, handle);
        syncKey(client.options.backKey, GLFW.GLFW_KEY_S, handle);
        syncKey(client.options.leftKey, GLFW.GLFW_KEY_A, handle);
        syncKey(client.options.rightKey, GLFW.GLFW_KEY_D, handle);

        syncKey(client.options.jumpKey, GLFW.GLFW_KEY_SPACE, handle);
        syncKey(client.options.sneakKey, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT, handle);

        syncKey(client.options.sprintKey, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL, handle);
    }

    private void syncKey(KeyBinding keyBinding, int key, long windowHandle) {
        boolean pressed = GLFW.glfwGetKey(windowHandle, key) == GLFW.GLFW_PRESS;

        keyBinding.setPressed(pressed);
    }

    private void syncKey(KeyBinding keyBinding, int key1, int key2, long windowHandle) {
        boolean pressed = GLFW.glfwGetKey(windowHandle, key1) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(windowHandle, key2) == GLFW.GLFW_PRESS;

        keyBinding.setPressed(pressed);
    }

    /// Пауза
    @Override
    public boolean shouldPause() {
        return false;
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