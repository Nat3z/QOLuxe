package com.nat3z.qoluxe.hooks

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.utils.FrameMaker
import com.nat3z.qoluxe.utils.ModAssistantHook
import com.nat3z.qoluxe.utils.WebUtils
import com.nat3z.qoluxe.utils.WebUtils.fetch
import net.minecraft.client.MinecraftClient
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.Dimension
import java.awt.event.ActionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.WindowConstants
import kotlin.system.exitProcess

object MinecraftHook {
    public var preparedUpdate = false
    private var updatePreperations = arrayOfNulls<Any>(5)
    var modName = "QOLuxe"
    var updateVersion = ""
    var updateMarkdown = ""
    var isPreRelease = false
    var updateUrl = ""
    fun checkUpdates(ci: CallbackInfo) {
        val viciousFolder = File(".\\vicious\\")
        if (!viciousFolder.exists()) {
            viciousFolder.mkdir()
        }
        val optOutPreRelease = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\opt-out-pre.txt")

        var disabledPreRelease = arrayOf("")
        if (optOutPreRelease.exists())
            disabledPreRelease = org.apache.commons.io.FileUtils.readFileToString(optOutPreRelease, "UTF-8").split("\n").toTypedArray()


        val viciousUpdateCycle = File(".\\vicious\\updater.jar")
        /* if not downloaded, download vicious updater */
        fetch("https://api.github.com/repos/Nat3z/ModAssistant/releases/latest") { res ->
            val downloadURL = res.asJson().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString()
            if (!viciousUpdateCycle.exists()) {
                QOLuxe.LOGGER.info("Mod Assistant is not installed. Now installing Mod Assistant.")
                try {
                    downloader(viciousFolder, downloadURL, "updater.jar")
                } catch (e: IOException) {
                    QOLuxe.LOGGER.error("Failed to download Vicious updater.jar")
                    e.printStackTrace()
                }

            }
        }
        var modsFolder = File(".\\mods\\")

        //ModAssistantHook.openLauncher(modsFolder);
        /* check for updates & auto update */
        val finalModsFolder = modsFolder
        fetch("https://api.github.com/repos/Nat3z/$modName/releases") { res ->
            for (asset in res.asJsonArray()) {
                if (asset.asJsonObject.get("tag_name").asString.contains(QOLuxe.MC_COMPATIBLE_VERSION)) {
                    if (QOLuxe.VERSION + "-" + QOLuxe.MC_COMPATIBLE_VERSION != res.asJsonArray()[0].asJsonObject.get("tag_name").asString) {
                        QOLuxe.LOGGER.info("Applying update to $modName...")
                        val prerelease = asset.asJsonObject.get("prerelease").asBoolean
                        val downloadURL = asset.asJsonObject.get("assets").asJsonArray[0].asJsonObject.get("browser_download_url").asString
                        val body = asset.asJsonObject.get("body").getAsString()
                        val jarFileName = asset.asJsonObject.get("assets").asJsonArray[0].asJsonObject.get("name").asString
                        var hashID: String = ""
                        if (body.contains("**SHA-256 Hash:** `")) {
                            QOLuxe.LOGGER.info(body.split("**SHA-256 Hash:** `"))
                            hashID =
                                body.split("Hash:** `")[1].split("`")[0]
                        }
                        QOLuxe.LOGGER.info(hashID)

                        QOLuxe.LOGGER.info("Prepared update for $modName.")
                        var updateAs = ""
                        /* for glass mod implementation */
                        for (file in finalModsFolder.listFiles()!!) {
                            if (file.name.startsWith(modName)) {
                                updateAs = file.name
                                break
                            }

                        }
                        if (updateAs == "") {
                            updateAs = "$modName.jar"
                        }

                        if (disabledPreRelease.contains(downloadURL)) {
                            QOLuxe.LOGGER.info("Skipping update for $modName.")
                            break
                        }
                        preparedUpdate = true
                        updateMarkdown = asset.asJsonObject["body"].asString
                        updateVersion = asset.asJsonObject["tag_name"].asString
                        updateUrl = downloadURL
                        updatePreperations = arrayOf<Any?>("https://api.github.com/repos/Nat3z/$modName/releases", downloadURL, finalModsFolder, updateAs, updateAs, hashID, jarFileName)
                        isPreRelease = prerelease
                        QOLuxe.LOGGER.info("Update prepared for $modName.")
                    }
                    break
                }
            }
        }
    }

    fun startUpdate() {
        if (preparedUpdate) {
//            MinecraftClient.getInstance().window.close()
            QOLuxe.LOGGER.info("Starting $modName update...")
            ModAssistantHook.open(updatePreperations[0] as String, updatePreperations[1] as String,
                updatePreperations[2] as File, updatePreperations[3] as String, updatePreperations[4] as String, updatePreperations[5] as String, updatePreperations[6] as String)
        }
    }

    private fun isRedirected(header: Map<String, List<String>>): Boolean {
        return false
    }

    @Throws(IOException::class)
    fun downloader(modsFolder: File, link: String, fileName: String) {
        var link = link
        var url = URL(link)
        var http = url.openConnection() as HttpURLConnection
        var header = http.headerFields
        while (isRedirected(header)) {
            link = header["Location"]?.get(0)!!
            url = URL(link)
            http = url.openConnection() as HttpURLConnection
            header = http.headerFields
        }
        val input = http.inputStream
        val buffer = ByteArray(4096)
        var n = -1
        if (modsFolder.isFile) {
            val output = FileOutputStream(modsFolder)
            while ({ n = input.read(buffer); n }() != -1) {
                output.write(buffer, 0, n)
            }
            output.close()
        } else {
            val output = FileOutputStream(File(modsFolder.absolutePath + "\\" + fileName))
            while ({ n = input.read(buffer); n }() != -1) {
                output.write(buffer, 0, n)
            }
            output.close()
        }
    }
}
