package com.baolong.ble_tpms.ui.db;

public class Item {
    public static final String item_type_integer = "item_type_integer";
    public static final String item_type_text = "item_type_text";
    public static final String item_type_long = "item_type_long";
    public static final String item_type_real = "item_type_real";
    public static final String item_type_boolen = "item_type_boolen";

    public String text = "";//用于SQL拼接的关键字（要是无法理解可见接下来的例子）
    public String type = "";//该item的类型

    public Item(String text, String type) {
        this.text = text;
        this.type = type;
    }

    public Item() {

    }
}
