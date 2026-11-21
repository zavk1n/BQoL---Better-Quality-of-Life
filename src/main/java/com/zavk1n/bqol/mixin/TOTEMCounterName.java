package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.totems.BetterTotems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(EntityRenderer.class)
public abstract class TOTEMCounterName {

    @Unique
    private static World bqol$cachedWorld;

    @Unique
    private static long bqol$cachedWorldTime = Long.MIN_VALUE;

    @Unique
    private static final Map<UUID, Integer> BQOL_UUID_POPS = new HashMap<>();

    @Unique
    private static final Map<String, Integer> BQOL_NAME_POPS = new HashMap<>();

    @Unique
    private static final List<String> BQOL_PLAYER_NAMES = new ArrayList<>();

    @Unique
    private static void bqol$refreshPopCache(World world) {
        long worldTime = world.getTime();

        if (bqol$cachedWorld == world && bqol$cachedWorldTime == worldTime) {
            return;
        }

        bqol$cachedWorld = world;
        bqol$cachedWorldTime = worldTime;

        BQOL_UUID_POPS.clear();
        BQOL_NAME_POPS.clear();
        BQOL_PLAYER_NAMES.clear();

        BetterTotems totems = BetterTotems.getInstance();

        for (PlayerEntity player : world.getPlayers()) {
            int pops = totems.getPops(player.getUuid());

            if (pops <= 0) {
                continue;
            }

            String name = player.getName().getString();

            if (name.isBlank()) {
                continue;
            }

            BQOL_UUID_POPS.put(player.getUuid(), pops);
            BQOL_NAME_POPS.put(name, pops);
            BQOL_PLAYER_NAMES.add(name);
        }

        BQOL_PLAYER_NAMES.sort((first, second) -> Integer.compare(second.length(), first.length()));
    }

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void bqol$appendPopCounter(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        if (!BetterTotems.isTotemCounterEnabled()
            || entity == null
            || (!(entity instanceof PlayerEntity)
            && !(entity instanceof ArmorStandEntity))) {
            return;
        }

        if (state.displayName == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return;
        }

        Text original = state.displayName != null
            ? state.displayName
            : entity.getDisplayName();

        if (original == null) {
            return;
        }

        bqol$refreshPopCache(client.world);

        int pops = 0;

        if (entity instanceof PlayerEntity player) {
            pops = BQOL_UUID_POPS.getOrDefault(player.getUuid(), 0);

        } else if (entity instanceof ArmorStandEntity) {
            String label = state.displayName.getString();

            for (String playerName : BQOL_PLAYER_NAMES) {
                if (!label.contains(playerName)) {
                    continue;
                }

                pops = BQOL_NAME_POPS.getOrDefault(playerName, 0);
                break;
            }
        }

        if (pops > 0) {
            state.displayName = appendCounter(original, pops);
        }
    }

    private static MutableText appendCounter(Text original, int pops) {
        MutableText result = original.copy();

        result.append(Text.literal(" | ").formatted(Formatting.GRAY));
        result.append(Text.literal("-" + pops)
            .styled(style -> style.withColor(getPopColor(pops))));

        return result;
    }

    private static int getPopColor(int pops) {
        if (pops <= 2) return 0xFF55AA55;
        if (pops <= 4) return 0xFF00AA00;
        if (pops <= 6) return 0xFFFFFF55;
        if (pops <= 8) return 0xFFFF8800;
        return 0xFFFF5555;
    }
}