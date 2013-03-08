package com.stromgens.myviewpagerdemo;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stormgens.myviewpagerdemo.R;

public class ImageViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> mItems;
    
    public ImageViewPagerAdapter(Context context,ArrayList<String> items) {
        // TODO Auto-generated constructor stub
        mContext=context;
        mItems=items;
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0==arg1;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewPagerItem item=new ViewPagerItem(mContext);
        int padding = container.getResources().getDimensionPixelSize(R.dimen.padding_image_paper);
        item.imageView.setPadding(padding, padding, padding, padding);
        item.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        container.addView(item);
        Log.v("xxx", "instantiateItem~~~"+position);
        item.setBitMap(mItems.get(position),position);
        return item;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPagerItem item= (ViewPagerItem)object;
        item.recycle();
        container.removeView((View)object);
    }

}
