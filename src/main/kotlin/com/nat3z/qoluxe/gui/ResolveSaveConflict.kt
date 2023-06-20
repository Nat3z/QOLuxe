package com.nat3z.qoluxe.gui;

import com.nat3z.qoluxe.QOLuxeConfig
import com.nat3z.qoluxe.utils.CloudProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.MessageScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text
import org.apache.commons.io.FileUtils
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat

public class ResolveSaveConflict(val levelName: String) : Screen(Text.of("Resolve Save Conflict")) {

    var checkingForSaveConflict = false;
    var conflictFound = false;
    val saveLogCloud = File("${QOLuxeConfig.cloudSaveLocation}/${levelName}/saveLog.txt")
    val saveLogLocal = File("${MinecraftClient.getInstance().runDirectory}/saves/${levelName}/saveLog.txt")

    var lastLocalSave = "Unknown-Set"
    var lastCloudSave = "Unknown-Set"
    val gridWidget = GridWidget()
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
        super.renderBackground(context)
        if (checkingForSaveConflict) {
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Checking for save conflicts..."), width / 2, height / 2, 0xFFFFFF)
            return
        }
        if (conflictFound) {
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Save conflict found!"), width / 2, gridWidget.y + 20, 0xFFFFFF)
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Please select which save you would like to use."), width / 2, gridWidget.y + 30, 0xFFFFFF)
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Last Cloud Save: $lastCloudSave"), width / 2, gridWidget.y + 50, 0xFFFFFF)
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Last Local Save: $lastLocalSave"), width / 2, gridWidget.y + 60, 0xFFFFFF)
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.of("No save conflict found!"), width / 2, height / 2, 0xFFFFFF)
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
        return
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false;
    }

    override fun init() {
        checkingForSaveConflict = true;
        Thread {

            val worldDirectory = File("${MinecraftClient.getInstance().runDirectory}/saves/$levelName")
            conflictFound = CloudProvider.checkForSaveConflict_Sync(worldDirectory)
            if (conflictFound) {
                gridWidget.mainPositioner.margin(4, 4, 4, 0)
                val adder = gridWidget.createAdder(2)
                adder.add(
                    ButtonWidget.builder(Text.of("Use Local Save")) { button: ButtonWidget ->
                        MinecraftClient.getInstance().setScreenAndRender(
                            MessageScreen(
                                Text.of("Overriding Cloud Save and Merging with Local Save...")
                            )
                        )
                        var result = CloudProvider.resolve_ClientToCloud(worldDirectory)
                        if (result) {
                            MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
                        } else {
                            MinecraftClient.getInstance().setScreenAndRender(
                                MessageScreen(
                                    Text.of("Merge failed! Please complete this merge manually by copying the contents of your local save into your cloud save")
                                )
                            )
                        }
                    }.dimensions(0, 0, 213, 20)
                        .tooltip(Tooltip.of(Text.of("This will override your cloud save, making the cloud save use the contents of your local save.\nTL;DR Cloud save loses progress.")))
                        .build(), 2, gridWidget.copyPositioner().marginTop(100)
                )
                adder.add(
                    ButtonWidget.builder(Text.of("Use Cloud Save")) { button: ButtonWidget ->
                        MinecraftClient.getInstance().setScreenAndRender(
                            MessageScreen(
                                Text.of("Overriding Cloud Save and Merging with Local Save...")
                            )
                        )
                        var result = CloudProvider.resolve_ClientToCloud(worldDirectory)
                        if (result) {
                            MinecraftClient.getInstance().setScreenAndRender(SelectWorldScreen(null))
                        } else {
                            MinecraftClient.getInstance().setScreenAndRender(
                                MessageScreen(
                                    Text.of("Merge failed! Please complete this merge manually by copying the contents of your local save into your cloud save")
                                )
                            )
                        }
                    }
                        .tooltip(Tooltip.of(Text.of("This will override your local save, making the local save use the contents of your cloud save.\nTL;DR Local save loses progress.")))
                        .dimensions(0, 0, 213, 20).build(), 2, gridWidget.copyPositioner().marginTop(10)
                )
                adder.add(ButtonWidget.builder(Text.of("Close")) { button: ButtonWidget ->
                    this.close()
                }.dimensions(0, 0, 213, 20).build(), 2, gridWidget.copyPositioner().marginTop(10))
            } else {
                gridWidget.mainPositioner.margin(4, 4, 4, 0)
                val adder = gridWidget.createAdder(2)
                adder.add(ButtonWidget.builder(Text.of("Close")) { button: ButtonWidget ->
                    MinecraftClient.getInstance().setScreenAndRender(null)
                }.dimensions(0, 0, 213, 20).build(), 2, gridWidget.copyPositioner().marginTop(100))
            }
            gridWidget.refreshPositions()
            SimplePositioningWidget.setPos(gridWidget, 0, this.height / 7, width, height, 0.5f, 0.25f)
            gridWidget.forEachChild { drawableElement: ClickableWidget? ->
                addDrawableChild(
                    drawableElement
                )
            }
            checkingForSaveConflict = false;
        }.start()
        super.init()
    }
}
