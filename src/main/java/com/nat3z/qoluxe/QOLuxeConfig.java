package com.nat3z.qoluxe;

import com.nat3z.qoluxe.vicious.ConfigType;
import com.nat3z.qoluxe.vicious.Configurable;
import com.nat3z.qoluxe.vicious.ViciousConfig;

public class QOLuxeConfig implements ViciousConfig {
    @Configurable(
            name = "Animals to Stop Rendering",
            description = "Animals to stop rendering. Separate with commas. (e.g. Sheep, Cow, Pig)",
            category = "Rendering",
            UAYOR = false,
            subCategory = "Animal",
            type = ConfigType.INPUT_FIELD
    )
    public static String animalsToNotRender = "";
}
