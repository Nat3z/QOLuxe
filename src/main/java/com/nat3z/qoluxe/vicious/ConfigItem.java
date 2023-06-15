package com.nat3z.qoluxe.vicious;

import java.lang.reflect.Field;

public class ConfigItem {
    private String name;
    private String description;
    private String category;
    private ConfigType vtype;
    private Field var;
    private ViciousConfig config;
    private String[] sliderOptions;
    private String subCategory;
    private boolean hidden;
    public ConfigItem(Field variable, String name, String description, String subCategory, String category, ConfigType type, boolean hidden, String[] sliderOptions, ViciousConfig config) {
        this.name = name;
        this.description = description;
        this.category = category;
        vtype = type;
        this.config = config;
        var = variable;
        this.sliderOptions = sliderOptions;
        this.hidden = hidden;
        this.subCategory = subCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String[] getSliderOptions() {
        return sliderOptions;
    }

    public Field getField() {
        return var;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ConfigType getType() {
        return vtype;
    }

    public String getCategory() {
        return category;
    }

    public Object getValue() {
        try {
            return var.get(config);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getHidden() {
        return hidden;
    }

    public ViciousConfig getConfig() {
        return config;
    }
}
