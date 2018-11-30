package com.baolong.ble_tpms.ui.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.utils.SharedPreferencesHelper;
import com.baolong.ble_tpms.ui.utils.Utils;

public class SystemSettingsActivity extends BaseTitleActivity implements View.OnClickListener,NumberPicker.OnValueChangeListener,NumberPicker.OnScrollListener,NumberPicker.Formatter {
    private static final String TAG = "SystemSettingsActivity";
    private RadioGroup rgPressureUnit, rgTemperatureUnit;
    private RadioButton rbBar, rbPsi, rbKpa, rbCelsius, rbFahrenheit;
    private SeekBar sbTripWarningLimit, sbTripLowerWarningLimit, sbTemperatureLimit;
    private Button btnResetSystem;
    private TextView tvTripWarningLimit, tvTripLowerWarningLimit, tvTemperatureLimit;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private String tempDefaultUnit;
    private int tempDefaultValue, tempDefaultMinValue, tempDefaultMaxValue;
    private String tempSelUnit;
    private int tempSelValue;
    private String tripPressureDefaultUnit;
    private int tripPressureUpDefaultValue, tripPressureUpDefaultMaxValue, tripPressureUpDefaultMinValue;
    private int tripPressureUpSeltValue = 0;

    private String tripPressureSeltUnit = null;

    private int tripPressureDownDefaultValue, tripPressureDownDefaultMaxValue, tripPressureDownDefaultMinValue;
    private int tripPressureDownSeltValue;

    private double tempSeekBarMaxValue;
    private double tempSeekBatCurrentValue;
    private int currentSeekBar;

    private float tripPressUpSeekBaMaxValue;
    private float tripPressUpSeekBarCurrentValue;
    private float tripPressUpcurrentSeekBar;

    private float tripPressDownSeekBaMaxValue;
    private float tripPressDownSeekBarCurrentValue;
    private float tripPressDowncurrentSeekBar;
    private NumberPicker numberPicker;
    private int numberDefaultValue = 0;
    private int numberCurrentSelectValue = 0;


