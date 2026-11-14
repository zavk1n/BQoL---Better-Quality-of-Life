package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTotems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.InGameHud.class)
public abstract class TOTEMCounterHud {

    @Unique
    private static final ItemStack BQOL_TOTEM_ICON = Items.TOTEM_OF_UNDYING.getDefaultStack();

    @Unique
    private static PlayerEntity bqol$cachedPlayer;

    @Unique
    private static int bqol$cachedAge = Integer.MIN_VALUE;

    @Unique
    private static int bqol$cachedTotemCount;

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void bqol$renderTotemCount(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null
            || client.world == null
            || !BetterTotems.isTotemCounterEnabled()) {
            return;
        }

        if (bqol$cachedPlayer != player || bqol$cachedAge != player.age) {
            bqol$cachedPlayer = player;
            bqol$cachedAge = player.age;

            PlayerInventory inventory = player.getInventory();

            int count = 0;

            for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
                ItemStack stack = inventory.getStack(i);

                if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                    count += stack.getCount();
                }
            }

            ItemStack offHand = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);

            if (offHand.isOf(Items.TOTEM_OF_UNDYING)) {
                count += offHand.getCount();
            }

            bqol$cachedTotemCount = count;
        }

        int count = bqol$cachedTotemCount;

        if (count <= 0) {
            return;
        }

        int x = client.getWindow().getScaledWidth() / 2 - 8;
        int y = client.getWindow().getScaledHeight() - 48;

        context.drawItem(BQOL_TOTEM_ICON, x, y);

        String text = String.valueOf(count);

        int textX = x + (16 - client.textRenderer.getWidth(text)) / 2;
        int textY = y - client.textRenderer.fontHeight - 2;

        context.drawTextWithShadow(client.textRenderer, text, textX, textY, getTotemColor(count));
    }

    private static int getTotemColor(int count) {
        if (count <= 2) return 0xFFFF5555;
        if (count <= 4) return 0xFFFF8B00;
        if (count <= 6) return 0xFFFFFF55;
        if (count <= 8) return 0xFF00FF00;
        return 0xFF00CC00;
    }
}
