package com.nat3z.qoluxe.gui

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import com.nat3z.qoluxe.utils.LithiumServerUtils
import com.nat3z.qoluxe.utils.WebUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class UpdateRealmURL(val realmsUUID: Long, val parent: Screen?) : Screen(Text.of("Update Realms URL")) {
    var error = ""
    var scheduleClose = false
    override fun init() {
        val textArea = TextFieldWidget(textRenderer, (width / 2) - 100, height / 2 + 20, 200, 20, Text.of(""))
        textArea.text = ""
        val setButton = ButtonWidget.builder(Text.of("Apply")) {
            while (textArea.text.endsWith("/")) {
                textArea.text = textArea.text.substring(0, textArea.text.length - 1)
            }

            WebUtils.fetch(textArea.text + "/", "GET",
                { res ->
                    if (res.asJson().get("version").asString != "") {
                        QOLuxeConfig.lithiumRealmsURL = QOLuxeConfig.lithiumRealmsURL.replace(";$realmsUUID:${LithiumServerUtils.getLithiumRealmsLocation(realmsUUID)}", "")
                        QOLuxeConfig.lithiumRealmsURL += ";$realmsUUID:${textArea.text}"
                        QOLuxe.viciousExt.saveConfig()
                        scheduleClose = true
                    } else {
                        error = "The URL you entered is not a valid Lithium Realms server."
                    }
                },
                { err ->
                    error = "An error occurred when attempting to connect to the server."
                }
            )
        }.dimensions((width / 2) - 100, height / 2 + 60, 200, 20).build()
        val clearAndExit = ButtonWidget.builder(Text.of("Clear and Exit")) {
            QOLuxeConfig.lithiumRealmsURL = QOLuxeConfig.lithiumRealmsURL.replace(";$realmsUUID:${LithiumServerUtils.getLithiumRealmsLocation(realmsUUID)}", "")
            this.close()
        }.dimensions((width / 2) - 100, height / 2 + 90, 200, 20).build()
        addDrawableChild(textArea)
        addDrawableChild(clearAndExit)
        addDrawableChild(setButton)
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun close() {
        MinecraftClient.getInstance().setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null) return
        if (scheduleClose) {
            this.close()
            return
        }
        super.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, "${Formatting.RED}$error", width / 2, height / 2 - 60, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer,"Lithium Realms Setup", width / 2, height / 2 - 40, 0xFFFFFF)
        context.drawCenteredTextWithShadow(textRenderer, "${Formatting.GRAY}Enter the URL of your Lithium Realms server below. Ask the operator if you don't know what it is.", width / 2, height / 2 - 20, 0xFFFFFF)


    }
}
