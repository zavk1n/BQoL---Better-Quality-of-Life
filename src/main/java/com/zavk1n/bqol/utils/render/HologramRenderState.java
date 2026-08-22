package com.zavk1n.bqol.utils.render;

import net.minecraft.entity.decoration.ArmorStandEntity;

public final class HologramRenderState {

    private static ArmorStandEntity currentHologram;

    private HologramRenderState() {}

    public static void begin(ArmorStandEntity armorStand) {
        currentHologram = armorStand;
    }

    public static void end() {
        currentHologram = null;
    }

    public static boolean isHologram() {
        return currentHologram != null;
    }

    public static ArmorStandEntity getCurrentHologram() {
        return currentHologram;
    }
}