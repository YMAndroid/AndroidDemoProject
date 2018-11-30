package com.baolong.ble_tpms.ui.db;

import android.content.Context;
import android.database.ContentObservable;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SQLiteManager {

    public static final String TAG = "SQLiteManager";
    public SQLiteDatabase db;
    private Context context;
    private DBOpenHelper dbOpenHelper;
    // DB_name
    private static final String DB_NAME = "ble_tpms.db";

    // DB_version
    private int DB_VERSION = 1;

    //ArrayList <Table>
    private ArrayList<Table> tables = new ArrayList<>();//该库中表对象列表

    public SQLiteManager() {

    }

    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            int size = tables.size();//获取当前需要建表的数量
            for (int i = 0; i < size; i++)//拼接建表SQL语句
            {
                String sql = getTableCreateSQLString(tables.get(i));
                db.execSQL(sql);//执行建库建表语句
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            int size = tables.size();//同创建表机制，拼接更新新表SQL语句
//            for(int i = 0;i<size;i++)
//            {
//                db.execSQL("DROP TABLE IF EXISTS " + getTableCreateSQLString(tables.get(i)));
//            }
//            onCreate(db);
        }
    }

    //init 初始化建库工作，传入库名
    public void init(Context context) {
        tables.clear();
        this.context = context;
    }

    private static SQLiteManager instance = null;

    public static SQLiteManager getInstance()//单例模式 方便外部调用
    {
        if (instance == null) {
            synchronized (SQLiteManager.class) {
                if (instance == null) {
                    instance = new SQLiteManager();
                }
            }
        }
        return instance;
    }

    //open 做数据库操作之前都要调用open方法
    public void open() {

        dbOpenHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbOpenHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            ex.printStackTrace();
            exceptionHandler();
        }
        db.beginTransaction();
    }

    /**
     * 数据库文件损坏删除异常处理
     */
    private void exceptionHandler() {
        if (db == null) {
            return;
        }
        File file = new File(db.getPath());
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    open();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //close 长时间不适用数据库可执行close方法关闭数据库
    public void close() {

        db.setTransactionSuccessful();
        db.endTransaction();
        if (db != null) {
            db.close();
            db = null;

        }
    }

    //create table 注册表 外界任何想在该库下创建的表都必须注册 注册后tables列表会增加，这回作用到建表时SQL语句的生成
    public void registerTable(String table_name, ArrayList<Item> items) {
        Table table = new Table(table_name, "id", items);

        tables.add(table);
    }

    //两种不同的注册方式 建议使用第二种方式进行注册，因为第二种方式的表对象是外界传入的，外界可以利用该对象进行更多的操作。详情见下
    public void registerTable(Table table) {
        tables.add(table);
    }

    // get table create sql by items 建表语句拼接代码 遍历tables列表，由于每个表又可以有多个字段，故字段类型不同又有对应的语句区别。
    private static String getTableCreateSQLString(Table table) {
        String sql = "create table " + table.tableName;//表名 下面依次是“主键”+“item1”+“item2”+.....+"item n"

        sql += "(" + table.keyItem + " integer primary key autoincrement";

        int size = table.items.size();

        for (int i = 0; i < size; i++) {
            sql += ",";
            if (table.items.get(i).type.equals(Item.item_type_integer)) {
                sql += table.items.get(i).text + " integer not null";
            } else if (table.items.get(i).type.equals(Item.item_type_text)) {
                sql += table.items.get(i).text + " text not null";
            } else if (table.items.get(i).type.equals(Item.item_type_boolen)) {
                sql += table.items.get(i).text + " bool not null";
            } else if (table.items.get(i).type.equals(Item.item_type_long)) {
                sql += table.items.get(i).text + " long not null";
            } else if (table.items.get(i).type.equals(Item.item_type_real)) {
                sql += table.items.get(i).text + " real not null";
            }
            // TODO: 16-8-23 add other data type 可能有别的数据属性 要看SQLite还支持那些数据的存储
        }
        sql += ");";
        Log.i(TAG, "getTableCreateSQLString" + sql);
        return sql;
    }

    //如果是使用第一种方式注册的 这里提供一个接口获取本类中tables列表中的某个表，传入所需要获取的表名即可（不是重点）
    public Table getTableByName(String tableName) {
        int size = tables.size();
        if (size == 0) {
            return null;
        } else {
            for (int i = 0; i < size; i++) {
                if (tableName.equalsIgnoreCase(tables.get(i).tableName)) {
                    return tables.get(i);
                }
            }
        }
        return null;
    }
}
