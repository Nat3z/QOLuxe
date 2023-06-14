package com.nat3z.qoluxe.vicious;

import java.util.List;

public class Category {
    private List<Object> cfg;
    private String name;
    private double offset;
    public int dynamicOffset;
    public Category(String name, List<Object> cfg, double offset) {
        this.cfg = cfg;
        this.name = name;
        this.offset = offset;
        this.dynamicOffset = (int) offset;
    }

    public double getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

    public List<Object> getConfigs() {
        return cfg;
    }
}
