/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * BaseActivity.java
 *
 */
package com.example.stormcamera;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

/**主要是做一些和屏幕解锁时候相机使用状态的处理（在解锁页面等地方不获取相机）。
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-4-12
 */
abstract public class BaseActivity extends Activity {
    protected Camera mCameraDevice;
    private boolean mOnResumePending;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent software keyboard or voice search from showing up.
        // 阻止在长按菜单键时虚拟键盘的弹出、阻止在长按搜索键时的语音搜索的弹出
        if (keyCode==KeyEvent.KEYCODE_SEARCH||keyCode==KeyEvent.KEYCODE_MENU) {
            if (event.isLongPress()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        Log.v("zlq", "----onPause");
        mOnResumePending=false;
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Don't grab the camera if in use by lockscreen. For example, face
        // unlock may be using the camera. Camera may be already opened in
        // onCreate. doOnResume should continue if mCameraDevice != null.
        // Suppose camera app is in the foreground. If users turn off and turn
        // on the screen very fast, camera app can still have the focus when the
        // lock screen shows up. The keyguard takes input focus, so the caemra
        // app will lose focus when it is displayed.
        //在锁屏状态下我们的程序不要抢占camera，比如，在面部解锁中可能会用到相机。
        //camera可能已经在onCreate里面打开了，如果mCameraDevice！=null。doOnResume应该继续
        //假设一种场景：我们的app在前面，用户如果快速的开关屏幕，在屏幕点亮时app可以继续拥有焦点
        //键盘守卫夺去了输入焦点，所以app将会失去焦点。
        //--by zlq:简单点说就是keyguard出现的时候，我们就把mOnResumePending标记为true，
        //延后的做doOnResume，以避免和面部解锁等操作争夺相机。
        Log.v("zlq", isKeyguardLocked()+"----onResume");
        if (mCameraDevice==null&&isKeyguardLocked()) {
            mOnResumePending=true;
        }else{
            doOnResume();
            mOnResumePending=false;
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.v("zlq", "onwindowsFOcused--"+hasFocus);
        if (hasFocus&&mOnResumePending) {
            doOnResume();
            mOnResumePending=false;
        }
    }
    
    // Put the code of onResume in this method.
    abstract protected void doOnResume();
    
    /**
     * @return
     */
    private boolean isKeyguardLocked(){
        KeyguardManager kgm=(KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return (kgm!=null&&kgm.isKeyguardLocked()&&kgm.isKeyguardSecure());
    }
}
