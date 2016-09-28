package com.rebo.bulb.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.rebo.bulb.R;
import com.rebo.bulb.ble.BlueDeviceWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceListAdapter extends CommonAdapter<BlueDeviceWrapper>{
    private TextView mTitleTextView;
    private static final Map<String, String> macMap = new ConcurrentHashMap<String, String>();

    public DeviceListAdapter(Context context,int resource){
        super(context, resource);
    }

    @Override
    public void convert(ViewHolder helper, BlueDeviceWrapper item) {
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

    public void addDevice(BlueDeviceWrapper device){
        if(!mDatas.contains(device)&&!macMap.containsKey(device.getAddress())){
            macMap.put(device.getAddress(),"");
            mDatas.add(device);
            this.notifyDataSetChanged();
        }
    }
    public void clearData(){
        this.mDatas.clear();
        macMap.clear();
        this.notifyDataSetChanged();
    }
}
