package com.nat3z.qoluxe.hooks

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

object CloudSave {

    fun init(screen: Screen, event: ButtonWidget.PressAction): ButtonWidget {
        return ButtonWidget.builder(Text.of("Cloud Save"), event).dimensions(screen.width / 2 - 76, screen.height - 52, 72, 20).build()
    }
}
