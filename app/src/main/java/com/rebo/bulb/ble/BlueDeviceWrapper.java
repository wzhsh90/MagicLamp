package com.rebo.bulb.ble;

import android.bluetooth.BluetoothDevice;

import com.google.common.base.Strings;

/**
 * Created by wzhsh90 on 2016/9/28.
 */

public class BlueDeviceWrapper {
    private BluetoothDevice bluetoothDevice;
    private String localName;
    private byte[] scanRecord;

    public BlueDeviceWrapper(BluetoothDevice bluetoothDevice, byte[] scanRecord) {
        this.bluetoothDevice = bluetoothDevice;
        this.scanRecord = scanRecord;
        if (null != scanRecord) {
            CustomScanResult rs = CustomScanResult.parseFromBytes(scanRecord);
            this.localName = rs.getDeviceName();
        }

    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        String name = "";
        if (!Strings.isNullOrEmpty(localName)) {
            name = this.localName;
        } else {
            if (Strings.isNullOrEmpty(bluetoothDevice.getName())) {
                name = bluetoothDevice.getAddress();
            } else {
                name = bluetoothDevice.getName();
            }
        }
        return name;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }
}
