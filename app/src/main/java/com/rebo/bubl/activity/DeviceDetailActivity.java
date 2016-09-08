package com.rebo.bubl.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.rebo.magiclamp.R;
import com.rebo.bubl.adapter.MusicListAdapter;
import com.rebo.bubl.bluetooth.BluetoothLeService;
import com.rebo.bubl.bluetooth.Utils;
import com.rebo.bubl.fragment.LightFragment;
import com.rebo.bubl.fragment.MusicFragment;
import com.rebo.bubl.model.MusicModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceDetailActivity extends BaseActivity{

    @Bind({R.id.tab_light, R.id.tab_music})
    List<RadioButton> mTabs;
    ListView mMusicListView;
    MusicListAdapter musicListAdapter;

    private Fragment mTab01;
    private Fragment mTab02;
    private Integer selectedIndex;

    private BluetoothDevice device;
    private final static String TAG = DeviceDetailActivity.class.getSimpleName();
    private static BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private String status="disconnected";
    BottomSheetDialog dialog;
    private static BluetoothGattCharacteristic target_chara=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        this.setNavigationTitle("设备名称");
        setSelect(1);
        setSelect(0);

//        mMusicListView.setOnItemClickListener(new OnItemClickListener());
//        mMusicListView.setAdapter(new MusicListAdapter(DeviceDetailActivity.this,getData(),R.layout.listview_item_music));

        Bundle bundle = getIntent().getExtras();
        device = (BluetoothDevice)bundle.get("device");
        mDeviceAddress = device.getAddress();
        //初始化蓝牙
        initBluetooth();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (selectedIndex == 1) {
            getMenuInflater().inflate(R.menu.menu_music, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean res = super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.action_music_list) {
            showMusicList();
        }
        return res;
    }


    @Override
    protected void setListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //显示音乐列表
    private void showMusicList(){
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_music,null);
        mMusicListView = (ListView)linearLayout.findViewById(R.id.lv_music);
        mMusicListView.setOnItemClickListener(new OnItemClickListener());
        musicListAdapter =  new MusicListAdapter(DeviceDetailActivity.this,getData(),R.layout.listview_item_music);
        mMusicListView.setAdapter(musicListAdapter);
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(linearLayout);
        dialog.show();
    }

    @OnClick({R.id.tab_light, R.id.tab_music})
    void tabbarClick(View view) {
        resetImgs();
        switch (view.getId()) {
            case R.id.tab_light:
                setSelect(0);
                break;
            case R.id.tab_music:
                setSelect(1);
                break;
        }
    }

    private void setSelect(int i) {
        selectedIndex = i;
        if(i==0){
            this.setNavigationTitle("光控");
        }else if(i==1){
            this.setNavigationTitle("音乐");
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);

        // 把图片设置为亮的
        // 设置内容区域
        switch (i) {
            case 0:
                if (mTab01 == null){
                    mTab01 = new LightFragment();
                    transaction.add(R.id.id_content, mTab01);
                }else {
                    transaction.show(mTab01);
                }
                mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0,R.mipmap.ic_control_pressed, 0, 0);
                break;
            case 1:
                if (mTab02 == null){
                    mTab02 = new MusicFragment();
                    transaction.add(R.id.id_content, mTab02);
                }else {
                    transaction.show(mTab02);
                }

                mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0,R.mipmap.ic_music_pressed, 0, 0);
                break;
            default:
                break;
        }
        transaction.commit();
        invalidateOptionsMenu();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (mTab01 != null) {
            transaction.hide(mTab01);
        }
        if (mTab02 != null) {
            transaction.hide(mTab02);
        }

    }

    /**
     * 切换图片至暗色
     */
    private void resetImgs() {
        mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0,R.mipmap.ic_control_normal, 0, 0);
        mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0,R.mipmap.ic_music_normal, 0, 0);
    }


    private final class OnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MusicModel musicModel = musicListAdapter.getMusicModel(position);
            ((MusicFragment)mTab02).playMusic(musicModel);
            dialog.dismiss();
        }
    }

    public List<MusicModel> getData(){
        List<MusicModel> list = new ArrayList<MusicModel>();
        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int i = 0;
        for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            MusicModel model = new MusicModel();
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String uriData = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

            model.setMusicId(i++);
            model.setName(title);
            model.setPath(uriData);
            list.add(model);
        }

        return list;
    }


    //-------------蓝牙相关------------
    private void initBluetooth(){
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                status="connected";
                updateConnectionState(status);
                System.out.println("BroadcastReceiver :"+"device connected");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                status="disconnected";
                updateConnectionState(status);
                System.out.println("BroadcastReceiver :"+"device disconnected");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                System.out.println("BroadcastReceiver :"+"device SERVICES_DISCOVERED");
            }
        }
    };

    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DeviceDetailActivity.this,status,Toast.LENGTH_SHORT).show();
            }
        });
    }



    //------------蓝牙写入数据-------------
    public static void write(String s)
    {
        final int charaProp = target_chara.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            //注意: 以下读取的值 通过 BluetoothGattCallback#onCharacteristicRead() 函数返回
            target_chara.setValue(s);
            mBluetoothLeService.writeCharacteristic(target_chara);
        }

    }
    public static void read() {
        mBluetoothLeService.setOnDataAvailableListener(mOnDataAvailable);
        mBluetoothLeService.readCharacteristic(target_chara);

    }

    public static byte[] read_byteArray() {
        mBluetoothLeService.setOnDataAvailableListener(mOnDataAvailable);
        return mBluetoothLeService.readCharacteristic_ByteArray(target_chara);

    }

    private static BluetoothLeService.OnDataAvailableListener mOnDataAvailable = new com.rebo.bubl.bluetooth.BluetoothLeService.OnDataAvailableListener(){

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG,"onCharacteristicRead "+gatt.getDevice().getName()
                        +" read "
                        +characteristic.getUuid().toString()
                        +" -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
                mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            Log.e(TAG,"onCharacteristicWrite "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e(TAG,"onCharacteristicChanged "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

        }
    };
}
