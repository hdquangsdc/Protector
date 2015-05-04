package com.protector.receivers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.protector.activities.PasswordActivity;
import com.protector.activities.UnlockAppActivity;
import com.protector.database.AppTableAdapter;

import java.util.ArrayList;
import java.util.List;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do nothing
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            String strRunningPackage = getRunningPackage(context);
            if (strRunningPackage != null) {
                if (context.getPackageName().equals(strRunningPackage)) {
                    showLockScreen(context);
                }
            }
            new AsynBlock(context).execute();

        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            // do nothing
        }
    }

    private void showLockScreen(Context context) {
        Intent i = new Intent(context, PasswordActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    private String getRunningPackage(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> lstInfo = mActivityManager.getRunningTasks(1);
        if (lstInfo.size() < 1)
            return null;
        RunningTaskInfo info = lstInfo.get(0);
        return info.topActivity.getPackageName();
    }

    public class AsynBlock extends AsyncTask<Void, Void, String> {
        private Context mContext;

        public AsynBlock(Context context1) {
            mContext = context1;
        }

        @Override
        protected String doInBackground(Void... params) {
            ActivityManager am = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity
                    .getPackageName();
            PackageManager pm = mContext.getPackageManager();
            PackageInfo foregroundAppPackageInfo = null;

            String foregroundTaskActivityName = foregroundTaskInfo.topActivity
                    .getShortClassName().toString();
            try {
                foregroundAppPackageInfo = pm.getPackageInfo(
                        foregroundTaskPackageName, 0);
                String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
                        .loadLabel(pm).toString();
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ArrayList<String> lockedApp = AppTableAdapter.getInstance(
                    mContext).getAllPass();
            for (int i = 0; i < lockedApp.size(); i++) {
                if (lockedApp.get(i).equals(foregroundTaskPackageName)) {
                    return foregroundAppPackageInfo.packageName + "#@#"
                            + foregroundTaskActivityName;
                } else {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                String[] str = result.split("#@#");
                if (str.length == 2) {
                    blockActivity(mContext, str[0], str[1]);
                }
            }
        }

    }

    private void blockActivity(Context context, String packageName,
                               String activityName) {
		Intent lockIntent = new Intent(context, UnlockAppActivity.class);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		lockIntent.putExtra(UnlockAppActivity.BLOCKED_ACTIVITY, activityName);
        lockIntent.putExtra(UnlockAppActivity.BLOCKED_PACKAGE, packageName);
        context.startActivity(lockIntent);
    }
}
