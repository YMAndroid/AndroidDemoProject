package com.baolong.ble_tpms.ui.db;

public class Config {
    public static final int MANUAL_MATCH = 1;//手动匹配
    public static final String MANUAL_MATCH_STR = "manul_match";//手动匹配
    public static final String MATCH_TYPE = "match_type";//手动匹配
    public static final int AUTO_MATCH = 2;//自动匹配
    public static final String AUTO_MATCH_STR = "auto_match";//自动匹配
    public static final String CELSIUS_UNIT = "℃";
    public static final String FAHRENHEIT_UNIT = "℉";
    public static final String PSI = "psi";
    public static final String KPA = "kPa";
    public static final String BAR = "bar";

    //默认值--目前从设备中获取到的 温度 单位为摄氏度  压强单位为 Kpa
    public static final String TEMPERATURE_DEFAULTS_UNIT_PREF = "temperature_defaults_unit_pref";
    public static final String TEMPERATURE_DEFAULTS_VALUE_PREF = "temperature_defaults_value_pref";
    public static final int TEMPERATURE_DEFAULTS_VALUE = 70;//摄氏度
    public static final String TEMPERATURE_MIN_VALUE_PREF = "temperature_min_value_pref";
    public static final int TEMPERATURE_MIN_VALUE = 60;//23;//60; ---备注用于测试--需要修改回来
    public static final String TEMPERATURE_MAX_VALUE_PREF = "temperature_max_value_pref";
    public static final int TEMPERATURE_MAX_VALUE = 95;
    //当前选择的单位值
    public static final String TEMPERATURE_CURRENT_SEL_UNIT_PREF = "temperature_current_sel_unit_pref";
    public static final String TEMPERATURE_CURRENT_SEL_VALUE_PREF = "temperature_current_sel_value_pref";//默认保存为摄氏度

    //压强上限
    public static final String PRESSURE_DEFAULTS_UNIT_PREF = "pressure_defaults_unit_pref";
    public static final String PRESSURE_UP_DEFAULTS_VALUE_PREF = "pressure_up_defaults_value_pref";
    public static final int PRESSURE_UP_DEFAULTS_VALUE = 300;//kpa
    public static final String PRESSURE_UP_MIN_VALUE_PREF = "pressure_up_min_value_pref";
    public static final int PRESSURE_UP_MIN_VALUE = 280;
    public static final String PRESSURE_UP_MAX_VALUE_PREF = "pressure_up_max_value_pref";
    public static final int PRESSURE_UP_MAX_VALUE = 640;

    public static final String PRESSURE_CURRENT_SEL_UNIT_PREF = "pressure_current_sel_unit_pref";
    public static final String PRESSURE_UP_CURRENT_SEL_VALUE_PREF = "pressure_up_current_sel_value_pref";//默认保存为kpa

    //压强下限
    public static final String PRESSURE_DOWN_DEFAULTS_VALUE_PREF = "pressure_down_defaults_value_pref";
    public static final int PRESSURE_DOWN_DEFAULTS_VALUE = 200;//200;--备注用于测试--需要修改回来
    public static final String PRESSURE_DOWN_MIN_VALUE_PREF = "pressure_down_min_value_pref";
    public static final int PRESSURE_DOWN_MIN_VALUE = 100;//100;//需要修改
    public static final String PRESSURE_DOWN_MAX_VALUE_PREF = "pressure_down_max_value_pref";
    public static final int PRESSURE_DOWN_MAX_VALUE = 250;
    public static final String PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF = "pressure_wodn_current_sel_value_pref";//默认保存为kpa

    public static final String STATISTICS_TYPE_PREF = "statistics_type_pref";//统计类型

    public static final String NUMBER_PICKER_DEFAULT_PREF = "number_picker_default_pref";//默认值
    public static final int NUMBER_PICKER_DEFAULT_VALUE = 10;//单位为S
    public static final String NUMBER_PICKER_CURRENT_SELECT_PREF = "number_picker_current_select_pref";//当前选择值
    public static final int NUMBER_MAX_VALUE = 100;
    public static final int NUMBER_MIN_VALUE = 5;

