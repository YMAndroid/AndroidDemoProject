package com.baolong.ble_tpms.ui.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.baolong.ble_tpms.ui.bean.CarRecode;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;

import java.util.ArrayList;

public class TripPressureDeviceTable extends Table {
    public static final String TAG = "TripPressureDeviceTable";
    public static final String carRecordId = "car_record_id";
    public static final String carName = "car_name";
    public static final String tripDataId = "trip_data_id";
    public static final String deviceName = "device_name";
    public static final String tripType = "tirp_type";
    public static final String isUpgrade = "is_upgrade";
    public static final String id = "id";
    public static final String macAddress = "mac_address";
    public static final String pairStatus = "pair_status";
    public static final String bindStatus = "bind_status";
    public static final String TABLE_NAME = "trip_pressure_device";

    public TripPressureDeviceTable() {
        init();
    }

    private void init() {
        tableName = TABLE_NAME;
        keyItem = "id";
        //构建属性
        items.add(new Item(carRecordId, Item.item_type_integer));
        items.add(new Item(carName, Item.item_type_text));
        items.add(new Item(tripDataId, Item.item_type_text));
        items.add(new Item(deviceName, Item.item_type_text));
        items.add(new Item(tripType, Item.item_type_integer));
        items.add(new Item(macAddress, Item.item_type_text));
        items.add(new Item(pairStatus, Item.item_type_integer));
        items.add(new Item(isUpgrade, Item.item_type_integer));
        items.add(new Item(bindStatus, Item.item_type_integer));
    }

    public long insert(ContentValues cv) {
        //先查询该设备是否已经插入过
        //mac 地址  轮胎类型  绑定状态
        String sql = "select * from " + tableName + " where " + "car_record_id=" + cv.get(carRecordId) + " and " + "tirp_type=" + cv.get(tripType) + " and " +
                "mac_address=" + "'" + cv.get(macAddress) + "'" + " and " + "bind_status=" + Config.BIND_VALUE;
//        Cursor cursor = SQLiteManager.getInstance().db.query(tableName,null,carRecordId +"=?" + " and " + tripType + "=?" + " and " + macAddress +"=?" + " and " + bindStatus + "!=" + Config.UN_BIND,
//                new String[]{String.valueOf(cv.get(carRecordId)),String.valueOf(cv.get(tripType)),(String) cv.get(macAddress)},null,null,null);
        Log.i(TAG,"insert sql = " + sql);
        Cursor cursor = SQLiteManager.getInstance().db.rawQuery(sql,null);
        if(cursor != null && cursor.moveToNext()){
            //该数据存在
            return 0;
        }
        return SQLiteManager.getInstance().db.insert(tableName, null, cv);
    }

    //delete item
    public long deleteAllItem() {
        return SQLiteManager.getInstance().db.delete(tableName, null, null);
    }

    //
    public long deleteOneByItem(String item, String content) {
        String[] args = {String.valueOf(content)};
        return SQLiteManager.getInstance().db.delete(tableName, item + " =?", args);
    }


    //update item
    public long updateOneByItem(String item, String content, ContentValues contentData) {
        String[] args = {String.valueOf(content)};
        return SQLiteManager.getInstance().db.update(tableName, contentData, item + " =? ", args);
    }

    public long updateAllStatusById(int id, boolean isTrue) {
        String sql = "update " + tableName + " set status = 1 " + " where id = " + id;
        String sql2 = "update " + tableName + " set status = 0 " + " where id != " + id;
        String sql3 = "update " + tableName + " set status = 0 ";
        if (isTrue) {
            SQLiteManager.getInstance().db.execSQL(sql);
            SQLiteManager.getInstance().db.execSQL(sql2);
        } else {
            SQLiteManager.getInstance().db.execSQL(sql3);
        }
        return 0;
    }

    public long updateBindStatus(int id, int bindStatus) {
        Log.i(TAG,"updateBindStatus");
        String sql = "update " + tableName + " set bind_status =  " + bindStatus + " where id = " + id;
        SQLiteManager.getInstance().db.execSQL(sql);
        return 0;
    }

