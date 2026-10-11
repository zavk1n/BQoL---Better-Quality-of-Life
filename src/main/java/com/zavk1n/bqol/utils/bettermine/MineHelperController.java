package com.zavk1n.bqol.utils.bettermine;

import com.zavk1n.bqol.utils.chat.Chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class MineHelperController {

    public enum Mode {
        DEFAULT("default"),
        NETHER("nether");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Mode from(String value) {
            if (value == null) {
                return null;
            }

            return switch (value.toLowerCase(Locale.ROOT)) {
                case "default" -> DEFAULT;
                case "nether" -> NETHER;
                default -> null;
            };
        }
    }

    private enum State {
        IDLE,
        WAITING_ANALYSIS,
        SHOWING_RESULT
    }

    private enum Rarity {
        DEFAULT("Обычная", 1),
        EPIC("Эпическая", 2),
        LEGENDARY("Легендарная", 3);

        private final String name;
        private final int score;

        Rarity(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }
    }

    private record HologramLine(String text, double y) {}
    private record TimeInfo(int totalSeconds, String text) {}
    private record MineData(String mineName, Rarity rarity, TimeInfo time) {}

    private record AnalysisResult(
        int players,
        double filledPercent,
        Rarity rarity,
        int timeSeconds,
        double competitionScore,
        int filledScore,
        int rarityScore,
        double timeScore,
        double score
    ) {}

    private static final int BIG_X1 = -184;
    private static final int BIG_Y1 = 50;
    private static final int BIG_Z1 = -195;

    private static final int BIG_X2 = -115;
    private static final int BIG_Y2 = 68;
    private static final int BIG_Z2 = -109;

    private static final int PLAYER_X1 = -179;
    private static final int PLAYER_Y1 = 62;
    private static final int PLAYER_Z1 = -163;

    private static final int PLAYER_X2 = -152;
    private static final int PLAYER_Y2 = 62;
    private static final int PLAYER_Z2 = -151;

    private static final int DEFAULT_FILL_X1 = -174;
    private static final int DEFAULT_FILL_Y1 = 52;
    private static final int DEFAULT_FILL_Z1 = -168;

    private static final int DEFAULT_FILL_X2 = -154;
    private static final int DEFAULT_FILL_Y2 = 61;
    private static final int DEFAULT_FILL_Z2 = -188;

    private static final int NETHER_FILL_X1 = -147;
    private static final int NETHER_FILL_Y1 = 51;
    private static final int NETHER_FILL_Z1 = -161;

    private static final int NETHER_FILL_X2 = -127;
    private static final int NETHER_FILL_Y2 = 62;
    private static final int NETHER_FILL_Z2 = -141;

    private static final double DEFAULT_HOLOGRAM_X = -172.0D;
    private static final double DEFAULT_HOLOGRAM_Y = 63.0D;
    private static final double DEFAULT_HOLOGRAM_Z = -178.0D;

    private static final double NETHER_HOLOGRAM_X = -143.0D;
    private static final double NETHER_HOLOGRAM_Y = 63.0D;
    private static final double NETHER_HOLOGRAM_Z = -151.0D;

    private static final double HOLOGRAM_RADIUS = 2.5D;

    private static final Pattern TIME_PATTERN = Pattern.compile(
        "(\\d+)\\s*мин\\.?\\s*(\\d+)\\s*сек\\.?",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern DEFAULT_MINE_PATTERN = Pattern.compile(
        "^(?:шахта\\s*)?(обычная|эпическая|легендарная)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern NETHER_MINE_PATTERN = Pattern.compile(
        "^(?:ад|энд)\\s*-\\s*(обычная|эпическая|легендарная)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private State state = State.IDLE;
    private Mode analyzeMode = Mode.DEFAULT;
    private int stateTicks;

    public boolean isBusy() {
        return state != State.IDLE;
    }

    public Mode getAnalyzeMode() {
        return analyzeMode;
    }

    public void setAnalyzeMode(Mode mode) {
        if (mode != null) {
            analyzeMode = mode;
        }
    }

    public void start(Mode mode) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mode == null
            || mc.player == null
            || mc.world == null
            || mc.getNetworkHandler() == null) {
            return;
        }

        if (isBusy()) {
            Chat.info(mc, message("Mine Helper уже выполняется."));
            return;
        }

        if (!isPlayerInsideBigCube(mc.player)) {
            Chat.info(mc, message("Mine Helper: игрок не находится в большом кубе."));
            return;
        }

        analyzeMode = mode;
        stateTicks = 0;
        state = State.WAITING_ANALYSIS;

        Chat.info(mc, message("Mine Helper: анализ шахты " + mode.getName() + "..."));
    }

    public void stopSilently() {
        reset();
    }

    public void tick(MinecraftClient mc) {
        if (!isBusy()) {
            return;
        }

        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) {
            reset();
            return;
        }

        if (!isPlayerInsideBigCube(mc.player)) {
            Chat.info(mc, message("Mine Helper: игрок вышел из большого куба."));
            reset();
            return;
        }

        stateTicks++;

        switch (state) {
            case WAITING_ANALYSIS -> {
                if (stateTicks < 5) {
                    return;
                }

                performAnalysis(mc);
            }

            case SHOWING_RESULT -> {
                if (stateTicks >= 40) {
                    reset();
                }
            }

            case IDLE -> {}
        }
    }

    private void performAnalysis(MinecraftClient mc) {
        if (!isPlayerInsideBigCube(mc.player)) {
            Chat.info(mc, message("Mine Helper: игрок не находится в большом кубе."));
            reset();
            return;
        }

        int players = countNetheritePickaxePlayers(mc);

        double filledPercent = getFilledPercent(mc);

        if (filledPercent <= 0.0D) {
            showZeroResult(mc, "Mine Helper: Заполненность 0% — 0/10.");
            return;
        }

        if (players >= 15) {
            showZeroResult(mc, "Mine Helper: Слишком много игроков (" + players + ") — 0/10.");
            return;
        }

        int filledScore = getFilledScore(filledPercent);

        MineData mine = readMineHologram(mc);

        if (mine == null) {
            Chat.info(mc, message("Mine Helper: не удалось найти нужную голограмму."));
            reset();
            return;
        }

        if (mine.rarity() == null) {
            Chat.info(mc, message("Mine Helper: не удалось определить редкость шахты."));
            reset();
            return;
        }

        if (mine.time() == null) {
            Chat.info(mc, message("Mine Helper: не удалось определить оставшееся время."));
            reset();
            return;
        }

        int timeSeconds = mine.time().totalSeconds();

        double competitionScore = getCompetitionScore(players);
        int rarityScore = mine.rarity().getScore();
        double timeScore = getTimeScore(timeSeconds);

        double scoreMultiplier;

        if (timeSeconds < 15) {
            scoreMultiplier = 1.0D / 3.0D;
        } else if (timeSeconds < 30) {
            scoreMultiplier = 1.0D / 2.0D;
        } else {
            scoreMultiplier = 1.0D;
        }

        competitionScore *= scoreMultiplier;
        rarityScore = (int) Math.round(rarityScore * scoreMultiplier);

        double score = competitionScore + filledScore + rarityScore + timeScore;

        score = Math.max(0.0D, Math.min(10.0D, score));
        score = Math.round(score * 10.0D) / 10.0D;

        AnalysisResult result = new AnalysisResult(players, filledPercent, mine.rarity(), timeSeconds, competitionScore, filledScore, rarityScore, timeScore, score);

        showResult(mc, result);

        state = State.SHOWING_RESULT;
        stateTicks = 0;
    }

    private void showZeroResult(MinecraftClient mc, String reason) {
        if (mc.player != null) {
            mc.inGameHud.setTitleTicks(10, 60, 15);
            mc.inGameHud.setTitle(Text.literal("0.0/10")
                    .styled(style -> style
                        .withColor(0xFF3333)
                        .withBold(true))
            );
        }

        Chat.info(mc, coloredMessage(reason, 0xFF3333));

        state = State.SHOWING_RESULT;
        stateTicks = 0;
    }

    private boolean isPlayerInsideBigCube(ClientPlayerEntity player) {
        if (player == null) {
            return false;
        }

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        return x >= BIG_X1
            && x <= BIG_X2
            && y >= BIG_Y1
            && y <= BIG_Y2
            && z >= BIG_Z1
            && z <= BIG_Z2;
    }

    private int countNetheritePickaxePlayers(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return 0;
        }

        Box area = new Box(PLAYER_X1, PLAYER_Y1, PLAYER_Z1,
            PLAYER_X2 + 1.0D,
            PLAYER_Y2 + 1.0D,
            PLAYER_Z2 + 1.0D
        );

        int count = 0;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !area.contains(player.getX(), player.getY(), player.getZ())) {
                continue;
            }

            if (hasNetheritePickaxe(player)) {
                count++;
            }
        }

        return count;
    }

    private boolean hasNetheritePickaxe(PlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && stack.getItem() == Items.NETHERITE_PICKAXE) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().offHand) {
            if (!stack.isEmpty() && stack.getItem() == Items.NETHERITE_PICKAXE) {
                return true;
            }
        }

        return false;
    }

    private double getCompetitionScore(int players) {
        if (players >= 8) {
            return 0.0D;
        }

        return switch (players) {
            case 0 -> 2.0D;
            case 1 -> 1.8D;
            case 2 -> 1.5D;
            case 3 -> 1.4D;
            case 4 -> 1.3D;
            case 5 -> 1.2D;
            case 6 -> 1.1D;
            case 7 -> 1.0D;
            default -> 0.0D;
        };
    }

    private double getFilledPercent(MinecraftClient mc) {
        if (analyzeMode == Mode.NETHER) {
            return filledRatio(mc, NETHER_FILL_X1, NETHER_FILL_Y1, NETHER_FILL_Z1, NETHER_FILL_X2, NETHER_FILL_Y2, NETHER_FILL_Z2) * 100.0D;
        }

        return filledRatio(mc, DEFAULT_FILL_X1, DEFAULT_FILL_Y1, DEFAULT_FILL_Z1, DEFAULT_FILL_X2, DEFAULT_FILL_Y2, DEFAULT_FILL_Z2) * 100.0D;
    }

    private double filledRatio(MinecraftClient mc, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (mc.world == null) {
            return 0.0D;
        }

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);

        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        long total = 0L;
        long filled = 0L;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);

                    BlockState blockState = mc.world.getBlockState(pos);

                    total++;

                    if (!blockState.isAir()) {
                        filled++;
                    }
                }
            }
        }

        return total == 0L ? 0.0D : (double) filled / total;
    }

    private int getFilledScore(double percent) {
        if (percent < 33.0D) {
            return 1;
        }

        if (percent < 66.0D) {
            return 2;
        }

        return 3;
    }

    private MineData readMineHologram(MinecraftClient mc) {
        List<HologramLine> lines = findHologramLines(mc);

        if (lines.isEmpty()) {
            return null;
        }

        lines.sort(Comparator.comparingDouble(HologramLine::y).reversed());

        String currentMine = null;
        TimeInfo time = null;

        for (int i = 0; i < lines.size(); i++) {
            String normalized = normalize(lines.get(i).text());

            if (normalized.contains(normalize("Текущая шахта:"))) {
                if (i + 1 < lines.size()) {
                    currentMine = lines.get(i + 1).text();
                }
            }

            if (time == null) {
                TimeInfo parsed = parseTime(normalized);

                if (parsed != null) {
                    time = parsed;
                }
            }
        }

        if (currentMine == null) {
            return null;
        }

        Rarity rarity = parseRarity(currentMine);

        return new MineData(currentMine, rarity, time);
    }

    private List<HologramLine> findHologramLines(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return List.of();
        }

        if (!isPlayerInsideBigCube(mc.player)) {
            return List.of();
        }

        double centerX;
        double centerY;
        double centerZ;

        if (analyzeMode == Mode.DEFAULT) {
            centerX = DEFAULT_HOLOGRAM_X;
            centerY = DEFAULT_HOLOGRAM_Y;
            centerZ = DEFAULT_HOLOGRAM_Z;
        } else {
            centerX = NETHER_HOLOGRAM_X;
            centerY = NETHER_HOLOGRAM_Y;
            centerZ = NETHER_HOLOGRAM_Z;
        }

        Box box = new Box(
            centerX - HOLOGRAM_RADIUS,
            centerY - HOLOGRAM_RADIUS,
            centerZ - HOLOGRAM_RADIUS,
            centerX + HOLOGRAM_RADIUS,
            centerY + HOLOGRAM_RADIUS,
            centerZ + HOLOGRAM_RADIUS
        );

        List<HologramLine> result = new ArrayList<>();

        for (Entity entity : mc.world.getOtherEntities(null, box)) {
            String text = getEntityText(entity);

            if (text == null || text.isBlank()) {
                continue;
            }

            result.add(new HologramLine(text, entity.getY()));
        }

        return result;
    }

    private static String getEntityText(Entity entity) {
        if (entity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
            DisplayEntity.TextDisplayEntity.Data data = textDisplay.getData();

            if (data != null && data.text() != null) {
                return data.text().getString();
            }
        }

        if (entity instanceof ArmorStandEntity armorStand) {
            Text customName = armorStand.getCustomName();

            if (customName != null) {
                return customName.getString();
            }
        }

        return null;
    }

    private Rarity parseRarity(String mineName) {
        if (mineName == null) {
            return null;
        }

        String normalized = normalize(mineName);

        Pattern pattern = analyzeMode == Mode.DEFAULT
            ? DEFAULT_MINE_PATTERN
            : NETHER_MINE_PATTERN;

        Matcher matcher = pattern.matcher(normalized);

        if (!matcher.matches()) {
            return null;
        }

        return switch (matcher.group(1)) {
            case "обычная" -> Rarity.DEFAULT;
            case "эпическая" -> Rarity.EPIC;
            case "легендарная" -> Rarity.LEGENDARY;
            default -> null;
        };
    }

    private static TimeInfo parseTime(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = TIME_PATTERN.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        try {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));

            if (seconds >= 60) {
                return null;
            }

            return new TimeInfo(minutes * 60 + seconds, matcher.group());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private double getTimeScore(int seconds) {
        if (seconds < 30) {
            return 0.0D;
        }

        if (seconds >= 120) {
            return 2.0D;
        }

        double progress = (seconds - 30) / 90.0D;
        double score = 1.0D + progress;

        return Math.round(score * 10.0D) / 10.0D;
    }

    private void showResult(MinecraftClient mc, AnalysisResult result) {
        String scoreText = String.format(Locale.US, "%.1f/10", result.score());
        int color = getScoreColor(result.score());

        if (mc.player != null) {
            mc.inGameHud.setTitleTicks(10, 60, 15);

            Text title = Text.literal(scoreText)
                .styled(style -> style
                    .withColor(color)
                    .withBold(true));

            if (result.score() >= 10.0D) {
                title = Text.literal("10.0/10 BEST!")
                    .styled(style -> style
                        .withColor(0x7CFC00)
                        .withBold(true));
            }

            mc.inGameHud.setTitle(title);
        }

        if (result.score() >= 10.0D) {
            Chat.info(mc, coloredMessage("Mine Helper: BEST! 10/10!", 0x7CFC00));
        } else {
            Chat.info(mc, coloredMessage("Mine Helper: " + scoreText, color));
        }

        Chat.info(mc, coloredMessage(String.format(Locale.US, "Игроки с незеритовой киркой: %d | %.1f балл.", result.players(), result.competitionScore()), color));
        Chat.info(mc, coloredMessage(String.format(Locale.US, "Заполненность: %.1f%% | %d балл.", result.filledPercent(), result.filledScore()), color));
        Chat.info(mc, coloredMessage("Редкость: " + result.rarity().getName() + " | " + result.rarityScore() + " балл.", color));
        Chat.info(mc, coloredMessage(String.format(Locale.US, "Время: %s | %.1f балл.", formatTime(result.timeSeconds()), result.timeScore()), color));
    }

    private int getScoreColor(double score) {
        if (score <= 1.5D) {
            return 0xFF3333;
        }

        if (score <= 3.0D) {
            return 0xFF8C00;
        }

        if (score <= 5.0D) {
            return 0xFFFF00;
        }

        if (score < 9.0D) {
            return 0x32CD32;
        }

        return 0x7CFC00;
    }

    private static Text coloredMessage(String text, int color) {
        return Text.literal(text)
            .styled(style -> style
                .withColor(color)
                .withBold(true));
    }

    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remaining = seconds % 60;

        return minutes + " мин. " + remaining + " сек.";
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
            .replaceAll("§[0-9a-fk-or]", "")
            .replace('\u00A0', ' ')
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private void reset() {
        state = State.IDLE;
        stateTicks = 0;
    }

    private static Text message(String text) {
        return Text.literal(text);
    }
}