package com.zavk1n.bqol.client.screen.featurescreen;

import com.zavk1n.bqol.client.screen.MainConfigScreen;
import com.zavk1n.bqol.features.BetterChat;
import com.zavk1n.bqol.utils.liteapi.LiteApiManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ChatConfigScreen extends MainConfigScreen{
    private ButtonWidget stackMessagesBtn, chatAnimationBtn, moreHistoryBtn, chatHeadsBtn, chatPingBtn, transliterationBtn, killMessageBtn;

    public TextFieldWidget killMessageField;

    private static final int BUTTON_WIDTH = 80, BUTTON_HEIGHT = 25, SPACING = 45;
    private boolean changed = false;

    public ChatConfigScreen(Screen parent) {
        super(Text.literal("Better Chat Settings"), parent);
    }

    @Override
    protected void init() {
        if (LiteApiManager.isFeatureBlocked("better_chat")) {
            close();
            return;
        }

        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        stackMessagesBtn = null;
        chatAnimationBtn = null;
        moreHistoryBtn = null;
        chatHeadsBtn = null;
        chatPingBtn = null;
        transliterationBtn = null;
        killMessageBtn = null;

        int rightX = width / 2 + 50;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_chat_stack_messages")) {
            stackMessagesBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatStackMessages() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatStackMessages();
                        config.setBetterChatStackMessages(state);
                        changed = true;
                        updateButton(stackMessagesBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(stackMessagesBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_animation")) {
            chatAnimationBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatAnimation() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatAnimation();
                        config.setBetterChatAnimation(state);
                        changed = true;
                        updateButton(chatAnimationBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(chatAnimationBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_more_history")) {
            moreHistoryBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatMoreHistory() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatMoreHistory();
                        config.setBetterChatMoreHistory(state);
                        changed = true;
                        updateButton(moreHistoryBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(moreHistoryBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_heads")) {
            chatHeadsBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatHeads() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatHeads();
                        config.setBetterChatHeads(state);
                        changed = true;
                        updateButton(chatHeadsBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(chatHeadsBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_ping")) {
            chatPingBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatPing() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatPing();
                        config.setBetterChatPing(state);
                        changed = true;
                        updateButton(chatPingBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(chatPingBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_transliteration")) {
            transliterationBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatTransliteration() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatTransliteration();
                        config.setBetterChatTransliteration(state);
                        changed = true;
                        updateButton(transliterationBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(transliterationBtn);
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_kill_message")) {
            killMessageBtn = ButtonWidget.builder(
                    Text.literal(config.isBetterChatKillMessage() ? "Enabled" : "Disabled"),
                    button -> {
                        boolean state = !config.isBetterChatKillMessage();
                        config.setBetterChatKillMessage(state);
                        changed = true;
                        updateButton(killMessageBtn, state);
                        save();
                    })
                .dimensions(rightX, y - 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

            addDrawableChild(killMessageBtn);

            killMessageField = new TextFieldWidget(textRenderer, rightX + BUTTON_WIDTH + 8, y - 3, 150, BUTTON_HEIGHT, Text.literal("Message text"));
            killMessageField.setMaxLength(200);
            killMessageField.setText(config.getBetterChatKillMessageText());

            killMessageField.setChangedListener(text -> {
                config.setBetterChatKillMessageText(text);
                changed = true;
            });

            addDrawableChild(killMessageField);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> close())
            .dimensions(width / 2 - 50, height - 40, 100, 25)
            .build()
        );

        updateAllButtons();
    }

    private void updateAllButtons() {
        if (stackMessagesBtn != null) {
            updateButton(stackMessagesBtn, config.isBetterChatStackMessages());
        }

        if (chatAnimationBtn != null) {
            updateButton(chatAnimationBtn, config.isBetterChatAnimation());
        }

        if (moreHistoryBtn != null) {
            updateButton(moreHistoryBtn, config.isBetterChatMoreHistory());
        }

        if (chatHeadsBtn != null) {
            updateButton(chatHeadsBtn, config.isBetterChatHeads());
        }

        if (chatPingBtn != null) {
            updateButton(chatPingBtn, config.isBetterChatPing());
        }

        if (transliterationBtn != null) {
            updateButton(transliterationBtn, config.isBetterChatTransliteration());
        }

        if (killMessageBtn != null) {
            updateButton(killMessageBtn, config.isBetterChatKillMessage());
        }
    }

    private void updateButton(ButtonWidget btn, boolean enabled) {
        btn.setMessage(Text.literal(enabled ? "Enabled" : "Disabled")
            .styled(s -> s.withColor(enabled ? ACCENT_COLOR : 0xFFFFFF)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftX = width / 4;
        int y = 60;

        if (!LiteApiManager.isFeatureBlocked("better_chat_stack_messages")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Stack Messages",
                "Identical messages don't clutter the chat."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_animation")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Animation",
                "Smooth chat animation."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_more_history")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "More History",
                "Extends the length of the chat."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_heads")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Chat Heads",
                "Players heads are displayed next to their nicknames."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_ping")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Chat Ping",
                "Marks your nickname when nickname is mentioned."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_transliteration")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Transliteration",
                "Now you write in an unusual style."
            );
            y += SPACING;
        }

        if (!LiteApiManager.isFeatureBlocked("better_chat_kill_message")) {
            renderLabel(context, leftX, y, mouseX, mouseY,
                "Kill Message",
                "The message that will be sent automatically after the kill."
            );
        }
    }

    private void renderLabel(DrawContext context, int x, int y, int mouseX, int mouseY, String title, String desc) {
        int titleWidth = textRenderer.getWidth(title);

        boolean hovered = mouseX >= x &&
            mouseX <= x + titleWidth &&
            mouseY >= y &&
            mouseY <= y + textRenderer.fontHeight;

        int color = hovered ? ACCENT_COLOR : 0xFFFFFFFF;

        context.drawText(textRenderer, Text.literal(title), x, y, color, false);
        context.drawText(textRenderer, Text.literal(desc), x, y + 12, 0xFF888888, false);
    }

    private void save() {
        config.save();
        BetterChat.refreshBlockedStatus();

        changed = false;
    }

    @Override
    public void close() {
        if (changed) {
            save();
        }

        if (client != null) {
            client.setScreen(parent);
        }
    }
}