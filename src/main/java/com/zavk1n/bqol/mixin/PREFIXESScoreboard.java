package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterPrefixes;

import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.client.gui.hud.InGameHud")
public abstract class PREFIXESScoreboard {

    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"
        ),
        index = 1,
        require = 0
    )
    private Text bqol$formatScoreboardText(Text original) {
        return formatText(original);
    }

    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I"
        ),
        index = 1,
        require = 0
    )
    private OrderedText bqol$formatScoreboardOrderedText(OrderedText original) {
        if (original == null) {
            return null;
        }

        return formatText(orderedTextToText(original)).asOrderedText();
    }

    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
        ),
        index = 1,
        require = 0
    )
    private Text bqol$formatShadowText(Text original) {
        return formatText(original);
    }

    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"
        ),
        index = 1,
        require = 0
    )
    private OrderedText bqol$formatShadowOrderedText(OrderedText original) {
        if (original == null) {
            return null;
        }

        return formatText(orderedTextToText(original)).asOrderedText();
    }

    private static Text formatText(Text original) {
        if (original == null) {
            return null;
        }

        return BetterPrefixes.getInstance()
            .formatScoreboardText(original);
    }

    private static Text orderedTextToText(OrderedText original) {
        MutableText result = Text.empty();
        StringBuilder buffer = new StringBuilder();
        Style[] currentStyle = {null};

        original.accept((index, style, codePoint) -> {
            Style safeStyle = style == null ? Style.EMPTY : style;

            if (currentStyle[0] == null || !currentStyle[0].equals(safeStyle)) {
                if (buffer.length() > 0) {
                    result.append(Text.literal(buffer.toString())
                        .setStyle(currentStyle[0])
                    );
                    buffer.setLength(0);
                }

                currentStyle[0] = safeStyle;
            }

            buffer.appendCodePoint(codePoint);
            return true;
        });

        if (buffer.length() > 0) {
            result.append(Text.literal(buffer.toString())
                .setStyle(currentStyle[0] == null ? Style.EMPTY : currentStyle[0])
            );
        }

        return result;
    }
}