package com.zavk1n.bqol.features.spheres;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.spheres.holyworld.HolyWorldProcess;
import com.zavk1n.bqol.features.spheres.reallyworld.ReallyWorldProcess;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BetterSpheres {

    private BetterSpheres() {
        this.holyWorldProcess = new HolyWorldProcess(this, config);
        this.reallyWorldProcess = new ReallyWorldProcess(this, config);
    }

    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private static BetterSpheres instance;
    private MinecraftClient client;
    private final MainConfig config = MainConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static class BlockedFeatures {
        boolean main;
        boolean holyWorldDefault;
        boolean holyWorldGolden;
        boolean reallyWorld;
        boolean funTime;
    }

    private final HolyWorldProcess holyWorldProcess;
    private final ReallyWorldProcess reallyWorldProcess;

    private boolean registered;

    private enum ServerType {
        OTHER,
        HOLYWORLD,
        REALLYWORLD,
    }

    private ServerType cachedServerType = ServerType.OTHER;
    private String cachedServerAddress;

    private static final String DAMAGE = "Урон";
    private static final String ARMOR = "Броня";
    private static final String SPEED = "Скорость";
    private static final String HEALTH = "Макс. здоровье";
    private static final String HASTE = "Спешка";
    private static final String ATTACK_SPEED = "Скорость атаки";
    private static final String VANISHING = "Проклятие утраты";

    static final List<String> PARAMETERS = List.of(
        DAMAGE, ARMOR, SPEED, HEALTH, HASTE, ATTACK_SPEED, VANISHING
    );

    private static final Map<String, Pattern> PARAMETER_PATTERNS = PARAMETERS.stream().collect(Collectors.toUnmodifiableMap(
            p -> p, p -> Pattern.compile("^" + Pattern.quote(p) + "(\\s+(\\d+|[IVXLCDM]+))?$")));

    private static final Pattern ROMAN_PATTERN = Pattern.compile("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
    private static final Pattern BULLET_PREFIX_PATTERN = Pattern.compile("^[◆▪•✦*]\\s*");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\b(\\d+)\\b");
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORa-fk-or]");

    private static final Map<Character, Integer> CONVERTER = Map.of(
        'I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000
    );

    public static void initialize() {
        if (instance == null) {
            instance = new BetterSpheres();
            instance.refreshBlockedStatusInternal();
            instance.registerCallbacks();
            BQoL.LOGGER.info("Better Spheres initialized");
        }
    }

    public static BetterSpheres getInstance() {
        if (instance == null) initialize();
        return instance;
    }

    public static void refreshBlockedStatus() {
        if (instance != null) instance.refreshBlockedStatusInternal();
    }

    public static boolean isEnabled() {
        return instance != null && instance.isEnabledInternal();
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) instance.setEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_spheres");
        blocked.holyWorldDefault = LiteApiManager.isFeatureBlocked("better_spheres_holyworld");
        blocked.holyWorldGolden = LiteApiManager.isFeatureBlocked("better_spheres_holyworld_golden");
        blocked.reallyWorld = LiteApiManager.isFeatureBlocked("better_spheres_reallyworld");
        blocked.funTime = LiteApiManager.isFeatureBlocked("better_spheres_funtime");
    }

    private boolean isEnabledInternal() {
        return config.isBetterSpheresEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterSpheresEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    public boolean isHolyWorldBlocked() { return blocked.holyWorldDefault; }
    public boolean isHolyWorldGoldenBlocked() { return blocked.holyWorldGolden; }
    public boolean isReallyWorldBlocked() { return blocked.reallyWorld; }
    public boolean isFunTimeBlocked() { return blocked.funTime; }

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
        if (!config.isHolyWorldSpheresEnabled() || blocked.holyWorldDefault) {
            return;
        }

        Map<String, Integer> parameters = parseParametersFromTooltip(lines);

        if (parameters.isEmpty()) {
            return;
        }

        Text firstLine = lines.get(0);

        if (firstLine == null) {
            return;
        }

        String originalName = COLOR_PATTERN
            .matcher(firstLine.getString())
            .replaceAll("")
            .trim();

        HolyWorldProcess.SphereInfo sphereInfo = holyWorldProcess.getSphereInfo(originalName);
        HolyWorldProcess.SphereResult sphereResult = holyWorldProcess.searchSpheres(parameters);

        holyWorldProcess.applySpheresParameters(lines, parameters, sphereInfo, sphereResult);
        holyWorldProcess.applySphereNames(stack, originalName, sphereInfo, sphereResult);
    }

    private void processTooltipReallyWorld(ItemStack stack, List<Text> lines) {
        if (blocked.main || blocked.reallyWorld || lines == null || lines.isEmpty()) {
            return;
        }

        for (ReallyWorldProcess.RWSphereMatch match : reallyWorldProcess.rwSphereMatches) {
            if (!match.enabled.getAsBoolean() || !containsLine(lines, match.name)) {
                continue;
            }

            reallyWorldProcess.applyReallyWorldMatch(stack, lines, match);
        }
    }

    public Map<String, Integer> parseParametersFromTooltip(List<Text> lines) {
        Map<String, Integer> result = new HashMap<>();

        for (Text line : lines) {
            if (line == null) {
                continue;
            }

            String raw = line.getString().trim();

            if (raw.isEmpty()) {
                continue;
            }

            raw = BULLET_PREFIX_PATTERN.matcher(raw).replaceFirst("");

            for (Map.Entry<String, Pattern> entry : PARAMETER_PATTERNS.entrySet()) {
                if (!entry.getValue().matcher(raw).find()) {
                    continue;
                }

                String parameter = entry.getKey();

                int level = parameter.equals(VANISHING) ? 0 : extractLevel(raw);

                if (!parameter.equals(VANISHING) && level < 0) {
                    continue;
                }

                result.put(parameter, level);

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

        return romanToInt(parts[parts.length - 1].toUpperCase(Locale.ROOT));
    }

    private int romanToInt(String roman) {
        if (roman == null
            || roman.isBlank()
            || !ROMAN_PATTERN.matcher(roman).matches()) {
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

    private void removeParameterLines(List<Text> lines) {
        lines.removeIf(line -> {
            String clean = line.getString().trim();

            clean = BULLET_PREFIX_PATTERN.matcher(clean).replaceFirst("");

            for (Pattern pattern : PARAMETER_PATTERNS.values()) {
                if (pattern.matcher(clean).matches()) {
                    return true;
                }
            }

            return false;
        });
    }

    public void replaceParameterLines(List<Text> lines, List<String> targetLines, List<Integer> colors) {
        if (targetLines == null
            || colors == null
            || targetLines.size() != colors.size()) {
            return;
        }

        int insertIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            String clean = lines.get(i).getString().trim();

            clean = BULLET_PREFIX_PATTERN.matcher(clean).replaceFirst("");

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

    private boolean containsLine(List<Text> lines, String text) {
        for (Text line : lines) {
            if (line != null && line.getString().contains(text)) {
                return true;
            }
        }

        return false;
    }

    public int findLineIndex(List<Text> lines, String text) {
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);

            if (line != null && line.getString().contains(text)) {
                return i;
            }
        }

        return -1;
    }

    public void setNameColor(ItemStack stack, int color) {
        Text name = stack.getName();

        Style style = getReallyWorldNameStyle(name).withColor(color);

        MutableText result = Text.literal(name.getString()).setStyle(style);

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public void renamePreservingStyle(ItemStack stack, String newName) {
        Text oldName = stack.getName();

        Style style = getReallyWorldNameStyle(oldName);

        MutableText result = Text.literal(newName).setStyle(style);

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public void setAlternatingNameColors(ItemStack stack, int color1, int color2) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        String name = oldName.getString();

        Style baseStyle = getReallyWorldNameStyle(oldName);

        int letterIndex = 0;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isWhitespace(c)) {
                result.append(Text.literal(String.valueOf(c)).setStyle(baseStyle));
                continue;
            }

            int color = (letterIndex % 2 == 0) ? color1 : color2;

            result.append(Text.literal(String.valueOf(c)).setStyle(baseStyle.withColor(color)));

            letterIndex++;
        }

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public void setSegmentedName(ItemStack stack, List<ReallyWorldProcess.NameSegment> segments) {
        Text oldName = stack.getName();

        MutableText result = Text.empty();

        Style baseStyle = getReallyWorldNameStyle(oldName);

        for (ReallyWorldProcess.NameSegment segment : segments) {
            result.append(Text.literal(segment.text()).setStyle(baseStyle.withColor(segment.color())));
        }

        stack.set(DataComponentTypes.CUSTOM_NAME, result);
    }

    public Style getReallyWorldNameStyle(Text name) {
        return (name.getStyle() != null ? name.getStyle() : Style.EMPTY)
            .withBold(true)
            .withItalic(false);
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
        }
        else {
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