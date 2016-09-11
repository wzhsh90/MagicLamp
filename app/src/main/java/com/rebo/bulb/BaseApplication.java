package com.rebo.bulb;

import android.app.Application;
import android.content.Context;

import com.clj.fastble.BleManager;

/**
 * Created by wzhsh90 on 2016/9/11.
 */
public class BaseApplication  extends Application {
    private static Context mContext;
    private static BleManager bleManager;
    @Override
    public void onCreate() {
        super.onCreate();
        setContext(this.getApplicationContext());
        bleManager = BleManager.getInstance();
        bleManager.init(BaseApplication.getContext());
    }
    private static synchronized void setContext(Context context) {
        mContext = context;
    }
    public static Context getContext() {
        return mContext;
    }
    public static BleManager getBleManager(){
        return bleManager;
    }
}
