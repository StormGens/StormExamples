package com.stormgens.myviewpagerdemo.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;

import com.stormgens.myviewpagerdemo.R;
import com.stormgens.myviewpagerdemo.adapter.ImageDetailAdapter;
import com.stormgens.util.FileUtils;
import com.stormgens.util.SDKVerUtil;

public class ImageDetailViewPagerActivity extends FragmentActivity implements OnClickListener{
    ViewPager mPager;
    ImageDetailAdapter mAdapter;
    
    String cameraDir=Environment.getExternalStorageDirectory()+"/DCIM/Camera";
    ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail_view_pager);
        items=FileUtils.listAllfile(new File(cameraDir));
        mPager=(ViewPager) findViewById(R.id.image_detail_pager);
        mPager.setOffscreenPageLimit(2);
        mAdapter=new ImageDetailAdapter(getSupportFragmentManager(), items);
        mPager.setAdapter(mAdapter);
        if (SDKVerUtil.hasHoneycomb()) {
            final ActionBar actionBar=getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            mPager.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    // TODO Auto-generated method stub
                    if ((visibility&View.SYSTEM_UI_FLAG_LOW_PROFILE)!=0) {
                        actionBar.hide();
                    }else{
                        actionBar.show();
                    }
                }
            });
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        final int vis=mPager.getSystemUiVisibility();
        if ((vis&View.SYSTEM_UI_FLAG_LOW_PROFILE)!=0) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else{
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

}
