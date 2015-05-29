package com.protector.asynctasks;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.protector.services.ProtectorService;

import java.io.BufferedReader;

public class MonitorThread extends Thread {

	IActivityStarting mListener;
	ProtectorService mService;
	private String strPackageName;

	public MonitorThread(ProtectorService service, IActivityStarting listener) {
		mListener = listener;
		mService = service;
	}

	BufferedReader br;

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				Thread.sleep(300);
				ActivityManager am = (ActivityManager) mService
						.getBaseContext().getSystemService(
								Context.ACTIVITY_SERVICE);
				RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(
						0);
				String foregroundTaskPackageName = foregroundTaskInfo.topActivity
						.getPackageName();
				PackageManager pm = mService.getBaseContext()
						.getPackageManager();
				PackageInfo foregroundAppPackageInfo = null;

				String foregroundTaskActivityName = foregroundTaskInfo.topActivity
						.getShortClassName().toString();
				try {
					foregroundAppPackageInfo = pm.getPackageInfo(
							foregroundTaskPackageName, 0);
					String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
							.loadLabel(pm).toString();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				if (ProtectorService.HOME_PACKAGE
						.equals(foregroundAppPackageInfo.packageName)) {
					strPackageName = foregroundAppPackageInfo.packageName;
					if (mListener != null) {
						mListener.onRemoveArrayAppCurrent();
					}
					continue;
				}

				if (strPackageName!=null&&!strPackageName
						.equals(foregroundAppPackageInfo.packageName)
						&& !mService.getPackageName().equals(
								foregroundAppPackageInfo.packageName)) {
					strPackageName = foregroundAppPackageInfo.packageName;
					Log.d("Log2 : ", strPackageName);
					if (mListener != null) {
						mListener.onActivityStarting(
								foregroundAppPackageInfo.packageName,
								foregroundTaskActivityName);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
