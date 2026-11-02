package com.zavk1n.bqol.client.screen.featurescreen.totems;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.client.screen.featurescreen.utils.ColorCheckbox;
import com.zavk1n.bqol.features.BetterTotems;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HolyWorldTotemsConfigScreen extends MainConfigScreen {
    private final List<TotemsToggleEntry> totemsEntries = new ArrayList<>();

    private static final int COLUMNS = 3, COL_SPACING = 130, ROW_SPACING = 48;

    private boolean changed = false;

    private static class TotemsToggleEntry {
        final String label;
        final int labelX;
        final int labelY;
        final ColorCheckbox checkbox;

        TotemsToggleEntry(String label, int labelX, int labelY, ColorCheckbox checkbox) {
            this.label = label;
            this.labelX = labelX;
            this.labelY = labelY;
            this.checkbox = checkbox;
        }
    }

    private static class TotemsEntry {
        final String displayName;
        final BooleanSupplier enabledGetter;
        final Consumer<Boolean> enabledSetter;

        TotemsEntry(
            String displayName,
            BooleanSupplier enabledGetter,
            Consumer<Boolean> enabledSetter
        ) {
            this.displayName = displayName;
            this.enabledGetter = enabledGetter;
            this.enabledSetter = enabledSetter;
        }
    }

    public HolyWorldTotemsConfigScreen(Screen parent) {
        super(Text.literal("HolyWorld Totems Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_totems_holyworld")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();
        totemsEntries.clear();

        createTotemsGrid();

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );
    }

    private void createTotemsGrid() {
        final int gridStartY = 60;

        List<TotemsEntry> totems = List.of(
            new TotemsEntry("Totem Infinity", config::isTotemInfinity, config::setTotemInfinity),
            new TotemsEntry("Totem Eternity", config::isTotemEternity, config::setTotemEternity),
            new TotemsEntry("Totem Stinger", config::isTotemStinger, config::setTotemStinger),
            new TotemsEntry("Totem Satires", config::isTotemSatires, config::setTotemSatires)
        );

        int totalRows = (totems.size() + COLUMNS - 1) / COLUMNS;

        for (int i = 0; i < totems.size(); i++) {

            TotemsEntry entry = totems.get(i);

            int row = i / COLUMNS;
            int col = i % COLUMNS;

            int itemsInRow = (row == totalRows - 1) ? totems.size() - row * COLUMNS : COLUMNS;

            int rowWidth = itemsInRow * COL_SPACING;

            int startX = (width - rowWidth) / 2;

            int x = startX + col * COL_SPACING;
            int y = gridStartY + row * ROW_SPACING;

            int labelWidth = textRenderer.getWidth(entry.displayName);

            int labelX = x + (COL_SPACING - labelWidth) / 2;

            ColorCheckbox checkbox = new ColorCheckbox(x + (COL_SPACING - 15) / 2, y + 20, Text.empty(), entry.enabledGetter.getAsBoolean(),
                button -> {
                    boolean enabled = !entry.enabledGetter.getAsBoolean();
                    entry.enabledSetter.accept(enabled);
                    ((ColorCheckbox) button).setChecked(enabled);
                    changed = true;
                }
            );

            checkbox.setColor(ACCENT_COLOR);

            totemsEntries.add(new TotemsToggleEntry(entry.displayName, labelX, y, checkbox));

            addDrawableChild(checkbox);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (TotemsToggleEntry entry : totemsEntries) {
            int titleWidth = textRenderer.getWidth(entry.label);

            boolean hovered = mouseX >= entry.labelX &&
                mouseX <= entry.labelX + titleWidth &&
                mouseY >= entry.labelY &&
                mouseY <= entry.labelY + textRenderer.fontHeight;

            int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

            context.drawText(textRenderer, Text.literal(entry.label), entry.labelX, entry.labelY, color, false);
        }
    }

    private void save() {
        config.save();

        BetterTotems.refreshBlockedStatus();

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