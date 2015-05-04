package com.protector.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import com.protector.asynctasks.MonitorThread;
import com.protector.handlers.ActivityStartingHandler;
import com.protector.receivers.ScreenReceiver;

public class ProtectorService extends Service {

	private static final String APPSOLUT_INTENT_ACTION_BIND_MESSAGE_SERVICE = "appsolut.intent.action.bindMessageService";
	private static final String LOG_TAG = "ProtectorService";
	public static String HOME_PACKAGE;
	BroadcastReceiver mReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "The AIDLMessageService was created.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerBroadcastReceiver();
		getHomePackage();
		handleCommand(intent);
		registerScreenListener();
		return Service.START_STICKY;
	}

	private void registerScreenListener(){
		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		final BroadcastReceiver mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "The AIDLMessageService was destroyed.");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (APPSOLUT_INTENT_ACTION_BIND_MESSAGE_SERVICE.equals(intent
				.getAction())) {
			Log.d(LOG_TAG, "The AIDLMessageService was binded.");
			return new IRemoteProtector(this);
		}
		return null;
	}

	String getABC() {
		return "abc";
	}

	private void getHomePackage() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		HOME_PACKAGE = resolveInfo.activityInfo.packageName;
		Log.i("Service",""+HOME_PACKAGE);
	}

	private void registerBroadcastReceiver() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
	}

	private void handleCommand(Intent intent) {
		startMonitorThread((ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE));
	}

	private void startMonitorThread(final ActivityManager am) {
		if (mThread != null)
			mThread.interrupt();

		mThread = new MonitorThread(this, new ActivityStartingHandler(this));
		mThread.start();
	}

	private static Thread mThread;
}
