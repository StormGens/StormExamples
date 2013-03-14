package com.stormgens.myviewpagerdemo;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.stormgens.myviewpagerdemo.R;
import com.stormgens.myviewpagerdemo.activity.ImageDetailViewPagerActivity;
import com.stormgens.util.FileUtils;

public class MainActivity extends Activity {
    ViewPager mViewPager;
    ImageViewPagerAdapter mAdapter;
    ArrayList<String> items=new ArrayList<String>();
    
    String cameraDir=Environment.getExternalStorageDirectory()+"/DCIM/Camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matchViews();
        initViewPagers();
    }

    private void matchViews() {
        // TODO Auto-generated method stub
        mViewPager=(ViewPager) findViewById(R.id.image_view_pager);
        mViewPager.setOffscreenPageLimit(2);
    }
    
    private void initDatas() {
        // TODO Auto-generated method stub
        items=FileUtils.listAllfile(new File(cameraDir));
    }
    
    private void initViewPagers() {
        initDatas();
        mAdapter=new ImageViewPagerAdapter(MainActivity.this, items);
        mViewPager.setAdapter(mAdapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case R.id.action_settings:
            Intent intent=new Intent(MainActivity.this, ImageDetailViewPagerActivity.class);
            startActivity(intent);
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
