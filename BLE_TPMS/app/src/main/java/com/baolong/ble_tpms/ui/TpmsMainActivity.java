package com.baolong.ble_tpms.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.NavigationMenuItemView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.CarDeviceTripDataBean;
import com.baolong.ble_tpms.ui.bean.CarRecode;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.broadcast.BluetoothBroadcastReceiver;
import com.baolong.ble_tpms.ui.db.CarDeviceTripDataTable;
import com.baolong.ble_tpms.ui.db.CarRecodeTable;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.db.SQLiteManager;
import com.baolong.ble_tpms.ui.db.TpmsDBHelper;
import com.baolong.ble_tpms.ui.db.TripPressureDeviceTable;
import com.baolong.ble_tpms.ui.service.TimeSendCmdService;
import com.baolong.ble_tpms.ui.service.TpmsService;
import com.baolong.ble_tpms.ui.systemtts.SystemTTS;
import com.baolong.ble_tpms.ui.ui.ActivityManager;
import com.baolong.ble_tpms.ui.ui.BindDeviceActivity;
import com.baolong.ble_tpms.ui.ui.BleDeviceActivity;
import com.baolong.ble_tpms.ui.ui.CarManagerActivity;
import com.baolong.ble_tpms.ui.ui.DataStatisticsActivity;
import com.baolong.ble_tpms.ui.ui.DeviceDetailActivity;
import com.baolong.ble_tpms.ui.ui.DeviceUpgradeActivity;
import com.baolong.ble_tpms.ui.ui.HelpActivity;
import com.baolong.ble_tpms.ui.ui.SystemSettingsActivity;
import com.baolong.ble_tpms.ui.ui.UnBindDeviceActivity;
import com.baolong.ble_tpms.ui.utils.BleCmdUtils;
import com.baolong.ble_tpms.ui.utils.BleUtils;
import com.baolong.ble_tpms.ui.utils.CRC16Util;
import com.baolong.ble_tpms.ui.utils.CRC8Util;
import com.baolong.ble_tpms.ui.utils.DataTransformUtils;
import com.baolong.ble_tpms.ui.utils.DialogUtils;
import com.baolong.ble_tpms.ui.utils.PollingUtils;
import com.baolong.ble_tpms.ui.utils.SharedPreferencesHelper;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.baolong.ble_tpms.ui.view.LoadingDialog;
import com.baolong.ble_tpms.ui.view.SendDataDialog;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.ToDoubleBiFunction;
import java.util.zip.Inflater;

import static java.lang.Thread.sleep;

