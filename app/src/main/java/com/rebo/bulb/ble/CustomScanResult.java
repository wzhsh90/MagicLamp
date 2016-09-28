package com.rebo.bulb.ble;

import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wzhsh90 on 2016/4/12.
 */
public final class CustomScanResult {
    private static final String TAG = "ScanRecordD";
    // The following data type values are assigned by Bluetooth SIG.
    // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
    private static final int DATA_TYPE_FLAGS = 0x01;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final int DATA_TYPE_SERVICE_DATA = 0x16;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;
    // Flags of the advertising data.
    private final int mAdvertiseFlags;
    @Nullable
    private final List<ParcelUuid> mServiceUuids;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    // Transmission power level(in dB).
    private final int mTxPowerLevel;
    // Local name of the Bluetooth LE device.
    private final String mDeviceName;
    // Raw bytes of scan record.
    private final byte[] mBytes;
    /**
     * Returns the advertising flags indicating the discoverable mode and capability of the device.
     * Returns -1 if the flag field is not set.
     */
    public int getAdvertiseFlags() {
        return mAdvertiseFlags;
    }
    /**
     * Returns a list of service UUIDs within the advertisement that are used to identify the
     * bluetooth GATT services.
     */
    public List<ParcelUuid> getServiceUuids() {
        return mServiceUuids;
    }

    public JSONArray getServiceJsonArray(){
        JSONArray array=new JSONArray();
        if(null!=mServiceUuids){
            for(ParcelUuid uuid :mServiceUuids){
                array.put(uuid.toString());
            }
        }
        return  array;
    }

    /**
     * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
     * data.
     */
    public SparseArray<byte[]> getManufacturerSpecificData() {
        return mManufacturerSpecificData;
    }
    /**
     * Returns the manufacturer specific data associated with the manufacturer id. Returns
     * {@code null} if the {@code manufacturerId} is not found.
     */
    @Nullable
    public byte[] getManufacturerSpecificData(int manufacturerId) {
        return mManufacturerSpecificData.get(manufacturerId);
    }
    /**
     * Returns a map of service UUID and its corresponding service data.
     */
    public Map<ParcelUuid, byte[]> getServiceData() {
        return mServiceData;
    }
    /**
     * Returns the service data byte array associated with the {@code serviceUuid}. Returns
     * {@code null} if the {@code serviceDataUuid} is not found.
     */
    @Nullable
    public byte[] getServiceData(ParcelUuid serviceDataUuid) {
        if (serviceDataUuid == null) {
            return null;
        }
        return mServiceData.get(serviceDataUuid);
    }
    /**
     * Returns the transmission power level of the packet in dBm. Returns {@link Integer#MIN_VALUE}
     * if the field is not set. This value can be used to calculate the path loss of a received
     * packet using the following equation:
     * <p>
     * <code>pathloss = txPowerLevel - rssi</code>
     */
    public int getTxPowerLevel() {
        return mTxPowerLevel;
    }
    /**
     * Returns the local name of the BLE device. The is a UTF-8 encoded string.
     */
    @Nullable
    public String getDeviceName() {
        return mDeviceName;
    }
    /**
     * Returns raw bytes of scan record.
     */
    public byte[] getBytes() {
        return mBytes;
    }
    private CustomScanResult(List<ParcelUuid> serviceUuids,
                             SparseArray<byte[]> manufacturerData,
                             Map<ParcelUuid, byte[]> serviceData,
                             int advertiseFlags, int txPowerLevel,
                             String localName, byte[] bytes) {
        mServiceUuids = serviceUuids;
        mManufacturerSpecificData = manufacturerData;
        mServiceData = serviceData;
        mDeviceName = localName;
        mAdvertiseFlags = advertiseFlags;
        mTxPowerLevel = txPowerLevel;
        mBytes = bytes;
    }
    /**
     * Parse scan record bytes to {@link CustomScanResult}.
     * <p>
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     * <p>
     * All numerical multi-byte entities and values shall use little-endian <strong>byte</strong>
     * order.
     *
     * @param ScanRecordD The scan record of Bluetooth LE advertisement and/or scan response.
     * @hide
     */
    public static CustomScanResult parseFromBytes(byte[] ScanRecordD) {
        if (ScanRecordD == null) {
            return null;
        }
        int currentPos = 0;
        int advertiseFlag = -1;
        List<ParcelUuid> serviceUuids = new ArrayList<ParcelUuid>();
        String localName = null;
        int txPowerLevel = Integer.MIN_VALUE;
        SparseArray<byte[]> manufacturerData = new SparseArray<byte[]>();
        Map<ParcelUuid, byte[]> serviceData = new HashMap<ParcelUuid, byte[]>();
        try {
            while (currentPos < ScanRecordD.length) {
                // length is unsigned int.
                int length = ScanRecordD[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = ScanRecordD[currentPos++] & 0xFF;
                switch (fieldType) {
                    case DATA_TYPE_FLAGS:
                        advertiseFlag = ScanRecordD[currentPos] & 0xFF;
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                        parseServiceUuid(ScanRecordD, currentPos,
                                dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                        parseServiceUuid(ScanRecordD, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                        parseServiceUuid(ScanRecordD, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_LOCAL_NAME_SHORT:
                    case DATA_TYPE_LOCAL_NAME_COMPLETE:
                        localName = new String(
                                extractBytes(ScanRecordD, currentPos, dataLength));
                        break;
                    case DATA_TYPE_TX_POWER_LEVEL:
                        txPowerLevel = ScanRecordD[currentPos];
                        break;
                    case DATA_TYPE_SERVICE_DATA:
                        // The first two bytes of the service data are service data UUID in little
                        // endian. The rest bytes are service data.
                        int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
                        byte[] serviceDataUuidBytes = extractBytes(ScanRecordD, currentPos,
                                serviceUuidLength);
                        ParcelUuid serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes);
                        byte[] serviceDataArray = extractBytes(ScanRecordD,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength);
                        serviceData.put(serviceDataUuid, serviceDataArray);
                        break;
                    case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        int manufacturerId = ((ScanRecordD[currentPos + 1] & 0xFF) << 8) +
                                (ScanRecordD[currentPos] & 0xFF);
                        byte[] manufacturerDataBytes = extractBytes(ScanRecordD, currentPos + 2,
                                dataLength - 2);
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                        break;
                    default:
                        // Just ignore, we don't handle such data type.
                        break;
                }
                currentPos += dataLength;
            }
            if (serviceUuids.isEmpty()) {
                serviceUuids = null;
            }
            return new CustomScanResult(serviceUuids, manufacturerData, serviceData,
                    advertiseFlag, txPowerLevel, localName, ScanRecordD);
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(ScanRecordD));
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw ScanRecordD bytes in results
            return new CustomScanResult(null, null, null, -1, Integer.MIN_VALUE, null, ScanRecordD);
        }
    }

    // Parse service UUIDs.
    private static int parseServiceUuid(byte[] ScanRecordD, int currentPos, int dataLength,
                                        int uuidLength, List<ParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            byte[] uuidBytes = extractBytes(ScanRecordD, currentPos,
                    uuidLength);
            serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }
    // Helper method to extract bytes from byte array.
    private static byte[] extractBytes(byte[] ScanRecordD, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(ScanRecordD, start, bytes, 0, length);
        return bytes;
    }
}