package com.baolong.ble_tpms.ui.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.baolong.ble_tpms.ui.bean.CarDeviceTripDataBean;
import com.baolong.ble_tpms.ui.bean.CarRecode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CarDeviceTripDataTable extends Table {
    private static final String TAG = "CarDeviceTripDataTable";
    public static final String id = "id";
    public static final String tripPressureDeviceId = "trip_pressure_device_id";
    public static final String temperature = "temperature";
    public static final String pressure = "pressure";
    public static final String addDate = "add_date";
    public static final String TABLE_NAME = "car_device_trip_data";

    public CarDeviceTripDataTable() {
        init();
    }

    private void init() {
        tableName = TABLE_NAME;
        keyItem = "id";
        //构建属性
        items.add(new Item(tripPressureDeviceId, Item.item_type_integer));
        items.add(new Item(temperature, Item.item_type_integer));
        items.add(new Item(pressure, Item.item_type_real));
        items.add(new Item(addDate, Item.item_type_text));
    }

    public long insert(ContentValues cv) {
        long result = SQLiteManager.getInstance().db.insert(tableName, null, cv);
        Log.i(TAG, "insert result = " + result);
        return result;
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

    public ArrayList<CarDeviceTripDataBean> queryDataByDeviceId(int deviceId, boolean isNewest) {////

        String sql = "select top 1 * from car_device_trip_data order by add_date desc ";

        Cursor cursor = SQLiteManager.getInstance().db.query(tableName, null, tripPressureDeviceId + "=?", new String[]{String.valueOf(deviceId)}, null, null, addDate + " desc");
        ArrayList<CarDeviceTripDataBean> CarDeviceTripDataBeans = null;
        if (cursor != null) {
            CarDeviceTripDataBeans = new ArrayList<>();
            while (cursor.moveToNext()) {
                CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
                carDeviceTripDataBean.setId(cursor.getInt(cursor.getColumnIndex(id)));
                carDeviceTripDataBean.setAddDate(cursor.getString(cursor.getColumnIndex(addDate)));
                carDeviceTripDataBean.setPressure(cursor.getDouble(cursor.getColumnIndex(pressure)));
                carDeviceTripDataBean.setTemperature(cursor.getInt(cursor.getColumnIndex(temperature)));
                carDeviceTripDataBean.setTripPressureDeviceId(cursor.getInt(cursor.getColumnIndex(tripPressureDeviceId)));
                CarDeviceTripDataBeans.add(carDeviceTripDataBean);
                if (isNewest)
                    break;
            }
        }
        return CarDeviceTripDataBeans;
    }

//    /**
//     * 按周查询
//     * @param deviceId
//     * @return
//     */
//    public ArrayList<CarDeviceTripDataBean> queryDataByWeek(int deviceId){
//        String sql = "select  time>=datetime('now','start of day','-7 day','weekday 1') AND time<datetime('now','start of day','+0 day','weekday 1') from" + TABLE_NAME;
//        Cursor cursor =  SQLiteManager.getInstance().db.rawQuery(sql,null);
//        ArrayList<CarDeviceTripDataBean> carDeviceTripDataBeans = null;
//        if(cursor != null){
//            carDeviceTripDataBeans = new ArrayList<>();
//            while (cursor.moveToNext()){
//                CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
//                carDeviceTripDataBean.setId(cursor.getInt(cursor.getColumnIndex(id)));
//                carDeviceTripDataBean.setAddDate(cursor.getString(cursor.getColumnIndex(addDate)));
//                carDeviceTripDataBean.setPressure(cursor.getDouble(cursor.getColumnIndex(pressure)));
//                carDeviceTripDataBean.setTemperature(cursor.getInt(cursor.getColumnIndex(temperature)));
//                carDeviceTripDataBean.setTripPressureDeviceId(cursor.getInt(cursor.getColumnIndex(tripPressureDeviceId)));
//                carDeviceTripDataBeans.add(carDeviceTripDataBean);
//            }
//        }
//        return carDeviceTripDataBeans;
//    }

    /**
     * 查询数据用于统计
     *
     * @param deviceId
     * @param type
     * @return
     */
    public ArrayList<CarDeviceTripDataBean> queryDataStatistics(final int deviceId, int type, String day) {//add_date<
//        String sqlDay = "select * from car_device_trip_data where add_date>=date('now','start of day','+0 day') and add_date<=date('now',' start of day',' +1 day') and " + tripPressureDeviceId + "=" + deviceId;
//        String sqlWeek = "select  add_date>=date('now','start of day','-7 day','weekday 1') AND add_date<=date('now','start of day','+0 day','weekday 1') from " + TABLE_NAME + " where " + tripPressureDeviceId + "=" + deviceId;
//        String sqlMonth = "select  add_date>=date('now','start of month','+0 month','-0 day') AND add_date<=date('now','start of month','+1 month','0 day') from " + TABLE_NAME + " where " + tripPressureDeviceId + "=" + deviceId;
//        Log.i(TAG, "sqlSay = " + sqlDay + " ;sqlWeek = " + sqlWeek + " ;sqlMonth = " + sqlMonth);
        //（’%Y-%m-%d’

        String sqlDay1 = "select * from car_device_trip_data where " + tripPressureDeviceId + "=" + deviceId + " and " + "strftime('%d', add_date) = " + "'" + day + "'";
        String sqlWeek1 = createByWeekSQL(TABLE_NAME, deviceId);
        String sqlMonth1 = "select * from car_device_trip_data where strftime('%m', add_date) = " + "'" + day + "'" + " and " + tripPressureDeviceId + "=" + deviceId;

        String sqlDay2 = "SELECT count(*) as count,add_date,avg(temperature) as temperature,avg(pressure) as pressure from car_device_trip_data where trip_pressure_device_id =" + "'" + deviceId + "'" + " and strftime('%Y-%m-%d',add_date) = " + "'" + day + "'" + " GROUP BY strftime('%H',  add_date) order by strftime('%H',add_date) ASC";
        String sqlMonth2 = "select count(*) as count ,avg(temperature) as temperature ,avg(pressure) as pressure,add_date from car_device_trip_data where " + tripPressureDeviceId + "=" + "'" + deviceId + "'" + " and strftime('%Y-%m', add_date) = " + "'" + day + "'" + " group by strftime('%d',add_date) order by strftime('%d',add_date) ASC";
        String sqlYear2 = "select count(*) as count, avg(temperature) as temperature, avg(pressure) as  pressure, add_date from car_device_trip_data " +
                "where " + tripPressureDeviceId + "=" + "'" + deviceId + "' and " + "strftime('%Y', add_date) = " + "'" + day + "'" + " GROUP BY strftime('%m',add_date) order by strftime('%m',add_date) ASC";

        Log.i(TAG, "sqlDay2 = " + sqlDay2 + " ;sqlMonth2 = " + sqlMonth2 + " ;sqlYear2 = " + sqlYear2);
//        String sqlTest = "select press/date as Avg from (" +
//                "select sum(pressure) as press,count(add_date) as date, datename(hour, add_date) as hour from car_device_trip_data " +
//                "where add_date > '2018-10-09 00:00:00.000' and add_date < '2018-10-11 00:00:00.000' " +
//                "group by add_date " +
//                ") as a ";
        Cursor cursor = null;
        if (type == Config.BY_DAY) {
            cursor = SQLiteManager.getInstance().db.rawQuery(sqlDay2, null);
        } else if (type == Config.BY_MONTH) {
            cursor = SQLiteManager.getInstance().db.rawQuery(sqlMonth2, null);
        } else if (type == Config.BY_YEAR) {
            cursor = SQLiteManager.getInstance().db.rawQuery(sqlYear2, null);
        }
        ArrayList<CarDeviceTripDataBean> carDeviceTripDataBeans = null;
        if (cursor != null) {
            carDeviceTripDataBeans = new ArrayList<>();
            if (!cursor.moveToFirst()) {
                Log.i(TAG, "queryDataStatistics no data");
                return null;
            } else {
                do {
                    CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
                    //carDeviceTripDataBean.setId(cursor.getInt(cursor.getColumnIndex(id)));
                    carDeviceTripDataBean.setAddDate(cursor.getString(cursor.getColumnIndex(addDate)));
                    carDeviceTripDataBean.setPressure(cursor.getDouble(cursor.getColumnIndex(pressure)));
                    carDeviceTripDataBean.setTemperature(cursor.getInt(cursor.getColumnIndex(temperature)));
                    //carDeviceTripDataBean.setTripPressureDeviceId(cursor.getInt(cursor.getColumnIndex(tripPressureDeviceId)));
                    carDeviceTripDataBeans.add(carDeviceTripDataBean);
                } while (cursor.moveToNext());
            }
        }
        return carDeviceTripDataBeans;
    }

    public static String createByWeekSQL(String table, int deviceId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long time = System.currentTimeMillis();
        // 现在的时间
        String currentTime = simpleDateFormat.format(time);
        //7天前的时间
        String before_Time = simpleDateFormat.format(time - 7 * 86400000);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from ");
        stringBuffer.append(" " + table);
        stringBuffer.append(" where ");
        stringBuffer.append(" date(");
        stringBuffer.append(" " + addDate);
        stringBuffer.append(" ) ");
        stringBuffer.append(" between date('");
        stringBuffer.append(before_Time);
        stringBuffer.append("') and date('");
        stringBuffer.append(currentTime);
        stringBuffer.append("')");
        stringBuffer.append(" and " + tripPressureDeviceId);
        stringBuffer.append("=" + deviceId);
        return stringBuffer.toString();
    }


//    public ArrayList<CarDeviceTripDataBean> queryDataByMonth(int deviceId){
//        String sql = "select  Time>=datetime('now','start of month','+0 month','-0 day') AND Time < datetime('now','start of month','+1 month','0 day') from " + TABLE_NAME;
//
//    }

    /**
     * @param deviceId   设备ID
     * @param statisType 统计类型 --按天 --按周 --按月
     * @return
     */
    public ArrayList<CarDeviceTripDataBean> queryDataByDeviceStatis(int deviceId, int statisType) {////

        String sql = "select top 1 * from car_device_trip_data order by add_date desc ";

        Cursor cursor = SQLiteManager.getInstance().db.query(tableName, null, tripPressureDeviceId + "=?", new String[]{String.valueOf(deviceId)}, null, null, addDate + " desc");
        ArrayList<CarDeviceTripDataBean> CarDeviceTripDataBeans = null;
        if (cursor != null) {
            CarDeviceTripDataBeans = new ArrayList<>();
            while (cursor.moveToNext()) {
                CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
                carDeviceTripDataBean.setId(cursor.getInt(cursor.getColumnIndex(id)));
                carDeviceTripDataBean.setAddDate(cursor.getString(cursor.getColumnIndex(addDate)));
                carDeviceTripDataBean.setPressure(cursor.getDouble(cursor.getColumnIndex(pressure)));
                carDeviceTripDataBean.setTemperature(cursor.getInt(cursor.getColumnIndex(temperature)));
                carDeviceTripDataBean.setTripPressureDeviceId(cursor.getInt(cursor.getColumnIndex(tripPressureDeviceId)));
                CarDeviceTripDataBeans.add(carDeviceTripDataBean);
            }
        }
        return CarDeviceTripDataBeans;
    }

    public void deleteDeviceData(int deviceId){
        //return SQLiteManager.getInstance().db.delete(tableName, carRecordId, new String[]{String.valueOf(carId)});
    }
}
