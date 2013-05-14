
package com.example.anjukebrokercamera;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.anjukebrokercamera.storage.StorageUtil;
import com.example.anjukebrokercamera.ui.RotateImageView;

import java.io.IOException;
import java.util.List;

public class Camera extends NoSearchActivity implements SurfaceHolder.Callback, OnClickListener,
        ShutterButton.OnShutterButtonListener {
    private final String TAG = "CameraActivity";
    private static final int SCREEN_DELAY = 2 * 60 * 1000;
    private static final int FOCUS_BEEP_VOLUME = 100;// 拍照声音大小
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;// 当用户手机屏幕亮度是自动时候，我们使用该值，不实用1.0是因为1.0太亮啦
    // View
    private ImageView mLastPictureButton;
    private ShutterButton mShutterButton;
    // 变量
    private android.hardware.Camera mCameraDevice; // 相机的硬件设备
    private SurfaceView mSurfaceView; // 用作预览区的mSurfaceView
    private SurfaceHolder mSurfaceHolder = null; // 相机预览区域的holder
    private ContentResolver mContentResolver;
    private ToneGenerator mFocusToneGenerator;
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
    private boolean mFirstTimeInitialized;// 第一次初始化是否完成
    private boolean mPreviewing;// 是不是正在预览

    // receiveer
    private boolean mDidRegister = false;

    // activity的状态标记
    private boolean mPausing;
    // 方向监听相关
    private OrientationEventListener mOrientationListener;
    private int mLastOrientation = 0; // No rotation (landscape) by default.

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
    private static final int FIRST_TIME_INIT = 2;
    private static final int RESTART_PREVIEW = 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;

    // 拍照过程相关
    private int mPicturesRemaining;

    // 已抛弃列表：1、抛弃CameraSettings.upgradePreferences()方法的实现
    // 2、抛弃掉他自定义的用来设置相机配置的CameraHeadUpDisplay mHeadUpDisplay
    // 3、抛弃掉updateFocusIndicator对焦框的操作
    // 4、zoomChangeListener
    // 5、ErrorCallback
    // 6、一些暂时用不上的相机的配置
    // 7、定位的功能

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
                    e.printStackTrace();
                    mStartPreviewFail = true;
                }
            }
        });
        startPreviewThread.start();
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 开始初始化相机操控界面
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup rootView = (ViewGroup) findViewById(R.id.camera);
        inflater.inflate(R.layout.camera_control, rootView);
        // 确保预览已开启
        try {
            startPreviewThread.join();
            if (mStartPreviewFail) {
                showCameraErrorAndFinish();
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPausing = false;

        // 如果还没有开启预览，那么就开启预览。
        if (!mPreviewing && !mStartPreviewFail) {
            resetExposureCompensation();
            try {
                startPreview();
            } catch (CameraHardwareException e) {
                showCameraErrorAndFinish();
                e.printStackTrace();
            }
        }
        if (mSurfaceHolder != null) {
            if (mFirstTimeInitialized) {
                mHandler.sendEmptyMessage(FIRST_TIME_INIT);
            } else {
                initializeSecondTime();
            }
        }
        keepScreenOnAwhile();
    }

    private void startPreview() throws CameraHardwareException {
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

        mPreviewing = true;
        mStatus = IDLE;
    }

    private void restartPreview() {
        try {
            startPreview();
        } catch (CameraHardwareException e) {
            showCameraErrorAndFinish();
            e.printStackTrace();
        }

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

        // 设置闪光灯模式
        String flashMode = mPreferences.getString(CameraSettings.KEY_FLASH_MODE,
                getString(R.string.pref_camera_flashmode_default));
        List<String> supported = mParameters.getSupportedFlashModes();
        if (Util.isSupported(flashMode, supported)) {
            mParameters.setFlashMode(flashMode);
        } else {// 其实这段代码没太大用
            flashMode = mParameters.getFlashMode();
            if (flashMode == null) {// 说明不支持闪光灯
                mParameters.setFlashMode(getString(R.string.pref_camera_flashmode_no_flash));
            }
        }

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

    /**
     * 总共做了一下事情： 1、初始化方向监听 2、初始化定位服务 3、初始化预览图片的最后一张缩略图 4、初始化拍照按钮
     */
    private void initializeFirstTime() {
        if (mFirstTimeInitialized) {
            return;
        }

        // 因为方向监听会有一个时间延迟，所以我们应该尽快的开启方向监听
        mOrientationListener = new OrientationEventListener(Camera.this) {

            @Override
            public void onOrientationChanged(int orientation) {
                // 我们记录activity上次(last)的方向，所以当用户第一次启动并将方向设置为横屏时候
                // 我们把这时候的方向记录下来。
                if (orientation != ORIENTATION_UNKNOWN) {
                    orientation += 90;
                }
                orientation = ImageManager.roundOrientation(orientation);
                if (orientation != mLastOrientation) {
                    mLastOrientation = orientation;
                    setOrientationIndicator();
                }
            }
        };
        mOrientationListener.enable();
        // keepMediaProviderInstance();
        checkStorage();
        // TODO 初始化定位服务--先砍掉
        // 初始化缩略图
        mContentResolver = getContentResolver();
        mLastPictureButton = (ImageView) findViewById(R.id.review_thumbnail);
        mLastPictureButton.setOnClickListener(this);
        updateThumbnailButton();
        // TODO 图片显示先不做
        // 初始化拍照按钮
        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnShutterButtonListener(this);

        initializeFocusTone();
        initializeScreenBrightness();
        installIntentFilter();

        mFirstTimeInitialized = true;

    }

    /**
     * 如果activity被pause();再被reSume()时候，该方法要被调用
     */
    private void initializeSecondTime() {
        mOrientationListener.enable();
        installIntentFilter();
        initializeFocusTone();
        checkStorage();
        // 更新缩略图按钮
        updateThumbnailButton();
        // 检查sd卡
        checkStorage();

    }

    private void initializeFocusTone() {
        try {
            mFocusToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, FOCUS_BEEP_VOLUME);
        } catch (Exception e) {
            Log.w(TAG, "Exception caught while creating tone generator: ", e);
            mFocusToneGenerator = null;
        }
    }

    /**
     * 如果用户的系统设置屏幕是自动亮度的话，就把他改为一个固定的值
     */
    private void initializeScreenBrightness() {
        Window win = getWindow();
        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams paras = win.getAttributes();
            paras.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(paras);
        }

    }

    private void installIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
        mDidRegister = true;
    }

    private void setOrientationIndicator() {
        ((RotateImageView) findViewById(R.id.review_thumbnail)).setDegree(mLastOrientation);
        ((RotateImageView) findViewById(R.id.camera_switch_icon)).setDegree(mLastOrientation);
        ((RotateImageView) findViewById(R.id.video_switch_icon)).setDegree(mLastOrientation);

    }

    private void updateThumbnailButton() {
        // Update last image if URI is invalid and the storage is ready.
        // if (!mThumbController.isUriValid() && mPicturesRemaining >= 0) {
        // updateLastImage();
        // }
        // mThumbController.updateDisplayIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    private void checkStorage() {
        mPicturesRemaining = StorageUtil.calculatePicturesRemaining();
        updateStorageHint();
    }

    private OnScreenHint mStorageHint;

    private void updateStorageHint() {
        String noStorageText = null;
        if (mPicturesRemaining == StorageUtil.NO_STORAGE_ERROR) {
            String stage = Environment.getExternalStorageState();
            if (stage.equals(Environment.MEDIA_CHECKING)) {
                noStorageText = getString(R.string.preparing_sd);
            } else {
                noStorageText = getString(R.string.no_storage);
            }
        } else if (mPicturesRemaining < 1) {
            noStorageText = getString(R.string.not_enough_space);
        }
        if (noStorageText != null) {
            if (mStorageHint == null) {
                mStorageHint = OnScreenHint.makeText(Camera.this, noStorageText);
            } else {
                mStorageHint.setText(noStorageText);
            }
            mStorageHint.show();
        } else if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
            restartPreview();
        }
        if (mFirstTimeInitialized) {
            mHandler.sendEmptyMessage(FIRST_TIME_INIT);
        } else {
            initializeSecondTime();
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
        switch (v.getId()) {
            case R.id.review_thumbnail:
                Log.v(TAG, "我要飞得更高。");
                break;

            default:
                break;
        }

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
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        mPausing = true;
        stopPreview();
        closeCamera();
        resetScreenOn();

        if (mFirstTimeInitialized) {
            mOrientationListener.disable();
        }

        if (mFocusToneGenerator != null) {
            mFocusToneGenerator.release();
            mFocusToneGenerator = null;
        }
        if (mDidRegister) {
            unregisterReceiver(mReceiver);
            mDidRegister = false;
        }

        // remove message in message queue
        mHandler.removeMessages(FIRST_TIME_INIT);
        mHandler.removeMessages(RESTART_PREVIEW);
        super.onPause();
    }

    /**
     * 去掉保持屏幕的点亮并去掉可能没执行的动作
     */
    private void resetScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                checkStorage();
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                checkStorage();
                updateThumbnailButton();
            }
        }
    };

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_CAMERA_PARAMETERS_WHEN_IDLE:
                    setCameraParametersWhenIdle(0);
                    break;
                case CLEAR_SCREEN_DELAY:
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                case FIRST_TIME_INIT:
                    initializeFirstTime();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
