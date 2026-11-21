package com.zavk1n.bqol.features.totems;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.rct.BetterRCT;
import com.zavk1n.bqol.features.totems.holyworld.HolyWorldProcess;
import com.zavk1n.bqol.features.totems.reallyworld.ReallyWorldProcess;
import com.zavk1n.bqol.utils.chat.Chat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BetterTotems {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterTotems () {
        this.holyWorldProcess = new HolyWorldProcess(this, config);
        this.reallyWorldProcess = new ReallyWorldProcess(this, config);
    }

    private static BetterTotems instance;
    private MinecraftClient client;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static final class BlockedFeatures {
        boolean main;
        boolean counter;
        boolean holyworld;
        boolean reallyworld;
    }

    private final HolyWorldProcess holyWorldProcess;
    private final ReallyWorldProcess reallyWorldProcess;

    private final Map<UUID, Integer> pops = new HashMap<>();

    private record NameSegment(String text, int color) {}
    public enum NameSpecType {
        NONE,
        COLOR,
        ALTERNATING,
        SEGMENTED,
        RENAME
    }

    private enum ServerType {
        OTHER,
        HOLYWORLD,
        REALLYWORLD
    }

    private ServerType cachedServerType = ServerType.OTHER;
    private String cachedServerAddress;

    public static class NameSpec {
        static final NameSpec NONE = new NameSpec(NameSpecType.NONE, null, null, null, List.of());

        public final NameSpecType type;
        public final Integer color1;
        public final Integer color2;
        public final String newName;
        public final List<NameSegment> segments;

        public NameSpec(
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

        public static NameSpec color(int color) {
            return new NameSpec(NameSpecType.COLOR, color, null, null, List.of());
        }

        public NameSpec alternating(int color1, int color2) {
            return new NameSpec(NameSpecType.ALTERNATING, color1, color2, null, List.of());
        }

        public NameSpec segmented(List<NameSegment> segments) {
            return new NameSpec(NameSpecType.SEGMENTED, null, null, null, List.copyOf(segments));
        }

        public NameSpec rename(String newName) {
            return new NameSpec(NameSpecType.RENAME, null, null, newName, List.of());
        }

        public NameSpec rename(String newName, int color) {
            return new NameSpec(NameSpecType.RENAME, color, null, newName, List.of());
        }
    }

    public static class TextLine {
        final String text;
        final Integer color;
        final boolean bold;

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

        public Text create() {
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

    private static final Map<String, Pattern> PARAMETER_PATTERNS = PARAMETERS.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
            p -> p, p -> Pattern.compile("^" + Pattern.quote(p) + "(\\s+(\\d+|[IVXLCDM]+))?$")));

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

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!isEnabled()
                || stack == null
                || lines == null
                || lines.isEmpty()) {
                return;
            }

            updateServerType();

            switch (cachedServerType) {
                case HOLYWORLD -> processTooltipHolyWorld(stack, lines);
                case REALLYWORLD -> processTooltipReallyWorld(stack, lines);
                default -> {}
            }
        });
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

        HolyWorldProcess.TotemInfo totemsInfo = HolyWorldProcess.getTotemsInfo(originalName);
        HolyWorldProcess.TotemResult totemsResult = HolyWorldProcess.searchTotems(parameters);

        if (HolyWorldProcess.isCustomTotem(parameters) && totemsResult.totems() == null) {
            return;
        }

        HolyWorldProcess.applyTotemsParameters(lines, parameters, totemsInfo, totemsResult, totemsResult.totems());
        HolyWorldProcess.applyTotemsNames(stack, originalName, totemsInfo, totemsResult );
    }

    private void processTooltipReallyWorld(ItemStack stack, List<Text> lines) {
        if (blocked.main
            || blocked.reallyworld
            || lines == null
            || lines.isEmpty()) {
            return;
        }

        for (ReallyWorldProcess.TotemMatch match : ReallyWorldProcess.totemMatches) {
            if (!match.enabled.getAsBoolean() || !containsLine(lines, match.name)) {
                continue;
            }

            ReallyWorldProcess.applyTotemParameters(stack, lines, match);
        }
    }

    public static int findLineIndex(List<Text> lines, String text) {
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);

            if (line != null && line.getString().contains(text)) {
                return i;
            }
        }

        return -1;
    }

    public boolean containsLine(List<Text> lines, String text) {
        for (Text line : lines) {
            if (line != null && line.getString().contains(text)) {
                return true;
            }
        }

        return false;
    }

    public static void setNameColor(ItemStack stack, int color) {
        Text name = stack.getName();

        Style style = ReallyWorldProcess.getReallyWorldNameStyle(name)
            .withColor(color);

        MutableText result = Text.literal(name.getString())
            .setStyle(style);

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public static void renamePreservingStyle(ItemStack stack, String newName) {
        Text oldName = stack.getName();

        Style style = ReallyWorldProcess.getReallyWorldNameStyle(oldName);

        MutableText result = Text.literal(newName)
            .setStyle(style);

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public static void setAlternatingNameColors(ItemStack stack, int color1, int color2) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        String name = oldName.getString();

        Style baseStyle = ReallyWorldProcess.getReallyWorldNameStyle(oldName);

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

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public static void setSegmentedName(ItemStack stack, List<NameSegment> segments) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        Style baseStyle = ReallyWorldProcess.getReallyWorldNameStyle(oldName);

        for (NameSegment segment : segments) {
            result.append( Text.literal(segment.text())
                .setStyle( baseStyle.withColor(segment.color())));
        }

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
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

    public static boolean parametersMatch(Map<String, Integer> actual, Map<String, Integer> required) {
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

    public static void replaceParameterLines(List<Text> lines, List<String> targetLines, List<Integer> colors) {
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

        instance.removeParameterLines(lines);

        for (int i = 0; i < targetLines.size(); i++) {
            final int color = colors.get(i);

            lines.add(insertIndex++, Text.literal("◆ " + targetLines.get(i)).styled(style -> style
                .withColor(color)
                .withItalic(false))
            );
        }
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

    private void updateServerType() {
        MinecraftClient client = mc();
        ServerInfo server = client.getCurrentServerEntry();

        String address = server != null ? server.address : null;

        if (Objects.equals(cachedServerAddress, address)) {
            return;
        }

        cachedServerAddress = address;

        if (address == null || address.isEmpty()) {
            cachedServerType = ServerType.OTHER;
            return;
        }

        String normalized = address.toLowerCase(Locale.ROOT);

        if (normalized.contains("holyworld")) {
            cachedServerType = ServerType.HOLYWORLD;
        } else if (normalized.contains("reallyworld")) {
            cachedServerType = ServerType.REALLYWORLD;
        } else {
            cachedServerType = ServerType.OTHER;
        }
    }

    private boolean isHolyWorld() {
        return cachedServerType == ServerType.HOLYWORLD;
    }

    private boolean isReallyWorld() {
        return cachedServerType == ServerType.REALLYWORLD;
    }
}