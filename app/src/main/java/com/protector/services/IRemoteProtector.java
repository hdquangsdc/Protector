package com.protector.services;


import android.os.RemoteException;

import com.protector.IRemoteProtectorService;

public class IRemoteProtector extends IRemoteProtectorService.Stub {
	private ProtectorService mService;

	public IRemoteProtector(ProtectorService service) {
		this.mService = service;
	}

	@Override
	public String getMessage() throws RemoteException {
		return mService.getABC();
	}

}
