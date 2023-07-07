package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.utils.ChatUtils;
import com.nat3z.qoluxe.utils.LithiumWebSocket;
import kotlin.text.Regex;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    Regex regex = new Regex("^<(.*?)> (.*)");
    Regex whispersRegex = new Regex("^(.*?) whispers to you: (.*)");
    Regex youWhisper = new Regex("^You whisper to (.*?): (.*)");

    @ModifyArg(method = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"), index = 0)
    private StringVisitable addMessage_OverrideMessage(StringVisitable message) {
        if (LithiumWebSocket.getNetworkConnection() == null || LithiumWebSocket.getNetworkConnection().isClosed() || !QOLuxeConfig.encryptRealmsMessages) return message;
        // decrypt the message
        // check if the message was sent by a player
        if (regex.matches(message.getString())) {
            String[] matches = regex.find(message.getString(), 0).getGroupValues().toArray(new String[0]);
            if (matches.length == 3) {
                String sender = matches[1];
                String content = matches[2];
                if (content.startsWith(".")) {
                    // remove the dot
                    content = content.substring(1);
                } else return message;

                if (!LithiumWebSocket.getNetworkConnection().getEncryptionKey(sender).equals("")) {
                    message = Text.of("<" + sender + "> " + ChatUtils.INSTANCE.decryptMessage(content, LithiumWebSocket.getNetworkConnection().getEncryptionKey(sender)));
                }
            }
        }

        if (whispersRegex.matches(message.getString()) || youWhisper.matches(message.getString())) {
            Regex regexSelected = whispersRegex;
            if (youWhisper.matches(message.getString())) regexSelected = youWhisper;

            String[] matches = regexSelected.find(message.getString(), 0).getGroupValues().toArray(new String[0]);
            if (matches.length == 3) {
                String sender = matches[1];
                String content = matches[2];
                if (content.startsWith(".")) {
                    // remove the dot
                    content = content.substring(1);
                } else return message;

                if (regexSelected == youWhisper) sender = MinecraftClient.getInstance().getSession().getUsername();

                if (!LithiumWebSocket.getNetworkConnection().getEncryptionKey(sender).equals("")) {
                    if (regexSelected == youWhisper) {
                        message = Text.of(Formatting.GRAY + "" + Formatting.ITALIC + "You whisper to " + sender + ": " + ChatUtils.INSTANCE.decryptMessage(content, LithiumWebSocket.getNetworkConnection().getEncryptionKey(sender)));
                    }
                    else
                        message = Text.of(Formatting.GRAY + "" + Formatting.ITALIC + sender + " whispers to you: " + ChatUtils.INSTANCE.decryptMessage(content, LithiumWebSocket.getNetworkConnection().getEncryptionKey(sender)));
                }
            }

        }

        return message;
    }

}