public class TpmsMainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "TpmsMainActivity";
    public static final String action = "com.baolong.ble_tpms.ui.TpmsMainActivity";
    private Context mContext;
    private ImageView imageCar;
    private ImageView mainMenu;
    private ImageView voiceSwitch;
    private boolean isVoiceStatus = false;//语音--播报开启、关闭
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private static final int REQUEST_CODE_CAR_MANAGER = 3;
    private static final int REQUEST_CODE_BIND_DEVICE = 4;
    private static final int REQUEST_CODE_LEFT_FRONT_SCAN_DEVICE = 5;
    private static final int REQUEST_CODE_RIGHT_FRONT_SCAN_DEVICE = 6;
    private static final int REQUEST_CODE_LEFT_REAR_SCAN_DEVICE = 7;
    private static final int REQUEST_CODE_RIGHT_REAR_SCAN_DEVICE = 8;
    private static final int REQUEST_CODE_UNBIND_DEVICE = 9;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private static final String VOICE_SWITCH_PREF = "voice_switch_pref";
    private static final String IS_FIRST_USE_PREF = "is_first_use_pref";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private boolean isFirstUse = true;
    private CarRecodeTable carRecodeTable;
    private TripPressureDeviceTable tripPressureDeviceTable;
    private CarDeviceTripDataTable carDeviceTripDataTable;

    private RelativeLayout rlLeftForntTrip;
    public TextView tvLeftFrontTirePre, tvLeftFrontTirePreUnit, tvLeftFrontTireTemp, tvLeftFrontTireTempUnit, tvLeftFrontTireUnbindDevice;
    private RelativeLayout rlRightFrontTrip;
    private TextView tvRightFrontTirePre, tvRightFrontTirePreUnit, tvRightFrontTireTemp, tvRightFrontTireTempUnit, tvRightFrontTireUnbindDevice;
    private RelativeLayout rlLeftRearWheel;
    private TextView tvLeftRearWheelTemp, tvLeftRearWheelTempUnit, tvLeftRearWheelPre, tvLeftRearWheelPreUnit, tvLeftRearWheelTrieUnbindDevice;
    private RelativeLayout rlRightRearWheel;
    private TextView tvRightRearWheelTemp, tvRightRearWheelTempUnit, tvRightRearWheelPre, tvRightRearWheelPreUnit, tvRightRearWheelTrieUnbindDevice;
    private ArrayList<CarRecode> carRecodes = null;
    private ArrayList<TripPressureDevice> tripPressureDevices = null;
    private int carRecodeId = 0;
    private static final String pleaseAddCar = "click to add vehicle";//请先添加车辆
    private static final String pleaseBindDevice = "please bind device";//请绑定设备
    private static final String BindDeviceLF = "learn FL sensor";
    private static final String BindDeviceRF = "learn FR sensor";
    private static final String BindDeviceLR = "learn RL sensor";
    private static final String BindDeviceRR = "learn RR sensor";
    private int leftFrontTripCurrentStatus = Config.ADD_CAR_STATUS;
    private int rightFrontTripCurrentStatus = Config.ADD_CAR_STATUS;
    private int leftRearTripCurrentStatus = Config.ADD_CAR_STATUS;
    private int rightRearTripCurrentStatus = Config.ADD_CAR_STATUS;
    private FrameLayout flLeftFrontTrip, flRightFrontTrip, flLeftRearWheel, flRightRearWheel;
    private List<BleDevice> bleScanResultList;
    private BluetoothGatt bluetoothGatt = null;
    //public static int[]
    public static int[] showDataLeftFront = null;
    public static int[] showDataLeftRear = null;
    public static int[] showDataRightFront = null;
    public static int[] showDataRightRear = null;
    private MyHandler myHandler;
    private MyThread myThread;
    private ProgressDialog pd;
    private SendDataDialog dialog = null;
    private int mProgress = 0;//下载进度
    private int mMaxProgress;//百分比
    private boolean sendDataStatus = false;
    private int cmdType = 0;
    private BleDevice leftFrontBle, rightFrontBle, leftRealBle, rightRealBle;
    private int leftFrontDeviceId, rightFrontDeviceId, leftRearDeviceId, rightRearDeviceId;
    private static final int SERVICE_DATA_MSG = 1;
    private String tempMacAddress = null;
    private LocalReceiver localReceiver;
    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    private int[] voiceSwitchImg = {
            R.mipmap.ic_open_voice_switch,//打开
            R.mipmap.ic_close_voice_switch//关闭
    };
    private SystemTTS systemTTS;
    private Vibrator vibrator;
    private FloatingActionButton floatingActionButton;
    private boolean isOnresume = false;
    private int unBind = 0;
    private int time = 0;
    private boolean isStartSendCmdService = true;
    private ImageView ivBackLogin;
    //有关服务状态的信息。
    private boolean mShouldUnbind;
    private TextView tv_app_name;
    private TextView tv_lf_sensor_id,tv_rf_sensor_id,tv_lr_sensor_id,tv_rr_sensor_id;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected");
            final TpmsService.LocalBinder binder = (TpmsService.LocalBinder) iBinder;
            binder.setData(tripPressureDevices);
            binder.getMyService().connectBleThread();
            binder.getMyService().setCallBack(new TpmsService.CallBack() {
                @Override
                public void onDataChanged(BleDevice bleDevice, int type) {
                    for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
                        if (tripPressureDevice.getMacAddress().equals(bleDevice.getMac())) {
                            if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
                                leftFrontDeviceId = tripPressureDevice.getId();
                            } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
                                rightFrontDeviceId = tripPressureDevice.getId();
                            } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
                                leftRearDeviceId = tripPressureDevice.getId();
                            } else if (type == Config.RIGHT_REAR_WHEEL_VALUE) {
                                rightRearDeviceId = tripPressureDevice.getId();
                            }
                            //PollingUtils.startPollingService(TpmsMainActivity.this, time, TimeSendCmdService.class, TimeSendCmdService.ACTION);
                        }
                    }
                    openDataNotify(bleDevice, type);
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, int type) {
                    binder.setData(tripPressureDevices);
                    binder.getMyService().connectBleThread();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //透明状态栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_tpms_main);
        ActivityManager.getInstance().addActivity(this);
        mContext = this;
        if (Build.VERSION.SDK_INT < 18) {
            Toast.makeText(mContext, "Only supports Android 4.3 and above!", Toast.LENGTH_LONG).show();
            return;
        }
        initBleDevice();
        //权限检查
        primissionCheck();
        myHandler = new MyHandler(TpmsMainActivity.this);
        initView();
        initData();
        bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
        regiestBroast();
        systemTTS = SystemTTS.getInstance(mContext);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    Intent intentTimeSendCmd = null;

    private void registMoniDataChange() {

        if ((int) mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_CURRENT_SELECT_PREF, 0) == 0) {
            time = (int) mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_DEFAULT_PREF, 0);
        } else {
            time = (int) mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_CURRENT_SELECT_PREF, 0);
        }
        Log.i(TAG, "time = " + time);
        //PollingUtils.startPollingService(this, time, TimeSendCmdService.class, TimeSendCmdService.ACTION);
        intentTimeSendCmd = new Intent(this, TimeSendCmdService.class);
        //startService(intentTimeSendCmd);
        //注册广播
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        filter.addAction("com.baolong.ble_tpms.ui.ui.UnBindDeviceActivity");
        filter.addAction("com.baolong.ble_tpms.ui.ui.CarManagerActivity");
        filter.addAction("com.android.bledeviceactivity.bledisconnect");
        registerReceiver(localReceiver, filter);
    }

    public void writeDataToBle(final BleDevice bleDevice, byte[] bytes, int type){
        Log.i(TAG,"writeDataToBle");
        //my.pauseThread();
        BleManager.getInstance().write(bleDevice, Config.SERVICE_UUID, Config.CHARACTER_UUID_NOTIFY, bytes, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                //发送广播
                Log.i(TAG,"onWriteSuccess bledevice = " + bleDevice.getMac());

                if (isStartSendCmdService) {
                    startService(intentTimeSendCmd);
                }

                Intent intent = new Intent();
                intent.setAction("com.android.bleconnecting");
                intent.putExtra("isConnecting", false);
                sendBroadcast(intent);
                isStartSendCmdService = false;
            }

            @Override
            public void onWriteFailure(BleException exception) {
                Log.i(TAG,"onWriteFailure exception = " + exception.getDescription());
            }
        });
    }


    private void initSQL() {
        carRecodeTable = new CarRecodeTable();
        tripPressureDeviceTable = new TripPressureDeviceTable();
        carDeviceTripDataTable = new CarDeviceTripDataTable();
        SQLiteManager.getInstance().init(mContext);
        SQLiteManager.getInstance().registerTable(carRecodeTable);
        SQLiteManager.getInstance().registerTable(tripPressureDeviceTable);
        SQLiteManager.getInstance().registerTable(carDeviceTripDataTable);
        SQLiteManager.getInstance().open();
    }

    /**
     * 初始化蓝牙设备
     */
    private void initBleDevice() {
        //初始化蓝牙
        BleManager.getInstance().init(getApplication());
        //BleManager.getInstance().enableBluetooth();
        //判断蓝牙是否打开
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        if (BleManager.getInstance().isSupportBle()) {
            if (!BleManager.getInstance().isBlueEnable()) {
                Log.d(TAG, "准备开启蓝牙!");
                BleManager.getInstance().enableBluetooth();
                Log.d(TAG, "蓝牙开启完成!");
            }
        } else {
            Toast.makeText(this, "This device does not support BLE!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void primissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(mContext, "need to open location permissions to search for BLE devices", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }

//            //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
//                        .WRITE_EXTERNAL_STORAGE)) {
//                    //Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
//                }
//                //申请权限
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
//
//            } else {
//                //Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "checkPermission: 已经授权！");
//            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
//            case R.id.home_page:
//                //返回主页面
//                break;
            case R.id.car_manager:
                intent = new Intent();
                intent.setClass(mContext, CarManagerActivity.class);
                startActivity(intent);
                break;
//            case R.id.bind_new_device:
//                intent = new Intent();
//                intent.setClass(mContext, BindDeviceActivity.class);
//                startActivity(intent);
//                break;
            case R.id.syetem_settings:
                intent = new Intent();
                intent.setClass(mContext, SystemSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.upgrade_device:
                intent = new Intent();
                intent.setClass(mContext, DeviceUpgradeActivity.class);
                intent.putExtra("carId", carRecodes.get(0).getId());
                //intent.putExtra("bleDevice", leftFrontBle);
                startActivity(intent);
                break;
//            case R.id.help:
//                intent = new Intent();
//                intent.setClass(mContext, HelpActivity.class);
//                startActivity(intent);
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void initView() {
        tv_app_name = findViewById(R.id.tv_app_name);
        imageCar = (ImageView) findViewById(R.id.image_car);
        mainMenu = (ImageView) findViewById(R.id.main_menu);
        voiceSwitch = (ImageView) findViewById(R.id.voice_switch);
        imageCar.setOnClickListener(this);
        mainMenu.setOnClickListener(this);
        voiceSwitch.setOnClickListener(this);
        mSharedPreferencesHelper = new SharedPreferencesHelper(mContext, mContext.getPackageName());
        navigationView = (NavigationView) findViewById(R.id.nav);
        //获取头部控件
        View headView = navigationView.getHeaderView(0);
        ImageView appIcon = headView.findViewById(R.id.app_icon);
        TextView versionInfo = headView.findViewById(R.id.version_info);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        rlLeftForntTrip = findViewById(R.id.rl_left_fornt_trip);
        rlRightFrontTrip = findViewById(R.id.rl_right_front_trip);
        rlLeftRearWheel = findViewById(R.id.rl_left_rear_wheel);
        rlRightRearWheel = findViewById(R.id.rl_right_rear_wheel);

        tv_lf_sensor_id = findViewById(R.id.tv_lf_sensor_id);
        tv_rf_sensor_id = findViewById(R.id.tv_rf_sensor_id);
        tv_lr_sensor_id = findViewById(R.id.tv_lr_sensor_id);
        tv_rr_sensor_id = findViewById(R.id.tv_rr_sensor_id);

        tvLeftFrontTirePre = findViewById(R.id.tv_left_front_tire_pre);
        tvLeftFrontTirePreUnit = findViewById(R.id.tv_left_front_tire_pre_unit);
        tvLeftFrontTireTemp = findViewById(R.id.tv_left_front_tire_temp);
        tvLeftFrontTireTempUnit = findViewById(R.id.tv_left_front_tire_temp_unit);
        tvLeftFrontTireUnbindDevice = findViewById(R.id.tv_left_front_tire_unbind_device);

        tvRightFrontTirePre = findViewById(R.id.tv_right_front_tire_pre);
        tvRightFrontTirePreUnit = findViewById(R.id.tv_right_front_tire_pre_unit);
        tvRightFrontTireTemp = findViewById(R.id.tv_right_front_tire_temp);
        tvRightFrontTireTempUnit = findViewById(R.id.tv_right_front_tire_temp_unit);
        tvRightFrontTireUnbindDevice = findViewById(R.id.tv_right_front_tire_unbind_device);

        tvLeftRearWheelTemp = findViewById(R.id.tv_left_rear_wheel_temp);
        tvLeftRearWheelTempUnit = findViewById(R.id.tv_left_rear_wheel_temp_unit);
        tvLeftRearWheelPre = findViewById(R.id.tv_left_rear_wheel_trip);
        tvLeftRearWheelPreUnit = findViewById(R.id.tv_left_rear_wheel_temp_unit);
        tvLeftRearWheelTrieUnbindDevice = findViewById(R.id.tv_left_rear_wheel_trip_unbind_device);

        tvRightRearWheelTemp = findViewById(R.id.tv_right_rear_wheel_temp);
        tvRightRearWheelTempUnit = findViewById(R.id.tv_right_rear_wheel_temp_unit);
        tvRightRearWheelPre = findViewById(R.id.tv_right_rear_wheel_pre);
        tvRightRearWheelPreUnit = findViewById(R.id.tv_right_rear_wheel_pre_unit);
        tvRightRearWheelTrieUnbindDevice = findViewById(R.id.tv_right_rear_wheel_unbind_device);
        flLeftFrontTrip = findViewById(R.id.fl_left_front_trip);
        flLeftFrontTrip.setOnClickListener(this);
        flRightFrontTrip = findViewById(R.id.fl_right_front_trip);
        flRightFrontTrip.setOnClickListener(this);
        flLeftRearWheel = findViewById(R.id.fl_left_rear_wheel);
        flLeftRearWheel.setOnClickListener(this);
        flRightRearWheel = findViewById(R.id.fl_right_rear_wheel);
        flRightRearWheel.setOnClickListener(this);
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        ivBackLogin = findViewById(R.id.iv_back_login);
        ivBackLogin.setOnClickListener(this);
        setNavigationViewOnItemSelectedListener();
    }


    public void setNavigationViewOnItemSelectedListener() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.back_home_page:
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.car_manager:
                        intent = new Intent();
                        intent.setClass(mContext, CarManagerActivity.class);
                        startActivity(intent);
                        break;
//                    case R.id.bind_new_device:
//                        intent = new Intent();
//                        intent.setClass(mContext, BindDeviceActivity.class);
//                        startActivity(intent);
//                        break;
                    case R.id.syetem_settings:
                        intent = new Intent();
                        intent.setClass(mContext, SystemSettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.upgrade_device:
                        intent = new Intent();
                        intent.setClass(mContext, DeviceUpgradeActivity.class);
                        if (carRecodes.size() > 0) {
                            intent.putExtra("carId", carRecodes.get(0).getId());
                        }
                        startActivity(intent);
                        break;
//                    case R.id.help:
//                        intent = new Intent();
//                        intent.setClass(mContext, HelpActivity.class);
//                        startActivity(intent);
//                        break;
                    case R.id.exit:
                        //isExit = true;
                        if (drawerLayout.isDrawerOpen(navigationView)) {
                            drawerLayout.closeDrawer(navigationView);
                        }
                        finish();
                        //onDestroy();
                        //ActivityManager.getInstance().exit();
                        break;
                    case R.id.unbind_device:
                        //解除绑定设备
                        intent = new Intent();
                        intent.setClass(mContext, UnBindDeviceActivity.class);
                        if (carRecodes.size() > 0) {
                            intent.putExtra("carId", carRecodes.get(0).getId());
                        }
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        navigationView.setItemIconTintList(null);
    }

    private void initData() {
        //初始化扫描设备
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setServiceUuids(new UUID[]{Config.SERVICE_UUID})  //只扫描制定的服务设备
                .setDeviceName(true, Config.DEVICE_NAME)//只扫描指定广播名的设备，可选
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        isVoiceStatus = (boolean) mSharedPreferencesHelper.getSharedPreference(VOICE_SWITCH_PREF, true);
        isFirstUse = (boolean) mSharedPreferencesHelper.getSharedPreference(IS_FIRST_USE_PREF, true);
        if (isFirstUse) {
            mSharedPreferencesHelper.put(IS_FIRST_USE_PREF, false);
            //初始化设置选项
            initSystemSettingsData();
        }
        registMoniDataChange();
        initSQL();
        queryCarRecodeData();
        queryBindDeviceData(unBind);
        queryBindDeviceTripPreData();
        bleScanResultList = new ArrayList<>();
    }

    private void queryCarRecodeData() {
        //查询设备数据
        //1、查询车辆数据
        //2、查询车辆所绑定的传感器设备，如果无车辆数据提示绑定传感器
        //3、如果没有车辆，提示先添加车辆，绑定车辆数据后，提示绑定传感器数据
        carRecodes = new ArrayList<>();
        CarRecodeTable carRecodeTable = new CarRecodeTable();
        carRecodes.clear();
        carRecodes = carRecodeTable.queryCurrentSelectCarData();
        if (carRecodes.size() > 0) {
            //有车辆数据，查询绑定设备
            carRecodeId = carRecodes.get(0).getId();
            setAppName();
            setBindDevice();
        } else {
            setAddCar();
        }
    }

    private void setAppName(){
        tv_app_name.setText(getResources().getString(R.string.app_name) + "-" + carRecodes.get(0).getCarRemark());
    }

    private void setAddCar() {
        //无车辆数据-请先添加车辆数据
        setTvShowText(pleaseAddCar,0);
        setRlHideOrShow(View.GONE);
        leftFrontTripCurrentStatus = Config.ADD_CAR_STATUS;
        rightFrontTripCurrentStatus = Config.ADD_CAR_STATUS;
        leftRearTripCurrentStatus = Config.ADD_CAR_STATUS;
        rightRearTripCurrentStatus = Config.ADD_CAR_STATUS;
    }

    private void insertTripPreDevice(TripPressureDevice tripPressureDevice) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TripPressureDeviceTable.carName, tripPressureDevice.getCarName());
        contentValues.put(TripPressureDeviceTable.carRecordId, tripPressureDevice.getCarRecodeId());
        contentValues.put(TripPressureDeviceTable.deviceName, tripPressureDevice.getDeviceName());
        contentValues.put(TripPressureDeviceTable.macAddress, tripPressureDevice.getMacAddress());
        contentValues.put(TripPressureDeviceTable.pairStatus, tripPressureDevice.getPairStatus());
        contentValues.put(TripPressureDeviceTable.isUpgrade, tripPressureDevice.getIsUpgrade());
        contentValues.put(TripPressureDeviceTable.tripType, tripPressureDevice.getTripType());
        contentValues.put(TripPressureDeviceTable.tripDataId, tripPressureDevice.getTripDataId());
        tripPressureDeviceTable.insert(contentValues);
    }

    private void queryBindDeviceData(int unBind) {
        if (carRecodeId != 0) {
            tripPressureDevices = tripPressureDeviceTable.queryDataByCarRecodeId(carRecodeId);
            if (tripPressureDevices.size() > 0) {
                for (int i = 0; i < tripPressureDevices.size(); i++) {
                    setShowMonitiorData(tripPressureDevices.get(i).getTripType(), unBind);
                }
                bindTpmsService();
            } else {
                setBindDevice();
            }
        }
    }

    public void bindTpmsService() {
        Intent intentTpmsService = new Intent();
        intentTpmsService.setClass(mContext, TpmsService.class);
        intentTpmsService.putExtra("tripPreDevices", ((Serializable) tripPressureDevices));
        if (bindService(intentTpmsService, serviceConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.i(TAG, "error:  The requested service doesn't "
                    + "exist, or this client isn't allowed access to it.");
        }
    }

    private void setShowMonitiorData(int type, int whereUnbind) {
        if (whereUnbind == 0) {
            if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
                tvLeftFrontTireUnbindDevice.setVisibility(View.GONE);
                rlLeftForntTrip.setVisibility(View.VISIBLE);
                leftFrontTripCurrentStatus = Config.MONITOR_DATA_STATUS;
            } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
                tvRightFrontTireUnbindDevice.setVisibility(View.GONE);
                rlRightFrontTrip.setVisibility(View.VISIBLE);
                rightFrontTripCurrentStatus = Config.MONITOR_DATA_STATUS;
            } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
                tvLeftRearWheelTrieUnbindDevice.setVisibility(View.GONE);
                rlLeftRearWheel.setVisibility(View.VISIBLE);
                leftRearTripCurrentStatus = Config.MONITOR_DATA_STATUS;
            } else if (type == Config.RIGHT_REAR_WHEEL_VALUE) {
                tvRightRearWheelTrieUnbindDevice.setVisibility(View.GONE);
                rlRightRearWheel.setVisibility(View.VISIBLE);
                rightRearTripCurrentStatus = Config.MONITOR_DATA_STATUS;
            }
        } else {
            if (whereUnbind == Config.LEFT_FRONT_WHEEL_VALUE) {
                tvLeftFrontTireUnbindDevice.setVisibility(View.VISIBLE);
                rlLeftForntTrip.setVisibility(View.GONE);
                leftFrontTripCurrentStatus = Config.BIND_DEVICE_STATUS;
            } else if (whereUnbind == Config.RIGHT_FRONT_WHEEL_VALUE) {
                tvRightFrontTireUnbindDevice.setVisibility(View.VISIBLE);
                rlRightFrontTrip.setVisibility(View.GONE);
                rightFrontTripCurrentStatus = Config.BIND_DEVICE_STATUS;
            } else if (whereUnbind == Config.LEFT_REAR_WHEEL_VALUE) {
                tvLeftRearWheelTrieUnbindDevice.setVisibility(View.VISIBLE);
                rlLeftRearWheel.setVisibility(View.GONE);
                leftRearTripCurrentStatus = Config.BIND_DEVICE_STATUS;
            } else if (whereUnbind == Config.RIGHT_REAR_WHEEL_VALUE) {
                tvRightRearWheelTrieUnbindDevice.setVisibility(View.VISIBLE);
                rlRightRearWheel.setVisibility(View.GONE);
                rightRearTripCurrentStatus = Config.BIND_DEVICE_STATUS;
            }
            unBind = 0;
        }
    }

    private void setBindDevice() {
        leftFrontTripCurrentStatus = Config.BIND_DEVICE_STATUS;
        rightFrontTripCurrentStatus = Config.BIND_DEVICE_STATUS;
        leftRearTripCurrentStatus = Config.BIND_DEVICE_STATUS;
        rightRearTripCurrentStatus = Config.BIND_DEVICE_STATUS;
        setTvShowText(pleaseBindDevice,1);
        setRlHideOrShow(View.GONE);
        //setFlBackgroundColor();
        unBind = 0;
    }

    private void setTvHideOrShow(int status) {
        tvLeftFrontTireUnbindDevice.setVisibility(status);
        tvRightFrontTireUnbindDevice.setVisibility(status);
        tvLeftRearWheelTrieUnbindDevice.setVisibility(status);
        tvRightRearWheelTrieUnbindDevice.setVisibility(status);
    }

    private void setRlHideOrShow(int status) {
        rlLeftForntTrip.setVisibility(status);
        rlRightFrontTrip.setVisibility(status);
        rlLeftRearWheel.setVisibility(status);
        rlRightRearWheel.setVisibility(status);

    }

    private void setFlBackgroundColor(int type) {
        //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
        if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
            flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
        } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
            flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
        } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
            flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
        } else if (type == Config.RIGHT_REAR_WHEEL_VALUE) {
            flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
        }
    }

    private void setTvShowText(String str,int type) {
        tvLeftFrontTireUnbindDevice.setVisibility(View.VISIBLE);
        tvRightFrontTireUnbindDevice.setVisibility(View.VISIBLE);
        tvLeftRearWheelTrieUnbindDevice.setVisibility(View.VISIBLE);
        tvRightRearWheelTrieUnbindDevice.setVisibility(View.VISIBLE);
        if(type ==1){
            tvLeftFrontTireUnbindDevice.setText(BindDeviceLF);
            tvRightFrontTireUnbindDevice.setText(BindDeviceRF);
            tvLeftRearWheelTrieUnbindDevice.setText(BindDeviceLR);
            tvRightRearWheelTrieUnbindDevice.setText(BindDeviceRR);
        } else {
            tvLeftFrontTireUnbindDevice.setText(str);
            tvRightFrontTireUnbindDevice.setText(str);
            tvLeftRearWheelTrieUnbindDevice.setText(str);
            tvRightRearWheelTrieUnbindDevice.setText(str);
        }

    }

    private void queryBindDeviceTripPreData() {
        CarDeviceTripDataTable carDeviceTripDataTable = new CarDeviceTripDataTable();
        if (tripPressureDevices != null) {
            for (int i = 0; i < tripPressureDevices.size(); i++) {
                ArrayList<CarDeviceTripDataBean> carDeviceTripDataBeanArrayList = new ArrayList<>();
                carDeviceTripDataBeanArrayList = carDeviceTripDataTable.queryDataByDeviceId(tripPressureDevices.get(i).getId(), true);
                if (carDeviceTripDataBeanArrayList.size() > 0) {
                    if (tripPressureDevices.get(i).getTripType() == Config.LEFT_FRONT_WHEEL_VALUE) {
                        if (BleManager.getInstance().isConnected(tripPressureDevices.get(i).getMacAddress())) {
                            tvLeftFrontTirePre.setText(String.valueOf((int) carDeviceTripDataBeanArrayList.get(0).getPressure()));
                            tvLeftFrontTireTemp.setText(String.valueOf(carDeviceTripDataBeanArrayList.get(0).getTemperature()));
                        }

                    }
                    if (tripPressureDevices.get(i).getTripType() == Config.RIGHT_FRONT_WHEEL_VALUE) {
                        if (BleManager.getInstance().isConnected(tripPressureDevices.get(i).getMacAddress())) {
                            tvRightFrontTirePre.setText(String.valueOf((int) carDeviceTripDataBeanArrayList.get(0).getPressure()));
                            tvRightFrontTireTemp.setText(String.valueOf(carDeviceTripDataBeanArrayList.get(0).getTemperature()));
                        }

                    }
                    if (tripPressureDevices.get(i).getTripType() == Config.LEFT_REAR_WHEEL_VALUE) {
                        if (BleManager.getInstance().isConnected(tripPressureDevices.get(i).getMacAddress())) {
                            tvLeftRearWheelPre.setText(String.valueOf((int) carDeviceTripDataBeanArrayList.get(0).getPressure()));
                            tvLeftRearWheelTemp.setText(String.valueOf(carDeviceTripDataBeanArrayList.get(0).getTemperature()));
                        }

                    }
                    if (tripPressureDevices.get(i).getTripType() == Config.RIGHT_REAR_WHEEL_VALUE) {
                        if (BleManager.getInstance().isConnected(tripPressureDevices.get(i).getMacAddress())) {
                            tvRightRearWheelPre.setText(String.valueOf((int) carDeviceTripDataBeanArrayList.get(0).getPressure()));
                            tvRightRearWheelTemp.setText(String.valueOf(carDeviceTripDataBeanArrayList.get(0).getTemperature()));
                        }
                    }
                }
            }
        }
    }

    //初始化系统设置项
    private void initSystemSettingsData() {
        //温度
        mSharedPreferencesHelper.put(Config.TEMPERATURE_DEFAULTS_UNIT_PREF, Config.CELSIUS_UNIT);
        mSharedPreferencesHelper.put(Config.TEMPERATURE_DEFAULTS_VALUE_PREF, Config.TEMPERATURE_DEFAULTS_VALUE);
        mSharedPreferencesHelper.put(Config.TEMPERATURE_MIN_VALUE_PREF, Config.TEMPERATURE_MIN_VALUE);
        mSharedPreferencesHelper.put(Config.TEMPERATURE_MAX_VALUE_PREF, Config.TEMPERATURE_MAX_VALUE);
        //压强上限
        mSharedPreferencesHelper.put(Config.PRESSURE_DEFAULTS_UNIT_PREF, Config.KPA);
        mSharedPreferencesHelper.put(Config.PRESSURE_UP_DEFAULTS_VALUE_PREF, Config.PRESSURE_UP_DEFAULTS_VALUE);
        mSharedPreferencesHelper.put(Config.PRESSURE_UP_MIN_VALUE_PREF, Config.PRESSURE_UP_MIN_VALUE);
        mSharedPreferencesHelper.put(Config.PRESSURE_UP_MAX_VALUE_PREF, Config.PRESSURE_UP_MAX_VALUE);
        //压强下限
        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_DEFAULTS_VALUE_PREF, Config.PRESSURE_DOWN_DEFAULTS_VALUE);
        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_MIN_VALUE_PREF, Config.PRESSURE_DOWN_MIN_VALUE);
        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_MAX_VALUE_PREF, Config.PRESSURE_DOWN_MAX_VALUE);

        //请求频率数字选择
        mSharedPreferencesHelper.put(Config.NUMBER_PICKER_DEFAULT_PREF, Config.NUMBER_PICKER_DEFAULT_VALUE);
    }

