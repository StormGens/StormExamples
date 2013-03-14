package com.stormgens.myviewpagerdemo.fragment;

import com.stormgens.myviewpagerdemo.R;
import com.stormgens.thread.MyAsyncTask;
import com.stormgens.util.ImageUtil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageDetailFragment extends Fragment {
    private static final String IMAGE_URL_EXTRA = "extra_image_data";
    
    private ImageView iv;
    private ProgressBar pb;
    private Bitmap bm;
    
    private int padding;
    
    public static ImageDetailFragment newInstance(String url){
        final ImageDetailFragment ret=new ImageDetailFragment();
        Bundle bundle=new Bundle();
        bundle.putString(IMAGE_URL_EXTRA, url);
        ret.setArguments(bundle);
        return ret;
    }
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view=inflater.inflate(R.layout.viewpager_itemview, container, false);
        pb=(ProgressBar) view.findViewById(R.id.album_progressbar);
        iv=(ImageView) view.findViewById(R.id.album_imgeview);
        padding = container.getResources().getDimensionPixelSize(R.dimen.padding_image_paper);
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        iv.setPadding(padding, padding, padding, padding);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        new MyAsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                // TODO Auto-generated method stub
                return ImageUtil.getCompressBitMap(params[0],1280,720);
            }
            
            @Override
            protected void onPostExecute(Bitmap result) {
                // TODO Auto-generated method stub
                bm=result;
                iv.setImageBitmap(bm);
                pb.setVisibility(View.GONE);
            }
            
        }.execute(getArguments().getString(IMAGE_URL_EXTRA));
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
}
