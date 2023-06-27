package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxeConfig;
import com.nat3z.qoluxe.gui.OverrideWarningCloud;
import com.nat3z.qoluxe.hooks.CloudSave;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "initWidgetsNormal")
    private void initWidgetsNormal(int y, int spacingY, CallbackInfo ci) {
        if (QOLuxeConfig.disableDownloadCloudSavesTitleScreen) return;

        this.addDrawableChild(new TexturedButtonWidget(
                this.width / 2 - 124, y, 20, 20, 60, 0, 20,
                CloudSave.getWidgetsTexture(), 128, 128,
                (buttonWidget) -> {
                    if (this.client == null) return;

                    this.client.setScreenAndRender(new OverrideWarningCloud());
                }
        ));
    }
}
