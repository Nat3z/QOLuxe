package com.nat3z.qoluxe.mixins;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Text.class)
public abstract interface TextMixin {

    @Inject(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, Object[] args, CallbackInfoReturnable<MutableText> cir) {
        if (key.equals("selectWorld.deleteWarning")) {
            cir.setReturnValue(MutableText.of(new TranslatableTextContent("bull.that.will.obviously.not.work", "'%s' will be lost forever! (A long time!)\nCloud saves aren't deleted, though!", args)));
        }
    }
}
