package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

import java.util.ArrayList;

public class VideoContentProvider extends BaseContentProvider {
	public static final String AUTHORITY = "com.protector.VideosProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DATABASE_NAME = "VideosDatabase";

	@Override
	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	ArrayList<String> getSQLCreate() {
		ArrayList<String> list=new ArrayList<>();
		list.add(VideoTableAdapter.CREATE_TABLE);
		return list;
	}

	@Override
	String getRootFolderPath(Context context) {
		return AppPreference.getInstance(context).getHideVideoRootPath();
	}

	@Override
	Uri getContentUri() {
		return CONTENT_URI;
	}

}
