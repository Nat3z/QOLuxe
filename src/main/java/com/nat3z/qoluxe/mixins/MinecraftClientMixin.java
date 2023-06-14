package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.gui.GuiConfig;
import com.nat3z.qoluxe.impls.MinecraftHook;
import com.nat3z.qoluxe.utils.ModAssistantHook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    boolean alreadyPressed = false;

    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo info) {
        QOLuxe.Companion.getViciousExt().saveConfig();
        MinecraftHook.INSTANCE.checkUpdates(info);
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void tick(CallbackInfo info) {
        if (QOLuxe.getDisableAnimalRendering().isPressed() && !alreadyPressed) {
            alreadyPressed = true;
            QOLuxe.setShouldDisableAnimalRendering(!QOLuxe.getShouldDisableAnimalRendering());
            MinecraftClient.getInstance().player.sendMessage(Text.of(Formatting.GOLD + "Animal Rendering is now " + (QOLuxe.getShouldDisableAnimalRendering() ? Formatting.RED + "disabled" : Formatting.GREEN + "enabled") + Formatting.GOLD + "!"));
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
