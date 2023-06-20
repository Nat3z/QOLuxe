package com.nat3z.qoluxe.utils

import com.nat3z.qoluxe.QOLuxe
import com.nat3z.qoluxe.QOLuxeConfig
import com.nat3z.qoluxe.hooks.MinecraftHook
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.MessageScreen
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry
import net.minecraft.text.Text
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object CloudProvider {
    private val cloudProviderJar: File = File("./vicious/cloud-provider.jar")
    @JvmStatic
    var performingCloudTask = false
    var scheduledDelete = false;
    var scheduledResolveSaveConflict = false;
    var openCloudSaveConfig = false;
    var worldEntryForSelected: WorldEntry? = null;
    fun getCloudWorldDirectory(worldFolder: File): File {
        val folder = File(QOLuxeConfig.cloudSaveLocation + "/${worldFolder.name}")
        if (!folder.exists())
            folder.mkdir()
        return folder
    }

    fun getLastSave(saveData: String): String {
        try {
            // get the last line of the save log
            var lastSave = saveData.split("\n").last()
            if (lastSave.isBlank() && saveData.split("\n").size > 1) {
                lastSave = saveData.split("\n").get(saveData.split("\n").size - 2)
            }
            // parse date from string
            return lastSave
        } catch (ex: Exception) {
            return "Unknown"
        }
    }

    fun deleteSave(worldFolder: File) {
        MinecraftClient.getInstance().setScreenAndRender(
            MessageScreen(
                Text.of("Deleting Cloud Save...")
            )
        )
        performingCloudTask = true
        FileUtils.deleteDirectory(getCloudWorldDirectory(worldFolder))
        performingCloudTask = false
        MinecraftClient.getInstance().setScreenAndRender(null)
    }

    fun checkForSaveConflict_Sync(worldFolder: File): Boolean {
        performingCloudTask = true
        System.out.println("java -jar ${cloudProviderJar.absolutePath} sync ${getCloudWorldDirectory(worldFolder).path} ${worldFolder.path}")
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "sync",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))

        val stdError = BufferedReader(InputStreamReader(process.errorStream))
        var s: String? = null
        while (stdInput.readLine().also { s = it } != null) {
            if (s?.contains("Conflict")!!) {
                performingCloudTask = false
                return true
            }
        }

        while (stdError.readLine().also { s = it } != null) {
            if (s?.contains("Conflict")!!) {
                performingCloudTask = false
                return true
            }
        }

        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode != 0
    }
    fun syncSave(worldFolder: File): Boolean {
        performingCloudTask = true
        System.out.println("java -jar ${cloudProviderJar.absolutePath} sync ${getCloudWorldDirectory(worldFolder).path} ${worldFolder.path}")
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "sync",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        ioStream(process)
        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode == 0
    }

    fun saveWorldChanges(worldFolder: File): Boolean {
        performingCloudTask = true
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "save",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        ioStream(process)
        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode == 0
    }

    // This command resolves the save confliction, overwriting the client save and turning it into the cloud save.
    fun resolve_ClientToCloud(worldFolder: File): Boolean {
        performingCloudTask = true
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "resolve",
            "cloud",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        ioStream(process)
        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode == 0
    }

    // This command resolves the save confliction, overwriting the cloud save and turning it into the client save.
    fun resolve_CloudToClient(worldFolder: File): Boolean {
        performingCloudTask = true
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "resolve",
            "client",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        ioStream(process)

        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode == 0
    }

    fun downloadAllSaves() {
        if (QOLuxeConfig.cloudSaveLocation.isEmpty()) {
            MinecraftClient.getInstance().setScreenAndRender(
                MessageScreen(
                    Text.of("Missing a cloud location, please set it in the config")
                )
            )
            return;
        }

        performingCloudTask = true

        MinecraftClient.getInstance()
            .setScreenAndRender(MessageScreen(Text.of("Downloading all cloud saves....")))

        System.out.println("Downloading Cloud Saves...")
        FileUtils.copyDirectory(File(QOLuxeConfig.cloudSaveLocation), File("${MinecraftClient.getInstance().runDirectory}/saves/"))
        MinecraftClient.getInstance()
            .setScreenAndRender(MessageScreen(Text.of("Cloud save downloads complete. Please restart Minecraft to continue.")))
        System.out.println("Completed.")
        performingCloudTask = false
    }
    private fun ioStream(process: Process) {

        val stdInput = BufferedReader(InputStreamReader(process.inputStream))

        val stdError = BufferedReader(InputStreamReader(process.errorStream))
        var s: String? = null
        while (stdInput.readLine().also { s = it } != null) {
            println(s)
        }

        while (stdError.readLine().also { s = it } != null) {
            println(s)
        }
    }

    fun uploadSave(worldFolder: File): Boolean {
        performingCloudTask = true
        val process = ProcessBuilder(
            "java",
            "-jar",
            cloudProviderJar.absolutePath,
            "upload",
            getCloudWorldDirectory(worldFolder).path,
            worldFolder.path
        ).start()
        ioStream(process)
        val exitCode = process.waitFor()
        performingCloudTask = false
        return exitCode == 0
    }
    fun updateCloudProviderJar() {
        val viciousFolder = File(".\\vicious\\")
        if (!viciousFolder.exists()) {
            viciousFolder.mkdir()
        }
        val viciousUpdateCycle = File(".\\vicious\\cloud-provider.jar")
        /* if not downloaded, download vicious updater */
        val updaterVersion = File(MinecraftClient.getInstance().runDirectory.absolutePath + "\\vicious\\versionCloudProvider.txt")
        WebUtils.fetch("https://api.github.com/repos/Nat3z/CloudSaveProvider/releases/latest") { res ->
            val downloadURL =
                res.asJson().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url")
                    .getAsString()
            val version = res.asJson().get("tag_name").getAsString()

            try {
                if (!updaterVersion.exists()) {
                    updaterVersion.createNewFile()
                    com.nat3z.qoluxe.utils.FileUtils.writeToFile(updaterVersion, "no version found.")
                }
                if (!viciousUpdateCycle.exists() || !com.nat3z.qoluxe.utils.FileUtils.readFile(updaterVersion).equals(version)) {
                    QOLuxe.LOGGER.info("Cloud Provider is not installed/not up to date. Now updating Cloud Provider.")
                    MinecraftHook.downloader(viciousFolder, downloadURL, "cloud-provider.jar")
                    /* create version system */
                    com.nat3z.qoluxe.utils.FileUtils.writeToFile(updaterVersion, version)
                }
            } catch (e: IOException) {
                QOLuxe.LOGGER.error("Failed to download Vicious cloud-provider.jar")
                e.printStackTrace()
            }
        }
    }
}
