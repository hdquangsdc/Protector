package com.protector;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;

import com.protector.utils.EncryptUtils;

import java.io.File;

public class AppPreference {
	private String SESSION = "com.protector.preferences";
	private static AppPreference mSessionManager;
	private SharedPreferences mSharedPreferences;
	private static Context mContext;
	private String KEY_PASSWORD = "password";
	private String KEY_LOCKED_TIMEOUT = "locked_time_out";
	String key = "123456";

	private AppPreference(Context context) {
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(SESSION,
					Context.MODE_PRIVATE);
		}
	}

	public static AppPreference getInstance(Context context) {
		if (mSessionManager == null) {
			mContext = context;
			mSessionManager = new AppPreference(context.getApplicationContext());
		}
		return mSessionManager;
	}

	public String getHideRootPath() {
		String path = mContext.getFilesDir().getParentFile().getPath()
				+ "/databases/SystemData/Data";
		String SDCardPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/SystemData/Data";

		String folderPath = this.mSharedPreferences.getString(
				"file_hide_root_path", null);
		if (folderPath == null) {
			File folder = new File(SDCardPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File f = new File(SDCardPath);
			if (f.exists()) {
				folderPath = SDCardPath;
				this.mSharedPreferences.edit()
						.putString("file_hide_root_path", folderPath).commit();
			} else {
				File fOther = new File(path);
				if (!fOther.exists()) {
					fOther.mkdirs();
				}
				folderPath = path;
				this.mSharedPreferences.edit()
						.putString("file_hide_root_path", folderPath).commit();

			}
		} else {
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		return folderPath;
	}

	public String getPassword() {
		return mSharedPreferences.getString(KEY_PASSWORD, null);
	}

	public void setPassword(String password) {
		String strMd5Password = EncryptUtils.getMD5(password);
		mSharedPreferences.edit().putString(KEY_PASSWORD, strMd5Password)
				.commit();
	}

	public void setLockedTimeOut(long timeInMilisecond) {
		mSharedPreferences.edit().putLong(KEY_LOCKED_TIMEOUT, timeInMilisecond)
				.commit();
	}

	public long getLockedTimeOut() {
		return mSharedPreferences.getLong(KEY_LOCKED_TIMEOUT, 0);
	}

	public String getHideImageRootPath() {
		File folder = new File(getHideRootPath(), Base64.encodeToString(
				key.getBytes(), Base64.NO_WRAP)
				+ "/.images");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder.getAbsolutePath();
	}

	public String getHideVideoRootPath() {
		File folder = new File(getHideRootPath(), Base64.encodeToString(
				key.getBytes(), Base64.NO_WRAP)
				+ "/.videos");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder.getAbsolutePath();
	}

	public String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
}
