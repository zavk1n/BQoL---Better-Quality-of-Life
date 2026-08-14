package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterSpheres {

    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterSpheres() {
        hwSphereMatches = createSphereMatchesHolyWorld();
        hwGoldenSphereMatches = createGoldenSphereMatchesHolyWorld();
        rwMatches = createReallyWorldMatches();
    }

    private static BetterSpheres instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    /// Really World Helpers
    private record NameSegment(String text, int color) {}
    private enum NameSpecType {
        NONE,
        COLOR,
        ALTERNATING,
        SEGMENTED,
        RENAME
    }

    private static final class NameSpec {
        static final NameSpec NONE = new NameSpec(NameSpecType.NONE, null, null, null, List.of());

        final NameSpecType type;
        final Integer color1;
        final Integer color2;
        final String newName;
        final List<NameSegment> segments;

        private NameSpec(
            NameSpecType type,
            Integer color1,
            Integer color2,
            String newName,
            List<NameSegment> segments
        ) {
            this.type = type;
            this.color1 = color1;
            this.color2 = color2;
            this.newName = newName;
            this.segments = segments;
        }

        static NameSpec color(int color) {
            return new NameSpec(NameSpecType.COLOR, color, null, null, List.of());
        }

        static NameSpec alternating(int color1, int color2) {
            return new NameSpec(NameSpecType.ALTERNATING, color1, color2, null, List.of());
        }

        static NameSpec segmented(List<NameSegment> segments) {
            return new NameSpec(NameSpecType.SEGMENTED, null, null, null, List.copyOf(segments));
        }

        static NameSpec rename(String newName) {
            return new NameSpec(NameSpecType.RENAME, null, null, newName, List.of());
        }

        static NameSpec rename(String newName, int color) {
            return new NameSpec(NameSpecType.RENAME, color, null, newName, List.of());
        }
    }

    private static final class TextLine {
        final String text;
        final Integer color;
        final boolean bold;

        private TextLine(String text, Integer color, boolean bold) {
            this.text = text;
            this.color = color;
            this.bold = bold;
        }

        static TextLine plain(String text) {
            return new TextLine(text, null, false);
        }

        static TextLine of(String text, int color, boolean bold) {
            return new TextLine(text, color, bold);
        }

        Text create() {
            MutableText result = Text.literal(text);

            if (color != null) {
                result.styled(style -> style
                    .withColor(color)
                    .withBold(bold)
                    .withItalic(false)
                );
            } else {
                result.styled(style -> style.withItalic(false));
            }

            return result;
        }
    }

    private static final class RWMatch {
        final java.util.function.BooleanSupplier enabled;
        final String name;
        final List<String> hiddenLines;
        final NameSpec nameSpec;
        final List<TextLine> outputLines;

        private RWMatch(java.util.function.BooleanSupplier enabled, String name, List<String> hiddenLines, NameSpec nameSpec, List<TextLine> outputLines) {
            this.enabled = enabled;
            this.name = name;
            this.hiddenLines = List.copyOf(hiddenLines);
            this.nameSpec = nameSpec;
            this.outputLines = List.copyOf(outputLines);
        }
    }

    private static final int WHITE = 0xFFFFFF;

    private static final int RED = 0xFF5555;
    private static final int REDMINIHEADER = 0xFF7A7A;
    private static final int REDSTROKE = 0xFF9E9E;

    private static final int PINK = 0xFF42AA;
    private static final int PINKMINIHEADER = 0xFF6BBC;
    private static final int PINKSTROKE = 0xFF99C8;

    private static final int BLUE = 0x5555FF;
    private static final int BLUEMINIHEADER = 0x8F8FFF;
    private static final int BLUESTROKE = 0xB3B3FF;

    private static final int CYAN = 0x33EFFF;
    private static final int CYANMINIHEADER = 0x8CF4FF;
    private static final int CYANSTROKE = 0xB5F8FF;

    private static final int ORANGE = 0xFF741A;
    private static final int ORANGEMINIHEADER = 0xFFB380;
    private static final int ORANGESTROKE = 0xFF9854;

    private static final int YELLOW = 0xFFFC3D;
    private static final int YELLOWMINIHEADER = 0xFFFEAD;
    private static final int YELLOWSTROKE = 0xFFFD7A;

    private static final int GREEN = 0x3DFF3D;
    private static final int GREENMINIHEADER = 0xADFFAD;
    private static final int GREENSTROKE = 0x80FF80;

    private static final int PURPLE = 0x560C94;
    private static final int PURPLEMINIHEADER = 0xA963FF;
    private static final int PURPLESTROKE = 0x7203FF;

    private static final int GRAY = 0xAAAAAA;
    private static final int GRAYMINIHEADER = 0xD6D6D6;
    private static final int GRAYSTROKE = 0xC2C2C2;

    private static final int BROWN = 0x8B4513;
    private static final int BROWNMINIHEADER = 0xBF895E;
    private static final int BROWNSTROKE = 0xA3673B;

    private static final int GOLD = 0xFFC800;
    private static final int GOLDMINIHEADER = 0xFFD640;
    private static final int GOLDSTROKE = 0xFFE275;

    /// HolyWorld Helpers
    private enum SphereType {
        DEFAULT,
        EPIC,
        LEGENDARY,
        MYTHIC
    }

    private static final class SpherePalette {
        private SpherePalette() {}

        public static final int DEFAULT_NAME = 0xBABABA;
        public static final int EPIC_NAME = 0xEA00FF;
        public static final int LEGENDARY_NAME = 0x0080FF;
        public static final int MYTHIC_NAME = 0xFF0000;
        public static final int GOLDEN_NAME = 0xFFE500;

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

        public static final List<Integer> MYTHIC_GRADIENT = List.of(
            0xD10000,
            0xFF0000,
            0xFF5252,
            0xFC6868
        );
    }

    private static final String DAMAGE = "Урон";
    private static final String ARMOR = "Броня";
    private static final String SPEED = "Скорость";
    private static final String HEALTH = "Макс. здоровье";
    private static final String HASTE = "Спешка";
    private static final String ATTACK_SPEED = "Скорость атаки";
    private static final String VANISHING = "Проклятие утраты";

    private static final List<String> PARAMETERS = List.of(
        DAMAGE,
        ARMOR,
        SPEED,
        HEALTH,
        HASTE,
        ATTACK_SPEED,
        VANISHING
    );

    private static final Map<String, Pattern> PARAMETER_PATTERNS =
        PARAMETERS.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
            p -> p,
            p -> Pattern.compile(
                "^" + Pattern.quote(p) + "(\\s+(\\d+|[IVXLCDM]+))?$"
            )
        ));

    private final List<HWDefaultSphereMatch> hwSphereMatches;
    private static class HWDefaultSphereMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final List<String> targetLines;
        final List<Integer> colors;

        HWDefaultSphereMatch(
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

    private final List<HWGoldenSphereMatch> hwGoldenSphereMatches;
    private static class HWGoldenSphereMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final String newName;
        final List<String> targetLines;
        final List<Integer> colors;

        HWGoldenSphereMatch(
            java.util.function.BooleanSupplier enabled,
            Map<String, Integer> requiredParameters,
            String newName,
            List<String> targetLines,
            List<Integer> colors
        ) {
            this.enabled = enabled;
            this.requiredParameters = Map.copyOf(requiredParameters);
            this.newName = newName;
            this.targetLines = List.copyOf(targetLines);
            this.colors = List.copyOf(colors);
        }
    }

    private record HWSphereInfo(
        SphereType type,
        int nameColor,
        List<Integer> gradient
    ) {}

    private record HWSphereResult(
        HWDefaultSphereMatch hwDefaultSphere,
        HWGoldenSphereMatch hwGoldenSphere
    ) {}

    private final List<RWMatch> rwMatches;

    /// Таблица перевода чисел
    private static final Map<Character, Integer> CONVERTER =
        Map.of(
            'I', 1,
            'V', 5,
            'X', 10,
            'L', 50,
            'C', 100,
            'D', 500,
            'M', 1000
        );

    /// Паттерны поиска кодов цветов и чисел
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORa-fk-or]");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\b(\\d+)\\b");

    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean holyworld;
        boolean golden;
        boolean parameters;
        boolean names;
    }

    private boolean registered;

    /// HOLYWORLD
    private List<HWDefaultSphereMatch> createSphereMatchesHolyWorld() {
        return List.of(
            new HWDefaultSphereMatch(
                config::isSphereStinger,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 1),
                List.of("Урон 2", "Броня 2", "Скорость 1"),
                List.of(0xFF2600, 0xFF4E2E, 0xFF6E54)
            ),
            new HWDefaultSphereMatch(
                config::isSphereEternity,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2"),
                List.of(0xFF008C, 0xFF47A9, 0xFF69B9)
            ),
            new HWDefaultSphereMatch(
                config::isSphereImmortality,
                Map.of("Урон", 3, "Скорость", 2),
                List.of("Урон 3", "Скорость 2"),
                List.of(0x7600ED, 0x6100BA)
            ),
            new HWDefaultSphereMatch(
                config::isSphereArmortality,
                Map.of("Урон", 2, "Броня", 2, "Макс. здоровье", 2),
                List.of("Урон 2", "Броня 2", "Макс. здоровье 2"),
                List.of(0x3A4A78, 0x4E6299, 0x687AB0)
            ),
            new HWDefaultSphereMatch(
                config::isSphereCerberus,
                Map.of("Проклятие утраты", 0, "Урон", 5, "Спешка", 1),
                List.of("Урон 5", "Спешка 1", "Проклятие утраты"),
                List.of(0xCF0000, 0xF70000, 0xFF3333)
            ),
            new HWDefaultSphereMatch(
                config::isSphereFlash,
                Map.of("Проклятие утраты", 0, "Скорость", 3, "Броня", 1),
                List.of("Скорость 3", "Броня 1", "Проклятие утраты"),
                List.of(0xCCF8FF, 0xB0F3FF, 0x8FEEFF)
            )
        );
    }

    private List<HWGoldenSphereMatch> createGoldenSphereMatchesHolyWorld() {
        return List.of(
            new HWGoldenSphereMatch(
                config::isHolyWorldSphereSpeed,
                Map.of("Скорость", 3),
                "Сфера Скорки",
                List.of("Скорость 3"),
                List.of(0xFFE500)
            ),
            new HWGoldenSphereMatch(
                config::isHolyWorldSphereMiner,
                Map.of("Спешка", 3),
                "Сфера Шахтера",
                List.of("Спешка 3"),
                List.of(0xFFE500)
            ),
            new HWGoldenSphereMatch(
                config::isHolyWorldSpherePvP,
                Map.of("Броня", 3, "Урон", 2),
                "Сфера ПвП",
                List.of("Урон 2", "Броня 3"),
                List.of(0xFFE600, 0xFFEC61)
            )
        );
    }

    private void processTooltipHolyWorld(ItemStack stack, List<Text> lines) {
        if (!config.isHolyWorldSpheresEnabled()
            || blocked.holyworld) {
            return;
        }

        Map<String, Integer> parameters = parseParametersFromTooltip(lines);

        if (parameters.isEmpty()) {
            return;
        }

        String originalName = COLOR_PATTERN
            .matcher(lines.get(0).getString())
            .replaceAll("")
            .trim();

        HWSphereInfo hwSphereInfo = getHWSphereInfo(originalName);
        HWSphereResult hwSphereResult = searchHWSpheres(parameters);

        applyHWSpheresParameters(lines, parameters, hwSphereInfo, hwSphereResult);
        applyHWSphereNames(stack, originalName, hwSphereInfo, hwSphereResult);
    }

    /// REALLYWORLD
    private void processTooltipReallyWorld(ItemStack stack, List<Text> lines) {
        if (blocked.main || lines == null || lines.isEmpty()) {
            return;
        }

        List<String> tooltip = lines.stream()
            .filter(Objects::nonNull)
            .map(Text::getString)
            .toList();

        for (RWMatch match : rwMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (!containsLine(tooltip, match.name)) {
                continue;
            }

            applyReallyWorldMatch(stack, lines, match);
        }
    }

    private List<RWMatch> createReallyWorldMatches() {
        return List.of(
            new RWMatch(config::isSphereAir, "Шар Воздуха",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Левитация II (00:08)",
                    "+1.5 Отбрасывание при атаке",
                    "+1.5 Отбрасывание атаки",
                    "+2.3 Урон при атаке",
                    "+2.3 Урон атаки",
                    "+35% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Активируемые эффекты:", WHITE, true),
                    TextLine.of("Левитация II (00:08)", WHITE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", WHITE, true),
                    TextLine.of("+35% Скорость", WHITE, false),
                    TextLine.of("+2.3 Урон", WHITE, false),
                    TextLine.of("+1.5 Отбрасывание", WHITE, false)
                )
            ),
            new RWMatch(config::isSphereFire, "Шар Огня",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Огнеупорность",
                    "+4 Максимальное здоровье",
                    "+4 Макс. здоровье",
                    "+2.5 Урон при атаке",
                    "+2.5 Урон атаки",
                    "+0.3 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.color(ORANGE),
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("Огнеупорность", ORANGESTROKE, false),
                    TextLine.of("+4 Максимальное здоровье", ORANGESTROKE, false),
                    TextLine.of("+2.5 Урон", ORANGESTROKE, false),
                    TextLine.of("+0.3 Скорость атаки", ORANGESTROKE, false)
                )
            ),
            new RWMatch(config::isSphereGround, "Шар Земли",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Спешка II (00:08)",
                    "+4.5 Броня",
                    "+20 Сопротивление к отбрасыванию",
                    "+2 Сопротивление к отбрасыванию",
                    "+4 Твёрдость брони",
                    "+4 Прочность брони",
                    "Проклятие утраты"
                ),
                NameSpec.color(BROWN),
                List.of(
                    TextLine.of("Активируемые эффекты:", BROWNMINIHEADER, true),
                    TextLine.of("Спешка 2 (00:08)", BROWNSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", BROWNMINIHEADER, true),
                    TextLine.of("+4.5 Броня", BROWNSTROKE, false),
                    TextLine.of("+2 Сопротивление к отбрасыванию", BROWNSTROKE, false),
                    TextLine.of("+4 Твёрдость брони", BROWNSTROKE, false)
                )
            ),
            new RWMatch(config::isSphereWater, "Шар Воды",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Подводное дыхание (01:00)",
                    "Грация дельфина (00:10)",
                    "+30% Скорость",
                    "+12 Максимальное здоровье",
                    "+12 Макс. здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(BLUE),
                List.of(
                    TextLine.of("Активируемые эффекты:", BLUEMINIHEADER, true),
                    TextLine.of("Подводное дыхание (01:00)", BLUESTROKE, false),
                    TextLine.of("Грация дельфина (00:10)", BLUESTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", BLUEMINIHEADER, true),
                    TextLine.of("+30% Скорость", BLUESTROKE, false),
                    TextLine.of("+12 Максимальное здоровье", BLUESTROKE, false)
                )
            ),
            new RWMatch(config::isSphereGOD, "ШАР БОГА",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Активируемые эффекты:",
                    "Сопротивление III (00:06)",
                    "Пассивные эффекты:",
                    "+3 Броня",
                    "+2.7 Урон при атаке",
                    "+2.7 Урон атаки",
                    "+25% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.color(GOLD),
                List.of(
                    TextLine.of("Активируемые эффекты:", GOLDMINIHEADER, true),
                    TextLine.of("Сопротивление 3 (00:06)", GOLDSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", GOLDMINIHEADER, true),
                    TextLine.of("+25% Скорость", GOLDSTROKE, false),
                    TextLine.of("+3 Броня", GOLDSTROKE, false),
                    TextLine.of("+2.7 Урон", GOLDSTROKE, false)
                )
            ),
            new RWMatch(config::isSphereCocaCola, "Шар КокаКолы",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+1.5 Урон при атаке",
                    "+0.3 Скорость атаки",
                    "-1.5 Броня",
                    "+30% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.alternating(RED, WHITE),
                List.of(
                    TextLine.of("Пассивные эффекты:", WHITE, true),
                    TextLine.of("+30% Скорость", REDSTROKE, false),
                    TextLine.of("+1.5 Урон", WHITE, false),
                    TextLine.of("+0.3 Скорость атаки", REDSTROKE, false),
                    TextLine.of("-1.5 Броня", WHITE, false)
                )
            ),
            new RWMatch(config::isSpherePepsi, "Шар Пепси",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+2 Урон при атаке",
                    "+0.3 Скорость атаки",
                    "-25% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.alternating(BLUE, WHITE),
                List.of(
                    TextLine.of("Пассивные эффекты:", BLUEMINIHEADER, true),
                    TextLine.of("+2 Урон", WHITE, false),
                    TextLine.of("+0.3 Скорость атаки", BLUESTROKE, false),
                    TextLine.of("-25% Скорость", WHITE, false)
                )
            ),
            new RWMatch(config::isSphereRedBull, "Шар РедБулла",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+3 Урон при атаке",
                    "+2 Броня",
                    "-8 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.segmented(List.of(
                    new NameSegment("Шар", YELLOW),
                    new NameSegment(" ", RED),
                    new NameSegment("Ред", RED),
                    new NameSegment("Булла", BLUE)
                )),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("+2 Броня", BLUESTROKE, false),
                    TextLine.of("+3 Урон", YELLOWSTROKE, false),
                    TextLine.of("-8 Максимальное здоровье", RED, false)
                )
            ),
            new RWMatch(config::isSphereSprite, "Шар Спрайта",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+30% Скорость",
                    "+2 Урон при атаке",
                    "+6 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(GREEN),
                List.of(
                    TextLine.of("Пассивные эффекты:", YELLOWMINIHEADER, true),
                    TextLine.of("+30% Скорость", GREENMINIHEADER, false),
                    TextLine.of("+2 Урон", GREENSTROKE, false),
                    TextLine.of("+6 Максимальное здоровье", YELLOWSTROKE, false)
                )
            ),
            new RWMatch(config::isSphereFanta, "Шар Фанты",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+4 Броня",
                    "+10 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(ORANGE),
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+4 Броня", ORANGESTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", ORANGESTROKE, false)
                )
            ),
            new RWMatch(config::isSphereShine, "Шар Света",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Светлячок (01:00)",
                    "+3 Броня",
                    "+2 Урон при атаке",
                    "+2 Урон атаки",
                    "+3.5 Везение",
                    "+3.5 Удачи",
                    "В радиусе 16 блоков",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Активируемые эффекты:", YELLOWMINIHEADER, true),
                    TextLine.of("Светлячок (01:00) Радиус: 16 блоков", YELLOWSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", YELLOWMINIHEADER, true),
                    TextLine.of("+3 Броня", YELLOWSTROKE, false),
                    TextLine.of("+2 Урон", YELLOWSTROKE, false),
                    TextLine.of("+3.5 Везение", YELLOWSTROKE, false)
                )
            ),
            new RWMatch(config::isSphereChaos, "Шар Хаоса",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Слабость 2 (00:10)",
                    "Слабость II (00:10)",
                    "+2.5 Броня",
                    "+3 Урон при атаке",
                    "+3 Урон атаки",
                    "+0.4 Скорость атаки",
                    "-10 Максимальное здоровье",
                    "-10 Макс. здоровье",
                    "В радиусе 8 блоков",
                    "Проклятие утраты"
                ),
                NameSpec.color(PURPLE),
                List.of(
                    TextLine.of("Активируемые эффекты:", PURPLEMINIHEADER, true),
                    TextLine.of("Слабость 2 (00:10) Радиус: 8 блоков", PURPLESTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", PURPLEMINIHEADER, true),
                    TextLine.of("+2.5 Броня", PURPLESTROKE, false),
                    TextLine.of("+3 Урон", PURPLESTROKE, false),
                    TextLine.of("+0.4 Скорость атаки", PURPLESTROKE, false),
                    TextLine.of("-10 Максимальное здоровье", PURPLESTROKE, false)
                )
            ),
            new RWMatch(config::isSphereDiscipline, "Шар Порядка",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Регенерация II (00:08)",
                    "+15% Скорость",
                    "+2 Броня",
                    "+20 Максимальное здоровье",
                    "+20 Макс. здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(PINK),
                List.of(
                    TextLine.of("Активируемые эффекты:", PINKMINIHEADER, true),
                    TextLine.of("Регенерация 2 (00:08)", PINKSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", PINKMINIHEADER, true),
                    TextLine.of("+15% Скорость", PINKSTROKE, false),
                    TextLine.of("+2 Броня", PINKSTROKE, false),
                    TextLine.of("+20 Максимальное здоровье", PINKSTROKE, false)
                )
            ),

            new RWMatch(config::isSpherePoseidon, "Шар Посейдона 2", List.of(), NameSpec.rename("Шар Посейдона"), List.of()),
            new RWMatch(config::isSphereArmadillo, "Шар Броненосца 2", List.of(), NameSpec.rename("Шар Броненосца", GRAY), List.of()),
            new RWMatch(config::isSphereHades, "Шар Аида 2", List.of(), NameSpec.rename("Шар Аида", RED), List.of()),

            new RWMatch(config::isSphereBUNNY, "Шар BUNNY",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Огнестойкость",
                    "+3.9 Броня",
                    "+21% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("Огнестойкость", GRAYSTROKE, false),
                    TextLine.of("+3.9 Броня", GRAYSTROKE, false),
                    TextLine.of("+21% Скорость", GRAYSTROKE, false)
                )
            ),
            new RWMatch(config::isSphereDHELPER, "Шар Д.Хелпера",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+7 Максимальное здоровье",
                    "+0.6 Скорость атаки",
                    "+3 Стойкость",
                    "+7.0 к здоровью",
                    "+0.6 к скорости атаки",
                    "+3 к стойкости",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+7 Максимальное здоровье", ORANGESTROKE, false),
                    TextLine.of("+0.6 Скорость атаки", ORANGESTROKE, false),
                    TextLine.of("+3 Стойкость", ORANGESTROKE, false)
                )
            ),
            new RWMatch(config::isHeadBatman, "Голова БетМена",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+1.5 Урон при атаке",
                    "+2.5 Броня",
                    "+0.3 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("+1.5 Урон", GRAYSTROKE, false),
                    TextLine.of("+2.5 Броня", GRAYSTROKE, false),
                    TextLine.of("+0.3 Скорость атаки", GRAYSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadVampire, "Голова Дракулы",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Вампиризм",
                    "+2 Урон при атаке",
                    "+2 Урон атаки",
                    "+4.5 Броня",
                    "+10 Максимальное здоровье",
                    "+10 Макс. здоровье",
                    "+8 Максимальное здоровье",
                    "+8 Макс. здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(RED),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("Вампиризм", REDSTROKE, false),
                    TextLine.of("+2 Урон", REDSTROKE, false),
                    TextLine.of("+4.5 Броня", REDSTROKE, false),
                    TextLine.of("+8 Максимальное здоровье", REDSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadVampire, "Голова Вампира",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Вампиризм",
                    "+2 Урон при атаке",
                    "+2 Урон атаки",
                    "+4.5 Броня",
                    "+10 Максимальное здоровье",
                    "+10 Макс. здоровье",
                    "+8 Максимальное здоровье",
                    "+8 Макс. здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(RED),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("Вампиризм", REDSTROKE, false),
                    TextLine.of("+2 Урон", REDSTROKE, false),
                    TextLine.of("+4.5 Броня", REDSTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", REDSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadJack, "Голова Джека",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Защита IV",
                    "+2 Твёрдость брони",
                    "+3 Броня",
                    "+6 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(ORANGE),
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("Защита 4", ORANGESTROKE, false),
                    TextLine.of("+2 Твёрдость брони", ORANGESTROKE, false),
                    TextLine.of("+3 Броня", ORANGESTROKE, false),
                    TextLine.of("+6 Максимальное здоровье", ORANGESTROKE, false)
                )
            ),
            new RWMatch(config::isHeadGrinch, "Голова Гринча",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+30% Скорость",
                    "+0.2 Скорость атаки",
                    "+2 Броня",
                    "-4 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GREENMINIHEADER, true),
                    TextLine.of("+30% Скорость", GREENSTROKE, false),
                    TextLine.of("+2 Броня", GREENSTROKE, false),
                    TextLine.of("+0.2 Скорость атаки", GREENSTROKE, false),
                    TextLine.of("-4 Максимальное здоровье", GREENSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadHydra, "Голова Гидры",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+20% Скорость",
                    "+5 Броня",
                    "+10 Максимальное здоровье",
                    "+10 Макс. здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GREENMINIHEADER, true),
                    TextLine.of("+20% Скорость", GREENSTROKE, false),
                    TextLine.of("+5 Броня", GREENSTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", GREENSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadIronMan, "Голова Железного Человека",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "-25% Скорость",
                    "+4 Броня",
                    "+10 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(WHITE),
                List.of(
                    TextLine.of("Пассивные эффекты:", WHITE, true),
                    TextLine.of("+4 Броня", WHITE, false),
                    TextLine.of("+10 Максимальное здоровье", WHITE, false),
                    TextLine.of("-25% Скорость", WHITE, false)
                )
            ),
            new RWMatch(config::isHeadCobra, "Голова Кобры",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+3.0 к Урону",
                    "+20% к Скорости атаки",
                    "+2.5 к Стойкости",
                    "Отравляет врагов ядом",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GREENMINIHEADER, true),
                    TextLine.of("Отравляет врагов ядом", GREENSTROKE, false),
                    TextLine.of("+3 Урон", GREENSTROKE, false),
                    TextLine.of("+20% Скорость атаки", GREENSTROKE, false),
                    TextLine.of("+2.5 Стойкость", GREENSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadBunny, "Голова Кролика",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+2.5 Броня",
                    "+10 Максимальное здоровье",
                    "-20% Скорость",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("+2.5 Броня", GRAYSTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", GRAYSTROKE, false),
                    TextLine.of("-20% Скорость", GRAYSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadPegasus, "Голова Пегаса",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Вы получаете СКОРОСТЬ III (0:10)",
                    "+3.5 Броня",
                    "+7 Максимальное здоровье",
                    "+7 Макс. здоровье",
                    "+2.5 Урон при атаке",
                    "+2.5 Урон атаки",
                    "+0.2 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.color(GOLD),
                List.of(
                    TextLine.of("Активируемые эффекты:", GOLDMINIHEADER, true),
                    TextLine.of("Скорость 3 (00:10)", GOLDMINIHEADER, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", GOLDMINIHEADER, true),
                    TextLine.of("+2.5 Урон", GOLDSTROKE, false),
                    TextLine.of("+3.5 Броня", GOLDSTROKE, false),
                    TextLine.of("+7 Максимальное здоровье", GOLDSTROKE, false),
                    TextLine.of("+0.2 Скорость атаки", GOLDSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadPenguin, "Голова Пингвина",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "-30% Скорость",
                    "+3 Урон при атаке",
                    "+3 Броня",
                    "Проклятие утраты"
                ),
                NameSpec.color(GRAY),
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("+3 Урон", GRAYSTROKE, false),
                    TextLine.of("+3 Броня", GRAYSTROKE, false),
                    TextLine.of("-30% Скорость", GRAYSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadGingerbread, "Голова Пряника",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "-6 Максимальное здоровье",
                    "+2.5 Урон при атаке",
                    "+0.4 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.color(BROWN),
                List.of(
                    TextLine.of("Пассивные эффекты:", CYANMINIHEADER, true),
                    TextLine.of("+2.5 Урон", CYANSTROKE, false),
                    TextLine.of("+0.4 Скорость атаки", CYANSTROKE, false),
                    TextLine.of("-6 Максимальное здоровье", CYANSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadRudolph, "Голова Рудольфа",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+20 Максимальное здоровье",
                    "+3.5 Броня",
                    "-0.4 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.color(BROWN),
                List.of(
                    TextLine.of("Пассивные эффекты:", BROWNMINIHEADER, true),
                    TextLine.of("+3.5 Броня>", BROWNSTROKE, false),
                    TextLine.of("+20 Максимальное здоровье", BROWNSTROKE, false),
                    TextLine.of("-0.4 Скорость атаки", BROWNSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadSanta, "Голова Санты",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+6 Максимальное здоровье",
                    "+4 Сопротивление к отбрасыванию",
                    "+4 Твёрдость брони",
                    "-0.3 Скорость атаки",
                    "Проклятие утраты"
                ),
                NameSpec.segmented(List.of(
                    new NameSegment("Голова", RED),
                    new NameSegment(" ", RED),
                    new NameSegment("Санты", WHITE)
                )),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("+4 Твёрдость брони", WHITE, false),
                    TextLine.of("+4 Сопротивление к отбрасыванию", REDSTROKE, false),
                    TextLine.of("+6 Максимальное здоровье", WHITE, false),
                    TextLine.of("-0.3 Скорость атаки", REDSTROKE, false)
                )
            ),
            new RWMatch(config::isHeadThor, "Голова Тора",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+100 Везение",
                    "+100 Скорость полёта",
                    "+4 Урона при атаке",
                    "+10 Сопротивление к отбрасыванию",
                    "-10 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(YELLOW),
                List.of(
                    TextLine.of("Пассивные эффекты:", YELLOWMINIHEADER, true),
                    TextLine.of("+4 Урон", YELLOW, false),
                    TextLine.of("+100 Скорость полёта", YELLOW, false),
                    TextLine.of("+100 Везение", YELLOW, false),
                    TextLine.of("+10 Сопротивление к отбрасыванию", YELLOW, false),
                    TextLine.of("-10 Максимальное здоровье", YELLOW, false)
                )
            ),
            new RWMatch(config::isHeadHulk, "ГОЛОВА ХАЛКА",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+3 Урон при атаке",
                    "+5 Сопротивление к отбрасыванию",
                    "+15 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", GREENMINIHEADER, true),
                    TextLine.of("+3 Урон", GREEN, false),
                    TextLine.of("+15 Максимальное здоровье", GREEN, false),
                    TextLine.of("+5 Сопротивление к отбрасыванию", GREEN, false)
                )
            ),
            new RWMatch(config::isHeadNutcracker, "Голова Щелкунчика",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+2 Урона при атаке",
                    "+0.3 Скорость атаки",
                    "-8 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.NONE,
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+2 Урон", ORANGE, false),
                    TextLine.of("+0.3 Скорость атаки", ORANGE, false),
                    TextLine.of("-8 Максимальное здоровье", ORANGE, false)
                )
            ),
            new RWMatch(config::isHeadElf, "Голова Эльфа",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+1.5 Броня",
                    "+30% Скорость",
                    "+10 Максимальное здоровье",
                    "Проклятие утраты"
                ),
                NameSpec.color(CYAN),
                List.of(
                    TextLine.of("Пассивные эффекты:", CYANMINIHEADER, true),
                    TextLine.of("+1.5 Броня", CYAN, false),
                    TextLine.of("+30% Скорость", CYAN, false),
                    TextLine.of("+10 Максимальное здоровье", CYAN, false)
                )
            ),
            new RWMatch(config::isEasterEgg, "Пасхальное яйцо",
                List.of(
                    "Когда во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+3 к урону",
                    "+5.5 к стойкости",
                    "+6 к здоровью",
                    "Проклятие утраты"
                ),
                NameSpec.color(YELLOW),
                List.of(
                    TextLine.of("Пассивные эффекты:", YELLOWMINIHEADER, true),
                    TextLine.of("+3 Урон", YELLOW, false),
                    TextLine.of("+6 Максимальное здоровье", YELLOW, false),
                    TextLine.of("+5.5 Стойкость", YELLOW, false)
                )
            )
        );
    }

    private void applyReallyWorldMatch(ItemStack stack, List<Text> lines, RWMatch match) {
        int nameIndex = findLineIndex(lines, match.name);

        if (nameIndex == -1) {
            return;
        }

        if (!match.hiddenLines.isEmpty()) {
            lines.removeIf(line -> {
                if (line == null) {
                    return false;
                }

                String text = line.getString();
                return match.hiddenLines.stream().anyMatch(text::contains);
            });
        }

        nameIndex = findLineIndex(lines, match.name);

        if (nameIndex == -1) {
            return;
        }

        applyReallyWorldName(stack, match.nameSpec);

        int index = nameIndex + 1;

        for (TextLine line : match.outputLines) {
            lines.add(index++, line.create());
        }
    }

    private int findLineIndex(List<Text> lines, String text) {
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);

            if (line != null && line.getString().contains(text)) {
                return i;
            }
        }

        return -1;
    }

    private void applyReallyWorldName(ItemStack stack, NameSpec spec) {
        if (spec == null || spec.type == NameSpecType.NONE) {
            return;
        }

        switch (spec.type) {
            case COLOR -> setNameColor(stack, spec.color1);
            case ALTERNATING -> setAlternatingNameColors(stack, spec.color1, spec.color2);
            case SEGMENTED -> setSegmentedName(stack, spec.segments);
            case RENAME -> {
                if (spec.color1 != null) {
                    setNameColor(stack, spec.color1);
                }
                renamePreservingStyle(stack, spec.newName);
            }
            case NONE -> { }
        }
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterSpheres();
            instance.refreshBlockedStatusInternal();
            instance.registerCallbacks();
            BQoL.LOGGER.info("Better Spheres initialized");
        }
    }

    public static BetterSpheres getInstance() {
        if (instance == null)
            initialize();

        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null)
            instance.refreshBlockedStatusInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null)
            instance.setEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_spheres");
        blocked.holyworld = LiteApiManager.isFeatureBlocked("better_spheres_holyworld");
        blocked.golden = LiteApiManager.isFeatureBlocked("better_spheres_special");
        blocked.parameters = LiteApiManager.isFeatureBlocked("better_spheres_parameters");
        blocked.names = LiteApiManager.isFeatureBlocked("better_spheres_names");
    }

    private boolean isEnabledInternal() {
        return config.isBetterSpheresEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterSpheresEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    /// Регистрация
    private void registerCallbacks() {
        if (registered) {
            return;
        }

        registered = true;

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!isEnabled()
                || stack.getItem() != Items.PLAYER_HEAD
                || lines.isEmpty()) {
                return;
            }

            if (isHolyWorld()) {
                processTooltipHolyWorld(stack, lines);
                return;
            }

            if (isReallyWorld()) {
                processTooltipReallyWorld(stack, lines);
            }
        });
    }

    /// Проверки на содержания тултипа
    private boolean containsLine(List<String> lines, String text) {
        return lines.stream()
            .anyMatch(line -> line.contains(text));
    }

    /// Меняет только цвет названия, сохраняя остальные свойства стиля
    private void setNameColor(ItemStack stack, int color) {
        Text name = stack.getName();

        Style style = getReallyWorldNameStyle(name)
            .withColor(color);

        MutableText result = Text.literal(name.getString())
            .setStyle(style);

        stack.setCustomName(result);
    }

    /// Меняет название, сохраняя исходный стиль
    private void renamePreservingStyle(ItemStack stack, String newName) {
        Text oldName = stack.getName();

        Style style = getReallyWorldNameStyle(oldName);

        MutableText result = Text.literal(newName)
            .setStyle(style);

        stack.setCustomName(result);
    }

    /// Окраска по принципу чередования
    private void setAlternatingNameColors(ItemStack stack, int color1, int color2) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        String name = oldName.getString();

        Style baseStyle = getReallyWorldNameStyle(oldName);

        int letterIndex = 0;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isWhitespace(c)) {
                result.append( Text.literal(String.valueOf(c))
                    .setStyle(baseStyle) ); continue;
            }

            int color = (letterIndex % 2 == 0)
                ? color1
                : color2;

            result.append(Text.literal(String.valueOf(c))
                .setStyle( baseStyle.withColor(color)));

            letterIndex++;
        }

        stack.setCustomName(result);
    }

    /// Окраска по принципу отдельных сегментов
    private void setSegmentedName(ItemStack stack, List<NameSegment> segments) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        Style baseStyle = getReallyWorldNameStyle(oldName);

        for (NameSegment segment : segments) {
            result.append( Text.literal(segment.text())
                .setStyle( baseStyle.withColor(segment.color())));
        }

        stack.setCustomName(result);
    }

    /// Поиск сфер
    private HWSphereResult searchHWSpheres(Map<String, Integer> parameters) {
        return new HWSphereResult(
            findHWDefaultSphere(parameters),
            findHWGoldenSphere(parameters)
        );
    }

    private HWDefaultSphereMatch findHWDefaultSphere(Map<String, Integer> parameters) {
        for (HWDefaultSphereMatch match : hwSphereMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (parametersMatch(parameters, match.requiredParameters)) {
                return match;
            }
        }

        return null;
    }

    private HWGoldenSphereMatch findHWGoldenSphere(Map<String, Integer> parameters) {
        for (HWGoldenSphereMatch match : hwGoldenSphereMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (parametersMatch(parameters, match.requiredParameters)) {
                return match;
            }
        }

        return null;
    }

    /// Определение информации о сфере
    private HWSphereInfo getHWSphereInfo(String name) {
        if (name == null) {
            return null;
        }

        if (name.contains("Мифическая")) {
            return new HWSphereInfo(
                SphereType.MYTHIC,
                SpherePalette.MYTHIC_NAME,
                SpherePalette.MYTHIC_GRADIENT
            );
        }

        if (name.contains("Легендарная")) {
            return new HWSphereInfo(
                SphereType.LEGENDARY,
                SpherePalette.LEGENDARY_NAME,
                SpherePalette.LEGENDARY_GRADIENT
            );
        }

        if (name.contains("Эпическая")) {
            return new HWSphereInfo(
                SphereType.EPIC,
                SpherePalette.EPIC_NAME,
                SpherePalette.EPIC_GRADIENT
            );
        }

        if (name.contains("Обычная")) {
            return new HWSphereInfo(
                SphereType.DEFAULT,
                SpherePalette.DEFAULT_NAME,
                SpherePalette.DEFAULT_GRADIENT
            );
        }

        return null;
    }

    /// Парс параметров
    private Map<String, Integer> parseParametersFromTooltip(List<Text> lines) {
        Map<String, Integer> result = new HashMap<>();

        for (Text line : lines) {
            if (line == null) {
                continue;
            }

            String raw = line.getString().trim();

            if (raw.isEmpty()) {
                continue;
            }

            raw = raw.replaceFirst("^[◆▪•✦*]\\s*", "");

            for (Map.Entry<String, Pattern> entry :
                PARAMETER_PATTERNS.entrySet()) {

                if (!entry.getValue().matcher(raw).find()) {
                    continue;
                }

                String parameter = entry.getKey();

                int level = parameter.equals(VANISHING)
                    ? 0
                    : extractLevel(raw);

                if (!parameter.equals(VANISHING) && level < 0) {
                    continue;
                }

                result.put(parameter, level);
                break;
            }
        }

        return result;
    }

    /// Извлечение уровня
    private int extractLevel(String line) {
        Matcher digitMatcher = LEVEL_PATTERN.matcher(line);

        if (digitMatcher.find()) {
            try {
                return Integer.parseInt(digitMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        String[] parts = line.split("\\s+");

        if (parts.length == 0) {
            return -1;
        }

        return romanToInt(parts[parts.length - 1]
                .toUpperCase(Locale.ROOT)
        );
    }

    /// Конвертация чисел
    private int romanToInt(String roman) {
        if (roman == null
            || roman.isBlank()
            || !roman.matches(
            "^M{0,4}(CM|CD|D?C{0,3})" + "(XC|XL|L?X{0,3})" + "(IX|IV|V?I{0,3})$")) {
            return 0;
        }

        int result = 0;

        for (int i = 0; i < roman.length(); i++) {
            int current = CONVERTER.get(roman.charAt(i));

            if (i + 1 < roman.length()) {
                int next = CONVERTER.get(roman.charAt(i + 1));

                if (current < next) {
                    result -= current;
                    continue;
                }
            }

            result += current;
        }

        return result;
    }

    /// Проверка параметров на совпадение
    private boolean parametersMatch(Map<String, Integer> actual, Map<String, Integer> required) {
        if (actual.size() != required.size()) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            if (!Objects.equals(actual.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /// Применение изменений
    private void removeParameterLines(List<Text> lines) {
        lines.removeIf(line -> {
            String clean = line.getString().trim();

            clean = clean.replaceFirst("^[◆▪•✦*]\\s*", "");

            for (Pattern pattern :
                PARAMETER_PATTERNS.values()) {

                if (pattern.matcher(clean).matches()) {
                    return true;
                }
            }

            return false;
        });
    }

    private void replaceParameterLines(List<Text> lines, List<String> targetLines, List<Integer> colors) {
        if (targetLines == null
            || colors == null
            || targetLines.size() != colors.size()) {
            return;
        }

        int insertIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            String clean = lines.get(i)
                .getString()
                .trim();

            clean = clean.replaceFirst("^[◆▪•✦*]\\s*", "");

            for (String parameter : PARAMETERS) {
                if (clean.startsWith(parameter)) {
                    insertIndex = i;
                    break;
                }
            }

            if (insertIndex != -1) {
                break;
            }
        }

        if (insertIndex == -1) {
            return;
        }

        removeParameterLines(lines);

        for (int i = 0; i < targetLines.size(); i++) {
            final int color = colors.get(i);

            lines.add(insertIndex++, Text.literal("◆ " + targetLines.get(i)).styled(style -> style
                    .withColor(color)
                    .withItalic(false))
            );
        }
    }

    private void applyHWSpheresParameters(List<Text> lines, Map<String, Integer> parameters, HWSphereInfo sphereInfo, HWSphereResult result) {
        if (result.hwGoldenSphere() != null) {
            applyHWGoldenSphereParameters(lines, result.hwGoldenSphere());
        } else {
            applyHWDefaultSphereParameters(lines, parameters, sphereInfo, result.hwDefaultSphere());
        }
    }

    private void applyHWDefaultSphereParameters(List<Text> lines, Map<String, Integer> parameters, HWSphereInfo sphereInfo, HWDefaultSphereMatch defaultMatch) {
        if (blocked.parameters || sphereInfo == null) {
            return;
        }

        if (defaultMatch != null) {
            replaceParameterLines(lines, defaultMatch.targetLines, defaultMatch.colors);
            return;
        }

        List<String> orderedLines = new ArrayList<>();
        List<Integer> orderedColors = new ArrayList<>();

        int colorIndex = 0;

        for (String parameter : PARAMETERS) {
            if (!parameters.containsKey(parameter)) {
                continue;
            }

            int level = parameters.get(parameter);

            orderedLines.add(
                parameter.equals(VANISHING)
                    ? parameter
                    : parameter + " " + level
            );

            orderedColors.add(sphereInfo.gradient().get(colorIndex % sphereInfo.gradient().size()));

            colorIndex++;
        }

        replaceParameterLines(lines, orderedLines, orderedColors);
    }

    private void applyHWGoldenSphereParameters(List<Text> lines, HWGoldenSphereMatch match) {
        if (!config.isHWGoldenSpheres()
            || blocked.golden
            || match == null) {
            return;
        }

        replaceParameterLines(lines, match.targetLines, match.colors);
    }

    private void applyHWSphereNames(ItemStack stack, String originalName, HWSphereInfo sphereInfo, HWSphereResult result) {
        if (result.hwGoldenSphere() == null) {
            applyHWDefaultSphereName(stack, originalName, sphereInfo);
        } else {
            applyHWGoldenSphereName(stack, result.hwGoldenSphere());
        }
    }

    private void applyHWDefaultSphereName(ItemStack stack, String originalName, HWSphereInfo sphereInfo) {
        if (blocked.names
            || sphereInfo == null
            || sphereInfo.type() == SphereType.LEGENDARY
            || sphereInfo.type() == SphereType.MYTHIC) {
            return;
        }

        stack.setCustomName(Text.literal(originalName)
                .styled(style -> style
                    .withColor(sphereInfo.nameColor())
                    .withItalic(false))
        );
    }

    private void applyHWGoldenSphereName(ItemStack stack, HWGoldenSphereMatch match) {
        if (!config.isHWGoldenSpheres()
            || blocked.names
            || match == null) {
            return;
        }

        Text customName = Text.literal(match.newName)
            .styled(style -> style
                .withColor(SpherePalette.GOLDEN_NAME)
                .withItalic(false));

        if (!stack.getName().equals(customName)) {
            stack.setCustomName(customName);
        }
    }

    private Style getReallyWorldNameStyle(Text name) {
        return (name.getStyle() != null
            ? name.getStyle()
            : Style.EMPTY)
            .withBold(true)
            .withItalic(false);
    }

    /// Проверка серверов
    private boolean isHolyWorld() {
        MinecraftClient client = mc();

        if (client == null) {
            return false;
        }

        ServerInfo server = client.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address
            .toLowerCase(Locale.ROOT)
            .contains("holyworld");
    }

    private boolean isReallyWorld() {
        MinecraftClient client = mc();

        if (client == null) {
            return false;
        }

        ServerInfo server = client.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address
            .toLowerCase(Locale.ROOT)
            .contains("reallyworld");
    }
}
// v1.0