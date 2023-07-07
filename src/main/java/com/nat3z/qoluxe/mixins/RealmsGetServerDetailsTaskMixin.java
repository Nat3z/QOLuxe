package com.nat3z.qoluxe.mixins;

import com.google.gson.JsonObject;
import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.utils.ChatUtils;
import com.nat3z.qoluxe.utils.LithiumServerUtils;
import com.nat3z.qoluxe.utils.LithiumWebSocket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.net.URL;
import java.security.KeyPair;

@Mixin(RealmsGetServerDetailsTask.class)
public abstract class RealmsGetServerDetailsTaskMixin extends LongRunningTask {
    @Shadow @Final private RealmsServer server;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/realms/task/RealmsGetServerDetailsTask;join()Lnet/minecraft/client/realms/dto/RealmsServerAddress;"))
    private void run_joinAfterInvoke(CallbackInfo ci) {
        try {
            String lithiumRealmsResourcePack = LithiumServerUtils.INSTANCE.getLithiumRealmsLocation(this.server.id);
            if (lithiumRealmsResourcePack.equals("")) return;
            if (server.worldType != RealmsServer.WorldType.NORMAL) return;

            if (!LithiumServerUtils.INSTANCE.doesPackExist(this.server.id)) {
                QOLuxe.getLOGGER().info("Lithium Realms resource pack does not exist.");
                return;
            }
            String downloadURL = lithiumRealmsResourcePack + "/getpack";
            String hash = LithiumServerUtils.INSTANCE.getResourcePackHash(this.server.id);
            if (hash.equals("FAILED")) {
                QOLuxe.getLOGGER().info("Failed to get Lithium Realms resource pack hash");
                return;
            }
            MinecraftClient.getInstance().getServerResourcePackProvider().download(new URL(downloadURL), hash, false);
        } catch (Exception ex) {
            QOLuxe.getLOGGER().info("Failed to download Lithium Realms resource pack: " + ex.getMessage());
        }
    }

    @Inject(method = "join", at = @At(value = "HEAD"))
    private void join_Head(CallbackInfoReturnable<RealmsServerAddress> cir) {
        new Thread(() -> {
            try {
                QOLuxe.getLOGGER().info("Generating public and private keys...");
                String encryptionKey = ChatUtils.INSTANCE.createKeys();
                ChatUtils.INSTANCE.setLocalKeys(encryptionKey);
                String lithiumServerURL = LithiumServerUtils.INSTANCE.getLithiumRealmsLocation(this.server.id);
                if (lithiumServerURL.equals("")) return;
                // connect to server
                QOLuxe.getLOGGER().info("Connecting to Lithium Server...");
                URI url = new URI(lithiumServerURL.replace("https", "http").replace("http", "ws"));
                QOLuxe.getLOGGER().info("Connecting to " + url.toString() + "...");
                LithiumWebSocket.setNetworkConnection(new LithiumWebSocket(url, server.id));
                if (LithiumWebSocket.getNetworkConnection() == null) return;
                if (!QOLuxeConfig.encryptRealmsMessages) return;
                LithiumWebSocket.getNetworkConnection().connect();
                // wait for connection
                while (!LithiumWebSocket.getNetworkConnection().isOpen()) {
                    Thread.sleep(100);
                }
                QOLuxe.getLOGGER().info("Connected to Lithium Server!");
                // send public key
                QOLuxe.getLOGGER().info("Sending public key to Lithium Server...");
                JsonObject json = new JsonObject();
                json.addProperty("key", encryptionKey);
                json.addProperty("username", MinecraftClient.getInstance().getSession().getUsername());
                LithiumWebSocket.getNetworkConnection().sendMessage(LithiumWebSocket.SocketMessageTypes.KEY_EXCHANGE, json.toString());
                LithiumWebSocket.getNetworkConnection().getHeldKeys().add(new LithiumWebSocket.UsernameKeyOwnership(MinecraftClient.getInstance().getSession().getUsername(), encryptionKey));
                QOLuxe.getLOGGER().info("Sent public key to Lithium Server!");

            } catch (Exception ex) {
                QOLuxe.getLOGGER().info("Failed to connect to Lithium Server: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }


}
