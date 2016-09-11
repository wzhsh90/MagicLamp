package com.rebo.bulb.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;

import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.adapter.MusicListAdapter;
import com.rebo.bulb.fragment.LightFragment;
import com.rebo.bulb.fragment.MusicFragment;
import com.rebo.bulb.model.MusicModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceDetailActivity extends BaseActivity {

    @Bind({R.id.tab_light, R.id.tab_music})
    List<RadioButton> mTabs;

    ListView mMusicListView;
    MusicListAdapter musicListAdapter;

    private Fragment mTab01;
    private Fragment mTab02;
    private Integer selectedIndex;
    private final static int TAB_MUSIC = 1;
    private final static int TAB_LIGHT = 0;

    //    private BluetoothDevice device;
    BottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        this.setNavigationTitle("设备名称");
        setSelect(TAB_MUSIC);
        setSelect(TAB_LIGHT);
//        Bundle bundle = getIntent().getExtras();
//        device = (BluetoothDevice) bundle.get("device");
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
        if (item.getItemId() == R.id.action_music_list) {
            showMusicList();
        }
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
                setSelect(TAB_MUSIC);
                break;
        }
    }

    private void setSelect(int i) {
        selectedIndex = i;
        if (i == 0) {
            this.setNavigationTitle("光控");
        } else if (i == 1) {
            this.setNavigationTitle("音乐");
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);

        // 把图片设置为亮的
        // 设置内容区域
        switch (i) {
            case 0:
                if (mTab01 == null) {
                    mTab01 = new LightFragment();
                    transaction.add(R.id.id_content, mTab01);
                } else {
                    transaction.show(mTab01);
                }
                mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_control_pressed, 0, 0);
                break;
            case 1:
                if (mTab02 == null) {
                    mTab02 = new MusicFragment();
                    transaction.add(R.id.id_content, mTab02);
                } else {
                    transaction.show(mTab02);
                }

                mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_music_pressed, 0, 0);
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
        mTabs.get(0).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_control_normal, 0, 0);
        mTabs.get(1).setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_music_normal, 0, 0);
    }


    private final class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MusicModel musicModel = musicListAdapter.getMusicModel(position);
            ((MusicFragment) mTab02).playMusic(musicModel);
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
        super.onDestroy();
        BaseApplication.getBleManager().closeBluetoothGatt();
    }

}
