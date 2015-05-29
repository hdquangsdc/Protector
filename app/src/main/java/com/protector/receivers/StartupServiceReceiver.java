package com.protector.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.protector.services.ProtectorService;

public class StartupServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.d("Detector", "Auto Start" +
		// AppLockerPreference.getInstance(context).isAutoStart());
		context.startService(new Intent(context, ProtectorService.class));
	}
}
