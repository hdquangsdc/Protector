package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

import java.util.ArrayList;

public class AppContentProvider extends BaseContentProvider{
	public static final String AUTHORITY = "com.protector.AppContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DATABASE_NAME = "LockedApp";


    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    ArrayList<String> getSQLCreate() {
        ArrayList<String> list=new ArrayList<>();
        list.add(AppTableAdapter.CREATE_TABLE);
        return list;
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
