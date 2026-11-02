package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class  BetterTotems {
    private MinecraftClient mc() {
        if (client == null) client = MinecraftClient.getInstance();
        return client;
    }

    private BetterTotems() {
        hwTotemsMatches = createTotemsMatchesHolyWorld();
        rwTotemsMatches = createTotemsMatchesReallyWorld();
    }

    private static BetterTotems instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static final class BlockedFeatures {
        boolean main;
        boolean counter;
        boolean holyworld;
        boolean reallyworld;
    }

    private final Map<UUID, Integer> pops = new HashMap<>();

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

    private static final int RED = 0xFF5555;
    private static final int REDMINIHEADER = 0xFF7A7A;
    private static final int REDSTROKE = 0xFF9E9E;

    private static final int PURPLE = 0x8A52CC;
    private static final int PURPLEMINIHEADER = 0x8F69BF;
    private static final int PURPLESTROKE = 0xA98FCC;

    private enum TotemsType {
        DEFAULT,
        EPIC,
        LEGENDARY,
        CUSTOM
    }

    private static final class TotemsPalette {
        private TotemsPalette() {}

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

    private static final Map<String, Pattern> PARAMETER_PATTERNS =
        PARAMETERS.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
            p -> p,
            p -> Pattern.compile(
                "^" + Pattern.quote(p) + "(\\s+(\\d+|[IVXLCDM]+))?$"
            )
        ));

    private final List<HWTotemsMatch> hwTotemsMatches;
    private static class HWTotemsMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final List<String> targetLines;
        final List<Integer> colors;

        HWTotemsMatch(
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

    private record HWTotemsInfo(
        TotemsType type,
        int nameColor,
        List<Integer> gradient
    ) {}

    private record HWTotemsResult(
        HWTotemsMatch hwTotems
    ) {}

    private final List<RWTotemsMatch> rwTotemsMatches;
    private static final class RWTotemsMatch {
        final java.util.function.BooleanSupplier enabled;
        final String name;
        final List<String> hiddenLines;
        final NameSpec nameSpec;
        final List<TextLine> outputLines;

        private RWTotemsMatch(java.util.function.BooleanSupplier enabled, String name, List<String> hiddenLines, NameSpec nameSpec, List<TextLine> outputLines) {
            this.enabled = enabled;
            this.name = name;
            this.hiddenLines = List.copyOf(hiddenLines);
            this.nameSpec = nameSpec;
            this.outputLines = List.copyOf(outputLines);
        }
    }

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

    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORa-fk-or]");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\b(\\d+)\\b");

    private boolean registered;
    private static boolean hudRegistered = false;

    public static void initialize() {
        if (instance == null) {
            instance = new BetterTotems();
            instance.refreshBlockedStatusInternal();
            instance.registerCallbacks();
            BQoL.LOGGER.info("Better Totems initialized");
        }
    }

    public static BetterTotems getInstance() {
        if (instance == null)
            initialize();

        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) {
            instance.refreshBlockedStatusInternal();
        }
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        instance.setEnabledInternal(enabled);
    }

    public static boolean isTotemCounterEnabled() {
        return instance != null && instance.isTotemCounterEnabledInternal();
    }

    public static void setTotemCounterEnabled(boolean enabled) {
        instance.setTotemCounterEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_totems");
        blocked.counter = LiteApiManager.isFeatureBlocked("better_totems_counter");
        blocked.holyworld = LiteApiManager.isFeatureBlocked("better_totems_holyworld");
        blocked.reallyworld = LiteApiManager.isFeatureBlocked("better_totems_reallyworld");
    }

    private boolean isEnabledInternal() {
        return config.isBetterTotemsEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterTotemsEnabled(enabled);

        refreshBlockedStatusInternal();
    }

    private boolean isTotemCounterEnabledInternal() {
        return config.isBetterTotemsCounterEnabled() && !blocked.counter;
    }

    private void setTotemCounterEnabledInternal(boolean enabled) {
        config.setBetterTotemsCounterEnabled(enabled);
    }

    private void registerCallbacks() {
        if (registered) {
            return;
        }

        registered = true;

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!isEnabled()
                || stack == null
                || lines == null || lines.isEmpty()) {
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

    private List<HWTotemsMatch> createTotemsMatchesHolyWorld() {
        return List.of(
            new HWTotemsMatch(
                config::isTotemInfinity,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2, "Макс. здоровье", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2", "Макс. здоровье 2"),
                List.of(0x00F231, 0x26FF53, 0x63FF84, 0x94FFAB)
            ),
            new HWTotemsMatch(
                config::isTotemEternity,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2"),
                List.of(0xFF008C, 0xFF47A9, 0xFF69B9)
            ),
            new HWTotemsMatch(
                config::isTotemStinger,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 1),
                List.of("Урон 2", "Броня 2", "Скорость 1"),
                List.of(0xFF2600, 0xFF4E2E, 0xFF6E54)
            ),
            new HWTotemsMatch(
                config::isTotemSatires,
                Map.of("Урон", 3, "Спешка", 2),
                List.of("Урон 3", "Спешка 2"),
                List.of(0x00FFFB, 0x87FFFE)
            )
        );
    }

    private List<RWTotemsMatch> createTotemsMatchesReallyWorld() {
        return List.of(
            new RWTotemsMatch(config::isTotemDrakonid, "Тотем Драконида",
                List.of(
                    "Срабатывает, когда",
                    "лежит в инвентаре"
                ),
                NameSpec.color(RED),
                List.of(TextLine.of("Пассивные эффекты:", REDMINIHEADER, true),
                    TextLine.of("Срабатывает, когда", REDSTROKE, false),
                    TextLine.of("лежит в инвентаре", REDSTROKE, false)
                )
            ),
            new RWTotemsMatch(config::isTotemWitch, "Тотем Ведьмы",
                List.of(
                    "Сохраняет эффекты"
                ),
                NameSpec.color(PURPLE),
                List.of(TextLine.of("Пассивные эффекты:", PURPLEMINIHEADER, true),
                    TextLine.of("Сохраняет эффекты", PURPLESTROKE, false)
                )
            )
        );
    }

    private void processTooltipHolyWorld(ItemStack stack, List<Text> lines) {
        if (!config.isHolyWorldTotemsEnabled()
            || blocked.main || blocked.holyworld) {
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

        String normalizedName = originalName.toLowerCase(Locale.ROOT);
        if (!normalizedName.contains("тотем") && !normalizedName.contains("талисман")
            && !normalizedName.contains("infinity") && !normalizedName.contains("eternity")
            && !normalizedName.contains("stinger") && !normalizedName.contains("сатир")) {
            return;
        }

        HWTotemsInfo hwTotemsInfo = getHWTotemsInfo(originalName);
        HWTotemsResult hwTotemsResult = searchHWTotems(parameters);

        applyHWTotemsParameters(
            lines,
            parameters,
            hwTotemsInfo,
            hwTotemsResult,
            hwTotemsResult.hwTotems()
        );
        applyHWTotemsNames(stack, originalName, hwTotemsInfo, hwTotemsResult);
    }

    private void processTooltipReallyWorld(ItemStack stack, List<Text> lines) {
        if (blocked.main || blocked.reallyworld || lines == null || lines.isEmpty()) {
            return;
        }

        List<String> tooltip = lines.stream()
            .filter(Objects::nonNull)
            .map(Text::getString)
            .toList();

        for (RWTotemsMatch match : rwTotemsMatches) {
            if (!match.enabled.getAsBoolean() || !containsLine(tooltip, match.name)) {
                continue;
            }

            applyReallyWorldMatch(stack, lines, match);
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

    private boolean containsLine(List<String> lines, String text) {
        return lines.stream()
            .anyMatch(line -> line.contains(text));
    }

    private void setNameColor(ItemStack stack, int color) {
        Text name = stack.getName();

        Style style = getReallyWorldNameStyle(name)
            .withColor(color);

        MutableText result = Text.literal(name.getString())
            .setStyle(style);

        stack.setCustomName(result);
    }

    private void renamePreservingStyle(ItemStack stack, String newName) {
        Text oldName = stack.getName();

        Style style = getReallyWorldNameStyle(oldName);

        MutableText result = Text.literal(newName)
            .setStyle(style);

        stack.setCustomName(result);
    }

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

    private HWTotemsResult searchHWTotems(Map<String, Integer> parameters) {
        return new HWTotemsResult(findHWTotems(parameters));
    }

    private HWTotemsMatch findHWTotems(Map<String, Integer> parameters) {
        for (HWTotemsMatch match : hwTotemsMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (parametersMatch(parameters, match.requiredParameters)) {
                return match;
            }
        }

        return null;
    }

    private HWTotemsInfo getHWTotemsInfo(String name) {
        if (name == null) {
            return null;
        }

        final List<String> CUSTOM_KEYWORDS = List.of("INFINITY", "ETERNITY", "STINGER", "Сатира", "Сатиры");

        for (String keyword : CUSTOM_KEYWORDS) {
            if (name.toUpperCase(Locale.ROOT).contains(keyword.toUpperCase(Locale.ROOT))) {
                return new HWTotemsInfo(
                    TotemsType.CUSTOM,
                    0xFFFFFF,
                    List.of(0xCCCCCC, 0xAAAAAA, 0x888888)
                );
            }
        }

        if (name.toLowerCase(Locale.ROOT).contains("легендарн")) {
            return new HWTotemsInfo(
                TotemsType.LEGENDARY,
                TotemsPalette.LEGENDARY_NAME,
                TotemsPalette.LEGENDARY_GRADIENT
            );
        }

        if (name.toLowerCase(Locale.ROOT).contains("эпическ")) {
            return new HWTotemsInfo(
                TotemsType.EPIC,
                TotemsPalette.EPIC_NAME,
                TotemsPalette.EPIC_GRADIENT
            );
        }

        if (name.toLowerCase(Locale.ROOT).contains("обычн")) {
            return new HWTotemsInfo(
                TotemsType.DEFAULT,
                TotemsPalette.DEFAULT_NAME,
                TotemsPalette.DEFAULT_GRADIENT
            );
        }

        return null;
    }

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

            for (Map.Entry<String, Pattern> entry : PARAMETER_PATTERNS.entrySet()) {
                if (!entry.getValue().matcher(raw).find()) {
                    continue;
                }

                String parameter = entry.getKey();

                result.put(parameter, extractLevel(raw));
                break;
            }
        }

        return result;
    }

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

    private int romanToInt(String roman) {
        if (roman == null
            || roman.isBlank()
            || !roman.matches(
            "^M{0,4}(CM|CD|D?C{0,3})" + "(XC|XL|L?X{0,3})" + "(IX|IV|V?I{0,3})$")) {
            return 0;
        }

        int result = 0;

        for (int i = 0; i < roman.length(); i++) {
            int current = CONVERTER.getOrDefault(roman.charAt(i), 0);

            if (i + 1 < roman.length()) {
                int next = CONVERTER.getOrDefault(roman.charAt(i + 1), 0);

                if (current < next) {
                    result -= current;
                    continue;
                }
            }

            result += current;
        }

        return result;
    }

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

    private void removeParameterLines(List<Text> lines) {
        lines.removeIf(line -> {
            if (line == null) return false;
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
            Text line = lines.get(i);
            if (line == null) {
                continue;
            }

            String clean = line.getString().trim();

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

    private void applyHWTotemsParameters(List<Text> lines, Map<String, Integer> parameters, HWTotemsInfo totemsInfo, HWTotemsResult hwTotemsResult, HWTotemsMatch hwTotemsMatch) {
        if (hwTotemsMatch != null) {
            replaceParameterLines(lines, hwTotemsMatch.targetLines, hwTotemsMatch.colors);
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

        replaceParameterLines(lines, orderedLines, orderedColors);
    }

    private void applyHWTotemsNames(ItemStack stack, String originalName, HWTotemsInfo totemsInfo, HWTotemsResult result) {
        if (totemsInfo == null
            || totemsInfo.type() == TotemsType.LEGENDARY
            || totemsInfo.type() == TotemsType.CUSTOM) {
            return;
        }

        stack.setCustomName(Text.literal(originalName)
            .styled(style -> style
                .withColor(totemsInfo.nameColor())
                .withItalic(false))
        );
    }

    private void applyReallyWorldMatch(ItemStack stack, List<Text> lines, RWTotemsMatch match) {
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

        applyReallyWorldNames(stack, match.nameSpec);

        int index = nameIndex + 1;

        for (TextLine line : match.outputLines) {
            lines.add(index++, line.create());
        }
    }

    private void applyReallyWorldNames(ItemStack stack, NameSpec spec) {
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

    private Style getReallyWorldNameStyle(Text name) {
        return (name.getStyle() != null ? name.getStyle() : Style.EMPTY)
            .withBold(true)
            .withItalic(false);
    }

    public boolean onChat(String message) {
        if (message == null) {
            return true;
        }

        String trimmed = message.trim();

        if (!trimmed.equalsIgnoreCase(".totemcounter") && !trimmed.toLowerCase(java.util.Locale.ROOT)
            .startsWith(".totemcounter ")) {
            return true;
        }

        String[] args = trimmed.split("\\s+");

        if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
            reset();
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        Chat.send(client, Text.literal("Использование: .totemcounter reset")
            .styled(style -> style.withColor(BetterRCT.getInstance().MESSAGE_COLOR)));

        return false;
    }

    public void onTotemPop(PlayerEntity player) {
        if (player != null) {
            registerPop(player.getUuid());
        }
    }

    public void registerPop(UUID uuid) {
        if (uuid == null) {
            return;
        }

        pops.merge(uuid, 1, Integer::sum);
    }

    public int getPops(UUID uuid) {
        if (uuid == null) {
            return 0;
        }

        return pops.getOrDefault(uuid, 0);
    }

    public Map<UUID, Integer> getAllPops() {
        return Collections.unmodifiableMap(pops);
    }

    public void reset() {
        pops.clear();

        Chat.info(mc(), Text.literal("Счетчики тотемов были сброшены.")
            .styled(style -> style
                .withColor(BetterRCT.getInstance().MESSAGE_COLOR)
                .withBold(true))
        );
    }

    public void resetPlayer(UUID uuid) {
        if (uuid != null) {
            pops.remove(uuid);
        }
    }

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