package com.rebo.bulb.service;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.common.base.Strings;
import com.rebo.bulb.AppConst;
import com.rebo.bulb.utils.EventBusUtil;

/**
 * Created by wzhsh90 on 2016/4/12.
 */
public class BluetoothStateReceiver extends BroadcastReceiver {

    private String TAG = "BluetoothStateReceiver";
    private String CHECK_STATE = "check_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Strings.isNullOrEmpty(action) && CHECK_STATE.equals(intent.getAction())) {
            BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
            int blueState = blueadapter.getState();
            bleStateSwitch(blueState);
        } else {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            bleStateSwitch(blueState);
        }

    }

    private void bleStateSwitch(int blueState) {
        switch (blueState) {
            case BluetoothAdapter.STATE_OFF:
                EventBusUtil.postEvent(AppConst.BLUE_OFF,"");
                break;
            case BluetoothAdapter.STATE_ON:
                EventBusUtil.postEvent(AppConst.BLUE_ON,"");
                break;
            default:
                break;
        }
    }
}
