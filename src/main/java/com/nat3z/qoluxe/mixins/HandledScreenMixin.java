package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.hooks.LockSlots;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Shadow @Final protected ScreenHandler handler;

    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(at = @At("RETURN"), method = "init")
    private void init(CallbackInfo ci) {
        LockSlots.INSTANCE.refreshLockSlotCache();
    }

    @Inject(at = @At("RETURN"), method = "close")
    private void close(CallbackInfo ci) {
        QOLuxeConfig.lockedSlots = LockSlots.INSTANCE.convertToString();
    }
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (currentScreen instanceof HandledScreen<?>) {
                if (!(currentScreen instanceof CreativeInventoryScreen))
                    if (LockSlots.INSTANCE.getLockHoveredSlot() && focusedSlot != null) {
                        LockSlots.INSTANCE.setLockHoveredSlot(false);
                        if (focusedSlot.id <= LockSlots.INSTANCE.getSlotsOccupiedByContainer((HandledScreen<ScreenHandler>) currentScreen)) return;
                        int slotId = focusedSlot.id;
                        if (currentScreen instanceof InventoryScreen && focusedSlot.id >= 5 && focusedSlot.id <= 8) {
                            // make the armor slots above 45 to make them not appear in chests and stuff
                            slotId += 45;
                        }
                        int trueSlotId = LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) currentScreen, slotId, false);
                        if (!LockSlots.INSTANCE.isSlotLocked(trueSlotId)) {
                            LockSlots.INSTANCE.addSlotToLock(trueSlotId);
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        }
                        else {
                            LockSlots.INSTANCE.removeSlotFromLock(trueSlotId);
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F));
                        }
                    }
//                if (focusedSlot != null) {
//                    context.fillGradient(
//                            RenderLayer.getGuiOverlay(),
//                            focusedSlot.x,
//                            focusedSlot.y,
//                            focusedSlot.x + 16,
//                            focusedSlot.y + 16,
//                            0x80ff0000,
//                            0x80ff0000,
//                            0
//                    );
//                    context.drawText(MinecraftClient.getInstance().textRenderer,
//                            focusedSlot.id + "",
//                            focusedSlot.x + 8 - MinecraftClient.getInstance().textRenderer.getWidth(focusedSlot.id + "") / 2,
//                            focusedSlot.y + 8 - MinecraftClient.getInstance().textRenderer.fontHeight / 2,
//                            0xffffffff, false);
//                }

                for (int lockedSlot : LockSlots.INSTANCE.getLockedSlots()) {
                    if (lockedSlot >= 0) {
                        if (lockedSlot > 45 && currentScreen instanceof InventoryScreen) {
                            lockedSlot -= 45;
                        }

                        int slotId = LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) currentScreen, lockedSlot, true);
                        if (slotId > ((HandledScreen<?>) currentScreen).getScreenHandler().slots.size()) {
                            continue;
                        }
                        Slot slot = handler.slots.get(slotId);
                        context.fillGradient(
                                RenderLayer.getGuiOverlay(),
                                slot.x,
                                slot.y,
                                slot.x + 16,
                                slot.y + 16,
                                0x80ff0000,
                                0x80ff0000,
                                0
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (focusedSlot == null) return;
        if (checkIfSlotIsLocked(focusedSlot.id)) cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (focusedSlot == null) return;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return;
        if (checkIfSlotIsLocked(focusedSlot.id)) cir.setReturnValue(true);
    }

    private boolean checkIfSlotIsLocked(int lockedSlot) {
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) {
            if (MinecraftClient.getInstance().currentScreen instanceof InventoryScreen && lockedSlot >= 5 && lockedSlot <= 8) {
                // make the armor slots above 45 to make them not appear in chests and stuff
                lockedSlot += 45;
            }
            return LockSlots.INSTANCE.isSlotLocked(LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) MinecraftClient.getInstance().currentScreen, lockedSlot, false));
        }
        return false;
    }

    @Inject(at = @At("HEAD"), method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot == null) return;
        if (checkIfSlotIsLocked(slot.id)) ci.cancel();
    }
}
