package com.baolong.ble_tpms.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baolong.ble_tpms.ui.service.TimeSendCmdService;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive intent = " + intent.getAction());
        Intent intent1 = new Intent(context, TimeSendCmdService.class);
        context.startService(intent1);
    }
}
