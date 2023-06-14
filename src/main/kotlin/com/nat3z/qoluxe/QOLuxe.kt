package com.nat3z.qoluxe

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.command.CommandRegistryAccess
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

class QOLuxe : ClientModInitializer {

    override fun onInitializeClient() {
        viciousExt.updateConfigVariables()
        println("Hello Fabric world!")
        // register keybind
        KeyBindingHelper.registerKeyBinding(disableAnimalRendering)
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, _: CommandRegistryAccess? ->
            dispatcher.register(
                ClientCommandManager.literal("luxe").executes { _ ->
                    showGui = true
                    0
                }
            )
        })
        viciousExt.saveConfig()
    }

    companion object {
        const val MOD_ID = "qoluxe"
        val config = QOLuxeConfig()
        val viciousExt = QOLuxeVicious()
        @JvmStatic
        val generalFolder = File("${MinecraftClient.getInstance().runDirectory.absolutePath}/config/qoluxe")
        @JvmStatic
        val IS_UNSTABLE = false
        @JvmStatic
        val VERSION = "1.0.0"
        @JvmStatic
        val LOGGER: Logger = LogManager.getLogger("QOLuxe Logger")!!
        @JvmStatic
        var shouldDisableAnimalRendering = false
        @JvmStatic
        var showGui = false
        @JvmStatic
        val disableAnimalRendering = KeyBinding("Disable Animal Rendering", InputUtil.GLFW_KEY_C, "Essentially Essential")
    }
}
