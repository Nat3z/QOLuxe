package com.nat3z.qoluxe.mixins;

import com.nat3z.qoluxe.QOLuxe;
import com.nat3z.qoluxe.QOLuxeConfig;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(MobEntityRenderer.class)
public class MobEntityRendererMixin {
    public String cachedAnimalList = "";
    public List<String> cachedAnimalArray = new ArrayList<>();
    @Inject(at = @At("HEAD"), cancellable = true, method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z")
    private void run(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (QOLuxeConfig.animalsToNotRender.equals("")) {
            cachedAnimalList = QOLuxeConfig.animalsToNotRender;
            cachedAnimalArray = new ArrayList<>();
        }
        if (!cachedAnimalList.equals(QOLuxeConfig.animalsToNotRender)) {
            String[] animalsToNotRender = QOLuxeConfig.animalsToNotRender.split(",");
            // trim each element
            for (int i = 0; i < animalsToNotRender.length; i++) {
                animalsToNotRender[i] = animalsToNotRender[i].trim().toLowerCase();
            }
            cachedAnimalArray = Arrays.asList(animalsToNotRender);
            cachedAnimalList = QOLuxeConfig.animalsToNotRender;
        }

        if (entity instanceof AnimalEntity && QOLuxeConfig.dontRenderAnimals && cachedAnimalArray.contains(entity.getType().getName().getString().toLowerCase())) {
            cir.setReturnValue(false);
        }
        else if (QOLuxeConfig.dontRenderAnimals && QOLuxeConfig.animalsToNotRender.equals("*") && entity instanceof AnimalEntity) {
            cir.setReturnValue(false);
        }
    }
}
