package com.stromgens.myviewpagerdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stormgens.myviewpagerdemo.R;
import com.stormgens.thread.MyAsyncTask;
import com.stormgens.util.ImageUtil;

public class ViewPagerItem extends FrameLayout{
    //context
    Context mContext;
    //inflater
    private final LayoutInflater inflater;
    //----views
    String url;
    ImageView imageView;
    ProgressBar progressBar;
    private Bitmap bp;
    //--
    int p;
    
    
    public ViewPagerItem(Context context) {
        super(context);
        mContext=context;
        inflater= (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
    }
    private void initView() {
        View view=inflater.inflate(R.layout.viewpager_itemview, null);
        imageView=(ImageView) view.findViewById(R.id.album_imgeview);
        progressBar=(ProgressBar) view.findViewById(R.id.album_progressbar);
        addView(view);
    }
    
    public void setBitMap(String string,int position){
        p=position;
        new MyAsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                // TODO Auto-generated method stub
                return ImageUtil.getCompressBitMap(params[0],1280,720);
            }
            
            @Override
            protected void onPostExecute(Bitmap result) {
                // TODO Auto-generated method stub
                bp=result;
                imageView.setImageBitmap(bp);
                progressBar.setVisibility(View.GONE);
            }
            
        }.execute(string);
    }
    
    public void recycle(){
        if (bp!=null) {
            imageView.setImageBitmap(null);
            imageView=null;
            bp.recycle();
            bp=null;
        }
        Log.v("xxx", "recycle"+imageView+"~~"+p);
//        if (bp!=null) {
//            bp.recycle();
//            bp=null;
//            imageView=null;
//            mContext=null;
//        }
    }
    
}
