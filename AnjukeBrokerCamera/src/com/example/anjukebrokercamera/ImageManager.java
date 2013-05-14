/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * ImageManager.java
 *
 */
package com.example.anjukebrokercamera;

import android.view.OrientationEventListener;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-13
 */
public class ImageManager {

    public static int roundOrientation(int orientationInput) {
        int orientation = orientationInput;
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            orientation = 0;
        }
        orientation = orientation % 360;
        int retVal;
        if (orientation < (0 * 90) + 45) {
            retVal = 0;
        } else if (orientation < (1 * 90) + 45) {
            retVal = 90;
        } else if (orientation < (2 * 90) + 45) {
            retVal = 180;
        } else if (orientation < (3 * 90) + 45) {
            retVal = 270;
        } else {
            retVal = 0;
        }
        return retVal;
    }

}
