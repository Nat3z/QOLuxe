package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.utils.ChatUtils;
import com.nat3z.qoluxe.utils.LithiumWebSocket;
import kotlin.text.Regex;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

// ChatHud.class (addMessage)
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow public abstract String normalize(String chatText);
    Regex whisperPattern = new Regex("\\/w (.*?) (.*)");
    String originalMessage = "";
    @Redirect(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;normalize(Ljava/lang/String;)Ljava/lang/String;"))
    private String sendMessage_normalize(ChatScreen instance, String chatText) {
        this.originalMessage = normalize(chatText);
        if (!MinecraftClient.getInstance().isConnectedToRealms()) return normalize(chatText);
        if (LithiumWebSocket.getNetworkConnection() == null || LithiumWebSocket.getNetworkConnection().isClosed() || !QOLuxeConfig.encryptRealmsMessages) return normalize(chatText);
        String originalText = this.originalMessage;
        String normalizedText = this.originalMessage;

        if (this.originalMessage.isEmpty()) return "";
        if (normalizedText.startsWith("/")) {
            if (whisperPattern.matches(normalizedText)) {
                String[] matches = Objects.requireNonNull(whisperPattern.find(normalizedText, 0)).getGroupValues().toArray(new String[0]);
                if (matches.length == 3) {
                    String recipient = matches[1];
                    String content = matches[2];
                    System.out.println("recipient: " + recipient);
                    System.out.println("content: " + content);

                    if (content.length() > 100) {
                        String[] splitText = normalizedText.split("(?<=\\G.{100})");
                        System.out.println("Splitting Chat Message into " + splitText.length + " messages");
                        for (String text : splitText) {
                            System.out.println("Sending message: " + text);
                            text = ChatUtils.INSTANCE.encryptMessage(text, Objects.requireNonNull(ChatUtils.INSTANCE.getLocalKeys()));
                            MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("w " + recipient + " ." + text);
                        }
                        return "";
                    }
                    if (!Objects.equals(ChatUtils.INSTANCE.getLocalKeys(), "")) {
                        normalizedText = "/w " + recipient + " ." + ChatUtils.INSTANCE.encryptMessage(content, Objects.requireNonNull(ChatUtils.INSTANCE.getLocalKeys()));
                    }
                }
            }
            return normalizedText;
        }
        if (ChatUtils.INSTANCE.getLocalKeys() == null) return normalizedText;
        // encrypt the message
        // check if the message is over ~200 characters, if so, then split it into multiple messages
        if (normalizedText.length() > 120) {
            String[] splitText = normalizedText.split("(?<=\\G.{120})");
            System.out.println("Splitting Chat Message into " + splitText.length + " messages");
            for (String text : splitText) {
                System.out.println("Sending message: " + text);
                ChatUtils.INSTANCE.sendEncryptedMessage(text, ChatUtils.INSTANCE.getLocalKeys());
            }
            return "";
        }
        normalizedText = "." + ChatUtils.INSTANCE.encryptMessage(normalizedText, ChatUtils.INSTANCE.getLocalKeys());
        if (normalizedText.length() > 256) {
            MinecraftClient.getInstance().player.sendMessage(Text.of(Formatting.RED + "The message you sent was too long to be encrypted and was sent as raw text."));
            return originalText;
        }
        return normalizedText;
    }

    @ModifyArg(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addToMessageHistory(Ljava/lang/String;)V"), index = 0)
    private String sendMessage_OverrideAddToMessageHistory(String message) {
        return originalMessage;
    }
}
