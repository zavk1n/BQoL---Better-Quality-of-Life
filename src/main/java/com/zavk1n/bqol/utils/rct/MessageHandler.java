package com.zavk1n.bqol.utils.rct;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public final class MessageHandler {
    private MessageHandler() {}

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleMessage(message));
    }

    private static void handleMessage(Text message) {
        if (message == null) {
            return;
        }

        HolyWorldController.handleIncomingMessage(message.getString());
        ReallyWorldController.handleIncomingMessage(message.getString());
        FakeTimeController.handleIncomingMessage(message.getString());
    }
}