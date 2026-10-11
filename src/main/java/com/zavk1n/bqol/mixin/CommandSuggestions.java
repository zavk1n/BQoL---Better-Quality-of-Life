package com.zavk1n.bqol.mixin;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.zavk1n.bqol.utils.chat.Chat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
@Mixin(ChatInputSuggestor.class)
public abstract class CommandSuggestions {

    @Shadow
    @Final
    private TextFieldWidget textField;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void show(boolean narrateFirstSuggestion);

    @Inject(method = "refresh", at = @At("TAIL"))
    private void bqol$suggestDotCommands(CallbackInfo ci) {
        String text = this.textField.getText();
        if (!text.startsWith(".")) {
            return;
        }

        List<String> suggestionTexts = Chat.matching(text);
        if (suggestionTexts.isEmpty()) {
            return;
        }

        StringRange range = Chat.suggestionRange(text);
        List<Suggestion> suggestions = suggestionTexts.stream()
            .map(suggestion -> new Suggestion(range, suggestion))
            .toList();

        this.pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        this.show(false);
    }
}