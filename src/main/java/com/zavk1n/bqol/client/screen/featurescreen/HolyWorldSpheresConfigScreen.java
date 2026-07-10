package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.BetterSpheres;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HolyWorldSpheresConfigScreen extends MainConfigScreen {

    /// Записи
    private final List<SphereToggleEntry> sphereEntries = new ArrayList<>();

    private static class SphereToggleEntry {
        final String label;
        final int labelX, labelY;
        final ColorCheckbox checkbox;

        SphereToggleEntry(String l, int lx, int ly, ColorCheckbox c) {
            label = l; labelX = lx; labelY = ly; checkbox = c;
        }
    }

    private static class SphereEntry {
        final String displayName;
        final BooleanSupplier enabledGetter;
        final Consumer<Boolean> enabledSetter;

        SphereEntry(String n, BooleanSupplier g, Consumer<Boolean> s) {
            displayName = n; enabledGetter = g; enabledSetter = s;
        }
    }

    private static final String[] FOOTNOTE_LINES = {
        "*Сфера PvP — Броня III + Урон II",
        "*Сфера Шахтера — Спешка III",
        "*Сфера Лива — Скорость III"
    };

    /// Виджеты
    private ButtonWidget coloredParametersBtn, coloredNamesBtn, goldenSpheresBtn;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, COLUMNS = 3,
        COL_SPACING = 130, ROW_SPACING = 48, LABEL_WIDTH = 180,
        CONTROL_SPACING = 45, BUTTON_OFFSET = 70;

    private boolean changed = false;

    /// Конструктор
    public HolyWorldSpheresConfigScreen(Screen parent) {
        super(Text.literal("HolyWorld Spheres Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_spheres_holyworld")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    /// Ядро создания экрана
    private void rebuildUI() {
        clearChildren();
        sphereEntries.clear();

        coloredParametersBtn = null;
        coloredNamesBtn = null;
        goldenSpheresBtn = null;

        createSphereGrid();
        createControlButtons();
        createFooter();

        updateAllButtons();
    }

    private void createSphereGrid() {
        int gridWidth = COLUMNS * COL_SPACING;
        int startX = (width - gridWidth) / 2;
        int gridStartY = 50;

        List<SphereEntry> spheres = List.of(
            new SphereEntry("Сфера Цербера", config::isSphereCerberusEnabled, config::setSphereCerberusEnabled),
            new SphereEntry("Сфера Eternity", config::isSphereEternityEnabled, config::setSphereEternityEnabled),
            new SphereEntry("Сфера Флеша", config::isSphereFleshEnabled, config::setSphereFleshEnabled),
            new SphereEntry("Сфера Armortality", config::isSphereArmortalityEnabled, config::setSphereArmortalityEnabled),
            new SphereEntry("Сфера Stinger", config::isSphereStingerEnabled, config::setSphereStingerEnabled),
            new SphereEntry("Сфера Immortality", config::isSphereImmortalityEnabled, config::setSphereImmortalityEnabled),
            new SphereEntry("Обычная сфера", config::isSphereDefaultEnabled, config::setSphereDefaultEnabled),
            new SphereEntry("Эпическая сфера", config::isSphereEpicEnabled, config::setSphereEpicEnabled),
            new SphereEntry("Легендарная сфера", config::isSphereLegendaryEnabled, config::setSphereLegendaryEnabled),
            new SphereEntry("Сфера Шахтера", config::isSphereMinerEnabled, config::setSphereMinerEnabled),
            new SphereEntry("Сфера PvP", config::isSpherePvPEnabled, config::setSpherePvPEnabled),
            new SphereEntry("Сфера Лива", config::isSphereLeaveEnabled, config::setSphereLeaveEnabled),
            new SphereEntry("Мифическая сфера", config::isSphereMythicEnabled, config::setSphereMythicEnabled)
        );

        int col = 0;
        int row = 0;

        for (int i = 0; i < spheres.size(); i++) {
            SphereEntry entry = spheres.get(i);

            if (i == spheres.size() - 1) {
                col = 1;
            }

            int x = startX + col * COL_SPACING;
            int y = gridStartY + row * ROW_SPACING;

            int labelWidth = textRenderer.getWidth(entry.displayName);
            int labelX = x + (COL_SPACING - labelWidth) / 2;

            ColorCheckbox checkbox = new ColorCheckbox(
                x + (COL_SPACING - 15) / 2,
                y + 20,
                Text.empty(),
                entry.enabledGetter.getAsBoolean(),
                button -> {
                    boolean enabled = !entry.enabledGetter.getAsBoolean();

                    entry.enabledSetter.accept(enabled);
                    ((ColorCheckbox) button).setChecked(enabled);

                    changed = true;
                });

            checkbox.setColor(ACCENT_COLOR);

            sphereEntries.add(
                new SphereToggleEntry(
                    entry.displayName,
                    labelX,
                    y,
                    checkbox
                )
            );

            addDrawableChild(checkbox);

            col++;

            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private void createControlButtons() {
        int gridStartY = 50;
        int maxRow = (sphereEntries.size() + COLUMNS - 1) / COLUMNS;
        int afterGridY = gridStartY + maxRow * ROW_SPACING + 20;

        int maxFootnoteWidth = 0;

        for (String line : FOOTNOTE_LINES) {
            maxFootnoteWidth = Math.max(maxFootnoteWidth, textRenderer.getWidth(line));
        }

        int controlsBlockWidth = LABEL_WIDTH + BUTTON_OFFSET + BUTTON_WIDTH;
        int groupWidth = maxFootnoteWidth + 40 + controlsBlockWidth;
        int groupX = (width - groupWidth) / 2;

        int controlsX = groupX + maxFootnoteWidth + 40;
        int controlsStartY = afterGridY + 10;

        if (!LiteApiManager.isFeatureBlocked("better_spheres_parameters")) {
            coloredParametersBtn = createButton(
                controlsX + LABEL_WIDTH + BUTTON_OFFSET,
                controlsStartY,
                config::isColoredParametersEnabled,
                config::setColoredParametersEnabled
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_spheres_names")) {
            coloredNamesBtn = createButton(
                controlsX + LABEL_WIDTH + BUTTON_OFFSET,
                controlsStartY + CONTROL_SPACING,
                config::isColoredNamesEnabled,
                config::setColoredNamesEnabled
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_spheres_golden")) {
            goldenSpheresBtn = createSpecialButton(
                controlsX + LABEL_WIDTH + BUTTON_OFFSET,
                controlsStartY + CONTROL_SPACING * 2,
                config::isGoldenSpheresEnabled,
                config::setGoldenSpheresEnabled
            );
        }
    }

    private ButtonWidget createButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(""),
                btn -> {
                    boolean enabled = !getter.getAsBoolean();

                    setter.accept(enabled);
                    changed = true;

                    updateButton(btn, enabled);

                    if (btn == coloredNamesBtn) {
                        updateGoldenSpheresButton();
                    }

                    save();
                })
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        updateButton(button, getter.getAsBoolean());

        addDrawableChild(button);

        return button;
    }

    private ButtonWidget createSpecialButton(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                Text.literal(""),
                btn -> {
                    boolean enabled = !getter.getAsBoolean();

                    setter.accept(enabled);
                    changed = true;

                    updateGoldenSpheresButton();

                    save();
                })
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

        addDrawableChild(button);

        updateGoldenSpheresButton();

        return button;
    }

    private void createFooter() {
        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Save & Back"),
                    button -> close()
                )
                .dimensions(width / 2 - 50, height - 40, 100, 25)
                .build()
        );
    }


    /// Обновление состояния кнопок
    private void updateButton(ButtonWidget btn, boolean enabled) {
        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled").styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    private void updateAllButtons() {
        if (coloredParametersBtn != null) {
            updateButton(coloredParametersBtn, config.isColoredParametersEnabled());
        }

        if (coloredNamesBtn != null) {
            updateButton(coloredNamesBtn, config.isColoredNamesEnabled());
        }

        updateGoldenSpheresButton();
    }

    private void updateGoldenSpheresButton() {
        if (goldenSpheresBtn == null) {
            return;
        }

        updateButton(
            goldenSpheresBtn,
            config.isGoldenSpheresEnabled()
        );
    }

    /// Создание названий и описаний
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        renderSphereLabels(context, mouseX, mouseY);
        renderFooter(context, mouseX, mouseY);
    }

    private void renderSphereLabels(DrawContext context, int mouseX, int mouseY) {
        for (SphereToggleEntry entry : sphereEntries) {

            int titleWidth = textRenderer.getWidth(entry.label);

            boolean hovered = mouseX >= entry.labelX &&
                    mouseX <= entry.labelX + titleWidth &&
                    mouseY >= entry.labelY &&
                    mouseY <= entry.labelY + textRenderer.fontHeight;

            int color = hovered
                ? ACCENT_COLOR
                : 0xFFFFFFFF;

            context.drawText(
                textRenderer,
                Text.literal(entry.label),
                entry.labelX,
                entry.labelY,
                color,
                false
            );
        }
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY) {
        int gridStartY = 50;
        int maxRow = (sphereEntries.size() + COLUMNS - 1) / COLUMNS;
        int afterGridY = gridStartY + maxRow * ROW_SPACING + 20;

        int maxFootnoteWidth = 0;

        for (String line : FOOTNOTE_LINES) {
            maxFootnoteWidth = Math.max(
                maxFootnoteWidth,
                textRenderer.getWidth(line)
            );
        }

        int controlsBlockWidth = LABEL_WIDTH + BUTTON_OFFSET + BUTTON_WIDTH;
        int groupWidth = maxFootnoteWidth + 40 + controlsBlockWidth;
        int groupX = (width - groupWidth) / 2;

        int footnoteX = groupX;
        int controlsX = footnoteX + maxFootnoteWidth + 40;
        int controlsStartY = afterGridY + 10;

        for (int i = 0; i < FOOTNOTE_LINES.length; i++) {
            context.drawTextWithShadow(
                textRenderer,
                Text.literal(FOOTNOTE_LINES[i]),
                footnoteX,
                controlsStartY + i * 16,
                0xFFAAAAAA
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_spheres_parameters")) {
            renderLabel(context, controlsX, controlsStartY, mouseX, mouseY,
                "Colored Parameters",
                "Customization of parameters for all spheres."
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_spheres_names")) {
            renderLabel(context, controlsX, controlsStartY + CONTROL_SPACING, mouseX, mouseY,
                "Colored Names",
                "Customize of names for all spheres."
            );
        }

        if (!LiteApiManager.isFeatureBlocked("better_spheres_golden")) {
            renderLabel(context, controlsX, controlsStartY + CONTROL_SPACING * 2, mouseX, mouseY,
                "Golden Spheres",
                "Customize unique sphere properties."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String description) {
        if (y + textRenderer.fontHeight < 0 || y > height) {
            return;
        }

        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x && mouseX <= x + titleWidth && mouseY >= y && mouseY <= y + textRenderer.fontHeight;

        int color = hovered
            ? ACCENT_COLOR
            : 0xFFFFFFFF;

        context.drawTextWithShadow(textRenderer, Text.literal(title), x, y, color);
        context.drawTextWithShadow(textRenderer, Text.literal(description), x, y + 12, 0xFF888888);
    }

    /// Сохранение и закрытие
    private void save() {
        config.save();
        BetterSpheres.refreshBlockedStatus();

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