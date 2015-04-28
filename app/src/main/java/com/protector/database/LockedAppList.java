package com.protector.database;

import java.util.ArrayList;

public class LockedAppList {
	static LockedAppList mStore;
	ArrayList<String> mLockedAppList;

	private LockedAppList() {
		if (mLockedAppList == null)
			mLockedAppList = new ArrayList<String>();
	}

	public static LockedAppList getInstance() {
		if (mStore == null) {
			mStore = new LockedAppList();
		}
		return mStore;
	}

	public ArrayList<String> getLockedApps() {
		return mLockedAppList;
	}

	public boolean containsApp(String app) {
		return mLockedAppList.contains(app);
	}

	public void addApp(String app) {
		mLockedAppList.add(app);
	}

	public void clear() {
		mLockedAppList = new ArrayList<String>();
	}

	public int size() {
		return mLockedAppList.size();
	}
}
