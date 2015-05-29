package com.protector.handlers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.protector.AppPreference;
import com.protector.activities.UnlockAppActivity;
import com.protector.asynctasks.IActivityStarting;
import com.protector.database.AppTableAdapter;
import com.protector.database.LockedAppList;
import com.protector.services.ProtectorService;

import java.util.ArrayList;
import java.util.Hashtable;

public class ActivityStartingHandler implements IActivityStarting {

    private Context mContext;
    private Handler handler;
    private ActivityManager mAm;
    private ArrayList<String> mLockedAppList;
    private boolean isClear;
    String mLockedApp;
    private Hashtable<String, Runnable> tempAllowedPackages = new Hashtable<String, Runnable>();

    public ActivityStartingHandler(Context context) {
        mContext = context;
        handler = new Handler();
        if (LockedAppList.getInstance().size() > 0) {
            isClear = true;
        }
        mAm = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String packagename = intent
                        .getStringExtra(UnlockAppActivity.EXTRA_PACKAGE_NAME);
                if (AppPreference.getInstance(mContext).getLockedTimeOut() > 0) {
                    if (tempAllowedPackages.containsKey(packagename)) {
                        // Extend the time
                        Log.d("Detector", "Extending timeout for: "
                                + packagename);
                        handler.removeCallbacks(tempAllowedPackages
                                .get(packagename));
                    }
                    Runnable runnable = new RemoveFromTempRunnable(packagename);
                    tempAllowedPackages.put(packagename, runnable);
                    handler.postDelayed(runnable,
                            AppPreference.getInstance(mContext)
                                    .getLockedTimeOut());
                    log();
                }
                Log.d("Package", packagename);
            }
        }, new IntentFilter(UnlockAppActivity.ACTION_APPLICATION_PASSED));

    }

    @Override
    public void onActivityStarting(String packageName, String activityName) {
        synchronized (this) {
            try {
                if (packageName.equals(mContext.getPackageName())) {
                    return;
                }
                if (packageName.equals(ProtectorService.HOME_PACKAGE)) {
                    return;
                }
                mLockedAppList = AppTableAdapter.getInstance(mContext).getAllPass();

                if (mLockedAppList.contains(packageName)) {
                    ArrayList<String> arr = LockedAppList.getInstance()
                            .getLockedApps();
                    if (!arr.contains(packageName)) {
                        if (mLockedApp != null
                                && mLockedApp.equals(packageName)) {
                            mLockedApp = null;
                            return;
                        }
                        isClear = true;
                        mLockedAppList.add(packageName);
                        blockActivity(packageName, activityName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRemoveArrayAppCurrent() {
        if (isClear) {
            mLockedAppList.clear();
            isClear = false;
        }

    }

    private void blockActivity(String packageName, String activityName) {
        // debug: log.i("Detector", "Blocking: " + packageName);
        // Block!
        Log.d("PagekageName--", packageName);
        Intent lockIntent = new Intent(mContext, UnlockAppActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        lockIntent.putExtra(UnlockAppActivity.BLOCKED_ACTIVITY, activityName)
                .putExtra(UnlockAppActivity.BLOCKED_PACKAGE, packageName);

        mContext.startActivity(lockIntent);
    }

    private class RemoveFromTempRunnable implements Runnable {
        private String mPackageName;

        public RemoveFromTempRunnable(String pname) {
            mPackageName = pname;
        }

        @Override
        public void run() {
            Log.d("Detector", "Lock timeout Expires: " + mPackageName);
            tempAllowedPackages.remove(mPackageName);
        }
    }

    private void log() {
        String output = "temp allowed: ";
        for (String p : tempAllowedPackages.keySet()) {
            output += p + ", ";
        }
        Log.d("Detector", output);
    }
}
