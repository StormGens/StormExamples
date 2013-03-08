package com.stromgens.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtil {
    
    public static Bitmap getCompressBitMap(String filePath,int maxWidth,int maxHeight) {
        // TODO Auto-generated method stub
        Bitmap ret = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        if(opts.outWidth == -1){//error
            return null;
        }
        int width = opts.outWidth;//图片宽
        int height = opts.outHeight;//图片高
        if(maxWidth >= width && maxHeight >= height){//略缩图比原图还大？！！
            ret = BitmapFactory.decodeFile(filePath);
        }else{
            //计算到maxWidth的压缩比
            float inSampleSizeWidthFloat = (float)width / (float)maxWidth;
            int inSampleSizeWidth = Math.round(inSampleSizeWidthFloat);
            //计算到maxHeight的压缩比
            float inSampleSizeHeightFloat = (float)height / (float)maxHeight;
            int inSampleSizeHeight = Math.round(inSampleSizeHeightFloat);

            int inSampleSize = Math.max(inSampleSizeWidth, inSampleSizeHeight);

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = inSampleSize;
            ret = BitmapFactory.decodeFile(filePath, opts);
        }
        return ret;
    }
}
