/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * ImageManager.java
 *
 */
package com.example.anjukebrokercamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.OrientationEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-13
 */
public class ImageManager {
    private static final String TAG = "ImageManager";
    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";

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

    public static Uri addImage(ContentResolver cr, String title, long dateTaken, String directory,
            String filename,
            Bitmap source, byte[] jpegData, int[] degree) {
        // We should store image data earlier than insert it to ContentProvider,
        // otherwise we may not be able to generate thumbnail in time.
        OutputStream outputStream = null;
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            if (source != null) {
                source.compress(CompressFormat.JPEG, 75, outputStream);
                degree[0] = 0;
            } else {
                outputStream.write(jpegData);
                degree[0] = getExifOrientation(filePath);
            }
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Util.closeSilently(outputStream);
        }

        // Read back the compressed file size.
        long size = new File(directory, filename).length();

        ContentValues values = new ContentValues(9);
        values.put(Images.Media.TITLE, title);

        // That filename is what will be handed to Gmail when a user shares a
        // photo. Gmail gets the name of the picture attachment from the
        // "DISPLAY_NAME" field.
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, degree[0]);
        values.put(Images.Media.DATA, filePath);
        values.put(Images.Media.SIZE, size);

        return cr.insert(STORAGE_URI, values);
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.e(TAG, "cannot read exif", ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }
}
