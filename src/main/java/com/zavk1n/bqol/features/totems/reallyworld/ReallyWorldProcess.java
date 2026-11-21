package com.zavk1n.bqol.features.totems.reallyworld;

import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.totems.BetterTotems;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class ReallyWorldProcess {

    public ReallyWorldProcess(BetterTotems betterTotems, MainConfig config) {
        this.betterTotems = betterTotems;
        this.config = config;
        this.totemMatches = createTotemMatches();
    }

    public final BetterTotems betterTotems;
    public final MainConfig config;

    public static List<TotemMatch> totemMatches;
    public class TotemMatch {
        public final java.util.function.BooleanSupplier enabled;
        public final String name;
        final List<String> hiddenLines;
        final BetterTotems.NameSpec nameSpec;
        final List<BetterTotems.TextLine> outputLines;

        private TotemMatch(java.util.function.BooleanSupplier enabled, String name, List<String> hiddenLines, BetterTotems.NameSpec nameSpec, List<BetterTotems.TextLine> outputLines) {
            this.enabled = enabled;
            this.name = name;
            this.hiddenLines = List.copyOf(hiddenLines);
            this.nameSpec = nameSpec;
            this.outputLines = List.copyOf(outputLines);
        }
    }

    private static final int RED = 0xFF5555;
    private static final int REDMINIHEADER = 0xFF7A7A;
    private static final int REDSTROKE = 0xFF9E9E;

    private static final int PURPLE = 0x8A52CC;
    private static final int PURPLEMINIHEADER = 0x8F69BF;
    private static final int PURPLESTROKE = 0xA98FCC;

    private List<TotemMatch> createTotemMatches() {
        return List.of(
            new TotemMatch(config::isTotemDrakonid, "Тотем Драконида",
                List.of(
                    "Срабатывает, когда",
                    "лежит в инвентаре"
                ),
                BetterTotems.NameSpec.color(RED),
                List.of(BetterTotems.TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    BetterTotems.TextLine.of("Срабатывает, когда", REDSTROKE, false),
                    BetterTotems.TextLine.of("лежит в инвентаре", REDSTROKE, false)
                )
            ),
            new TotemMatch(config::isTotemWitch, "Тотем Ведьмы",
                List.of(
                    "Сохраняет эффекты"
                ),
                BetterTotems.NameSpec.color(PURPLE),
                List.of(BetterTotems.TextLine.of("Пассивные эффекты:", PURPLEMINIHEADER, true),
                    BetterTotems.TextLine.of("Сохраняет эффекты", PURPLESTROKE, false)
                )
            )
        );
    }

    public static void applyTotemParameters(ItemStack stack, List<Text> lines, TotemMatch match) {
        int nameIndex = BetterTotems.findLineIndex(lines, match.name);

        if (nameIndex == -1) {
            return;
        }

        if (!match.hiddenLines.isEmpty()) {
            lines.removeIf(line -> {
                if (line == null) {
                    return false;
                }

                String text = line.getString();

                for (String hidden : match.hiddenLines) {
                    if (text.contains(hidden)) {
                        return true;
                    }
                }

                return false;
            });
        }

        nameIndex = BetterTotems.findLineIndex(lines, match.name);

        if (nameIndex == -1) {
            return;
        }

        applyTotemNames(stack, match.nameSpec);

        int index = nameIndex + 1;

        for (BetterTotems.TextLine line : match.outputLines) {
            lines.add(index++, line.create());
        }
    }

    public static void applyTotemNames(ItemStack stack, BetterTotems.NameSpec spec) {
        if (spec == null || spec.type == BetterTotems.NameSpecType.NONE) {
            return;
        }

        switch (spec.type) {
            case COLOR -> BetterTotems.setNameColor(stack, spec.color1);
            case ALTERNATING -> BetterTotems.setAlternatingNameColors(stack, spec.color1, spec.color2);
            case SEGMENTED -> BetterTotems.setSegmentedName(stack, spec.segments);
            case RENAME -> {
                if (spec.color1 != null) {
                    BetterTotems.setNameColor(stack, spec.color1);
                }
                BetterTotems.renamePreservingStyle(stack, spec.newName);
            }
            case NONE -> { }
        }
    }

    public static Style getReallyWorldNameStyle(Text name) {
        return (name.getStyle() != null ? name.getStyle() : Style.EMPTY)
            .withBold(true)
            .withItalic(false);
    }
}