package com.rebo.bubl.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rebo.magiclamp.R;
import com.rebo.bubl.adapter.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


public class MainActivity extends BaseActivity {

    private static final int RECORD_AUDIO_REQUEST_CODE = 976;
    private static final int REQUEST_ENABLE_BT = 977;
    private static final int ACCESS_FINE_LOCATION_CODE = 988;


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Bind(R.id.lv_device)
    ListView listView;
    @Bind(R.id.rl_empty)
    RelativeLayout emptyRelativeLayout;

    BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private DeviceListAdapter deviceListAdapter;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(this.mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setNavigationTitle("设备");

        //开启定位权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_CODE);
        }
        //开启声音权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},RECORD_AUDIO_REQUEST_CODE);
        }
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

    private final class OnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
                Bundle bundle = new Bundle();
                BluetoothDevice device = devices.get(position);
                bundle.putParcelable("device",device);
                intent.putExtras(bundle);
                MainActivity.this.startActivity(intent);
            }else {
                Toast.makeText(MainActivity.this,"请开启声音权限",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},RECORD_AUDIO_REQUEST_CODE);
            }
        }
    }

    private void initBluetooth(){
        listView.setOnItemClickListener(new OnItemClickListener());
        deviceListAdapter =  new DeviceListAdapter(MainActivity.this,devices,R.layout.listview_item_device);
        listView.setAdapter(deviceListAdapter);

        //是否支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        //获取蓝牙服务，即打开本地蓝牙
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mHandler = new Handler();
        scanLeDevice(true);
    }


    //------------------蓝牙相关------------------
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    // TODO Auto-generated method stub
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("MainActivity","========>:"+device.getName());
                            deviceListAdapter.addDevice(device,rssi);
                            deviceListAdapter.notifyDataSetChanged();
                            emptyRelativeLayout.setVisibility(View.GONE);
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        Log.i("MainActivity","========> scan");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

}
