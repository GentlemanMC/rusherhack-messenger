package me.gentleman.messenger.windows;

import me.gentleman.messenger.module.MessengerSettings;
import me.gentleman.messenger.util.RegexUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.rusherhack.client.api.Globals;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.ui.window.ResizeableWindow;
import org.rusherhack.client.api.ui.window.content.ComboContent;
import org.rusherhack.client.api.ui.window.content.component.ButtonComponent;
import org.rusherhack.client.api.ui.window.content.component.TextFieldComponent;
import org.rusherhack.client.api.ui.window.view.RichTextView;
import org.rusherhack.client.api.ui.window.view.TabbedView;
import org.rusherhack.client.api.ui.window.view.WindowView;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.notification.NotificationType;

import java.awt.*;
import java.util.List;

/**
 * @author gentleman 2024/01/22
 */
public class MessengerWindow extends ResizeableWindow {

    public static MessengerWindow INSTANCE;

    private final TabbedView rootView;

    private final RichTextView messageView;

    private final MessengerSettings messengerSettings = new MessengerSettings();
    public MessengerWindow() {
        super("Messenger", 150, 100, 300, 500);
        RusherHackAPI.getEventBus().subscribe(this);
        INSTANCE = this;

        this.setMinWidth(150);
        this.setMinHeight(150);

        this.messageView = new RichTextView("Messages", this);

        final ComboContent inputCombo = new ComboContent(this);

        final TextFieldComponent rawMessage = new TextFieldComponent(this, "enter message", 100);
        final ButtonComponent sendButton = new ButtonComponent(this, "send", () -> {
            final String input = rawMessage.getValue();

            if (input.isEmpty() || Globals.mc.player == null) {
                return;
            }

            Globals.mc.player.connection.sendChat(input);
            this.messageView.add(Component.literal("> " + input), Color.WHITE.getRGB());

            rawMessage.setValue("");
        });
        rawMessage.setReturnCallback((str) -> sendButton.onClick());
        inputCombo.addContent(rawMessage, ComboContent.AnchorSide.LEFT);
        inputCombo.addContent(sendButton, ComboContent.AnchorSide.RIGHT);

        this.rootView = new TabbedView(this, List.of(this.messageView, inputCombo));
    }

    @Subscribe
    public void onPacketReceive(EventPacket.Receive event) {

        if (Globals.mc.player == null || Globals.mc.level == null) {
            return;
        }

        if (event.getPacket() instanceof ClientboundPlayerChatPacket chatPacket) {

            RegexUtils.ChatMessageInfo chatInfo = RegexUtils.extractPlayerAndMessage(chatPacket.body().content());

            if (chatInfo != null && chatInfo.getPlayerName() != null && chatInfo.getPlayerName().equals(Globals.mc.name())) {
                return;
            }

            if (chatInfo != null) {
                String message = chatInfo.getMessage();

                if (message != null) {
                    ChatUtils.print(message);
                    this.messageView.add(Component.literal("> " + message), Color.lightGray.getRGB());

                    if(messengerSettings.Notifications.getValue()) {
                        RusherHackAPI.getNotificationManager().send(NotificationType.INFO, message);
                    }
                }
            }

        } else if (event.getPacket() instanceof ClientboundSystemChatPacket chatPacket) {

            RegexUtils.ChatMessageInfo chatInfo = RegexUtils.extractPlayerAndMessage(chatPacket.content().getString());

            if (chatInfo != null && chatInfo.getPlayerName() != null && chatInfo.getPlayerName().equals(Globals.mc.name())) {
                return;
            }

            if (chatInfo != null) {
                String message = chatInfo.getMessage();

                if (message != null) {
                    ChatUtils.print(message);
                    this.messageView.add(Component.literal("> " + message), Color.lightGray.getRGB());

                    if(messengerSettings.Notifications.getValue()) {
                        RusherHackAPI.getNotificationManager().send(NotificationType.INFO, message);
                    }
                }
            }
        }
    }


    @Override
    public WindowView getRootView() {
        return this.rootView;
    }

    public RichTextView getMessageView() {
        return this.messageView;
    }
}
