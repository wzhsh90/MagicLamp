package com.rebo.bulb.fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.ble.BleConst;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Created by guodunsong on 16/7/10.
 */
public class LightFragment extends BaseFragment {

    @Bind(R.id.opacitybar)
    OpacityBar opacityBar;

    @Bind(R.id.colorPicker)
    ColorPicker colorPicker;

    @Bind(R.id.switchOpenClose)
    Switch switchOpenClose;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light, null);
        ButterKnife.bind(this,view);
        colorPicker.addOpacityBar(opacityBar);
        return view;
    }

    @OnCheckedChanged({R.id.switchColor, R.id.switchOpenClose})
    void switchButtonChange(android.widget.CompoundButton btn,boolean checked){
        switch (btn.getId()){
            case R.id.switchColor:
                 if(checked){

                 }else{

                 }
                break;
            case R.id.switchOpenClose:
                if(checked){
                    openLight();
                }else{
                    closeLight();
                }
                break;
        }
    }
    @Override
    protected void lazyLoad() {

    }
    private void openLight() {
        write("2");
    }

    private void closeLight() {
        write("1");
    }
    private void write(String data) {
        BaseApplication.getBleManager().writeDevice(
                BleConst.RX_SERVICE_UUID,
                BleConst.RX_WRITE_UUID,
                BleConst.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR,
                data,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
//                        Log.d(TAG, "写特征值成功: " + '\n' + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        Log.e(TAG, "写读特征值失败: " + '\n' + exception.toString());
//                        bleManager.handleException(exception);
                    }
                });
    }
}
