package com.rebo.bulb.permission;

import java.util.List;

/**
 * Created by wzhsh90 on 2016/7/29.
 */
public interface PermissionCallBack {

    void onGranted();
    void onDenied(List<String> permissions);
}
