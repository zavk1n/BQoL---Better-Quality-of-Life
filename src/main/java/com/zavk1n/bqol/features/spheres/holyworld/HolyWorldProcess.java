package com.zavk1n.bqol.features.spheres.holyworld;

import com.zavk1n.bqol.config.MainConfig;
import com.zavk1n.bqol.features.spheres.BetterSpheres;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HolyWorldProcess {
    public HolyWorldProcess(BetterSpheres betterSpheres, MainConfig config) {
        this.betterSpheres = betterSpheres;
        this.config = config;
        this.sphereMatches = createSphereMatchesHolyWorld();
        this.goldenSphereMatches = createGoldenSphereMatchesHolyWorld();
    }

    public final BetterSpheres betterSpheres;
    public final MainConfig config;

    public final List<DefaultSphereMatch> sphereMatches;
    public final List<GoldenSphereMatch> goldenSphereMatches;

    public static final String DAMAGE = "Урон";
    public static final String ARMOR = "Броня";
    public static final String SPEED = "Скорость";
    public static final String HEALTH = "Макс. здоровье";
    public static final String HASTE = "Спешка";
    public static final String ATTACK_SPEED = "Скорость атаки";
    public static final String VANISHING = "Проклятие утраты";

    public final List<String> PARAMETERS = List.of(
        DAMAGE,
        ARMOR,
        SPEED,
        HEALTH,
        HASTE,
        ATTACK_SPEED,
        VANISHING
    );

    public enum SphereType {
        DEFAULT,
        EPIC,
        LEGENDARY,
        MYTHIC,
        CUSTOM
    }

    public static final class SpherePalette {
        public SpherePalette() {}

        public static final int DEFAULT_NAME = 0xBABABA;
        public static final int EPIC_NAME = 0xEA00FF;
        public static final int LEGENDARY_NAME = 0x0080FF;
        public static final int MYTHIC_NAME = 0xFF0000;
        public static final int GOLDEN_NAME = 0xFFE500;

        public static final List<Integer> DEFAULT_GRADIENT = List.of(0xC2C2C2, 0xD6D6D6, 0xE3E3E3);
        public static final List<Integer> EPIC_GRADIENT = List.of(0xEC17FF, 0xEF52FF, 0xF173FF);
        public static final List<Integer> LEGENDARY_GRADIENT = List.of(0x0092FF, 0x47B0FF, 0x7EC6FC, 0xA3D8FF);
        public static final List<Integer> MYTHIC_GRADIENT = List.of(0xD10000, 0xFF0000, 0xFF5252, 0xFC6868);
    }

    public record SphereInfo(SphereType type, int nameColor, List<Integer> gradient) {}
    public record SphereResult(DefaultSphereMatch defaultSphere, GoldenSphereMatch goldenSphere) {}

    public static class DefaultSphereMatch {
        public final java.util.function.BooleanSupplier enabled;
        public final Map<String, Integer> requiredParameters;
        public final List<String> targetLines;
        public final List<Integer> colors;

        DefaultSphereMatch(java.util.function.BooleanSupplier enabled,
                             Map<String, Integer> requiredParameters,
                             List<String> targetLines,
                             List<Integer> colors) {
            this.enabled = enabled;
            this.requiredParameters = Map.copyOf(requiredParameters);
            this.targetLines = List.copyOf(targetLines);
            this.colors = List.copyOf(colors);
        }
    }

    public static class GoldenSphereMatch {
        public final java.util.function.BooleanSupplier enabled;
        public final Map<String, Integer> requiredParameters;
        public final String newName;
        public final List<String> targetLines;
        public final List<Integer> colors;

        GoldenSphereMatch(java.util.function.BooleanSupplier enabled,
                            Map<String, Integer> requiredParameters,
                            String newName,
                            List<String> targetLines,
                            List<Integer> colors) {
            this.enabled = enabled;
            this.requiredParameters = Map.copyOf(requiredParameters);
            this.newName = newName;
            this.targetLines = List.copyOf(targetLines);
            this.colors = List.copyOf(colors);
        }
    }

    public List<DefaultSphereMatch> createSphereMatchesHolyWorld() {
        return List.of(
            new DefaultSphereMatch(
                config::isSphereStingerHW,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 1),
                List.of("Урон 2", "Броня 2", "Скорость 1"),
                List.of(0xFF2600, 0xFF4E2E, 0xFF6E54)
            ),
            new DefaultSphereMatch(
                config::isSphereEternityHW,
                Map.of("Урон", 2, "Броня", 2, "Скорость", 2),
                List.of("Урон 2", "Броня 2", "Скорость 2"),
                List.of(0xFF008C, 0xFF47A9, 0xFF69B9)
            ),
            new DefaultSphereMatch(
                config::isSphereImmortalityHW,
                Map.of("Урон", 3, "Скорость", 2),
                List.of("Урон 3", "Скорость 2"),
                List.of(0x7600ED, 0x6100BA)
            ),
            new DefaultSphereMatch(
                config::isSphereArmortalityHW,
                Map.of("Урон", 2, "Броня", 2, "Макс. здоровье", 2),
                List.of("Урон 2", "Броня 2", "Макс. здоровье 2"),
                List.of(0x3A4A78, 0x4E6299, 0x687AB0)
            ),
            new DefaultSphereMatch(
                config::isSphereCerberusHW,
                Map.of("Проклятие утраты", 0, "Урон", 5, "Спешка", 1),
                List.of("Урон 5", "Спешка 1", "Проклятие утраты"),
                List.of(0xCF0000, 0xF70000, 0xFF3333)
            ),
            new DefaultSphereMatch(
                config::isSphereFlashHW,
                Map.of("Проклятие утраты", 0, "Скорость", 3, "Броня", 1),
                List.of("Скорость 3", "Броня 1", "Проклятие утраты"),
                List.of(0xCCF8FF, 0xB0F3FF, 0x8FEEFF)
            )
        );
    }

    public List<GoldenSphereMatch> createGoldenSphereMatchesHolyWorld() {
        return List.of(
            new GoldenSphereMatch(
                config::isSphereSpeedHW,
                Map.of("Скорость", 3),
                "Сфера Скорости",
                List.of("Скорость 3"),
                List.of(0xFFE500)
            ),
            new GoldenSphereMatch(
                config::isSphereMinerHW,
                Map.of("Спешка", 3),
                "Сфера Шахтера",
                List.of("Спешка 3"),
                List.of(0xFFE500)
            ),
            new GoldenSphereMatch(
                config::isSpherePvPHW,
                Map.of("Броня", 3, "Урон", 2),
                "Сфера ПвП",
                List.of("Урон 2", "Броня 3"),
                List.of(0xFFE600, 0xFFEC61)
            )
        );
    }

    public boolean isCustomSphere(Map<String, Integer> parameters) {
        for (DefaultSphereMatch match : sphereMatches) {
            if (parametersMatch(parameters, match.requiredParameters)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCustomSphereFromName(String name) {
        if (name == null) {
            return false;
        }

        return name.contains("Цербера")
            || name.contains("Флеша")
            || name.contains("ARMORTALITY")
            || name.contains("IMMORTALITY")
            || name.contains("Eternity")
            || name.contains("Stinger");
    }

    public SphereInfo getSphereInfo(String name) {
        if (name == null) {
            return null;
        }

        final List<String> CUSTOM_KEYWORDS = List.of(
            "Цербера", "Флеша", "ARMORTALITY", "IMMORTALITY", "Eternity", "Stinger"
        );

        for (String keyword : CUSTOM_KEYWORDS) {
            if (name.contains(keyword)) {
                return new SphereInfo(
                    SphereType.CUSTOM,
                    0xFFFFFF,
                    List.of(0xCCCCCC, 0xAAAAAA, 0x888888)
                );
            }
        }

        if (name.contains("Мифическая")) {
            return new SphereInfo(
                SphereType.MYTHIC,
                SpherePalette.MYTHIC_NAME,
                SpherePalette.MYTHIC_GRADIENT
            );
        }

        if (name.contains("Легендарная")) {
            return new SphereInfo(
                SphereType.LEGENDARY,
                SpherePalette.LEGENDARY_NAME,
                SpherePalette.LEGENDARY_GRADIENT
            );
        }

        if (name.contains("Эпическая")) {
            return new SphereInfo(
                SphereType.EPIC,
                SpherePalette.EPIC_NAME,
                SpherePalette.EPIC_GRADIENT
            );
        }

        if (name.contains("Обычная")) {
            return new SphereInfo(
                SphereType.DEFAULT,
                SpherePalette.DEFAULT_NAME,
                SpherePalette.DEFAULT_GRADIENT
            );
        }

        return null;
    }

    public SphereResult searchSpheres(Map<String, Integer> parameters) {
        return new SphereResult(
            findDefaultSphere(parameters),
            findGoldenSphere(parameters)
        );
    }

    public DefaultSphereMatch findDefaultSphere(Map<String, Integer> parameters) {
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

    public GoldenSphereMatch findGoldenSphere(Map<String, Integer> parameters) {
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

    public boolean parametersMatch(Map<String, Integer> actual, Map<String, Integer> required) {
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

    public void applySpheresParameters(List<Text> lines, Map<String, Integer> parameters, SphereInfo sphereInfo, SphereResult result) {
        if (result.goldenSphere() != null) {
            applyGoldenSphereParameters(lines, result.goldenSphere());
            return;
        }

        if (isCustomSphere(parameters)) {
            if (result.defaultSphere() != null) {
                applyDefaultSphereParameters(lines, parameters, sphereInfo, result.defaultSphere());
            }

            return;
        }

        applyDefaultSphereParameters(lines, parameters, sphereInfo, null);
    }

    public void applyDefaultSphereParameters(List<Text> lines, Map<String, Integer> parameters, SphereInfo sphereInfo, DefaultSphereMatch defaultMatch) {
        if (defaultMatch != null) {
            betterSpheres.replaceParameterLines(lines, defaultMatch.targetLines, defaultMatch.colors);
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

            orderedLines.add(parameter.equals(VANISHING)
                ? parameter
                : parameter + " " + level);

            orderedColors.add(sphereInfo.gradient().get(colorIndex % sphereInfo.gradient().size()));

            colorIndex++;
        }

        betterSpheres.replaceParameterLines(lines, orderedLines, orderedColors);
    }

    public void applyGoldenSphereParameters(List<Text> lines, GoldenSphereMatch match) {
        if (!config.isHWGoldenSpheres()
            || BetterSpheres.getInstance().isHolyWorldGoldenBlocked()
            || match == null) {
            return;
        }

        betterSpheres.replaceParameterLines(lines, match.targetLines, match.colors);
    }

    public void applySphereNames(ItemStack stack, String originalName, SphereInfo sphereInfo, SphereResult result) {
        if (result.goldenSphere() != null) {
            applyGoldenSphereName(stack, result.goldenSphere());
            return;
        }

        if (isCustomSphereFromName(originalName)) {
            if (result.defaultSphere() != null) {
                applyDefaultSphereName(stack, originalName, sphereInfo);
            }

            return;
        }

        applyDefaultSphereName(stack, originalName, sphereInfo);
    }

    public void applyDefaultSphereName(ItemStack stack, String originalName, SphereInfo sphereInfo) {
        if (sphereInfo == null
            || sphereInfo.type() == SphereType.LEGENDARY
            || sphereInfo.type() == SphereType.MYTHIC
            || sphereInfo.type() == SphereType.CUSTOM) {
            return;
        }

        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(originalName)
            .styled(style -> style
                .withColor(sphereInfo.nameColor())
                .withItalic(false))
        );
    }

    public void applyGoldenSphereName(ItemStack stack, GoldenSphereMatch match) {
        if (!config.isHWGoldenSpheres() || match == null) {
            return;
        }

        Text customName = Text.literal(match.newName)
            .styled(style -> style
                .withColor(SpherePalette.GOLDEN_NAME)
                .withItalic(false));

        if (!stack.getName().equals(customName)) {
            stack.set(DataComponentTypes.CUSTOM_NAME, customName);
        }
    }
}