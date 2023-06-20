package com.nat3z.qoluxe.gui;

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.utils.CloudProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

public class OverrideWarningCloud : Screen(Text.of("Override Cloud Save")) {

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null) return
        super.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.RED}WARNING:"), width / 2, 20, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("All saves on this Minecraft instance that are named the same"), width / 2, 30, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("as the ones in the cloud will be ${Formatting.DARK_RED}${Formatting.BOLD}OVERWRITTEN."), width / 2, 40, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("This action cannot be undone."), width / 2, 50, 0xFFFFFF)
    }

    override fun init() {
        val gridWidget = GridWidget()
        gridWidget.mainPositioner.margin(4, 4, 4, 0)
        val adder = gridWidget.createAdder(2)
        adder.add(ButtonWidget.builder(Text.of("Cancel")) { button: ButtonWidget ->
            MinecraftClient.getInstance().setScreenAndRender(null)
        }.dimensions(0, 0, 213, 20).build(), 2, gridWidget.copyPositioner().marginTop(100))
        var overrideWorlds = ButtonWidget.builder(Text.of("Override")) { button: ButtonWidget ->
            CloudProvider.downloadAllSaves()
        }.dimensions(0, 0, 213, 20).build()

        adder.add(overrideWorlds, 2, gridWidget.copyPositioner().marginTop(10))

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 7, width, height, 0.5f, 0.25f)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            addDrawableChild(
                drawableElement
            )
        }

        Thread {
            var secondsRemaining = 10
            overrideWorlds.active = false
            while (secondsRemaining > 0) {
                Thread.sleep(1000)
                secondsRemaining--
                overrideWorlds.message = Text.of("Override (${secondsRemaining}s)")
            }
            overrideWorlds.active = true
            overrideWorlds.message = Text.of("${Formatting.RED}Override")

        }.start()
    }
}
