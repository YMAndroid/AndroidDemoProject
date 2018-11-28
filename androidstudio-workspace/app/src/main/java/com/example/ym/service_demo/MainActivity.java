package com.example.ym.service_demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ym.service_demo.service.LocalService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button btnStartService, btnStopService, btnBindService, btnUnbindService;
    private LocalService mLocalService;
    private Intent intent;
    private boolean mShouldUnbind = false;
    private boolean mShouldStopService = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalService = ((LocalService.LocalBinder)service).getService();
            Toast.makeText(MainActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocalService = null;
            Toast.makeText(MainActivity.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void doBindService(){
        if(bindService(new Intent(MainActivity.this,LocalService.class),mConnection,Context.BIND_AUTO_CREATE)){
            mShouldUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindService(){
        if(mShouldUnbind){
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    private void doStartService(){
        startService(new Intent(MainActivity.this,LocalService.class));
        mShouldStopService = true;
    }

    private void doStopService(){
        if(mShouldStopService){
            stopService(new Intent(MainActivity.this,LocalService.class));
            mShouldStopService = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        initView();
        //serviceTest = new ServiceTest();
        intent = new Intent();
    }

    private void initView() {
        btnStartService = findViewById(R.id.btn_start_service);
        btnStartService.setOnClickListener(this);
        btnStopService = findViewById(R.id.btn_stop_service);
        btnStopService.setOnClickListener(this);
        btnBindService = findViewById(R.id.btn_bind_service);
        btnBindService.setOnClickListener(this);
        btnUnbindService = findViewById(R.id.btn_unbind_service);
        btnUnbindService.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_start_service:
                doStartService();
                break;
            case R.id.btn_stop_service:
                doStopService();
                break;
            case R.id.btn_bind_service:
                doBindService();
                break;
            case R.id.btn_unbind_service:
                doUnbindService();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        doUnbindService();
        doStopService();
    }
}
