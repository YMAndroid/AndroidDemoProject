package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.TpmsMainActivity;
import com.baolong.ble_tpms.ui.adapter.DeviceAdapter;
import com.baolong.ble_tpms.ui.adapter.DistributorServiceAdapter;
import com.baolong.ble_tpms.ui.adapter.ObserverManager;
import com.baolong.ble_tpms.ui.bean.LocalBleDevice;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.baolong.ble_tpms.ui.utils.BleCmdUtils;
import com.baolong.ble_tpms.ui.utils.BleUtils;
import com.baolong.ble_tpms.ui.utils.DataTransformUtils;
import com.baolong.ble_tpms.ui.utils.DecodeBleData;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.baolong.ble_tpms.ui.view.SendDataDialog;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistributorMainActivity extends BaseTitleActivity implements View.OnClickListener {
    private Button btnStartScan;
    private ImageView img_loading;
    private Animation operatingAnim;
    private ProgressDialog progressDialog;
    private ListView listView;
    private DistributorServiceAdapter deviceAdapter;
    private static final String TAG = "DistributorMainActivity";
    private SendDataDialog dialog = null;
    private int mProgress = 0;//下载进度
    private boolean sendUpdateDataStatus = false;

    private int type = -1;
    private String strSendTypeName;
    private boolean isUpgradeConnect = false;
    private boolean test = false;
    private List<BleDevice> connectDevices = new ArrayList<>();
    private boolean isConnectDevice = false;

    @Override
    public void init() {
        initScanConfig();
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId(getResources().getString(R.string.distributor_service_main), R.layout.activity_distributor_service);
        initView();
        initData();
    }

    private void initData() {
        deviceAdapter = new DistributorServiceAdapter(mContext);
        listView.setAdapter(deviceAdapter);
        progressDialog = new ProgressDialog(this);
        deviceAdapter.setOnDeviceClickListener(new DistributorServiceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Log.i(TAG, "onDisConnect!!!");
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void updateDevice(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    dialogCheckUpgradeType(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {

            }
        });
    }

    private void initView() {
        btnStartScan = findViewById(R.id.btn_scan);
        btnStartScan.setOnClickListener(this);
        listView = findViewById(R.id.list_device);
        img_loading = findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
    }

    public void initScanConfig() {
        //初始化扫描设备
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setServiceUuids(new UUID[]{Config.SERVICE_UUID})  //只扫描制定的服务设备
                .setDeviceName(true, Config.DEVICE_NAME)//只扫描指定广播名的设备，可选
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (BleManager.getInstance().isBlueEnable()) {
                    if (btnStartScan.getText().equals("scan")) {
                        startScan();
                    } else if (btnStartScan.getText().equals(getString(R.string.stop_scan))) {
                        BleManager.getInstance().cancelScan();
                    }
                } else {
                    Toast.makeText(DistributorMainActivity.this, "bluetooth switch not open!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void sendUpdateLiestMessage() {
        Message msg = mySendDataHandler.obtainMessage();
        msg.what = 101;
        msg.obj = mProgress;
        mySendDataHandler.sendMessage(msg);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                deviceAdapter.clearScanDevice();
                deviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btnStartScan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                LocalBleDevice localBleDevice = new LocalBleDevice();
                localBleDevice.setBleDevice(bleDevice);
                deviceAdapter.addDevice(localBleDevice);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btnStartScan.setText("scan");
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        connectDevices = BleManager.getInstance().getAllConnectedDevice();
        if (connectDevices.size() >= 7) {
            Toast.makeText(mContext, "Maximum number of connections is 7, Please disconnect unused devices!", Toast.LENGTH_LONG).show();
            return;
        }
        isConnectDevice = true;
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                //progressDialog.setTitle("connecting SENSOR ID:" + "UNKNOWN" + " ,BLE device: " + bleDevice.getMac());
                progressDialog.setTitle("connecting BLE device: " + bleDevice.getMac());
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btnStartScan.setText("scan");
                progressDialog.dismiss();
                isConnectDevice = false;
                Toast.makeText(DistributorMainActivity.this, getString(R.string.connect_fail) + ", please reconnect", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                LocalBleDevice localBleDevice = new LocalBleDevice();
                Toast.makeText(mContext, "Connection succeeded!", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                localBleDevice.setBleDevice(bleDevice);
                deviceAdapter.addDevice(localBleDevice);
                sendUpdateLiestMessage();
                openDataNotify(bleDevice);
                isConnectDevice = false;
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, final BleDevice bleDevice, BluetoothGatt gatt, int status) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.i(TAG, "disconnected!");
                if (isActiveDisConnected) {
                    Toast.makeText(mContext, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Bluetooh initiative disconnect!");
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                    LocalBleDevice localBleDevice = new LocalBleDevice();
                    localBleDevice.setBleDevice(bleDevice);
                    deviceAdapter.removeDevice(localBleDevice);
                    sendUpdateLiestMessage();
                } else {
                    Log.i(TAG, "Bluetooh passive disconnect!, sendBroad to connect Ble Device!");
                    //Intent intent = new Intent();
                    //intent.setAction("com.android.bledeviceactivity.bledisconnect");
                    //Log.i(TAG, "send broad cast com.android.bledeviceactivity.bledisconnect");
                    //sendBroadcast(intent);
//                    connect(bleDevice);
                    if (sendUpdateDataStatus) {
                        //Toast.makeText(mContext, "Send Upgrade package failed!", Toast.LENGTH_LONG).show();
                        //判断是否是发送升级包的设备断开连接了,如果是，则提示发送升级包失败，尝试重新连接设备
                        if (currentSendUpdatePackgeBle != null) {
                            if (bleDevice.getMac().equalsIgnoreCase(currentSendUpdatePackgeBle.getMac())) {
                                Toast.makeText(mContext, "BLE device is disconnect ,Send Upgrade package failed, try reconnect BLE device!", Toast.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect(bleDevice);
                                    }
                                }, 200);
                            }
                        }
                        Log.i(TAG, "end Upgrade package failed!");
                        sendUpdateDataStatus = false;
                    } else {
                        Toast.makeText(DistributorMainActivity.this, "device " + bleDevice.getMac() + " is disconnected", Toast.LENGTH_LONG).show();
                        LocalBleDevice localBleDevice = new LocalBleDevice();
                        localBleDevice.setBleDevice(bleDevice);
                        deviceAdapter.removeDevice(localBleDevice);
                        sendUpdateLiestMessage();
                    }
                }
            }
        });
    }

    private void openDataNotify(final BleDevice ble) {
        BleManager.getInstance().notify(ble, Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY, new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.i(TAG, "open notify success, mac: " + ble.getMac());
                        //發送獲取UUID指令
                        //bleDeviceHexDataWrite(ble,Config.SEND_CMD,DataTransformUtils.hex2byte(BleCmdUtils.getPtvCmd()));
                        if (!isUpgradeConnect) {
                            bleDeviceHexDataWrite(ble, Config.SEND_CMD_UUID, DataTransformUtils.hex2byte(BleCmdUtils.getUUIDCmd()));
                        }
                        if (isUpgradeConnect) {
                            //test = true;
                            //主動讀取取數
                            bleDeviceDataRead(ble);
                            isUpgradeConnect = false;
                        }
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.i(TAG, "open notify failed!");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        Log.i(TAG, "receive notify data: " + DataTransformUtils.byte2hex(data));
                        //解析
                        ParseData(data, ble);
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
                        Log.i(TAG, "read data success :" + DataTransformUtils.byte2hex(data));
                        String str = Utils.bytesToHexString(data);
                        String strTemp = str.substring(10, 12);
                        if (strTemp.equalsIgnoreCase("00")) {
                            Log.i(TAG, "升级成功!");
                            Toast.makeText(mContext, "update successed.", Toast.LENGTH_SHORT).show();
                        }
                        //獲取UUID
                        bleDeviceHexDataWrite(ble, Config.SEND_CMD_UUID, DataTransformUtils.hex2byte(BleCmdUtils.getUUIDCmd()));
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        Log.i(TAG, "read data failed : exception = " + exception.getDescription());
                        // 读特征值数据失败
                        //Toast.makeText(mContext, "读取数据失败", Toast.LENGTH_LONG).show();
                    }
                });
    }

    //解析返回的数据
    public void ParseData(byte[] data, final BleDevice bleDevice) {
        String strHexData = DataTransformUtils.byte2hex(data);
        Log.i(TAG, "notify data :" + strHexData);
        if (Utils.judgeDataCheckDigit(data)) {
            if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_GET_PTV)) {
            } else if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_DOWNLOAD)) {
                Log.i(TAG, "decode download cmd notify data!");
                //判断返回的数据是否成功
                //发送数据给升级设备界面
                int[] tempDatas = DataTransformUtils.stringIntercept(Config.CMD_1_DOWNLOAD, strHexData);
                if (tempDatas[0] == 0) {
                    //成功
                    Log.i(TAG, "donwload指令发送成功!");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isUpgradeConnect = true;
                            connect(bleDevice);
                        }
                    }, 3500);
                }
            } else if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_SEND_FILE)) {
                Log.i(TAG, " decode send file cmd get notify data!");
                //判斷數據是否發送陳宮
                //發送Download指令
                bleDeviceHexDataWrite(bleDevice, Config.SEND_CMD_DOWNLOAD, DataTransformUtils.hex2byte(BleCmdUtils.getDownLoadCmd()));

            } else if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_GET_UUID)) {
                Log.i(TAG, " decode get uuid cmd get notify data!");
                //UUID
                String str = null;
                str = DecodeBleData.DecodeUUIDData(strHexData);
//                if(true ){
//                    str = "3049";
//                } else {
//
//                }

                LocalBleDevice localBleDevice = new LocalBleDevice();
                localBleDevice.setBleDevice(bleDevice);
                localBleDevice.setDevideVersion(str);
                deviceAdapter.addDevice(localBleDevice);
                sendUpdateLiestMessage();
            }
        } else {
            //数据校验失败
            Log.i(TAG, "crc8 数据校验失败!");
        }
    }

    private void dialogCheckUpgradeType(final BleDevice bleDevice) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            Log.i(TAG, "升级的设备MAC地址是: " + bleDevice.getMac());
            Toast.makeText(mContext, "this device is not connect!", Toast.LENGTH_LONG).show();
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
//                Intent intent = new Intent();
//                intent.putExtra("isPauseMonitor", true);
//                intent.setAction(ACTION);
//                sendBroadcast(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                    byte[] bytes = DataTransformUtils.sendData(mContext, type);
                    bleDeviceHexDataWrite(bleDevice, Config.SEND_DATA, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.show();
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

    private BleDevice currentSendUpdatePackgeBle = null;

    private void bleDeviceHexDataWrite(BleDevice bleDevice, final int type, byte[] bytes) {
        //sendDataStatus = true;
        if (type == Config.SEND_DATA) {
            sendUpdateDataStatus = true;
            currentSendUpdatePackgeBle = bleDevice;
            showDialog();
        }
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
                        Log.i(TAG, "WriteSuccess:   " + DataTransformUtils.byte2hex(justWrite) + " ;current size = " + current + " ;total = " + total);
                        if (type == Config.SEND_DATA) {
                            Log.i(TAG, "SEND update data!");
                            //返回发送成功的数据后，跟新进度条，实时显示发送的状态
                            mProgress = (int) ((double) current / total * 100);
                            Message msg = mySendDataHandler.obtainMessage();
                            msg.what = 100;
                            msg.obj = mProgress;
                            mySendDataHandler.sendMessage(msg);
                        } else if (type == Config.SEND_CMD_UUID) {
                            Log.i(TAG, "send get uuid cmd success!");
                            //if is getUUID update 界面
                        } else if (type == Config.SEND_CMD_DOWNLOAD) {
                            Log.i(TAG, "send download cmd success!");

                        }
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i(TAG, "write failed exception = " + exception.toString());
                        //解析返回的数据
                        if (exception.getCode() == Config.BLE_ERROR_CODE_102) {
                            Toast.makeText(mContext, "send data failed!", Toast.LENGTH_LONG).show();
                        }
//                        if(type == Config.SEND_DATA){
//                            sendUpdateDataStatus = false;
//                        }
                        if (sendUpdateDataStatus) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }
                });
    }

    Handler mySendDataHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    int progress = (int) msg.obj;
                    dialog.setProgress(progress);
                    if (100 == progress) {
                        dialog.dismiss();
                        sendUpdateDataStatus = false;
                        Toast.makeText(mContext, "upgrade package is sent", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 101:
                    deviceAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
//        if(isConnectDevice){
//            BleManager.getInstance().setMaxConnectCount()
//        }
        BleManager.getInstance().disconnectAllDevice();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "opPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }
}
