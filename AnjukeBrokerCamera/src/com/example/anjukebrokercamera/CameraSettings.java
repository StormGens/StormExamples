/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * CameraSettings.java
 *
 */
package com.example.anjukebrokercamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-7
 */
public class CameraSettings {
    private static final String TAG = "CameraSettings";
    private static final int NOT_FOUND = -1;

    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";// 曝光补偿
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";// 拍照分辨率
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";

    public static final String EXPOSURE_DEFAULT_VALUE = "0";// 默认的曝光补偿值

    /**
     * 该方法仅仅是用来升级保存的配置文件的（官方为了考虑他们app的从低级到高级的升级），我们就没必要实现了
     * 
     * @param pref
     */
    public static void upgradePreferences(SharedPreferences pref) {

    }

    public static boolean setCameraPictureSize(String candidate, List<Size> supported,
            Parameters mParameters) {
        int index = candidate.indexOf("x");
        if (index==NOT_FOUND) {
            return false;
        }
        int width = Integer.parseInt(candidate.substring(0, index));
        int height = Integer.parseInt(candidate.substring(index + 1));
        for (Size size : supported) {
            if (size.width == width && size.height == height) {
                mParameters.setPictureSize(width, height);
                return true;
            }
        }
        Log.v(TAG, "没有找到目标尺寸");
        return false;
    }

    public static void initialCameraPictureSize(Context context, Parameters mParameters) {
        // TODO Auto-generated method stub
        List<Size> supported = mParameters.getSupportedPictureSizes();
        if (supported == null) {
            return;
        }
        for (Size size : supported) {
            Log.v(TAG, "支持的拍照尺寸" + size.width + "*" + size.height);
        }
        String[] expected = context.getResources().getStringArray(
                R.array.pref_camera_picturesize_entryvalues);
        for (String candidate : expected) {
            if (setCameraPictureSize(candidate, supported, mParameters)) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString(KEY_PICTURE_SIZE, candidate);
                editor.commit();
                return;
            }
        }

    }

    public static String getOptimalFocuseMode(Context context, Parameters mParameters) {
        List<String> supported = mParameters.getSupportedFocusModes();
        if (supported == null) {
            return null;
        }
        String[] expected = context.getResources().getStringArray(
                R.array.pref_camera_focusemode_entryvalues);
        for (String focuseMode : expected) {
            if ( Util.isSupported(focuseMode, supported)) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString(KEY_FOCUS_MODE, focuseMode);
                editor.commit();
                Log.v(TAG, focuseMode);
                return focuseMode;
            }
        }
        return null;

    }

}
