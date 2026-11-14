package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BetterPrefixes {

    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterPrefixes() {}

    private static BetterPrefixes instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    private final BlockedFeatures blocked = new BlockedFeatures();
    private static final class BlockedFeatures {
        private boolean main;
        private boolean nameProtect;
        private boolean donate;
        private boolean title;
    }

    private static final Pattern PREFIX_PATTERN = Pattern.compile("^\\s*[GgɢＧLlʟＬ]\\s*\\|\\s*");
    private static final Pattern SCOREBOARD_NAME_PATTERN = Pattern.compile("(?i)Ник\\s*:\\s*(\\S+)|Вы\\s*:\\s*(\\S+)");
    private static final Pattern SCOREBOARD_DONATE_PATTERN = Pattern.compile("(?iu)(?:Группа|Ранг)\\s*:\\s*(\\S+)");
    private static final Pattern SCOREBOARD_TITLE_PATTERN = Pattern.compile("(?i)Титул\\s*:\\s*(.*\\S)(\\s*)$");

    private record StyledPart(String text, Style style) {}

    private record Replacement(
        int start,
        int end,
        String value,
        Style style
    ) {}

    private record FlattenedText(
        String text,
        Style[] styles
    ) {}

    public static void initialize() {
        if (instance != null) {
            return;
        }
        instance = new BetterPrefixes();
        instance.refreshBlockedStatusInternal();
        BQoL.LOGGER.info("Better Prefixes initialized");
    }

    public static BetterPrefixes getInstance() {
        if (instance == null) {
            initialize();
        }

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

    public static boolean isNameProtectEnabled() {
        return instance != null && instance.isNameProtectEnabledInternal();
    }

    public static void setNameProtectEnabled(boolean enabled) {
        instance.setNameProtectEnabledInternal(enabled);
    }

    public static boolean isNameColorEnabled() {
        return instance != null && instance.isNameColorEnabledInternal();
    }

    public static void setNameColorEnabled(boolean enabled) {
        instance.setNameColorEnabledInternal(enabled);
    }

    public static boolean isDonateEnabled() {
        return instance != null && instance.isDonateEnabledInternal();
    }

    public static void setDonateEnabled(boolean enabled) {
        instance.setDonateEnabledInternal(enabled);
    }

    public static boolean isDonateColorEnabled() {
        return instance != null && instance.isDonateColorEnabledInternal();
    }

    public static void setDonateColorEnabled(boolean enabled) {
        instance.setDonateColorEnabledInternal(enabled);
    }

    public static boolean isTitleEnabled() {
        return instance != null && instance.isTitleEnabledInternal();
    }

    public static void setTitleEnabled(boolean enabled) {
        instance.setTitleEnabledInternal(enabled);
    }

    public static boolean isTitleColorEnabled() {
        return instance != null && instance.isTitleColorEnabledInternal();
    }

    public static void setTitleColorEnabled(boolean enabled) {
        instance.setTitleColorEnabledInternal(enabled);
    }

    private void refreshBlockedStatusInternal() {
        blocked.main = LiteApiManager.isFeatureBlocked("better_prefixes");
        blocked.nameProtect = LiteApiManager.isFeatureBlocked("better_prefixes_name_protect");
        blocked.donate = LiteApiManager.isFeatureBlocked("better_prefixes_donate");
        blocked.title = LiteApiManager.isFeatureBlocked("better_prefixes_title");
    }

    private boolean isEnabledInternal() {
        return config.isBetterPrefixesEnabled() && !blocked.main;
    }

    private void setEnabledInternal(boolean enabled) {
        config.setBetterPrefixesEnabled(enabled);
        refreshBlockedStatusInternal();
    }

    private boolean isNameProtectEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterPrefixesNameProtect()
            && !blocked.nameProtect;
    }

    private void setNameProtectEnabledInternal(boolean enabled) {
        config.setBetterPrefixesNameProtect(enabled);
    }

    private boolean isNameColorEnabledInternal() {
        return isEnabledInternal() && config.betterPrefixesNameColor;
    }

    private void setNameColorEnabledInternal(boolean enabled) {
        config.betterPrefixesNameColor = enabled;
        config.save();
    }

    private boolean isDonateEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterPrefixesDonate()
            && !blocked.donate;
    }

    private void setDonateEnabledInternal(boolean enabled) {
        config.setBetterPrefixesDonate(enabled);
    }

    private boolean isDonateColorEnabledInternal() {
        return isDonateEnabledInternal() && config.betterPrefixesDonateColor;
    }

    private void setDonateColorEnabledInternal(boolean enabled) {
        config.betterPrefixesDonateColor = enabled;
        config.save();
    }

    private boolean isTitleEnabledInternal() {
        return isEnabledInternal()
            && config.isBetterPrefixesTitle()
            && !blocked.title;
    }

    private void setTitleEnabledInternal(boolean enabled) {
        config.setBetterPrefixesTitle(enabled);
    }

    private boolean isTitleColorEnabledInternal() {
        return isTitleEnabledInternal()
            && config.betterPrefixesTitleColor;
    }

    private void setTitleColorEnabledInternal(boolean enabled) {
        config.betterPrefixesTitleColor = enabled;
        config.save();
    }

    private boolean starEnabled() {
        return config.betterPrefixesStar;
    }

    private boolean lightningEnabled() {
        return config.betterPrefixesLightning;
    }

    public boolean isLocalPlayerName(String name) {
        if (name == null || !isEnabledInternal()) {
            return false;
        }

        MinecraftClient minecraft = mc();

        return minecraft.player != null
            && minecraft.player.getNameForScoreboard().equalsIgnoreCase(name);
    }

    private String getProtectedName(String fallback) {
        String value = config.betterPrefixesProtectedName;

        return value == null || value.isBlank() ? fallback : value;
    }

    private String getDonateText() {
        String value = config.betterPrefixesDonateText;

        if (value == null) {
            return "";
        }

        String trimmed = value.trim();

        return isKnownDonateRank(trimmed)
            ? toSmallCaps(trimmed)
            : value;
    }

    private boolean isKnownDonateRank(String value) {
        if (value == null) {
            return false;
        }

        return switch (value.toUpperCase(java.util.Locale.ROOT)) {
            case
                "GRIEFER",
                "MUSTANG",
                "GHAST",
                "WITHER",
                "KRAKEN",
                "DRAGON",
                "STINGER",
                "ETERNITY",
                "HOLY",
                "PALADIN",
                "MASTER",
                "GLADIATOR",
                "OVERLORD",
                "ELDER",
                "PHANTOM",
                "KING" -> true;

            default -> false;
        };
    }

    private String toSmallCaps(String value) {
        StringBuilder result = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));

            result.append(switch (c) {
                case 'a' -> 'ᴀ';
                case 'b' -> 'ʙ';
                case 'c' -> 'ᴄ';
                case 'd' -> 'ᴅ';
                case 'e' -> 'ᴇ';
                case 'f' -> 'ꜰ';
                case 'g' -> 'ɢ';
                case 'h' -> 'ʜ';
                case 'i' -> 'ɪ';
                case 'j' -> 'ᴊ';
                case 'k' -> 'ᴋ';
                case 'l' -> 'ʟ';
                case 'm' -> 'ᴍ';
                case 'n' -> 'ɴ';
                case 'o' -> 'ᴏ';
                case 'p' -> 'ᴘ';
                case 'q' -> 'ǫ';
                case 'r' -> 'ʀ';
                case 's' -> 's';
                case 't' -> 'ᴛ';
                case 'u' -> 'ᴜ';
                case 'v' -> 'ᴠ';
                case 'w' -> 'ᴡ';
                case 'x' -> 'x';
                case 'y' -> 'ʏ';
                case 'z' -> 'ᴢ';
                default -> value.charAt(i);
            });
        }

        return result.toString();
    }

    private String getTitleText() {
        String value = config.betterPrefixesTitleText;
        return value == null ? "" : value;
    }

    private MutableText makePart(String value, boolean useColor, int rgb, boolean bold, Style baseStyle) {
        MutableText result = Text.literal(value);

        Style style = baseStyle == null ? Style.EMPTY : baseStyle;

        if (useColor) {
            style = style.withColor(TextColor.fromRgb(rgb & 0xFFFFFF));
        }

        if (bold) {
            style = style.withBold(true);
        }

        result.setStyle(style);

        return result;
    }

    private MutableText makeStar(Style baseStyle) {
        return makePart("★", true, config.betterPrefixesStarColorRgb, true, baseStyle);
    }

    private void appendStar(MutableText result) {
        result.append(makeStar(Style.EMPTY));
        result.append(Text.literal(" "));
    }

    private MutableText makeLightning(Style baseStyle) {
        return makePart("⚡", true, config.betterPrefixesLightningColorRgb, false, baseStyle);
    }

    private void appendLightning(MutableText result) {
        result.append(makeLightning(Style.EMPTY));
        result.append(Text.literal(" "));
    }

    public Text formatName(String originalName) {
        if (originalName == null) {
            return Text.literal("");
        }

        if (!isEnabledInternal()) {
            return Text.literal(originalName);
        }

        MinecraftClient minecraft = mc();

        if (minecraft.player == null) {
            return Text.literal(originalName);
        }

        String realName = minecraft.player.getNameForScoreboard();

        if (!originalName.equalsIgnoreCase(realName)) {
            return Text.literal(originalName);
        }

        MutableText result = Text.empty();

        if (starEnabled()) {
            appendStar(result);
        }

        if (lightningEnabled()) {
            appendLightning(result);
        }

        if (isDonateEnabledInternal()) {
            String donate = getDonateText();

            if (!donate.isBlank()) {
                result.append(makePart(donate + " ", isDonateColorEnabledInternal(), config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold, Style.EMPTY));
            }
        }

        String displayedName = isNameProtectEnabledInternal() ? getProtectedName(realName) : realName;

        result.append(makePart(displayedName, isNameColorEnabledInternal(), config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold, Style.EMPTY));

        if (isTitleEnabledInternal()) {
            String title = getTitleText();

            if (!title.isBlank()) {
                result.append(Text.literal(" "));
                result.append(makePart(title, isTitleColorEnabledInternal(), config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold, Style.EMPTY));
            }
        }

        return result;
    }

    private List<StyledPart> collectParts(Text original) {
        List<StyledPart> parts = new ArrayList<>();

        original.visit((style, text) -> {
            if (!text.isEmpty()) {
                parts.add(new StyledPart(text, style));
            }

            return Optional.empty();
        }, Style.EMPTY);

        return parts;
    }

    private FlattenedText flattenText(Text original) {
        List<StyledPart> parts = collectParts(original);

        StringBuilder flattened = new StringBuilder();

        for (StyledPart part : parts) {
            flattened.append(part.text());
        }

        String plain = flattened.toString();
        Style[] styles = new Style[plain.length()];

        int offset = 0;

        for (StyledPart part : parts) {
            int end = Math.min(plain.length(), offset + part.text().length());

            java.util.Arrays.fill(styles, offset, end, part.style());

            offset = end;
        }

        return new FlattenedText(plain, styles);
    }

    private String getPlainText(Text original) {
        return original == null ? "" : original.getString();
    }

    private Style getStyleAt(Style[] styles, int position) {
        return position >= 0 && position < styles.length && styles[position] != null ? styles[position] : Style.EMPTY;
    }

    private Style getStyleAt(Text original, int position) {
        return getStyleAt(flattenText(original).styles(), position);
    }

    private Style replacementStyle(Text original, int position, boolean useColor, int rgb, boolean bold) {
        Style style = getStyleAt(original, position);

        if (useColor) {
            style = style.withColor(TextColor.fromRgb(rgb & 0xFFFFFF));
        }

        if (bold) {
            style = style.withBold(true);
        }

        return style;
    }

    private static int findPlayerName(String text, String name, int fromIndex) {
        int nameLength = name.length();
        int textLength = text.length();

        for (int i = Math.max(0, fromIndex); i <= textLength - nameLength; i++) {
            if (!text.regionMatches(true, i, name, 0, nameLength)) {
                continue;
            }

            int end = i + nameLength;

            boolean validStart = i == 0 || !isNameCharacter(text.charAt(i - 1));
            boolean validEnd = end == textLength || !isNameCharacter(text.charAt(end));

            if (validStart && validEnd) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isNameCharacter(char c) {
        return c >= 'a' && c <= 'z'
            || c >= 'A' && c <= 'Z'
            || c >= '0' && c <= '9'
            || c == '_';
    }

    private Text replaceRangesWithInsertions(Text original, List<Replacement> replacements) {
        if (replacements.isEmpty()) {
            return original;
        }

        FlattenedText flattened = flattenText(original);

        String plain = flattened.text();
        Style[] styles = flattened.styles();

        replacements.sort(Comparator.comparingInt(Replacement::start)
            .thenComparingInt(r -> r.start() == r.end() ? 0 : 1)
        );

        MutableText output = Text.empty();

        int cursor = 0;

        for (Replacement replacement : replacements) {
            int start = replacement.start();
            int end = replacement.end();

            if (start < cursor
                || start < 0
                || end > plain.length()
                || end < start) {
                continue;
            }

            appendStyledRange(output, plain, styles, cursor, start);

            output.append(Text.literal(replacement.value())
                .setStyle(replacement.style() == null ? Style.EMPTY : replacement.style())
            );

            if (end > start) {
                cursor = end;
            }
        }

        appendStyledRange(output, plain, styles, cursor, plain.length());

        return output;
    }

    private void appendStyledRange(MutableText output, String plain, Style[] styles, int start, int end) {
        int index = start;

        while (index < end) {
            Style style = styles[index] == null ? Style.EMPTY : styles[index];

            int next = index + 1;

            while (next < end) {
                Style nextStyle = styles[next] == null ? Style.EMPTY : styles[next];

                if (!style.equals(nextStyle)) {
                    break;
                }

                next++;
            }

            output.append(Text.literal(plain.substring(index, next))
                .setStyle(style)
            );

            index = next;
        }
    }

    public Text formatMessage(Text originalMessage) {
        if (originalMessage == null || !isEnabledInternal()) {
            return originalMessage;
        }

        MinecraftClient minecraft = mc();

        if (minecraft.player == null) {
            return originalMessage;
        }

        String realName = minecraft.player.getNameForScoreboard();

        if (realName == null || realName.isBlank()) {
            return originalMessage;
        }

        FlattenedText flattened = flattenText(originalMessage);

        String source = flattened.text();
        Style[] styles = flattened.styles();

        if (source.isEmpty()) {
            return originalMessage;
        }

        Matcher prefixMatcher = PREFIX_PATTERN.matcher(source);

        int prefixEnd = prefixMatcher.find() ? prefixMatcher.end() : 0;

        int nameStart = findPlayerName(source, realName, prefixEnd);

        if (nameStart < 0) {
            return originalMessage;
        }

        int nameEnd = nameStart + realName.length();

        int colon = source.indexOf(':', nameEnd);

        if (colon < 0 || colon - nameEnd > 100) {
            return originalMessage;
        }

        MutableText result = Text.empty();

        appendStyledRange(result, source, styles, 0, prefixEnd);

        if (starEnabled()) {
            appendStar(result);
        }

        if (lightningEnabled()) {
            appendLightning(result);
        }

        String donate = getDonateText().trim();

        if (isDonateEnabledInternal() && !donate.isBlank()) {
            result.append(makePart(donate, isDonateColorEnabledInternal(), config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold, Style.EMPTY));

            result.append(Text.literal(" "));
        }

        boolean nameProtect = isNameProtectEnabledInternal();
        boolean nameColor = isNameColorEnabledInternal();

        String displayedName = nameProtect ? getProtectedName(realName) : realName;

        result.append(makePart(displayedName, nameColor, config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold, getStyleAt(styles, nameStart)));

        String originalTitle = source.substring(nameEnd, colon).trim();

        boolean titleEnabled = isTitleEnabledInternal();

        if (titleEnabled && !getTitleText().isBlank()) {
            result.append(Text.literal(" "));
            result.append(makePart(getTitleText(), isTitleColorEnabledInternal(), config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold, getStyleAt(styles, nameEnd)));
        } else if (!originalTitle.isEmpty()) {
            result.append(Text.literal(" "));

            int titleStart = nameEnd;
            int titleEnd = colon;

            while (titleStart < titleEnd && Character.isWhitespace(source.charAt(titleStart))) {
                titleStart++;
            }

            while (titleEnd > titleStart && Character.isWhitespace(source.charAt(titleEnd - 1))) {
                titleEnd--;
            }

            appendStyledRange(result, source, styles, titleStart, titleEnd);
        }

        appendStyledRange(result, source, styles, colon, source.length());

        return result;
    }

    public Text formatScoreboardText(Text originalText) {
        if (originalText == null || !isEnabledInternal()) {
            return originalText;
        }

        String plain = getPlainText(originalText);

        if (plain.isEmpty()) {
            return originalText;
        }

        MinecraftClient minecraft = mc();

        String realName = minecraft.player != null ? minecraft.player.getNameForScoreboard() : "";

        List<Replacement> replacements = new ArrayList<>();

        boolean nameProtect = isNameProtectEnabledInternal();
        boolean nameColor = isNameColorEnabledInternal();

        boolean donateEnabled = isDonateEnabledInternal();
        boolean donateColor = isDonateColorEnabledInternal();

        boolean titleEnabled = isTitleEnabledInternal();
        boolean titleColor = isTitleColorEnabledInternal();

        if (nameProtect) {
            String customName = getProtectedName(realName);

            if (!customName.isBlank()) {
                Matcher matcher = SCOREBOARD_NAME_PATTERN.matcher(plain);

                while (matcher.find()) {
                    for (int group = 1; group <= 2; group++) {
                        int start = matcher.start(group);

                        if (start < 0) {
                            continue;
                        }

                        int end = matcher.end(group);

                        Style style = replacementStyle(originalText, start, nameColor, config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold);

                        replacements.add(new Replacement(start, end, customName, style));
                    }
                }
            }
        }

        if (donateEnabled) {
            String customDonate = getDonateText();

            if (!customDonate.isBlank()) {
                Matcher matcher = SCOREBOARD_DONATE_PATTERN.matcher(plain);

                while (matcher.find()) {
                    int start = matcher.start(1);
                    int end = matcher.end(1);

                    Style style = replacementStyle(originalText, start, donateColor, config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold);

                    replacements.add(new Replacement(start, end, customDonate, style));
                }
            }
        }

        if (titleEnabled) {
            String customTitle = getTitleText();

            if (!customTitle.isBlank()) {
                Matcher matcher = SCOREBOARD_TITLE_PATTERN.matcher(plain);

                if (matcher.find()) {
                    int start = matcher.start(1);
                    int end = matcher.end(1);

                    Style style = replacementStyle(originalText, start, titleColor, config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold);

                    replacements.add(new Replacement(start, end, customTitle, style));
                }
            }
        }

        if (replacements.isEmpty()) {
            return originalText;
        }

        return replaceRangesWithInsertions(originalText, replacements);
    }
}