package com.nat3z.qoluxe.hooks

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import net.minecraft.client.MinecraftClient
import java.lang.Integer.parseInt

object BindSlots {

    // give me a list of a bunch of different colors to signify each slot binding using this color as the reference color 0x80ff0000
    val slotBindingColors = listOf(
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
        0x80ff00ff,
        0x8000ffff,
        0x80ff8000,
        0x8000ff80,
        0x800080ff,
        0x80ff0080,
        0x8000ff00,
        0x800000ff,
        0x80ffff00,
    )

    var initialSlotToBind = 0
    var startSlotLockingProcess = false;
    fun setInitialSlot(slotId: Int) {
        initialSlotToBind = slotId
    }

    fun bindSlots(letGoSlotId: Int) {
        if (initialSlotToBind == 0) return
        if (letGoSlotId == initialSlotToBind) return
        val resultString = "$initialSlotToBind+$letGoSlotId;"
        if (QOLuxeConfig.slotBinding.contains(resultString)) return
        if (QOLuxeConfig.lockedSlots.equals("$letGoSlotId") || QOLuxeConfig.lockedSlots.contains("$letGoSlotId,")) return
        if (QOLuxeConfig.lockedSlots.equals("$initialSlotToBind") || QOLuxeConfig.lockedSlots.contains("$initialSlotToBind,")) return
        QOLuxeConfig.slotBinding += resultString

        initialSlotToBind = 0
        updateCache()
    }

    private var slotsSplitBySemicolon = QOLuxeConfig.slotBinding.split(";")

    fun getBindedSlot(slotId: Int): Int {
        val resultString = "$slotId+"
        val result = slotsSplitBySemicolon.find { it.startsWith(resultString) || it.endsWith("+$slotId") } ?: return slotId

        var firstResult = result.split("+")[1].split(";")[0].toInt()
        if (firstResult == slotId) {
            firstResult = result.split("+")[0].split(";")[0].toInt()
        }
        return firstResult
    }

    fun isSlotTheSecondary(slotId: Int): Boolean {
        val resultString = "$slotId+"
        for (i in slotsSplitBySemicolon) {
            if (i.startsWith(resultString)) {
                return false
            }
            else if (i.endsWith("+$slotId")) {
                return true
            }
        }
        return false
    }

    fun unbindSlots(slotId: Int) {
        val resultString = "$slotId+${getBindedSlot(slotId)}"
        val alternateString = "${getBindedSlot(slotId)}+$slotId"
        QOLuxeConfig.slotBinding = QOLuxeConfig.slotBinding.replace("$resultString;", "")
        QOLuxeConfig.slotBinding = QOLuxeConfig.slotBinding.replace("$alternateString;", "")
        updateCache()
    }

    fun updateCache() {
        slotsSplitBySemicolon = QOLuxeConfig.slotBinding.split(";")
    }

    fun getSlotBinds(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        slotsSplitBySemicolon.forEach {
            if (it.isNotEmpty()) {
                result.add(Pair(parseInt(it.split("+")[0]), parseInt(it.split("+")[1])))
            }
        }
        return result
    }
}
