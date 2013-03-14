package com.stormgens.myviewpagerdemo.adapter;

import java.util.ArrayList;

import com.stormgens.myviewpagerdemo.fragment.ImageDetailFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ImageDetailAdapter extends FragmentStatePagerAdapter {
    ArrayList<String> mUrls;

    public ImageDetailAdapter(FragmentManager fm,ArrayList<String> urls) {
        super(fm);
        this.mUrls=urls;
    }

    @Override
    public Fragment getItem(int arg0) {
        Fragment fragment;
        fragment=ImageDetailFragment.newInstance(mUrls.get(arg0));
        return fragment;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

}
