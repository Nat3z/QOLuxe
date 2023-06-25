package com.nat3z.qoluxe.hooks

import com.nat3z.qoluxe.QOLuxeConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen
import net.minecraft.client.gui.screen.ingame.AnvilScreen
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen
import net.minecraft.client.gui.screen.ingame.CraftingScreen
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HopperScreen
import net.minecraft.client.gui.screen.ingame.HorseScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.screen.ingame.LoomScreen
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
import net.minecraft.client.gui.screen.ingame.SmithingScreen
import net.minecraft.client.gui.screen.ingame.StonecutterScreen
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import java.util.*
import java.util.stream.Collectors

object LockSlots {
    var lockHoveredSlot = false;
    var alreadyClicked = false;

    var lockedSlots: Set<Int> = HashSet()
    fun isSlotLocked(slot: Int): Boolean {
        return lockedSlots.contains(slot);
    }

    fun refreshLockSlotCache() {
        lockedSlots = Arrays.stream(QOLuxeConfig.lockedSlots.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()).map { s: String -> s.toInt() }.collect(Collectors.toSet())
    }

    fun getSlotsOccupiedByContainer(currentScreen: HandledScreen<ScreenHandler>): Int {
        if (currentScreen is GenericContainerScreen) {
            return ((currentScreen.screenHandler as GenericContainerScreenHandler).rows - 1) * 9 + 8
        }
        else if (currentScreen is GrindstoneScreen)
            return 2
        else if (currentScreen is HopperScreen)
            return 4
        else if (currentScreen is InventoryScreen)
            return 0
        else if (currentScreen is ShulkerBoxScreen)
            return 26
        else if (currentScreen is LoomScreen)
            return 3
        else if (currentScreen is SmithingScreen)
            return 3
        else if (currentScreen is AbstractFurnaceScreen)
            return 2
        else if (currentScreen is AnvilScreen)
            return 2
        else if (currentScreen is CraftingScreen)
            return 9
        else if (currentScreen is EnchantmentScreen)
            return 1
        else if (currentScreen is StonecutterScreen)
            return 1
        else if (currentScreen is CartographyTableScreen)
            return 2
        else if (currentScreen is HorseScreen)
            return 1
        return 0
    }

    fun getSlotDifference(currentScreen: HandledScreen<ScreenHandler>?, focusedSlotId: Int, getRelativeToContainer: Boolean = false): Int {
        val containerOffset = if (currentScreen != null) getSlotsOccupiedByContainer(currentScreen) else 0
        var armorOffset = 8

        if (currentScreen is InventoryScreen) armorOffset = 0

        return if (!getRelativeToContainer)
                (focusedSlotId - containerOffset) + armorOffset
        else (focusedSlotId + containerOffset) - armorOffset
    }

    fun removeSlotFromLock(slot: Int) {
        lockedSlots = lockedSlots.stream().filter { i: Int -> i != slot }.collect(Collectors.toSet())
    }
    fun addSlotToLock(slot: Int) {
        lockedSlots = lockedSlots.plus(slot)
    }

    fun convertToString(): String {
        return lockedSlots.stream().map(Objects::toString)
            .reduce { s, s2 -> "$s,$s2" }
            .orElse("")
    }

    fun onKey() {
        if (MinecraftClient.getInstance().currentScreen is InventoryScreen) {
            lockHoveredSlot = true;
        }
    }

    fun renderLockedSlot(context: DrawContext, handler: ScreenHandler, allLockedSlots: MutableList<Int>) {

    }
}
