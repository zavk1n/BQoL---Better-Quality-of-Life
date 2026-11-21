package com.zavk1n.bqol.client.screen.featurescreen.mine;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.mine.BetterMine;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FunTimeMineConfigScreen extends MainConfigScreen {
    private ButtonWidget mineHelperButton;

    private boolean changed = false;

    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;

    public FunTimeMineConfigScreen(Screen parent) {
        super(Text.literal("Better Mine FunTime Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_mine_funtime")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        mineHelperButton = null;

        int rightX = width / 2 + 50;

        if (!LiteApiManager.isFeatureBlocked("better_mine_funtime_mine_helper")) {
            mineHelperButton = ButtonWidget.builder(
                    Text.literal(config.isFunTimeMineHelperEnabled() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean enabled = !config.isFunTimeMineHelperEnabled();

                        config.setFunTimeMineHelperEnabled(enabled);

                        changed = true;

                        updateMineHelperButton(enabled);
                        save();
                    }
                )
                .dimensions(rightX, 57, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(mineHelperButton);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (mineHelperButton != null) {
            updateMineHelperButton(config.isFunTimeMineHelperEnabled());
        }
    }

    private void updateMineHelperButton(boolean enabled) {
        if (mineHelperButton == null) {
            return;
        }

        mineHelperButton.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(style -> style.withColor(enabled ? ACCENT_COLOR : 0xFFFFFFFF))
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;

        if (!LiteApiManager.isFeatureBlocked("better_mine_funtime_mine_helper")) {
            renderLabel(context, leftX, 60, mouseX, mouseY,
                "Mine Helper", "Подробно анализирует текущую шахту и оценивает ее."
            );
        }
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

    private void save() {
        config.save();

        BetterMine.refreshBlockedStatus();

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