package com.baolong.ble_tpms.ui.utils;

import android.content.Intent;
import android.util.Log;

import com.baolong.ble_tpms.ui.db.Config;

public class DecodeBleData {

    private static final String TAG = "DecodeBleData";
    public static final void DecodeNotifyData(byte[] bytes){
        String strHexData = DataTransformUtils.byte2hex(bytes);
        if (Utils.judgeDataCheckDigit(bytes)) {
            if (Utils.judgeCmdType(bytes).equalsIgnoreCase(Config.CMD_1_GET_PTV)) {
                //数据校验正确-开始解析数据
                //响应的数据格式 如下：AA
                // AA8D800600000056B04800000000000000000000
                //数据格式解析  AA:固定值  8D:crc校验位 80:cmd0 06:cmd1 00:param[0] param[1]:00/其它 00:压力减小 56:电量 B0:温度 48:压强 00000000000无效位
                String cmd1 = strHexData.substring(6, 8);//判断是什么指令
                int[] tempDatas = DataTransformUtils.stringIntercept(Config.CMD_1_GET_PTV, strHexData);

            } else if (Utils.judgeCmdType(bytes).equalsIgnoreCase(Config.CMD_1_DOWNLOAD)) {
                //判断返回的数据是否成功
                //发送数据给升级设备界面
                int[] tempDatas = DataTransformUtils.stringIntercept(Config.CMD_1_DOWNLOAD, strHexData);
                if (tempDatas[0] == 0) {

                }
            } else if (Utils.judgeCmdType(bytes).equalsIgnoreCase(Config.CMD_1_SEND_FILE)) {

            }
        } else {
            //数据校验失败
            Log.i(TAG, "crc8 数据校验失败!");
        }
    }

    public static final String DecodeUUIDData(String strData){
        //AA 8D 80 06 00 00 00 56 B0 48 00000000000000000000
        //11、12 uuid flag
        String str = "30";
        String temp =  strData.substring(12,14);
        if(temp.equalsIgnoreCase(Config.BLE_VERSION_TYPE_49)){
            return  str + Config.BLE_VERSION_TYPE_49;
        } else if(temp.equalsIgnoreCase(Config.BLE_VERSION_TYPE_11)){
            return str + Config.BLE_VERSION_TYPE_11;
        } else {
            return Config.BLE_VERSION_TYPE_UNKNOWN;
        }
    }
}