    public void updateUpgradeStatus(int id, int type) {
        String sql = "update " + tableName + " set is_upgrade = " + type + " where id= " + id;
        SQLiteManager.getInstance().db.execSQL(sql);
    }

    public ArrayList<TripPressureDevice> queryAllData() {
        if (SQLiteManager.getInstance().db == null) {
            return null;
        }
        Cursor cursor = SQLiteManager.getInstance().db.query(tableName, null, null,null, null, null, null);
        ArrayList<TripPressureDevice> tripPressureDevices = null;
        if (cursor != null) {
            tripPressureDevices = new ArrayList<>();
            while (cursor.moveToNext()) {
                TripPressureDevice tripPressureDevice = new TripPressureDevice();
                tripPressureDevice.setCarName(cursor.getString(cursor.getColumnIndex(carName)));
                tripPressureDevice.setId(cursor.getInt(cursor.getColumnIndex(id)));
                tripPressureDevice.setCarRecodeId(cursor.getInt(cursor.getColumnIndex(carRecordId)));
                tripPressureDevice.setDeviceName(cursor.getString(cursor.getColumnIndex(deviceName)));
                tripPressureDevice.setIsUpgrade(cursor.getInt(cursor.getColumnIndex(isUpgrade)));
                tripPressureDevice.setTripDataId(cursor.getInt(cursor.getColumnIndex(tripDataId)));
                tripPressureDevice.setTripType(cursor.getInt(cursor.getColumnIndex(tripType)));
                //tripPressureDevice.setDeviceName(cursor.getString(cursor.getColumnIndex(deviceName)));
                tripPressureDevice.setMacAddress(cursor.getString(cursor.getColumnIndex(macAddress)));
                tripPressureDevice.setPairStatus(cursor.getInt(cursor.getColumnIndex(pairStatus)));
                tripPressureDevices.add(tripPressureDevice);
            }
        }
        return tripPressureDevices;
    }

    public ArrayList<TripPressureDevice> queryDataByCarRecodeId(int carRecodeId) {
        Log.i(TAG,"queryDataByCarRecodeId");
        Cursor cursor = SQLiteManager.getInstance().db.query(tableName, null, carRecordId + "=?" + " and " + bindStatus + "=?", new String[]{String.valueOf(carRecodeId),String.valueOf(Config.BIND_VALUE)}, null, null,null);// " order by " + tripType +" ASC "
        ArrayList<TripPressureDevice> tripPressureDevices = null;
        if (cursor != null) {
            tripPressureDevices = new ArrayList<>();
            while (cursor.moveToNext()) {
                TripPressureDevice tripPressureDevice = new TripPressureDevice();
                tripPressureDevice.setCarName(cursor.getString(cursor.getColumnIndex(carName)));
                tripPressureDevice.setId(cursor.getInt(cursor.getColumnIndex(id)));
                tripPressureDevice.setCarRecodeId(cursor.getInt(cursor.getColumnIndex(carRecordId)));
                tripPressureDevice.setDeviceName(cursor.getString(cursor.getColumnIndex(deviceName)));
                tripPressureDevice.setIsUpgrade(cursor.getInt(cursor.getColumnIndex(isUpgrade)));
                tripPressureDevice.setTripDataId(cursor.getInt(cursor.getColumnIndex(tripDataId)));
                tripPressureDevice.setTripType(cursor.getInt(cursor.getColumnIndex(tripType)));
                tripPressureDevice.setMacAddress(cursor.getString(cursor.getColumnIndex(macAddress)));
                tripPressureDevice.setPairStatus(cursor.getInt(cursor.getColumnIndex(pairStatus)));
                tripPressureDevice.setBindStatus(cursor.getInt(cursor.getColumnIndex(bindStatus)));
                tripPressureDevices.add(tripPressureDevice);
            }
        }
        return tripPressureDevices;
    }

    //删除设备，通过车辆Id
    public long deleteDeviceByCarId(int carId){
        return SQLiteManager.getInstance().db.delete(tableName, carRecordId, new String[]{String.valueOf(carId)});
    }
}
