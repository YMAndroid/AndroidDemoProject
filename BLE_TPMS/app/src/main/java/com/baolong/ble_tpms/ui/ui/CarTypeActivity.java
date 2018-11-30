package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.adapter.CarTypeAdapter;
import com.baolong.ble_tpms.ui.bean.CarRecode;
import com.baolong.ble_tpms.ui.bean.CarTypeBean;
import com.baolong.ble_tpms.ui.db.CarRecodeTable;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.TpmsDBHelper;

import java.util.ArrayList;
import java.util.List;

public class CarTypeActivity extends BaseTitleActivity {

    private ListView carTypeList;
    private String[] data = {"car"};
    //            ,"tricycle"
//            ,"motorcycle"
//            ,"bicycle"};//1,2,3,4
    private int[] dataType = {1, 2, 3, 4};//1,2,3,4
    private int[] resourceId = {R.mipmap.ic_directions_car};//, R.mipmap.ic_tricycle, R.mipmap.ic_motorcycle, R.mipmap.ic_bicycle};
    private static final String TAG = "CarTypeActivity";

    private List<CarTypeBean> carTypes = new ArrayList<CarTypeBean>();

    @Override
    public void init() {
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId(getResources().getString(R.string.choise_car_type), R.layout.acticity_car_type_list);
        carTypeList = findViewById(R.id.car_tyep_list);
        initCarTypeData();
        CarTypeAdapter carTypeAdapter = new CarTypeAdapter(this, R.layout.car_type_item, carTypes);
        carTypeList.setAdapter(carTypeAdapter);
        carTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alertEdit(view, position);
            }
        });
    }


    private void initCarTypeData() {
        //从数据库中获取
        //暂时写死
        for (int i = 0; i < data.length; i++) {
            CarTypeBean carTypeBean = new CarTypeBean(data[i], resourceId[i]);
            carTypes.add(carTypeBean);
        }

    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }


    public void alertEdit(View view, final int position) {
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.vehicle_note_information))
                .setView(et)
                .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG,"onClick cancel!");
                    }
                }).setNegativeButton(getResources().getString(R.string.enter), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //数据库插入一条记录
                //TpmsDBHelper helper = new TpmsDBHelper(getApplicationContext());
                //SQLiteDatabase database = helper.getWritableDatabase();
                CarRecodeTable carRecodeTable = new CarRecodeTable();
                CarRecode carRecode = new CarRecode();
                carRecode.setCarName(data[position]);
                carRecode.setCarType(dataType[position]);
                carRecode.setStatus(Config.UNSELECTED);
                carRecode.setCarRemark(et.getText().toString());
                if (carRecodeTable.queryAllData().size() <= 0) {
                    carRecode.setStatus(Config.SELECTED);
                }
                ContentValues values = carRecodeToContentValues(carRecode);


                long result = carRecodeTable.insert(values);
                Toast.makeText(mContext, getResources().getString(R.string.vehicle_add_success), Toast.LENGTH_LONG).show();
                Log.i(TAG, "车辆添加成功，数据库记录为：" + result);
                //database.insert(TpmsDBHelper.CAR_RECORD_TABLE,null,values);
                //database.close();
//                        //按下确定键后的事件
//                        //数据是使用Intent返回
//                        Intent intent = new Intent();
//                        //把返回数据存入Intent
//                        intent.putExtra("carTypeName", data[position]);
//                        intent.putExtra("carType", dataType[position]);
//                        //设置返回数据
                CarTypeActivity.this.setResult(RESULT_OK);
                finish();
            }
        }).show();
    }

    private ContentValues carRecodeToContentValues(CarRecode carRecode) {
        ContentValues contentValues = new ContentValues();
        //contentValues.put("id", carRecode.getId());
        contentValues.put("car_name", carRecode.getCarName());
        contentValues.put("car_remark", carRecode.getCarRemark());
        contentValues.put("car_type", carRecode.getCarType());
        contentValues.put("status", carRecode.getStatus());
        return contentValues;
    }
}
