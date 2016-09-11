package com.rebo.bulb.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.BleGattCallback;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.ListScanCallback;
import com.rebo.bulb.AppConst;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.adapter.DeviceListAdapter;
import com.rebo.bulb.ble.BleConst;
import com.rebo.bulb.utils.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    private static final String TAG = "magic_ble";

    @Bind(R.id.lv_device)
    ListView listView;
    @Bind(R.id.rl_empty)
    RelativeLayout emptyRelativeLayout;
    BluetoothAdapter mBluetoothAdapter;
    @Bind(R.id.tv_empty)
    TextView emptyView;

    @Bind(R.id.iv_lamp)
    ImageView lampImageView;
    @Bind(R.id.tv_home_title)
    TextView titleTextView;

    private Animation operatingAnim;
    private DeviceListAdapter deviceListAdapter;
    private BleManager bleManager;
    private ListScanCallback listScanCallback = new ListScanCallback(BleConst.SCAN_TIME_OUT) {
        @Override
        public void onDeviceFound(BluetoothDevice device) {
            onBleDeviceFound(device);
        }

        @Override
        public void onScanTimeout() {
            super.onScanTimeout();
            Log.i(TAG, "搜索时间结束");
        }

    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBusUtil.registerEvent(this);
        if (this.mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setNavigationTitle("设备");
        initBleManager();
        initBluetooth();
        spinAnimation();
    }
    private void spinAnimation() {
        //专辑旋转动画
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        operatingAnim.setInterpolator(linearInterpolator);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerEventBus(JSONObject jsonObject) {
        if (null != jsonObject) {
            String code;
            try {
                code = jsonObject.getString("code");
                switch (code) {
                    case AppConst.BLUE_ON:
                        onBlueOn();
                        break;
                    case AppConst.BLUE_OFF:
                        onBlueOff();
                        break;
                    case AppConst.BLUE_CONN_FAIL:
                        bleConnFail(jsonObject.getString("content"));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void setListener() {
    }

    private void onBleDeviceFound(final BluetoothDevice device) {
//        device.getName() + "------mac:" + device.getAddress());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emptyRelativeLayout.setVisibility(View.GONE);
                deviceListAdapter.addDevice(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        });

    }

    private void onBlueOn() {
        if (bleManager.isSupportBle()) {
            stopScan();
            scanBleDevice();
        }
    }

    private void onBlueOff() {
        if (bleManager.isSupportBle()) {
            emptyRelativeLayout.setVisibility(View.VISIBLE);
            emptyView.setText("请打开手机蓝牙");
            deviceListAdapter.clearData();
            deviceListAdapter.notifyDataSetChanged();
        }
    }
    private void bleConnFail(String errMsg){
        Toast.makeText(MainActivity.this,errMsg, Toast.LENGTH_LONG).show();
    }

    private void notifyChar() {
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

    private void connectToDevice(final BluetoothDevice device) {
        stopScan();
        bleManager.closeBluetoothGatt();
        Toast.makeText(MainActivity.this, "正在连接...", Toast.LENGTH_SHORT).show();
        bleManager.connectDevice(device, new BleGattCallback() {
            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                Log.i(TAG, "连接成功！");
                gatt.discoverServices();
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
                EventBusUtil.postEvent(AppConst.BLUE_CONN_FAIL,"连接失败或连接中断");
//                Toast.makeText(MainActivity.this, "连接失败或连接中断", Toast.LENGTH_LONG).show();
//              bleManager.handleException(exception);
            }
        });
    }

    private final class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            BluetoothDevice device = deviceListAdapter.getItem(position);
            if (null != device) {
                connectToDevice(device);
            }
        }
    }

    private void initBleManager() {
        bleManager = BaseApplication.getBleManager();
    }

    private void scanBleDevice() {
        startScanAnim();
        bleManager.scanDevice(listScanCallback);
    }

    private void stopScan() {
        stopScanAnim();
        if (bleManager.isInScanning()) {
            bleManager.stopScan(listScanCallback);
        }
    }

    private void initBluetooth() {
        listView.setOnItemClickListener(new OnItemClickListener());
        deviceListAdapter = new DeviceListAdapter(MainActivity.this, R.layout.listview_item_device);
        listView.setAdapter(deviceListAdapter);
        if (!bleManager.isSupportBle()) {
            emptyView.setText("该手机不支持BLE 4.0");
            return;
        }
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!bleManager.isBlueEnable()) {
            emptyView.setText("请打开手机蓝牙");
            return;
        }
        scanBleDevice();
    }

    @OnClick({R.id.iv_lamp})
    public void onLampImageViewClick() {
        if (bleManager.isInScanning()) {
            stopScan();
        } else {
            scanBleDevice();

        }
    }

    public void startScanAnim() {
        if (operatingAnim != null) {
            titleTextView.setText("正在扫描...");
            lampImageView.startAnimation(operatingAnim);
        }
    }

    public void stopScanAnim() {
        if (operatingAnim != null) {
            titleTextView.setText("我的设备");
            lampImageView.clearAnimation();
        }

    }
}
