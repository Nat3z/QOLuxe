package com.nat3z.qoluxe.vicious;

import java.util.ArrayList;
import java.util.List;

public class HudElement {
    public int x;
    public int y;

    public int width;
    public int height;
    public HudElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public List<Integer> toList() {
        List<Integer> arIntegerList = new ArrayList<>();

        arIntegerList.add(x);
        arIntegerList.add(y);
        arIntegerList.add(width);
        arIntegerList.add(height);

        return arIntegerList;
    }

    public HudElement(List<Integer> array) {
        this.x = array.get(0);
        this.y = array.get(1);
        this.width = array.get(2);
        this.height = array.get(3);
    }
}
