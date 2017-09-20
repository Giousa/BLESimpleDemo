/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsrk.ble.service;

import java.util.HashMap;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "0000C004-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static  String EWQ_UUID_Notify="0000fff2-0000-1000-8000-00805f9b34fb";
    public static  String CHARACTERISTIC_UUID_RETURN="00002902-0000-1000-8000-00805f9b34fb";

    //通用特征值,模块一
    public static  String UUID_01="00001910-0000-1000-8000-00805f9b34fb";
    public static final String CH_01="0000fff2-0000-1000-8000-00805f9b34fb";
    //通用特征值,模块二
    public static  String UUID_02="0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String CH_02="0000fff6-0000-1000-8000-00805f9b34fb";
    //脂肪称
    public static  String UUID_FAT="0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String CH_FAT="0000fff1-0000-1000-8000-00805f9b34fb";
    //发送数据特征值
    public static final String WRITE="0000fff6-0000-1000-8000-00805f9b34fb";
    public static final String WRITE_FAT="0000fff2-0000-1000-8000-00805f9b34fb";

    public static BluetoothGattCharacteristic SEND_CHARACT;



    static {
        // Sample Services.
        attributes.put("0000fff00000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

 