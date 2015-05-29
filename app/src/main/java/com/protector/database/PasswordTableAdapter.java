package com.protector.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.protector.AppPreference;
import com.protector.utils.EncryptUtils;

import java.io.File;

public class PasswordTableAdapter {

    public static final String TABLE_NAME = "password_table";
    public static int PASSWORD_1 = 1;
    public static int PASSWORD_CURRENT_ID;
    public static String PASSWORD_CURRENT_TEXT;

    public static final String COL_ID = "_id";
    public static final String COL_PASSWORD = "password";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + COL_ID + " integer primary key, "
            + COL_PASSWORD + " text)";

    private Context mContext;
    private static PasswordTableAdapter instance;
    private PasswordContentProvider mContentProvider;

    private PasswordTableAdapter(Context context) {
        mContext = context.getApplicationContext();
        mContentProvider = new PasswordContentProvider();
    }

    public static PasswordTableAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new PasswordTableAdapter(context);
        }
        return instance;
    }

    public synchronized long addPassword(long id, String password) {
        String folder = AppPreference.getInstance(mContext).getHideRootPath();
        String DB_PATH = folder + "/" + PasswordContentProvider.DATABASE_NAME;
        File f = new File(DB_PATH);

        if (f.exists()
                || Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
        } else {
            return -1;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(COL_ID, id);
            values.put(COL_PASSWORD,
                    new String(EncryptUtils.encryptV1(password)));
            Uri contentUri = Uri.withAppendedPath(
                    PasswordContentProvider.CONTENT_URI, TABLE_NAME);
            Uri result = mContext.getContentResolver().insert(contentUri,
                    values);
            if (result == null) {
                return 0;
            }
            return Long.parseLong(result.getLastPathSegment());
        } catch (Exception ex) {
            return 0;
        }

    }

    public synchronized String checkPassword(long id) {
        String folder = AppPreference.getInstance(mContext).getHideRootPath();
        String DB_PATH = folder + "/" + PasswordContentProvider.DATABASE_NAME;
        File f = new File(DB_PATH);

        if (f.exists()
                || Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
        } else {
            return null;
        }
        String pw = null;
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    PasswordContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(contentUri, null,
                    COL_ID + " = ?", new String[]{id + ""}, null);
            if (cursor != null && cursor.moveToNext()) {
                pw = cursor.getString(cursor.getColumnIndex(COL_PASSWORD));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pw;
    }

    public synchronized int checkPassword(String password) {
        String folder = AppPreference.getInstance(mContext).getHideRootPath();
        String DB_PATH = folder + "/" + PasswordContentProvider.DATABASE_NAME;
        File f = new File(DB_PATH);

        if (f.exists()
                || Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
        } else {
            return -1;
        }
        int pw = -1;
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    PasswordContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(contentUri, null,
                    COL_PASSWORD + " = ?",
                    new String[]{EncryptUtils.encryptV1(password)}, null);
            if (cursor != null && cursor.moveToNext()) {
                pw = cursor.getInt(cursor.getColumnIndex(COL_ID));
                if (pw < 4) {

                } else {
                    pw = -1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return pw;
    }
    //
    // public synchronized int checkPatternPassword(String password) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/" + PasswordContentProvider.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return -1;
    // }
    // int pw = -1;
    // Cursor cursor = null;
    // try {
    // Uri contentUri = Uri.withAppendedPath(
    // PasswordContentProviderDB.CONTENT_URI, TABLE_NAME);
    // cursor = mContext.getContentResolver().query(contentUri, null,
    // COL_PASSWORD + " = ?",
    // new String[] { EncryptUtils.encryptV1(password) }, null);
    // if (cursor != null && cursor.moveToNext()) {
    // pw = cursor.getInt(cursor.getColumnIndex(COL_ID));
    // if (pw >= 4) {
    // } else {
    // if (cursor.moveToNext()) {
    // pw = cursor.getInt(cursor.getColumnIndex(COL_ID));
    // }
    // }
    // }
    //
    // } catch (Exception ex) {
    //
    // } finally {
    // if (cursor != null)
    // cursor.close();
    // }
    // return pw;
    // }

    public synchronized long updatePassword(long id, String password) {
        String folder = AppPreference.getInstance(mContext).getHideRootPath();
        String DB_PATH = folder + "/" + PasswordContentProvider.DATABASE_NAME;
        File f = new File(DB_PATH);

        if (f.exists()
                || Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
        } else {
            return -1;
        }
        if (checkPassword(id) == null) {
            return addPassword(id, password);
        }
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, new String(EncryptUtils.encryptV1(password)));
        Uri contentUri = Uri.withAppendedPath(
                PasswordContentProvider.CONTENT_URI, TABLE_NAME);
        return mContext.getContentResolver().update(contentUri, values,
                COL_ID + " = ?", new String[]{id + ""});
    }
    //
    // public synchronized int clear() {
    // String folder = AppPreference.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/" + PasswordContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return -1;
    // }
    // Uri contentUri = Uri.withAppendedPath(
    // PasswordContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().delete(contentUri, null, null);
    // }
    //
    // public synchronized int clear(long id) {
    // Uri contentUri = Uri.withAppendedPath(
    // PasswordContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().delete(contentUri,
    // COL_ID + " = ?", new String[] { id + "" });
    // }
}
