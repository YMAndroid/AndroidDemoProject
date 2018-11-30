package com.baolong.ble_tpms.ui.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TpmsDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "TpmsDBHelper";
    public static final int Db_VERSION = 1;
    private static final String DATABASE_NAME = "ble_tpms.db";
    public static final String CAR_TYPE_CONFIG_TABLE = "car_type_config";
    public static final String CAR_RECORD_TABLE = "car_record";



    //汽车类型配置表
    private static final String CREATE_TABLE_CAR_TYPE_CONFIG = "create table car_type_config ("
            + "id integer primary key autoincrement,"
            + "car_type_name text, "
            + "trip_number text, "
            + "status integer)";//数据库里的表

    //汽车表
    private static final String CREATE_TABLE_CAR_RECORD = "create table car_record ("
            + "id integer primary key autoincrement,"
            + "car_type integer, "
            + "car_name text, "
            + "car_remark text, "
            + "status integer)";

    public TpmsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, Db_VERSION);
    }

    public TpmsDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version) {
        super(context, name, factory, version);
        Log.d(TAG,"New TpmsDBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"onCreate");
        db.execSQL(CREATE_TABLE_CAR_TYPE_CONFIG);
        db.execSQL(CREATE_TABLE_CAR_RECORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
