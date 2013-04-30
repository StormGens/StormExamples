/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * ComboPreferences.java
 *
 */
package com.example.stormcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.security.spec.MGF1ParameterSpec;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**该类主要是用于保存相机的设置等信息，而这些信息有全局的，也有针对某个摄像头的。所以叫做组合的preferences
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-4-12
 */
public class ComboPreferences implements SharedPreferences,OnSharedPreferenceChangeListener{
    private SharedPreferences mPrefGlobal;  // global preferences   全局配置
    private SharedPreferences mPrefLocal;  // per-camera preferences   单个相机的配置
    private CopyOnWriteArrayList<OnSharedPreferenceChangeListener> mListeners;  // 配置改变时候的监听们
    private static WeakHashMap<Context, ComboPreferences> sMap =
            new WeakHashMap<Context, ComboPreferences>();//用于保存当前读取到内存中的ComboPreferences的map。目测是方便全局访问
    
    public ComboPreferences(Context context) {
        mPrefGlobal=PreferenceManager.getDefaultSharedPreferences(context);
        mPrefGlobal.registerOnSharedPreferenceChangeListener(this);
        synchronized (sMap) {
            sMap.put(context, this);
        }
    }
    
    public static ComboPreferences get(Context context){
        synchronized (sMap) {
            return sMap.get(context);
        }
    }
    /**Sets the camera id and reads its preferences. Each camera has its own
     * preferences.设置cameraId并且读取该相机的配置，每个相机都有其专属的配置信息
     * @param context
     * @param cameraId 相机id
     */
    public void setLocalId(Context context,int cameraId){
        String prefName=context.getPackageName()+"_preference_"+cameraId;
        if (mPrefLocal!=null) {
            unregisterOnSharedPreferenceChangeListener(this);
        }
        mPrefLocal=context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        registerOnSharedPreferenceChangeListener(this);
    }
    
    public SharedPreferences getGlobal() {
        return mPrefGlobal;
    }

    public SharedPreferences getLocal() {
        return mPrefLocal;
    }
    
    private static boolean isGlobal(String key) {
        return key.equals(CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL)
                || key.equals(CameraSettings.KEY_CAMERA_ID)
                || key.equals(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN)
                || key.equals(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN)
                || key.equals(CameraSettings.KEY_VIDEO_EFFECT);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (OnSharedPreferenceChangeListener listener : mListeners) {
            listener.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException(); // Can be implemented if needed.
    }

    @Override
    public String getString(String key, String defValue) {
        if (isGlobal(key) || !mPrefLocal.contains(key)) {
            return mPrefGlobal.getString(key, defValue);
        }else{
            return mPrefLocal.getString(key, defValue);
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        if (isGlobal(key) || !mPrefLocal.contains(key)) {
            return mPrefGlobal.getInt(key, defValue);
        }else{
            return mPrefLocal.getInt(key, defValue);
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        if (isGlobal(key) || !mPrefLocal.contains(key)) {
            return mPrefGlobal.getLong(key, defValue);
        }else{
            return mPrefLocal.getLong(key, defValue);
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        if (isGlobal(key) || !mPrefLocal.contains(key)) {
            return mPrefGlobal.getFloat(key, defValue);
        }else{
            return mPrefLocal.getFloat(key, defValue);
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        if (isGlobal(key) || !mPrefLocal.contains(key)) {
            return mPrefGlobal.getBoolean(key, defValue);
        }else{
            return mPrefLocal.getBoolean(key, defValue);
        }
    }

    @Override
    public boolean contains(String key) {
        boolean result1=mPrefGlobal.contains(key);
        boolean result2=mPrefLocal.contains(key);
        return result1||result2;
    }

    @Override
    public Editor edit() {
        return new MyEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.remove(listener);
    }
 // This method is not used.
    @Override
    public Set<String> getStringSet(String arg0, Set<String> arg1) {
        throw new UnsupportedOperationException();
    }
    
    private class MyEditor implements Editor{

        private Editor mEditorGlobal;
        private Editor mEditorLocal;
        
        public MyEditor() {
            mEditorGlobal=mPrefGlobal.edit();
            mEditorLocal=mPrefLocal.edit();
        }
        
        @Override
        public Editor putString(String key, String value) {
            if (isGlobal(key)) {
                mEditorGlobal.putString(key, value);
            }else{
                mEditorLocal.putString(key, value);
            }
            return null;
        }

     // This method is not used.
        @Override
        public Editor putStringSet(String key, Set<String> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editor putInt(String key, int value) {
            if (isGlobal(key)) {
                mEditorGlobal.putInt(key, value);
            }else{
                mEditorLocal.putInt(key, value);
            }
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            if (isGlobal(key)) {
                mEditorGlobal.putLong(key, value);
            }else{
                mEditorLocal.putLong(key, value);
            }
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            if (isGlobal(key)) {
                mEditorGlobal.putFloat(key, value);
            }else{
                mEditorLocal.putFloat(key, value);
            }
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            if (isGlobal(key)) {
                mEditorGlobal.putBoolean(key, value);
            }else{
                mEditorLocal.putBoolean(key, value);
            }
            return this;
        }

        @Override
        public Editor remove(String key) {
            mEditorGlobal.remove(key);
            mEditorLocal.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            mEditorGlobal.clear();
            mEditorLocal.clear();
            return this;
        }

        @Override
        public boolean commit() {
            boolean result1=mEditorGlobal.commit();
            boolean result2=mEditorLocal.commit();
            return result1&&result2;
        }

        @Override
        public void apply() {
            mEditorGlobal.apply();
            mEditorLocal.apply();
        }
        
    }
    
    

}
