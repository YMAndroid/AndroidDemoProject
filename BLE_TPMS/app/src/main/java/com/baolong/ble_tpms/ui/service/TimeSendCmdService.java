package com.baolong.ble_tpms.ui.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baolong.ble_tpms.ui.TpmsMainActivity;
import com.baolong.ble_tpms.ui.broadcast.AlarmReceiver;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.ui.DeviceDetailActivity;
import com.baolong.ble_tpms.ui.utils.BleCmdUtils;
import com.baolong.ble_tpms.ui.utils.DataTransformUtils;
import com.baolong.ble_tpms.ui.utils.SharedPreferencesHelper;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.ArrayList;
import java.util.List;

public class TimeSendCmdService extends Service {

    public static final String ACTION = "com.baolong.ble_tpms.ui.service.TimeSendCmdService";
    private static final String TAG = "TimeSendCmdService";
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private byte[] bytes = null;
    private boolean isPauseMoniterData = false;
    private LocalReceiver localReceiver;
    private boolean isPause = false;
    private Thread thread;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private boolean isConnecting = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceDetailActivity.ACTION);
        filter.addAction("com.android.bleconnecting");
        registerReceiver(localReceiver, filter);
        mSharedPreferencesHelper = new SharedPreferencesHelper(this, this.getPackageName());
//        my = new MyThread();
//        thread = new Thread(my);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        bleDeviceList = BleManager.getInstance().getAllConnectedDevice();
        setAlarmManager();
        if(bleDeviceList != null && bleDeviceList.size() > 0){
            if(!isPauseMoniterData && !isConnecting){
                Log.i(TAG,"开启监听");
                bytes = DataTransformUtils.hex2byte(BleCmdUtils.getPtvCmd());
                //thread.start();
                //new PollingThread().start();
                if(bleDeviceList.size() > 0){
                    for (BleDevice bleDevice : bleDeviceList){
                        if (BleManager.getInstance().isConnected(bleDevice)){
                            writeDataToBle(bleDevice,bytes);
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

//    class PollingThread extends Thread {
//
//        @Override
//        public void run() {
//            if(bleDeviceList.size() > 0){
//                for (BleDevice bleDevice : bleDeviceList){
//                    if (!BleManager.getInstance().isConnected(bleDevice)){
//                        return;
//                    }
//                    try {
//                        writeDataToBle(bleDevice,bytes);
//                        sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    private AlarmManager alarmManager = null;
    private PendingIntent pendingIntent = null;
    public void setAlarmManager(){
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = (int)mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_CURRENT_SELECT_PREF,Config.NUMBER_PICKER_DEFAULT_VALUE);
        Log.i(TAG,"开启监听的时间间隔 time = " + time);
        long triggerAtTime = SystemClock.elapsedRealtime() + (time * 1000);
        Intent i = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
    }

    public void cancelAlarmManager(){
        //用于取消的
        Intent intent = new Intent(this, AlarmReceiver.class);
        // 创建PendingIntent对象
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        unregisterReceiver(localReceiver);
        if(alarmManager != null && pendingIntent != null){
            Log.i(TAG,"cancel alarmManager");
            alarmManager.cancel(pendingIntent);
        }
        System.out.println("Service:onDestroy");
        super.onDestroy();
    }

    public void writeDataToBle(final BleDevice bleDevice, byte[] bytes){
        Log.i(TAG,"writeDataToBle");
        //my.pauseThread();
        BleManager.getInstance().write(bleDevice, Config.SERVICE_UUID, Config.CHARACTER_UUID_NOTIFY, bytes, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                //发送广播
                Log.i(TAG,"onWriteSuccess bledevice = " + bleDevice.getMac());
//                Intent intent = new Intent();
//                intent.putExtra("mac", bleDevice.getMac());
//                intent.setAction(TpmsMainActivity.action);
//                sendBroadcast(intent);
                //my.resumeThread();
            }

            @Override
            public void onWriteFailure(BleException exception) {
                Log.i(TAG,"onWriteFailure exception = " + exception.getDescription());
            }
        });
    }

    //内部类，实现BroadcastReceiver
    public class LocalReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals(DeviceDetailActivity.ACTION)) {
                //暂停监听数据
                isPauseMoniterData = intent.getBooleanExtra("isPauseMonitor",false);
                Log.i(TAG, "接收到发送方升级包的广播  = " + isPauseMoniterData);
            } else if(intentAction.equals("com.android.bleconnecting")){
                isConnecting = intent.getBooleanExtra("isConnecting",false);
                Log.i(TAG,"接收到正在连接设备广播 = " + isConnecting);
            }
        }
    }
}
