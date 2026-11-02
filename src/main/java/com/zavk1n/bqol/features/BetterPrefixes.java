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
import java.util.regex.Pattern;

public final class BetterPrefixes {
    private MinecraftClient mc() {
        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        return client;
    }

    private BetterPrefixes() {
        client = MinecraftClient.getInstance();
    }

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

    private record StyledPart(String text, Style style) {}

    private record Replacement(
        int start,
        int end,
        String value,
        Style style
    ) {}

    public static void initialize() {
        if (instance == null) {
            instance = new BetterPrefixes();
            instance.refreshBlockedStatusInternal();
            BQoL.LOGGER.info("Better Prefixes initialized");
        }
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
        getInstance().setEnabledInternal(enabled);
    }

    public static boolean isNameProtectEnabled() {
        return instance != null && instance.isNameProtectEnabledInternal();
    }

    public static void setNameProtectEnabled(boolean enabled) {
        getInstance().setNameProtectEnabledInternal(enabled);
    }

    public static boolean isNameColorEnabled() {
        return instance != null && instance.isNameColorEnabledInternal();
    }

    public static void setNameColorEnabled(boolean enabled) {
        getInstance().setNameColorEnabledInternal(enabled);
    }

    public static boolean isDonateEnabled() {
        return instance != null && instance.isDonateEnabledInternal();
    }

    public static void setDonateEnabled(boolean enabled) {
        getInstance().setDonateEnabledInternal(enabled);
    }

    public static boolean isDonateColorEnabled() {
        return instance != null && instance.isDonateColorEnabledInternal();
    }

    public static void setDonateColorEnabled(boolean enabled) {
        getInstance().setDonateColorEnabledInternal(enabled);
    }

    public static boolean isTitleEnabled() {
        return instance != null && instance.isTitleEnabledInternal();
    }

    public static void setTitleEnabled(boolean enabled) {
        getInstance().setTitleEnabledInternal(enabled);
    }

    public static boolean isTitleColorEnabled() {
        return instance != null && instance.isTitleColorEnabledInternal();
    }

    public static void setTitleColorEnabled(boolean enabled) {
        getInstance().setTitleColorEnabledInternal(enabled);
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
        return isEnabledInternal()
            && config.betterPrefixesNameColor;
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
        return isEnabledInternal()
            && isDonateEnabledInternal()
            && config.betterPrefixesDonateColor;
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
        return isEnabledInternal()
            && isTitleEnabledInternal()
            && config.betterPrefixesTitleColor;
    }

    private void setTitleColorEnabledInternal(boolean enabled) {
        config.betterPrefixesTitleColor = enabled;
        config.save();
    }

