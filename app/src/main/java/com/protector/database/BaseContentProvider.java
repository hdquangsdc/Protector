package com.protector.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public abstract class BaseContentProvider extends ContentProvider {
	private DatabaseHelper mDBHelper;

	private class DatabaseHelper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private SQLiteDatabase mDatabase;

		public DatabaseHelper(Context context) {
			super(context, BaseContentProvider.this.getDBPath(context), null,
					DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			this.mDatabase = db;
			mDatabase.execSQL(getSQLCreate());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

		@Override
		public synchronized void close() {
			if (mDatabase != null && mDatabase.isOpen())
				mDatabase.close();
			super.close();
		}
	}

	protected String getDBPath(Context context) {
		return getRootFolderPath(context) + "/" + getDatabaseName();
	}

	public abstract String getDatabaseName();

	abstract String getSQLCreate();

	abstract String getRootFolderPath(Context context);

	abstract Uri getContentUri();

	@Override
	public boolean onCreate() {
		mDBHelper = new DatabaseHelper(getContext());
		if (mDBHelper != null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String table = getTableName(uri);
		SQLiteDatabase database = mDBHelper.getReadableDatabase();
		Cursor cursor = database.query(table, projection, selection,
				selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase mDB = mDBHelper.getWritableDatabase();
		long rowID = mDB.insert(getTableName(uri), null, values);
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(getContentUri(), rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to add a record into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = getTableName(uri);
		SQLiteDatabase dataBase = mDBHelper.getWritableDatabase();
		return dataBase.delete(table, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String table = getTableName(uri);
		SQLiteDatabase database = mDBHelper.getWritableDatabase();
		return database.update(table, values, selection, selectionArgs);
	}

	private String getTableName(Uri uri) {
		String value = uri.getLastPathSegment();
		return value;
	}

}
