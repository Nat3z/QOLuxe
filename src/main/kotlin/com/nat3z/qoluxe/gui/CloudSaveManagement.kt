package com.nat3z.qoluxe.gui

import com.nat3z.qoluxe.QOLuxeConfig
import com.nat3z.qoluxe.utils.CloudProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.io.FileUtils
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class CloudSaveManagement(val worldName: String, val worldEntry: WorldEntry) : Screen(Text.of("Cloud Save Management")) {
    val isCloudSaveActive = false;
    var gridWidget = GridWidget()
    val saveLogCloud = File("${QOLuxeConfig.cloudSaveLocation}/${worldName}/saveLog.txt")
    val saveLogLocal = File("${MinecraftClient.getInstance().runDirectory}/saves/${worldName}/saveLog.txt")

    var lastLocalSave = "Unknown-Set"
    var lastCloudSave = "Unknown-Set"
    var verifyDeletion = false

    override fun close() {
        MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
        return
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (lastCloudSave == "Unknown-Set") {
            if (saveLogCloud.exists())
                lastCloudSave = CloudProvider.getLastSave(FileUtils.readFileToString(saveLogCloud, "UTF-8"))
        }
        if (lastLocalSave == "Unknown-Set") {
            if (saveLogLocal.exists())
                lastLocalSave = CloudProvider.getLastSave(FileUtils.readFileToString(saveLogLocal, "UTF-8"))
        }
        if (context == null) return
        context.drawCenteredTextWithShadow(textRenderer, Text.of("Cloud Data Management"), width / 2, gridWidget.y + 10, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("World Name: $worldName"), width / 2, gridWidget.y + 40, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("Last Save (Local): $lastLocalSave"), width / 2, gridWidget.y + 50, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, Text.of("Last Save (Cloud): $lastCloudSave"), width / 2, gridWidget.y + 60, 0xFFFFFF)
        super.renderBackground(context)
        // draw a square below the grid widget to make it look like a box
        context.fillGradient(gridWidget.x - 4, gridWidget.y - 4, gridWidget.x + gridWidget.width + 4, gridWidget.y + gridWidget.height + 10, -1072689136, -804253680)

        super.render(context, mouseX, mouseY, delta)
    }
    override fun init() {
        QOLuxeConfig.levelsOptedOut = QOLuxeConfig.levelsOptedOut.replace("[%%%%]$worldName", "")
        gridWidget.mainPositioner.margin(4, 4, 4, 0)
        val adder = gridWidget.createAdder(2)
        if (QOLuxeConfig.cloudSaveLocation.isNotEmpty())
            adder.add(ButtonWidget.builder(Text.of("Resolve Conflicts")) { button: ButtonWidget ->
                CloudProvider.scheduledResolveSaveConflict = true
                MinecraftClient.getInstance().setScreenAndRender(null)
                worldEntry.play()
            }.dimensions(0, 0, 213, 20).build(), 2, gridWidget.copyPositioner().marginTop(90))
        else
            MinecraftClient.getInstance().setScreenAndRender(SelectCloudSaveFolder(this))

        // -- what to do with cloud saves --
        adder.add(ButtonWidget.builder(Text.of("Delete")) { button: ButtonWidget ->
            if (!verifyDeletion) {
                Thread {
                    button.active = false
                    button.message = Text.of("Delete (5s)")
                    var timeRemaining = 5
                    while (timeRemaining > 0) {
                        button.message = Text.of("Delete (${timeRemaining}s)")
                        Thread.sleep(1000)
                        timeRemaining--
                    }
                    button.message = Text.of("${Formatting.RED}Delete")
                    verifyDeletion = true
                    button.active = true
                }.start()
                return@builder
            }

            CloudProvider.deleteSave(File("${QOLuxeConfig.cloudSaveLocation}/${worldName}"))
        }.tooltip(Tooltip.of(Text.of("This will delete the cloud save of $worldName, not including the local save.\n\nThis is a ${Formatting.RED}destructive${Formatting.RESET} action."))).dimensions(0, 0, 102, 20).build(), 1, gridWidget.copyPositioner().marginTop(10))

        val disableButton = ButtonWidget.builder(Text.of("${Formatting.RED}Disable")) { button: ButtonWidget ->
            QOLuxeConfig.levelsOptedOut = QOLuxeConfig.levelsOptedOut + "[%%%%]" + worldName
            MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
        }.dimensions(0, 0, 102, 20).tooltip(Tooltip.of(Text.of("${Formatting.RED}This will disable cloud saves for " +
                "this world. ${Formatting.YELLOW}If you decide to reopen the cloud save menu, it will be re-enabled."))).build()
        adder.add(disableButton, 1, gridWidget.copyPositioner().marginTop(10))

        val editCloudPath = ButtonWidget.builder(Text.of("Edit Cloud Path")) { button: ButtonWidget ->
            MinecraftClient.getInstance().setScreenAndRender(SelectCloudSaveFolder(this))
        }.dimensions(0, 0, 102, 20).build()
        adder.add(editCloudPath, 1, gridWidget.copyPositioner().marginTop(10))

        val closeButton = ButtonWidget.builder(Text.of("Close")) { button: ButtonWidget ->
            MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
        }.dimensions(0, 0, 102, 20).build()

        adder.add(closeButton, 1, gridWidget.copyPositioner().marginTop(10))

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 8, width, height, 0.5f, 0.25f)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            addDrawableChild(
                drawableElement
            )
        }
        super.init()
    }
}
