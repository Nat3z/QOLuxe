package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @Shadow @Final private WorldCreator worldCreator;

    @Shadow @Nullable private GridWidget grid;

    private ButtonWidget createButton;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.worldCreator.getWorldName().trim().isBlank()) return;
        if (QOLuxeConfig.cloudSaveLocation.isBlank()) return;

        File otherFolder = new File(QOLuxeConfig.cloudSaveLocation + "./" + this.worldCreator.getWorldName().trim());
        if (!otherFolder.exists()) {
            if (this.createButton != null)
                this.createButton.active = true;
            return;
        };
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%sThis world name is already taken in the cloud!", Formatting.RED), 10, 10, 0xFFFFFF, false);
        if (this.grid == null) return;
        if (createButton == null)
            this.grid.forEachChild((child) -> {
                if (child instanceof ButtonWidget) {
                    ButtonWidget buttonEntry = (ButtonWidget) child;
                    if (buttonEntry.getMessage().equals(Text.translatable("selectWorld.create"))) {
                        createButton = buttonEntry;
                    }
                }
            });

        if (createButton != null) {
            this.createButton.active = false;
        }
    }
}
