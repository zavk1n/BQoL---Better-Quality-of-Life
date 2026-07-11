package com.zavk1n.bqol.features;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.config.BQoLConfig;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        sphereMatches = createSphereMatches();
        goldenSphereMatches = createGoldenSphereMatches();
    }

    private static BetterSpheres instance;
    private MinecraftClient client;
    private final BQoLConfig config = BQoLConfig.getInstance();

    /// Типы сфер по редкости
    private enum SphereType {
        DEFAULT,
        EPIC,
        LEGENDARY,
        MYTHIC
    }

    /// Палитра цветов для отображения сфер
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

    /// Названия поддерживаемых характеристик
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

    /// Паттерн характеристик
    private static final Map<String, Pattern> PARAMETER_PATTERNS =
        PARAMETERS.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(p -> p, p -> Pattern.compile("^" + Pattern.quote(p) + "(\\s+(\\d+|[IVXLCDM]+))?$")));

    /// Распознование и оформление сфер
    private final List<DefaultSphereMatch> sphereMatches;

    private static class DefaultSphereMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final List<String> targetLines;
        final List<Integer> colors;

        DefaultSphereMatch(
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

    private final List<GoldenSphereMatch> goldenSphereMatches;

    private static class GoldenSphereMatch {
        final java.util.function.BooleanSupplier enabled;
        final Map<String, Integer> requiredParameters;
        final String newName;
        final List<String> targetLines;
        final List<Integer> colors;

        GoldenSphereMatch(
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

    /// Информация о сфере
    private record SphereInfo(
        SphereType type,
        int nameColor,
        List<Integer> gradient
    ) {}

    /// Итоговый результат
    private record SphereResult(
        DefaultSphereMatch defaultSphere,
        GoldenSphereMatch goldenSphere
    ) {}

    /// Таблица перевода чисел
    private static final Map<Character, Integer> CONVERTER =
        Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);

    /// Паттерны поиска кодов цветов и чисел
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORa-fk-or]");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\b(\\d+)\\b");


    /// Блокировки
    private final BlockedFeatures blocked = new BlockedFeatures();

    private static class BlockedFeatures {
        boolean main;
        boolean holyworld;
        boolean golden;
        boolean parameters;
        boolean names;
    }

    private boolean registered;

    /// Категории сфер
    private List<DefaultSphereMatch> createSphereMatches() {
        return List.of(
            new DefaultSphereMatch(
                config::isSphereStingerEnabled,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 1),
                List.of("Урон 2", "Броня 2", "Скорость 1"),
                List.of(0xFF2600, 0xFF4E2E, 0xFF6E54)
            ),
            new DefaultSphereMatch(
                config::isSphereEternityEnabled,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2"),
                List.of(0xFF008C, 0xFF47A9, 0xFF69B9)
            ),
            new DefaultSphereMatch(
                config::isSphereImmortalityEnabled,
                Map.of("Урон", 3, "Скорость", 2),
                List.of("Урон 3", "Скорость 2"),
                List.of(0x7600ED, 0x6100BA)
            ),
            new DefaultSphereMatch(
                config::isSphereArmortalityEnabled,
                Map.of("Урон", 2, "Броня", 2, "Макс. здоровье", 2),
                List.of("Урон 2", "Броня 2", "Макс. здоровье 2"),
                List.of(0x3A4A78, 0x4E6299, 0x687AB0)
            ),
            new DefaultSphereMatch(
                config::isSphereCerberusEnabled,
                Map.of("Проклятие утраты", 0, "Урон", 5, "Спешка", 1),
                List.of("Урон 5", "Спешка 1", "Проклятие утраты"),
                List.of(0xCF0000, 0xF70000, 0xFF3333)
            ),
            new DefaultSphereMatch(
                config::isSphereFlashEnabled,
                Map.of("Проклятие утраты", 0, "Скорость", 3, "Броня", 1),
                List.of("Скорость 3", "Броня 1", "Проклятие утраты"),
                List.of(0xCCF8FF, 0xB0F3FF, 0x8FEEFF)
            )
        );
    }

    private List<GoldenSphereMatch> createGoldenSphereMatches() {
        return List.of(
            new GoldenSphereMatch(
                config::isSphereSpeedEnabled,
                Map.of("Скорость", 3),
                "Сфера Лива",
                List.of("Скорость 3"),
                List.of(0xFFE500)
            ),
            new GoldenSphereMatch(
                config::isSphereMinerEnabled,
                Map.of("Спешка", 3),
                "Сфера Шахтера",
                List.of("Спешка 3"),
                List.of(0xFFE500)
            ),
            new GoldenSphereMatch(
                config::isSpherePvPEnabled,
                Map.of("Броня", 3, "Урон", 2),
                "Сфера ПвП",
                List.of("Урон 2", "Броня 3"),
                List.of(0xFFE600, 0xFFEC61)
            )
        );
    }

    /// Публичные статические методы
    public static void initialize() {
        if (instance == null) {
            instance = new BetterSpheres();
            instance.refreshBlockedStatusInternal();
            instance.registerCallbacks();
            BQoL.LOGGER.info("BetterSpheres initialized");
        }
    }

    public static BetterSpheres getInstance() {
        if (instance == null)
            initialize();
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

    /// Внутренние динамические методы
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

            processTooltip(stack, lines);}
        );
    }

    /// Центральная логика
    private void processTooltip(ItemStack stack, List<Text> lines) {
        if (!config.isHolyWorldSpheresEnabled() || blocked.holyworld || !isHolyWorld()) {
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

        SphereInfo sphereInfo = getSphereInfo(originalName);
        SphereResult sphereResult = searchSpheres(parameters);

        applySpheresParameters(lines, parameters, sphereInfo, sphereResult);
        applySphereNames(stack, originalName, sphereInfo, sphereResult);
    }

    /// Поиск сфер
    private SphereResult searchSpheres(Map<String, Integer> parameters) {
        return new SphereResult(
            findDefaultSphere(parameters),
            findGoldenSphere(parameters));
    }

    private DefaultSphereMatch findDefaultSphere(Map<String, Integer> parameters) {
        for (DefaultSphereMatch match : sphereMatches) {
            if (!match.enabled.getAsBoolean()) {
                continue;
            }

            if (parametersMatch(parameters, match.requiredParameters)) {
                return match;
            }
        }

        return null;
    }

    private GoldenSphereMatch findGoldenSphere(Map<String, Integer> parameters) {
        for (GoldenSphereMatch match : goldenSphereMatches) {
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
    private SphereInfo getSphereInfo(String name) {
        if (name == null) {
            return null;
        }

        if (name.contains("Мифическая")) {
            return new SphereInfo( SphereType.MYTHIC, SpherePalette.MYTHIC_NAME, SpherePalette.MYTHIC_GRADIENT );
        }

        if (name.contains("Легендарная")) {
            return new SphereInfo( SphereType.LEGENDARY, SpherePalette.LEGENDARY_NAME, SpherePalette.LEGENDARY_GRADIENT );
        }

        if (name.contains("Эпическая")) {
            return new SphereInfo( SphereType.EPIC, SpherePalette.EPIC_NAME, SpherePalette.EPIC_GRADIENT );
        }

        if (name.contains("Обычная")) {
            return new SphereInfo( SphereType.DEFAULT, SpherePalette.DEFAULT_NAME, SpherePalette.DEFAULT_GRADIENT );
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

    /// Извлечение уровня
    private int extractLevel(String line) {
        Matcher digitMatcher = LEVEL_PATTERN.matcher(line);

        if (digitMatcher.find()) {
            try {
                return Integer.parseInt(digitMatcher.group(1));
            }

            catch (NumberFormatException ignored) {
            }
        }

        String[] parts = line.split("\\s+");

        if (parts.length == 0) {
            return -1;
        }

        return romanToInt(parts[parts.length - 1].toUpperCase(Locale.ROOT));
    }

    /// Конвертация чисел
    private int romanToInt(String roman) {
        if (roman == null
                || roman.isBlank()
                || !roman.matches("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$")) {
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

    /// Проверка параметров
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

            for (Pattern pattern : PARAMETER_PATTERNS.values()) {
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
            String clean = lines.get(i).getString().trim();
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
                .withItalic(false)));
        }
    }


    private void applySpheresParameters(List<Text> lines, Map<String, Integer> parameters, SphereInfo sphereInfo, SphereResult result) {
        if (result.goldenSphere() != null) {
            applyGoldenSphereParameters(lines, result.goldenSphere());
        } else {
            applyDefaultSphereParameters(lines, parameters, sphereInfo, result.defaultSphere());
        }
    }

    private void applyDefaultSphereParameters(List<Text> lines, Map<String, Integer> parameters, SphereInfo sphereInfo, DefaultSphereMatch defaultMatch) {
        if (!config.isColoredParametersEnabled() || blocked.parameters) {
            return;
        }

        if (defaultMatch != null) {
            replaceParameterLines(lines, defaultMatch.targetLines, defaultMatch.colors);
            return;
        }

        if (sphereInfo == null) {
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
            orderedLines.add(parameter.equals(VANISHING) ? parameter : parameter + " " + level);
            orderedColors.add(sphereInfo.gradient().get(colorIndex % sphereInfo.gradient().size()));
            colorIndex++;
        }

        replaceParameterLines(lines, orderedLines, orderedColors);
    }

    private void applyGoldenSphereParameters(List<Text> lines, GoldenSphereMatch match) {
        if (!config.isGoldenSpheresEnabled() || blocked.golden || match == null) {
            return;
        }

        replaceParameterLines(lines, match.targetLines, match.colors);
    }


    private void applySphereNames(ItemStack stack, String originalName, SphereInfo sphereInfo, SphereResult result ) {
        if (result.goldenSphere() == null) {
            applyDefaultSphereName(stack, originalName, sphereInfo);
        } else {
            applyGoldenSphereName(stack, result.goldenSphere());
        }
    }

    private void applyDefaultSphereName(ItemStack stack, String originalName, SphereInfo sphereInfo) {
        if (!config.isColoredNamesEnabled()
            || blocked.names
            || sphereInfo == null
            || sphereInfo.type() == SphereType.LEGENDARY
            || sphereInfo.type() == SphereType.MYTHIC) {
            return;
        }

        stack.setCustomName(Text.literal(originalName).styled(style -> style
            .withColor(sphereInfo.nameColor())
            .withItalic(false)));
    }

    private void applyGoldenSphereName(ItemStack stack, GoldenSphereMatch match) {
        if (!config.isGoldenSpheresEnabled()
            || blocked.names
            || match == null) {
            return;
        }

        Text customName = Text.literal(match.newName).styled(style -> style
            .withColor(SpherePalette.GOLDEN_NAME)
            .withItalic(false));

        if (!stack.getName().equals(customName)) {
            stack.setCustomName(customName);
        }
    }

    /// Проверка сервера
    private boolean isHolyWorld() {
        MinecraftClient client = mc();

        if (client == null) {
            return false;
        }

        ServerInfo server = client.getCurrentServerEntry();

        if (server == null || server.address == null) {
            return false;
        }

        return server.address.toLowerCase(Locale.ROOT).contains("holyworld");
    }
}
// v1.0