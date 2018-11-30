package com.baolong.ble_tpms.ui.bean;

public class CarTypeBean {
    private String name;
    private int imageId;

    public CarTypeBean(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }
}
