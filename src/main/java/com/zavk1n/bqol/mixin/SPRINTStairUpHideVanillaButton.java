package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.features.BetterSprint;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsOptionsScreen.class)
public class SPRINTStairUpHideVanillaButton {

    @Inject(method = "init", at = @At("TAIL"))
    private void bqol$hideAutoJumpButton(CallbackInfo ci) {
        if (!BetterSprint.isStairUpActive()) {
            return;
        }

        ControlsOptionsScreen screen = (ControlsOptionsScreen) (Object) this;

        for (Element element : screen.children()) {

            /// Проверка типа элемента
            if (!(element instanceof ClickableWidget widget)) {
                continue;
            }

            Text message = widget.getMessage();

            if (message == null) {
                continue;
            }

            /// Проверка кнопки
            if (message.getContent() instanceof TranslatableTextContent content && "options.autoJump".equals(content.getKey())) {
                widget.visible = false;
                widget.active = false;
                break;
            }
        }
    }
}
// v1.0