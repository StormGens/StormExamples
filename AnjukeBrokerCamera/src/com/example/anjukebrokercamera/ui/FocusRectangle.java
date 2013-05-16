/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * FocusRectangle.java
 *
 */
package com.example.anjukebrokercamera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.anjukebrokercamera.R;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-15
 */
public class FocusRectangle extends View {

    @SuppressWarnings("unused")
    private static final String TAG = "FocusRectangle";

    public FocusRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setDrawable(int resid) {
        setBackgroundDrawable(getResources().getDrawable(resid));
    }

    public void showStart() {
        setDrawable(R.drawable.focus_focusing);
    }

    public void showSuccess() {
        setDrawable(R.drawable.focus_focused);
    }

    public void showFail() {
        setDrawable(R.drawable.focus_focus_failed);
    }

    public void clear() {
        setBackgroundDrawable(null);
    }
}
