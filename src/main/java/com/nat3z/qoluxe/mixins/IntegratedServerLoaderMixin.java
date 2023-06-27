package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.gui.CloudSaveManagement;
import com.nat3z.qoluxe.gui.ResolveSaveConflict;
import com.nat3z.qoluxe.gui.SelectCloudSaveFolder;
import com.nat3z.qoluxe.utils.CloudProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.Text;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {
    @Shadow @Final private LevelStorage storage;

    @Inject(method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V", at = @At("HEAD"), cancellable = true)
    private void start(Screen parent, String levelName, boolean safeMode, boolean canShowBackupPrompt, CallbackInfo ci) {
        if (CloudProvider.getPerformingCloudTask()) {
            MinecraftClient.getInstance().setScreenAndRender(new MessageScreen(Text.of("Waiting for previous cloud task to complete...")));
            while (CloudProvider.getPerformingCloudTask()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        QOLuxe.setCurrentClientWorldName(levelName);
        if (CloudProvider.INSTANCE.getOpenCloudSaveConfig() && QOLuxeConfig.cloudSaveLocation.isEmpty()) {
            CloudProvider.INSTANCE.setOpenCloudSaveConfig(false);
            MinecraftClient.getInstance().setScreenAndRender(new SelectCloudSaveFolder(new CloudSaveManagement(levelName, Objects.requireNonNull(CloudProvider.INSTANCE.getWorldEntryForSelected()))));
            ci.cancel();
            return;
        }
        if (QOLuxeConfig.cloudSaveLocation.isEmpty()) return;
        if (CloudProvider.INSTANCE.getScheduledResolveSaveConflict()) {
            CloudProvider.INSTANCE.setScheduledResolveSaveConflict(false);
            MinecraftClient.getInstance().setScreenAndRender(new ResolveSaveConflict(levelName));
            ci.cancel();
            return;
        }

        QOLuxe.getLOGGER().info("Launching Cloud Save...");
        MinecraftClient.getInstance().setScreenAndRender(new MessageScreen(Text.of("Synchronizing Cloud Save")));
        File worldDirectory = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/saves/" + levelName);
        boolean syncSuccess = CloudProvider.INSTANCE.syncSave(worldDirectory);
        if (!syncSuccess || CloudProvider.INSTANCE.getScheduledResolveSaveConflict()) {
            CloudProvider.INSTANCE.setScheduledResolveSaveConflict(false);
            MinecraftClient.getInstance().setScreenAndRender(new ResolveSaveConflict(levelName));
            ci.cancel();
            return;
        }

        if (CloudProvider.INSTANCE.getOpenCloudSaveConfig()) {
            CloudProvider.INSTANCE.setOpenCloudSaveConfig(false);
            MinecraftClient.getInstance().setScreenAndRender(new CloudSaveManagement(levelName, Objects.requireNonNull(CloudProvider.INSTANCE.getWorldEntryForSelected())));
            CloudProvider.INSTANCE.setWorldEntryForSelected(null);
            ci.cancel();
            return;
        }
        QOLuxe.getLOGGER().info("Save Synchronized.");

    }
}
