package com.zavk1n.bqol.mixin;

import com.zavk1n.bqol.accessors.ACCESSORHologramRenderState;

import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ArmorStandEntityRenderState.class)
public abstract class HOLOGRAMSArmorStandRenderState
    implements ACCESSORHologramRenderState {

    @Unique
    private boolean bqol$hologram;

    @Override
    public boolean bqol$isHologram() {
        return bqol$hologram;
    }

    @Override
    public void bqol$setHologram(boolean hologram) {
        this.bqol$hologram = hologram;
    }
}