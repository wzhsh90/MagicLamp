package com.rebo.bulb.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
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

import com.clj.fastble.bluetooth.BleGattCallback;
import com.clj.fastble.exception.BleException;
import com.rebo.bulb.AppConst;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.adapter.MusicListAdapter;
import com.rebo.bulb.fragment.LightFragment;
import com.rebo.bulb.fragment.MusicFragment;
import com.rebo.bulb.model.MusicModel;
import com.rebo.bulb.permission.PermissionCallBack;
import com.rebo.bulb.permission.PermissionUtil;
import com.rebo.bulb.utils.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceDetailActivity extends BaseActivity {

    @BindViews({R.id.tab_light, R.id.tab_music})
    List<RadioButton> mTabs;

    ListView mMusicListView;
    MusicListAdapter musicListAdapter;

    private LightFragment tabLight;
    private MusicFragment tabMusic;
    private Integer selectedIndex;
    private final static int TAB_MUSIC = 1;
    private final static int TAB_LIGHT = 0;
    private static final int RECORD_AUDIO_REQUEST_CODE = 976;
    private static final String TAG = "magic_ble";

    private BluetoothDevice device;
    BottomSheetDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        this.setNavigationTitle("设备名称");
        EventBusUtil.registerEvent(this);
        setSelect(TAB_LIGHT);
        Bundle bundle = getIntent().getExtras();
        device = (BluetoothDevice) bundle.get("device");
        connectToDevice(device);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerEventBus(JSONObject jsonObject) {
        if (null != jsonObject) {
            String code;
            try {
                code = jsonObject.getString("code");
                switch (code) {
                    case AppConst.BLUE_CONN_FAIL:
                        bleConnFail(jsonObject.getString("content"));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void bleConnFail(String errMsg) {
        Toast.makeText(DeviceDetailActivity.this, errMsg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void connectToDevice(final BluetoothDevice device) {
        BaseApplication.getBleManager().connectDevice(device, new BleGattCallback() {
            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                gatt.discoverServices();
                EventBusUtil.postEvent(AppConst.BLUE_CONN133, "");
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.i(TAG, "服务被发现！");
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.i(TAG, "连接失败或连接中断：" + '\n' + exception.toString());
                EventBusUtil.postEvent(AppConst.BLUE_CONN_FAIL, "连接失败或连接中断");

            }
        });
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
        if (item.getItemId() == R.id.action_music_list) {
            showMusicList();
        } else if (item.getItemId() == android.R.id.home) {
            BaseApplication.getBleManager().closeBluetoothGatt();
            if(null!=tabMusic){
                tabMusic.pauseAndStop();
            }
        }
        boolean res = super.onOptionsItemSelected(item);
        return res;
    }

    @Override
    protected void setListener() {

    }

    //显示音乐列表
    private void showMusicList() {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_music, null);
        mMusicListView = (ListView) linearLayout.findViewById(R.id.lv_music);
        mMusicListView.setOnItemClickListener(new OnItemClickListener());
        musicListAdapter = new MusicListAdapter(DeviceDetailActivity.this, getData(), R.layout.listview_item_music);
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
                setSelect(TAB_LIGHT);
                break;
            case R.id.tab_music:
                PermissionUtil.getInstance(this).requestPermissions(new PermissionCallBack() {
                    @Override
                    public void onGranted() {
                        setSelect(TAB_MUSIC);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
                break;
        }
    }

    private void setSelect(final int i) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selectedIndex = i;
                if (i == 0) {
                    setNavigationTitle("光控");
                } else if (i == 1) {
                    setNavigationTitle("音乐");
                }

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                hideFragment(transaction);

                // 把图片设置为亮的
                // 设置内容区域
                switch (i) {
                    case 0:
                        if (tabLight == null) {
                            tabLight = new LightFragment();
                            transaction.add(R.id.id_content, tabLight);
                        } else {
                            transaction.show(tabLight);
                        }
                        mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_control_pressed, 0, 0);
                        break;
                    case 1:
                        if (tabMusic == null) {
                            tabMusic = new MusicFragment();
                            transaction.add(R.id.id_content, tabMusic);
                        } else {
                            transaction.show(tabMusic);
                        }

                        mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_music_pressed, 0, 0);
                        break;
                    default:
                        break;
                }
                try {
                    transaction.commitAllowingStateLoss();
                    invalidateOptionsMenu();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void hideFragment(FragmentTransaction transaction) {
        if (tabLight != null) {
            transaction.hide(tabLight);
        }
        if (tabMusic != null) {
            transaction.hide(tabMusic);
        }
    }

    /**
     * 切换图片至暗色
     */
    private void resetImgs() {
        mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_control_normal, 0, 0);
        mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_music_normal, 0, 0);
    }


    private final class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MusicModel musicModel = musicListAdapter.getMusicModel(position);
            tabMusic.playMusic(musicModel);
            dialog.dismiss();
        }
    }

    public List<MusicModel> getData() {
        List<MusicModel> list = new ArrayList<MusicModel>();
        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
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

    @Override
    protected void onDestroy() {
        BaseApplication.getBleManager().closeBluetoothGatt();
        EventBusUtil.unRegisterEvent(this);
        super.onDestroy();

    }
}
