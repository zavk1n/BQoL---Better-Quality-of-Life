package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.accessors.ACCESSORTNTRenderState;

import net.minecraft.client.render.entity.state.TntEntityRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TntEntityRenderState.class)
public abstract class TNTEntityRenderState implements ACCESSORTNTRenderState {

    @Unique
    private int bqol$tntTypeOrdinal;

    @Override
    public int bqol$getTntTypeOrdinal() {
        return bqol$tntTypeOrdinal;
    }

    @Override
    public void bqol$setTntTypeOrdinal(int ordinal) {
        bqol$tntTypeOrdinal = ordinal;
    }
}