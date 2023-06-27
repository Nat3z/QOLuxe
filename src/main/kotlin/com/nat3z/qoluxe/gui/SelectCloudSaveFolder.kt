package com.nat3z.qoluxe.gui;

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.io.File
import javax.swing.JFileChooser

class SelectCloudSaveFolder(private val parent: Screen) : Screen(Text.of("Select Cloud Save Folder")) {
    var showingCloudSaveFolder = false;
    val gridWidget = GridWidget()
    var error = "";

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        if (context == null) return
        context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.RED}$error"), width / 2, gridWidget.y, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("Select Cloud Save Folder"), width / 2, gridWidget.y + 20, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.UNDERLINE}Tips"), width / 2, gridWidget.y + 100, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("If you are using ${Formatting.YELLOW}Google Drive Desktop${Formatting.RESET}, insert \"G:\\My Drive\\Minecraft\\\""), width / 2, gridWidget.y + 120, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("If you are using ${Formatting.YELLOW}OneDrive Desktop${Formatting.RESET}, insert \"C:\\Users\\{Desktop Username}\\OneDrive\\Minecraft\\\""), width / 2, gridWidget.y + 130, 0xFFFFFF)
    }

    override fun close() {
        MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
        return
    }

    override fun init() {
        gridWidget.mainPositioner.margin(4, 4, 4, 0)
        val adder = gridWidget.createAdder(2)
        var textArea = TextFieldWidget(textRenderer, 0, 0, 200, 20, Text.of(""))
        textArea.text = QOLuxeConfig.cloudSaveLocation
        adder.add(textArea, 2, gridWidget.copyPositioner().marginTop(40))
        adder.add(ButtonWidget.builder(Text.of("Confirm")) {
            try {
                var folder = File(textArea.text)
                System.out.println(folder.isDirectory)
                error = ""
                if (!folder.exists())
                    error = "The path you entered does not exist."
                else if (!folder.isDirectory) {
                    error = "The path you entered is not a folder."
                }

                if (error == "") {
                    QOLuxeConfig.cloudSaveLocation = folder.absolutePath
                    QOLuxe.viciousExt.saveConfig()
                    MinecraftClient.getInstance().setScreenAndRender(parent)
                }
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            }

        }.dimensions(0, 0, 200, 20).build(), 2, gridWidget.copyPositioner().marginTop(10))
        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 7, width, height, 0.5f, 0.25f)
        gridWidget.forEachChild { drawableElement ->
            addDrawableChild(
                drawableElement
            )
        }
    }

}
