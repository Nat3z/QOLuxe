package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.hooks.BindSlots;
import com.nat3z.qoluxe.hooks.LockSlots;
import com.nat3z.qoluxe.utils.SlotUtils;
import kotlin.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
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
        SlotUtils.INSTANCE.setHoveredSlotId(-1);
        QOLuxeConfig.lockedSlots = LockSlots.INSTANCE.convertToString();
    }
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.focusedSlot != null)
            SlotUtils.INSTANCE.setHoveredSlotId(this.focusedSlot.id);

        try {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (currentScreen instanceof HandledScreen<?>) {
                if (!(currentScreen instanceof CreativeInventoryScreen))
                    if (LockSlots.INSTANCE.getLockHoveredSlot() && focusedSlot != null) {
                        LockSlots.INSTANCE.setLockHoveredSlot(false);
                        if (BindSlots.INSTANCE.getBindedSlot(focusedSlot.id) == focusedSlot.id) {
                            if (focusedSlot.id <= LockSlots.INSTANCE.getSlotsOccupiedByContainer((HandledScreen<ScreenHandler>) currentScreen))
                                return;
                            int slotId = focusedSlot.id;
                            if (currentScreen instanceof InventoryScreen && focusedSlot.id >= 5 && focusedSlot.id <= 8) {
                                // make the armor slots above 45 to make them not appear in chests and stuff
                                slotId += 45;
                            }
                            int trueSlotId = LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) currentScreen, slotId, false);
                            if (!LockSlots.INSTANCE.isSlotLocked(trueSlotId)) {
                                LockSlots.INSTANCE.addSlotToLock(trueSlotId);
                                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            } else {
                                LockSlots.INSTANCE.removeSlotFromLock(trueSlotId);
                                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F));
                            }
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
                        drawOntoSlot(context, currentScreen, 0x80ff0000, lockedSlot);
                    }
                }

                int amountOfBinds = 0;
                if (!MinecraftClient.getInstance().isConnectedToRealms() && !MinecraftClient.getInstance().isConnectedToLocalServer())
                    if (!QOLuxeConfig.allowExternalSlotBinding) return;

                for (Pair<Integer, Integer> binds : BindSlots.INSTANCE.getSlotBinds()) {
                    if (BindSlots.INSTANCE.getSlotBindingColors().size() < amountOfBinds + 1)
                        continue;
                    long bindColor = BindSlots.INSTANCE.getSlotBindingColors().get(amountOfBinds);
                    int firstSlot = binds.component1();
                    int secondSlot = binds.component2();
                    if (firstSlot >= 0) {
                        if (firstSlot > 45 && currentScreen instanceof InventoryScreen) {
                            firstSlot -= 45;
                        }

                        if (secondSlot > 45 && currentScreen instanceof InventoryScreen) {
                            secondSlot -= 45;
                        }


                        if (drawOntoSlot(context, currentScreen, (int) bindColor, firstSlot)) continue;
                        if (drawOntoSlot(context, currentScreen, (int) bindColor, secondSlot)) continue;
                        amountOfBinds++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean drawOntoSlot(DrawContext context, Screen currentScreen, int bindColor, int secondSlot) {
        int slotIdSecond = LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) currentScreen, secondSlot, true);
        if (slotIdSecond > ((HandledScreen<?>) currentScreen).getScreenHandler().slots.size()) {
            return true;
        }
        Slot slot2 = handler.slots.get(slotIdSecond);
        context.fillGradient(
                RenderLayer.getGuiOverlay(),
                slot2.x,
                slot2.y,
                slot2.x + 16,
                slot2.y + 16,
                bindColor,
                bindColor,
                0
        );
        return false;
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (focusedSlot == null) return;
        if (checkIfSlotIsLocked(focusedSlot.id)) cir.setReturnValue(true);
        getBindedSlot(focusedSlot.id, cir);
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

    private void getBindedSlot(int slotId, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isConnectedToRealms() && !MinecraftClient.getInstance().isConnectedToLocalServer())
            if (!QOLuxeConfig.allowExternalSlotBinding) return;

        int originalSlotId = slotId;
        slotId = LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) MinecraftClient.getInstance().currentScreen, slotId, false);
        int otherSlot = BindSlots.INSTANCE.getBindedSlot(slotId);
        if (otherSlot == slotId) return;
        System.out.println("" + slotId + " " + otherSlot);
        // check if shift is held down
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            // swap the slots in the inventory
            Slot slot = handler.slots.get(originalSlotId);
            Slot slot2 = handler.slots.get(LockSlots.INSTANCE.getSlotDifference((HandledScreen<ScreenHandler>) MinecraftClient.getInstance().currentScreen, otherSlot, true));
            ItemStack itemStack = slot.getStack();
            ItemStack itemStack2 = slot2.getStack();
//            slot.setStack(itemStack2);
//            slot2.setStack(itemStack);
            if (otherSlot == 36 || slotId == 36) {
                if (otherSlot == 36)
                    MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
                else
                    MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId, slot2.id, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
            }
            else {
                MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
                MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId, slot2.id, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
                MinecraftClient.getInstance().interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.SWAP, MinecraftClient.getInstance().player);
            }
            if (ci instanceof CallbackInfoReturnable<?>)
                ((CallbackInfoReturnable<Boolean>) ci).setReturnValue(true);
            else
                ci.cancel();
        }
        else {

            if (ci instanceof CallbackInfoReturnable<?>)
                ((CallbackInfoReturnable<Boolean>) ci).setReturnValue(true);
            else
                ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot == null) return;

        if (checkIfSlotIsLocked(slot.id)) ci.cancel();
        getBindedSlot(slot.id, ci);

    }
}