//    //获取设备信息
//    private void getGattServiceAndChar() {
//        //获取信息
//        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
//        for (BluetoothGattService service : serviceList) {
//            UUID uuid_service = service.getUuid();
//
//            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristicList) {
//                UUID uuid_chara = characteristic.getUuid();
//                Log.d(TAG, "characteristic.getUuid() = " + characteristic.getUuid());
//            }
//        }
//    }

    //打开数据变化通知
    private void openDataNotify(final BleDevice ble, final int type) {
        final int tempType = -1;
        setBledevice(ble, type);
        openNotify(ble, type);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    sleep(500);
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public void openNotify(final BleDevice ble, final int type) {
        Log.i(TAG, "openNotify ble = " + ble.getMac());
        BleManager.getInstance().notify(
                ble,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Toast.makeText(mContext, Utils.tripType(type) + " open BLE device notify success!", Toast.LENGTH_LONG).show();
//                        cmdType = Config.SEND_CMD;
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    sleep(500);
//
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
                        //bleDeviceWrite(BleCmdUtils.getPtvCmd(), type, ble);
                        //获取GETUUID
                        writeDataToBle(ble,DataTransformUtils.hex2byte(BleCmdUtils.getUUIDCmd()),type);


                        //PollingUtils.startPollingService(TpmsMainActivity.this, time, TimeSendCmdService.class, TimeSendCmdService.ACTION);
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Toast.makeText(mContext, Utils.tripType(type) + " open BLE device notify fail!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, Utils.tripType(type) + " 打开通知失败,异常信息为：" + exception.toString());
                        //尝试重新打开
                        //openDataNotify();
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        String strHex = DataTransformUtils.byte2hex(data);
                        Log.i(TAG, Utils.tripType(type) + " 获取到的通知数据 data =" + strHex + " ; 设备为： " + ble.getMac() + " type = " + type);
                        byteToHexStr(data, type);
                        myThread = new MyThread(myHandler, judgeNotifySource(ble));
                        myThread.start();
                    }
                });
    }

    public int judgeNotifySource(BleDevice bleDevice) {
        int type = 0;
        if (leftFrontBle != null && leftFrontBle.getMac().equalsIgnoreCase(bleDevice.getMac())) {
            type = Config.LEFT_FRONT_WHEEL_VALUE;
        }
        if (rightFrontBle != null && rightFrontBle.getMac().equalsIgnoreCase(bleDevice.getMac())) {
            type = Config.RIGHT_FRONT_WHEEL_VALUE;
        }
        if (leftRealBle != null && leftRealBle.getMac().equalsIgnoreCase(bleDevice.getMac())) {
            type = Config.LEFT_REAR_WHEEL_VALUE;
        }
        if (rightRealBle != null && rightRealBle.getMac().equalsIgnoreCase(bleDevice.getMac())) {
            type = Config.RIGHT_REAR_WHEEL_VALUE;
        }
        return type;
    }

    private void setBledevice(BleDevice ble, int type) {
        if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
            leftFrontBle = ble;
        } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
            rightFrontBle = ble;
        } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
            leftRealBle = ble;
        } else {
            rightRealBle = ble;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //PollingUtils.startPollingService(this, time, TimeSendCmdService.class, TimeSendCmdService.ACTION);
    }

    private boolean isExit = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        for (BleDevice bleDevice : BleManager.getInstance().getAllConnectedDevice()) {
            BleManager.getInstance().stopNotify(bleDevice, Config.SERVICE_UUID, Config.CHARACTER_UUID_NOTIFY);
        }
        BleManager.getInstance().disconnectAllDevice();
        myHandler.removeCallbacks(myThread);   //清除runnable对应的message
        //PollingUtils.stopPollingService(this, TimeSendCmdService.class, TimeSendCmdService.ACTION);
        stopService(intentTimeSendCmd);
        doUnbindService();
        //unbindService(this);
        if (localReceiver != null) {
            unregisterReceiver(localReceiver);
        }
        if (bluetoothBroadcastReceiver != null) {
            unregisterReceiver(bluetoothBroadcastReceiver);
        }
        if (systemTTS != null) {
            systemTTS.stopSpeak();
        }
        SQLiteManager.getInstance().close();
        if (isExit) {
            ActivityManager.getInstance().exit();
        }
    }

    private void doUnbindService() {
        if (mShouldUnbind) {
            unbindService(serviceConnection);
            mShouldUnbind = false;
        }
    }

    private String tripTypeName = "tripTypeName";
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_car:
                break;
            case R.id.main_menu:
                //进入到侧滑界面
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
                break;
            case R.id.voice_switch:
                if (isVoiceStatus) {
                    //关闭
                    mSharedPreferencesHelper.put(VOICE_SWITCH_PREF, false);
                    voiceSwitch.setImageDrawable(getResources().getDrawable(voiceSwitchImg[1], null));
                    isVoiceStatus = false;
                    Toast.makeText(mContext, "Voice alert is off!", Toast.LENGTH_LONG).show();
                } else {
                    //开启
                    mSharedPreferencesHelper.put(VOICE_SWITCH_PREF, true);
                    voiceSwitch.setImageDrawable(getResources().getDrawable(voiceSwitchImg[0], null));
                    isVoiceStatus = true;
                    Toast.makeText(mContext, "Voice alarm is turned on!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.fl_left_front_trip:
                if (leftFrontTripCurrentStatus == Config.ADD_CAR_STATUS) {
                    //跳转到车辆添加界面
                    Intent intent = new Intent();
                    intent.putExtra("main", true);
                    intent.setClass(mContext, CarManagerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CAR_MANAGER);
                } else if (leftFrontTripCurrentStatus == Config.BIND_DEVICE_STATUS || leftFrontTripCurrentStatus == Config.MONITOR_DATA_STATUS) {
                    //点击开始匹配
                    Intent intent = new Intent();
                    intent.setClass(mContext, BleDeviceActivity.class);
                    intent.putExtra("tirePosition", Config.LEFT_FRONT_WHEEL_VALUE);
                    intent.putExtra("carName", carRecodes.get(0).getCarName());
                    intent.putExtra("carRecodeId", carRecodes.get(0).getId());
                    intent.putExtra(tripTypeName,BindDeviceLF);
                    startActivityForResult(intent, REQUEST_CODE_LEFT_FRONT_SCAN_DEVICE);
                }
                break;
            case R.id.fl_right_front_trip:
                //sendCmdEntryOffModel(Config.RIGHT_FRONT_WHEEL_VALUE);
                if (rightFrontTripCurrentStatus == Config.ADD_CAR_STATUS) {
                    //跳转到车辆添加界面
                    Intent intent = new Intent();
                    intent.putExtra("main", true);
                    intent.setClass(mContext, CarManagerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CAR_MANAGER);
                } else if (rightFrontTripCurrentStatus == Config.BIND_DEVICE_STATUS || rightFrontTripCurrentStatus == Config.MONITOR_DATA_STATUS) {
                    //点击开始匹配
                    Intent intent = new Intent();
                    intent.setClass(mContext, BleDeviceActivity.class);
                    intent.putExtra("tirePosition", Config.RIGHT_FRONT_WHEEL_VALUE);
                    intent.putExtra("carName", carRecodes.get(0).getCarName());
                    intent.putExtra("carRecodeId", carRecodes.get(0).getId());
                    intent.putExtra(tripTypeName,BindDeviceRF);
                    startActivityForResult(intent, REQUEST_CODE_RIGHT_FRONT_SCAN_DEVICE);
                }
                break;
            case R.id.fl_left_rear_wheel:
                //sendCmdEntryFactoryModel(Config.LEFT_REAR_WHEEL_VALUE);
                if (leftRearTripCurrentStatus == Config.ADD_CAR_STATUS) {
                    //跳转到车辆添加界面
                    Intent intent = new Intent();
                    intent.putExtra("main", true);
                    intent.setClass(mContext, CarManagerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CAR_MANAGER);
                } else if (leftRearTripCurrentStatus == Config.BIND_DEVICE_STATUS || leftRearTripCurrentStatus == Config.MONITOR_DATA_STATUS) {
                    //点击开始匹配
                    Intent intent = new Intent();
                    intent.setClass(mContext, BleDeviceActivity.class);
                    intent.putExtra("tirePosition", Config.LEFT_REAR_WHEEL_VALUE);
                    intent.putExtra("carName", carRecodes.get(0).getCarName());
                    intent.putExtra("carRecodeId", carRecodes.get(0).getId());
                    intent.putExtra(tripTypeName,BindDeviceLR);
                    startActivityForResult(intent, REQUEST_CODE_LEFT_REAR_SCAN_DEVICE);
                }
                break;
            case R.id.fl_right_rear_wheel:
                if (rightRearTripCurrentStatus == Config.ADD_CAR_STATUS) {
                    //跳转到车辆添加界面
                    Intent intent = new Intent();
                    intent.putExtra("main", true);
                    intent.setClass(mContext, CarManagerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CAR_MANAGER);
                } else if (rightRearTripCurrentStatus == Config.BIND_DEVICE_STATUS || rightRearTripCurrentStatus == Config.MONITOR_DATA_STATUS) {
                    //点击开始匹配
                    Intent intent = new Intent();
                    intent.setClass(mContext, BleDeviceActivity.class);
                    intent.putExtra("tirePosition", Config.RIGHT_REAR_WHEEL_VALUE);
                    intent.putExtra("carName", carRecodes.get(0).getCarName());
                    intent.putExtra("carRecodeId", carRecodes.get(0).getId());
                    intent.putExtra(tripTypeName,BindDeviceRR);
                    startActivityForResult(intent, REQUEST_CODE_RIGHT_REAR_SCAN_DEVICE);
                }
                break;
            case R.id.fab:
                //Toast.makeText(mContext, "悬浮菜单", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(mContext, DataStatisticsActivity.class);
                intent.putExtra("deviceObject", tripPressureDevices);
                startActivity(intent);
                break;
            case R.id.iv_back_login:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
                if (Utils.isLocaltionEnable(mContext)) {//位置开关是打开的，可以开始扫描了
                    //Toast.makeText(mContext, "可以开始Ble设备扫描了", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "位置开关已经打开！");
                } else {//设置打开位置服务
                    setLocationService();
                }
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
                Toast.makeText(mContext, getResources().getString(R.string.location_permission_no_open), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (Utils.isLocaltionEnable(mContext)) {
                //定位已经打开,可以开启Ble设备扫描
                //Toast.makeText(mContext, "定位开关已打开，可以进行Ble设备扫描了", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "location switch is open!");
            } else {
                //定位未打开，请求打开位置
                Toast.makeText(mContext, getResources().getString(R.string.location_switch_no_open), Toast.LENGTH_LONG).show();
                Log.i(TAG, "location switch no open!");
            }
        } else if (requestCode == REQUEST_CODE_CAR_MANAGER && resultCode == RESULT_OK) {
            queryCarRecodeData();
        } else if (requestCode == REQUEST_CODE_BIND_DEVICE && resultCode == RESULT_OK) {
            //设置提示信息
            int type = data.getIntExtra(Config.MATCH_TYPE, Config.MANUAL_MATCH);
            if (type == Config.MANUAL_MATCH) {
                tvLeftFrontTireUnbindDevice.setText("请输入轮胎编号");
            } else {
                tvLeftFrontTireUnbindDevice.setText("点击自动匹配");
            }
        } else if (requestCode == REQUEST_CODE_LEFT_FRONT_SCAN_DEVICE && resultCode == RESULT_OK) {
            if (data != null) {
                Log.i(TAG, "left_front_trip_onActivity_type");
                BleDevice tempBle = (BleDevice) data.getParcelableExtra("bleDevice");
                if (tempBle != null) {
                    leftFrontBle = tempBle;
                    leftFrontDeviceId = data.getIntExtra("deviceId", 0);
                    int type = data.getIntExtra("type", 0);
                    setShowMonitiorData(type, unBind);
                    Log.i(TAG, "unBind = " + unBind);
                    openDataNotify(leftFrontBle, Config.LEFT_FRONT_WHEEL_VALUE);
                } else {

                }
            }
        } else if (requestCode == REQUEST_CODE_RIGHT_FRONT_SCAN_DEVICE && resultCode == RESULT_OK) {
            if (data != null) {
                BleDevice tempBle = (BleDevice) data.getParcelableExtra("bleDevice");
                if (tempBle != null) {
                    Log.i(TAG, "right_front_trip_onActivity_type");
                    rightFrontBle = (BleDevice) data.getParcelableExtra("bleDevice");
                    int type = data.getIntExtra("type", 0);
                    rightFrontDeviceId = data.getIntExtra("deviceId", 0);
                    setShowMonitiorData(type, unBind);
                    openDataNotify(rightFrontBle, Config.RIGHT_FRONT_WHEEL_VALUE);
                } else {
                    Log.i(TAG, "not connect device");
                }
            }
        } else if (requestCode == REQUEST_CODE_LEFT_REAR_SCAN_DEVICE && resultCode == RESULT_OK) {
            if (data != null) {
                BleDevice tempBle = (BleDevice) data.getParcelableExtra("bleDevice");
                if (tempBle != null) {
                    Log.i(TAG, "left_rear_trip_onActivity_type");
                    leftRealBle = (BleDevice) data.getParcelableExtra("bleDevice");
                    int type = data.getIntExtra("type", 0);
                    leftRearDeviceId = data.getIntExtra("deviceId", 0);
                    setShowMonitiorData(type, unBind);
                    openDataNotify(leftRealBle, Config.LEFT_REAR_WHEEL_VALUE);
                } else {
                    Log.i(TAG, "not connect device");
                }
            }
        } else if (requestCode == REQUEST_CODE_RIGHT_REAR_SCAN_DEVICE && resultCode == RESULT_OK) {
            if (data != null) {
                BleDevice tempBle = (BleDevice) data.getParcelableExtra("bleDevice");
                if (tempBle != null) {
                    Log.i(TAG, "right_rear_trip_onActivity_type");
                    rightRealBle = (BleDevice) data.getParcelableExtra("bleDevice");
                    int type = data.getIntExtra("type", 0);
                    rightRearDeviceId = data.getIntExtra("deviceId", 0);
                    setShowMonitiorData(type, unBind);
                    openDataNotify(rightRealBle, Config.RIGHT_REAR_WHEEL_VALUE);
                } else {
                    Log.i(TAG, "not connect device");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void  getUUIDCmd(){
        BleCmdUtils.getUUIDCmd();
    }

    private String sensorIdLf,sensorIdRf,sensorIdLr,sensorIdRr;
    private boolean sensorIdLfBol,sensorIdRfBol,sensorIdLrBol,sensorIdRrBol;
    //解析返回的数据
    public void byteToHexStr(byte[] data, int type) {
        String strHexData = DataTransformUtils.byte2hex(data);
        if (Utils.judgeDataCheckDigit(data)) {
            if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_GET_PTV)) {
                //数据校验正确-开始解析数据
                //响应的数据格式 如下：AA
                // AA8D800600000056B04800000000000000000000
                //数据格式解析  AA:固定值  8D:crc校验位 80:cmd0 06:cmd1 00:param[0] param[1]:00/其它 00:压力减小 56:电量 B0:温度 48:压强 00000000000无效位
                String cmd1 = strHexData.substring(6, 8);//判断是什么指令
                int[] tempDatas = DataTransformUtils.stringIntercept(Config.CMD_1_GET_PTV, strHexData);
                int devideId = 0;
                if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr LEFT_FRONT_WHEEL_VALUE");
                    showDataLeftFront = tempDatas;
                    devideId = leftFrontDeviceId;
                } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr RIGHT_FRONT_WHEEL_VALUE");
                    showDataRightFront = tempDatas;
                    devideId = rightFrontDeviceId;
                } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr LEFT_REAR_WHEEL_VALUE");
                    showDataLeftRear = tempDatas;
                    devideId = leftRearDeviceId;
                } else if (type == Config.RIGHT_REAR_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr RIGHT_REAR_WHEEL_VALUE");
                    showDataRightRear = tempDatas;
                    devideId = rightRearDeviceId;
                }
                //保存胎压数据到数据库
                //carDeviceTripDataTable.insert();
                insertDeviceTripData(contrastTripData(tempDatas, devideId));
            } else if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_DOWNLOAD)) {
                //判断返回的数据是否成功
                //发送数据给升级设备界面
                int[] tempDatas = DataTransformUtils.stringIntercept(Config.CMD_1_DOWNLOAD, strHexData);
                if (tempDatas[0] == 0) {
                    //成功
                    Log.i(TAG, "donwload指令发送成功!");
                    //发送广播，等待4秒中去连接连蓝牙设备
                    //发送广播--停止获取数据跟新
                    Intent intent = new Intent();
                    intent.putExtra("cmdType", Config.CMD_1_DOWNLOAD);
                    intent.setAction("com.android.tpmsmainActivity.connectDevice");
                    sendBroadcast(intent);
                }
            } else if (Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_SEND_FILE)) {
                Intent intent = new Intent();
                intent.putExtra("notifyData", strHexData);
                intent.putExtra("cmdType", Config.CMD_1_SEND_FILE);
                intent.setAction("com.android.tpmsmainActivity.connectDevice");
                sendBroadcast(intent);
            } else if(Utils.judgeCmdType(data).equalsIgnoreCase(Config.CMD_1_GET_UUID)){
                //解析sensorId
                //AAEB80050000497321DC00000000000000000000
                String strSensorId = strHexData.substring(12,20);
                if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr LEFT_FRONT_WHEEL_VALUE");
                    sensorIdLfBol = true;
                    sensorIdLf = strSensorId;
                } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr RIGHT_FRONT_WHEEL_VALUE");
                    sensorIdRf = strSensorId;
                    sensorIdRfBol = true;
                } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr LEFT_REAR_WHEEL_VALUE");
                    sensorIdLr = strSensorId;
                    sensorIdLrBol = true;
                } else if (type == Config.RIGHT_REAR_WHEEL_VALUE) {
                    Log.i(TAG, "byteToHexStr RIGHT_REAR_WHEEL_VALUE");
                    sensorIdRr = strSensorId;
                    sensorIdRrBol = true;
                }
            }
        } else {
            //数据校验失败
            Log.i(TAG, "crc8 数据校验失败!");
        }
    }

    private CarDeviceTripDataBean contrastTripData(int[] datas, int deviceId) {
        CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
        carDeviceTripDataBean.setTripPressureDeviceId(deviceId);
        carDeviceTripDataBean.setTemperature(DataTransformUtils.tempProcess(datas[1]));
        carDeviceTripDataBean.setPressure(DataTransformUtils.pressureProcess(datas[0]));
        carDeviceTripDataBean.setAddDate(Utils.getSystemCurrentTime());
        return carDeviceTripDataBean;
    }

    private void insertDeviceTripData(CarDeviceTripDataBean carDeviceTripDataBean) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarDeviceTripDataTable.tripPressureDeviceId, carDeviceTripDataBean.getTripPressureDeviceId());
        contentValues.put(CarDeviceTripDataTable.temperature, carDeviceTripDataBean.getTemperature());
        contentValues.put(CarDeviceTripDataTable.pressure, carDeviceTripDataBean.getPressure());
        contentValues.put(CarDeviceTripDataTable.addDate, carDeviceTripDataBean.getAddDate());
        long insertDataId = carDeviceTripDataTable.insert(contentValues);
        Log.i(TAG, "insertDeviceTripData ID = " + insertDataId + " ; PressureDeviceId = : " + carDeviceTripDataBean.getTripPressureDeviceId());
    }


    //byte[] test = new byte[]{0x00,0x9c,0x06,0x00,0x00,0x00,0x00};
    //String str = "AA BB 00 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    byte[] byteTemp = {};

    //    定期发送心跳包数据
    private void bleDeviceDataWrite(String str, final int type) {
        BleDevice bleDevice = null;
        if (type == Config.LEFT_FRONT_WHEEL_VALUE) {
            //更新
            bleDevice = leftFrontBle;
        } else if (type == Config.RIGHT_FRONT_WHEEL_VALUE) {
            bleDevice = rightFrontBle;
        } else if (type == Config.LEFT_REAR_WHEEL_VALUE) {
            bleDevice = leftRealBle;
        } else {
            bleDevice = rightRealBle;
        }
        bleDeviceWrite(str, type, bleDevice);
    }

    private void bleDeviceWrite(String str, final int type, BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Config.SERVICE_UUID,
                Config.CHARACTER_UUID_NOTIFY,
                DataTransformUtils.hex2byte(str),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        //通过写成功的数据计算CRC8 ，写成功的数据CRC位置为00
                        //Toast.makeText(mContext, "写数据成功 " + DataTransformUtils.byte2hex(justWrite), Toast.LENGTH_LONG).show();
                        Toast.makeText(mContext, "write data success", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "写数据成功 " + DataTransformUtils.byte2hex(justWrite) + "; current = " + current + " ;total= " + total);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i(TAG, "发送命令数据失败,异常为 ： " + exception.toString());
                        // 发送数据到设备失败
                    }
                });
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//        Log.i(TAG, "onServiceConnected");
//        final TpmsService.LocalBinder binder = (TpmsService.LocalBinder) service;
//        binder.setData(tripPressureDevices);
//        binder.getMyService().connectBleThread();
//        binder.getMyService().setCallBack(new TpmsService.CallBack() {
//            @Override
//            public void onDataChanged(BleDevice bleDevice, int type) {
//                for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
//                    if (tripPressureDevice.getMacAddress().equals(bleDevice.getMac())) {
//                        if(type == Config.LEFT_FRONT_WHEEL_VALUE){
//                            leftFrontDeviceId = tripPressureDevice.getId();
//                        } else if(type == Config.RIGHT_FRONT_WHEEL_VALUE){
//                            rightFrontDeviceId = tripPressureDevice.getId();
//                        } else if(type == Config.LEFT_REAR_WHEEL_VALUE){
//                            leftRearDeviceId = tripPressureDevice.getId();
//                        } else if(type == Config.RIGHT_REAR_WHEEL_VALUE){
//                            rightRearDeviceId = tripPressureDevice.getId();
//                        }
//                        //PollingUtils.startPollingService(TpmsMainActivity.this, time, TimeSendCmdService.class, TimeSendCmdService.ACTION);
//                    }
//                }
//                openDataNotify(bleDevice, type);
//            }
//
//            @Override
//            public void onConnectFail(BleDevice bleDevice, int type) {
//                binder.setData(tripPressureDevices);
//                binder.getMyService().connectBleThread();
//            }
//        });
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//
//    }

    private boolean leftFrontRedTemp = false;
    private boolean leftFrontRedUpPre = false;
    private boolean leftFrontRedDownPre = false;

    private boolean rightFrontRedTemp = false;
    private boolean rightFrontRedUpPre = false;
    private boolean rightFrontRedDownPre = false;

    private boolean leftRearRedTemp = false;
    private boolean leftRearRedUpPre = false;
    private boolean leftRearRedDownPre = false;

    private boolean rightRearRedTemp = false;
    private boolean rightRearRedUpPre = false;
    private boolean rightRearRedDownPre = false;

    /**
     * 为避免handler造成的内存泄漏
     * 1、使用静态的handler，对外部类不保持对象的引用
     * 2、但Handler需要与Activity通信，所以需要增加一个对Activity的弱引用
     */
    private class MyHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        MyHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TpmsMainActivity activity = (TpmsMainActivity) mActivityReference.get();  //获取弱引用队列中的activity
            int currentSelTemp = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0);
            int defaultTemp = Config.TEMPERATURE_DEFAULTS_VALUE;
            int defaultUpPre = Config.PRESSURE_UP_DEFAULTS_VALUE;
            int defaultDownPre = Config.PRESSURE_DOWN_DEFAULTS_VALUE;
            int currentSelUpPre = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, 0);
            int currentSelDownPre = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, 0);
            switch (msg.what) {    //获取消息，更新UI
                case Config.LEFT_FRONT_WHEEL_VALUE:
                    if (showDataLeftFront != null && showDataLeftFront.length > 0) {
                        int temp = DataTransformUtils.tempProcess(showDataLeftFront[1]);//温度
                        int press = (int) DataTransformUtils.pressureProcess(showDataLeftFront[0]);
                        if (currentSelTemp != 0) {
                            if (temp > currentSelTemp) {
                                leftFrontRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.left_front_temp_limit));//The left front wheel temperature has exceeded the set warning value, please note! 左前轮温度已超过设置的预警值，请注意！
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedTemp = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (temp > defaultTemp) {
                                leftFrontRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.left_front_temp_limit));
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedTemp = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (currentSelUpPre != 0) {
                            if (press > currentSelUpPre) {
                                leftFrontRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.left_front_press_hight));
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedUpPre = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press > defaultUpPre) {
                                leftFrontRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.left_front_press_hight));
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedUpPre = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (currentSelDownPre != 0) {
                            if (press < currentSelDownPre) {
                                leftFrontRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.left_front_press_low));
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedDownPre = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press < defaultDownPre) {
                                leftFrontRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.left_front_press_low));
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftFrontRedDownPre = false;
                                //flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (leftFrontRedDownPre || leftFrontRedTemp || leftFrontRedUpPre) {
                            flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                        } else {
                            flLeftFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                        }

