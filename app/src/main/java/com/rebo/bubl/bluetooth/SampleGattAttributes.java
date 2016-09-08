package com.rebo.bubl.bluetooth;

/**
 * Created by guodunsong on 16/7/14.
 */
import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";//"00002a37-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT2 = "0000ffe2-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "基于自定义通信协议的蓝牙服务");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile Service");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile Service");

        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("0000fff1-0000-1000-8000-00805f9b34fb", "第一特征值");
        attributes.put("0000fff2-0000-1000-8000-00805f9b34fb", "第二特征值");
        attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "第三特征值");
        attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "第四特征值");
        attributes.put("0000fff5-0000-1000-8000-00805f9b34fb", "第五特征值");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}