    public boolean isLocalPlayerName(String name) {
        if (!isEnabledInternal() || name == null) {
            return false;
        }

        MinecraftClient minecraft = mc();

        return minecraft.player != null && minecraft.player.getGameProfile().getName().equalsIgnoreCase(name);
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

        if (isKnownDonateRank(trimmed)) {
            return toSmallCaps(trimmed);
        }

        return value;
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

    private boolean starEnabled() {
        return config.betterPrefixesStar;
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

    public Text formatName(String originalName) {
        if (!isEnabledInternal() || originalName == null) {
            return Text.literal(originalName == null ? "" : originalName);
        }

        MinecraftClient minecraft = mc();

        if (minecraft.player == null) {
            return Text.literal(originalName);
        }

        String realName = minecraft.player.getGameProfile().getName();

        if (!originalName.equalsIgnoreCase(realName)) {
            return Text.literal(originalName);
        }

        MutableText result = Text.empty();

        if (starEnabled()) {
            appendStar(result);
        }

        if (isDonateEnabled() && !getDonateText().isBlank()) {
            result.append(makePart(getDonateText() + " ", isDonateColorEnabled(), config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold, Style.EMPTY));
        }

        String displayedName = isNameProtectEnabled()
            ? getProtectedName(realName)
            : realName;

        result.append(makePart(displayedName, isNameColorEnabled(), config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold, Style.EMPTY));

        if (isTitleEnabled() && !getTitleText().isBlank()) {
            result.append(Text.literal(" "));
            result.append(makePart(getTitleText(), isTitleColorEnabled(), config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold, Style.EMPTY));
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

    private String getPlainText(Text original) {
        return original == null ? "" : original.getString();
    }

    private Style getStyleAt(Text original, int position) {
        List<StyledPart> parts = collectParts(original);
        int offset = 0;

        for (StyledPart part : parts) {
            int end = offset + part.text().length();

            if (position >= offset && position < end) {
                return part.style();
            }

            offset = end;
        }

        return Style.EMPTY;
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

    private Text replaceRangesWithInsertions(Text original, List<Replacement> replacements) {
        if (replacements.isEmpty()) {
            return original;
        }

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

            for (int i = offset; i < end; i++) {
                styles[i] = part.style();
            }

            offset = end;
        }

        replacements.sort(Comparator.comparingInt(Replacement::start)
            .thenComparingInt(r -> r.start() == r.end() ? 0 : 1)
        );

        MutableText output = Text.empty();
        int cursor = 0;

        for (Replacement replacement : replacements) {
            if (replacement.start() < cursor
                || replacement.start() < 0
                || replacement.end() > plain.length()
                || replacement.end() < replacement.start()) {
                continue;
            }

            appendStyledRange(output, plain, styles, cursor, replacement.start());

            if (replacement.end() > replacement.start()) {
                output.append(Text.literal(replacement.value())
                    .setStyle(replacement.style() == null ? Style.EMPTY : replacement.style())
                );

                cursor = replacement.end();
            } else {
                output.append(Text.literal(replacement.value())
                    .setStyle(replacement.style() == null ? Style.EMPTY : replacement.style())
                );
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

            output.append(Text.literal(plain.substring(index, next)).setStyle(style));

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

        String realName = minecraft.player.getGameProfile().getName();
        String plain = originalMessage.getString();

        if (realName == null || realName.isBlank() || plain.isEmpty()) {
            return originalMessage;
        }

        Pattern prefixPattern = Pattern.compile("^\\s*[GgɢＧLlʟＬ]\\s*\\|\\s*");

        java.util.regex.Matcher prefixMatcher = prefixPattern.matcher(plain);

        int prefixEnd = prefixMatcher.find() ? prefixMatcher.end() : 0;

        Pattern namePattern = Pattern.compile("(?i)(?<![a-zA-Z0-9_])" + Pattern.quote(realName) + "(?![a-zA-Z0-9_])");

        java.util.regex.Matcher nameMatcher =
            namePattern.matcher(plain);

        if (!nameMatcher.find(prefixEnd)) {
            return originalMessage;
        }

        int nameStart = nameMatcher.start();
        int nameEnd = nameMatcher.end();

        int colon = plain.indexOf(':', nameEnd);

        if (colon < 0 || colon - nameEnd > 100) {
            return originalMessage;
        }

        List<StyledPart> parts = collectParts(originalMessage);
        StringBuilder flattened = new StringBuilder();

        for (StyledPart part : parts) {
            flattened.append(part.text());
        }

        String source = flattened.toString();

        Style[] styles = new Style[source.length()];
        int offset = 0;

        for (StyledPart part : parts) {
            int end = Math.min(source.length(), offset + part.text().length());

            for (int i = offset; i < end; i++) {
                styles[i] = part.style();
            }

            offset = end;
        }

        MutableText result = Text.empty();

        appendStyledRange(result, source, styles, 0, prefixEnd);

        if (starEnabled()) {
            result.append(makeStar(Style.EMPTY));
            result.append(Text.literal(" "));
        }

        String donate = getDonateText().trim();

        if (isDonateEnabledInternal() && !donate.isBlank()) {
            result.append(makePart(donate, isDonateColorEnabledInternal(), config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold, Style.EMPTY));

            result.append(Text.literal(" "));
        }

        String displayedName = isNameProtectEnabledInternal() ? getProtectedName(realName) : realName;

        result.append(makePart(displayedName, isNameColorEnabledInternal(), config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold, getStyleAt(originalMessage, nameStart)));

        String originalTitle = source.substring(nameEnd, colon).trim();

        if (isTitleEnabledInternal() && !getTitleText().isBlank()) {
            result.append(Text.literal(" "));

            result.append(makePart(getTitleText(), isTitleColorEnabledInternal(), config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold, getStyleAt(originalMessage, nameEnd)));
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

        if (plain == null || plain.isEmpty()) {
            return originalText;
        }

        List<Replacement> replacements = new ArrayList<>();

        MinecraftClient minecraft = mc();

        String realName = minecraft.player != null ? minecraft.player.getGameProfile().getName() : "";

        if (isNameProtectEnabledInternal()) {
            String customName = getProtectedName(realName);

            if (customName != null && !customName.isBlank()) {
                Pattern[] namePatterns = {
                    Pattern.compile("(?i)Ник\\s*:\\s*(\\S+)"),
                    Pattern.compile("(?i)Вы\\s*:\\s*(\\S+)"),
                };

                for (Pattern pattern : namePatterns) {
                    java.util.regex.Matcher matcher = pattern.matcher(plain);

                    while (matcher.find()) {
                        int start = matcher.start(1);
                        int end = matcher.end(1);

                        Style style = replacementStyle(originalText, start, isNameColorEnabledInternal(), config.betterPrefixesNameColorRgb, config.betterPrefixesNameBold);

                        replacements.add(new Replacement(start, end, customName, style));
                    }
                }
            }
        }

        if (isDonateEnabledInternal()) {
            String customDonate = getDonateText();

            if (customDonate != null && !customDonate.isBlank()) {
                Pattern pattern = Pattern.compile("(?iu)(?:Группа|Ранг)\\s*:\\s*(\\S+)");
                java.util.regex.Matcher matcher = pattern.matcher(plain);

                while (matcher.find()) {

                    int start = matcher.start(1);
                    int end = matcher.end(1);

                    Style style = replacementStyle(originalText, start, isDonateColorEnabledInternal(), config.betterPrefixesDonateColorRgb, config.betterPrefixesDonateBold);

                    replacements.add(new Replacement(start, end, customDonate, style));
                }
            }
        }

        if (isTitleEnabledInternal()) {
            String customTitle = getTitleText();

            if (customTitle != null && !customTitle.isBlank()) {
                Pattern titlePattern = Pattern.compile("(?i)Титул\\s*:\\s*(.*\\S)(\\s*)$");

                java.util.regex.Matcher matcher = titlePattern.matcher(plain);

                if (matcher.find()) {
                    int start = matcher.start(1);
                    int end = matcher.end(1);

                    Style style = replacementStyle(originalText, start, isTitleColorEnabledInternal(), config.betterPrefixesTitleColorRgb, config.betterPrefixesTitleBold);

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
