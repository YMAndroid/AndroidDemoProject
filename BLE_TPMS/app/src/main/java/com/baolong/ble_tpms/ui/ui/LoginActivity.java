package com.baolong.ble_tpms.ui.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.TpmsMainActivity;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.clj.fastble.BleManager;

import org.w3c.dom.Text;

public class LoginActivity extends Activity implements View.OnClickListener {
    private Button btnOwnerService, btnDistributorService;
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private Context mContext;
    private ImageView backImage;
    private TextView tvTitle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityManager.getInstance().addActivity(this);
        mContext = this;
        if (Build.VERSION.SDK_INT < 18) {
            Toast.makeText(mContext, "Only supports Android 4.3 and above!", Toast.LENGTH_LONG).show();
            return;
        }
        initBleDevice();
        primissionCheck();
        initView();
    }

    public void primissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(mContext, "need to open location permissions to search for BLE devices", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    /**
     * 初始化蓝牙设备
     */
    private void initBleDevice() {
        //初始化蓝牙
        BleManager.getInstance().init(getApplication());
        //BleManager.getInstance().enableBluetooth();
        //判断蓝牙是否打开
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        if (BleManager.getInstance().isSupportBle()) {
            if (!BleManager.getInstance().isBlueEnable()) {
                Log.d(TAG, "准备开启蓝牙!");
                BleManager.getInstance().enableBluetooth();
                Log.d(TAG, "蓝牙开启完成!");
            }
        } else {
            Toast.makeText(this, "This device does not support BLE!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initView() {
        backImage = findViewById(R.id.back_image);
        backImage.setOnClickListener(this);
        btnOwnerService = findViewById(R.id.btn_owner_service);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("Select Demo Type ");
        btnOwnerService.setOnClickListener(this);
        btnDistributorService = findViewById(R.id.btn_distributor_service);
        btnDistributorService.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.btn_owner_service:
                intent.setClass(mContext, TpmsMainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_distributor_service:
                intent.setClass(mContext,DistributorMainActivity.class);
                startActivity(intent);
                break;
            case R.id.back_image:
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
                if (Utils.isLocaltionEnable(mContext)) {//位置开关是打开的，可以开始扫描了
                    //Toast.makeText(mContext, "可以开始Ble设备扫描了", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "位置开关已经打开！");
                } else {//设置打开位置服务
                    setLocationService();
                }
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
                Toast.makeText(mContext, getResources().getString(R.string.location_permission_no_open), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (Utils.isLocaltionEnable(mContext)) {
                //定位已经打开,可以开启Ble设备扫描
                //Toast.makeText(mContext, "定位开关已打开，可以进行Ble设备扫描了", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "location switch is open!");
            } else {
                //定位未打开，请求打开位置
                Toast.makeText(mContext, getResources().getString(R.string.location_switch_no_open), Toast.LENGTH_LONG).show();
                Log.i(TAG, "location switch no open!");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BleManager.getInstance() != null){
            BleManager.getInstance().disableBluetooth();
        }
    }
}
