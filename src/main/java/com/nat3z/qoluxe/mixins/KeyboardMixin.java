package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.hooks.BindSlots;
import com.nat3z.qoluxe.hooks.LockSlots;
import com.nat3z.qoluxe.utils.SlotUtils;
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
    private boolean isUserDevotedToLockSlots = false;
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // check if it's a keypress and if the lock slot key is pressed
        if (action == 1 && QOLuxe.getLockSlot().matchesKey(key, scancode) && MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) {
            if (SlotUtils.INSTANCE.getHoveredSlotId() == -1) return;
            LockSlots.INSTANCE.setLockHoveredSlot(true);
            LockSlots.INSTANCE.setAlreadyClicked(true);
        }

        // check if it's a key release and if the lock slot key is pressed
        if (action == 0 && BindSlots.INSTANCE.getStartSlotLockingProcess()) {
            if (!isUserDevotedToLockSlots) {
                return;
            }
            BindSlots.INSTANCE.setStartSlotLockingProcess(false);
            assert MinecraftClient.getInstance().player != null;
            assert MinecraftClient.getInstance().currentScreen != null;
            if (SlotUtils.INSTANCE.getHoveredSlotId() == -1) return;
            if (SlotUtils.INSTANCE.getHoveredSlotId() == BindSlots.INSTANCE.getInitialSlotToBind()) return;
            int slotId = SlotUtils.INSTANCE.getHoveredSlotId();
            if (MinecraftClient.getInstance().currentScreen instanceof InventoryScreen && slotId >= 5 && slotId <= 8) {
                // make the armor slots above 45 to make them not appear in chests and stuff
                slotId += 45;
            }
            int slotWithOffset = LockSlots.INSTANCE.getSlotDifference((HandledScreen<net.minecraft.screen.ScreenHandler>) MinecraftClient.getInstance().currentScreen, slotId, false);
            LockSlots.INSTANCE.removeSlotFromLock(BindSlots.INSTANCE.getInitialSlotToBind());
            BindSlots.INSTANCE.bindSlots(slotWithOffset);
        }
        if (action == 2 && BindSlots.INSTANCE.getStartSlotLockingProcess() && QOLuxe.getLockSlot().matchesKey(key, scancode)) {
            isUserDevotedToLockSlots = true;
        }
        if (action == 1 && QOLuxe.getLockSlot().matchesKey(key, scancode) && MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> && !BindSlots.INSTANCE.getStartSlotLockingProcess()) {
            if (SlotUtils.INSTANCE.getHoveredSlotId() == -1) return;
            int slotId = SlotUtils.INSTANCE.getHoveredSlotId();
            if (MinecraftClient.getInstance().currentScreen instanceof InventoryScreen && slotId >= 5 && slotId <= 8) {
                // make the armor slots above 45 to make them not appear in chests and stuff
                slotId += 45;
            }

            int slotWithOffset = LockSlots.INSTANCE.getSlotDifference((HandledScreen<net.minecraft.screen.ScreenHandler>) MinecraftClient.getInstance().currentScreen, slotId, false);
            if (BindSlots.INSTANCE.getBindedSlot(slotWithOffset) != slotWithOffset) {
                BindSlots.INSTANCE.unbindSlots(slotWithOffset);
                return;
            }

            BindSlots.INSTANCE.setStartSlotLockingProcess(true);
            BindSlots.INSTANCE.setInitialSlot(slotWithOffset);
        }
    }
}
