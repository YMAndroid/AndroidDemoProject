package com.baolong.ble_tpms.ui.adapter;

import com.clj.fastble.data.BleDevice;

public interface Observer {
    void disConnected(BleDevice bleDevice);
}
