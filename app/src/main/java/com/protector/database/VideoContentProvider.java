package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

public class VideoContentProvider extends BaseContentProvider {
	public static final String AUTHORITY = "com.protector.VideosProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DATABASE_NAME = "VideosDatabase";

	@Override
	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	String getSQLCreate() {
		return VideoTableAdapter.CREATE_TABLE;
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
