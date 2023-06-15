package com.nat3z.qoluxe.utils

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot
import org.lwjgl.opengl.GL11
import java.awt.Color


object ContainerUtils {
    fun colorOnContainerSlot(context: DrawContext, slot: Slot, color: Color) {
        RenderSystem.enableDepthTest()
        RenderSystem.colorMask(true, true, true, false);
        // fill slot with color

    }
}
