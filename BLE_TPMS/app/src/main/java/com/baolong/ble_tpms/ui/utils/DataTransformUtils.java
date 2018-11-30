package com.baolong.ble_tpms.ui.utils;

import android.content.Context;
import android.util.Log;

import com.baolong.ble_tpms.ui.db.Config;
import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.MultipleBluetoothController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransformUtils {
    private static final String TAG = "DataTransformUtils";

    /**
     * 数字字符串转ASCII码字符串
     *
     * @param
     * @return ASCII字符串
     */
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }

    /**
     * 十六进制转字符串
     *
     * @param hexString  十六进制字符串
     * @param encodeType 编码类型4：Unicode，2：普通编码
     * @return 字符串
     */
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) DataTransformUtils.hexStringToAlgorism(hexString
                    .substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }

    /**
     * 十六进制字符串装十进制
     *
     * @param hex 十六进制字符串
     * @return 十进制数值
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十六转二进制
     *
     * @param hex 十六进制字符串
     * @return 二进制字符串
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result += "0000";
                    break;
                case '1':
                    result += "0001";
                    break;
                case '2':
                    result += "0010";
                    break;
                case '3':
                    result += "0011";
                    break;
                case '4':
                    result += "0100";
                    break;
                case '5':
                    result += "0101";
                    break;
                case '6':
                    result += "0110";
                    break;
                case '7':
                    result += "0111";
                    break;
                case '8':
                    result += "1000";
                    break;
                case '9':
                    result += "1001";
                    break;
                case 'A':
                    result += "1010";
                    break;
                case 'B':
                    result += "1011";
                    break;
                case 'C':
                    result += "1100";
                    break;
                case 'D':
                    result += "1101";
                    break;
                case 'E':
                    result += "1110";
                    break;
                case 'F':
                    result += "1111";
                    break;
            }
        }
        return result;
    }

    /**
     * ASCII码字符串转数字字符串
     *
     * @return 字符串
     */
    public static String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * 将十进制转换为指定长度的十六进制字符串
     *
     * @param algorism  int 十进制数字
     * @param maxLength int 转换后的十六进制字符串长度
     * @return String 转换后的十六进制字符串
     */
    public static String algorismToHEXString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray byte[]
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * 二进制字符串转十进制
     *
     * @param binary 二进制字符串
     * @return 十进制数值
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十进制转换为十六进制字符串
     *
     * @param algorism int 十进制的数字
     * @return String 对应的十六进制字符串
     */
    public static String algorismToHEXString(int algorism) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;

        }
        result = result.toUpperCase();

        return result;
    }

    /**
     * HEX字符串前补0，主要用于长度位数不足。
     *
     * @param str       String 需要补充长度的十六进制字符串
     * @param maxLength int 补充后十六进制字符串的长度
     * @return 补充结果
     */
    static public String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }

    /**
     * 将一个字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @param radix      int 要转换的字符串是什么进制的,如16 8 10.
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt, int radix) {
        int i = 0;
        try {
            i = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 将一个十进制形式的数字字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 十六进制字符串转为Byte数组,每两个十六进制字符转为一个Byte
     *
     * @param hex 十六进制字符串
     * @return byte 转换结果
     */
    public static byte[] hexStringToByte(String hex) {
        int max = hex.length() / 2;
        byte[] bytes = new byte[max];
        String binarys = DataTransformUtils.hexStringToBinary(hex);
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) DataTransformUtils.binaryToAlgorism(binarys.substring(
                    i * 8 + 1, (i + 1) * 8));
            if (binarys.charAt(8 * i) == '1') {
                bytes[i] = (byte) (0 - bytes[i]);
            }
        }
        return bytes;
    }

    /**
     * 十六进制串转化为byte数组
     *
     * @return the array of byte
     */
    public static final byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if(hex != null)
        {
            //Log.i(TAG,"hex length = " + hex.length() + " ;hex= " + hex);
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    public static void main(String args[]) {

        MultipleBluetoothController multipleBluetoothController = BleManager.getInstance().getMultipleBluetoothController();
       // System.out.println(multipleBluetoothController.getDeviceList().size());
//        System.out.println(Utils.fahrenheitToCelsius(63)+ " ; " + Utils.fahrenheitToCelsius(203) + " ; " + Utils.celsiusToFahrenheit(60) + " ; " + Utils.celsiusToFahrenheit(95));
        String str = "AA9C800200004900000000000000000000000000";
//        System.out.println(str.length());
        System.out.println(str.substring(12,14));
        System.out.println(Utils.getSystemTimeToStatics(1));
        System.out.println(Utils.getSystemTimeToStatics(3));
        System.out.println(Utils.getSystemCurrentTime().substring(9,11));




        //System.out.println();
//        String str = "AABB000600000000000000000000000000000000";
//        String str1 = "AA00000600000000000000000000000000000000";
//        String str2 = "AABB800600000056B04C00000000000000000000";
//        String str4 = "AAF2800600000056B04C00000000000000000000";
//        String DATA_HEAD = ":A000";
//        String str5 = "A0009DEC984005FCEE9DFE0FD5F0E9E4CEFD22ED";
//        String str6 = "AA0000010000ca6bc0ed0fc40000000000000000";
//        String str7 = "AAa800010000ca6bc0ed0fc40000000000000000";
//        String str8 = "AAB8800600000A57B04A00000000000000000000";
//        String str9 = "0A";
//        String str10 = "57";
//        String str11 = "B0";
//        String str12 = "4A";
//        String str13 = "13";
//        String str14 = "56";
//        String str15 = "57";
//        System.out.println("Status = " + Integer.parseInt(str9,16)
//                + " ;电量 " + Integer.parseInt(str10,16)
//                + " ;温度 " + Integer.parseInt(str14,16) + " ;" + Integer.parseInt(str15,16)
//                + " ;压强 " + Integer.parseInt(str13,16));
//        byte[] bytes = hex2byte(str8);
//        System.out.println(Arrays.toString(bytes));
//        System.out.println("bytes[6] = " + bytes[6] + "; byteToInt = " + Byte2Int(bytes));
//        System.out.println(Utils.getSystemCurrentTime());
//        byte[] b = new byte[]{-86, 40, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        System.out.println(DataTransformUtils.byte2hex(b));
//        System.out.println(CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(str6)));
//        System.out.println(Arrays.toString(DataTransformUtils.hex2byte(str7)));
//        //System.out.println(str5.substring(3,4));
//        System.out.println(replaceStr(2,4,"b2",str5));
        //System.out.print(str5.replaceFirst(str5.substring(3,5),"b2"));
        //tagertStr.replaceFirst(tagertStr.substring(start,end),replaceStr);
        //replaceStr()
//        System.out.println(DATA_HEAD);
//        System.out.println(DATA_HEAD.getBytes());
//        System.out.println(DataTransformUtils.bytetoString(DATA_HEAD.getBytes()));
//        for(int i=0;i<DATA_HEAD.getBytes().length;i++){
//            System.out.println(DATA_HEAD.getBytes()[i]);
//
//        }
//        System.out.println(AppendPrefix(20,"aaaaaaaaaaaaaaaa","0"));
//        //String str3 = str4.substring(2,4);
//        str4 = str4.replaceFirst(str4.substring(2,4),"00");
//        String crc8Str1 = Integer.toHexString(CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(str4)));
//        System.out.println("str4 = " + str4 + " ;crc8Str1 =" + crc8Str1);
//        String a="46547";
//// 第一位
//        int i = 1;
//        a = a.replaceFirst(a.substring(i, i+1), "0");
//        System.out.println(a);

//        String str2 = "AA00800600000056B04C00000000000000000000";
//        byte[] test = new byte[]{-86, -115, -128, 6, 0, 0, 0, 86, -80, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        byte[] test1 = new byte[]{-86, 0, -128, 6, 0, 0, 0, 86, -80, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        //System.out.println(crcTable(test1));
//        byte[] temp = DataTransformUtils.hex2byte(str);
//        System.out.println(Arrays.toString(DataTransformUtils.hex2byte(str)));
//        System.out.println(DataTransformUtils.byte2hex(temp).toCharArray());
//        System.out.println(DataTransformUtils.byte2hex(test).toCharArray());
//        System.out.println(CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(str1)));
//        System.out.println(DataTransformUtils.intToHexString(CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(str1))));
//        System.out.println(CRC8Util.getCalcCrc8(test1));
//        System.out.println(DataTransformUtils.intToHexString(CRC8Util.getCalcCrc8(test1)));
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static final String byte2hex(byte[] b) {
        //Log.i(TAG,"byte2Hex b.length = " + b.length);
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * int 转 16进制字符串
     *
     * @param i
     * @return
     */
    public static String intToHexString(int i) {
        String src = Integer.toHexString(i);
        return src;
    }

    //字符串截取
    public static int[] stringIntercept(String cmd, String str) {
        int[] strData = null;
        switch (cmd) {
            case Config.CMD_1_GET_PTV:
                //分解数据
                strData = segmentationStr(str.substring(12, 20));
                break;
            case Config.CMD_1_ENTER_FACTORY:
                break;
            case Config.CMD_1_ENTER_OFF:
                break;
            case Config.CMD_1_SEND_FILE:
                break;
            case Config.CMD_1_DOWNLOAD:
                strData = segmentationStr(str.substring(10,12));
                break;
        }
        Log.i(TAG,"stringIntercept strData = " + Arrays.toString(strData));
        return strData;
    }

    public static int[] segmentationStr(String str) {
        StringBuffer s1 = new StringBuffer(str);
        int index;
        for (index = 2; index < s1.length(); index += 3) {
            s1.insert(index, ',');
        }
        String[] array = s1.toString().split(",");
        int[] temp = new int[array.length];
        //转换位10进制的字符串
        for (int i = 0; i < array.length; i++) {
            temp[i] = DataTransformUtils.hexStringToAlgorism(array[i]);
        }
        return temp;
    }

    public static double PressurCoe = 3.13;
    //压强数据转换
    //单位: Kpa
    public static double pressureProcess(int data){
        //压力系数：2.745 KPa
        return data * PressurCoe;
    }

    public static int tempConstant = 60;
    //温度数据转换 摄氏度
    public  static int tempProcess(int temp){
        return temp - 60;
    }

    //电量数据转换
    //公式：

    /**
     * InputStream to String
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static final String inputStreamToString(InputStream is) throws IOException {
        if (is == null) {
            Log.i(TAG, "is == null");
            return null;
        } else {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String str = result.toString(StandardCharsets.UTF_8.name());
            return str;
        }
    }

    /**
     * String To InputStream
     *
     * @param str
     * @return
     */
    public static final InputStream stringToInputStream(String str) {
        if (str == null) {
            Log.i(TAG, "str is null");
            return null;
        } else {
            InputStream is = new ByteArrayInputStream(str.getBytes());
            return is;
        }
    }

    /**
     * inputStream To bytes
     * @param is
     * @return
     * @throws IOException
     */
    public static final byte[] inputStreamToByte(InputStream is) throws IOException {
        if (is == null) {
            Log.i(TAG, "inputStreamToByte is == null");
            return null;
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while ((n = is.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, n);
            }
            //return  new String(byteArrayOutputStream.toByteArray(), "utf-8");
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static final InputStream bytesToInputeStream(byte[] bytes){
        if(bytes == null){
            Log.i(TAG, "bytesToInputeStream bytes is == null");
            return null;
        }else{
            return null;
        }
    }

    public static final byte[] sendData(Context context,int type) throws IOException {
        byte[] bytes = DataTransformUtils.inputStreamToByte(Utils.getInputStream(type,context));
        String strData = DataTransformUtils.byte2hex(bytes);
        //byte[] bytes = DataTransformUtils.inputStreamToByte(Utils.getInputStream(type,context));
        //String tempData = replaceBlank(strData);
        //Log.i(TAG,"strData = " + replaceBlank(tempData));
        byte[] byteHeads =  ConstructSendFileCMD(bytes,strData);
        byte[] byteDatas = ConstructSendData(bytes);
        Log.i(TAG,"byteHeads = " + Arrays.toString(byteHeads) + " ;byteDatas = " + Arrays.toString(byteDatas));
        return combinationHeadAndData(byteHeads,byteDatas);
//        //数据转换为Byte[]
//        byte[] dataBytes = DataTransformUtils.inputStreamToByte(Utils.getInputStream(type,context));
//        String crc16 = CRC16Util.GetCRC(dataBytes);//暂居4位
//        //获取数据长度
//        int fileSize = Utils.CacluHexBytes(type,context);//字节
//        String strHex = Integer.toHexString(dataBytes.length);
//        Log.i(TAG,"文件长度 strHex = " + strHex + " ; CRC16 = " + crc16 + " ;数据的长度 = " + dataBytes.length);
//        //拼接前半部分数据 A0 00 00 00 00 00 01 00 00 00 2e 01
//        //文件长度剩余8位
//        //计算需要循环的次数
//        String strHeadData = Integer.toHexString(Config.DATA_HEAD_INT) + crc16 + "0001000000" +strHex;
//        Log.i(TAG,"str2HexStr(Config.DATA_HEAD) = " + Integer.toHexString(Config.DATA_HEAD_INT));
//        byte[] byteHeadData = DataTransformUtils.hex2byte(strHeadData);
//        Log.i(TAG,"strHeadData = " + strHeadData);
//        Object[] objs = splitAry(dataBytes,8,byteHeadData);
//        return byteMergerAll(objs);
//        for (Object obj : objs){
//
//            byte[] bytes = (byte[]) obj;
//            System.out.println(bytes.length);
//            System.out.println(DataTransformUtils.byte2hex(bytes));
//        }
    }

    //数据格式: A0 CRC FileData  文件数据包含18字节
    public static byte[] ConstructSendData(byte[] byteDatas) throws UnsupportedEncodingException {
        String DATA_HEAD = "A000";
        Object[] objs = splitAry(byteDatas,18,DataTransformUtils.hex2byte(DATA_HEAD));
        return byteMergerAll(objs);
    }

    public static byte[] combinationHeadAndData(byte[] heads,byte[] datas){
        return unitByteArray(heads,datas,0);
    }


    //格式 --20字节
    //AA CRC 00 01 00 00 CRC16 FileSize 00 00 00 00 00 00 .... 00
    //CRC16 是文件的CRC16  fileSize 文件的大小
    //文件大小 -默认32 位 高位不足需要补零
    public static byte[] ConstructSendFileCMD(byte[] byteDatas,String strData) throws UnsupportedEncodingException {
        String  CMD_HEAD = "AA0000010000";
        //byte[] dataBytes = DataTransformUtils.inputStreamToByte(Utils.getInputStream(type,context));
        //byte[] dataBytes = DataTransformUtils.hex2byte(strData);//strData.getBytes("utf-8");//DataTransformUtils.hex2byte(strData);
        String CRC16 = CRC16Util.GetCRC(byteDatas);
        if(CRC16.length() < 8){
            //低位补0
            int numbertemp = 8 - CRC16.length();
            CRC16 = lowInsertData(CRC16,numbertemp);
        }
        String fileSize = Integer.toHexString(byteDatas.length);
        if(fileSize.length() < 8){
            //高位补0
            int number = 8 - fileSize.length();
            fileSize = insertData(fileSize,number);
        }
        String cmd_str = CMD_HEAD + CRC16 + fileSize;
        String cmd_str_new = AppendPrefix(40,cmd_str,"0");
        //获取CRC8 校验位
        String CRC8 = CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(cmd_str_new));
        //替换命令串的校验位 2,3
        String tagrtStrCmd = replaceStr(2,4,CRC8,cmd_str_new);
        Log.i(TAG,"ConstructSendFileCMD string : " + tagrtStrCmd);
        return DataTransformUtils.hex2byte(tagrtStrCmd);
    }

    //高位插入数据
    public static String insertData(String str1,int number){
        StringBuilder sb = new StringBuilder(str1);
        for(int i=0; i < number; i++){
            sb.insert(0, "0");
        }
        str1 = sb.toString();
        return str1;
    }

    //低位插入数据
    public static String lowInsertData(String str1,int number){
        StringBuilder sb = new StringBuilder(str1);
        for(int i=0; i < number; i++){
            sb.append("0");
        }
        str1 = sb.toString();
        return str1;
    }

    //替换字符串中的某些位
    public static String replaceStr(int start, int end, String replaceStr,String tagertStr){
        StringBuilder sb = new StringBuilder(tagertStr);
        return sb.replace(start,end,replaceStr).toString();
    }

    //strHexData = strHexData.replaceFirst(strHexData.substring(2,4),"00");
    /**
     *
     *当传入的length长度小于传入字符串target的长度时，输出原有字符串
     */
    public static String AppendPrefix(int length, String target, String append) {
        StringBuffer sb = new StringBuffer();
        int len = target.length();
        sb.append(target);
        for (int i = 0; i < length - len; i++) {
            sb.append(append);
        }
        return sb.toString();
    }

    /**

     * splitAry方法<br>
     * @param ary 要分割的数组
     * @param subSize 分割的块大小
     * @return
     *
     */
    public static Object[] splitAry(byte[] ary, int subSize,byte[] strHeadData) {
        boolean b = false;
        b = ary.length % subSize == 0 ? true : false;
        int count = ary.length % subSize == 0 ? ary.length / subSize: ary.length / subSize + 1;

        List<List<Byte>> subAryList = new ArrayList<List<Byte>>();

        for (int i = 0; i < count; i++) {
            int index = i * subSize;
            List<Byte> list = new ArrayList<Byte>();
            int j = 0;
            while (j < subSize && index < ary.length) {
                list.add(ary[index++]);
                j++;
            }
            if(list.size() < subSize){
                byte b1 = 00;
                //补齐
                int len = subSize - list.size();
                //需要补齐的个数
                for(int z=0; z<len;z++){
                    list.add(b1);
                }
            }

            subAryList.add(list);
        }

        Object[] subAry = new Object[subAryList.size()];

        for(int i = 0; i < subAryList.size(); i++){
            List<Byte> subList = subAryList.get(i);
            byte[] subAryItem = new byte[subList.size()];
            for(int j = 0; j < subList.size(); j++){
                subAryItem[j] = subList.get(j);
            }
            //拼接生成新的byte
            //unitByteArray(strHeadData,subAryItem);
            //subAry[i] = subAryItem;
            Log.i(TAG,"记录报错的位置: i = " + i + " ;strHeadData = "+ Arrays.toString(strHeadData) + " ;subAryItem = " + Arrays.toString(subAryItem));
            subAry[i] = unitByteArray(strHeadData,subAryItem,2);//2 需要处理CRC8
        }
        return subAry;
    }

    //合并多个byte
    public static byte[] byteMergerAll(Object[] values) {
        int length_byte = 0;
        if(values != null && values.length > 0){
            length_byte = values.length * 20;
//            for (int i = 0; i < values.length; i++) {
//                length_byte += values[i].length;
//            }
            byte[] all_byte = new byte[length_byte];
            int countLength = 0;
            for (Object obj : values) {
                byte[] b = (byte[]) obj;
                System.arraycopy(b, 0, all_byte, countLength, b.length);
                countLength += b.length;
                Log.i(TAG,"升级数据拼接: " + DataTransformUtils.byte2hex(b));
            }
            return all_byte;
        } else{
            return null;
        }
    }


    /**
     * 合并byte数组
     */
    public static byte[] unitByteArray(byte[] byte1,byte[] byte2,int type){
        byte[] unitByte = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, unitByte, 0, byte1.length);
        System.arraycopy(byte2, 0, unitByte, byte1.length, byte2.length);
        if(type == 2){
            String strData = DataTransformUtils.byte2hex(unitByte);
            String crc8 = CRC8Util.getCalcCrc8(unitByte);
            String strNewData = replaceStr(2,4,crc8,strData);
            //添加CRC位 数据替换
            byte[] temp = DataTransformUtils.hex2byte(strNewData);
            return temp;
        } else {
            Log.i(TAG,"unitByteArray = " + Arrays.toString(unitByte));
            return unitByte;
        }
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String inputStream2String(InputStream is, String charset) {
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            int i = -1;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            return baos.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"FileWRUtil.inputStream2String(InputStream is, String charset) occur error:"
                    + e.getMessage());
        } finally {
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"FileWRUtil.inputStream2String(InputStream is, String charset) occur error:"
                            + e.getMessage());
                }
                baos = null;
            }
        }
        return null;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n|:");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
