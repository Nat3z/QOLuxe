package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.gui.GuiConfig;
import com.nat3z.qoluxe.hooks.LockSlots;
import com.nat3z.qoluxe.hooks.MinecraftHook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.resource.ResourceReload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        MinecraftHook.INSTANCE.checkUpdates(ci);
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
    }
}
