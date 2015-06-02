package com.protector.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * @author Ho Duy Quang
 */
public class PhoneMemoryUtils {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        if (AndroidVersion.isJellyBeanMR2orHigher()) {
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            return stat.getBlockSize() * stat.getAvailableBlocks();
        }
    }
}
