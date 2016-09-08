package com.rebo.bubl.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rebo.magiclamp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceListAdapter extends CommonAdapter<BluetoothDevice>{
    private Context context;
    private TextView mTitleTextView;
    private Button mConnectButton;
    private List<BluetoothDevice> mDatas;
    private List<Integer> rssis;


    public DeviceListAdapter(Context context, List<BluetoothDevice> mDatas, int resource){
        super(context, mDatas, resource);
        this.rssis = new ArrayList<Integer>();
        this.mDatas = mDatas;
        this.context = context;
    }

    @Override
    public void convert(ViewHolder helper, BluetoothDevice item) {
        View convertView = helper.getConvertView();
        mTitleTextView = (TextView)convertView.findViewById(R.id.tv_title);
        mConnectButton = (Button)convertView.findViewById(R.id.btn_connect);
//        mConnectButton.setText();
        mTitleTextView.setText(item.getName());
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"正在连接",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addDevice(BluetoothDevice device, int rssi){
        if(!mDatas.contains(device)){
            mDatas.add(device);
            rssis.add(rssi);
        }
    }

}
