package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.hooks.LockSlots;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    int hotbarSlotIndex = 0;
    // add mixin to the render method
    @Inject(at = @At("RETURN"), method = "renderHealthBar")
    private void render(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {

    }

    @Inject(at = @At("RETURN"), method = "renderHotbar")
    private void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        hotbarSlotIndex = 36;
    }

    @Inject(at = @At("HEAD"), method = "renderHotbarItem")
    private void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (LockSlots.INSTANCE.isSlotLocked(hotbarSlotIndex)) {
            context.fillGradient(x, y, x + 16, y + 16, 0x80ff0000, 0x80ff0000);
        }
        hotbarSlotIndex++;
    }
}
