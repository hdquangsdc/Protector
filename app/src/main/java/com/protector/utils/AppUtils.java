package com.protector.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.protector.database.AppTableAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ho on 4/21/2015.
 */
public class AppUtils {
    public static List<String> getApp(Context context) {
        List<String> apps = new ArrayList<String>();
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> pkgAppsList = context.getPackageManager()
                    .queryIntentActivities(mainIntent, 0);
            List<String> lockedApps = AppTableAdapter.getInstance(context)
                    .getAll();
            for (ResolveInfo appInfo : pkgAppsList) {
                boolean isContain = appInfo.activityInfo.packageName
                        .equals(context.getPackageName());
                if ((!lockedApps.contains(appInfo.activityInfo.packageName))
                        && !isContain) {
                    if (apps.contains(appInfo.activityInfo.packageName))
                        continue;
                    apps.add(appInfo.activityInfo.packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apps;
    }
}
