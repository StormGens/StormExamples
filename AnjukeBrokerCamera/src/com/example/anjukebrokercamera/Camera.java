
package com.example.anjukebrokercamera;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.util.List;

public class Camera extends NoSearchActivity implements SurfaceHolder.Callback, OnClickListener,
        ShutterButton.OnShutterButtonListener {
    private final String TAG = "CameraActivity";
    // 变量
    private android.hardware.Camera mCameraDevice; // 相机的硬件设备
    private SurfaceView mSurfaceView; // 用作预览区的mSurfaceView
    private SurfaceHolder mSurfaceHolder = null; // 相机预览区域的holder
    // 配置相关
    private SharedPreferences mPreferences; // 本app的sharepreference文件,很多时候的配置修改都要保存到这里来
    private Parameters mParameters; // 相机的配置
    private Parameters mInitialParams; // 相机的初始配置
    private String mFocusMode; // 相机对焦模式

    // 在setCameraParameters()中需要更新的parameters的子集
    private static final int UPDATE_PARAM_INITIALIZE = 1; // 0001
    private static final int UPDATE_PARAM_ZOOM = 2; // 0010
    private static final int UPDATE_PARAM_PREFERENCE = 4; // 0100
    private static final int UPDATE_PARAM_ALL = -1; // 1111

    // 当setCameraParametersWhenIdle()被调用时，我们把需要跟新的子集积累到到mUpdateSet中
    private int mUpdateSet;

    // 各种状态标记
    private boolean mStartPreviewFail = false; // 启动相机预览是不是成功

    private boolean mPreviewing;// 是不是正在预览

    // activity的状态标记
    private boolean mPausing;

    // 对焦状态
    private static final int FOCUS_NOT_STARTED = 0;
    private static final int FOCUSING = 1;
    private static final int FOCUSING_SNAP_ON_FINISH = 2;
    private static final int FOCUS_SUCCESS = 3;
    private static final int FOCUS_FAIL = 4;
    private int mFocusState = FOCUS_NOT_STARTED;

    private int mStatus = IDLE;
    private static final int IDLE = 1;
    private static final int SNAPSHOT_IN_PROGRESS = 2;

    // Handler相关
    MyHandler mHandler = new MyHandler();
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;

    // 已抛弃列表：1、抛弃CameraSettings.upgradePreferences()方法的实现
    // 2、抛弃掉他自定义的用来设置相机配置的CameraHeadUpDisplay mHeadUpDisplay
    // 3、抛弃掉updateFocusIndicator对焦框的操作
    // 4、zoomChangeListener
    // 5、ErrorCallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // 在开始预览之前，我们要重置相机的曝光补偿
        resetExposureCompensation();
        // 为了节约启动时间，我们将startPreview放到线程里来做，我们在onCreate之前，确保该线程执行完
        Thread startPreviewThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mStartPreviewFail = false;
                    startPreview();
                } catch (CameraHardwareException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    mStartPreviewFail = true;
                }
            }
        });
        startPreviewThread.start();
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 确保预览已开启
        try {
            startPreviewThread.join();
            if (mStartPreviewFail) {
                showCameraErrorAndFinish();
                return;
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void startPreview() throws CameraHardwareException {
        // TODO Auto-generated method stub
        if (mPausing || isFinishing()) {
            return;
        }
        ensureCameraDevice();
        if (mPreviewing) {
            stopPreview();
        }
        setPreviewDisplay(mSurfaceHolder);
        setCameraParameters(UPDATE_PARAM_ALL);

        // setCameraParameters(UPDATE_PARAM_ALL);
        try {
            mCameraDevice.startPreview();
        } catch (Exception e) {
            closeCamera();
            throw new RuntimeException("startPreview failed", e);
        }

        mPreviewing=true;
        mStatus = IDLE;
    }

    private void stopPreview() {
        if (mCameraDevice != null && mPreviewing) {
            mCameraDevice.stopPreview();
        }
        mPreviewing = false;
        // 如果对焦正在进行，那么他需要被取消掉
        clearFocusState();
    }

    /**
     * 确保得到相机设备
     */
    private void ensureCameraDevice() throws CameraHardwareException {
        // TODO Auto-generated method stub
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open();
        }

    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            Log.v(TAG, "setPreviewDisplay----" + SystemClock.currentThreadTimeMillis());
            mCameraDevice.setPreviewDisplay(holder);
        } catch (IOException e) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", e);
        }
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            CameraHolder.instance().release();
            mCameraDevice = null;
            mPreviewing = false;
        }
    }

    private void clearFocusState() {
        // TODO Auto-generated method stub
        mFocusState = FOCUS_NOT_STARTED;
        updateFocusIndicator();
    }

    private void updateFocusIndicator() {
        // TODO Auto-generated method stub

    }

    /**
     * 重置曝光补偿,这里目的是，即使你上次使用将曝光补偿设置为其他的，重新启动应用，也要重置
     */
    private void resetExposureCompensation() {
        String value = mPreferences.getString(CameraSettings.KEY_EXPOSURE,
                CameraSettings.EXPOSURE_DEFAULT_VALUE);
        if (!CameraSettings.EXPOSURE_DEFAULT_VALUE.equals(value)) {
            Editor editor = mPreferences.edit();
            editor.putString(CameraSettings.KEY_EXPOSURE, CameraSettings.EXPOSURE_DEFAULT_VALUE);
            editor.commit();

        }
    }
    
    private void updateCameraParametersPreference() {
        // 设置拍照尺寸
        String pictureSize = mPreferences.getString(CameraSettings.KEY_PICTURE_SIZE, null);
        if (pictureSize == null) {
            CameraSettings.initialCameraPictureSize(this, mParameters);
        } else {
            List<Size> supported = mParameters.getSupportedPictureSizes();
            CameraSettings.setCameraPictureSize(pictureSize, supported, mParameters);
        }
        Log.v(TAG,
                "拍照的尺寸：" + mParameters.getPictureSize().width + "*"
                        + mParameters.getPictureSize().height);

        // 根据图片尺寸设置preview frame的宽高比。
        Size size = mParameters.getPictureSize();
        PreviewFrameLayout frameLayout =
                (PreviewFrameLayout) findViewById(R.id.preview_fl);
        double ratio = (double) size.width / size.height;
        Log.v(TAG, "预览宽高比：" + ratio);
        frameLayout.setAspectRatio(ratio);

        // 设置预览区尺寸
        Size optimalSize = Util.getOptimalPreviewSize(Camera.this,
                mParameters.getSupportedPreviewSizes(), ratio);
        Log.v(TAG, "预览区域的宽高" + optimalSize.width + "*" + optimalSize.height);
        mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
        // 设置场景模式

        // 设置对焦模式
        mFocusMode = mPreferences.getString(CameraSettings.KEY_FOCUS_MODE, null);
        if (mFocusMode == null) {
            mFocusMode = CameraSettings.getOptimalFocuseMode(this, mParameters);
        }
        if (mFocusMode == null) {
            mFocusMode = mParameters.FOCUS_MODE_AUTO;
        }
        mParameters.setFocusMode(mFocusMode);
    }


    private void setCameraParameters(int updateSet) {
        mParameters = mCameraDevice.getParameters();
        if ((updateSet & UPDATE_PARAM_ALL) != 0) {
            updateCameraParametersPreference();
        }
        mCameraDevice.setParameters(mParameters);
        Log.v(TAG, "对焦模式设置成功，对焦模式：" + mCameraDevice.getParameters().getFocusMode());
        Log.v(TAG, "对焦模式设置成功，对焦模式：" + mFocusMode);
    }

    // 如果相机处于idle状态，立即更新他的设置，否则，把需要更新的配置累积到mUpdateSet中稍后更新
    private void setCameraParametersWhenIdle(int additionalUpdateSet) {
        mUpdateSet |= additionalUpdateSet;
        if (mCameraDevice == null) {
            mUpdateSet = 0;
            return;
        } else if (mStatus == IDLE) {
            setCameraParameters(mUpdateSet);
            mUpdateSet = 0;
        } else {
            if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
                mHandler.sendEmptyMessageDelayed(SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        Log.v(TAG, "surfaceChanged---" + SystemClock.currentThreadTimeMillis());
        if (holder.getSurface() == null) {
            Log.e(TAG, "holder.getSurface() is null" + SystemClock.currentThreadTimeMillis());
            return;
        }
        mSurfaceHolder = holder;

        if (mCameraDevice == null) {
            return;
        }
        if (mPausing || isFinishing()) {
            return;
        }
        if (mPreviewing && mSurfaceHolder.isCreating()) {
            setPreviewDisplay(mSurfaceHolder);
        } else {
            // TODO
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        mSurfaceHolder = null;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onShutterButtonClick() {
        // TODO Auto-generated method stub

    }

    private void showCameraErrorAndFinish() {
        Resources res = getResources();
        Util.showFatalErrorAndFinish(Camera.this, res.getString(R.string.camera_error_title),
                res.getString(R.string.cannot_connect_camera));
    }

    @Override
    protected void onPause() {
        mPausing = true;
        stopPreview();
        closeCamera();
        super.onPause();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_CAMERA_PARAMETERS_WHEN_IDLE:
                    setCameraParametersWhenIdle(0);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
