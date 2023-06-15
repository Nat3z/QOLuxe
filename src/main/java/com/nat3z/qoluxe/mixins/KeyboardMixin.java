package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.hooks.LockSlots;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // check if it's a keypress and if the lock slot key is pressed
        if (!LockSlots.INSTANCE.getAlreadyClicked() && QOLuxe.getLockSlot().matchesKey(key, scancode) && MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) {
            LockSlots.INSTANCE.setLockHoveredSlot(true);
            LockSlots.INSTANCE.setAlreadyClicked(true);
        }

        // check if the lock slot key is released
        if (action == 0 && LockSlots.INSTANCE.getAlreadyClicked()) {
            LockSlots.INSTANCE.setAlreadyClicked(false);
        }
    }
}
