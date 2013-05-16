/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * Util.java
 *
 */
package com.example.anjukebrokercamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;

import java.io.Closeable;
import java.util.List;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-8
 */
public class Util {
    private final static String TAG = "Util";

    public static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
    public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void showFatalErrorAndFinish(
            final Activity activity, String title, String message) {
        DialogInterface.OnClickListener buttonListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                };
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.details_ok, buttonListener)
                .show();
    }

    public static Size getOptimalPreviewSize(Activity currentActivity,
            List<Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurfaceView, use screen height
            targetHeight = display.getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            Log.v(TAG, "支持的预览尺寸" + size.width + "*" + size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
}
