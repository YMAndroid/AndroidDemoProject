package com.baolong.ble_tpms.ui.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.adapter.DeviceAdapter;
import com.baolong.ble_tpms.ui.adapter.ObserverManager;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.baolong.ble_tpms.ui.utils.DataTransformUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.List;

public class BleDeviceActivity extends BaseTitleActivity implements View.OnClickListener {
    private static final String TAG = "BleDeviceActivity";
    private ListView bleListView;
    private DeviceAdapter mDeviceAdapter;
    private Button btnScan;
    private Button btnStopScan;
    private ImageView img_loading;
    private Animation operatingAnim;
    private ProgressDialog progressDialog;
    private int tripType = 0,recodeId = 0;
    private String carName;
    private TripPressureDeviceTable tripPressureDeviceTable;
    private BleDevice mbleDevice = null;
    private int deviceId;
    private boolean isConnectDevice = false;
    private boolean isScanDevice = false;
    private String titleName;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);

        tripType = intent.getIntExtra("tirePosition",0);
        recodeId = intent.getIntExtra("carRecodeId",0);
        carName = intent.getStringExtra("carName");
        titleName = intent.getStringExtra("tripTypeName");
    }

    @Override
    public void init() {
        intent = getIntent();
        titleName = intent.getStringExtra("tripTypeName");
        setTitleAndContentLayoutId(titleName, R.layout.activity_scanble_list);//getResources().getString(R.string.please_choise_bind_device)
        bleListView = findViewById(R.id.list_device);
        btnScan = findViewById(R.id.btn_scan);
        //btnScan.setText(R.string.start_scan);
        tvRightTitle.setVisibility(View.VISIBLE);
        tvRightTitle.setText("OK");
        tvRightTitle.setOnClickListener(this);
        btnScan.setText(getString(R.string.start_scan));
        btnScan.setOnClickListener(this);
        img_loading = findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        mDeviceAdapter = new DeviceAdapter(BleDeviceActivity.this);
        progressDialog = new ProgressDialog(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
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
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {

            }
        });
        bleListView.setAdapter(mDeviceAdapter);
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if(BleManager.getInstance().isBlueEnable()){
                    if (btnScan.getText().equals(getString(R.string.start_scan))) {

                        startScan();
                    } else if (btnScan.getText().equals(getString(R.string.stop_scan))) {
                        BleManager.getInstance().cancelScan();
                    }
                } else{
                    Toast.makeText(BleDeviceActivity.this,"bluetooth switch not open!",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_right_title:
                Intent intent = new Intent();
                intent.putExtra("type",tripType);
                intent.putExtra("bleDevice",mbleDevice);
                intent.putExtra("deviceId",deviceId);
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }

    private void startScan() {
        isScanDevice = true;
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btnScan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                isScanDevice = false;
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btnScan.setText(getString(R.string.start_scan));
            }
        });
    }

    private String sensorId = "SENSOR ID:";
    private void connect(final BleDevice bleDevice) {

        final Intent intent = new Intent();
        intent.setAction("com.android.bleconnecting");
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                //progressDialog.setTitle("connecting " + sensorId + "UNKNOW, " +"BLE device: " + bleDevice.getMac());
                progressDialog.setTitle("connecting BLE device: " + bleDevice.getMac());
                progressDialog.show();
                intent.putExtra("isConnecting",true);
                sendBroadcast(intent);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btnScan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                intent.putExtra("isConnecting",false);
                sendBroadcast(intent);
                Toast.makeText(BleDeviceActivity.this, getString(R.string.connect_fail) + ", please reconnect", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                //数据保存到数据库
                tripPressureDeviceTable = new TripPressureDeviceTable();
                insertTripPreDevice(contractTripDeviceObj(bleDevice));
                mbleDevice = bleDevice;
                isConnectDevice = true;
                Toast.makeText(BleDeviceActivity.this,"CLICK OK TO UPDATE",Toast.LENGTH_LONG).show();
//                intent.putExtra("isConnecting",false);
//                sendBroadcast(intent);
                //openDataNotify();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    //Toast.makeText(BleDeviceActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                    Log.i(TAG,"Bluetooh initiative disconnect!");
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                } else {
                    Log.i(TAG,"Bluetooh passive disconnect!, sendBroad to connect Ble Device!");
                    Intent intent = new Intent();
                    intent.setAction("com.android.bledeviceactivity.bledisconnect");
                    Log.i(TAG,"send broad cast com.android.bledeviceactivity.bledisconnect");
                    sendBroadcast(intent);
//                    connect(bleDevice);
                    Toast.makeText(BleDeviceActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void insertTripPreDevice(TripPressureDevice tripPressureDevice) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TripPressureDeviceTable.carName, tripPressureDevice.getCarName());
        contentValues.put(TripPressureDeviceTable.carRecordId, tripPressureDevice.getCarRecodeId());
        contentValues.put(TripPressureDeviceTable.deviceName, tripPressureDevice.getDeviceName());
        contentValues.put(TripPressureDeviceTable.macAddress, tripPressureDevice.getMacAddress());
        contentValues.put(TripPressureDeviceTable.pairStatus, tripPressureDevice.getPairStatus());
        contentValues.put(TripPressureDeviceTable.isUpgrade, tripPressureDevice.getIsUpgrade());
        contentValues.put(TripPressureDeviceTable.tripType, tripPressureDevice.getTripType());
        contentValues.put(TripPressureDeviceTable.tripDataId, tripPressureDevice.getTripDataId());
        contentValues.put(TripPressureDeviceTable.bindStatus, tripPressureDevice.getBindStatus());
        deviceId = (int) tripPressureDeviceTable.insert(contentValues);
        if(deviceId == 0){
            Log.i(TAG,"insertTripPreDevice failed reason : " + "该数据已经存在!");
        } else {
            Log.i(TAG,"insertTripPreDevice id = " + deviceId);
        }
    }

    public TripPressureDevice contractTripDeviceObj(BleDevice bleDevice){
        //构建设备对象
        TripPressureDevice tripPressureDevice = new TripPressureDevice();
        tripPressureDevice.setPairStatus(Config.PAIRED);
        tripPressureDevice.setMacAddress(bleDevice.getMac());
        tripPressureDevice.setDeviceName(Config.DEVICE_NAME);
        tripPressureDevice.setCarName(carName);
        tripPressureDevice.setIsUpgrade(Config.NOT_UPGRADED);
        tripPressureDevice.setCarRecodeId(recodeId);
        tripPressureDevice.setBindStatus(Config.BIND_VALUE);
        //tripPressureDevice.setTripDataId();
        tripPressureDevice.setTripType(tripType);//左前轮--需要修改
//                insertTripPreDevice(tripPressureDevice);
        return tripPressureDevice;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if(BleManager.getInstance() != null && isScanDevice){
            BleManager.getInstance().cancelScan();
        }
        if(progressDialog != null){
            if(progressDialog.isShowing()){
                progressDialog.cancel();
                progressDialog = null;
            }
        }
    }
}