//                        tvLeftFrontTireTemp.setVisibility(View.VISIBLE);
//                        tvLeftFrontTirePre.setVisibility(View.VISIBLE);
//                        tvLeftFrontTireTempUnit.setVisibility(View.VISIBLE);
//                        tvLeftFrontTirePreUnit.setVisibility(View.VISIBLE);
                        tvLeftFrontTireTemp.setText(String.valueOf(temp));
                        //int pre = (int) DataTransformUtils.pressureProcess(showDataLeftFront[0]);//压强
                        tvLeftFrontTirePre.setText(String.valueOf(press));
                    }
                    if(sensorIdLfBol){
                        tv_lf_sensor_id.setText("ID:"+sensorIdLf);
                        sensorIdLfBol = false;
                    }
                    break;
                case Config.RIGHT_FRONT_WHEEL_VALUE:
                    if (showDataRightFront != null && showDataRightFront.length > 0) {
                        int temp = DataTransformUtils.tempProcess(showDataRightFront[1]);//温度
                        if (currentSelTemp != 0) {
                            if (temp > currentSelTemp) {
                                rightFrontRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.right_front_temp_limit));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedTemp = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (temp > defaultTemp) {
                                rightFrontRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.right_front_temp_limit));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedTemp = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }
                        int press = (int) DataTransformUtils.pressureProcess(showDataRightFront[0]);
                        if (currentSelUpPre != 0) {
                            if (press > currentSelUpPre) {
                                rightFrontRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.right_front_press_hight));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedUpPre = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press > defaultUpPre) {
                                rightFrontRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.right_front_press_hight));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedUpPre = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (currentSelDownPre != 0) {
                            if (press < currentSelDownPre) {
                                rightFrontRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.right_front_press_low));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedDownPre = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press < defaultDownPre) {
                                rightFrontRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.right_front_press_low));
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightFrontRedDownPre = false;
                                //flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }
                        if (rightFrontRedDownPre || rightFrontRedUpPre || rightFrontRedTemp) {
                            flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.red));
                        } else {
                            flRightFrontTrip.setBackgroundColor(getResources().getColor(R.color.black));
                        }
                        tvRightFrontTireTemp.setText(String.valueOf(temp));
                        tvRightFrontTirePre.setText(String.valueOf(press));
                    }
                    if(sensorIdRfBol){
                        tv_rf_sensor_id.setText("ID:"+sensorIdRf);
                        sensorIdRfBol = false;
                    }
                    break;
                case Config.LEFT_REAR_WHEEL_VALUE:
                    if (showDataLeftRear != null && showDataLeftRear.length > 0) {
                        int temp = DataTransformUtils.tempProcess(showDataLeftRear[1]);//温度
                        if (currentSelTemp != 0) {
                            if (temp > currentSelTemp) {
                                leftRearRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.left_rear_temp_limit));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedTemp = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (temp > defaultTemp) {
                                leftRearRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.left_rear_temp_limit));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedTemp = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }
                        int press = (int) DataTransformUtils.pressureProcess(showDataLeftRear[0]);
                        if (currentSelUpPre != 0) {
                            if (press > currentSelUpPre) {
                                leftRearRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.left_rear_press_hight));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedUpPre = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press > defaultUpPre) {
                                leftRearRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.left_rear_press_hight));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedUpPre = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (currentSelDownPre != 0) {
                            if (press < currentSelDownPre) {
                                leftRearRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.left_rear_press_low));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedDownPre = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press < defaultDownPre) {
                                leftRearRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.left_rear_press_low));
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                leftRearRedDownPre = false;
                                //flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (leftRearRedTemp || leftRearRedDownPre || leftRearRedUpPre) {
                            flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                        } else {
                            flLeftRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                        }
                        tvLeftRearWheelTemp.setText(String.valueOf(temp));
                        tvLeftRearWheelPre.setText(String.valueOf(press));
                    }
                    if(sensorIdLrBol){
                        tv_lr_sensor_id.setText("ID:"+sensorIdLr);
                        sensorIdLrBol = false;
                    }
                    break;
                case Config.RIGHT_REAR_WHEEL_VALUE:
                    if (showDataRightRear != null && showDataRightRear.length > 0) {
                        int temp = DataTransformUtils.tempProcess(showDataRightRear[1]);//温度
                        if (currentSelTemp != 0) {
                            if (temp > currentSelTemp) {
                                rightRearRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.right_rear_temp_limit));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedTemp = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (temp > defaultTemp) {
                                rightRearRedTemp = true;
                                //温度超过上限
                                dialogWarning(getResources().getString(R.string.right_rear_temp_limit));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedTemp = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }
                        int press = (int) DataTransformUtils.pressureProcess(showDataRightRear[0]);
                        if (currentSelUpPre != 0) {
                            if (press > currentSelUpPre) {
                                rightRearRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.right_rear_press_hight));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedUpPre = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press > defaultUpPre) {
                                rightRearRedUpPre = true;
                                dialogWarning(getResources().getString(R.string.right_rear_press_hight));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedUpPre = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }

                        if (currentSelDownPre != 0) {
                            if (press < currentSelDownPre) {
                                rightRearRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.right_rear_press_low));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedDownPre = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } else {
                            if (press < defaultDownPre) {
                                rightRearRedDownPre = true;
                                dialogWarning(getResources().getString(R.string.right_rear_press_low));
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                rightRearRedDownPre = false;
                                //flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        }
                        if (rightRearRedTemp || rightRearRedUpPre || rightRearRedDownPre) {
                            flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.red));
                        } else {
                            flRightRearWheel.setBackgroundColor(getResources().getColor(R.color.black));
                        }

                        tvRightRearWheelTemp.setText(String.valueOf(temp));
                        tvRightRearWheelPre.setText(String.valueOf(press));
                    }
                    if(sensorIdRrBol){
                        tv_rr_sensor_id.setText("ID:"+sensorIdRr);
                        sensorIdRrBol = false;
                    }
                    break;
            }
        }
    }

    public class MyThread extends Thread {
        private Handler handler;
        private int type = 0;

        public MyThread(Handler h, int t) {
            handler = h;
            type = t;
        }

        @Override
        public void run() {
            super.run();
            //run方法
            Message message = Message.obtain();
            message.what = type;
            handler.sendMessage(message);
        }
    }

    //内部类，实现BroadcastReceiver
    public class LocalReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals(action)) {
                tempMacAddress = intent.getStringExtra("mac");
                Log.i(TAG, "接收到sendCmdService 广播 + mac = " + tempMacAddress);
            } else if (intentAction.equalsIgnoreCase("com.baolong.ble_tpms.ui.ui.UnBindDeviceActivity")) {
                unBind = intent.getIntExtra("type", 0);
                Log.i(TAG, "RECEIVE com.baolong.ble_tpms.ui.ui.UnBindDeviceActivity unBind =" + unBind);
                //重新查询数据
                setFlBackgroundColor(unBind);
                queryBindDeviceData(unBind);
            } else if (intentAction.equalsIgnoreCase("com.baolong.ble_tpms.ui.ui.CarManagerActivity")) {
                queryCarRecodeData();
            } else if (intentAction.equalsIgnoreCase("com.android.bledeviceactivity.bledisconnect")) {
                //请求连接设备
                BleDevice bleDevice = intent.getParcelableExtra("bledevice");
                queryBindDeviceData(unBind);
                //intent1.setAction(TpmsService.class);
                //startService();
            }
        }
    }

    //注册蓝牙状态监听广播
    private void regiestBroast() {
        IntentFilter connectedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver, connectedFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void dialogWarning(String info) {
        if ((boolean) mSharedPreferencesHelper.getSharedPreference(VOICE_SWITCH_PREF, true)) {
            systemTTS.playText(info);
        }
        DialogUtils.showExitDialog07(mContext, info);
    }
}

