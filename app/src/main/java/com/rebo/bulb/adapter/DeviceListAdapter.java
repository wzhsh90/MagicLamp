package com.rebo.bulb.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.rebo.bulb.R;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceListAdapter extends CommonAdapter<BluetoothDevice>{
    private Context context;
    private TextView mTitleTextView;

    public DeviceListAdapter(Context context,int resource){
        super(context, resource);
        this.context = context;
    }

    @Override
    public void convert(ViewHolder helper, BluetoothDevice item) {
        View convertView = helper.getConvertView();
        mTitleTextView = (TextView)convertView.findViewById(R.id.tv_title);
        String name="";
        if(Strings.isNullOrEmpty(item.getName())){
            name=item.getAddress();
        }else{
            name=item.getName();
        }
        mTitleTextView.setText(name);
    }

    public void addDevice(BluetoothDevice device){
        if(!mDatas.contains(device)){
            mDatas.add(device);
        }
    }
    public void clearData(){
        this.mDatas.clear();
    }
}
