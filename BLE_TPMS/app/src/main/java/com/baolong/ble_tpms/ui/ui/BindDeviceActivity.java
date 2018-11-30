package com.baolong.ble_tpms.ui.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.TpmsMainActivity;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;

public class BindDeviceActivity extends BaseTitleActivity implements View.OnClickListener{
    private static final String TAG = "BindDeviceActivity";
    private Button manualMatch,autoMatch,scanQrCode;
    private boolean isMain = false;
    private TripPressureDeviceTable tripPressureDeviceTable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
    }

    @Override
    public void init() {
        Intent intent = getIntent();
        isMain = intent.getBooleanExtra("main",false);
        setTitleAndContentLayoutId(getResources().getString(R.string.bind_sendor), R.layout.activity_bind_device);
        manualMatch = findViewById(R.id.manual_match);
        autoMatch = findViewById(R.id.auto_match);
        scanQrCode = findViewById(R.id.scan_qr_code);
        manualMatch.setOnClickListener(this);
        autoMatch.setOnClickListener(this);
        scanQrCode.setOnClickListener(this);
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        //跳转到主界面
        intent = new Intent(BindDeviceActivity.this, TpmsMainActivity.class);
        switch (v.getId()){
            case R.id.manual_match:
                intent.putExtra(Config.MATCH_TYPE, Config.MANUAL_MATCH);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.auto_match:
                //跳转到主界面
                intent.putExtra(Config.MATCH_TYPE, Config.AUTO_MATCH);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.scan_qr_code:
                break;
        }
    }
}
