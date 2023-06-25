package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.hooks.BindSlots;
import com.nat3z.qoluxe.hooks.LockSlots;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Shadow @Final protected MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "dropSelectedItem", cancellable = true)
    private void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (!(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) &&
                LockSlots.INSTANCE.isSlotLocked((MinecraftClient.getInstance().player.getInventory().selectedSlot + 36))
        ) {
            cir.setReturnValue(false);
        }
        if (!MinecraftClient.getInstance().isConnectedToRealms() && !MinecraftClient.getInstance().isConnectedToLocalServer())
            if (!QOLuxeConfig.allowExternalSlotBinding) return;

        if (!(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) &&
                BindSlots.INSTANCE.getBindedSlot(
                        MinecraftClient.getInstance().player.getInventory().selectedSlot + 36
                ) != MinecraftClient.getInstance().player.getInventory().selectedSlot + 36)
        {
            // swap the slots instead
            int slotToSwap = BindSlots.INSTANCE.getBindedSlot(
                    MinecraftClient.getInstance().player.getInventory().selectedSlot + 36
            );
            if (MinecraftClient.getInstance().player.getInventory().selectedSlot == 0) {
                MinecraftClient.getInstance().interactionManager.clickSlot(
                        MinecraftClient.getInstance().player.playerScreenHandler.syncId,
                        slotToSwap,
                        0,
                        SlotActionType.SWAP,
                        MinecraftClient.getInstance().player
                );
            }
            else {
                MinecraftClient.getInstance().interactionManager.clickSlot(MinecraftClient.getInstance().player.playerScreenHandler.syncId, MinecraftClient.getInstance().player.getInventory().selectedSlot + 36, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
                MinecraftClient.getInstance().interactionManager.clickSlot(MinecraftClient.getInstance().player.playerScreenHandler.syncId, slotToSwap, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
                MinecraftClient.getInstance().interactionManager.clickSlot(MinecraftClient.getInstance().player.playerScreenHandler.syncId, MinecraftClient.getInstance().player.getInventory().selectedSlot + 36, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
            }
            cir.setReturnValue(false);
        }
    }
}
