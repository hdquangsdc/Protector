package com.protector.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.protector.AppPreference;
import com.protector.utils.EncryptUtils;

import java.io.File;
import java.util.ArrayList;

public class DBAdapter {

	private static DBAdapter mDBAdapter;
	private Context mContext;

	private DBAdapter(Context context) {
		mContext=context;
	}

	public static DBAdapter getInstance(Context context) {
		if (mDBAdapter == null) {
			mDBAdapter = new DBAdapter(context);
		}
		return mDBAdapter;
	}
	
	public static final String TABLE_NAME = "locked_app";

	public static final String COL_ID = "id";
	public static final String COL_PASSWORD_ID = "password_id";
	public static final String COL_NAME = "app_name";
	public static final String COL_PACKAGE = "app_package";

	public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + " (" + COL_ID + " integer primary key, " + COL_NAME
			+ " text, " + COL_PASSWORD_ID + " integer, " + COL_PACKAGE
			+ " text not null)";
	
	public synchronized ArrayList<String> getAllPass() {
		ArrayList<String> items = new ArrayList<String>();
		String folder = AppPreference.getInstance(mContext).getHideRootPath();
		String DB_PATH = folder + "/" + AppContentProvider.DATABASE_NAME;
		File f = new File(DB_PATH);

		if (f.exists()
				|| Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
			if (f.exists()) {
				if (f.canRead() && f.canWrite()) {

				} else {
					return items;
				}
			}
		} else {
			return items;
		}
		Cursor cursor = null;
		try {
			Uri contentUri = Uri.withAppendedPath(
					AppContentProvider.CONTENT_URI, TABLE_NAME);
			cursor = mContext.getContentResolver().query(contentUri,
					new String[] { COL_PACKAGE }, null, null, COL_NAME);
			while (cursor != null && cursor.moveToNext()) {
				String packageName = EncryptUtils
						.decryptV1(cursor.getString(0));
				try {
					mContext.getPackageManager().getApplicationInfo(
							packageName, 0);
					items.add(packageName);
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return items;
	}
}
