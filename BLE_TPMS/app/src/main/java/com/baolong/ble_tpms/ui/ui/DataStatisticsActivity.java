package com.baolong.ble_tpms.ui.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.CarDeviceTripDataBean;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.CarDeviceTripDataTable;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.utils.SharedPreferencesHelper;
import com.baolong.ble_tpms.ui.utils.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataStatisticsActivity extends BaseTitleActivity implements RadioGroup.OnCheckedChangeListener, OnChartGestureListener, OnChartValueSelectedListener {

    private static final String TAG = "DataStatisticsActivity";
    private LineChart lineChartTemp, lineChartPress;
    private CarDeviceTripDataTable carDeviceTripDataTable;
    ArrayList<TripPressureDevice> tripPressureDevices = null;
    private RadioButton rbByMonth, rbByYear, rbByDay;
    private RadioGroup rgStatisticalMethods;
    private TextView tvAverageTemp, tvAveragePress;//平均温度、平均压强
    private TextView tvTempExceedLimitNum, tvPressExceedLimitNum;//温度、压强
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private Map<String, List<CarDeviceTripDataBean>> hashMap = new HashMap<>();
    private int statisticsType = 0;

    private XAxis tempXAxis;                //X轴
    private YAxis tempLeftYAxis;            //左侧Y轴
    private YAxis tempRightYaxis;           //右侧Y轴

    private XAxis pressXAxis;                //X轴
    private YAxis pressLeftYAxis;            //左侧Y轴
    private YAxis pressRightYaxis;           //右侧Y轴
    private Legend tempLegend;              //图例
    private Legend pressLegend;              //图例

    private LimitLine templimitLine;        //限制线

    private LimitLine pressUplimitLine;
    private LimitLine pressLowerlimitLine;        //限制线
    //  private MyMarkerView markerView;    //标记视图 即点击xy轴交点时弹出展示信息的View 需自定义
    private int[] color = {R.color.red, R.color.yellow
            , R.color.blue, R.color.limegreen};

    @Override
    public void init() {
        ActivityManager.getInstance().addActivity(this);
        setTitleAndContentLayoutId("history data statistics", R.layout.activity_data_statistics);
        Intent intent = getIntent();
        tripPressureDevices = new ArrayList<>();
        tripPressureDevices = (ArrayList<TripPressureDevice>) intent.getSerializableExtra("deviceObject");
        initView();
        initData();
    }

    private void initData() {
        //radioGroup onclick
        rgStatisticalMethods.setOnCheckedChangeListener(this);
        queryDeviceData();
        showLineCharTable();
    }

    private void queryDeviceData() {
        hashMap.clear();
        carDeviceTripDataTable = new CarDeviceTripDataTable();
        //testData();
        if (tripPressureDevices != null && tripPressureDevices.size() > 0) {
            for (TripPressureDevice tripPressureDevice : tripPressureDevices) {
                //查询每个设备的数据
                ArrayList<CarDeviceTripDataBean> tripData = carDeviceTripDataTable.queryDataStatistics(tripPressureDevice.getId(), statisticsType, com.baolong.ble_tpms.ui.utils.Utils.getSystemTimeToStatics(statisticsType));
                if (tripData != null && tripData.size() > 0) {
                    hashMap.put(com.baolong.ble_tpms.ui.utils.Utils.tripType(tripPressureDevice.getTripType()), tripData);
                }
            }
        }
    }

    private void testData() {
        ArrayList<CarDeviceTripDataBean> tripData = new ArrayList<>();
        ArrayList<CarDeviceTripDataBean> tripData1 = new ArrayList<>();
        ArrayList<CarDeviceTripDataBean> tripData2 = new ArrayList<>();
        ArrayList<CarDeviceTripDataBean> tripData3 = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
            carDeviceTripDataBean.setTemperature(i);
            carDeviceTripDataBean.setPressure(i);
            carDeviceTripDataBean.setAddDate(Utils.getSystemCurrentTime());
            tripData.add(carDeviceTripDataBean);
        }
        hashMap.put("测试", tripData);
        for (int i = 50; i < 100; i++) {
            CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
            carDeviceTripDataBean.setTemperature(i);
            carDeviceTripDataBean.setPressure(i);
            carDeviceTripDataBean.setAddDate(Utils.getSystemCurrentTime());
            tripData1.add(carDeviceTripDataBean);
        }
        hashMap.put("测试1", tripData1);
        for (int i = 100; i < 150; i++) {
            CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
            carDeviceTripDataBean.setTemperature(i);
            carDeviceTripDataBean.setPressure(i);
            carDeviceTripDataBean.setAddDate(Utils.getSystemCurrentTime());
            tripData2.add(carDeviceTripDataBean);
        }
        hashMap.put("测试2", tripData2);
        for (int i = 150; i < 200; i++) {
            CarDeviceTripDataBean carDeviceTripDataBean = new CarDeviceTripDataBean();
            carDeviceTripDataBean.setTemperature(i);
            carDeviceTripDataBean.setPressure(i);
            carDeviceTripDataBean.setAddDate(Utils.getSystemCurrentTime());
            tripData3.add(carDeviceTripDataBean);
        }
        hashMap.put("测试3", tripData3);
    }

    private void showLineCharTable() {
        if (hashMap != null && hashMap.size() > 0) {
            initTempLineChar(lineChartTemp, "temperature");
            initPressLineChar(lineChartPress, "pressure");

            //showLineChart(hashMap.get("测试"), "测试", Color.RED, lineChartTemp, tempXAxis, tempLeftYAxis, 1);

           String tempKey = null;

            if (hashMap.size() > 0) {
                Iterator<Map.Entry<String, List<CarDeviceTripDataBean>>> iterator = hashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, List<CarDeviceTripDataBean>> entry = iterator.next();
                    tempKey = entry.getKey();
                    break;
                }
            }
            //温度
            showLineChart(hashMap.get(tempKey), tempKey, Color.RED, lineChartTemp,tempXAxis,tempLeftYAxis,1);
            //压强
            showLineChart(hashMap.get(tempKey), tempKey,  Color.RED, lineChartPress,pressXAxis,pressLeftYAxis,2);


//            if (hashMap.size() > 1) {
//                Iterator<Map.Entry<String, List<CarDeviceTripDataBean>>> iterator = hashMap.entrySet().iterator();
//                while (iterator.hasNext()) {
//                    Map.Entry<String, List<CarDeviceTripDataBean>> entry = iterator.next();
//                    int tempColor = 0;
//                    if (entry.getKey().equalsIgnoreCase("测试1")) {
//                        tempColor = Color.YELLOW;
//                        addTempLine(entry.getValue(), entry.getKey(), tempColor,lineChartTemp,1);
//                    } else if (entry.getKey().equalsIgnoreCase("测试2")) {
//                        tempColor = Color.BLUE;
//                        addTempLine(entry.getValue(), entry.getKey(), tempColor,lineChartTemp,1);
//                    } else if (entry.getKey().equalsIgnoreCase("测试3")) {
//                        tempColor = Color.GREEN;
//                        addTempLine(entry.getValue(), entry.getKey(), tempColor,lineChartTemp,1);
//                    }
//                }
//            }


            if (hashMap.size() > 1) {
                Iterator<Map.Entry<String, List<CarDeviceTripDataBean>>> iterator = hashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, List<CarDeviceTripDataBean>> entry = iterator.next();
                    if (!entry.getKey().equalsIgnoreCase(tempKey)) {
                        int tempColor = 0;
                        if (entry.getKey().equalsIgnoreCase(Config.RIGHT_FRONT_WHEEL)) {
                            tempColor = Color.YELLOW;
                        } else if (entry.getKey().equalsIgnoreCase(Config.LEFT_REAR_WHEEL)) {
                            tempColor = Color.BLUE;
                        } else if (entry.getKey().equalsIgnoreCase(Config.RIGHT_REAR_WHEEL)) {
                            tempColor = Color.GREEN;
                        } else if(entry.getKey().equalsIgnoreCase(Config.LEFT_FRONT_WHEEL)){
                            tempColor = Color.BLACK;
                        }
                        addTempLine(entry.getValue(), entry.getKey(), tempColor,lineChartTemp,1);
                        addTempLine(entry.getValue(), entry.getKey(), tempColor,lineChartPress,2);
                        //addLine(entry.getValue(), entry.getKey(), tempColor, 2);
                    }
                }
            }
        }
    }

    private void initView() {
        lineChartTemp = findViewById(R.id.line_char_temp);
        lineChartPress = findViewById(R.id.line_char_press);
        rbByMonth = findViewById(R.id.rb_by_month);
        rbByDay = findViewById(R.id.rb_by_day);
        rbByYear = findViewById(R.id.rb_by_year);
        rgStatisticalMethods = findViewById(R.id.rg_statistical_methods);
        tvAverageTemp = findViewById(R.id.tv_average_temp);
        tvAveragePress = findViewById(R.id.tv_average_press);
        tvTempExceedLimitNum = findViewById(R.id.tv_temp_exceed_limit_num);
        tvPressExceedLimitNum = findViewById(R.id.tv_press_exceed_limit_num);
        mSharedPreferencesHelper = new SharedPreferencesHelper(mContext, mContext.getPackageName());
        statisticsType = (Integer) mSharedPreferencesHelper.getSharedPreference(Config.STATISTICS_TYPE_PREF, Config.DAY_STATISTICS);//默认按天
        if (statisticsType == Config.DAY_STATISTICS) {
            rbByDay.setChecked(true);
            rbByYear.setChecked(false);
            rbByMonth.setChecked(false);
        } else if (statisticsType == Config.YEAR_STATISTICS) {
            rbByDay.setChecked(false);
            rbByYear.setChecked(true);
            rbByMonth.setChecked(false);
        } else if (statisticsType == Config.MONTHLY_STATISTICS) {
            rbByMonth.setChecked(true);
            rbByDay.setChecked(false);
            rbByYear.setChecked(false);
        }
    }

    private void initLineChare(LineChart lineChart, String str) {
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //是否显示边界
        lineChart.setDrawBorders(true);
        //是否可以拖动
        lineChart.setDragEnabled(true);
        //是否有触摸事件
        lineChart.setTouchEnabled(true);
        //设置x,y轴动画效果
        lineChart.animateX(1500);
        lineChart.animateY(2500);
        Description description = new Description();
        description.setText(str);
        lineChart.setDescription(description);
        //lineChart.setBackgroundColor(R.color.white);
        //是否显示边界
        lineChart.setDrawBorders(false);
    }

    //初始化温度曲线图表
    private void initTempLineChar(LineChart lineChart, String str) {
        initLineChare(lineChart, str);

        //XY 轴设置
        tempXAxis = lineChart.getXAxis();
        tempLeftYAxis = lineChart.getAxisLeft();
        tempRightYaxis = lineChart.getAxisRight();
        //X轴设置显示位置在底部
        tempXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        setXAxisValue(tempXAxis);
        tempXAxis.setGranularity(1f);
        //保证Y轴从0开始，不然会上移一点
        tempLeftYAxis.setAxisMinimum(0f);
        tempLeftYAxis.setAxisMaximum(100f);
        //tempLeftYAxis.setLabelCount(9);
        tempLeftYAxis.enableGridDashedLine(10f, 10f, 0f);
        tempRightYaxis.setEnabled(false);
        tempXAxis.setDrawGridLines(false);
        tempLeftYAxis.setDrawGridLines(true);
        tempRightYaxis.setDrawGridLines(true);

        initLegend(lineChart,tempLegend);

        if ((int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0) == 0) {
            setHightLimitLine((float) (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_DEFAULTS_VALUE_PREF, 0), "temperature limit", Color.RED, tempLeftYAxis, lineChartTemp);
        } else {
            setHightLimitLine((float) (int) mSharedPreferencesHelper.getSharedPreference(Config.TEMPERATURE_CURRENT_SEL_VALUE_PREF, 0), "temperature limit", Color.RED, tempLeftYAxis, lineChartTemp);
        }
    }

    private void setXAxisValue(XAxis xAxis) {
        if (rbByDay.isChecked()) {
            xAxis.setAxisMaximum(23f);
            xAxis.setAxisMinimum(0f);
        } else if (rbByMonth.isChecked()) {
            try {
                xAxis.setAxisMaximum(com.baolong.ble_tpms.ui.utils.Utils.currentMonthDayNumber(com.baolong.ble_tpms.ui.utils.Utils.getCurrentMonth()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            xAxis.setAxisMinimum(1f);
        } else if (rbByYear.isChecked()) {
            xAxis.setAxisMaximum(12f);
            xAxis.setAxisMinimum(1f);
        }
    }


    //初始化温度曲线图表
    private void initPressLineChar(LineChart lineChart, String str) {
        initLineChare(lineChart, str);

        //XY 轴设置
        pressXAxis = lineChart.getXAxis();
        pressLeftYAxis = lineChart.getAxisLeft();
        pressLeftYAxis.setAxisMaximum((int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_MAX_VALUE_PREF, 640));
        pressRightYaxis = lineChart.getAxisRight();
        //X轴设置显示位置在底部
        pressXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        setXAxisValue(pressXAxis);
        pressXAxis.setGranularity(1f);
        //保证Y轴从0开始，不然会上移一点
        pressLeftYAxis.setAxisMinimum(0f);
        pressLeftYAxis.enableGridDashedLine(10f, 10f, 0f);
        pressRightYaxis.setEnabled(false);
        pressXAxis.setDrawGridLines(false);
        pressRightYaxis.setDrawGridLines(true);
        pressRightYaxis.setDrawGridLines(true);

        initLegend(lineChart,pressLegend);
//        xAxis.setDrawGridLines(false);
//        rightYaxis.setDrawGridLines(false);
//        leftYAxis.setDrawGridLines(true);


        if ((int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, 0) == 0) {
            setHightLimitLine((float) (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_DEFAULTS_VALUE_PREF, Config.PRESSURE_UP_DEFAULTS_VALUE), "pressure limit", Color.RED, pressLeftYAxis, lineChartPress);
        } else {
            setHightLimitLine((float) (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_UP_CURRENT_SEL_VALUE_PREF, Config.PRESSURE_UP_DEFAULTS_VALUE), "pressure limit", Color.RED, pressLeftYAxis, lineChartPress);
        }

        if ((int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, 0) == 0) {
            setLowLimitLine(lineChartPress, (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_DEFAULTS_VALUE_PREF, Config.PRESSURE_DOWN_DEFAULTS_VALUE), Color.RED, "pressures lower limit", pressLeftYAxis);
        } else {
            setLowLimitLine(lineChartPress, (int) mSharedPreferencesHelper.getSharedPreference(Config.PRESSURE_DOWN_CURRENT_SEL_VALUE_PREF, Config.PRESSURE_DOWN_DEFAULTS_VALUE), Color.RED, "pressures lower limit", pressLeftYAxis);
        }
    }

    private void initLegend(LineChart lineChart,Legend legend) {
        //折线图标签设置
        legend = lineChart.getLegend();
        //开启设置图例
        legend.setEnabled(true);
        //设置显示类型， LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看Legend 即可
        legend.setForm(Legend.LegendForm.CIRCLE);
        //如果设置为true,那么当图例过长或者过多，一行显示不下的时候，就会进行换行
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12f);
        //显示位置 -左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        //是否绘制在图表里面
        legend.setDrawInside(false);
    }

    /**
     * 曲线初始化设置  一个LineDataSet代表一条曲线
     *
     * @param lineDataSet
     * @param color
     * @param mode
     */
    public void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);

        //设置曲线值的原点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);

        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);

        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }

        //用于测试
        initLegend(lineChartTemp,tempLegend);
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(LineChart lineChart, Drawable drawable) {
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            lineChart.invalidate();
        }
    }

    /**
     * 展示曲线
     *
     * @param dataBeans 数据集合
     * @param name      曲线名称
     * @param color     曲线颜色
     */
    public void showLineChart(final List<CarDeviceTripDataBean> dataBeans, String name, int color, LineChart lineChart, XAxis xAxis, YAxis yAxis, final int type) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataBeans.size(); i++) {
            CarDeviceTripDataBean carDeviceTripDataBean = dataBeans.get(i);
            /**         * 在此可查看 Entry构造方法，可发现 可传入数值 Entry(float x, float y)         * 也可传入Drawable， Entry(float x, float y, Drawable icon) 可在XY轴交点 设置Drawable图像展示         */
            Entry entry = null;
            if (type == 1) {
                entry = new Entry(Float.valueOf((Utils.dateDeal(carDeviceTripDataBean.getAddDate(), statisticsType))), (float) carDeviceTripDataBean.getTemperature());
            } else {
                entry = new Entry(Float.valueOf((Utils.dateDeal(carDeviceTripDataBean.getAddDate(), statisticsType))), (float) carDeviceTripDataBean.getPressure());
            }
            entries.add(entry);
        }

        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