    @Override
    public void init() {
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId(getResources().getString(R.string.system_settings), R.layout.activity_system_settings);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        rgPressureUnit = findViewById(R.id.rg_pressure_unit);
        rgTemperatureUnit = findViewById(R.id.rg_temperature_unit);
        rbBar = findViewById(R.id.rb_bar);
        rbPsi = findViewById(R.id.rb_psi);
        rbKpa = findViewById(R.id.rb_kpa);
        rbCelsius = findViewById(R.id.rb_celsius);
        rbFahrenheit = findViewById(R.id.rb_fahrenheit);
        sbTripWarningLimit = findViewById(R.id.sb_trip_warning_limit);
        sbTripLowerWarningLimit = findViewById(R.id.sb_trip_lower_warning_limit);
        sbTemperatureLimit = findViewById(R.id.sb_temperature_limit);
        btnResetSystem = findViewById(R.id.btn_reset_system);
        btnResetSystem.setOnClickListener(this);
        tvTripWarningLimit = findViewById(R.id.tv_trip_warning_limit);
        tvTripLowerWarningLimit = findViewById(R.id.tv_trip_lower_warning_limit);
        tvTemperatureLimit = findViewById(R.id.tv_temperature_limit);
        numberPicker = findViewById(R.id.number_picker);

        mSharedPreferencesHelper = new SharedPreferencesHelper(mContext, mContext.getPackageName());
        tempDefaultUnit = (String) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_DEFAULTS_UNIT_PREF, null);
        tempDefaultMaxValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_MAX_VALUE_PREF, 0);
        tempDefaultMinValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_MIN_VALUE_PREF, 0);
        tempDefaultValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_DEFAULTS_VALUE_PREF, 0);

        tripPressureDefaultUnit = (String) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DEFAULTS_UNIT_PREF, null);

        tripPressureUpDefaultValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_DEFAULTS_VALUE_PREF, 0);
        tripPressureUpDefaultMinValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_MIN_VALUE_PREF, 0);
        tripPressureUpDefaultMaxValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_MAX_VALUE_PREF, 0);

        tripPressureDownDefaultValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_DEFAULTS_VALUE_PREF, 0);
        tripPressureDownDefaultMinValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_MIN_VALUE_PREF, 0);
        tripPressureDownDefaultMaxValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_MAX_VALUE_PREF, 0);
        tempSelUnit = (String) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_UNIT_PREF, null);
        tripPressureSeltUnit = (String) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_CURRENT_SEL_UNIT_PREF, null);

        numberDefaultValue = (int)mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_DEFAULT_PREF,0);
        numberCurrentSelectValue = (int)mSharedPreferencesHelper.getSharedPreference(Config.NUMBER_PICKER_CURRENT_SELECT_PREF,0);
        
        setPressUnit(false);
        setTempUnit(false);
        initTempSeekBar();
        initPressSeekBar();
        initUiShow();
        initNumberPicker();
    }

    private void initNumberPicker() {
        numberPicker.setMaxValue(Config.NUMBER_MAX_VALUE);
        numberPicker.setMinValue(Config.NUMBER_MIN_VALUE);
        if(numberCurrentSelectValue == 0){
            numberPicker.setValue(numberDefaultValue);
        }else{
            numberPicker.setValue(numberCurrentSelectValue);
        }
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//不可编辑
        //numberPicker.setFormatter(this);
        numberPicker.setOnValueChangedListener(this);
        numberPicker.setOnScrollListener(this);
    }

    private void initTempSeekBar() {
        tempSelValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0);
        if (rbCelsius.isChecked()) {
            tempSeekBarMaxValue = tempDefaultMaxValue - tempDefaultMinValue;
            if (tempSelValue == 0) {
                tempSeekBatCurrentValue = tempDefaultValue - tempDefaultMinValue;
            } else {
                tempSeekBatCurrentValue = tempSelValue - tempDefaultMinValue;
            }
        } else if (rbFahrenheit.isChecked()) {
            tempSeekBarMaxValue = (int) Utils.celsiusToFahrenheit(tempDefaultMaxValue) - (int) Utils.celsiusToFahrenheit(tempDefaultMinValue);
            if (tempSelValue == 0) {
                tempSeekBatCurrentValue = (int) Utils.celsiusToFahrenheit(tempDefaultValue) - (int) Utils.celsiusToFahrenheit(tempDefaultMinValue);
            } else {
                tempSeekBatCurrentValue = Utils.celsiusToFahrenheit(tempSelValue) - Utils.celsiusToFahrenheit(tempDefaultMinValue);
            }
        }
    }

    private void initPressSeekBar() {
        tripPressureUpSeltValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, 0);
        tripPressureDownSeltValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, 0);

        if (rbBar.isChecked()) {
            tripPressUpSeekBaMaxValue = (float) Utils.kpaToBar(tripPressureUpDefaultMaxValue) - (float)Utils.kpaToBar(tripPressureUpDefaultMinValue);
            if (tripPressureUpSeltValue == 0) {
                tripPressUpSeekBarCurrentValue = (float) Utils.kpaToBar(tripPressureUpDefaultValue) - (float) Utils.kpaToBar(tripPressureUpDefaultMinValue);
            } else {
                tripPressUpSeekBarCurrentValue = (float) Utils.kpaToBar(tripPressureUpSeltValue) - (float) Utils.kpaToBar(tripPressureUpDefaultMinValue);
            }

            tripPressDownSeekBaMaxValue = (float) Utils.kpaToBar(tripPressureDownDefaultMaxValue) - (float) Utils.kpaToBar(tripPressureDownDefaultMinValue);
            if (tripPressureDownSeltValue == 0) {
                tripPressDownSeekBarCurrentValue = (float) Utils.kpaToBar(tripPressureDownDefaultValue) - (float) Utils.kpaToBar(tripPressureDownDefaultMinValue);
            } else {
                tripPressDownSeekBarCurrentValue = (float) Utils.kpaToBar(tripPressureDownSeltValue) - (float) Utils.kpaToBar(tripPressureDownDefaultMinValue);
            }

        } else if (rbKpa.isChecked()) {
            tripPressUpSeekBaMaxValue = tripPressureUpDefaultMaxValue - tripPressureUpDefaultMinValue;
            if (tripPressureUpSeltValue == 0) {
                tripPressUpSeekBarCurrentValue = tripPressureUpDefaultValue - tripPressureUpDefaultMinValue;
            } else {
                tripPressUpSeekBarCurrentValue = tripPressureUpSeltValue - tripPressureUpDefaultMinValue;
            }

            tripPressDownSeekBaMaxValue = tripPressureDownDefaultMaxValue - tripPressureDownDefaultMinValue;
            if (tripPressureDownSeltValue == 0) {
                tripPressDownSeekBarCurrentValue = tripPressureDownDefaultValue - tripPressureDownDefaultMinValue;
            } else {
                tripPressDownSeekBarCurrentValue = tripPressureDownSeltValue - tripPressureDownDefaultMinValue;
            }
        } else if (rbPsi.isChecked()) {
            tripPressUpSeekBaMaxValue = (int) Utils.kpaToPsi(tripPressureUpDefaultMaxValue) - (int) Utils.kpaToPsi(tripPressureUpDefaultMinValue);
            if (tripPressureUpSeltValue == 0) {
                tripPressUpSeekBarCurrentValue = (int) Utils.kpaToPsi(tripPressureUpDefaultValue) - (int) Utils.kpaToPsi(tripPressureUpDefaultMinValue);
            } else {
                tripPressUpSeekBarCurrentValue = (int) Utils.kpaToPsi(tripPressureUpSeltValue) - (int) Utils.kpaToPsi(tripPressureUpDefaultMinValue);
            }

            tripPressDownSeekBaMaxValue = (int) Utils.kpaToPsi(tripPressureDownDefaultMaxValue) - (int) Utils.kpaToPsi(tripPressureDownDefaultMinValue);
            if (tripPressureDownSeltValue == 0) {
                tripPressDownSeekBarCurrentValue = (int) Utils.kpaToPsi(tripPressureDownDefaultValue) - (int) Utils.kpaToPsi(tripPressureDownDefaultMinValue);
            } else {
                tripPressDownSeekBarCurrentValue = (int) Utils.kpaToPsi(tripPressureDownSeltValue) - (int) Utils.kpaToPsi(tripPressureDownDefaultMinValue);
            }
        }
    }

    private void initUiShow() {
        tempLimitDataDeal();
        tvTripPressUpDataDeal();
        tvTripPressDownDataDeal();
        setTempSeekBarInitData();
        setTripPressUpSeekBarInitData();
        setTripPressDownSeekBarInitData();
    }

    private void setPressUnit(boolean isRest) {
        if (isRest) {
            rbKpa.setChecked(true);
            rbPsi.setChecked(false);
            rbBar.setChecked(false);
        } else {
            //设置压强的单位
            if (tripPressureSeltUnit != null) {
                if (tripPressureSeltUnit.equals(Config.PSI)) {
                    rbPsi.setChecked(true);
                    rbBar.setChecked(false);
                    rbKpa.setChecked(false);
                }
                if (tripPressureSeltUnit.equals(Config.BAR)) {
                    rbBar.setChecked(true);
                    rbPsi.setChecked(false);
                    rbKpa.setChecked(false);
                }
                if (tripPressureSeltUnit.equals(Config.KPA)) {
                    rbKpa.setChecked(true);
                    rbPsi.setChecked(false);
                    rbBar.setChecked(false);
                }
            } else {
                rbKpa.setChecked(true);
                rbPsi.setChecked(false);
                rbBar.setChecked(false);
            }
        }
    }

    private void setTempUnit(boolean isRest) {
        if (isRest) {
            rbCelsius.setChecked(true);
            rbFahrenheit.setChecked(false);
        } else {
            //设置温度的单位
            if (tempSelUnit != null) {
                if (tempSelUnit.equals(Config.CELSIUS_UNIT)) {
                    rbCelsius.setChecked(true);
                    rbFahrenheit.setChecked(false);
                }
                if (tempSelUnit.equals(Config.FAHRENHEIT_UNIT)) {
                    rbCelsius.setChecked(false);
                    rbFahrenheit.setChecked(true);
                }
            } else {
                rbCelsius.setChecked(true);
                rbFahrenheit.setChecked(false);
            }
        }
    }

    private void setTempSeekBarInitData() {
        sbTemperatureLimit.setMax((int) tempSeekBarMaxValue);
        sbTemperatureLimit.setProgress((int) tempSeekBatCurrentValue);
    }

    private void setTripPressUpSeekBarInitData() {
        if(rbBar.isChecked()){
            sbTripWarningLimit.setMax((int) (tripPressUpSeekBaMaxValue * 10));
            sbTripWarningLimit.setProgress((int) (tripPressUpSeekBarCurrentValue * 10));
        }else{
            sbTripWarningLimit.setMax((int) tripPressUpSeekBaMaxValue);
            sbTripWarningLimit.setProgress((int) tripPressUpSeekBarCurrentValue);
        }
    }

    private void setTripPressDownSeekBarInitData() {
        if(rbBar.isChecked()){
            sbTripLowerWarningLimit.setMax((int)(tripPressDownSeekBaMaxValue * 10));
            sbTripLowerWarningLimit.setProgress((int)(tripPressDownSeekBarCurrentValue * 10));
        } else {
            sbTripLowerWarningLimit.setMax((int)(tripPressDownSeekBaMaxValue));
            sbTripLowerWarningLimit.setProgress((int)(tripPressDownSeekBarCurrentValue));
        }
    }


    private void tempLimitDataDeal() {
        int showValue = 0;
        String typeUnit = Config.CELSIUS_UNIT;
        if (rbFahrenheit.isChecked()) {
            if (tempSelValue == 0) {
                showValue = (int) Utils.celsiusToFahrenheit(tempDefaultValue);
            } else {
                showValue = (int) Utils.celsiusToFahrenheit(tempSelValue);
            }
            typeUnit = Config.FAHRENHEIT_UNIT;
        } else {
            if (tempSelValue == 0) {
                showValue = tempDefaultValue;
            } else {
                showValue = tempSelValue;
            }
            typeUnit = Config.CELSIUS_UNIT;
        }
        tvTemperatureLimit.setText(String.valueOf(showValue) + typeUnit);
    }

    //设置显示的上限压强的值
    private void tvTripPressUpDataDeal() {
        int showData = 0;
        String typeUint = Config.KPA;
        if (rbPsi.isChecked()) {
            if (tripPressureUpSeltValue == 0) {
                showData = (int) Utils.kpaToPsi(tripPressureUpDefaultValue);
            } else {
                showData = (int) Utils.kpaToPsi(tripPressureUpSeltValue);
            }
            typeUint = Config.PSI;
        } else if (rbKpa.isChecked()) {
            if (tripPressureUpSeltValue == 0) {
                showData = tripPressureUpDefaultValue;
            } else {
                showData = tripPressureUpSeltValue;
            }
            typeUint = Config.KPA;
        } else if (rbBar.isChecked()) {
            if (tripPressureUpSeltValue == 0) {
                showData = (int) Utils.kpaToBar(tripPressureUpDefaultValue);

            } else {
                showData = (int) Utils.kpaToBar(tripPressureUpSeltValue);
            }
            typeUint = Config.BAR;
        }
        tvTripWarningLimit.setText(String.valueOf(showData) + typeUint);
    }

    //设置显示下限的压强的值
    private void tvTripPressDownDataDeal() {
        int showData = 0;
        String typeUnit = Config.KPA;
        if (rbPsi.isChecked()) {
            if (tripPressureDownSeltValue == 0) {
                showData = (int) Utils.kpaToPsi(tripPressureDownDefaultValue);
            } else {
                showData = (int) Utils.kpaToPsi(tripPressureDownSeltValue);
            }
            typeUnit = Config.PSI;
        } else if (rbKpa.isChecked()) {
            if (tripPressureDownSeltValue == 0) {
                showData = tripPressureDownDefaultValue;
            } else {
                showData = tripPressureDownSeltValue;
            }
            typeUnit = Config.KPA;
        } else if (rbBar.isChecked()) {
            if (tripPressureDownSeltValue == 0) {
                showData = (int) Utils.kpaToBar(tripPressureDownDefaultValue);

            } else {
                showData = (int) Utils.kpaToBar(tripPressureDownSeltValue);
            }
            typeUnit = Config.BAR;
        }
        tvTripLowerWarningLimit.setText(String.valueOf(showData) + typeUnit);
    }

    private void initData() {
        rgPressureUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                tripPressureUpSeltValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, 0);
                tripPressureDownSeltValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, 0);
                switch (checkedId) {
                    case R.id.rb_bar:
                        mSharedPreferencesHelper.put(Config.PRESSURE_CURRENT_SEL_UNIT_PREF, Config.BAR);
                        if (tripPressUpcurrentSeekBar == 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureUpDefaultValue)) + Config.BAR);
                        } else if (tripPressUpcurrentSeekBar == 0 && (tripPressureUpSeltValue != 0)) {
                            tvTripWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureUpSeltValue)) + Config.BAR);
                        } else if (tripPressUpcurrentSeekBar != 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressUpcurrentSeekBar + tripPressureUpDefaultMinValue)) + Config.BAR);
                        } else {
                            tvTripWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureUpSeltValue)) + Config.BAR);
                        }

                        if (tripPressDowncurrentSeekBar == 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureDownDefaultValue)) + Config.BAR);
                        } else if (tripPressDowncurrentSeekBar == 0 && (tripPressureDownSeltValue != 0)) {
                            tvTripLowerWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureDownSeltValue)) + Config.BAR);
                        } else if (tripPressDowncurrentSeekBar != 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressDowncurrentSeekBar + tripPressureDownDefaultMinValue)) + Config.BAR);
                        } else {
                            tvTripLowerWarningLimit.setText(String.valueOf((float) Utils.kpaToBar(tripPressureDownSeltValue)) + Config.BAR);
                        }
                        break;
                    case R.id.rb_psi:
                        mSharedPreferencesHelper.put(Config.PRESSURE_CURRENT_SEL_UNIT_PREF, Config.PSI);

                        int  test1 = (int) Utils.kpaToPsi(tripPressureUpDefaultValue);
                        if (tripPressUpcurrentSeekBar == 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureUpDefaultValue)) + Config.PSI);
                        } else if (tripPressUpcurrentSeekBar == 0 && (tripPressureUpSeltValue != 0)) {
                            tvTripWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureUpSeltValue)) + Config.PSI);
                        } else if (tripPressUpcurrentSeekBar != 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressUpcurrentSeekBar + tripPressureUpDefaultMinValue)) + Config.PSI);
                        } else {
                            tvTripWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureUpSeltValue)) + Config.PSI);
                        }

                        int  test2 = (int) Utils.kpaToPsi(tripPressureDownDefaultValue);
                        if (tripPressDowncurrentSeekBar == 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureDownDefaultValue)) + Config.PSI);
                        } else if (tripPressDowncurrentSeekBar == 0 && (tripPressureDownSeltValue != 0)) {
                            tvTripLowerWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureDownSeltValue)) + Config.PSI);
                        } else if (tripPressDowncurrentSeekBar != 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressDowncurrentSeekBar + tripPressureDownDefaultMinValue)) + Config.PSI);
                        } else {
                            tvTripLowerWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureDownSeltValue)) + Config.PSI);
                        }
                        break;
                    case R.id.rb_kpa:
                        mSharedPreferencesHelper.put(Config.PRESSURE_CURRENT_SEL_UNIT_PREF, Config.KPA);

                        if (tripPressUpcurrentSeekBar == 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf(tripPressureUpDefaultValue) + Config.KPA);
                        } else if (tripPressUpcurrentSeekBar == 0 && (tripPressureUpSeltValue != 0)) {
                            tvTripWarningLimit.setText(String.valueOf(tripPressureUpSeltValue) + Config.KPA);
                        } else if (tripPressUpcurrentSeekBar != 0 && tripPressureUpSeltValue == 0) {
                            tvTripWarningLimit.setText(String.valueOf(tripPressUpcurrentSeekBar + tripPressureUpDefaultMinValue) + Config.KPA);
                        } else {
                            tvTripWarningLimit.setText(String.valueOf(tripPressureUpSeltValue) + Config.KPA);
                        }

                        if (tripPressDowncurrentSeekBar == 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf(tripPressureDownDefaultValue) + Config.KPA);
                        } else if (tripPressDowncurrentSeekBar == 0 && (tripPressureDownSeltValue != 0)) {
                            tvTripLowerWarningLimit.setText(String.valueOf(tripPressureDownSeltValue) + Config.KPA);

                        } else if (tripPressDowncurrentSeekBar != 0 && tripPressureDownSeltValue == 0) {
                            tvTripLowerWarningLimit.setText(String.valueOf(tripPressDowncurrentSeekBar + tripPressureDownDefaultMinValue) + Config.KPA);
                        } else {
                            tvTripLowerWarningLimit.setText(String.valueOf(tripPressureDownSeltValue) + Config.KPA);
                        }

                        break;
                }
                initPressSeekBar();
                setTripPressUpSeekBarInitData();
                setTripPressDownSeekBarInitData();
            }
        });

        rgTemperatureUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                tempSelValue = (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0);
                if (checkedId == R.id.rb_celsius) {
                    mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_UNIT_PREF, Config.CELSIUS_UNIT);
                    if (currentSeekBar == 0) {
                        if (tempSelValue == 0) {
                            tvTemperatureLimit.setText(String.valueOf(tempDefaultValue) + Config.CELSIUS_UNIT);
                        } else {
                            tvTemperatureLimit.setText(String.valueOf(tempSelValue) + Config.CELSIUS_UNIT);
                        }
                    } else {
                        if (tempSelValue == 0) {
                            tvTemperatureLimit.setText(String.valueOf(currentSeekBar + tempDefaultMinValue) + Config.CELSIUS_UNIT);
                        } else {
                            //tvTemperatureLimit.setText(String.valueOf(currentSeekBar + tempSelValue) + Config.CELSIUS_UNIT);
                            tvTemperatureLimit.setText(String.valueOf(tempSelValue) + Config.CELSIUS_UNIT);
                        }
                    }
                }
                if (checkedId == R.id.rb_fahrenheit) {
                    mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_UNIT_PREF, Config.FAHRENHEIT_UNIT);
                    if (currentSeekBar == 0) {
                        if (tempSelValue == 0) {
                            tvTemperatureLimit.setText(String.valueOf((int) Utils.celsiusToFahrenheit(tempDefaultValue)) + Config.FAHRENHEIT_UNIT);
                        } else {
                            tvTemperatureLimit.setText(String.valueOf((int) Utils.celsiusToFahrenheit(tempSelValue)) + Config.FAHRENHEIT_UNIT);
                        }
                    } else {
                        if (tempSelValue == 0) {
                            tvTemperatureLimit.setText(String.valueOf((int) (Utils.celsiusToFahrenheit(currentSeekBar + tempDefaultMinValue))) + Config.FAHRENHEIT_UNIT);
                        } else {
                            //tvTemperatureLimit.setText(String.valueOf((int)(Utils.celsiusToFahrenheit(currentSeekBar+tempSelValue))) + Config.FAHRENHEIT_UNIT);
                            tvTemperatureLimit.setText(String.valueOf((int) (Utils.celsiusToFahrenheit(tempSelValue))) + Config.FAHRENHEIT_UNIT);
                        }
                    }
                }
                initTempSeekBar();
                setTempSeekBarInitData();
            }
        });

        //压强上限
        sbTripWarningLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean fromUserT = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, " 压强上限  progress = " + progress);
                fromUserT = fromUser;
                if (fromUser) {
                    if (rbBar.isChecked()) {
                        //Log.i(TAG,"KPAtObAR  = " + Utils.kpaToBar(tripPressureUpDefaultMinValue) + " ; convertProgress = " + Utils.convertProgress(progress));
                        tvTripWarningLimit.setText(Utils.division(Utils.kpaToBar(tripPressureUpDefaultMinValue) + Utils.convertProgress(progress)) + Config.BAR);
                    } else if (rbKpa.isChecked()) {
                        tvTripWarningLimit.setText(String.valueOf((int) tripPressureUpDefaultMinValue + progress) + Config.KPA);
                    } else if (rbPsi.isChecked()) {
                        tvTripWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureUpDefaultMinValue) + progress) + Config.PSI);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "current 压强上限 progress = " + seekBar.getProgress());
                tripPressUpcurrentSeekBar = seekBar.getProgress();
                if (fromUserT) {
                    if (rbBar.isChecked()) {
                        tripPressUpcurrentSeekBar = (float) Utils.convertProgress(seekBar.getProgress());
                        mSharedPreferencesHelper.put(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, (int) Utils.barToKpa(tripPressUpcurrentSeekBar + (int) Utils.kpaToBar(tripPressureUpDefaultMinValue)));
                    } else if (rbPsi.isChecked()) {
                        mSharedPreferencesHelper.put(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, (int) Utils.psiToKpa(tripPressUpcurrentSeekBar + (int) Utils.kpaToPsi(tripPressureUpDefaultMinValue)));
                    } else {
                        mSharedPreferencesHelper.put(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, (int) (tripPressUpcurrentSeekBar + tripPressureUpDefaultMinValue));
                    }
                }
            }
        });

        //压强下限
        sbTripLowerWarningLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean fromUserT = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, " 压强下限  progress = " + progress);
                fromUserT = fromUser;
                if (fromUser) {
                    if (rbBar.isChecked()) {
                        tvTripLowerWarningLimit.setText(Utils.division(Utils.kpaToBar(tripPressureDownDefaultMinValue) + Utils.convertProgress(progress))  + Config.BAR);
                    } else if (rbKpa.isChecked()) {
                        tvTripLowerWarningLimit.setText(String.valueOf(tripPressureDownDefaultMinValue + progress) + Config.KPA);
                    } else if (rbPsi.isChecked()) {
                        tvTripLowerWarningLimit.setText(String.valueOf((int) Utils.kpaToPsi(tripPressureDownDefaultMinValue) + progress) + Config.PSI);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "current 压强上限 progress = " + seekBar.getProgress());
                tripPressDowncurrentSeekBar = seekBar.getProgress();
                //tripPressUpcurrentSeekBar = seekBar.getProgress();
                if (fromUserT) {
                    if (rbBar.isChecked()) {
                        tripPressDowncurrentSeekBar = (float) Utils.convertProgress(seekBar.getProgress());
                        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, (int) Utils.barToKpa(tripPressDowncurrentSeekBar + (int) Utils.kpaToBar(tripPressureDownDefaultMinValue)));
                    } else if (rbPsi.isChecked()) {
                        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, (int) Utils.psiToKpa(tripPressDowncurrentSeekBar + (int) Utils.kpaToPsi(tripPressureDownDefaultMinValue)));
                    } else {
                        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, (int) (tripPressDowncurrentSeekBar + tripPressureDownDefaultMinValue));
                    }
                }
                // mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, tripPressDowncurrentSeekBar + tripPressureDownDefaultMinValue);
            }
        });

        //温度上限
        sbTemperatureLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean fromUserT = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, " 温度 progress = " + progress + " ;fromUser = " + fromUser);
                fromUserT = fromUser;
                if (fromUser) {
                    if (rbCelsius.isChecked()) {
                        tvTemperatureLimit.setText(String.valueOf(progress + tempDefaultMinValue) + Config.CELSIUS_UNIT);
                    } else {
                        tvTemperatureLimit.setText(String.valueOf((int) Utils.celsiusToFahrenheit(tempDefaultMinValue) + progress) + Config.FAHRENHEIT_UNIT);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fromUserT) {
                    Log.i(TAG, "CURRENT progress = " + seekBar.getProgress());
                    currentSeekBar = seekBar.getProgress();
                    //保存当前的选择的值
                    if (rbFahrenheit.isChecked()) {
                        int temp = (int) Utils.celsiusToFahrenheit(tempDefaultMinValue);
                        int temp1 = temp + currentSeekBar;
                        Log.i(TAG, "测试 = " + temp1 + " ;temp = " + temp);
                        mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, (int) Utils.fahrenheitToCelsius(temp1));
                    } else {
                        mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, (int) (currentSeekBar + tempDefaultMinValue));
                    }
                }
            }
        });

    }

    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_system:
                //重置系统设置
                resetSystemSettings();
                break;
            default:
                break;
        }
    }

    //清除已保存的选择的值
    private void resetSystemSettings() {
        new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.confirm))
                .setMessage(getResources().getString(R.string.restore_system_settings))
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //系统设置
                        mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0);
                        mSharedPreferencesHelper.put(Config.TEMPERATURE_CURRENT_SEL_UNIT_PREF, Config.CELSIUS_UNIT);
                        mSharedPreferencesHelper.put(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, 0);
                        mSharedPreferencesHelper.put(Config.PRESSURE_CURRENT_SEL_UNIT_PREF, Config.KPA);
                        mSharedPreferencesHelper.put(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, 0);
                        mSharedPreferencesHelper.put(Config.NUMBER_PICKER_CURRENT_SELECT_PREF,0);
                        currentSeekBar = 0;
                        tripPressUpcurrentSeekBar = 0;
                        tripPressDowncurrentSeekBar = 0;
                        numberCurrentSelectValue = 0;
                        setPressUnit(true);
                        setTempUnit(true);
                        initTempSeekBar();
                        initPressSeekBar();
                        initUiShow();
                        initNumberPicker();
                        Toast.makeText(SystemSettingsActivity.this, getResources().getString(R.string.restroe_success), Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel), null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDerstroy");
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {
        switch (scrollState) {
            case NumberPicker.OnScrollListener.SCROLL_STATE_FLING:
                Log.i(TAG,"后续滑动(飞呀飞，根本停下来)");
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
                Log.i(TAG,"不滑动了");
                mSharedPreferencesHelper.put(Config.NUMBER_PICKER_CURRENT_SELECT_PREF,view.getValue());
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                Log.i(TAG,"滑动中...");
                break;
        }
    }

    @Override
    public String format(int value) {
        return null;
    }
}
