package com.nat3z.qoluxe.gui

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class GuiConfig : Screen(Text.of("QOLuxe Config")) {
    val gridWidget = GridWidget()

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context)
        // Using the text renderer, make the color of the text into rainbow that changes smoothly and not give epilepsy
        System.currentTimeMillis().let {
            Formatting.byColorIndex((it / 90 % 12).toInt())?.colorValue?.let { it1 ->
                context.drawText(textRenderer, "QOLuxe Config", width / 2 - 35, gridWidget.y + 20,
                    it1, true)
            }
        }
        Formatting.WHITE.colorValue?.let { it1 ->
            context.drawText(textRenderer, "v${QOLuxe.VERSION}", width / 2 - 35, gridWidget.y + 10,
                it1, true)
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun init() {
        initWidgets()
        super.init()
    }

    fun generateEmptyButton(): ButtonWidget {
        val button = ButtonWidget.builder(
            Text.of("-- None --")
        ) { button: ButtonWidget ->

        }.width(204).build()
        button.active = false
        return button
    }
    fun initWidgets() {
        gridWidget.mainPositioner.margin(4, 4, 4, 0)
        val adder = gridWidget.createAdder(2)
        adder.add(
            ButtonWidget.builder(
                Text.of("Render Animals: " + if (QOLuxe.shouldDisableAnimalRendering) Formatting.RED.toString() + "Disabled" else Formatting.GREEN.toString() + "Enabled")
            ) { button: ButtonWidget ->
                QOLuxe.shouldDisableAnimalRendering =
                    !QOLuxe.shouldDisableAnimalRendering
                button.message =
                    Text.of("Render Animals: " + if (QOLuxe.shouldDisableAnimalRendering) Formatting.RED.toString() + "Disabled" else Formatting.GREEN.toString() + "Enabled")
            }.width(204).build(), 2, gridWidget.copyPositioner().marginTop(50)
        )
        val animalsEffectedWidget = TextFieldWidget(textRenderer, 0, 0, 200, 20, Text.of("Sheep"))
        animalsEffectedWidget.text = QOLuxeConfig.animalsToNotRender
        animalsEffectedWidget.setPlaceholder(Text.of("${Formatting.BOLD}Sheep"));
        animalsEffectedWidget.setChangedListener { _: String? ->
            QOLuxeConfig.animalsToNotRender = animalsEffectedWidget.text
            QOLuxe.viciousExt.saveConfig()
        }
        animalsEffectedWidget.tooltip = Tooltip.of(Text.of("Animals to not render, separated by commas (e.x. Sheep, Chicken)"))
        adder.add(animalsEffectedWidget, 2, gridWidget.copyPositioner().marginTop(10))

        adder.add(generateEmptyButton(), 2, gridWidget.copyPositioner().marginTop(10))
        adder.add(generateEmptyButton(), 2, gridWidget.copyPositioner().marginTop(10))
        adder.add(generateEmptyButton(), 2, gridWidget.copyPositioner().marginTop(10))
        // create a page navigation button
        val pageBack = ButtonWidget.builder(Text.of("<")) { button: ButtonWidget ->
        }.width(25).build()
        val pageForward = ButtonWidget.builder(Text.of(">")) { button: ButtonWidget ->
        }.width(25).build()
        adder.add(pageBack, 1, gridWidget.copyPositioner().marginTop(10).marginLeft(75))
        adder.add(pageForward, 1, gridWidget.copyPositioner().marginTop(10))

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(gridWidget, 0, 0, width, height, 0.5f, 0.25f)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            addDrawableChild(
                drawableElement
            )
        }
    }
}
