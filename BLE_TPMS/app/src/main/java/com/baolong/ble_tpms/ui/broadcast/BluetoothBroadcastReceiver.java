package com.baolong.ble_tpms.ui.broadcast;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.clj.fastble.BleManager;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothBroadcastReceiver";
    private AlertDialog alertDialog;
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(final Context context, Intent intent) {

        int action = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR);
        Log.i(TAG,"action === " + action);
        switch (action){
            case BluetoothAdapter.STATE_OFF:
                Log.i(TAG,"手机蓝牙关闭!");
                Toast.makeText(context,"蓝牙开关已关闭，请打开蓝牙！",Toast.LENGTH_LONG).show();
                BleManager.getInstance().enableBluetooth();
                break;
            case BluetoothAdapter.STATE_ON:
                Log.i(TAG,"手机蓝牙开启!");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.i(TAG,"手机蓝牙正在关闭！");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.i(TAG,"手机蓝牙正在开启!");
                break;
        }
    }
}
