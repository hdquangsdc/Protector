package com.protector;

import android.app.Application;
import android.content.Intent;

import com.protector.services.ProtectorService;

public class ProtectorApplication extends Application {
	@Override
	public void onCreate() {		
		startService(new Intent(this,ProtectorService.class));
		super.onCreate();
	}
}
