package com.nat3z.qoluxe.hooks

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.utils.FileUtils
import com.nat3z.qoluxe.utils.FrameMaker
import com.nat3z.qoluxe.utils.ModAssistantHook
import com.nat3z.qoluxe.utils.WebUtils.fetch
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
    fun checkUpdates(ci: CallbackInfo) {
        val viciousFolder = File(".\\vicious\\")
        if (!viciousFolder.exists()) {
            viciousFolder.mkdir()
        }
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
        val subMods = File(".\\mods\\1.8.9\\")

        if (subMods.exists()) {
            modsFolder = subMods
        }
        //ModAssistantHook.openLauncher(modsFolder);
        /* check for updates & auto update */
        val finalModsFolder = modsFolder
        fetch("https://api.github.com/repos/Nat3z/$modName/releases") { res ->
            if (QOLuxe.VERSION != res.asJsonArray().get(0).getAsJsonObject().get("tag_name").getAsString()) {
                /* update that mod! */
                QOLuxe.LOGGER.info("Applying update to $modName...")
                fetch("https://api.github.com/repos/Nat3z/$modName/releases") { res1 ->
                    val downloadURL = res1.asJsonArray().get(0).getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString()
                    val body = res1.asJsonArray().get(0).getAsJsonObject().get("body").getAsString()
                    val jarFileName = res1.asJsonArray().get(0).getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()
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
                        if (file.name.startsWith("$modName")) {
                            updateAs = file.name
                            break
                        }

                    }
                    if (updateAs == "") {
                        updateAs = "$modName.jar"
                    }
                    preparedUpdate = true
                    updateMarkdown = res.asJsonArray()[0].asJsonObject["body"].asString
                    updateVersion = res.asJsonArray().get(0).asJsonObject["tag_name"].asString
                    updatePreperations = arrayOf<Any?>("https://api.github.com/repos/Nat3z/$modName/releases", downloadURL, finalModsFolder, updateAs, updateAs, hashID, jarFileName)
                }
            } else if (res.asJsonArray().size() > 1 && QOLuxe.VERSION != res.asJsonArray().get(1).getAsJsonObject().get("tag_name").getAsString() && res.asJsonArray().get(1).getAsJsonObject().get("prerelease").asBoolean) {
                /* update that mod! */
                QOLuxe.LOGGER.info("Applying update to QOLuxe...")
                fetch("https://api.github.com/repos/Nat3z/$modName/releases") { res1 ->
                    val downloadURL = res1.asJsonArray().get(1).getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString()
                    QOLuxe.LOGGER.info("Prepared update for $modName.")
                    var updateAs = ""
                    var hashID: String = ""
                    val body = res1.asJsonArray().get(1).getAsJsonObject().get("body").getAsString()
                    val jarFileName = res1.asJsonArray().get(1).getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()

                    if (body.contains("**SHA-256 Hash:** `")) {
                        QOLuxe.LOGGER.info(body.split("**SHA-256 Hash:** `"))
                        hashID =
                            body.split("Hash:** `")[1].split("`")[0]
                    }
                    /* for glass mod implementation */
                    for (file in finalModsFolder.listFiles()!!) {
                        if (file.name.startsWith("$modName")) {
                            updateAs = file.name
                            break
                        }

                    }
                    if (updateAs == "") {
                        updateAs = "$modName.jar"
                    }
                    preparedUpdate = true
                    updateMarkdown = res.asJsonArray()[1].asJsonObject["body"].asString
                    updateVersion = res.asJsonArray().get(1).asJsonObject["tag_name"].asString
                    updatePreperations = arrayOf<Any?>("https://api.github.com/repos/Nat3z/$modName/releases", downloadURL, finalModsFolder, updateAs, updateAs, hashID, jarFileName)
                }
            } else {
                val versionType = File(QOLuxe.generalFolder.getAbsolutePath() + "\\versionType.txt")
                if (QOLuxe.IS_UNSTABLE) {
                    QOLuxe.LOGGER.info("THIS USER IS CURRENTLY USING AN UNSTABLE RELEASE OF QOLuxe. IF LOGS WERE SENT AND THIS WAS RECEIVED, PLEASE DO NOT GIVE ANY SUPPORT.")
                }
                if (versionType.exists()) {
                    FileUtils.writeToFile(versionType, "" + QOLuxe.IS_UNSTABLE)
                } else if (QOLuxe.IS_UNSTABLE) {
                    /* version is unstable ui ui */
                    val willAllow = AtomicBoolean(true)
                    val maker = FrameMaker("Version detected as UNSTABLE.", Dimension(350, 150), WindowConstants.DO_NOTHING_ON_CLOSE, false)
                    val suc = AtomicBoolean(false)

                    val frame = maker.pack()
                    maker.addText("You are currently using an UNSTABLE release.", 10, 10, 11, false)
                    maker.addText("<html>" +
                            "Since this is your first release, we highly<br/>recommend you download the latest stable release.<br/>" +
                            "Do you acknowledge that you will not receive any<br/>support and I (Nat3z) am not<br/>" +
                            "liable for any lost items due to<br/>crashes related to the mod?</html>", 10, 25, 11, false)

                    val allowUse = maker.addButton("Yes", 180, 70, 60, ActionListener{ e ->
                        suc.set(true)
                        willAllow.set(true)
                        frame.dispose()
                    })
                    val disallowUse = maker.addButton("No", 270, 70, 60, ActionListener { e ->
                        suc.set(true)
                        willAllow.set(false)
                        frame.dispose()
                    })
                    maker.override()
                    /* keep it from continuing */
                    while (!suc.get()) {
                        try {
                            TimeUnit.SECONDS.sleep(1)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }

                    if (!willAllow.get())
                        exitProcess(0)
                    else {
                        try {
                            versionType.createNewFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        FileUtils.writeToFile(versionType, "" + QOLuxe.IS_UNSTABLE)
                    }
                }/* if downloaded is first ever download && version is unstable... */
                QOLuxe.LOGGER.info("User is on the latest version of $modName.")
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
    private fun downloader(modsFolder: File, link: String, fileName: String) {
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
