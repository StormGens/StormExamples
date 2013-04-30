
package com.example.stormcamera;

import android.os.Bundle;
import android.view.Menu;

public class CameraActivity extends BaseActivity {
    
    private ComboPreferences mPreferences;
    
    // multiple cameras support
    private int mNumberOfCameras;
    private int mCameraId;
    private int mFrontCameraId;
    private int mBackCameraId;
    
    private boolean mOpenCameraFail = false;
    private boolean mCameraDisabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getPreferredCameraId();
        
        
        
        
        setContentView(R.layout.activity_camera);
        
    }
    
    Thread mCameraOpenThread = new Thread(new Runnable() {
        public void run() {
            try {
                mCameraDevice = Util.openCamera(CameraActivity.this, mCameraId);
            } catch (CameraHardwareException e) {
                mOpenCameraFail = true;
            } catch (CameraDisabledException e) {
                mCameraDisabled = true;
            }
        }
    });
    

    private void getPreferredCameraId() {
        // TODO Auto-generated method stub
        mPreferences=new ComboPreferences(this);
        CameraSettings.upgradeGlobalPreferences(mPreferences);
        mCameraId=CameraSettings.readPreferredCameraId(mPreferences);
        
     // Testing purpose. Launch a specific camera through the intent extras.
        //实验内容，通过intent的 extras 来指定启动的相机
        int intentCameraId = Util.getCameraFacingIntentExtras(this);
        if (intentCameraId != -1) {
            mCameraId = intentCameraId;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera, menu);
        
        return true;
    }

    @Override
    protected void doOnResume() {
        
    }
    

}
