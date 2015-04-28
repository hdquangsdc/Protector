package com.protector.database;

import android.content.Context;
import android.os.Environment;

import com.protector.AppPreference;

import java.io.File;

public abstract class BaseTableAdapter {
	protected Context mContext;

	protected BaseTableAdapter(Context context) {
		mContext = context.getApplicationContext();
	}

	private File openDBFile() {
		String folder = AppPreference.getInstance(mContext)
				.getHideImageRootPath();
		String DB_PATH = folder + "/" + getDatabaseName();
		return new File(DB_PATH);
	}

	protected boolean isDBFileExist() {
		File f = openDBFile();
		return (f.exists() || Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()));
	}

	abstract protected String getDatabaseName();
}