//                String tradeDate = dataBeans.get((int) value % dataBeans.size()).getAddDate();
//                return tradeDate.substring(9, 11);//DateUtil.formatDate(tradeDate);
                String tempStr = null;
                if (statisticsType == Config.DAY_STATISTICS) {
                    tempStr = ((int) (value)) + "h";
                } else if (statisticsType == Config.MONTHLY_STATISTICS) {
                    tempStr = String.valueOf(((int) (value)));
                } else if (statisticsType == Config.YEAR_STATISTICS) {
                    tempStr = ((int) (value)) + "m";
                }
                return tempStr;
            }
        });
        xAxis.setLabelCount(24, false);
        if (type == 1) {
            yAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return ((int) (value)) + Config.CELSIUS_UNIT;
                }
            });
            yAxis.setLabelCount(9);//0 - 100
        } else {
            yAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return ((int) (value)) + Config.KPA;
                }
            });
            yAxis.setLabelCount(7);//0 - 100
        }
    }

    /**
     * 设置高限制线
     *
     * @param high
     * @param name
     */
    public void setHightLimitLine(float high, String name, int color, YAxis yAxis, LineChart lineChart) {
        LimitLine hightLimit = new LimitLine(high, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(color);
        hightLimit.setTextColor(color);
        yAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }

    /**
     * 设置低限制线
     *
     * @param low
     * @param name
     */
    public void setLowLimitLine(LineChart lineChart, int low, int color, String name, YAxis yAxis) {
        LimitLine hightLimit = new LimitLine(low, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(color);
        hightLimit.setTextColor(color);
        yAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }


    /**
     * 添加曲线
     */
    private void addTempLine(List<CarDeviceTripDataBean> dataList, String name, int color, LineChart lineChart, int type) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            CarDeviceTripDataBean data = dataList.get(i);
            Entry entry = null;
            if (type == 1) {
                entry = new Entry(Float.valueOf((Utils.dateDeal(data.getAddDate(), statisticsType))), (float) data.getTemperature());
            } else {
                entry = new Entry(Float.valueOf((Utils.dateDeal(data.getAddDate(), statisticsType))), (float) data.getPressure());
            }
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        lineChart.getLineData().addDataSet(lineDataSet);
        lineChart.invalidate();
        lineChart.notifyDataSetChanged();
    }


    @Override
    public View.OnClickListener getBackOnClickLisener() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_by_day:
                Log.i(TAG, "choice day statistics");
                mSharedPreferencesHelper.put(Config.STATISTICS_TYPE_PREF, Config.DAY_STATISTICS);
                //切换统计模式
                statisticsType = Config.DAY_STATISTICS;
                break;
            case R.id.rb_by_year:
                Log.i(TAG, "choice year statistics");
                mSharedPreferencesHelper.put(Config.STATISTICS_TYPE_PREF, Config.YEAR_STATISTICS);
                statisticsType = Config.YEAR_STATISTICS;
                break;
            case R.id.rb_by_month:
                Log.i(TAG, "choice month statistics");
                mSharedPreferencesHelper.put(Config.STATISTICS_TYPE_PREF, Config.MONTHLY_STATISTICS);
                statisticsType = Config.MONTHLY_STATISTICS;
                break;
        }
        queryDeviceData();
        showLineCharTable();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
