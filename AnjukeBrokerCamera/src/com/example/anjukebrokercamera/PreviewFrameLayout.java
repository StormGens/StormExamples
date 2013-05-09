/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * PreViewFrameLayout.java
 *
 */

package com.example.anjukebrokercamera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author liqiangzhang (liqiangzhang@anjuke.com)
 * @date 2013-5-1
 */
public class PreviewFrameLayout extends ViewGroup {
    private final String TAG = "PreviewFrameLayout";

    public PreviewFrameLayout(Context context) {
        super(context);
    }

    public PreviewFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private double mAspectRatio = 4.0 / 3.0;// 预期的比例
    private FrameLayout mFrame;
    onSizeChangedListener mOnSizeChangedListener;

    public void setOnSizeChangedListener(onSizeChangedListener listener) {
        // TODO Auto-generated method stub
        mOnSizeChangedListener = listener;
    }

    public interface onSizeChangedListener {
        public void onSizeChanged();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFrame = (FrameLayout) findViewById(R.id.frame);
    }

    public void setAspectRatio(double ratio) {
        if (ratio <= 0.0) {
            throw new IllegalArgumentException("ratio can not be less than 0.0");
        }
        if (mAspectRatio != ratio) {
            mAspectRatio = ratio;
            requestLayout();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        FrameLayout f = mFrame;
        int horizontalPadding = f.getPaddingLeft() + f.getPaddingRight();
        int verticalPadding = f.getPaddingTop() + f.getPaddingBottom();
        int previewWidth = frameWidth - horizontalPadding;
        int previewHeight = frameHeight - verticalPadding;
        if (previewWidth > previewHeight * mAspectRatio) {// 如果刚开始的宽度比定义宽高比*高度的话，需要重新定义宽度（太宽了）
            previewWidth = (int) (previewHeight * mAspectRatio + .5);
        } else {// 否则，重新定义高度（太高了）
            previewHeight = (int) (previewWidth / mAspectRatio + .5);
        }
        // 重新定义更改了预览区大小后的FrameLayout的大小。
        frameWidth = previewWidth + horizontalPadding;
        frameHeight = previewHeight + verticalPadding;
        Log.v(TAG, "PreviewFrameLayout的尺寸：" + frameWidth + "*" + frameHeight);
        mFrame.measure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
        int hSpace = ((r - l) - frameWidth) / 2; // 算出水平方宽度改变的大小的一半
        int vSpace = ((b - t) - frameHeight) / 2;// 算出竖直方面宽度改变的多少的一半

        mFrame.layout(l + hSpace, t + vSpace, r - hSpace, b - vSpace);// 这样保持中心不变

        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged();
        }
    }
}
