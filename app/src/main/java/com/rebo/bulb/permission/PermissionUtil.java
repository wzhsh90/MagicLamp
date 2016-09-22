package com.rebo.bulb.permission;

import android.content.Context;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;

/**
 * Created by wzhsh90 on 2016/7/29.
 */
public final class PermissionUtil {
    private static PermissionUtil mInstance;
    private final static byte[] lock= new byte[0];
    private static Acp acp;
    private PermissionUtil() {
    }

    public static PermissionUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (lock) {
                if (mInstance == null) {
                    mInstance = new PermissionUtil();
                    acp = Acp.getInstance(context);
                }
            }
        }
        return mInstance;
    }

    public void requestPermissions(final PermissionCallBack callBack, String... mPermissions) {
        if (callBack == null) new NullPointerException("callBack is null...");
        acp.request(new AcpOptions.Builder()
                        .setPermissions(mPermissions)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        callBack.onGranted();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        callBack.onDenied(permissions);
                    }
                });
    }
}
