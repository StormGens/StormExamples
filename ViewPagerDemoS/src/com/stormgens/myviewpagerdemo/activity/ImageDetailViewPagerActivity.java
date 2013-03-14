package com.stormgens.myviewpagerdemo.activity;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.stormgens.myviewpagerdemo.R;
import com.stormgens.myviewpagerdemo.adapter.ImageDetailAdapter;
import com.stormgens.util.FileUtils;

public class ImageDetailViewPagerActivity extends FragmentActivity {
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
    }

}
