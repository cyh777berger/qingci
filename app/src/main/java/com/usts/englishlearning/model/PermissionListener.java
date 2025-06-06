package com.usts.englishlearning.model;

import java.util.List;

public interface PermissionListener {

    //已授权
    void onGranted();

    //未授权
    void onDenied(List<String> deniedPermission);


}
