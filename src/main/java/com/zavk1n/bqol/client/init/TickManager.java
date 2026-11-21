package com.zavk1n.bqol.client.init;

import com.zavk1n.bqol.BQoL;
import com.zavk1n.bqol.features.*;

import com.zavk1n.bqol.features.mine.BetterMine;
import com.zavk1n.bqol.features.rct.BetterRCT;
import com.zavk1n.bqol.features.totems.BetterTotems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

public final class TickManager {
    private static final FeatureRegistry[] FEATURES = FeatureRegistry.values();

    private TickManager() { }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (FeatureRegistry feature : FEATURES) {
                try {
                    feature.tick();
                } catch (Exception e) {
                    BQoL.LOGGER.error("Error in feature tick handler: {}", feature.name(), e);
                }
            }
        });

        /// Better Gamma
        BetterGamma.initialize();
        BetterGamma gamma = BetterGamma.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register(gamma::tick);

        /// Better Tnt
        BetterTnt.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {BetterTnt.getInstance().tickTracking();});

        /// Better Mine
        BetterMine.initialize();
        BetterMine autoMine = BetterMine.getInstance();

        ClientSendMessageEvents.ALLOW_CHAT.register(autoMine::onChat);
        ClientSendMessageEvents.ALLOW_COMMAND.register(autoMine::onCommand);
        ClientTickEvents.END_CLIENT_TICK.register(autoMine::onTick);

        /// Better RCT
        BetterRCT.initialize();
        BetterRCT rct = BetterRCT.getInstance();

        ClientSendMessageEvents.ALLOW_CHAT.register(rct::onChat);
        ClientSendMessageEvents.ALLOW_COMMAND.register(rct::onCommand);
        ClientTickEvents.END_CLIENT_TICK.register(rct::onTick);

        /// Better Totem
        BetterTotems.initialize();
        BetterTotems totems = BetterTotems.getInstance();

        ClientSendMessageEvents.ALLOW_CHAT.register(totems::onChat);

        /// Better Chat
        BetterChat.initialize();
        BetterChat chat = BetterChat.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register(chat::tick);

        BQoL.LOGGER.info("Registered all feature tick handlers");
    }
}