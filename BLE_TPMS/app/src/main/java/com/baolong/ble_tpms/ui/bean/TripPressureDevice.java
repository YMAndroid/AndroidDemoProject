package com.baolong.ble_tpms.ui.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class TripPressureDevice implements Serializable {
    private int id;
    private int carRecodeId;
    private String carName;
    private int tripDataId;
    private int tripType;
    private int isUpgrade;//0 1 2
    private String deviceName;//蓝牙设备名称
    private String macAddress;//蓝牙设备Mac地址
    private int pairStatus;//1:配对 2：未配对
    private int bindStatus;//1：已绑定  2：未绑定（解绑）

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getPairStatus() {
        return pairStatus;
    }

    public void setPairStatus(int pairStatus) {
        this.pairStatus = pairStatus;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarRecodeId() {
        return carRecodeId;
    }

    public void setCarRecodeId(int carRecodeId) {
        this.carRecodeId = carRecodeId;
    }

    public int getTripDataId() {
        return tripDataId;
    }

    public void setTripDataId(int tripDataId) {
        this.tripDataId = tripDataId;
    }

    public int getTripType() {
        return tripType;
    }

    public void setTripType(int tripType) {
        this.tripType = tripType;
    }

    public int getIsUpgrade() {
        return isUpgrade;
    }

    public void setIsUpgrade(int isUpgrade) {
        this.isUpgrade = isUpgrade;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(int bindStatus) {
        this.bindStatus = bindStatus;
    }
}