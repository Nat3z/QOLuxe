package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.utils.CloudProvider;
import com.nat3z.qoluxe.utils.LithiumWebSocket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Shadow @Final private WorldRenderer worldRenderer;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void disconnect(CallbackInfo ci) {
        if (LithiumWebSocket.getNetworkConnection() != null && LithiumWebSocket.getNetworkConnection().isOpen()) {
            System.out.println("Closing connection to Lithium Websocket...");
            LithiumWebSocket.getNetworkConnection().close();
        }

        if (QOLuxeConfig.cloudSaveLocation.isEmpty()) return;
        if (QOLuxe.getCurrentClientWorldName() == null || QOLuxe.getCurrentClientWorldName().isEmpty()) return;
        new Thread(() -> {
            System.out.println("Saving world changes...");
            File worldFolder = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/saves/" + QOLuxe.getCurrentClientWorldName());
            System.out.println("Saving world changes to " + worldFolder.getAbsolutePath());
            CloudProvider.INSTANCE.saveWorldChanges(worldFolder);
            System.out.println("Uploading world changes to cloud...");
            CloudProvider.INSTANCE.uploadSave(worldFolder);
        }).start();
    }
}
