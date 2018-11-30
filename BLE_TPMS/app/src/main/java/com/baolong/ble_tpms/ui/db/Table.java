package com.baolong.ble_tpms.ui.db;

import java.util.ArrayList;

public class Table {
    //表名
    public String tableName;
    //table string 表属性列表
    public ArrayList<Item> items = new ArrayList<>();
    //key 自定义主键项 一般默认未id
    public String keyItem;

    //db
    public Table(){

    }

    public Table(String tableName, String keyItem){
        this.keyItem = keyItem;
        this.tableName = tableName;
    }

    public Table(String tableName,String keyItem,ArrayList<Item> items){
        this.tableName = tableName;
        this.items = items;
        this.keyItem = keyItem;
    }

      /*
    fun 查改增删 方法的具体实现  这里强烈建议具体方法由子类区实现，因为不同的表所对应的查改增删方法都有不同。
    以后我们在使用的时候，我们可以让自己的表对象继承与Table对象，这样项目中就很好区分各个表对象，也可以很好的管理项目中的表。
     */

    //create table

    //delete table

    //inserte item
}
