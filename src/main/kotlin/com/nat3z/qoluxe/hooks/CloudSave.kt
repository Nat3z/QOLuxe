package com.nat3z.qoluxe.hooks

import com.nat3z.qoluxe.gui.OverrideWarningCloud
import com.nat3z.qoluxe.utils.CloudProvider.openCloudSaveConfig
import com.nat3z.qoluxe.utils.CloudProvider.worldEntryForSelected
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.WorldListWidget
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Consumer

object CloudSave {
    @JvmStatic
    val widgetsTexture = Identifier.of("qoluxe", "textures/gui/widgets.png")!!

    fun init(screen: Screen, event: ButtonWidget.PressAction): ButtonWidget {
        return ButtonWidget.builder(Text.of("Cloud Save"), event).dimensions(screen.width / 2 - 76, screen.height - 52, 72, 20).build()
    }

    fun downloadAllCloudSavesButton(widgetsTexture: Identifier, button: ButtonWidget): ButtonWidget {
        return TexturedButtonWidget(
            button.x - 25, button.y, 20, 20, 60, 0, 20, widgetsTexture, 128, 128
        ) { button: ButtonWidget? ->
            MinecraftClient.getInstance().setScreenAndRender(OverrideWarningCloud())
        };
    }

    fun manageCloudSave(widgetsTexture: Identifier, button: ButtonWidget, levelList: WorldListWidget): ButtonWidget {
        return TexturedButtonWidget(
            button.x - 25, button.y, 20, 20, 20, 0, 20, widgetsTexture, 128, 128
        ) { button: ButtonWidget? ->
            openCloudSaveConfig = true
            levelList.selectedAsOptional.ifPresent(Consumer { worldEntry: WorldEntry ->
                worldEntryForSelected = worldEntry
                worldEntry.play()
            })
        };
    }
}
