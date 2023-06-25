package com.nat3z.qoluxe.utils
import com.nat3z.qoluxe.QOLuxe
import net.minecraft.client.MinecraftClient
import java.awt.Dimension
import java.awt.event.ActionListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.WindowConstants
import kotlin.system.exitProcess

object ModAssistantHook {

    fun openLauncher(modsFolder: File) {
        val viciousFolder = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\")
        if (!viciousFolder.exists()) {
            viciousFolder.mkdir()
        }

        val launcherSettings = File(viciousFolder.absolutePath + "\\launcherSettings.txt")
        val viciousLauncher = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\launcher.jar")

        if (launcherSettings.exists()) {
            if (FileUtils.readFile(launcherSettings).equals("disabled"))
                return
        }
        if (!viciousLauncher.exists()) {
            val maker = FrameMaker("Mod Assistant", Dimension(350, 150), WindowConstants.DO_NOTHING_ON_CLOSE, false)
            val suc = AtomicBoolean(false)
            val downloadingFull = AtomicBoolean(false)

            val frame = maker.pack()
            maker.addText("Would you like to install Mod Assistant Lite or Mod Assistant?", 10, 10, 11, false)
            maker.addText("<html>Lite: Auto-Update system<br/>" + "Full: Mod Manager, Mods profile system, Minecraft pre-launch ui</html>", 10, 30, 11, false)

            val skipUpdate = maker.addButton("Lite", 50, 70, 100, ActionListener { e ->
                suc.set(true)
                frame.dispose()
            })
            val downloadUpdate = maker.addButton("Full", 180, 70, 100, ActionListener { e ->
                suc.set(true)
                downloadingFull.set(true)
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

            if (!downloadingFull.get()) {
                if (!launcherSettings.exists()) {
                    try {
                        launcherSettings.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    FileUtils.writeToFile(launcherSettings, "disabled")
                    return
                }
            }
        }
        /* check if version is update */
        WebUtils.fetch("https://api.github.com/repos/Nat3z/ModAssistant-Launcher/releases/latest") { res ->
            val viciousCycle = res.asJson().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString()
            try {
                val version = res.asJson().get("tag_name").getAsString()
                if (!launcherSettings.exists()) {
                    launcherSettings.createNewFile()
                    FileUtils.writeToFile(launcherSettings, "no version found.")
                }

                if (!launcherSettings.exists() || !FileUtils.readFile(launcherSettings).equals(version)) {
                    QOLuxe.LOGGER.info("Mod Assistant Launcher is not installed/not up to date. Now installing Mod Assistant.")
                    downloader(viciousFolder, viciousCycle, "launcher.jar")
                    /* create version system */
                    FileUtils.writeToFile(launcherSettings, version)
                }
            } catch (e: IOException) {
                QOLuxe.LOGGER.error("Failed to download Mod Assistant launcher.jar")
                e.printStackTrace()
            }
        }

        var classes: List<Class<*>>? = null
        try {
            classes = JarFileReader.getClassesFromJarFile(viciousLauncher)

            for (c in classes!!) {
                if (c.simpleName.toLowerCase() == "modassistant") {
                    val replaceMethod = c.getDeclaredMethod("open", File::class.java, File::class.java)

                    QOLuxe.LOGGER.info("Attempting to open Mod Assistant Launcher.")
                    val result = replaceMethod.invoke(c.newInstance(), modsFolder, MinecraftClient.getInstance().runDirectory) as Boolean
                    if (!result) exitProcess(0)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun open(apiURL: String, downloadURL: String, modsFolder: File, filename: String, replacement: String, sha256: String, newNameOfJar: String): Boolean {
        val viciousFolder = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\")
        if (!viciousFolder.exists()) {
            viciousFolder.mkdir()
        }
        val viciousUpdateCycle = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\updater.jar")
        val updaterVersion = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\update-version.txt")
        val optOutPreRelease = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\opt-out-pre.txt")

        /* if not downloaded, download vicious updater */
        WebUtils.fetch("https://api.github.com/repos/Nat3z/ModAssistant/releases/latest") { res ->
            val viciousCycle = res.asJson().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString()
            val version = res.asJson().get("tag_name").getAsString()

            try {
                if (!updaterVersion.exists()) {
                    updaterVersion.createNewFile()
                    FileUtils.writeToFile(updaterVersion, "no version found.")
                }
                if (!viciousUpdateCycle.exists() || !FileUtils.readFile(updaterVersion).equals(version)) {
                    QOLuxe.LOGGER.info("Mod Assistant is not installed/not up to date. Now installing Mod Assistant.")
                    downloader(viciousFolder, viciousCycle, "updater.jar")
                    /* create version system */
                    FileUtils.writeToFile(updaterVersion, version)
                }
            } catch (e: IOException) {
                QOLuxe.LOGGER.error("Failed to download Vicious updater.jar")
                e.printStackTrace()
            }
        }
        var disabledPreRelease = arrayOf("")
        if (optOutPreRelease.exists())
            disabledPreRelease = FileUtils.readFile(optOutPreRelease).split("/n").toTypedArray()

        val willDownload = AtomicBoolean(true)
        val assistVersionDownload = AtomicReference("")
        val finalDisabledPreRelease = disabledPreRelease
        WebUtils.fetch(apiURL) { res ->
            assistVersionDownload.set(res.asJsonArray().get(0).getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString())

            for (uri in finalDisabledPreRelease) {
                if (uri == assistVersionDownload.get()) {
                    willDownload.set(false)
                }
            }
            /* not in list of no pre-release */
            if (willDownload.get()) {
                if (res.asJsonArray().size() > 1)
                    if (res.asJsonArray()
                            .get(1).asJsonObject.get("prerelease").asBoolean && downloadURL == res.asJsonArray().get(1)
                            .getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject()
                            .get("browser_download_url").getAsString()
                    ) {
                        /* pre-release ui */
                        val maker = FrameMaker(
                            "Pre-Release update found.",
                            Dimension(350, 150),
                            WindowConstants.DO_NOTHING_ON_CLOSE,
                            false
                        )
                        val suc = AtomicBoolean(false)

                        val frame = maker.pack()
                        maker.addText("The update for $filename is a pre-release.", 10, 10, 11, false)
                        maker.addText("Would you like to download this update?", 40, 30, 15, false)

                        val skipUpdate = maker.addButton("Skip", 50, 70, 100, ActionListener { e ->
                            suc.set(true)
                            willDownload.set(false)
                            frame.dispose()
                        })
                        val downloadUpdate = maker.addButton("Download", 180, 70, 100, ActionListener { e ->
                            suc.set(true)
                            willDownload.set(true)
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
                    }
            }
        }
        if (!willDownload.get()) {
            if (!optOutPreRelease.exists()) {
                try {
                    optOutPreRelease.createNewFile()
                    FileUtils.writeToFile(optOutPreRelease, assistVersionDownload.get() + "/n")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                var alreadyDisabled = false
                for (uri in finalDisabledPreRelease) {
                    if (uri == assistVersionDownload.get()) {
                        alreadyDisabled = true
                        break
                    }
                }
                if (!alreadyDisabled)
                    FileUtils.writeToFile(optOutPreRelease, FileUtils.readFile(optOutPreRelease) + assistVersionDownload.get() + "/n")
            }
            QOLuxe.LOGGER.info("QOLuxe Update Abandoned.")
            return false
        }

        try {
            // get the exe that is running the jar (jre)
            val javaHome = System.getProperty("java.home")
            // get the java bin path
            val javaBin = javaHome + File.separator + "bin" + File.separator + "java"
            // make it so that it appendes exe if on windows (so it can run) and if not, it will just run it as is
            val javaBinPath = if (System.getProperty("os.name").toLowerCase().contains("win")) javaBin + ".exe" else javaBin

            // go to updater.jar and run the jar file with

            val process = ProcessBuilder(
                "java",
                "-jar",
                viciousUpdateCycle.absolutePath,
                downloadURL,
                modsFolder.path,
                filename,
                replacement,
                sha256,
                "true",
                newNameOfJar
            ).start()

            QOLuxe.LOGGER.info("Running Mod Assistant. " + "java -jar " + viciousUpdateCycle.absolutePath + " " + downloadURL + " " + modsFolder.absolutePath + " " + filename + " " + replacement + " " + sha256 + " true " + newNameOfJar)
            exitProcess(0)

        } catch (ex: Exception) {
            QOLuxe.LOGGER.error("Failed to auto-update using Mod Assistant.")
            ex.printStackTrace()
            return false
        }
        return true
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
