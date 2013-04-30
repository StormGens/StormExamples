/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * CameraSettings.java
 *
 */
package com.example.stormcamera;

import android.content.SharedPreferences;


/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-4-12
 */
public class CameraSettings {
    private static final int NOT_FOUND = -1;

    public static final int CURRENT_VERSION = 5;
    
    public static final String KEY_VERSION = "pref_version_key";
    public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
//    public static final String KEY_RECORD_LOCATION = RecordLocationPreference.KEY;
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN = "pref_camera_first_use_hint_shown_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN = "pref_video_first_use_hint_shown_key";

    /**升级Global设置，不太懂里面为什么这么写，应该是兼容老版本。
     * 在老版本升级时候让其转化为新的数据
     * @param pref 
     */
    public static void upgradeGlobalPreferences(SharedPreferences pref) {
        int version;
        try {
            version = pref.getInt(KEY_VERSION, 0);
        } catch (Exception ex) {
            version = 0;
        }
        if (version == CURRENT_VERSION) return;

        SharedPreferences.Editor editor = pref.edit();
        if (version == 0) {
            // We won't use the preference which change in version 1.
            // So, just upgrade to version 1 directly
            version = 1;
        }
        if (version == 1) {
            // Change jpeg quality {65,75,85} to {normal,fine,superfine}
            String quality = pref.getString(KEY_JPEG_QUALITY, "85");
            if (quality.equals("65")) {
                quality = "normal";
            } else if (quality.equals("75")) {
                quality = "fine";
            } else {
                quality = "superfine";
            }
            editor.putString(KEY_JPEG_QUALITY, quality);
            version = 2;
        }
        if (version == 2) {
            //TODO 暂时不做这个，等弄明白了设置那一块，再来看这个内容。
//            editor.putString(KEY_RECORD_LOCATION,
//                    pref.getBoolean(KEY_RECORD_LOCATION, false)
//                    ? RecordLocationPreference.VALUE_ON
//                    : RecordLocationPreference.VALUE_NONE);
//            version = 3;
        }
        if (version == 3) {
            // Just use video quality to replace it and
            // ignore the current settings.
            editor.remove("pref_camera_videoquality_key");
            editor.remove("pref_camera_video_duration_key");
        }

        editor.putInt(KEY_VERSION, CURRENT_VERSION);
        editor.apply();
    }
    
    public static int readPreferredCameraId(SharedPreferences pref) {
        return pref.getInt(KEY_CAMERA_ID, 0);//TODO 此处还不清楚为什么google使用String类型的，我们暂时先使用int型的。
    }
}
