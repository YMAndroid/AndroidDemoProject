package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.CarDeviceTripDataBean;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.CarDeviceTripDataTable;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.baolong.ble_tpms.ui.utils.BleCmdUtils;
import com.baolong.ble_tpms.ui.utils.CRC16Util;
import com.baolong.ble_tpms.ui.utils.CRC8Util;
import com.baolong.ble_tpms.ui.utils.DataTransformUtils;
import com.baolong.ble_tpms.ui.utils.SharedPreferencesHelper;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.baolong.ble_tpms.ui.view.SendDataDialog;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.baolong.ble_tpms.ui.utils.Utils.convertProgress;
import static com.baolong.ble_tpms.ui.utils.Utils.upgradeStatusToString;
import static java.lang.Thread.sleep;

public class DeviceDetailActivity extends BaseTitleActivity {
    private static final String TAG = "DeviceDetailActivity";
    private TextView deviceName, tymeName, pressureValue, tripTempValue, upgradeStatus;
    private RadioGroup rgStatisticalMethods;
    private RadioButton rbByMonth, rbByWeek, rbByDay;
    private TripPressureDevice tp = null;
    private CarDeviceTripDataTable carDeviceTripDataTable;
    private List<CarDeviceTripDataBean> carDeviceTripDataBeanList = new ArrayList<>();
    private InputStream inputStream;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private byte[] bytes;
    private BleDevice bleDevice;
    private int mProgress = 0;//下载进度
    private SendDataDialog dialog = null;
    private MyThread myThread;
    private String strSendTypeName;
    private List<BleDevice> bleDeviceList;
    public static final String ACTION = "com.baolong.ble_tpms.ui.ui.DeviceDetailActivity";
    private boolean isConnectDevice = false;
    private String tempMac = null;
    private LocalReceiver localReceiver;
    private int type = 0;
    private boolean isSecondConnect = false;
    private static final int UPDATE_UI_MSG = 1;

    @Override
    public void init() {
        mContext = this;
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId("device detail", R.layout.activity_device_detail);
        mSharedPreferencesHelper = new SharedPreferencesHelper(mContext, mContext.getPackageName());
        initView();
        initData();
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.tpmsmainActivity.connectDevice");
        registerReceiver(localReceiver, filter);

    }

