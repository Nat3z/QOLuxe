package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.gui.UpdateRealmURL;
import com.nat3z.qoluxe.hooks.RealmsMainScreenHook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsMainScreen.class)
public abstract class RealmsMainScreenMixin extends RealmsScreen {
    @Shadow abstract boolean shouldPlayButtonBeActive(@Nullable RealmsServer server);

    private ButtonWidget lithiumRealmsButton;

    private RealmsServer server;

    public RealmsMainScreenMixin(Text text) {
        super(text);
    }

    @Inject(method = "addLowerButtons", at = @At("RETURN"))
    private void addLowerButtons_return(CallbackInfo ci) {
        lithiumRealmsButton = ButtonWidget.builder(Text.of("Edit Lithium URL"), (it) -> {
            MinecraftClient.getInstance().setScreenAndRender(new UpdateRealmURL(this.server.id, this));
        }).dimensions((width / 2) - 205, height - 30, 100, 20).build();
        addDrawableChild(lithiumRealmsButton);
    }

    @Inject(method = "updateButtonStates", at = @At("RETURN"))
    private void updateButtonStates_return(RealmsServer server, CallbackInfo ci) {
        this.server = server;
        lithiumRealmsButton.active = this.shouldPlayButtonBeActive(server);
    }
}
