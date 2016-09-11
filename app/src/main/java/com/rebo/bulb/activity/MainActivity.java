package com.rebo.bulb.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.BleGattCallback;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.ListScanCallback;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.adapter.DeviceListAdapter;
import com.rebo.bulb.ble.BleConst;

import java.util.Arrays;

import butterknife.Bind;


public class MainActivity extends BaseActivity {

    private static final int RECORD_AUDIO_REQUEST_CODE = 976;
    private static final int REQUEST_ENABLE_BT = 977;
    private static final int ACCESS_FINE_LOCATION_CODE = 988;
    private static final String TAG = "magic_ble";

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Bind(R.id.lv_device)
    ListView listView;
    @Bind(R.id.rl_empty)
    RelativeLayout emptyRelativeLayout;
    BluetoothAdapter mBluetoothAdapter;
    @Bind(R.id.tv_empty)
    TextView emptyView;

    private DeviceListAdapter deviceListAdapter;
    private BleManager bleManager;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (this.mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setNavigationTitle("设备");
        //开启声音权限
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
//        }
        initBleManager();
        initBluetooth();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {//请求声音权限
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
            }
        }
    }

    @Override
    protected void setListener() {

    }
    private void connectDevice(){

    }
    private void notifyChar(){
        bleManager.notifyDevice(
                BleConst.RX_SERVICE_UUID.toString(),
                BleConst.TX_READ_UUID.toString(),
                BleConst.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        Log.d(TAG, "特征值Notify通知数据回调： " + '\n' + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Log.e(TAG, "特征值Notify通知回调失败: " + '\n' + exception.toString());
//                        bleManager.handleException(exception);
                    }
                });
    }

    private final class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

//            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
//                    == PackageManager.PERMISSION_GRANTED) {
            final BluetoothDevice device = deviceListAdapter.getItem(position);
            bleManager.connectDevice(device, new BleGattCallback() {
                @Override
                public void onConnectSuccess(BluetoothGatt gatt, int status) {
                    Log.i(TAG, "连接成功！");
                    gatt.discoverServices();                // 连接上设备后搜索服务
                    Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("device", device);
                    intent.putExtras(bundle);
                    MainActivity.this.startActivity(intent);
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.i(TAG, "服务被发现！");
                }
                @Override
                public void onConnectFailure(BleException exception) {
                    Log.i(TAG, "连接失败或连接中断：" + '\n' + exception.toString());
                    Toast.makeText(MainActivity.this, "连接失败或连接中断", Toast.LENGTH_LONG).show();
//                    bleManager.handleException(exception);
                }
            });


//            } else {
//                Toast.makeText(MainActivity.this, "请开启声音权限", Toast.LENGTH_LONG).show();
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
//            }
        }
    }

    private boolean isBleSupport() {
        boolean support = false;
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            support = true;
        }
        return support;
    }

    private void initBleManager() {
        bleManager =BaseApplication.getBleManager();
    }
    private void scanBleDevice() {
        bleManager.scanDevice(new ListScanCallback(BleConst.SCAN_TIME_OUT) {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
//             device.getName() + "------mac:" + device.getAddress());
                emptyRelativeLayout.setVisibility(View.GONE);
                deviceListAdapter.addDevice(device);
                deviceListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanTimeout() {
                super.onScanTimeout();
                Log.i(TAG, "搜索时间结束");
            }
        });
    }

    private void initBluetooth() {
        listView.setOnItemClickListener(new OnItemClickListener());
        deviceListAdapter = new DeviceListAdapter(MainActivity.this, R.layout.listview_item_device);
        listView.setAdapter(deviceListAdapter);
        //是否支持蓝牙
        if (!isBleSupport()) {
            emptyView.setText("该手机不支持BLE 4.0");
            return;
        }
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            emptyView.setText("请打开手机蓝牙");
            return;
        }
        scanBleDevice();
    }

}
