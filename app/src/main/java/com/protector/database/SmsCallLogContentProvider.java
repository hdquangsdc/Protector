package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

import java.util.ArrayList;

public class SmsCallLogContentProvider extends BaseContentProvider {
	public static final String AUTHORITY = "com.protector.SmsCallLogContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DATABASE_NAME = "SmsCallog";

	@Override
	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	ArrayList<String> getSQLCreate() {
		ArrayList<String> list=new ArrayList<>();
		list.add(SmsCallLogTableAdapter.CREATE_TABLE);
		list.add(PrivateContactTableAdapter.CREATE_TABLE);
		return list;
	}

	@Override
	String getRootFolderPath(Context context) {
		return AppPreference.getInstance(context).getHideRootPath();
	}

	@Override
	Uri getContentUri() {
		return CONTENT_URI;
	}

}
