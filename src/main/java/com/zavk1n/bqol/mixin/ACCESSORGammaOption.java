package com.zavk1n.bqol.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleOption.class)
public interface ACCESSORGammaOption {

    @Accessor("value")
    void bqol$setValue(Object value);
}
