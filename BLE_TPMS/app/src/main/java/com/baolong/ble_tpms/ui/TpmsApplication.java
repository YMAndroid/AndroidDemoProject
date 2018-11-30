package com.baolong.ble_tpms.ui;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.baolong.ble_tpms.ui.utils.CrashCat;
import com.clj.fastble.BleManager;

public class TpmsApplication extends Application {

    private static final String TAG = "TpmsApplication";
    @Override
    public void onCreate() {
        super.onCreate();
       // CrashCat.getInstance(getApplicationContext(), Environment.getExternalStorageDirectory().getPath()+ ConstValue.DIRECTORY_ROOT,ConstValue.FILE_LOG).start();
        Log.i(TAG,"onCreate");
    }
}
