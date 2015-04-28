package com.protector.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.protector.IRemoteProtectorService;

public class ProtectorServiceConnection implements ServiceConnection {

	private static final String INTENT_ACTION_BIND_SERVICE = "protector.intent.action.BindService";
	IRemoteProtectorService mRemoteService;
	Context mContext;

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mRemoteService = IRemoteProtector.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.mRemoteService = null;
	}

	public void disconnect() {
		if (mRemoteService != null) {
			mRemoteService = null;
			mContext.unbindService(this);
		}
	}
	
	public void connect() {
		if (mRemoteService == null) {
			Intent intentBindService = new Intent(INTENT_ACTION_BIND_SERVICE);
			intentBindService.setClassName(ProtectorService.class.getPackage()
					.getName(), ProtectorService.class.getCanonicalName());
			mContext.bindService(intentBindService, this,
					Context.BIND_AUTO_CREATE);
		}
	}		
}
