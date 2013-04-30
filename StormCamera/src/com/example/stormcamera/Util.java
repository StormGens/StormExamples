/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * Util.java
 *
 */
package com.example.stormcamera;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-4-12
 */
public class Util {

    // Private intent extras. Test only.
    private static final String EXTRAS_CAMERA_FACING =
            "android.intent.extras.CAMERA_FACING";
    
    
    // This is for test only. Allow the camera to launch the specific camera.
    
    /**这个方法仅仅用来测试，允许启动特定的相机
     * @param currentActivity
     * @return
     */
    public static int getCameraFacingIntentExtras(Activity currentActivity) {
        int cameraId = -1;

        int intentCameraId =
                currentActivity.getIntent().getIntExtra(Util.EXTRAS_CAMERA_FACING, -1);

        if (isFrontCameraIntent(intentCameraId)) {
            // Check if the front camera exist
            // 检查前置摄像头是否存在
            int frontCameraId = CameraHolder.instance().getFrontCameraId();
            if (frontCameraId != -1) {
                cameraId = frontCameraId;
            }
        } else if (isBackCameraIntent(intentCameraId)) {
            // Check if the back camera exist
            // 检查后置摄像头是否存在
            int backCameraId = CameraHolder.instance().getBackCameraId();
            if (backCameraId != -1) {
                cameraId = backCameraId;
            }
        }
        return cameraId;
    }
    
    /**检查参数cond是不是为真，若不为真，则抛出异常。
     * @param cond
     */
    public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }
    /* 是不是前置摄像头
     * @param intentCameraId
     * @return
     */
    private static boolean isFrontCameraIntent(int intentCameraId) {
        return (intentCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    private static boolean isBackCameraIntent(int intentCameraId) {
        return (intentCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
    }
    
    public static android.hardware.Camera openCamera(Activity activity, int cameraId)
            throws CameraHardwareException, CameraDisabledException {
        // Check if device policy has disabled the camera.
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }

        try {
            return CameraHolder.instance().open(cameraId);
        } catch (CameraHardwareException e) {
            // In eng build, we throw the exception so that test tool
            // can detect it and report it
            if ("eng".equals(Build.TYPE)) {
                throw new RuntimeException("openCamera failed", e);
            } else {
                throw e;
            }
        }
    }
}