    public static final int DAY_STATISTICS = 1;
    public static final int YEAR_STATISTICS = 3;
    public static final int MONTHLY_STATISTICS = 2;


    public static final int NOT_UPGRADED = 0;//未升级
    public static final int UPGRADED_3049_HEX = 1;//未升级
    public static final int UPGRADED_3011_HEX = 2;//未升级
    public static final int UPGRADED_3049_AND_3011_HEX = 3;//均已升级

    public static final int LEFT_FRONT_WHEEL_VALUE = 1;//左前轮
    public static final int RIGHT_FRONT_WHEEL_VALUE = 2;//右前轮
    public static final int LEFT_REAR_WHEEL_VALUE = 3;//左后轮
    public static final int RIGHT_REAR_WHEEL_VALUE = 4;//右后轮

    public static final String LEFT_FRONT_WHEEL = "left front wheel";//左前轮
    public static final String RIGHT_FRONT_WHEEL = "right front wheel";//右前轮
    public static final String LEFT_REAR_WHEEL = "left rear wheel";//左后轮
    public static final String RIGHT_REAR_WHEEL = "right rear wheel";//右后轮

    public static final String NOT_UPGRADED_STR = "not upgraded";//左前轮
    public static final String UPGRADED_3049_STR = "upgraded 3049";//右前轮
    public static final String UPGRADED_3011_STR = "upgraded 3011";//左后轮
    public static final String UPGRADED_3049_AND_3011_STR = "upgraded 3049 and 3011";//右后轮

    public static final int SELECTED = 1;//选中
    public static final int UNSELECTED = 0;//未选中

    public static final int ADD_CAR_STATUS = 1;//添加车辆状态
    public static final int BIND_DEVICE_STATUS = 2;//绑定设备状态
    public static final int MONITOR_DATA_STATUS = 3;//监控数据状态

    public static final String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTER_UUID_NOTIFY = "0000fff6-0000-1000-8000-00805f9b34fb";//读写
    public static final String NOTIFY_UUID = "";

    public static final String DEVICE_NAME = "SNP710A";

    public static final int PAIRED = 1;//已配对
    public static final int UNPAIRED = 2;//未配对

    //用16进制表示
    public static final int CMD_TYPE = 1;//指令类型
    public static final int SEND_FILE_TYPE = 2;//文件类型

    public static final String CMD_HEAD = "AA";//命令行头文件
    public static final String DATA_HEAD = "A0";//数据行头文件
    public static final String CRC8_DEFAULT = "00";//数据行头文件
    public static final String CMD_0 = "00";
    public static final String CMD_1_SEND_FILE = "01";//send file
    public static final String CMD_1_DOWNLOAD = "02";//download
    public static final String CMD_1_ENTER_FACTORY = "03";//entry factory
    public static final String CMD_1_ENTER_OFF = "04";//enter off
    public static final String CMD_1_GET_UUID = "05";//get uuid
    public static final String CMD_1_GET_PTV = "06";//get ptv
    public static final String CMD_1_POWER_OFF = "07";//get ptv
    public static final String PARAM_0 = "00";
    public static final String PARMA_1 = "00";
    public static final String EXTERN_PARMA = "00";
    //版本type
    public static final String BLE_VERSION_TYPE_49 = "49";
    public static final String BLE_VERSION_TYPE_11 = "11";
    public static final String BLE_VERSION_TYPE_UNKNOWN = "UNKNOWN";

    public static final int SEND_DATA = 1;//发送数据
    public static final int SEND_CMD = 2;//发送指令
    public static final int SEND_CMD_UUID = 3;//发送指令
    public static final int SEND_CMD_DOWNLOAD = 4;//发送指令

    public static final String BIND = "bind";//已绑定
    public static final String UN_BIND = "unbind";//已解绑

    public static final int BIND_VALUE = 1;//已绑定
    public static final int UN_BIND_VALUE = 2;//已解绑

    //Ble返回的错误码
    public static final int BLE_ERROR_CODE_102 = 102; //device is not connect

    public static final int BY_DAY = 1;
    public static final int BY_MONTH = 2;
    public static final int BY_YEAR = 3;

    public static final String FILE_SAVE_PATH = "";
}
