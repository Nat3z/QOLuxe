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
import net.minecraft.util.Util
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*

class AddResourcePackScreen(val realmsUUID: Long, val parent: Screen?, val editLithiumRealmsURL: Boolean = false) : Screen(Text.of("Add Resource Pack")) {
    var resourcePackFolder: File? = null
    var error = ""
    var inEditMode = false
    val storedSignatureDirectory = File("${MinecraftClient.getInstance().runDirectory}/signatures/")
    val storedSignatureFile = File("${MinecraftClient.getInstance().runDirectory}/signatures/$realmsUUID.txt")
    var cancelButton: ButtonWidget? = null

    val lithiumRealmsURL = LithiumServerUtils.getLithiumRealmsLocation(realmsUUID)
    companion object {
        var alreadyOpenedFileExplorer = false
    }

    var scheduleClose = false

    override fun close() {
        alreadyOpenedFileExplorer = false
        MinecraftClient.getInstance().setScreen(parent)
    }
    override fun init() {
        if (!storedSignatureDirectory.exists() || !storedSignatureDirectory.isDirectory) {
            storedSignatureDirectory.mkdir()
        }

        if (lithiumRealmsURL == "" || editLithiumRealmsURL) {
            val textArea = TextFieldWidget(textRenderer, (width / 2) - 100, height / 2, 200, 20, Text.of(""))
            textArea.text = ""
            inEditMode = true
            val setButton = ButtonWidget.builder(Text.of("Apply")) {
                while (textArea.text.endsWith("/")) {
                    textArea.text = textArea.text.substring(0, textArea.text.length - 1)
                }

                WebUtils.fetch(textArea.text + "/", "GET",
                    { res ->
                        WebUtils.fetch(textArea.text + "/generatesignature", "POST",
                            { genSigRes ->
                                if (genSigRes.asJson().get("success").asBoolean) {
                                    if (!storedSignatureFile.exists()) {
                                        storedSignatureFile.createNewFile()
                                    }

                                    FileUtils.writeStringToFile(storedSignatureFile, genSigRes.asJson().get("signature").asString, "UTF-8")

                                    QOLuxeConfig.lithiumRealmsURL = QOLuxeConfig.lithiumRealmsURL.replace(";$realmsUUID:", "")
                                    QOLuxeConfig.lithiumRealmsURL += ";$realmsUUID:${textArea.text}"
                                    QOLuxe.viciousExt.saveConfig()
                                    MinecraftClient.getInstance().setScreenAndRender(AddResourcePackScreen(realmsUUID, parent, false))
                                }
                                else {
                                    error = "Failed to generate a signature. If you own this server, delete the file '.signature' in the root directory of your server."
                                }

                            },
                            { err ->
                                error = "An error occurred when attempting to connect to the server."
                            }
                        )
                    },
                    { err ->
                        error = "An error occurred when attempting to connect to the server."
                    }
                )
            }.dimensions((width / 2) - 100, height / 2 + 40, 200, 20).build()
            val cancelButton = ButtonWidget.builder(Text.of("Cancel")) {
                this.close()
            }.dimensions((width / 2) - 85, height / 2 + 70, 80, 20).build()
            val helpButton = ButtonWidget.builder(Text.of("Help")) {
                val string = "https://docs.nat3z.com/qoluxe/lithium-server"
                client!!.keyboard.clipboard = string
                Util.getOperatingSystem().open(string)
            }.dimensions((width / 2) + 5, height / 2 + 70, 80, 20).build()
            addDrawableChild(helpButton)
            addDrawableChild(cancelButton)
            addDrawableChild(textArea)
            addDrawableChild(setButton)
            return
        }


        resourcePackFolder = File("${System.getProperty("java.io.tmpdir")}/qoluxe-realms-$realmsUUID/")
        if (!resourcePackFolder!!.exists()) {
            resourcePackFolder!!.mkdirs()
        }
        val info = File("$resourcePackFolder/INSERT RESOURCE PACK HERE")
        if (!info.exists()) info.createNewFile()

        // open folder with os file explorer
        if (!alreadyOpenedFileExplorer) {
            alreadyOpenedFileExplorer = true
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe /select,${info.absolutePath}")
            }
            else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open ${info.absolutePath}")
            }
            else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                Runtime.getRuntime().exec("xdg-open ${info.absolutePath}")
            }

        }

        cancelButton = ButtonWidget.builder(Text.of("Cancel")) {
            this.close()
        }.dimensions((width / 2) - 50, height / 2 + 40, 100, 20).build()
        addDrawableChild(cancelButton)
    }

    var ticks = 0
    var uploadingFile = false
    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null) return
        if (scheduleClose) {
            this.close()
            return
        }

        super.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        ticks++

        Thread {
            if (resourcePackFolder == null) return@Thread
            if (ticks % 50 == 0 && !uploadingFile) {
                // check folder to see if there is a file that ends with .zip
                val files = resourcePackFolder!!.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.name.endsWith(".zip")) {
                            // upload file to server
                            uploadingFile = true
                            cancelButton?.visible = false

                            WebUtils.uploadFile(lithiumRealmsURL + "/uploadpack", file, FileUtils.readFileToString(storedSignatureFile, "UTF-8"), { res ->
                                if (res.asJson().get("success").asBoolean) {
                                    FileUtils.deleteDirectory(resourcePackFolder)
                                    scheduleClose = true
                                }
                                else {
                                    error = "An error occurred when attempting to upload the resource pack."
                                }
                            }, { err ->
                                error = "An error occurred when attempting to connect to the server."
                            })
                        }
                    }
                }
            }
        }.start()

        if (inEditMode) {
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Setup Lithium Realms"), width / 2, height / 2 - 40, 0xFFFFFF)
            context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.GRAY}Insert the URL of your Lithium Realms Server"), width / 2, height / 2 - 20, 0xFFFFFF)

            context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.RED}$error"), width / 2, height / 2 - 60, 0xFFFFFF)
        }
        else {
            context.drawCenteredTextWithShadow(textRenderer, Text.of("Add Resource Pack"), width / 2, height / 2 - 20, 0xFFFFFF)
            if (uploadingFile) {
                context.drawCenteredTextWithShadow(textRenderer, Text.of("Please wait while the resource pack is being uploaded..."), width / 2, height / 2, 0xFFFFFF)
            }
            else {
                context.drawCenteredTextWithShadow(textRenderer, Text.of("Please insert the resource pack that you would like to use inside of the folder."), width / 2, height / 2, 0xFFFFFF)
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.of("${Formatting.RED}$error"), width / 2, height / 2 - 40, 0xFFFFFF)
        }
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
}
