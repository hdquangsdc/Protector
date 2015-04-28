package com.protector.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.protector.AppPreference;
import com.protector.objects.MediaStorageItem;

import java.io.File;
import java.util.ArrayList;

public class PhotoTableAdapter extends BaseTableAdapter {
	public static final String TABLE_NAME = "photos_table";

	public static final String COL_ID = "id";
	public static final String COL_PW_ID = "pw_id";
	public static final String COL_ORG_PATH = "file_org_path";
	public static final String COL_NAME = "col_file_name";
	public static final String COL_NEW_PATH = "file_new_path";
	public static final String COL_VD_TIME = "video_time";
	public static final String COL_EXTENTION = "extention";
	public static final String COL_DATE = "file_date";
	public static final String COL_SOLUTION = "file_solution";

	public static final String COL_THUMBNAIL = "thumbnail";

	public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + " (" + COL_ID + " integer primary key, " + COL_PW_ID
			+ " text not null, " + COL_ORG_PATH + " text not null, " + COL_NAME
			+ " text not null, " + COL_NEW_PATH + " text not null, " + COL_DATE
			+ " text, " + COL_SOLUTION + " text, " + COL_VD_TIME
			+ " text not null, " + COL_EXTENTION + " text, " + COL_THUMBNAIL
			+ " blob)";

	private static PhotoTableAdapter instance;

	protected PhotoTableAdapter(Context applicationContext) {
		super(applicationContext);
	}

	public static PhotoTableAdapter getInstance(Context context) {
		if (instance == null) {
			instance = new PhotoTableAdapter(context.getApplicationContext());
		}
		return instance;
	}

	@Override
	protected String getDatabaseName() {
		return PhotoContentProvider.DATABASE_NAME;
	}

