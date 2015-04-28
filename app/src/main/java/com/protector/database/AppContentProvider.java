package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

public class AppContentProvider extends BaseContentProvider{
	public static final String AUTHORITY = "com.protector.AppContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DATABASE_NAME = "LockedApp";


    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    String getSQLCreate() {
        return AppTableAdapter.CREATE_TABLE;
    }

    @Override
    String getRootFolderPath(Context context) {
        return AppPreference.getInstance(context).getHideImageRootPath();
    }

    @Override
    Uri getContentUri() {
        return CONTENT_URI;
    }
}