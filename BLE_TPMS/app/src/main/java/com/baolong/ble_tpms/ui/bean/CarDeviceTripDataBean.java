package com.baolong.ble_tpms.ui.bean;

import java.io.Serializable;

public class CarDeviceTripDataBean implements Serializable {
    private int id;
    private int tripPressureDeviceId;
    private int temperature;
    private double pressure;
    private String addDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripPressureDeviceId() {
        return tripPressureDeviceId;
    }

    public void setTripPressureDeviceId(int tripPressureDeviceId) {
        this.tripPressureDeviceId = tripPressureDeviceId;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public String getAddDate() {
        return addDate;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }
}
