package com.zavk1n.bqol.features.totems.holyworld;

import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.totems.BetterTotems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HolyWorldProcess {
    public HolyWorldProcess(BetterTotems betterTotems, MainConfig config) {
        this.betterTotems = betterTotems;
        this.config = config;
        this.totemMatches = createTotemMatches();
    }

    public final BetterTotems betterTotems;
    public final MainConfig config;

    private static final class TotemPalette {
        private TotemPalette() {}

        public static final int DEFAULT_NAME = 0xBABABA;
        public static final int EPIC_NAME = 0xEA00FF;
        public static final int LEGENDARY_NAME = 0x0080FF;

        public static final List<Integer> DEFAULT_GRADIENT = List.of(
            0xC2C2C2,
            0xD6D6D6,
            0xE3E3E3
        );

        public static final List<Integer> EPIC_GRADIENT = List.of(
            0xEC17FF,
            0xEF52FF,
            0xF173FF
        );

        public static final List<Integer> LEGENDARY_GRADIENT = List.of(
            0x0092FF,
            0x47B0FF,
            0x7EC6FC,
            0xA3D8FF
        );
    }

    private static final String DAMAGE = "Урон";
    private static final String ARMOR = "Броня";
    private static final String SPEED = "Скорость";
    private static final String HEALTH = "Макс. здоровье";
    private static final String HASTE = "Спешка";
    private static final String ATTACK_SPEED = "Скорость атаки";

    private static final List<String> PARAMETERS = List.of(
        DAMAGE,
        ARMOR,
        SPEED,
        HEALTH,
        HASTE,
        ATTACK_SPEED
    );

    private static final List<String> PREFIXES = List.of(
        "INFINITY",
        "ETERNITY",
        "STINGER",
        "САТИРА",
        "САТИРЫ"
    );

    public static List<TotemMatch> totemMatches;
    public static class TotemMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final List<String> targetLines;
        final List<Integer> colors;

        TotemMatch(
            java.util.function.BooleanSupplier enabled,
            Map<String, Integer> requiredParameters,
            List<String> targetLines,
            List<Integer> colors
        ) {
            this.enabled = enabled;
            this.requiredParameters = Map.copyOf(requiredParameters);
            this.targetLines = List.copyOf(targetLines);
            this.colors = List.copyOf(colors);
        }
    }

    public record TotemInfo(
        TotemType type,
        int nameColor,
        List<Integer> gradient
    ) {}

    public record TotemResult(
        TotemMatch totems
    ) {}

    private enum TotemType {
        DEFAULT,
        EPIC,
        LEGENDARY,
        CUSTOM
    }

    private List<TotemMatch> createTotemMatches() {
        return List.of(
            new TotemMatch(
                config::isTotemInfinity,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2, "Макс. здоровье", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2", "Макс. здоровье 2"),
                List.of(0x00F231, 0x26FF53, 0x63FF84, 0x94FFAB)
            ),
            new TotemMatch(
                config::isTotemEternity,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2"),
                List.of(0xFF008C, 0xFF47A9, 0xFF69B9)
            ),
            new TotemMatch(
                config::isTotemStinger,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 1),
                List.of("Урон 2", "Броня 2", "Скорость 1"),
                List.of(0xFF2600, 0xFF4E2E, 0xFF6E54)
            ),
            new TotemMatch(
                config::isTotemSatires,
                Map.of("Урон", 3, "Спешка", 2),
                List.of("Урон 3", "Спешка 2"),
                List.of(0x00FFFB, 0x87FFFE)
            )
        );
    }

    public static TotemResult searchTotems(Map<String, Integer> parameters) {
        return new TotemResult(findTotems(parameters));
    }

    public static TotemMatch findTotems(Map<String, Integer> parameters) {
        for (TotemMatch match : totemMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (BetterTotems.parametersMatch(parameters, match.requiredParameters)) {
                return match;
            }
        }

        return null;
    }

    public static boolean isCustomTotem(Map<String, Integer> parameters) {
        return BetterTotems.parametersMatch(parameters, Map.of("Урон", 2, "Броня", 2, "Скорость", 2, "Макс. здоровье", 2))
            || BetterTotems.parametersMatch(parameters, Map.of("Урон", 2, "Броня", 2, "Скорость", 2))
            || BetterTotems.parametersMatch(parameters, Map.of("Урон", 2, "Броня", 2, "Скорость", 1))
            || BetterTotems.parametersMatch(parameters, Map.of("Урон", 3, "Спешка", 2));
    }

    public static TotemInfo getTotemsInfo(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String upperName = name.toUpperCase(Locale.ROOT);
        String lowerName = name.toLowerCase(Locale.ROOT);

        for (String keyword : PREFIXES) {
            if (upperName.contains(keyword)) {
                return new TotemInfo(
                    TotemType.CUSTOM,
                    0xFFFFFF,
                    List.of(0xCCCCCC, 0xAAAAAA, 0x888888)
                );
            }
        }

        if (lowerName.contains("легендарн")) {
            return new TotemInfo(
                TotemType.LEGENDARY,
                TotemPalette.LEGENDARY_NAME,
                TotemPalette.LEGENDARY_GRADIENT
            );
        }

        if (lowerName.contains("эпическ")) {
            return new TotemInfo(
                TotemType.EPIC,
                TotemPalette.EPIC_NAME,
                TotemPalette.EPIC_GRADIENT
            );
        }

        if (lowerName.contains("обычн")) {
            return new TotemInfo(
                TotemType.DEFAULT,
                TotemPalette.DEFAULT_NAME,
                TotemPalette.DEFAULT_GRADIENT
            );
        }

        return null;
    }

    public static void applyTotemsParameters(List<Text> lines, Map<String, Integer> parameters, TotemInfo totemsInfo, TotemResult totemResult, TotemMatch totemMatch) {
        if (isCustomTotem(parameters)) {
            if (totemMatch != null) {
                BetterTotems.replaceParameterLines(lines, totemMatch.targetLines, totemMatch.colors);
            }

            return;
        }

        if (totemsInfo == null) {
            return;
        }

        List<String> orderedLines = new ArrayList<>();
        List<Integer> orderedColors = new ArrayList<>();

        int colorIndex = 0;

        for (String parameter : PARAMETERS) {
            if (!parameters.containsKey(parameter)) {
                continue;
            }

            Integer level = parameters.get(parameter);
            if (level == null) {
                continue;
            }

            orderedLines.add(parameter + " " + level);
            orderedColors.add(totemsInfo.gradient().get(colorIndex % totemsInfo.gradient().size()));
            colorIndex++;
        }

        BetterTotems.replaceParameterLines(lines, orderedLines, orderedColors);
    }

    public static void applyTotemsNames(ItemStack stack, String originalName, TotemInfo totemsInfo, TotemResult result) {
        if (originalName != null) {
            String upperName = originalName.toUpperCase(Locale.ROOT);

            if (upperName.contains("INFINITY")
                || upperName.contains("ETERNITY")
                || upperName.contains("STINGER")
                || upperName.contains("САТИРЫ")
                || upperName.contains("САТИРА")) {

                if (result.totems() != null) {}

                return;
            }
        }

        if (totemsInfo == null
            || totemsInfo.type() == TotemType.LEGENDARY
            || totemsInfo.type() == TotemType.CUSTOM) {
            return;
        }

        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(originalName)
            .styled(style -> style
                .withColor(totemsInfo.nameColor())
                .withItalic(false))
        );
    }
}
