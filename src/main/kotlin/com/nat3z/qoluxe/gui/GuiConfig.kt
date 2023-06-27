package com.nat3z.qoluxe.gui

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import com.nat3z.qoluxe.vicious.ConfigType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.client.gui.widget.GridWidget.Adder
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class GuiConfig : Screen(Text.of("QOLuxe Config")) {
    var currentGridWidget = GridWidget()
    var pages = mutableListOf<GridWidget>()

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context)

        // Using the text renderer, make the color of the text into rainbow that changes smoothly and not give epilepsy
        System.currentTimeMillis().let {
            Formatting.byColorIndex((it / 90 % 12).toInt())?.colorValue?.let { it1 ->
                context.drawText(textRenderer, "QOLuxe Config", width / 2 - 35, currentGridWidget.y + 20,
                    it1, true)
            }
        }
        Formatting.WHITE.colorValue?.let { it1 ->
            context.drawText(textRenderer, "v${QOLuxe.VERSION}", width / 2 - 15, currentGridWidget.y + 30,
                it1, true)
        }

        Formatting.WHITE.colorValue?.let { it1 ->
            context.drawText(textRenderer, QOLuxeConfig.cloudSaveSignature, 10, 10,
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
        button.visible = false
        return button
    }
    fun addNavigationButtons(gridWidget: GridWidget, adder: Adder, canGoForward: Boolean = true, canGoBack: Boolean = true) {
        val pageBack = ButtonWidget.builder(Text.of("<")) { button: ButtonWidget ->
            goBackPage()
        }.width(25).build()
        val pageForward = ButtonWidget.builder(Text.of(">")) { button: ButtonWidget ->
            goForwardPage()
        }.width(25).build()

        pageBack.active = canGoBack
        pageForward.active = canGoForward
        adder.add(pageBack, 1, gridWidget.copyPositioner().marginTop(10).marginLeft(78))
        adder.add(pageForward, 1, gridWidget.copyPositioner().marginTop(10))
    }

    fun goForwardPage() {
        currentGridWidget.forEachChild() { child ->
            child.visible = false
        }
        currentGridWidget = pages[pages.indexOf(currentGridWidget) + 1]
        currentGridWidget.forEachChild() { child ->
            child.visible = true
        }
    }

    fun goBackPage() {
        currentGridWidget.forEachChild() { child ->
            child.visible = false
        }
        currentGridWidget = pages[pages.indexOf(currentGridWidget) - 1]
        currentGridWidget.forEachChild() { child ->
            child.visible = true
        }
    }


    fun initWidgets() {
        val gridWidgets = mutableListOf<GridWidget>()

        var gridWidget = GridWidget()
        gridWidget.mainPositioner.margin(4, 4, 4, 0)
        var adder = gridWidget.createAdder(2)
        var length = 0

        var itemsAllLength = 0
        for (configItem in QOLuxe.viciousExt.configItems) {
            if (length >= 4) {
                val canGoForward = itemsAllLength < QOLuxe.viciousExt.configItems.filter { !it.hidden }.size
                val canGoBack = itemsAllLength > 4
                addNavigationButtons(gridWidget, adder, canGoForward, canGoBack)
                gridWidget.refreshPositions()
                SimplePositioningWidget.setPos(gridWidget, 0, 0, width, height, 0.5f, 0.25f)

                gridWidgets.add(gridWidget)
                length = 0;
                gridWidget = GridWidget()
                adder = gridWidget.createAdder(2)
                gridWidget.mainPositioner.margin(4, 4, 4, 0)
            }
            var item: Widget = generateEmptyButton()
            if (configItem.type == ConfigType.TOGGLE) {
                val button = ButtonWidget.builder(
                    Text.of(configItem.name + ": " + if (configItem.field.getBoolean(QOLuxe.viciousExt.config)) "${Formatting.GREEN}Enabled" else "${Formatting.RED}Disabled")
                ) { button: ButtonWidget ->
                    configItem.field.set(QOLuxe.viciousExt.config, !(configItem.field.getBoolean(QOLuxe.viciousExt.config)))
                    button.message = Text.of(configItem.name + ": " + if (configItem.field.getBoolean(QOLuxe.viciousExt.config)) "${Formatting.GREEN}Enabled" else "${Formatting.RED}Disabled")
                }.tooltip(Tooltip.of(Text.of(configItem.description))).width(204).build()
                // check if the user is currently connected to the realms and disable the button if they are
                button.visible = false
                item = button
            }
            else if (configItem.type == ConfigType.INPUT_FIELD) {
                val textField = TextFieldWidget(textRenderer, 0, 0, 204, 20, Text.of(configItem.name))
                textField.setText(configItem.field.get(QOLuxe.viciousExt.config).toString())
                textField.setChangedListener { text: String ->
                    try {
                        configItem.field.set(QOLuxe.viciousExt.config, text)
                    } catch (e: Exception) {
                        textField.setText(configItem.field.get(QOLuxe.viciousExt.config).toString())
                    }
                }
                textField.visible = false
                textField.tooltip = Tooltip.of(Text.of("${Formatting.YELLOW}${configItem.name}${Formatting.RESET}\n${configItem.description}"))
                item = textField
            }
            else if (configItem.type == ConfigType.RUNNABLE) {
                val button = ButtonWidget.builder(
                    Text.of(configItem.name)
                ) { button: ButtonWidget ->
                    (configItem.field.get(QOLuxe.viciousExt.config) as Runnable).run()
                }.tooltip(Tooltip.of(Text.of(configItem.description))).width(204).build()
                // check if the user is currently connected to the realms and disable the button if they are
                button.visible = false
                item = button
            }

            if (!configItem.hidden) {
                adder.add(item, 2, if (length == 0) gridWidget.copyPositioner().marginTop(50) else gridWidget.copyPositioner().marginTop(10))
                length += 1
                itemsAllLength += 1
            }

        }

        if (length < 4) {
            while (length < 4) {
                if (length == 0) {
                    adder.add(generateEmptyButton(), 2, gridWidget.copyPositioner().marginTop(50))
                    length += 1
                    continue
                }
                adder.add(generateEmptyButton(), 2, gridWidget.copyPositioner().marginTop(10))
                length += 1
            }

            val canGoBack = itemsAllLength > 4
            addNavigationButtons(gridWidget, adder, false, canGoBack)
            gridWidget.refreshPositions()
            SimplePositioningWidget.setPos(gridWidget, 0, 0, width, height, 0.5f, 0.25f)

            gridWidgets.add(gridWidget)
        }

        pages = gridWidgets
        currentGridWidget = pages[0]
        System.out.println("Pages: ${pages.size}")

        gridWidgets.forEach() { gridWidget ->
            gridWidget.forEachChild() { child ->
                child.visible = false
            }
            gridWidget.forEachChild { drawableElement: ClickableWidget? ->
                addDrawableChild(
                    drawableElement
                )
            }
        }

        currentGridWidget.forEachChild() { child ->
            child.visible = true
        }
    }
}
