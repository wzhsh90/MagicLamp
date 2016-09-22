package com.rebo.bulb.ble;

import java.util.UUID;

/**
 * Created by wzhsh90 on 2016/9/11.
 */
public class BleConst {

    public static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String  RX_SERVICE_UUID = UUID.fromString(String.format(
            "%08x-0000-1000-8000-00805f9b34fb", 0xFFE0)).toString();
    public static final String RX_WRITE_UUID = UUID.fromString(String.format(
            "%08x-0000-1000-8000-00805f9b34fb", 0xFFE1)).toString();
    public static final String TX_READ_UUID = UUID.fromString(String.format(
            "%08x-0000-1000-8000-00805f9b34fb", 0xFFE1)).toString();
//    public static final String  RX_SERVICE_UUID="6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
//    public static final String RX_WRITE_UUID="6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
//    public static final String TX_READ_UUID="6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final long SCAN_TIME_OUT = 10000;                                   // 扫描超时时间s
}
