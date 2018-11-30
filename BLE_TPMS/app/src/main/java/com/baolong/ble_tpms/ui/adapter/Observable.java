package com.baolong.ble_tpms.ui.adapter;

import com.clj.fastble.data.BleDevice;


public interface Observable {

    void addObserver(Observer obj);


    void deleteObserver(Observer obj);

    void notifyObserver(BleDevice bleDevice);
}
