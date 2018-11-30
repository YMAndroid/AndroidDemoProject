package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.adapter.DeviceUpgradeAdapter;
import com.baolong.ble_tpms.ui.adapter.MyOnItemClickListener;
import com.baolong.ble_tpms.ui.adapter.UnBindDeviceAdapter;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.CarRecodeTable;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class UnBindDeviceActivity extends BaseTitleActivity {
    private TextView tvNoData;
    private RecyclerView mRecyclerView;
    private UnBindDeviceAdapter unBindDeviceAdapter;
    private List<TripPressureDevice> tripPressureDevices = new ArrayList<TripPressureDevice>();
    private TripPressureDeviceTable tripPressureDeviceTable = new TripPressureDeviceTable();
    private int carId = 0;
    private static final String TAG = "UnBindDeviceActivity";

    @Override
    public void init() {
        setTitleAndContentLayoutId(getResources().getString(R.string.unbind_device), R.layout.activity_unbind_device);
        Intent intent = getIntent();
        carId = intent.getIntExtra("carId", 0);
        initView();
        initData();
    }

    private void initView() {
        tvNoData = findViewById(R.id.tv_unbind_no_data);
        mRecyclerView = findViewById(R.id.recyle_view_unbind_device);
    }

    private void initData() {
        //tripPressureDevices.addAll(fakeData());
        queryData();
        //queryData();
        unBindDeviceAdapter = new UnBindDeviceAdapter(this, tripPressureDevices);
        mRecyclerView.setAdapter(unBindDeviceAdapter);
        // 给每个item添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 设置item增加和移除的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UnBindDeviceActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //carManagerAdapter.notifyDataSetChanged();
        onItemClick();
    }

    private void queryData() {
        tripPressureDevices.clear();
        if (carId != 0) {
            ArrayList<TripPressureDevice> tempList = tripPressureDeviceTable.queryDataByCarRecodeId(carId);
            if (tempList != null) {
                tripPressureDevices.addAll(tempList);
            }
        }
        setNoDataIsShow();

    }

    public void setNoDataIsShow(){
        if (tripPressureDevices.size() <= 0) {
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
    }

    public void onItemClick() {
        unBindDeviceAdapter.setOnItemClickListener(new MyOnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                //解除设备绑定
                //弹出删除dialog
                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.prompt))
                        .setMessage(getResources().getString(R.string.unbind_device) + " mac address: " + tripPressureDevices.get(position).getMacAddress() + " device ?")
                        .setNegativeButton("confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i(TAG,"unbind device mac address = " + tripPressureDevices.get(position).getMacAddress());
                                if(BleManager.getInstance().isConnected(tripPressureDevices.get(position).getMacAddress())){
                                    //断开连接
                                    List<BleDevice> bleDevices = BleManager.getInstance().getAllConnectedDevice();
                                    if (bleDevices != null && bleDevices.size() > 0) {
                                        for (int j = 0; j < bleDevices.size(); j++) {
                                            if (bleDevices.get(j).getMac().equalsIgnoreCase(tripPressureDevices.get(position).getMacAddress())) {
                                                BleManager.getInstance().disconnect(bleDevices.get(j));
                                                //Toast.makeText(UnBindDeviceActivity.this,"unbind success",Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
                                }
                                tripPressureDeviceTable.updateBindStatus(tripPressureDevices.get(position).getId(), Config.UN_BIND_VALUE);
                                //queryData();
                                //unBindDeviceAdapter.notifyDataSetChanged();
                                Intent intent = new Intent();
                                intent.putExtra("macAddress",tripPressureDevices.get(position).getMacAddress());
                                intent.putExtra("type",tripPressureDevices.get(position).getTripType());
                                intent.setAction("com.baolong.ble_tpms.ui.ui.UnBindDeviceActivity");
                                sendBroadcast(intent);
                                Log.i(TAG,"position = " + position);
                                tripPressureDevices.remove(position);
                                unBindDeviceAdapter.notifyDataSetChanged();
                                setNoDataIsShow();
                            }

                        }).setPositiveButton("cancel", null).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

//    class MyHandler extends Handler{
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                unBindDeviceAdapter.notifyDataSetChanged();
//            }
//        }
//    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }
}
