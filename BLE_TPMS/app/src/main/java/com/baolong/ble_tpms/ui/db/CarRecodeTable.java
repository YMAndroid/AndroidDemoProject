package com.baolong.ble_tpms.ui.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baolong.ble_tpms.ui.bean.CarRecode;

import java.util.ArrayList;

public class CarRecodeTable extends Table {
    public static final String  carType = "car_type";
    public static final String carName = "car_name";
    public static final String carRemark = "car_remark";
    public static final String status = "status";
    public static final String TABLE_NAME = "car_record";
    public static final String  carId = "id";

    public CarRecodeTable(){
        init();
    }

    private void init() {
        tableName = TABLE_NAME;
        keyItem = "id";
        //构建属性
        items.add(new Item(carType  , Item.item_type_integer));
        items.add(new Item(carName  , Item.item_type_text));
        items.add(new Item(carRemark  , Item.item_type_text));
        items.add(new Item(status  , Item.item_type_integer));
    }

    public long insert(ContentValues cv)
    {
        return SQLiteManager.getInstance().db.insert(tableName,null,cv);
    }

    //delete item
    public long deleteAllItem()
    {
        return SQLiteManager.getInstance().db.delete(tableName,null,null);
    }
    //
    public long deleteOneByItem(String item,String content)
    {
        String[] args = {String.valueOf(content)};
        //1、删除绑定的设备
        //String sql1 = "select * from "
        //2、删除绑定设备对应的数据

        return SQLiteManager.getInstance().db.delete(tableName,item+" =?",args);
    }



    //update item
    public long updateOneByItem(String item,String content,ContentValues contentData)
    {
        String[] args = {String.valueOf(content)};
        return SQLiteManager.getInstance().db.update(tableName,contentData, item+" =? ",args);
    }

    public long updateAllStatusById(int id, boolean isTrue)
    {
        String sql = "update " + tableName + " set status = 1 " + " where id = " + id;
        String sql2 = "update " + tableName + " set status = 0 " + " where id != " + id;
        String sql3 = "update " + tableName + " set status = 0 ";
        if(isTrue){
            SQLiteManager.getInstance().db.execSQL(sql);
            SQLiteManager.getInstance().db.execSQL(sql2);
        } else {
            SQLiteManager.getInstance().db.execSQL(sql3);
        }
        return 0;
    }

    public ArrayList<CarRecode> queryAllData(){
        Cursor cursor = SQLiteManager.getInstance().db.query(tableName,null,null,null,null,null,null);
        ArrayList<CarRecode> carRecodes = null;
        if(cursor != null){
            carRecodes = new ArrayList<>();
            while (cursor.moveToNext()){
                CarRecode carRecode = new CarRecode();
                carRecode.setCarType(cursor.getInt(cursor.getColumnIndex(carType)));
                carRecode.setId(cursor.getInt(cursor.getColumnIndex(carId)));
                carRecode.setCarRemark(cursor.getString(cursor.getColumnIndex(carRemark)));
                carRecode.setCarName(cursor.getString(cursor.getColumnIndex(carName)));
                carRecode.setStatus(cursor.getInt(cursor.getColumnIndex(status)));
                carRecodes.add(carRecode);
            }
        }
        return carRecodes;
    }

    public ArrayList<CarRecode> queryCurrentSelectCarData(){
        //int status  = Config.SELECTED;
        Cursor cursor = SQLiteManager.getInstance().db.query(tableName,null,status+"=?",new String[]{String.valueOf(Config.SELECTED)},null,null,null);
        ArrayList<CarRecode> carRecodes = null;
        if(cursor != null){
            carRecodes = new ArrayList<>();
            while (cursor.moveToNext()){
                CarRecode carRecode = new CarRecode();
                carRecode.setCarType(cursor.getInt(cursor.getColumnIndex(carType)));
                carRecode.setId(cursor.getInt(cursor.getColumnIndex(carId)));
                carRecode.setCarRemark(cursor.getString(cursor.getColumnIndex(carRemark)));
                carRecode.setCarName(cursor.getString(cursor.getColumnIndex(carName)));
                carRecode.setStatus(cursor.getInt(cursor.getColumnIndex(status)));
                carRecodes.add(carRecode);
            }
        }
        return carRecodes;
    }
}
