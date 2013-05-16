/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * StorageUtil.java
 *
 */
package com.example.anjukebrokercamera.storage;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-5-14
 */
public class StorageUtil {
    public static final int NO_STORAGE_ERROR = -1;
    public static final int CANNOT_STAT_ERROR = -2;

    public static int calculatePicturesRemaining() {
        try {
            if (!hasStorage()) {
                return NO_STORAGE_ERROR;
            } else {
                String storageDirectory =
                        Environment.getExternalStorageDirectory().toString();
                StatFs stat = new StatFs(storageDirectory);
                final int PICTURE_BYTES = 1500000;
                float remaining = ((float) stat.getAvailableBlocks()
                        * (float) stat.getBlockSize()) / PICTURE_BYTES;
                return (int) remaining;
            }
        } catch (Exception ex) {
            // if we can't stat the filesystem then we don't know how many
            // pictures are remaining. it might be zero but just leave it
            // blank since we really don't know.
            return CANNOT_STAT_ERROR;
        }
    }

    public static boolean hasStorage() {
        return hasStorage(true);
    }

    public static boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess
                && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName =
                Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }

    @SuppressWarnings("resource")
    public static boolean saveDataToFile(String filePath, byte[] data) {
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(filePath);
            f.write(data);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date);
    }

}
