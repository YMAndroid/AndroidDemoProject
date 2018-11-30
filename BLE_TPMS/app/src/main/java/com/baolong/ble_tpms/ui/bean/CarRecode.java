package com.baolong.ble_tpms.ui.bean;

import java.io.Serializable;

public class CarRecode implements Serializable {
    private int id;
    private int carType;
    private String carName;
    private String carRemark;
    private int status;

    public int getId() {
        return id;
    }

    public int getCarType() {
        return carType;
    }

    public String getCarName() {
        return carName;
    }

    public String getCarRemark() {
        return carRemark;
    }

    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCarType(int carType) {
        this.carType = carType;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public void setCarRemark(String carRemark) {
        this.carRemark = carRemark;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
