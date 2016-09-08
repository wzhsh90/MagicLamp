package com.rebo.bubl.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rebo.magiclamp.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by guodunsong on 16/7/10.
 */
public class LightFragment extends BaseFragment {

    @Bind(R.id.opacitybar)
    OpacityBar opacityBar;

    @Bind(R.id.colorPicker)
    ColorPicker colorPicker;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light, null);
        ButterKnife.bind(this,view);
        colorPicker.addOpacityBar(opacityBar);
        return view;
    }


    @Override
    protected void lazyLoad() {

    }
}
