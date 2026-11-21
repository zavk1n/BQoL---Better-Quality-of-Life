package com.zavk1n.bqol.features.mine.funtime;

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
import net.minecraft.entity.player.PlayerInventory;
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
public class FunTimeProcess {
    public enum Mode {
        DEFAULT("default");

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
                default -> null;
            };
        }
    }

    private enum State {
        IDLE,
        WAITING_ANALYSIS,
        SHOWING_RESULT
    }

    private record HologramLine(String text, double y) {}
    private record TimeInfo(int totalSeconds, String text) {}
    private record MineData(String mineName, TimeInfo time) {}

    private record AnalysisResult(
        int players,
        double filledPercent,
        int timeSeconds,
        double competitionScore,
        int filledScore,
        double timeScore,
        double score
    ) {}

    private static final int PLAYER_X1 = 50;
    private static final int PLAYER_Y1 = 91;
    private static final int PLAYER_Z1 = 37;

    private static final int PLAYER_X2 = 97;
    private static final int PLAYER_Y2 = 75;
    private static final int PLAYER_Z2 = -20;

    private static final int DEFAULT_FILL_X1 = 69;
    private static final int DEFAULT_FILL_Y1 = 75;
    private static final int DEFAULT_FILL_Z1 = 11;

    private static final int DEFAULT_FILL_X2 = 89;
    private static final int DEFAULT_FILL_Y2 = 85;
    private static final int DEFAULT_FILL_Z2 = -8;

    private static final double DEFAULT_HOLOGRAM_X = 71.0D;
    private static final double DEFAULT_HOLOGRAM_Y = 85.0D;
    private static final double DEFAULT_HOLOGRAM_Z = 1.0D;

    private static final double HOLOGRAM_RADIUS = 2.0D;

    private static final Box PLAYER_AREA = new Box(
        Math.min(PLAYER_X1, PLAYER_X2),
        Math.min(PLAYER_Y1, PLAYER_Y2),
        Math.min(PLAYER_Z1, PLAYER_Z2),
        Math.max(PLAYER_X1, PLAYER_X2) + 1.0D,
        Math.max(PLAYER_Y1, PLAYER_Y2) + 1.0D,
        Math.max(PLAYER_Z1, PLAYER_Z2) + 1.0D
    );

    private static final Box DEFAULT_HOLOGRAM_BOX = new Box(
        DEFAULT_HOLOGRAM_X - HOLOGRAM_RADIUS,
        DEFAULT_HOLOGRAM_Y - HOLOGRAM_RADIUS,
        DEFAULT_HOLOGRAM_Z - HOLOGRAM_RADIUS,
        DEFAULT_HOLOGRAM_X + HOLOGRAM_RADIUS,
        DEFAULT_HOLOGRAM_Y + HOLOGRAM_RADIUS,
        DEFAULT_HOLOGRAM_Z + HOLOGRAM_RADIUS
    );

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Comparator<HologramLine> HOLOGRAM_LINE_ORDER = Comparator.comparingDouble(HologramLine::y).reversed();

    private static final Pattern TIME_PATTERN = Pattern.compile("(?:(\\d+)\\s*мин\\.?\\s*(\\d+)\\s*сек\\.?|(\\d{1,2}):(\\d{2}))", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
            Chat.info(mc, message("Mine Helper: игрок не находится в нужном кубе."));
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

        if (mine.time() == null) {
            Chat.info(mc, message("Mine Helper: не удалось определить оставшееся время."));
            reset();
            return;
        }

        int timeSeconds = mine.time().totalSeconds();

        double competitionScore = getCompetitionScore(players);
        double timeScore = getTimeScore(timeSeconds);

        double timeMultiplier;

        if (timeSeconds < 15) {
            timeMultiplier = 1.0D / 3.0D;
        } else if (timeSeconds < 30) {
            timeMultiplier = 1.0D / 2.0D;
        } else {
            timeMultiplier = 1.0D;
        }

        double fillMultiplier;

        if (filledPercent < 20.0D) {
            fillMultiplier = 1.0D / 3.0D;
        } else if (filledPercent < 50.0D) {
            fillMultiplier = 1.0D / 2.0D;
        } else {
            fillMultiplier = 1.0D;
        }

        double finalMultiplier = timeMultiplier * fillMultiplier;

        competitionScore *= finalMultiplier;

        double score = competitionScore + filledScore + timeScore;

        score = Math.max(0.0D, Math.min(10.0D, score));
        score = Math.round(score * 10.0D) / 10.0D;

        AnalysisResult result = new AnalysisResult(players, filledPercent, timeSeconds, competitionScore, filledScore, timeScore, score);

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

        return PLAYER_AREA.contains(player.getX(), player.getY(), player.getZ());
    }

    private int countNetheritePickaxePlayers(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return 0;
        }

        int count = 0;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !PLAYER_AREA.contains(player.getX(), player.getY(), player.getZ())) {
                continue;
            }

            if (hasNetheritePickaxe(player)) {
                count++;
            }
        }

        return count;
    }

    private boolean hasNetheritePickaxe(PlayerEntity player) {
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isOf(Items.NETHERITE_PICKAXE)) {
                return true;
            }
        }

        ItemStack offHand = player.getInventory()
            .getStack(PlayerInventory.OFF_HAND_SLOT);

        return offHand.isOf(Items.NETHERITE_PICKAXE);
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

        long total = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        long filled = 0L;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);

                    BlockState blockState = mc.world.getBlockState(pos);

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

        lines.sort(HOLOGRAM_LINE_ORDER);

        String currentMine = null;

        TimeInfo time = null;

        for (HologramLine line : lines) {
            String normalized = normalize(line.text());

            if (currentMine == null && normalized.contains("обычн")) {
                currentMine = "Обычная";
            }

            if (time == null) {
                TimeInfo parsed = parseTime(normalized);

                if (parsed != null) {
                    time = parsed;
                }
            }

            if (currentMine != null && time != null) {
                break;
            }
        }

        if (currentMine == null || time == null) {
            return null;
        }

        return new MineData(currentMine, time);
    }

    private List<HologramLine> findHologramLines(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return List.of();
        }

        if (!isPlayerInsideBigCube(mc.player)) {
            return List.of();
        }

        Box box = DEFAULT_HOLOGRAM_BOX;

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

    private static TimeInfo parseTime(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = TIME_PATTERN.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        try {
            int minutes;
            int seconds;

            if (matcher.group(1) != null) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
            } else {
                minutes = Integer.parseInt(matcher.group(3));
                seconds = Integer.parseInt(matcher.group(4));
            }

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

        return WHITESPACE_PATTERN.matcher(
                COLOR_CODE_PATTERN.matcher(text).replaceAll("")
                    .replace('\u00A0', ' ')
            )
            .replaceAll(" ")
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