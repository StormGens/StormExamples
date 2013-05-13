/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * A @{code ImageView} which can rotate it's content.
 */
public class RotateImageView extends ImageView {

    @SuppressWarnings("unused")
    private static final String TAG = "RotateImageView";

    private static final int ANIMATION_SPEED = 180; // 180 deg/sec 180度每秒

    private int mCurrentDegree = 0; // [0, 359]，这里虽然是0，但在ImageView实例初始化后会被赋值的。
    private int mStartDegree = 0; // 起始角度
    private int mTargetDegree = 0; // 目标角度，也就是每次旋转动画做完后的角度

    private boolean mClockwise = false;// 是否是顺时针方向

    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 一定要在UI线程调用
     * 
     * @param degree
     */
    public void setDegree(int degree) {
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree)
            return;

        mTargetDegree = degree;
        mStartDegree = mCurrentDegree;
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        // 定义最终算出的需要旋转的最近角度diff。
        int diff = mTargetDegree - mCurrentDegree;
        diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

        // Make it in range [-179, 180]. That's the shorted distance between the
        // two angles
        diff = diff > 180 ? diff - 360 : diff;

        mClockwise = diff >= 0;
        mAnimationEndTime = mAnimationStartTime // 算出结束时间，在这个结束时间到来前，一直保持ANIMATION_SPEED速度旋转
                + Math.abs(diff) * 1000 / ANIMATION_SPEED; // 更易懂的写法是
                                                           // +(Math.abs(diff)/ANIMATION_SPEED)*1000

        invalidate();// invalidate单词意思：使无效，使无价值,在这里的意思相当于请求重绘该View
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable == null)
            return;

        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0)
            return; // nothing to draw

        // 以上都是nothing to draw的情况，下面重头戏来了
        // 重头戏分两步：
        // 1、算出什么时候需要绘制绘制再绘制
        // 2、怎么绘制

        // 【第一步开始】计算！
        if (mCurrentDegree != mTargetDegree) {// 若还没有旋转到指定角度
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {// 若还没有到动画结束时间
                int deltaTime = (int) (time - mAnimationStartTime);// 已经旋转了多长时间了
                int degree = mStartDegree + ANIMATION_SPEED
                        * (mClockwise ? deltaTime : -deltaTime) / 1000;// 现在的角度
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;// 确保在[0,
                                                                         // 359]之间
                mCurrentDegree = degree;
                invalidate();// 请求重新绘制，这里有个小小的递归循环，有木有！
            } else {
                mCurrentDegree = mTargetDegree;// 旋转动画结束时候，赋值
            }
        }

        // 【第二步开始】 绘制！
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right; // drawable的宽
        int height = getHeight() - top - bottom; // drawable的高

        int saveCount = canvas.getSaveCount();// 取得开始操作之前的画布的状态
        canvas.translate(left + width / 2, top + height / 2);// 将基准点（原点）移到中心
        canvas.rotate(-mCurrentDegree); // 将画布旋转到-mCurrentDegree度
        canvas.translate(-w / 2, -h / 2);// 若布局中用的是wrap-content,那么w/2就是画布宽的一半，h/2也正是画布高的一半，所以此处是将基准点移到(0，0)；
        drawable.draw(canvas); // 画图
        canvas.restoreToCount(saveCount); // 画布状态还原以便接下来的绘制
        // 其实上面这写对画布的操作，啰啰嗦嗦一大堆，其实就是以imageView的中心点旋转图片到-mCurrentDegree这个角度
    }
}