	public synchronized long add(String orgPath, String newPath, String name,
			long videoTime, String extention, byte[] thumbnail, long date,
			String solution) {
		if (!isDBFileExist()) {
			return -1;
		}
		ContentValues values = new ContentValues();
		if (PasswordTableAdapter.PASSWORD_CURRENT_ID < 1) {
			values.put(COL_PW_ID, 1);
		} else {
			values.put(COL_PW_ID, PasswordTableAdapter.PASSWORD_CURRENT_ID);
		}
		try {
			values.put(COL_ORG_PATH, orgPath);
			values.put(COL_NEW_PATH, newPath);
			values.put(COL_NAME, name);
			values.put(COL_VD_TIME, videoTime);
			values.put(COL_EXTENTION, extention);
			values.put(COL_THUMBNAIL, thumbnail);
			values.put(COL_DATE, date);
			values.put(COL_SOLUTION, solution);
			Uri contentUri = Uri.withAppendedPath(
					PhotoContentProvider.CONTENT_URI, TABLE_NAME);
			Uri result = mContext.getContentResolver().insert(contentUri,
					values);
			if (result == null) {
				return 0;
			}
			return Long.parseLong(result.getLastPathSegment());
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public synchronized int remove(long id) {
		if (!isDBFileExist()) {
			return -1;
		}
		Uri contentUri = Uri.withAppendedPath(PhotoContentProvider.CONTENT_URI,
				TABLE_NAME);
		return mContext.getContentResolver().delete(contentUri,
				COL_ID + "=" + id, null);
	}

	// public synchronized int clear() {
	// String folder = Preferences.getInstance(mContext)
	// .getHideImageRootPath();
	// String DB_PATH = folder + "/" + ImagesContentProviderDB.DATABASE_NAME;
	// File f = new File(DB_PATH);
	//
	// if (f.exists()
	// || Environment.MEDIA_MOUNTED.equals(Environment
	// .getExternalStorageState())) {
	// } else {
	// return -1;
	// }
	// try {
	// Uri contentUri = Uri.withAppendedPath(
	// ImagesContentProviderDB.CONTENT_URI, TABLE_NAME);
	// return mContext.getContentResolver().delete(contentUri, null, null);
	// } catch (Exception x) {
	// return -1;
	// }
	// }
	//
	public synchronized ArrayList<MediaStorageItem> getAll() {
		if (!isDBFileExist()) {
			return new ArrayList<MediaStorageItem>();
		}
		ArrayList<String> sdcard = getAllSdcard();
		ArrayList<MediaStorageItem> items = new ArrayList<MediaStorageItem>();

		Uri contentUri = Uri.withAppendedPath(PhotoContentProvider.CONTENT_URI,
				TABLE_NAME);
		Cursor cursor = mContext.getContentResolver().query(
				contentUri,
				new String[] { COL_ID, COL_ORG_PATH, COL_NAME, COL_NEW_PATH,
						COL_VD_TIME, COL_EXTENTION, COL_DATE, COL_SOLUTION },
				COL_PW_ID + "=? ",
				new String[] { PasswordTableAdapter.PASSWORD_CURRENT_ID + "" },
				COL_ID + " DESC");
		while (cursor != null && cursor.moveToNext()) {
			try {
				String sdcOldpath = cursor.getString(1);
				sdcOldpath = checkSdcard(sdcard, sdcOldpath) ? sdcOldpath
						: AppPreference.getInstance(mContext)
								.getExternalStorageDirectory() + sdcOldpath;

				String sdcNewpath = cursor.getString(3);
				sdcNewpath = checkSdcard(sdcard, sdcNewpath) ? sdcNewpath
						: AppPreference.getInstance(mContext)
								.getExternalStorageDirectory() + sdcNewpath;

				MediaStorageItem item = new MediaStorageItem();
				item.setId(cursor.getLong(0));
				/*
				 * item.setOrgPath(Preferences.getInstance(mContext)
				 * .getExternalStorageDirectory() + cursor.getString(1));
				 */
				item.setOrgPath(sdcOldpath);

				item.setName(cursor.getString(2));
				/*
				 * item.setNewPath(Preferences.getInstance(mContext)
				 * .getExternalStorageDirectory() + cursor.getString(3));
				 */
				item.setNewPath(sdcNewpath);

				item.setVideoTime(cursor.getLong(4));
				item.setExtention(cursor.getString(5));
				item.setDate(cursor.getLong(6));
				item.setSolution(cursor.getString(7));
				items.add(item);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (cursor != null)
			cursor.close();
		return items;
	}

	//
	// public synchronized boolean checkData() {
	// String folder = Preferences.getInstance(mContext)
	// .getHideImageRootPath();
	// String DB_PATH = folder + "/" + ImagesContentProviderDB.DATABASE_NAME;
	// File f = new File(DB_PATH);
	//
	// if (f.exists()
	// || Environment.MEDIA_MOUNTED.equals(Environment
	// .getExternalStorageState())) {
	// } else {
	// return false;
	// }
	// boolean isCheck = false;
	//
	// Uri contentUri = Uri.withAppendedPath(
	// ImagesContentProviderDB.CONTENT_URI, TABLE_NAME);
	// Cursor cursor = mContext.getContentResolver().query(
	// contentUri,
	// new String[] { COL_ID, COL_ORG_PATH, COL_NAME, COL_NEW_PATH,
	// COL_VD_TIME, COL_EXTENTION, COL_DATE, COL_SOLUTION },
	// COL_PW_ID + "=? ",
	// new String[] { PasswordTableAdapter.PASSWORD_CURRENT_ID + "" },
	// COL_ID + " DESC");
	// if (cursor != null && cursor.moveToNext()) {
	// isCheck = true;
	// }
	// if (cursor != null)
	// cursor.close();
	// return isCheck;
	// }
	//
	// public synchronized int getCount() {
	// String folder = Preferences.getInstance(mContext)
	// .getHideImageRootPath();
	// String DB_PATH = folder + "/" + ImagesContentProviderDB.DATABASE_NAME;
	// File f = new File(DB_PATH);
	//
	// if (f.exists()
	// || Environment.MEDIA_MOUNTED.equals(Environment
	// .getExternalStorageState())) {
	// } else {
	// return 0;
	// }
	// Cursor cursor = null;
	// int count = 0;
	// try {
	// Uri contentUri = Uri.withAppendedPath(
	// ImagesContentProviderDB.CONTENT_URI, TABLE_NAME);
	// cursor = mContext.getContentResolver().query(
	// contentUri,
	// new String[] { COL_ID },
	// COL_PW_ID + "=? ",
	// new String[] { PasswordTableAdapter.PASSWORD_CURRENT_ID
	// + "" }, null);
	// if (cursor != null) {
	// count = cursor.getCount();
	// }
	//
	// } catch (Exception ex) {
	//
	// } finally {
	// if (cursor != null)
	// cursor.close();
	// }
	// return count;
	// }
	//
	public synchronized boolean checkSdcard(ArrayList<String> arrs, String path) {
		if (!isDBFileExist()) {
			return false;
		}
		for (String item : arrs) {
			if (path.contains(item))
				return true;
		}
		return false;
	}

	public synchronized ArrayList<String> getAllSdcard() {
		ArrayList<String> arrs = new ArrayList<String>();
		String state = Environment.getExternalStorageState();
		if (!isDBFileExist()) {
			return arrs;
		}
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			final File primaryExternalStorage = Environment
					.getExternalStorageDirectory();
			final String externalStorageRootDir;
			if ((externalStorageRootDir = primaryExternalStorage.getParent()) == null) { // no
			} else {
				final File externalStorageRoot = new File(
						externalStorageRootDir);
				final File[] files = externalStorageRoot.listFiles();

				for (final File file : files) {
					if (file.isDirectory() && file.canRead()
							&& (file.listFiles().length > 0)) {
						arrs.add(file.getAbsolutePath());
					}
				}
			}
		}
		return arrs;
	}

	public synchronized byte[] getThumbnail(long id) {
		if (!isDBFileExist()) {
			return null;
		}
		Cursor cursor = null;
		byte[] thumbnail = null;
		try {
			Uri contentUri = Uri.withAppendedPath(
					PhotoContentProvider.CONTENT_URI, TABLE_NAME);
			cursor = mContext.getContentResolver().query(contentUri,
					new String[] { COL_THUMBNAIL }, COL_ID + "=" + id, null,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				thumbnail = cursor.getBlob(0);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return thumbnail;
	}
	//
	// public synchronized long updateFileName(long id, String fileName,
	// String oldPath) {
	// String folder = Preferences.getInstance(mContext)
	// .getHideImageRootPath();
	// String DB_PATH = folder + "/" + ImagesContentProviderDB.DATABASE_NAME;
	// File f = new File(DB_PATH);
	//
	// if (f.exists()
	// || Environment.MEDIA_MOUNTED.equals(Environment
	// .getExternalStorageState())) {
	// } else {
	// return -1;
	// }
	// String[] arrTmp = oldPath.split("/");
	// String oldFileName = oldPath;
	// if (arrTmp.length > 0) {
	// oldFileName = arrTmp[arrTmp.length - 1];
	// }
	//
	// ContentValues values = new ContentValues();
	// values.put(COL_NAME, fileName);
	// values.put(COL_ORG_PATH, oldPath.replace(oldFileName, fileName));
	// Uri contentUri = Uri.withAppendedPath(
	// ImagesContentProviderDB.CONTENT_URI, TABLE_NAME);
	// return mContext.getContentResolver().update(contentUri, values,
	// COL_ID + " = ?", new String[] { id + "" });
	// }
	//
	// public synchronized boolean checkFileName(String fileName) {
	// String folder = Preferences.getInstance(mContext)
	// .getHideImageRootPath();
	// String DB_PATH = folder + "/" + ImagesContentProviderDB.DATABASE_NAME;
	// File f = new File(DB_PATH);
	//
	// if (f.exists()
	// || Environment.MEDIA_MOUNTED.equals(Environment
	// .getExternalStorageState())) {
	// } else {
	// return true;
	// }
	// boolean isCheck = false;
	// Cursor cursor = null;
	// try {
	// Uri contentUri = Uri.withAppendedPath(
	// ImagesContentProviderDB.CONTENT_URI, TABLE_NAME);
	// cursor = mContext.getContentResolver().query(
	// contentUri,
	// new String[] { COL_ID },
	// COL_PW_ID + "=? AND " + COL_NAME + "=?",
	// new String[] {
	// PasswordTableAdapter.PASSWORD_CURRENT_ID + "",
	// fileName }, COL_ID + " DESC");
	// if (cursor != null && cursor.moveToNext()) {
	// isCheck = true;
	// }
	// } catch (Exception ex) {
	//
	// } finally {
	// if (cursor != null) {
	// cursor.close();
	// }
	// }
	// return isCheck;
	// }
	//
	// public static void clearInstance() {
	// instance = null;
	// }
}
