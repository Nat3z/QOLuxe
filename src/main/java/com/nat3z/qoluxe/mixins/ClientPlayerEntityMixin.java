package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.hooks.LockSlots;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "dropSelectedItem", cancellable = true)
    private void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (!(MinecraftClient.getInstance().currentScreen instanceof InventoryScreen) &&
                LockSlots.INSTANCE.isSlotLocked((MinecraftClient.getInstance().player.getInventory().selectedSlot + 36))
        ) {
            cir.setReturnValue(false);
        }
    }
}
