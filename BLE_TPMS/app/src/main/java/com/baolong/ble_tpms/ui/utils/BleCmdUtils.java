package com.baolong.ble_tpms.ui.utils;

import android.util.Log;

import com.baolong.ble_tpms.ui.db.Config;

public class BleCmdUtils {
    public static final String TAG = "BleCmdUtils";

    //只有在off模式，才能接收APP指令进入update模式  factory 或者 off才能唤醒BLE 工作
    public static String getEntryOffModelCmd() {
        //目前处于工作状态
        //String  strOff = "A000000300000000";//数据头
        String strOff = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_ENTER_OFF + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        strOff = DataTransformUtils.AppendPrefix(40, strOff, "0");
        strOff = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(strOff)), strOff);
        Log.i(TAG, "获取EntryOffModel 命令：" + strOff);
        return strOff;
    }

    public static String getEntryFactoryModelCmd() {
        String strFactory = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_ENTER_FACTORY + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        strFactory = DataTransformUtils.AppendPrefix(40, strFactory, "0");
        strFactory = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(strFactory)), strFactory);
        Log.i(TAG, "发送entryFactorModel 命令：" + strFactory);
        return strFactory;
    }

    public static String getEntryPowerOffCmd() {
        String strPowerOff = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_POWER_OFF + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        strPowerOff = DataTransformUtils.AppendPrefix(40, strPowerOff, "0");
        strPowerOff = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(strPowerOff)), strPowerOff);
        Log.i(TAG, "发送powerOff 命令：" + strPowerOff);
        return strPowerOff;
    }

    public static String getPtvCmd() {
        //String strGetPtv = "AABB000600000000000000000000000000000000";
        String strGetPtv = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_GET_PTV + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        strGetPtv = DataTransformUtils.AppendPrefix(40, strGetPtv, "0");
        strGetPtv = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(strGetPtv)), strGetPtv);
        Log.i(TAG, "发送获取PTV命令：" + strGetPtv);
        return strGetPtv;
        //bleDeviceDataWrite(strGetPtv, type);
    }

    public static String getDownLoadCmd() {
        //String strGetPtv = "AABB000600000000000000000000000000000000";
        String sendDownload = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_DOWNLOAD + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        sendDownload = DataTransformUtils.AppendPrefix(40, sendDownload, "0");
        sendDownload = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(sendDownload)), sendDownload);
        Log.i(TAG, "发送download命令：" + sendDownload);
        //bleDeviceDataWrite(sendDownload, type);
        return sendDownload;
    }

    public static String getUUIDCmd() {
        //String strGetPtv = "AABB000600000000000000000000000000000000";
        String sendDownload = Config.CMD_HEAD + Config.CRC8_DEFAULT + Config.CMD_0 + Config.CMD_1_GET_UUID + Config.PARAM_0 + Config.PARMA_1 + Config.EXTERN_PARMA;
        sendDownload = DataTransformUtils.AppendPrefix(40, sendDownload, "0");
        sendDownload = DataTransformUtils.replaceStr(2, 4, CRC8Util.getCalcCrc8(DataTransformUtils.hex2byte(sendDownload)), sendDownload);
        Log.i(TAG, "发送getUUID命令：" + sendDownload);
        //bleDeviceDataWrite(sendDownload, type);
        return sendDownload;
    }
}
