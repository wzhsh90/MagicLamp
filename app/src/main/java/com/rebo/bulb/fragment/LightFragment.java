package com.rebo.bulb.fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.ble.BleCommand;
import com.rebo.bulb.ble.BleConst;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/10.
 */
public class LightFragment extends BaseFragment {

    @BindView(R.id.opacitybar)
    OpacityBar opacityBar;

    @BindView(R.id.colorPicker)
    ColorPicker colorPicker;

    @BindView(R.id.btn_switch)
    ImageView swithcBtn;


    @BindView(R.id.switchOpenClose)
    Switch switchOpenClose;
    private Boolean lampswitch;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light, null);
        ButterKnife.bind(this, view);
        colorPicker.addOpacityBar(opacityBar);
        colorPicker.setShowOldCenterColor(false);

        //监听颜色改变
        colorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                Log.i("LightFragment", "------>color:" + color);
                writeColor(color);
            }
        });
        opacityBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                    Log.i("LightFragment", "------>opacity:" + opacityBar.getOpacity());
                    writeOpacity(opacityBar.getOpacity());
                    return true;
                }
                return false;
            }
        });
//        opacityBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
//            @Override
//            public void onOpacityChanged(int opacity) {
//                Log.i("LightFragment", "------>opacity:" + opacity);
////                writeOpacity(opacity);
//            }
//        });
//        colorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
//            @Override
//            public void onColorChanged(int color) {
//                Log.i("LightFragment", "------>color:" + color);
////                writeColor(color);
//            }
//        });
        lampswitch = false;

        return view;
    }

    @OnCheckedChanged({R.id.switchColor, R.id.switchOpenClose})
    void switchButtonChange(android.widget.CompoundButton btn, boolean checked) {
        switch (btn.getId()) {
            case R.id.switchColor:
                if (checked) {

                } else {

                }
                break;
            case R.id.switchOpenClose:
                if (checked != lampswitch) {
                    onSwitchClick();
                }

                break;
        }
    }

    @Override
    protected void lazyLoad() {

    }

    private void openLight() {
//        write("2");
        write(BleCommand.getAllData(BleCommand.getHead(0, 0), BleCommand.lockOrUnlockBody(false)));
    }

    private void closeLight() {
        write(BleCommand.getAllData(BleCommand.getHead(0, 0), BleCommand.lockOrUnlockBody(true)));
//        write("1");
    }

    private void writeColor(int color) {

//        write(String.valueOf(color));
        write(BleCommand.getAllData(BleCommand.getHead(0, 0), BleCommand.colorBody(0)));
    }

    private void writeOpacity(int opacity) {
//        write(String.valueOf(opacity));
        write(BleCommand.getAllData(BleCommand.getHead(0, 0), BleCommand.colorBody(opacity)));
    }

    private void write(final byte[] data) {
        if (!BaseApplication.getBleManager().isConnected()) {
            return;
        }
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
                        int seq = BleCommand.getCurrentSeq(data);
                        if (seq >= 1) {
                            BleCommand.setCurrentSeq(data, seq - 1);
                            write(data);
                        }
//                        Log.e(TAG, "写读特征值失败: " + '\n' + exception.toString());
//                        bleManager.handleException(exception);
                    }
                });
    }

    private void write(String data) {
        if (!BaseApplication.getBleManager().isConnected()) {
            return;
        }
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

    /**
     * 灯开关事件
     */
    @OnClick({R.id.btn_switch})
    public void onSwitchClick() {
        if (lampswitch == true) {
            swithcBtn.setImageResource(R.mipmap.ic_off);
            lampswitch = false;
            closeLight();

        } else {
            swithcBtn.setImageResource(R.mipmap.ic_on);
            lampswitch = true;
            openLight();
        }

        switchOpenClose.setChecked(lampswitch);
    }

}


