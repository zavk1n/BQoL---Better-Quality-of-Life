package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterSounds;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SoundsConfigScreen2 extends MainConfigScreen {
    private List<SoundsModeEntry> column1 = new ArrayList<>(), column2 = new ArrayList<>(), column3 = new ArrayList<>();

    /// Виджеты
    private ButtonWidget previousButton;

    private int columnWidth, col1X, col2X, col3X;
    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45, COLUMN_GAP = 40, MARGIN_LEFT = 20, MARGIN_RIGHT = 20, BOTTOM_OFFSET = 40;

    private boolean changed = false;

    private static class SoundsModeEntry {
        final String title, description;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        ButtonWidget button;
        SoundsModeEntry(String title, String desc, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.title = title; this.description = desc;
            this.getter = getter; this.setter = setter;
        }
    }

    /// Конструктор
    public SoundsConfigScreen2(Screen parent) {
        super(Text.literal("Better Sounds Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_sounds")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();

        columnWidth = (width - MARGIN_LEFT - MARGIN_RIGHT - 2 * COLUMN_GAP) / 3;

        col1X = MARGIN_LEFT;
        col2X = col1X + columnWidth + COLUMN_GAP;
        col3X = col2X + columnWidth + COLUMN_GAP;

        fillColumns();

        createColumn(column1, col1X);
        createColumn(column2, col2X);
        createColumn(column3, col3X);

        int previousY = height - BOTTOM_OFFSET - BUTTON_HEIGHT - SPACING;

        previousButton = ButtonWidget.builder(
                Text.literal("Previous"),
                button -> {
                    if (changed) {
                        save();
                    }

                    if (client != null) {
                        client.setScreen(new SoundsConfigScreen(this));
                    }
                }
            )
            .dimensions(col2X - BUTTON_WIDTH - 30, previousY - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(previousButton);

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(
                    width / 2 - 50,
                    height - BOTTOM_OFFSET,
                    100,
                    BUTTON_HEIGHT
                )
                .build()
        );

        updateAllButtons();
    }

    /// Работа с колоннами
    private void fillColumns() {
        column1.clear();
        column2.clear();
        column3.clear();

        column1.add(new SoundsModeEntry("Swim Mode", "Disables swim sounds.",
            config::isBetterSoundsSwim, config::setBetterSoundsSwim));
        column1.add(new SoundsModeEntry("Charge Crossbow Mode", "Disables crossbow sounds.",
            config::isBetterSoundsChargeCrossbow, config::setBetterSoundsChargeCrossbow));
        column1.add(new SoundsModeEntry("Bee Mode", "Disables crossbow sounds.",
            config::isBetterSoundsBee, config::setBetterSoundsBee));

        column2.add(new SoundsModeEntry("Enderman Mode", "Disables fall sounds.",
            config::isBetterSoundsEnderman, config::setBetterSoundsEnderman));

        column3.add(new SoundsModeEntry("Fall Mode", "Disables fall sounds.",
            config::isBetterSoundsFall, config::setBetterSoundsFall));
        column3.add(new SoundsModeEntry("Fireworks Mode", "Disables eating sounds.",
            config::isBetterSoundsFireworks, config::setBetterSoundsFireworks));
        column3.add(new SoundsModeEntry("Blaze Mode", "Disables crossbow sounds.",
            config::isBetterSoundsBlaze, config::setBetterSoundsBlaze));
    }

    private void createColumn(List<SoundsModeEntry> column, int columnX) {
        int row = 0;

        for (SoundsModeEntry entry : column) {
            int y = 60 + row * SPACING;

            entry.button = createButton(
                columnX + columnWidth - BUTTON_WIDTH,
                y,
                entry.getter,
                entry.setter
            );

            addDrawableChild(entry.button);

            row++;
        }
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget btn = ButtonWidget.builder(
                Text.literal(""),
                button -> {
                    boolean newState = !getter.getAsBoolean();
                    setter.accept(newState);
                    changed = true;
                    updateButton(button, newState);
                })
            .dimensions(x, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(btn, getter.getAsBoolean());

        return btn;
    }

    /// Обновление состояния кнопок
    private void updateButton(ButtonWidget button, boolean enabled) {
        button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        for (SoundsModeEntry e : column1) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }

        for (SoundsModeEntry e : column2) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }

        for (SoundsModeEntry e : column3) {
            if (e.button != null) {
                updateButton(e.button, e.getter.getAsBoolean());
            }
        }
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        drawColumnText(context, column1, col1X, mouseX, mouseY);
        drawColumnText(context, column2, col2X, mouseX, mouseY);
        drawColumnText(context, column3, col3X, mouseX, mouseY);
    }

    private void drawColumnText(DrawContext context, List<SoundsModeEntry> column, int columnX, int mouseX, int mouseY) {
        int row = 0;

        for (SoundsModeEntry entry : column) {
            int y = 60 + row * SPACING;

            renderLabel(context, columnX, y, mouseX, mouseY, entry.title, entry.description);

            row++;
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

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterSounds.refreshBlockedStatus();
        BetterSounds.reloadFromConfig();

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