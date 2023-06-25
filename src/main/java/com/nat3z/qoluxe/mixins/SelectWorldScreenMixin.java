package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.gui.CloudSaveManagement;
import com.nat3z.qoluxe.gui.OverrideWarningCloud;
import com.nat3z.qoluxe.hooks.CloudSave;
import com.nat3z.qoluxe.utils.CloudProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin extends Screen {
    private ButtonWidget cloudSaveButton;
    private Identifier widgetsTexture = Identifier.of("qoluxe", "textures/gui/widgets.png");
    private ButtonWidget downloadAllCloudSavesButton;
    @Shadow private ButtonWidget selectButton;

    @Shadow private WorldListWidget levelList;

    @Shadow private ButtonWidget recreateButton;

    @Shadow protected TextFieldWidget searchBox;

    @Shadow private ButtonWidget editButton;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    // injects a button to the screen and rescaling the "Select" button to fit it alongside it
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.cloudSaveButton = CloudSave.INSTANCE.manageCloudSave(widgetsTexture, selectButton, this.levelList);
        // create a new textured button at 10, 22 with a width of 40 and a height of 40 with the texture of widgets.png offseted at 60, 0
        this.downloadAllCloudSavesButton = CloudSave.INSTANCE.downloadAllCloudSavesButton(widgetsTexture, editButton);
        this.cloudSaveButton.active = false;
        this.addDrawableChild(this.cloudSaveButton);
        this.addDrawableChild(this.downloadAllCloudSavesButton);


    }

    @Inject(method = "worldSelected", at = @At("HEAD"))
    private void worldSelected(boolean buttonsActive, boolean deleteButtonActive, CallbackInfo ci) {
        if (this.cloudSaveButton != null)
            this.cloudSaveButton.active = buttonsActive;
    }

}
