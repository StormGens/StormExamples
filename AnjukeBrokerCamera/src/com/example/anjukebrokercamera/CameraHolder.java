/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * CameraHolder.java
 *
 */
package com.example.anjukebrokercamera;
import static com.example.anjukebrokercamera.Util.Assert;

import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * 这个类是用来持有一个android.hardware.Camera对象实例
 * 
 * @author liqiangzhang (liqiangzhang@anjuke.com)
 * @date 2013-5-8
 */
public class CameraHolder {
    private final String TAG = "CameraHolder";
    private android.hardware.Camera mCameraDevice;
    // 当我们打开相机时保存相机的parameters，以便我们在用户随后的open()请求时候
    // 回复他们。这样做保证了相机的在CameraActivity里面的parameters的设置不会被用户不经意的ViedoActivity 影响
    private Parameters mParameters;
    private int mUsers = 0; // 调用open()的次数 - 调用 release()的次数

    private long mKeepBeforeTime = 0; // time.在这个时间点之前持有相机
    private final Handler mHandler;
    private static final int RELEASE_CAMERA = 1;
    // Use a singleton.
    private static CameraHolder sHolder;

    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }

    public CameraHolder() {
        HandlerThread ht = new HandlerThread("CameraHolder");
        ht.start();
        mHandler = new Handler(ht.getLooper());
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RELEASE_CAMERA:
                    synchronized (CameraHolder.this) {
                        // 在CameraHolder.open方法中，如果发现消息队列中有RELEASE_CAMERA,
                        // 会移除他，但是也有一种可能是，在移除之前，已经开始处理这个消息了，所以
                        // 这里还是需要一个判断
                        if (CameraHolder.this.mUsers == 0) {
                            releaseCamera();
                        }
                    }
                    break;
            }
        }
    }

    public synchronized android.hardware.Camera open() throws CameraHardwareException {
        Assert(mUsers == 0);
        if (mCameraDevice == null) {
            try {
                mCameraDevice = android.hardware.Camera.open();
            } catch (Exception e) {
                throw new CameraHardwareException(e);
            }
            mParameters = mCameraDevice.getParameters();
        } else {
            try {
                mCameraDevice.reconnect();
            } catch (Exception e) {
                throw new CameraHardwareException(e);
            }
            mCameraDevice.setParameters(mParameters);
        }
        mKeepBeforeTime = 0;
        ++mUsers;
        mHandler.removeMessages(RELEASE_CAMERA);
        Log.v(TAG, "得到相机设备成功");
        return mCameraDevice;
    }

    public synchronized void keep() {
        // 允许mUser==0，因为在用户在菜单里面切换相机时候，Activity可能还没有机会去
        // 调用open()方法。
        Assert(mUsers == 1 || mUsers == 0);
        // 持有相机三秒
        mKeepBeforeTime = SystemClock.currentThreadTimeMillis() + 3000;
    }

    public synchronized void release() {
        Assert(mUsers == 1);
        mCameraDevice.stopPreview();
        --mUsers;
        releaseCamera();
    }

    private void releaseCamera() {
        Assert(mUsers == 0);
        long now = SystemClock.currentThreadTimeMillis();
        if (now < mKeepBeforeTime) {
            mHandler.sendEmptyMessageDelayed(RELEASE_CAMERA, mKeepBeforeTime - now);
            return;
        }
        mCameraDevice.release();
        mCameraDevice = null;
    }

}
