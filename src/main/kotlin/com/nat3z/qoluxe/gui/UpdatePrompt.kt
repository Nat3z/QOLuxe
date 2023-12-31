package com.nat3z.qoluxe.gui

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.hooks.MinecraftHook
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.io.FileUtils
import java.io.File

class UpdatePrompt : Screen(Text.of("Update Available")) {
    val gridWidget = GridWidget()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null) return
        renderBackground(context)
        context.matrices.push()
        context.matrices.scale(2f, 2f, 1f)
        if (MinecraftHook.isPreRelease)
            context.drawCenteredTextWithShadow(textRenderer, "Pre-Release Available", ((width / 2) / 2f).toInt(), (gridWidget.y - 40) / 2, 0xffffff)
        else if (!MinecraftHook.criticalUpdate)
            context.drawCenteredTextWithShadow(textRenderer, "Update Available", ((width / 2) / 2f).toInt(), (gridWidget.y - 40) / 2, 0xffffff)
        else
            context.drawCenteredTextWithShadow(textRenderer, "${Formatting.RED}CRITICAL UPDATE AVAILABLE", ((width / 2) / 2f).toInt(), (gridWidget.y - 40) / 2, 0xffffff)
        context.matrices.pop()

        context.drawCenteredTextWithShadow(textRenderer, "You are required to update to the latest version of QOLuxe", width / 2, gridWidget.y, 0xffffff)
        context.drawCenteredTextWithShadow(textRenderer, "Current Version: ${QOLuxe.VERSION}", width / 2, gridWidget.y + 20, 0xffffff)
        context.drawCenteredTextWithShadow(textRenderer, "Latest Version: ${MinecraftHook.updateVersion}", width / 2, gridWidget.y + 30, 0xffffff)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        MinecraftClient.getInstance().setScreenAndRender(null)
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun init() {
        val adder = gridWidget.createAdder(2)
        adder.add(ButtonWidget.builder(Text.of("Update Now")) {
            MinecraftClient.getInstance().close()
        }.dimensions(0, 0, 150, 20).build(), 2, gridWidget.copyPositioner().marginTop(70))
        if (!MinecraftHook.criticalUpdate) {
            adder.add(ButtonWidget.builder(Text.of("Update Later")) {
                MinecraftClient.getInstance().setScreenAndRender(null)
            }.dimensions(0, 0, 150, 20).build(), 2, gridWidget.copyPositioner().marginTop(10))
            adder.add(ButtonWidget.builder(Text.of("Ignore Update")) {
                MinecraftClient.getInstance().setScreenAndRender(null)
                MinecraftHook.preparedUpdate = false
                val optOutPreRelease = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\opt-out-pre.txt")
                if (!optOutPreRelease.exists()) {
                    optOutPreRelease.createNewFile()
                }
                val currentContents = FileUtils.readFileToString(optOutPreRelease, "UTF-8")
                FileUtils.writeStringToFile(optOutPreRelease, currentContents + MinecraftHook.updateUrl + "\n", "UTF-8")

            }.dimensions(0, 0, 150, 20).build(), 2, gridWidget.copyPositioner().marginTop(10))
        }
        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 7, width, height, 0.5f, 0.25f)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            addDrawableChild(
                drawableElement
            )
        }
    }
}
