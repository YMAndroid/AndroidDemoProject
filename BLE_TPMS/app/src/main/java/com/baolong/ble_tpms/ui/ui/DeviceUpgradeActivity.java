package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.adapter.DeviceUpgradeAdapter;
import com.baolong.ble_tpms.ui.adapter.MyOnItemClickListener;
import com.baolong.ble_tpms.ui.adapter.MyOnItemLongClickListener;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.CarRecodeTable;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceUpgradeActivity extends BaseTitleActivity {
    private RecyclerView mRecyclerView;
    private DeviceUpgradeAdapter deviceUpgradeAdapter;
    private List<TripPressureDevice> tripPressureDevices = new ArrayList<TripPressureDevice>();
    private TextView tvNoData;
    private TripPressureDeviceTable tripPressureDeviceTable = new TripPressureDeviceTable();
    private BleDevice bleDevice;
    private List<BleDevice> bleDeviceList;
    private int carId = 0;

    @Override
    public void init() {
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId(getResources().getString(R.string.upgrade_device), R.layout.activity_device_upgrade);
        Intent intent = getIntent();
        BleManager.getInstance().getAllConnectedDevice();
        //bleDevice = intent.getParcelableExtra("bleDevice");
        carId = intent.getIntExtra("carId", 0);
        initView();
        initData();
    }

    private List<TripPressureDevice> fakeData() {
        List<TripPressureDevice> tripPressureDeviceList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TripPressureDevice tripPressureDevice = new TripPressureDevice();
            tripPressureDevice.setTripType(1);
            tripPressureDevice.setTripDataId(1);
            tripPressureDevice.setDeviceName("测试数据" + i);
            tripPressureDevice.setCarRecodeId(i);
            tripPressureDevice.setIsUpgrade(0);
            tripPressureDevice.setCarName("小汽车" + i);
            tripPressureDeviceList.add(tripPressureDevice);
        }
        return tripPressureDeviceList;
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyle_view_device_upgrade);
        tvNoData = findViewById(R.id.tv_no_data);
    }

    private void initData() {
        //tripPressureDevices.addAll(fakeData());
        queryData();
        //queryData();
        deviceUpgradeAdapter = new DeviceUpgradeAdapter(this, tripPressureDevices);
        //添加头部
        //View headerView = LayoutInflater.from(this).inflate(R.layout.device_upgrade_item, null);
        //deviceUpgradeAdapter.addHeaderView(headerView);
        mRecyclerView.setAdapter(deviceUpgradeAdapter);
        // 给每个item添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 设置item增加和移除的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DeviceUpgradeActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //carManagerAdapter.notifyDataSetChanged();
        onItemClick();
        onItemLongClik();
    }

    private void queryData() {
        tripPressureDevices.clear();
        if (carId != 0) {
            ArrayList<TripPressureDevice> tempList = tripPressureDeviceTable.queryDataByCarRecodeId(carId);
            if (tempList != null) {
                tripPressureDevices.addAll(tempList);
            }
        }
        if (tripPressureDevices.size() <= 0) {
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryData();
        deviceUpgradeAdapter.notifyDataSetChanged();
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    public void onItemClick() {
        deviceUpgradeAdapter.setOnItemClickListener(new MyOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //点击进入到详情界面
                Intent intent = new Intent();
                intent.setClass(DeviceUpgradeActivity.this, DeviceDetailActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable("tripPressureDevice", tripPressureDevices.get(position));
                //bundle.putParcelable("bleDevice",bleDevice);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void onItemLongClik() {
        deviceUpgradeAdapter.setOnItemLongClickListener(new MyOnItemLongClickListener() {
            @Override
            public void OnItemLongClickListener(View view, final int position) {
                //弹出删除
                //弹出删除dialog
                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.prompt))
                        .setMessage(getResources().getString(R.string.delete) + tripPressureDevices.get(position).getDeviceName() + "device")
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tripPressureDeviceTable.deleteOneByItem(TripPressureDeviceTable.id, String.valueOf(tripPressureDevices.get(position).getId()));
                                tripPressureDevices.remove(position);
                                deviceUpgradeAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton(getResources().getString(R.string.cancel), null).show();
            }
        });
    }

}
