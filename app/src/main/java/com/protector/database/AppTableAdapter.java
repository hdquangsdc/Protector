package com.protector.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.protector.AppPreference;
import com.protector.utils.EncryptUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ho on 4/20/2015.
 */
public class AppTableAdapter extends BaseTableAdapter {


    public static final String TABLE_NAME = "app_locked";

    public static final String COL_ID = "id";
    public static final String COL_PASSWORD_ID = "password_id";
    public static final String COL_NAME = "app_name";
    public static final String COL_PACKAGE = "app_package";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + COL_ID + " integer primary key, " + COL_NAME
            + " text, " + COL_PASSWORD_ID + " integer, " + COL_PACKAGE
            + " text not null)";
    private static AppTableAdapter mInstance;

    private AppTableAdapter(Context context) {
        super(context);
    }

    public static AppTableAdapter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppTableAdapter(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    protected String getDatabaseName() {
        return AppContentProvider.DATABASE_NAME;
    }

    public synchronized long add(String appName, String appPakage) {
        if (!isDBFileExist()) {
            return -1;
        }
        if (checkApp(appPakage))
            return -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, EncryptUtils.encryptV1(appName));
            values.put(COL_PACKAGE, EncryptUtils.encryptV1(appPakage));
            values.put(COL_PASSWORD_ID,
                    PasswordTableAdapter.PASSWORD_CURRENT_ID);
            Uri contentUri = Uri.withAppendedPath(
                    AppContentProvider.CONTENT_URI, TABLE_NAME);
            Uri result = mContext.getContentResolver().insert(contentUri,
                    values);
            if (result == null) {
                return 0;
            }
            return Long.parseLong(result.getLastPathSegment());
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    //    public synchronized int remove(String pakageName, int passwordID) {
//        String folder = Preferences.getInstance(mContext).getHideRootPath();
//        String DB_PATH = folder + "/" + AppLockContentProviderDB.DATABASE_NAME;
//        File f = new File(DB_PATH);
//
//        if (f.exists()
//                || Environment.MEDIA_MOUNTED.equals(Environment
//                .getExternalStorageState())) {
//        } else {
//            return -1;
//        }
//        Uri contentUri = Uri.withAppendedPath(
//                AppLockContentProviderDB.CONTENT_URI, TABLE_NAME);
//        return mContext.getContentResolver().delete(
//                contentUri,
//                COL_PACKAGE + "=? AND " + COL_PASSWORD_ID + "= ?",
//                new String[] { EncryptUtils.encryptV1(pakageName),
//                        passwordID + "" });
//    }
//
//    public synchronized ArrayList<Integer> getPasswordApp(String pakageName) {
//        ArrayList<Integer> passIDs = new ArrayList<Integer>();
//        String folder = Preferences.getInstance(mContext).getHideRootPath();
//        String DB_PATH = folder + "/" + AppLockContentProviderDB.DATABASE_NAME;
//        File f = new File(DB_PATH);
//
//        if (f.exists()
//                || Environment.MEDIA_MOUNTED.equals(Environment
//                .getExternalStorageState())) {
//        } else {
//            return passIDs;
//        }
//        Cursor cursor = null;
//        try {
//            Uri contentUri = Uri.withAppendedPath(
//                    AppLockContentProviderDB.CONTENT_URI, TABLE_NAME);
//            cursor = mContext.getContentResolver().query(contentUri,
//                    new String[] { COL_PASSWORD_ID }, COL_PACKAGE + " = ?",
//                    new String[] { EncryptUtils.encryptV1(pakageName) }, null);
//            while (cursor != null && cursor.moveToNext()) {
//                passIDs.add(cursor.getInt(cursor
//                        .getColumnIndex(COL_PASSWORD_ID)));
//            }
//        } catch (Exception ex) {
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return passIDs;
//    }
//
//    public synchronized int clear() {
//        String folder = Preferences.getInstance(mContext).getHideRootPath();
//        String DB_PATH = folder + "/" + AppLockContentProviderDB.DATABASE_NAME;
//        File f = new File(DB_PATH);
//
//        if (f.exists()
//                || Environment.MEDIA_MOUNTED.equals(Environment
//                .getExternalStorageState())) {
//        } else {
//            return -1;
//        }
//        Uri contentUri = Uri.withAppendedPath(
//                AppLockContentProviderDB.CONTENT_URI, TABLE_NAME);
//        return mContext.getContentResolver().delete(contentUri, null, null);
//    }
//
    public synchronized boolean checkApp(String packagename) {
        if (!isDBFileExist()) {
            return false;
        }
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    AppContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_PACKAGE},
                    COL_PASSWORD_ID + " = ? AND " + COL_PACKAGE + "=?",
                    new String[]{
                            PasswordTableAdapter.PASSWORD_CURRENT_ID + "",
                            EncryptUtils.encryptV1(packagename)}, null);
            if (cursor != null && cursor.moveToNext()) {
                cursor.close();
                cursor = null;
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return false;
    }

    public synchronized ArrayList<String> getAll() {
        ArrayList<String> items;
        items = new ArrayList<>();

        if (!isDBFileExist()) {
            return items;
        }
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    AppContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_PACKAGE},
                    COL_PASSWORD_ID + " = ?",
                    new String[]{PasswordTableAdapter.PASSWORD_CURRENT_ID
                            + ""}, COL_NAME);
            while (cursor != null && cursor.moveToNext()) {
                String packageName = EncryptUtils
                        .decryptV1(cursor.getString(0));
                try {
                    mContext.getPackageManager().getApplicationInfo(
                            packageName, 0);
                    items.add(packageName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return items;
    }

    //    public boolean checkData(){
//        String folder = Preferences.getInstance(mContext).getHideRootPath();
//        String DB_PATH = folder + "/" + AppLockContentProviderDB.DATABASE_NAME;
//        File f = new File(DB_PATH);
//
//        if (f.exists()
//                || Environment.MEDIA_MOUNTED.equals(Environment
//                .getExternalStorageState())) {
//        } else {
//            return false;
//        }
//        Cursor cursor = null;
//        boolean isCheck = false;
//        try {
//            Uri contentUri = Uri.withAppendedPath(
//                    AppLockContentProviderDB.CONTENT_URI, TABLE_NAME);
//            cursor = mContext.getContentResolver().query(
//                    contentUri,
//                    new String[] { COL_PACKAGE },
//                    COL_PASSWORD_ID + " = ?",
//                    new String[] { PasswordTableAdapter.PASSWORD_CURRENT_ID
//                            + "" }, COL_NAME);
//            if (cursor != null && cursor.moveToNext()) {
//                isCheck = true;
//            }
//        } catch (Exception e) {
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return isCheck;
//    }
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
                    new String[]{COL_PACKAGE}, null, null, COL_NAME);
            while (cursor != null && cursor.moveToNext()) {
                String packageName = EncryptUtils
                        .decryptV1(cursor.getString(0));
                try {
                    mContext.getPackageManager().getApplicationInfo(
                            packageName, 0);
                    items.add(packageName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return items;
    }
}
