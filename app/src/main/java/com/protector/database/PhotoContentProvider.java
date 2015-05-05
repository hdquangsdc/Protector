package com.protector.database;

import android.content.Context;
import android.net.Uri;

import com.protector.AppPreference;

import java.util.ArrayList;

public class PhotoContentProvider extends BaseContentProvider {
    public static final String AUTHORITY = "com.protector.ImagesProviderDB";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String DATABASE_NAME = "ImagesDatabase";

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    ArrayList<String> getSQLCreate() {
        ArrayList<String> list=new ArrayList<>();
        list.add(PhotoTableAdapter.CREATE_TABLE);
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
