package com.rebo.bulb.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import butterknife.ButterKnife;

/**
 * Created by guodunsong on 16/7/8.
 */
public abstract class BaseFragment extends Fragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //fragment 当前状态是否可见
    protected boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()){
            isVisible = true;
            onVisible();
        }else {
            isVisible = false;
            onInVisible();
        }
    }

    /**
     * 可见
     */
    protected void onVisible(){
        lazyLoad();
    }

    /**
     * 不可见
     */
    protected void onInVisible(){

    }

    /**
     * 延迟加载
     * 子类必须重写此方法
     */
    protected abstract void lazyLoad();

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
