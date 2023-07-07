package com.nat3z.qoluxe.utils
import com.nat3z.qoluxe.QOLuxe
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
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

    private val LOGGER = LogManager.getLogger("Mod Assistant")!!

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
                    LOGGER.info("Mod Assistant is not installed/not up to date. Now installing Mod Assistant.")
                    downloader(viciousFolder, viciousCycle, "updater.jar")
                    /* create version system */
                    FileUtils.writeToFile(updaterVersion, version)
                }
            } catch (e: IOException) {
                LOGGER.error("Failed to download Vicious updater.jar")
                e.printStackTrace()
            }
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

            LOGGER.info("Running Mod Assistant. " + "java -jar " + viciousUpdateCycle.absolutePath + " " + downloadURL + " " + modsFolder.absolutePath + " " + filename + " " + replacement + " " + sha256 + " true " + newNameOfJar)
            exitProcess(0)

        } catch (ex: Exception) {
            LOGGER.error("Failed to auto-update using Mod Assistant.")
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
