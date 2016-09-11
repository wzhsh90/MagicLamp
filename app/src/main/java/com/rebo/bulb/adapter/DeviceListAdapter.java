package com.rebo.bulb.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rebo.bulb.R;

/**
 * Created by guodunsong on 16/7/9.
 */
public class DeviceListAdapter extends CommonAdapter<BluetoothDevice>{
    private Context context;
    private TextView mTitleTextView;
    private Button mConnectButton;

    public DeviceListAdapter(Context context,int resource){
        super(context, resource);
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

    public void addDevice(BluetoothDevice device){
        if(!mDatas.contains(device)){
            mDatas.add(device);
        }
    }
}
