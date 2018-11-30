package com.baolong.ble_tpms.ui.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.db.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean isLocationPermissionOpen(Context context) {

        return false;
    }

    /**
     * Location service if enable
     *
     * @param context
     * @return location is enable if return true , otherwise disable.
     */
    public static final boolean isLocaltionEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean netWorkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (netWorkProvider || gpsProvider) return true;
        return false;
    }

    /**
     * 摄氏度转换为华氏度
     *
     * @param celsius
     * @return
     */
    public static double celsiusToFahrenheit(double celsius) {
        double c = ((9.0 / 5) * celsius + 32);
        return new BigDecimal(c).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 华氏度转换为摄氏度
     *
     * @param fahrenhei
     * @return
     */
    public static double fahrenheitToCelsius(double fahrenhei) {
        double c = (fahrenhei - 32) * 5 / 9;
        return new BigDecimal(c).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * psi to mpa
     *
     * @param psi
     * @return
     */
    public static double psiToKpa(double psi) {
        double m = psi * 6.894757;
        return new BigDecimal(m).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double psiToBar(double psi) {
        double m = psi * 0.0689476;
        return new BigDecimal(m).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double kpaToPsi(double kpa) {
        double k = kpa * 0.1450377;
        return new BigDecimal(k).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double kpaToBar(double kpa) {
        double k = kpa * 0.01;
        return new BigDecimal(k).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double barToPsi(double bar) {
        double b = bar * 14.5037744;
        return new BigDecimal(b).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double barToKpa(double bar) {
        double b = bar * 100;
        return new BigDecimal(b).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String tripType(int typeId) {
        String tripTypeName = null;
        switch (typeId) {
            case Config.LEFT_FRONT_WHEEL_VALUE:
                tripTypeName = Config.LEFT_FRONT_WHEEL;
                break;
            case Config.RIGHT_FRONT_WHEEL_VALUE:
                tripTypeName = Config.RIGHT_FRONT_WHEEL;
                break;
            case Config.LEFT_REAR_WHEEL_VALUE:
                tripTypeName = Config.LEFT_REAR_WHEEL;
                break;
            case Config.RIGHT_REAR_WHEEL_VALUE:
                tripTypeName = Config.RIGHT_REAR_WHEEL;
                break;
        }
        return tripTypeName;
    }

    public static String upgradeStatusToString(int upgradeStatus) {
        String upgradeStatusName = null;
        switch (upgradeStatus) {
            case Config.NOT_UPGRADED:
                upgradeStatusName = Config.NOT_UPGRADED_STR;
                break;
            case Config.UPGRADED_3049_HEX:
                upgradeStatusName = Config.UPGRADED_3049_STR;
                break;
            case Config.UPGRADED_3011_HEX:
                upgradeStatusName = Config.UPGRADED_3011_STR;
                break;
            case Config.UPGRADED_3049_AND_3011_HEX:
                upgradeStatusName = Config.UPGRADED_3049_AND_3011_STR;
                break;
        }
        return upgradeStatusName;
    }

    //byte[]转换为16进制字符串
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if ((src == null) || (src.length <= 0)) {
            return null;
        }
        for (int i = 0; i < src.length; ++i) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] insertLocationData(byte[] bytes, byte b, int index) {
        byte[] newArray = new byte[bytes.length + 1];
        for (int i = 0; i < bytes.length; i++) {
            newArray[i] = bytes[i];
        }
        for (int i = newArray.length - 1; i > index; i--) {
            newArray[i] = newArray[i - 1];
        }
        newArray[index] = b;
        return newArray;
    }

    public static final String cmdSplice(int type) {
        String headStr = null;
        if (type == Config.CMD_TYPE) {
            headStr = Config.CMD_HEAD;
        } else {
            headStr = Config.DATA_HEAD;
        }
        return null;
    }

//    public static InputStream String ReadResRawData(int type,Context context) throws IOException {
//        InputStream is = getInputStream(type,context);
//
//    }


    //读取文件的长度
    public static final int CacluHexBytes(int type, Context context) throws IOException {
        InputStream is = getInputStream(type, context);
        return is.available();
    }

    public static InputStream getInputStream(int type, Context context) {
        if (type == Config.UPGRADED_3049_HEX) {
            InputStream is = context.getResources().openRawResource(R.raw.senasic_app_snp709_qy3049);
            return is;
        } else {
            InputStream is = context.getResources().openRawResource(R.raw.senasic_app_snp709_qy3011);
            return is;
        }
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date getDate(String str) {
        try {
            java.text.SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(str);
            return date;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static String getSystemCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String getSystemTimeToStatics(int type) {
        SimpleDateFormat simpleDateFormat = null;
        if (type == Config.DAY_STATISTICS) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        } else if (type == Config.MONTHLY_STATISTICS) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        } else if (type == Config.YEAR_STATISTICS) {
            simpleDateFormat = new SimpleDateFormat("yyyy");
        }
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static double convertProgress(int intVal) {
        double result;
        //这里/10 是因为前面每一个数都扩大10倍，因此这里需要/10还原
        result = intVal / 10f;
        return new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String division(double f) {
        DecimalFormat df = new DecimalFormat("0.0");
        String result = df.format(f);
        return result;
    }

    //判断数据的校验位
    public static final boolean judgeDataCheckDigit(byte[] data) {
        String strHexData = DataTransformUtils.byte2hex(data);
        //获取字符串CRC校验位
        String crcStr = strHexData.substring(2, 4);
        strHexData = strHexData.replaceFirst(strHexData.substring(2, 4), "00");
        String crc8Str1 = CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(strHexData));
        Log.i(TAG, "crcStr = " + crcStr + " ; crcStr1 = " + crc8Str1);
        return crc8Str1.equalsIgnoreCase(crcStr) ? true : false;
    }

    //判断是什么指令
    public static final String judgeCmdType(byte[] data) {
        String strHexData = DataTransformUtils.byte2hex(data);
        String cmd1 = strHexData.substring(6, 8);//判断是什么指令
        return cmd1;
    }

    /**
     * 判断当前月有多少天
     * @param YYMM
     * @return
     * @throws ParseException
     */
    public static int currentMonthDayNumber(String YYMM) throws ParseException {
        String strDate = YYMM;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar calendar = new GregorianCalendar();
        Date date = sdf.parse(strDate);
        calendar.setTime(date);
        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static String getCurrentMonth(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String dateDeal(String str,int type){
        Date d1 = null;//定义起始日期
        try {
            d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
        SimpleDateFormat sdf2= new SimpleDateFormat("dd");
        SimpleDateFormat sdf3= new SimpleDateFormat("HH");
        SimpleDateFormat sdf4= new SimpleDateFormat("mm");
        SimpleDateFormat sdf5= new SimpleDateFormat("ss");
        String str1 = null;
        if(type == Config.DAY_STATISTICS){
            str1 = sdf3.format(d1);
        } else if(type == Config.MONTHLY_STATISTICS){
            str1 = sdf2.format(d1);
        } else if(type == Config.YEAR_STATISTICS){
            str1 = sdf1.format(d1);
        }
        return str1;
    }

    static String formType = "yyyy-MM-dd HH:mm:ss";
    public static Date longToDate(long tagergetTime) throws ParseException {
        Date dateOld = new Date(tagergetTime);
        String strDate = dateToString(dateOld,formType);
        Date date = stringToDate(strDate,formType);
        return date;
    }

    public static String longToString(long tagerTime){
        Date date = new Date(tagerTime);
        String strDate = dateToString(date,formType);
        return strDate;
    }

    public static Date stringToDate(String strDate,String formType) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formType);
        Date date = format.parse(strDate);
        return date;
    }

    public static String dateToString(Date date,String formType){
        return new SimpleDateFormat(formType).format(date);

    }
}
