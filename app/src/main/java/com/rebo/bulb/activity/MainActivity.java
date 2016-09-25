package com.rebo.bulb.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.scan.ListScanCallback;
import com.rebo.bulb.AppConst;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.adapter.DeviceListAdapter;
import com.rebo.bulb.ble.BleConst;
import com.rebo.bulb.permission.PermissionCallBack;
import com.rebo.bulb.permission.PermissionUtil;
import com.rebo.bulb.utils.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;


public class MainActivity extends BaseActivity {

    private static final String TAG = "magic_ble";
    private static final byte[] lock = new byte[0];

    @BindView(R.id.lv_device)
    ListView listView;
    @BindView(R.id.rl_empty)
    RelativeLayout emptyRelativeLayout;
    BluetoothAdapter mBluetoothAdapter;
    @BindView(R.id.tv_empty)
    TextView emptyView;

    @BindView(R.id.iv_lamp)
    ImageView lampImageView;
    @BindView(R.id.tv_home_title)
    TextView titleTextView;

    @OnItemClick(R.id.lv_device)
    void onItemClick(int position) {
        BluetoothDevice device = deviceListAdapter.getItem(position);
        if (null != device) {
            connectToDevice(device);
        }
    }

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
            EventBusUtil.postEvent(AppConst.BLUE_STOP_SCAN, "");
            Log.i(TAG, "搜索时间结束");
        }

    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBusUtil.registerEvent(this);
        if (this.mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setNavigationTitle("设备");
        spinAnimation();
        initBleManager();
        initBluetooth();
    }

    private void spinAnimation() {
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
                    case AppConst.BLUE_STOP_SCAN:
                        stopTimeout();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopTimeout() {
        scanNoDevice();
        stopScanAnim();
    }

    @Override
    protected void setListener() {
    }

    private void onBleDeviceFound(final BluetoothDevice device) {
//        System.out.println( device.getName() + "------mac:" + device.getAddress());;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emptyRelativeLayout.setVisibility(View.GONE);
                deviceListAdapter.addDevice(device);
            }
        });

    }

    private void onBlueOn() {
        if (bleManager.isSupportBle()) {
            if (emptyView.getVisibility() == View.VISIBLE) {
                emptyView.setText("暂无数据");
            }
            stopScan();
            scanBleDevice();
        }
    }

    private void resetEmptyData(String msg) {
        emptyRelativeLayout.setVisibility(View.VISIBLE);
        emptyView.setText(msg);
    }

    private void onBlueOff() {
        if (bleManager.isSupportBle()) {
            resetEmptyData("请打开手机蓝牙");
            clearDeviceListData();
        }
    }

    private void connectToDevice(final BluetoothDevice device) {

        bleManager.closeBluetoothGatt();
        Toast.makeText(MainActivity.this, "正在连接...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("device", device);
        intent.putExtras(bundle);
        MainActivity.this.startActivity(intent);
        stopScan();

    }


    private void initBleManager() {
        bleManager = BaseApplication.getBleManager();
    }

    private void clearDeviceListData() {
        if (deviceListAdapter.getCount() >= 1) {
            deviceListAdapter.clearData();

        }

    }

    private void scanBleDevice() {

        PermissionUtil.getInstance(this).requestPermissions(new PermissionCallBack() {
            @Override
            public void onGranted() {
                bleManager.closeBluetoothGatt();
                clearDeviceListData();
                bleManager.stopScan(listScanCallback);
                startScanAnim();
                bleManager.scanDevice(listScanCallback);
            }

            @Override
            public void onDenied(List<String> permissions) {
            }
        }, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void stopScan() {
        bleManager.closeBluetoothGatt();
        stopScanAnim();
        if (bleManager.isInScanning()) {
            bleManager.stopScan(listScanCallback);
        }
        scanNoDevice();

    }

    private void scanNoDevice() {
        if (deviceListAdapter.getCount() == 0) {
            resetEmptyData("暂无数据");
        }
    }

    private void initBluetooth() {
        deviceListAdapter = new DeviceListAdapter(MainActivity.this, R.layout.listview_item_device);
        listView.setAdapter(deviceListAdapter);
//        listView.setOnItemClickListener(new OnItemClickListener());
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
        if (bleManager.isSupportBle() && bleManager.isBlueEnable()) {
            if (bleManager.isInScanning()) {
                stopScan();
            } else {
                scanBleDevice();
            }
        }
//        Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("device",null);
//        intent.putExtras(bundle);
//        MainActivity.this.startActivity(intent);
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
