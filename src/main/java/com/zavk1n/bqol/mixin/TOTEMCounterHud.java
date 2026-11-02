package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterTotems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.InGameHud.class)
public abstract class TOTEMCounterHud {

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void bqol$renderTotemCount(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
            || client.world == null
            || !BetterTotems.isTotemCounterEnabled()) {
            return;
        }

        PlayerInventory inventory = client.player.getInventory();

        int count = 0;

        for (ItemStack stack : inventory.main) {
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        for (ItemStack stack : inventory.offHand) {
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        if (count <= 0) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = screenWidth / 2 - 8;
        int y = screenHeight - 48;

        context.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), x, y);

        String text = String.valueOf(count);

        int textWidth = client.textRenderer.getWidth(text);

        context.drawTextWithShadow(client.textRenderer, text, x + 8 - textWidth / 2, y - 10, getTotemColor(count));
    }

    private static int getTotemColor(int count) {
        if (count <= 2) return 0xFF5555;
        if (count <= 4) return 0xFF8B00;
        if (count <= 6) return 0xFFFF55;
        if (count <= 8) return 0x00FF00;
        return 0x00CC00;
    }
}
