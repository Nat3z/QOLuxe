package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.gui.AddResourcePackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSettingsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RealmsSettingsScreen.class)
public class RealmsSettingsScreenMixin extends RealmsScreen {
    @Shadow @Final private RealmsServer serverData;

    protected RealmsSettingsScreenMixin(Text text) {
        super(text);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init_return(CallbackInfo ci) {

        ButtonWidget widget = ButtonWidget.builder(Text.of("Add Resource Pack"), button -> {
            AddResourcePackScreen screen = new AddResourcePackScreen(this.serverData.id, this, false);
            MinecraftClient.getInstance().setScreenAndRender(screen);
        }).dimensions(width / 2 - 53, row(14), 106, 20).build();
        this.addDrawableChild(widget);
    }
}
