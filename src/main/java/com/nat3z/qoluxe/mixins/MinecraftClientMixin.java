package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.gui.GuiConfig;
import com.nat3z.qoluxe.gui.UpdatePrompt;
import com.nat3z.qoluxe.hooks.LockSlots;
import com.nat3z.qoluxe.hooks.MinecraftHook;
import com.nat3z.qoluxe.utils.CloudProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.resource.ResourceReload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    boolean alreadyPressed = false;
    boolean alreadyPressedLockSlot = false;
    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo info) {
        QOLuxe.Companion.getViciousExt().saveConfig();
        MinecraftHook.INSTANCE.startUpdate();
    }

    @Inject(at = @At("HEAD"), method = "onInitFinished")
    private void initHook(RealmsClient realms, ResourceReload reload, RunArgs.QuickPlay quickPlay, CallbackInfo ci) {
        CloudProvider.INSTANCE.updateCloudProviderJar();
        MinecraftHook.INSTANCE.checkUpdates(ci);
    }

    @Redirect(method = "onInitFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void redirectSetScreen(MinecraftClient instance, Screen screen) {
        if (MinecraftHook.INSTANCE.getPreparedUpdate()) {
            instance.setScreen(new UpdatePrompt());
        }
        else {
            instance.setScreen(new TitleScreen(true));
        }

    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void tick(CallbackInfo info) {
        if (QOLuxe.getDisableAnimalRendering().isPressed() && !alreadyPressed) {
            alreadyPressed = true;
            QOLuxeConfig.dontRenderAnimals = !QOLuxeConfig.dontRenderAnimals;
            MinecraftClient.getInstance().player.sendMessage(Text.of(Formatting.GOLD + "Animal Rendering is now " + (QOLuxeConfig.dontRenderAnimals ? Formatting.RED + "disabled" : Formatting.GREEN + "enabled") + Formatting.GOLD + "!"));
        }


        if (!QOLuxe.getDisableAnimalRendering().isPressed()) {
            alreadyPressed = false;
        }
        if (QOLuxe.getShowGui()) {
            QOLuxe.setShowGui(false);
            System.out.println("Showing GUI");
            MinecraftClient.getInstance().setScreen(new GuiConfig());
        }

        if (QOLuxe.Companion.getShownGui() != null) {
            MinecraftClient.getInstance().setScreen(QOLuxe.Companion.getShownGui());
            QOLuxe.Companion.setShownGui(null);
        }
    }
}
