package com.zavk1n.bqol.features.spheres.reallyworld;

import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.spheres.BetterSpheres;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public class ReallyWorldProcess {
    public ReallyWorldProcess(BetterSpheres betterSpheres, MainConfig config) {
        this.betterSpheres = betterSpheres;
        this.config = config;
        this.rwSphereMatches = createReallyWorldSphereMatches();
    }

    public final BetterSpheres betterSpheres;
    public final MainConfig config;

    public final List<RWSphereMatch> rwSphereMatches;

    public enum NameSpecType {
        NONE,
        COLOR,
        ALTERNATING,
        SEGMENTED,
        RENAME
    }

    public record NameSegment(String text, int color) {}

    public static class NameSpec {
        public static NameSpec NONE = new NameSpec(NameSpecType.NONE, null, null, null, List.of());

        public static NameSpecType type;
        public static Integer color1;
        public static Integer color2;
        public static String newName;
        public static List<NameSegment> segments;

        public NameSpec(NameSpecType type, Integer color1, Integer color2, String newName, List<NameSegment> segments) {
            this.type = type;
            this.color1 = color1;
            this.color2 = color2;
            this.newName = newName;
            this.segments = segments;
        }

        public static NameSpec color(int color) { return new NameSpec(NameSpecType.COLOR, color, null, null, List.of()); }
        public static NameSpec alternating(int c1, int c2) { return new NameSpec(NameSpecType.ALTERNATING, c1, c2, null, List.of()); }
        public static NameSpec segmented(List<NameSegment> segments) { return new NameSpec(NameSpecType.SEGMENTED, null, null, null, List.copyOf(segments)); }
        public static NameSpec rename(String newName) { return new NameSpec(NameSpecType.RENAME, null, null, newName, List.of()); }
        public static NameSpec rename(String newName, int color) { return new NameSpec(NameSpecType.RENAME, color, null, newName, List.of()); }
    }

    public static class TextLine {
        public static String text;
        public static Integer color;
        public static boolean bold;

        public TextLine(String text, Integer color, boolean bold) {
            this.text = text;
            this.color = color;
            this.bold = bold;
        }

        public static TextLine plain(String text) {
            return new TextLine(text, null, false);
        }

        public static TextLine of(String text, int color, boolean bold) {
            return new TextLine(text, color, bold);
        }

        public static Text create() {
            MutableText result = Text.literal(text);

            if (color != null) {
                result.styled(style -> style.withColor(color).withBold(bold).withItalic(false));
            } else {
                result.styled(style -> style.withItalic(false));
            }

            return result;
        }
    }

    public class RWSphereMatch {
        public java.util.function.BooleanSupplier enabled;
        public String name;
        public List<String> hiddenLines;
        public NameSpec nameSpec;
        public List<TextLine> outputLines;

        public RWSphereMatch(java.util.function.BooleanSupplier enabled,
                        String name,
                        List<String> hiddenLines,
                        NameSpec nameSpec,
                        List<TextLine> outputLines) {
            this.enabled = enabled;
            this.name = name;
            this.hiddenLines = List.copyOf(hiddenLines);
            this.nameSpec = nameSpec;
            this.outputLines = List.copyOf(outputLines);
        }
    }

    private static final int WHITE = 0xFFFFFF;

    private static final int BLACK = 0x404040;
    private static final int BLACKMINIHEADER = 0x616161;
    private static final int BLACKSTROKE = 0x787878;

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

    private static final int TURQUOISE = 0x00B88C;
    private static final int TURQUOISEMINIHEADER = 0x02E8B1;
    private static final int TURQUOISESTROKE = 0x40FFD3;

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

    private List<RWSphereMatch> createReallyWorldSphereMatches() {
        return List.of(
            new RWSphereMatch(config::isSphereAirRW, "Шар Воздуха",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereFireRW, "Шар Огня",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                    TextLine.of("Активируемые эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("Огнеупорность", ORANGESTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+4 Максимальное здоровье", ORANGESTROKE, false),
                    TextLine.of("+2.5 Урон", ORANGESTROKE, false),
                    TextLine.of("+0.3 Скорость атаки", ORANGESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereGroundRW, "Шар Земли",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                    " +2 Сопротивление к отбрасыванию",
                    "+2 Сопротивление отбрасыванию",
                    " +2 Сопротивление отбрасыванию",
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
            new RWSphereMatch(config::isSphereWaterRW, "Шар Воды",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereGODRW, "ШАР БОГА",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                    "+2.5 Урон при атаке",
                    "+2.5 Урон атаки",
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
                    TextLine.of("+2.5 Урон", GOLDSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereCocaColaRW, "Шар КокаКолы",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSpherePepsiRW, "Шар Пепси",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereRedBullRW, "Шар РедБулла",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereSpriteRW, "Шар Спрайта",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereFantaRW, "Шар Фанты",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereShineRW, "Шар Света",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                    "+3.5 Удача",
                    " +3.5 Удача",
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
            new RWSphereMatch(config::isSphereChaosRW, "Шар Хаоса",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                NameSpec.color(BLACK),
                List.of(
                    TextLine.of("Активируемые эффекты:", BLACKMINIHEADER, true),
                    TextLine.of("Слабость 2 (00:10) Радиус: 8 блоков", BLACKSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", BLACKMINIHEADER, true),
                    TextLine.of("+2.5 Броня", BLACKSTROKE, false),
                    TextLine.of("+3 Урон", BLACKSTROKE, false),
                    TextLine.of("+0.4 Скорость атаки", BLACKSTROKE, false),
                    TextLine.of("-10 Максимальное здоровье", BLACKSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereDarkRW, "Шар Тьмы",
                List.of(
                    "Когда во второй руке:",
                    "Пока во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Тьма (00:06)",
                    "+2 Урон при атаке",
                    "+2 Урон к урону",
                    "+2 к урону",
                    "+30% к скорости передвижения",
                    "+6 Максимальное здоровье",
                    "+6 к максимальному здоровью",
                    "+6 к Максимальное здоровье",
                    "+6 Макс. здоровье",
                    "В радиусе 15 блоков",
                    "Проклятие утраты"
                ),
                NameSpec.color(PURPLE),
                List.of(
                    TextLine.of("Активируемые эффекты:", PURPLEMINIHEADER, true),
                    TextLine.of("Тьма (00:06) Радиус: 15 блоков", PURPLESTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", PURPLEMINIHEADER, true),
                    TextLine.of("+2 Урон", PURPLESTROKE, false),
                    TextLine.of("+30% Скорость", PURPLESTROKE, false),
                    TextLine.of("+6 Максимальное здоровье", PURPLESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereBloodRW, "Шар Крови",
                List.of(
                    "Когда во второй руке:",
                    "Пока во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Моментальный урон 2 (00:10)",
                    "Моментальный урон 2",
                    "Игнорирует броню",
                    "Моментальный урон II (00:10)",
                    "+3.5 к броне",
                    "+2 к урону",
                    "-10% к скорости передвижения",
                    "В радиусе 15 блоков",
                    "Проклятие утраты"
                ),
                NameSpec.color(RED),
                List.of(
                    TextLine.of("Активируемые эффекты:", REDMINIHEADER, true),
                    TextLine.of("Моментальный урон 2 Радиус: 15 блоков", REDSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", REDSTROKE, true),
                    TextLine.of("+3.5 Броня", REDSTROKE, false),
                    TextLine.of("+2 Урон", REDSTROKE, false),
                    TextLine.of("-10% Скорость", REDSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereLightningRW, "Шар Молнии",
                List.of(
                    "Когда во второй руке:",
                    "Пока во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Скорость атаки +0.35 (00:08)",
                    "+3.5 к броне",
                    "+3 к урону",
                    "+25% к скорости передвижения",
                    "Проклятие утраты"
                ),
                NameSpec.color(CYAN),
                List.of(
                    TextLine.of("Активируемые эффекты:", CYANMINIHEADER, true),
                    TextLine.of("+0.35 Скорость атаки", CYANSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", CYANSTROKE, true),
                    TextLine.of("+3 Урон", CYANSTROKE, false),
                    TextLine.of("+25% Скорость", CYANSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereIceRW, "Шар Льда",
                List.of(
                    "Когда во второй руке:",
                    "Пока во второй руке:",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Заморозка (00:04)",
                    "+2.5 к броне",
                    "+10 к максимальному здоровью",
                    "В радиусе 8 блоков",
                    "Проклятие утраты"
                ),
                NameSpec.color(CYAN),
                List.of(
                    TextLine.of("Активируемые эффекты:", CYANMINIHEADER, true),
                    TextLine.of("Заморозка (00:04) Радиус: 8 блоков", CYANSTROKE, false),
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", CYANSTROKE, true),
                    TextLine.of("+2.5 Броня", CYANSTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", CYANSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereDisciplineRW, "Шар Порядка",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isSphereWardenRW, "Шар Вардена",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "Регенерация II (00:08)",
                    "+14 к Здоровью",
                    "+2 к Урону",
                    "+2 к Броне",
                    "-30% Скорости передвижения",
                    "Проклятие утраты"
                ),
                NameSpec.color(TURQUOISE),
                List.of(
                    TextLine.of("Пассивные эффекты:", TURQUOISEMINIHEADER, true),
                    TextLine.of("+2 Урон", TURQUOISESTROKE, false),
                    TextLine.of("+2 Броня", TURQUOISESTROKE, false),
                    TextLine.of("+14 Максимальное здоровье", TURQUOISESTROKE, false),
                    TextLine.of("-30% Скорость", TURQUOISESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereModerRW, "Шар Модера",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+10 к Здоровью",
                    "+3.5 к Броне",
                    "-15% Скорости передвижения",
                    "Проклятие утраты"
                ),
                NameSpec.color(BLUE),
                List.of(
                    TextLine.of("Пассивные эффекты:", BLUEMINIHEADER, true),
                    TextLine.of("+3.5 Броня", BLUESTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", BLUESTROKE, false),
                    TextLine.of("-15% Скорость", BLUESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereCheaterRW, "Шар Читера",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+6 к Здоровью",
                    "+1 к Урону",
                    "-1 к Броне",
                    "Проклятие утраты"
                ),
                NameSpec.color(RED),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("+1 Урон", REDSTROKE, false),
                    TextLine.of("+6 Максимальное здоровье", REDSTROKE, false),
                    TextLine.of("-1 Броня", REDSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereAdminRW, "Шар Админа",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+10 к Здоровью",
                    "+2 к Урону",
                    "+3 к Броне",
                    "Проклятие утраты"
                ),
                NameSpec.color(GOLD),
                List.of(
                    TextLine.of("Пассивные эффекты:", GOLDMINIHEADER, true),
                    TextLine.of("+2 Урон", GOLDSTROKE, false),
                    TextLine.of("+3 Броня", GOLDSTROKE, false),
                    TextLine.of("+10 Максимальное здоровье", GOLDSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereTeacherRW, "Шар Учителя",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+8 к Здоровью",
                    "-1 к Урону",
                    "+3 к Броне",
                    "Проклятие утраты"
                ),
                NameSpec.color(GREEN),
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("+3 Броня", GREENSTROKE, false),
                    TextLine.of("+8 Максимальное здоровье", GREENSTROKE, false),
                    TextLine.of("-1 Урон", GREENSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereDisciplineRW, "Шар Порядка",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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

            new RWSphereMatch(config::isSpherePoseidonRW, "Шар Посейдона 2",
                List.of(), NameSpec.rename("Шар Посейдона", CYAN), List.of()),
            new RWSphereMatch(config::isSphereArmadilloRW, "Шар Броненосца 2",
                List.of(), NameSpec.rename("Шар Броненосца", GRAY), List.of()),
            new RWSphereMatch(config::isSphereHadesRW, "Шар Аида 2",
                List.of(), NameSpec.rename("Шар Аида", RED), List.of()),

            new RWSphereMatch(config::isSphereBUNNYRW, "Шар BUNNY",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                NameSpec.color(GRAY),
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("Огнестойкость", GRAYSTROKE, false),
                    TextLine.of("+3.9 Броня", GRAYSTROKE, false),
                    TextLine.of("+21% Скорость", GRAYSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereDHELPERRW, "Шар Д.Хелпера",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                NameSpec.color(ORANGE),
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+7 Максимальное здоровье", ORANGESTROKE, false),
                    TextLine.of("+0.6 Скорость атаки", ORANGESTROKE, false),
                    TextLine.of("+3 Стойкость", ORANGESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isSphereDiabloRW, "Шар Диабло",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "При нажатии SHIFT:",
                    "+2 Урон к атаки",
                    "+2 Урон атаки",
                    "+7 Макс. здоровье",
                    "+3 Прочность брони",
                    "Проклятие утраты"
                ),
                NameSpec.color(RED),
                List.of(
                    TextLine.of("Пассивные эффекты:", REDSTROKE, true),
                    TextLine.of("+2 Урон", REDSTROKE, false),
                    TextLine.of("+7 Максимальное здоровье", REDSTROKE, false),
                    TextLine.of("+3 Прочность брони", REDSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isHeadBatmanRW, "Голова БетМена",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                NameSpec.color(GRAY),
                List.of(
                    TextLine.of("Пассивные эффекты:", GRAYMINIHEADER, true),
                    TextLine.of("+1.5 Урон", GRAYSTROKE, false),
                    TextLine.of("+2.5 Броня", GRAYSTROKE, false),
                    TextLine.of("+0.3 Скорость атаки", GRAYSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isHeadVampireRW, "Голова Дракулы",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadVampireRW, "Голова Вампира",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadJackRW, "Голова Джека",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadGrinchRW, "Голова Гринча",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadHydraRW, "Голова Гидры",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadIronManRW, "Голова Железного Человека",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadTigerRW, "Тигринная голова",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
                    "Когда в руке",
                    "При экиперовке в левой руке:",
                    "Когда во второcтепенной руке:",
                    "Когда во второй руке",
                    "Когда во второcтепенной руке",
                    "Когда надето на голову",
                    "Когда надето на голову:",
                    "Когда надето на голове:",
                    "При нажатии SHIFT:",
                    "+2.5 к урону",
                    "+1 здоровье",
                    "+25% к скорости",
                    "Проклятие утраты"
                ),
                NameSpec.color(ORANGE),
                List.of(
                    TextLine.of("Пассивные эффекты:", ORANGEMINIHEADER, true),
                    TextLine.of("+2.5 Урон", ORANGESTROKE, false),
                    TextLine.of("+25% Скорость", ORANGESTROKE, false),
                    TextLine.of("+1 Максимальное здоровье", ORANGESTROKE, false)
                )
            ),
            new RWSphereMatch(config::isHeadCobraRW, "Голова Кобры",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
                    TextLine.plain(""),
                    TextLine.of("Пассивные эффекты:", GREENMINIHEADER, true),
                    TextLine.of("+3 Урон", GREENSTROKE, false),
                    TextLine.of("+20% Скорость атаки", GREENSTROKE, false),
                    TextLine.of("+2.5 Стойкость", GREENSTROKE, false)
                )
            ),
            new RWSphereMatch(config::isHeadBunnyRW, "Голова Кролика",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadPegasusRW, "Голова Пегаса",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadPenguinRW, "Голова Пингвина",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadGingerbreadRW, "Голова Пряника",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadRudolphRW, "Голова Рудольфа",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadSantaRW, "Голова Санты",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadThorRW, "Голова Тора",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadHulkRW, "ГОЛОВА ХАЛКА",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadNutcrackerRW, "Голова Щелкунчика",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isHeadElfRW, "Голова Эльфа",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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
            new RWSphereMatch(config::isEasterEggRW, "Пасхальное яйцо",
                List.of(
                    "Когда во второй руке:",
                    "При экипировке в левой руке",
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

    public void applyReallyWorldMatch(ItemStack stack, List<Text> lines, RWSphereMatch match) {
        int nameIndex = betterSpheres.findLineIndex(lines, match.name);

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

        nameIndex = betterSpheres.findLineIndex(lines, match.name);

        if (nameIndex == -1) {
            return;
        }

        applyReallyWorldName(stack, match.nameSpec);

        int index = nameIndex + 1;

        for (TextLine line : match.outputLines) {
            lines.add(index++, line.create());
        }
    }

    private void applyReallyWorldName(ItemStack stack, NameSpec spec) {
        if (spec == null || spec.type == NameSpecType.NONE) {
            return;
        }

        switch (spec.type) {
            case COLOR -> betterSpheres.setNameColor(stack, spec.color1);
            case ALTERNATING -> betterSpheres.setAlternatingNameColors(stack, spec.color1, spec.color2);
            case SEGMENTED -> betterSpheres.setSegmentedName(stack, spec.segments);
            case RENAME -> {
                if (spec.color1 != null) {
                    betterSpheres.setNameColor(stack, spec.color1);
                }

                betterSpheres.renamePreservingStyle(stack, spec.newName);
            }
            case NONE -> { }
        }
    }
}