package com.nat3z.qoluxe;

import com.nat3z.qoluxe.vicious.ConfigType;
import com.nat3z.qoluxe.vicious.Configurable;
import com.nat3z.qoluxe.vicious.ViciousConfig;

public class QOLuxeConfig implements ViciousConfig {

    @Configurable(
            name = "Prevent Animals from Rendering",
            description = "",
            category = "Rendering",
            hidden = false,
            subCategory = "Animal",
            type = ConfigType.TOGGLE
    )
    public static boolean dontRenderAnimals = false;
    @Configurable(
            name = "Animals to Stop Rendering",
            description = "Separate with commas. (e.g. Sheep, Cow, Pig)",
            category = "Rendering",
            hidden = false,
            subCategory = "Animal",
            type = ConfigType.INPUT_FIELD
    )
    public static String animalsToNotRender = "Sheep";
    @Configurable(
            name = "Locked Slots",
            description = "Slots that are locked",
            category = "Inventory",
            hidden = true,
            subCategory = "Slots",
            type = ConfigType.INPUT_FIELD
    )
    public static String lockedSlots = "";

}
