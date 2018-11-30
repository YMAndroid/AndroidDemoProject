package com.baolong.ble_tpms.ui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.adapter.CarManagerAdapter;
import com.baolong.ble_tpms.ui.adapter.MyOnItemClickListener;
import com.baolong.ble_tpms.ui.adapter.MyOnItemLongClickListener;
import com.baolong.ble_tpms.ui.bean.CarRecode;
import com.baolong.ble_tpms.ui.db.CarRecodeTable;

import java.util.ArrayList;
import java.util.List;

public class CarManagerActivity extends BaseTitleActivity {
    private static final String TAG = "CarManagerActivity";
    private RecyclerView mRecyclerView;
    private static final int REQUEST_CODE = 1;
    private ArrayList<CarRecode> carRecodes = new ArrayList<CarRecode>();
    private CarRecodeTable carRecodeTable = new CarRecodeTable();
    private CarManagerAdapter carManagerAdapter;
    private int tvRightStatus = 0;
    private String titileStr = null;
    private String rightShowText = null;
    private boolean isMain = false;
    private TextView tvNoCarData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        mContext = this;
    }

    @Override
    public void init() {
        Intent intent = getIntent();//获取传来的intent对象
        isMain = intent.getBooleanExtra("main",false);//获取键值对的键名
        if(isMain){
            titileStr = getResources().getString(R.string.please_add_car);
        } else {
            titileStr = getResources().getString(R.string.car_manager);
        }
        setTitleAndContentLayoutId(titileStr, R.layout.activity_car_manager);
        tvRightTitle.setText("add");
        tvRightTitle.setVisibility(View.VISIBLE);
        //ivType.setVisibility(View.VISIBLE);
        tvRightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到汽车类型界面
                Intent intent = new Intent();
                intent.setClass(CarManagerActivity.this, CarTypeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        ivType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到主界面
                setResult(RESULT_OK);
                finish();
            }
        });
        mContext = this;
        initView();
        initData();
    }

    private void initData() {
        queryCarRecodeData();
        carManagerAdapter = new CarManagerAdapter(mContext, carRecodes);
        mRecyclerView.setAdapter(carManagerAdapter);
        // 给每个item添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 设置item增加和移除的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CarManagerActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //carManagerAdapter.notifyDataSetChanged();
        onItemClick();
        onItemLongClik();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyle_view);
        tvNoCarData = findViewById(R.id.tv_no_car_data);
    }

    private void queryCarRecodeData() {
        carRecodes.clear();
        //查询数据
        carRecodes.addAll(carRecodeTable.queryAllData());
        if(carRecodes.size() >0 ){
            tvNoCarData.setVisibility(View.GONE);
        } else {
            tvNoCarData.setVisibility(View.VISIBLE);
        }
    }

    public void onItemClick() {
        carManagerAdapter.setOnItemClickListener(new MyOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CheckBox checkBox = view.findViewById(R.id.item_check);
                if(checkBox.isChecked()){
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
                carRecodeTable.updateAllStatusById(carRecodes.get(position).getId(),checkBox.isChecked());
                queryCarRecodeData();
                carManagerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onItemLongClik() {
        carManagerAdapter.setOnItemLongClickListener(new MyOnItemLongClickListener() {
            @Override
            public void OnItemLongClickListener(View view, final int position) {
                 CheckBox checkBox = (CheckBox)view.findViewById(R.id.item_check);
                checkBox.setChecked(true);
                //弹出删除dialog
                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.prompt))
                        .setMessage(getResources().getString(R.string.delete) + carRecodes.get(position).getCarRemark() + getResources().getString(R.string.car_infomation))
                        .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                carRecodeTable.deleteOneByItem(CarRecodeTable.carId, String.valueOf(carRecodes.get(position).getId()));
                                carRecodes.remove(position);
                                carManagerAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("cancel", null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

//    @Override
//    public void onItemLongClick(View view, final int position) {

//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            queryCarRecodeData();
            carManagerAdapter.notifyDataSetChanged();
            //发送广播
            Intent intent = new Intent();
            intent.setAction("com.baolong.ble_tpms.ui.ui.CarManagerActivity");
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isMain){
            setResult(RESULT_OK);
        }
    }
}
