package com.baolong.ble_tpms.ui.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import com.clj.fastble.data.BleDevice;

public class LocalBleDevice {
    private String devideVersion;
    private BleDevice bleDevice;

    public String getDevideVersion() {
        return devideVersion;
    }

    public void setDevideVersion(String devideVersion) {
        this.devideVersion = devideVersion;
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
}
