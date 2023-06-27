package com.nat3z.qoluxe;

import com.nat3z.qoluxe.hooks.BindSlots;
import com.nat3z.qoluxe.hooks.LockSlots;
import com.nat3z.qoluxe.vicious.ConfigType;
import com.nat3z.qoluxe.vicious.Configurable;
import com.nat3z.qoluxe.vicious.ViciousConfig;
import net.minecraft.util.Formatting;

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
            name = "Allow Slot Binding Externally",
            description = "If enabled, this will enable slot binding on other servers other than singleplayer and realms.\n\n" +
                    "§cThis is not recommended as it can cause issues with server anticheats and is (technically) modifying vanilla actions, which can be considered cheating.",
            category = "Inventory",
            hidden = false,
            subCategory = "Slots",
            type = ConfigType.TOGGLE
    )
    public static boolean allowExternalSlotBinding = false;

    @Configurable(
            name = "Hide Cloud Save in Title Screen",
            description = "This will disable the \"Download All Cloud Saves\" Button in the Title Screen.",
            category = "Cloud Save",
            hidden = false,
            subCategory = "---",
            type = ConfigType.TOGGLE
    )
    public static boolean disableDownloadCloudSavesTitleScreen = false;

    @Configurable(
            name = "Clear all Slot Binds",
            description = "",
            category = "Inventory",
            hidden = false,
            subCategory = "Slots",
            type = ConfigType.RUNNABLE
    )
    public static Runnable clearAllSlotBinds = () -> {
        QOLuxeConfig.slotBinding = "";
        BindSlots.INSTANCE.updateCache();
    };

    @Configurable(
            name = "Clear all Slot Locks",
            description = "",
            category = "Inventory",
            hidden = false,
            subCategory = "Slots",
            type = ConfigType.RUNNABLE
    )
    public static Runnable clearAllLocks = () -> {
        QOLuxeConfig.lockedSlots = "";
        LockSlots.INSTANCE.refreshLockSlotCache();
    };


    @Configurable(
            name = "Locked Slots",
            description = "Slots that are locked",
            category = "Inventory",
            hidden = true,
            subCategory = "Slots",
            type = ConfigType.INPUT_FIELD
    )
    public static String lockedSlots = "";

    @Configurable(
            name = "Bind Slots",
            description = "Slots that are binded together",
            category = "Inventory",
            hidden = true,
            subCategory = "Slots",
            type = ConfigType.INPUT_FIELD
    )
    public static String slotBinding = "";

    @Configurable(
            name = "Cloud Save",
            description = "Location for Cloud Saves",
            category = "Cloud",
            hidden = true,
            subCategory = "---",
            type = ConfigType.INPUT_FIELD
    )
    public static String cloudSaveLocation = "";

    @Configurable(
            name = "Signature",
            description = "Cloud Save Signature",
            category = "Cloud",
            hidden = true,
            subCategory = "---",
            type = ConfigType.INPUT_FIELD
    )
    public static String cloudSaveSignature = "";
    @Configurable(
            name = "Levels Opted For Cloud Save",
            description = "Levels for Cloud Saving",
            category = "Cloud",
            hidden = true,
            subCategory = "---",
            type = ConfigType.INPUT_FIELD
    )
    public static String levelsOptedCloudSave = "";

}