    private void initView() {
        deviceName = findViewById(R.id.tv_trip_device_name);
        tymeName = findViewById(R.id.tv_trip_type_name);
        pressureValue = findViewById(R.id.tv_tire_pressure_value);
        tripTempValue = findViewById(R.id.tv_trip_temp_value);
        upgradeStatus = findViewById(R.id.tv_upgrade_status);
        rgStatisticalMethods = findViewById(R.id.rg_statistical_methods);
        rbByMonth = findViewById(R.id.rb_by_month);
        rbByWeek = findViewById(R.id.rb_by_week);
        rbByDay = findViewById(R.id.rb_by_day);
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        tp = (TripPressureDevice) bundle.getSerializable("tripPressureDevice");
        //获取所有连接的Ble设备
        bleDeviceList = new ArrayList<>();
        bleDeviceList.clear();
        bleDeviceList.addAll(BleManager.getInstance().getAllConnectedDevice());
        if (bleDeviceList.size() > 0) {
            for (BleDevice ble : bleDeviceList) {
                if (ble.getMac().equalsIgnoreCase(tp.getMacAddress())) {
                    bleDevice = ble;
                }
            }
        }
        deviceName.setText("device name: " + tp.getDeviceName() + "(" + tp.getMacAddress() +")");
        tymeName.setText("tire position: " + Utils.tripType(tp.getTripType()));
        upgradeStatus.setText("device status: " + Utils.upgradeStatusToString(tp.getIsUpgrade()));
        //查询设备数据表
        queryDeviceData();
        if (carDeviceTripDataBeanList.size() > 0) {
            tripTempValue.setText("tire temperature: " + String.valueOf(carDeviceTripDataBeanList.get(0).getTemperature()) + Config.CELSIUS_UNIT);
            pressureValue.setText("tire pressure: " + String.valueOf((int)carDeviceTripDataBeanList.get(0).getPressure()) + Config.KPA);
        }
        tvRightTitle.setVisibility(View.VISIBLE);
        tvRightTitle.setText("upgrade");
        tvRightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCheckUpgradeType();
            }
        });
    }

    private void dialogCheckUpgradeType() {
        if (!BleManager.getInstance().isConnected(tp.getMacAddress())) {
            Log.i(TAG, "升级的设备MAC地址是: " + tp.getMacAddress());
            Toast.makeText(DeviceDetailActivity.this, "this device is not connect!", Toast.LENGTH_LONG).show();
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
        builder.setTitle("please choose the upgrade method");
        final String[] items = {"3049 upgrade", "3011 upgrade"};
        //    设置一个单项选择下拉框
        /**
         * 第一个参数指定我们要显示的一组下拉单选框的数据集合
         * checkedItem  指定默认哪一个单选框被勾选上，-1 表示默认不选中 选中数组第一个为0  选中数组第二个为1 选中数组第三个为2 ...
         * 第三个参数给每一个单选项绑定一个监听器
         */
        final int[] checkUpgradeType = {-1};
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "选择的升级方式是 " + items[which]);
                checkUpgradeType[0] = which;
            }
        });
        //
        builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //发送广播--停止获取数据跟新
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.putExtra("isPauseMonitor", true);
                intent.setAction(ACTION);
                sendBroadcast(intent);

                Log.i(TAG, "which = " + which);

                if (checkUpgradeType[0] == 0) {
                    type = Config.UPGRADED_3049_HEX;
                    strSendTypeName = "3049";
                } else if (checkUpgradeType[0] == 1) {
                    type = Config.UPGRADED_3011_HEX;
                    strSendTypeName = "3011";
                } else {
                    Toast.makeText(mContext, "please choose the upgrade method !", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    bytes = DataTransformUtils.sendData(mContext, type);
                    bleDeviceHexDataWrite(Config.SEND_DATA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //    定期发送心跳包数据
    private void bleDeviceHexDataWrite(final int type) {
        //sendDataStatus = true;
        showDialog();
        //byteData(test);
        BleManager.getInstance().write(
                bleDevice,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                bytes,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        Log.i(TAG, "写数据成功,返回的数据 justWrite = " + Arrays.toString(justWrite) + " ;current size = " + current + " ;total = " + total);
                        if (type == Config.SEND_DATA) {
                            //返回发送成功的数据后，跟新进度条，实时显示发送的状态
                            mProgress = (int) ((double) current / total * 100);
                            Message msg = mySendDataHandler.obtainMessage();
                            msg.what = 100;
                            msg.obj = mProgress;
                            mySendDataHandler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i(TAG, "写数据失败 exception = " + exception.toString());
                        //解析返回的数据
                        if (exception.getCode() == Config.BLE_ERROR_CODE_102) {
                            Toast.makeText(DeviceDetailActivity.this, exception.getDescription(), Toast.LENGTH_LONG).show();
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void showDialog() {
        dialog = new SendDataDialog(this);
        dialog.setTextView("sending " + strSendTypeName + " upgrade package, please wait.");
        //正在发送升级包，不允许关闭dialog
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    Toast.makeText(mContext, "sending " + strSendTypeName + " upgrade package, please wait.", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
        dialog.show();
    }

    private void queryDeviceData() {
        carDeviceTripDataTable = new CarDeviceTripDataTable();
        carDeviceTripDataBeanList = carDeviceTripDataTable.queryDataByDeviceId(tp.getId(), true);
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    Handler mySendDataHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    int progress = (int) msg.obj;
                    dialog.setProgress(progress);
                    if (100 == progress) {
                        dialog.dismiss();
                        Toast.makeText(mContext, "upgrade package is sent", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

//    private void sendDownLoad(int type) {
//        //String strGetPtv = "AABB000600000000000000000000000000000000";
//        String sendDownload = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_DOWNLOAD + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
//        sendDownload = DataTransformUtils.AppendPrefix(40, sendDownload, "0");
//        sendDownload = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(sendDownload)), sendDownload);
//        Log.i(TAG, "发送download命令：" + sendDownload);
//        bleDeviceDataWrite(sendDownload, type);
//    }

    private void bleDeviceWrite(String str, final int type) {
        BleManager.getInstance().write(
                bleDevice,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                DataTransformUtils.hex2byte(str),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        //通过写成功的数据计算CRC8 ，写成功的数据CRC位置为00
                        //Toast.makeText(mContext, "send download cmd success " + DataTransformUtils.byte2hex(justWrite), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "写数据成功 " + DataTransformUtils.byte2hex(justWrite) + "; current = " + current + " ;total= " + total);
                        //myThread = new MyThread();
                        //myThread.start();
                        //取消暂停监控数据
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i(TAG, "发送命令数据失败,异常为 ： " + exception.toString());
                        // 发送数据到设备失败
                    }
                });
    }

    public class MyThread extends Thread {
        //继承Thread类，并改写其run方法
        private final static String TAG = "My Thread ===> ";
        Handler handler;
        int upgradeType;
        MyThread(Handler han,int type){
            handler = han;
            upgradeType = type;
        }

        public void run() {
            Message message = Message.obtain();
            message.what = UPDATE_UI_MSG;
            Bundle bundle = new Bundle();
            bundle.putString("upgradeType",Utils.upgradeStatusToString(upgradeType));
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    //连接设备
    private void connectBle(final String tempMac) {
        BleManager.getInstance().connect(tempMac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.i(TAG, "start reconnect device " + tempMac);
                Toast.makeText(DeviceDetailActivity.this, "reconnect device!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Toast.makeText(DeviceDetailActivity.this, "reconnect device fail ",Toast.LENGTH_LONG).show();
                Log.i(TAG, "start reconnect device  " +  bleDevice.getMac() + "fail, exception: " +  exception.getDescription());
                connectBle(tempMac);
                isSecondConnect = true;
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Toast.makeText(DeviceDetailActivity.this, "device reconnect success!", Toast.LENGTH_LONG).show();
                Log.i(TAG,"开始重新连接连接设备成功,mac地址为: " + bleDevice.getMac());
                if(!isSecondConnect){
                    bleDeviceDataRead(bleDevice);
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.i(TAG,"设备断开连接，isActiveDisConnected = " + isActiveDisConnected + " ; mac = " + device.getMac());
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void bleDeviceDataRead(final BleDevice ble) {
        BleManager.getInstance().read(
                ble,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        // 读特征值数据成功
                        //Toast.makeText(mContext, "主动读取的数据成功:" + Arrays.toString(data), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "主动读取的数据成功:" + DataTransformUtils.byte2hex(data));
                        String str = Utils.bytesToHexString(data);
                        String strTemp = str.substring(10, 12);
                        if (strTemp.equalsIgnoreCase("00")) {
                            Log.i(TAG, "升级成功!");
                            Toast.makeText(DeviceDetailActivity.this, "update successed.", Toast.LENGTH_SHORT).show();
                            //更新升级状态表
                            TripPressureDeviceTable tripPressureDeviceTable = new TripPressureDeviceTable();
                            if (tp.getMacAddress().equalsIgnoreCase(ble.getMac())) {
                                tripPressureDeviceTable.updateUpgradeStatus(tp.getId(), type);
                            }
                        }
                        //升级成功后，开启监听
                        Intent intent = new Intent();
                        intent.putExtra("isPauseMonitor", false);
                        intent.setAction(ACTION);
                        sendBroadcast(intent);

                        //跟新界面设备的升级状态
                        myThread = new MyThread(mHandler,type);
                        myThread.start();
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        // 读特征值数据失败
                        //Toast.makeText(mContext, "读取数据失败", Toast.LENGTH_LONG).show();
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
                String cmdType = intent.getStringExtra("cmdType");
                String strData = intent.getStringExtra("notifyData");
                if(cmdType.equalsIgnoreCase(Config.CMD_1_SEND_FILE)){
                    //choise data crc16 是否正确
                    String crc16 = strData.substring(12,16);
                    try {
                        byte[] byteTemps = DataTransformUtils.inputStreamToByte(Utils.getInputStream(type,context));
                        if(crc16.equalsIgnoreCase(CRC16Util.GetCRC(byteTemps))){
                            //send file success
                            Toast.makeText(DeviceDetailActivity.this,"send upgrade file success!",Toast.LENGTH_LONG).show();
                            //发送download指令
                            bleDeviceWrite(BleCmdUtils.getDownLoadCmd(), 0);
                        } else{
                            Toast.makeText(DeviceDetailActivity.this,"send upgrade file fail, please resend！",Toast.LENGTH_LONG).show();
                            if(dialog != null){
                                dialog.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else{
                    //Log.i(TAG, "接收到的需要连接的mac地址为" + tempMac);
                    //暂停监听数据
                    isConnectDevice = true;
                    //tempMac = intent.getStringExtra("connectDevice");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectBle(bleDevice.getMac());
                        }
                    },3500);
                }
            }
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI_MSG:
                    upgradeStatus.setText("device status: " + msg.getData().getString("upgradeType"));
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localReceiver);
        if(dialog != null){
            dialog.dismiss();
        }
    }
}
