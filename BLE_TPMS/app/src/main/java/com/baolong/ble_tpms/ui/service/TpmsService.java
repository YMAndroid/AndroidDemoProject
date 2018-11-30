package com.baolong.ble_tpms.ui.service;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.ui.DeviceDetailActivity;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.MultipleBluetoothController;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class TpmsService extends Service {

    private static final String TAG = "TpmsService";
    private ArrayList<TripPressureDevice> tripPressureDevices;
    private BleDevice bleDevice;
    //用于存储不同设备的Gatt
    private ArrayMap<String, BluetoothGatt> getBleGatts = new ArrayMap<>();
    private Handler handler;
    private CallBack listener;
    private int type;
    private boolean isConnectDevice = false;
    private String tempMac = null;
    private LocalReceiver localReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
//        localReceiver = new LocalReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.android.tpmsmainActivity.connectDevice");
//        registerReceiver(localReceiver,filter);
        //BleManager.getInstance().getBluetoothGatt();
    }

    public interface CallBack {
        void onDataChanged(BleDevice bleDevice, int type);
        void onConnectFail(BleDevice bleDevice,int type);
    }

    public void setCallBack(CallBack listener) {
        this.listener = listener;
    }

    public void connectBleThread() {
        if(tripPressureDevices.size() > 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
                        if(BleManager.getInstance().isConnected(tripPressureDevice.getMacAddress())){
                            continue;
                        }
                        connectBleDevice(tripPressureDevice.getMacAddress(),tripPressureDevice.getTripType());
                    }
                }
            }, 1000);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                if(!BleManager.getInstance().isBlueEnable()){
////                    BleManager.getInstance().enableBluetooth();
////                }
//                    for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
//                        if(BleManager.getInstance().isConnected(tripPressureDevice.getMacAddress())){
//                            continue;
//                        }
//                        try {
//                            sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        connectBleDevice(tripPressureDevice.getMacAddress(),tripPressureDevice.getTripType());
//                    }
//                }
//            }).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        tripPressureDevices = new ArrayList<>();
        tripPressureDevices = (ArrayList<TripPressureDevice>) intent.getSerializableExtra("tripPreDevices");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public void setData(ArrayList<TripPressureDevice> data) {
            TpmsService.this.tripPressureDevices = data;
        }

        public TpmsService getMyService() {
            return TpmsService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void connectBleDevice(final String macAddress, final int type) {
        if (BleManager.getInstance().isConnected(macAddress)) {
            return;
        }

        final Intent intent = new Intent();
        intent.setAction("com.android.bleconnecting");
        BleManager.getInstance().connect(macAddress, new BleGattCallback() {
            @Override
            public void onStartConnect() {
//
                intent.putExtra("isConnecting",true);
                sendBroadcast(intent);
                Log.i(TAG, "开始连接Ble设备：" + macAddress);
                if (getBleGatts.size() > 0) {
                    if (getBleGatts.containsKey(macAddress)) {
                        getBleGatts.remove(macAddress);
                    }
                }
            }

            @Override
            public void onConnectFail(final BleDevice bleDevice, BleException exception) {
                Log.i(TAG, "连接Ble设备失败: " + bleDevice.getMac() + " ;异常信息 ：" + exception.getDescription());
                //connectBleDevice(bleDevice.getMac(),type);//重新连接

                intent.putExtra("isConnecting",false);
                sendBroadcast(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnectFail(bleDevice, type);
                    }
                }, 3000);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            sleep(3000);
//                            listener.onConnectFail(bleDevice, type);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.i(TAG, "连接Ble设备成功: " + bleDevice.getMac() + " ;连接状态 ：" + status);
                if (!getBleGatts.containsKey(bleDevice.getMac())) {
                    getBleGatts.put(bleDevice.getMac(), gatt);
                }
                if (listener != null) {
                    listener.onDataChanged(bleDevice, type);
                }

                if(isConnectDevice){
                    bleDeviceDataRead(bleDevice,type);
                }
//                intent.putExtra("isConnecting",true);
//                sendBroadcast(intent);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, final BleDevice device, BluetoothGatt gatt, int status) {
                Log.i(TAG, "Ble设备断开连接：" + device.getMac() + " ;连接状态为：" + status + " ;isActiveDisConnected = " + isActiveDisConnected);
                getBleGatts.remove(device.getMac());
                if (!isActiveDisConnected) {//非主动调用的disConnect
                    //try reconnect ---等待300ms后，尝试重新连接
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                sleep(3000);
                                int type = 0;
                                for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
                                    if (tripPressureDevice.getMacAddress().equalsIgnoreCase(device.getMac())) {
                                        type = tripPressureDevice.getTripType();
                                    }
                                }
                                connectBleDevice(device.getMac(), type);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }


    //内部类，实现BroadcastReceiver
    public class LocalReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("com.android.tpmsmainActivity.connectDevice")) {
                //暂停监听数据
                isConnectDevice = true;
                tempMac = intent.getStringExtra("connectDevice");
                for(final TripPressureDevice tripPressureDevice : tripPressureDevices){
                    if(tripPressureDevice.getMacAddress().equalsIgnoreCase(tempMac)){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sleep(3000);
                                    connectBleDevice(tempMac,tripPressureDevice.getTripType());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                Log.i(TAG, "接收到的需要连接的mac地址为" + tempMac);
            }
        }
    }

    private void bleDeviceDataRead(BleDevice ble, int type) {
        BleManager.getInstance().read(
                ble,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        // 读特征值数据成功
                        //Toast.makeText(mContext, "主动读取的数据成功:" + Arrays.toString(data), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "主动读取的数据成功:" + Arrays.toString(data));
                        String str = Utils.bytesToHexString(data);
                        String strTemp = str.substring(10,12);
                        if(strTemp.equalsIgnoreCase("00")){
                            Log.i(TAG,"升级成功!");
                        }
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        // 读特征值数据失败
                        //Toast.makeText(mContext, "读取数据失败", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG,"onRebind!");
        super.onRebind(intent);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }
}
