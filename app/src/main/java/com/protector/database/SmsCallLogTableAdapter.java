package com.protector.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.EncryptUtils;

import java.util.ArrayList;
import java.util.Date;

public class SmsCallLogTableAdapter extends BaseTableAdapter {
    public static final int TYPE_CALL_INCOMING = 1;
    public static final int TYPE_CALL_OUTGOING = 2;
    public static final int TYPE_CALL_MISSED = 3;

    public static final int TYPE_SMS_INBOX = 1;
    public static final int TYPE_SMS_SEND = 2;
    public static final int TYPE_CALL_DRAFT = 3;

    public static final int TYPE_CALL_LOG = 2;
    public static final int TYPE_SMS = 1;

    public static final String TABLE_NAME = "sms_call_log_phone_table";

    public static final String COL_ID = "_id";
    public static final String COL_GROUP_ID = "groupid";
    public static final String COL_TYPE = "type";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_TIME = "time";
    public static final String COL_BODY = "body_sms";
    public static final String COL_READ = "read";
    public static final String COL_DATE = "date";
    public static final String COL_STATE = "state";
    public static final String COL_NUMBER_INDEX = "number_index";

    public static final String COL_DURATION_CALL_LOG = "duration_call_log";
    public static final String COL_TYPE_COMPARE = "type_compare";
    public static final String COL_THREAD_ID_SMS = "thread_id_sms";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + COL_ID + " integer primary key, "
            + COL_GROUP_ID + " integer, " + COL_TYPE + " integer not null, "
            + COL_NAME + " text, " + COL_ADDRESS + " text not null, "
            + COL_TIME + " text, " + COL_BODY + " text not null, "
            + COL_DURATION_CALL_LOG + " text, " + COL_TYPE_COMPARE
            + " integer not null, " + COL_THREAD_ID_SMS + " integer, "
            + COL_READ + " text, " + COL_DATE + " text, " + COL_STATE
            + " text, " + COL_NUMBER_INDEX + " integer)";
    private static SmsCallLogTableAdapter instance;

    private SmsCallLogTableAdapter(Context context) {
        super(context);
    }

    public static SmsCallLogTableAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new SmsCallLogTableAdapter(context);
        }
        return instance;
    }

    @Override
    protected String getDatabaseName() {
        return SmsCallLogContentProvider.DATABASE_NAME;
    }

    public synchronized ContactItem getContactName(Context context,
                                                   String phoneNumber) {
        if (!isDBFileExist()) {
            return new ContactItem();
        }
        Cursor cursor = null;
        ContactItem contact = new ContactItem();
        if (phoneNumber == null
                || (phoneNumber != null && phoneNumber.length() == 0)) {
            return contact;
        }
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contact.setName(cursor.getString(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                contact.setId(cursor.getInt(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup._ID)));
            } else {
                contact.setName(phoneNumber);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    public synchronized long addSMS(int groupId, int type, String name,
                                    String address, long time, String body, int read, int state,
                                    int numberIndex) {
        if (!isDBFileExist()) {
            return -1;
        }
        PrivateContactTableAdapter contactAdapter = PrivateContactTableAdapter
                .getInstance(mContext);
        ContactItem contact = contactAdapter.getContactByAddress(address,
                PasswordTableAdapter.PASSWORD_CURRENT_ID);
        long idContact = -1;
        if (contact != null) {
            idContact = contact.getId();
        } else {
            ContactItem contactObject = getContactName(mContext, address);
            idContact = contactAdapter.addContact(-1,
                    PrivateContactTableAdapter.TYPE_PUBLIC,
                    contactObject.getName(), address, contactObject.getId(),
                    -1, -1, "", -1, "",
                    PasswordTableAdapter.PASSWORD_CURRENT_ID);
        }
        try {
            ContentValues values = new ContentValues();
            values.put(COL_GROUP_ID, idContact);
            values.put(COL_NAME, EncryptUtils.encryptV1(name));
            values.put(COL_ADDRESS, EncryptUtils.encryptV1(address));
            values.put(COL_TIME, time);
            values.put(COL_BODY, EncryptUtils.encryptV1(body));
            values.put(COL_DURATION_CALL_LOG, EncryptUtils.encryptV1("1"));
            values.put(COL_TYPE, type);
            values.put(COL_READ, read);
            values.put(COL_DATE, new Date().getTime());
            values.put(COL_STATE, state);
            values.put(COL_NUMBER_INDEX, numberIndex);
            values.put(COL_TYPE_COMPARE, 1);
            values.put(COL_THREAD_ID_SMS, 0);
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
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

    // public synchronized void addArraySmsWithSingleAddress(
    // ArrayList<SmsCalLogObject> arrSms) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return;
    // }
    // if (arrSms.size() > 0) {
    // ArrayList<SmsCalLogObject> myArr = arrSms;
    // PrivateContactTableAdapter contactAdapter = PrivateContactTableAdapter
    // .getInstance(mContext);
    // ContactObject contact = contactAdapter.getContactByAddress(myArr
    // .get(0).getAddress(),
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // long idContact = -1;
    // if (contact != null) {
    // idContact = contact.getId();
    // } else {
    // ContactObject contactObject = getContactName(mContext, myArr
    // .get(0).getAddress());
    // idContact = contactAdapter.addContact(-1,
    // PrivateContactTableAdapter.TYPE_PUBLIC,
    // contactObject.getName(), myArr.get(0).getAddress(),
    // contactObject.getId(), -1, -1, "", -1, "",
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // }
    // for (SmsCalLogObject sms : myArr) {
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, idContact);
    // values.put(COL_NAME, EncryptUtils.encryptV1(sms.getName()));
    // values.put(COL_ADDRESS,
    // EncryptUtils.encryptV1(sms.getAddress()));
    // values.put(COL_TIME, sms.getTime());
    // values.put(COL_BODY,
    // EncryptUtils.encryptV1(sms.getBodySms()));
    // values.put(COL_TYPE, sms.getType());
    // values.put(COL_READ, sms.getRead());
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, sms.getState());
    // values.put(COL_NUMBER_INDEX, sms.getNumberIndex());
    // values.put(COL_TYPE_COMPARE, 1);
    // values.put(COL_DURATION_CALL_LOG,
    // EncryptUtils.encryptV1("1"));
    // values.put(COL_THREAD_ID_SMS, 0);
    // Uri contentUri = Uri
    // .withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI,
    // TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(
    // contentUri, values);
    // } catch (Exception ex) {
    // }
    // }
    // }
    // }
    //
    // public synchronized void addArraySms(ArrayList<SmsCalLogObject> arrSms,
    // long groupID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return;
    // }
    // if (arrSms.size() > 0) {
    // ArrayList<SmsCalLogObject> myArr = arrSms;
    // for (SmsCalLogObject sms : myArr) {
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, groupID);
    // values.put(COL_NAME, EncryptUtils.encryptV1(sms.getName()));
    // values.put(COL_ADDRESS,
    // EncryptUtils.encryptV1(sms.getAddress()));
    // values.put(COL_TIME, sms.getTime());
    // values.put(COL_BODY,
    // EncryptUtils.encryptV1(sms.getBodySms()));
    // values.put(COL_TYPE, sms.getType());
    // values.put(COL_READ, sms.getRead());
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, sms.getState());
    // values.put(COL_NUMBER_INDEX, sms.getNumberIndex());
    // values.put(COL_TYPE_COMPARE, 1);
    // values.put(COL_DURATION_CALL_LOG,
    // EncryptUtils.encryptV1("1"));
    // values.put(COL_THREAD_ID_SMS, 0);
    // Uri contentUri = Uri
    // .withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI,
    // TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(
    // contentUri, values);
    // } catch (Exception ex) {
    // }
    // }
    // }
    // }
    //
    // public synchronized void addArraySms(SmsCalLogObject sms, long groupID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return;
    // }
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, groupID);
    // values.put(COL_NAME, EncryptUtils.encryptV1(sms.getName()));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(sms.getAddress()));
    // values.put(COL_TIME, sms.getTime());
    // values.put(COL_BODY, EncryptUtils.encryptV1(sms.getBodySms()));
    // values.put(COL_TYPE, sms.getType());
    // values.put(COL_READ, sms.getRead());
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, sms.getState());
    // values.put(COL_NUMBER_INDEX, sms.getNumberIndex());
    // values.put(COL_TYPE_COMPARE, 1);
    // values.put(COL_DURATION_CALL_LOG, EncryptUtils.encryptV1("1"));
    // values.put(COL_THREAD_ID_SMS, 0);
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // } catch (Exception ex) {
    // }
    // }
    //
    // public synchronized void addArrayCallLogWithSingleAddress(
    // ArrayList<SmsCalLogObject> arrCallLog) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return;
    // }
    // if (arrCallLog.size() > 0) {
    // ArrayList<SmsCalLogObject> myArr = arrCallLog;
    // PrivateContactTableAdapter contactAdapter = PrivateContactTableAdapter
    // .getInstance(mContext);
    // ContactObject contact = contactAdapter.getContactByAddress(myArr
    // .get(0).getAddress(),
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // long idContact = -1;
    // if (contact != null) {
    // idContact = contact.getId();
    // } else {
    // ContactObject contactObject = getContactName(mContext, myArr
    // .get(0).getAddress());
    // idContact = contactAdapter.addContact(-1,
    // PrivateContactTableAdapter.TYPE_PUBLIC,
    // contactObject.getName(), myArr.get(0).getAddress(),
    // contactObject.getId(), -1, -1, "", -1, "",
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // }
    // for (SmsCalLogObject callLog : myArr) {
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, idContact);
    // values.put(COL_NAME,
    // EncryptUtils.encryptV1(callLog.getName()));
    // values.put(COL_ADDRESS,
    // EncryptUtils.encryptV1(callLog.getAddress()));
    // values.put(COL_TIME, callLog.getTime());
    // values.put(
    // COL_DURATION_CALL_LOG,
    // EncryptUtils.encryptV1(callLog.getDurationCallLog()
    // + ""));
    // values.put(COL_TYPE, callLog.getType());
    // values.put(COL_READ, callLog.getRead());
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, callLog.getState());
    // values.put(COL_NUMBER_INDEX, callLog.getNumberIndex());
    // values.put(COL_TYPE_COMPARE, 2);
    // values.put(COL_BODY, EncryptUtils.encryptV1(""));
    // Uri contentUri = Uri
    // .withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI,
    // TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(
    // contentUri, values);
    // } catch (Exception ex) {
    // }
    // }
    // }
    // }
    //
    // public synchronized void addArrayCallLog(
    // ArrayList<SmsCalLogObject> arrCallLog, long groupID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return;
    // }
    // ArrayList<SmsCalLogObject> myArr = arrCallLog;
    // for (SmsCalLogObject callLog : myArr) {
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, groupID);
    // values.put(COL_NAME, EncryptUtils.encryptV1(callLog.getName()));
    // values.put(COL_ADDRESS,
    // EncryptUtils.encryptV1(callLog.getAddress()));
    // values.put(COL_TIME, callLog.getTime());
    // values.put(
    // COL_DURATION_CALL_LOG,
    // EncryptUtils.encryptV1(callLog.getDurationCallLog()
    // + ""));
    // values.put(COL_TYPE, callLog.getType());
    // values.put(COL_READ, callLog.getRead());
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, callLog.getState());
    // values.put(COL_NUMBER_INDEX, callLog.getNumberIndex());
    // values.put(COL_TYPE_COMPARE, 2);
    // values.put(COL_BODY, EncryptUtils.encryptV1(""));
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // } catch (Exception ex) {
    // }
    // }
    // }
    //
    // public synchronized long addCallog(int groupId, int type, String name,
    // String address, long time, long duration, int read, int state,
    // int numberIndex) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    // String state1 = Environment.getExternalStorageState();
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())
    // || Environment.MEDIA_MOUNTED.equals(state1)) {
    // } else {
    // return -1;
    // }
    // PrivateContactTableAdapter contactAdapter = PrivateContactTableAdapter
    // .getInstance(mContext);
    // ContactObject contact = contactAdapter.getContactByAddress(address,
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // long idContact = -1;
    // if (contact != null) {
    // idContact = contact.getId();
    // } else {
    // ContactObject contactObject = getContactName(mContext, address);
    // idContact = contactAdapter.addContact(-1,
    // PrivateContactTableAdapter.TYPE_PUBLIC,
    // contactObject.getName(), address, contactObject.getId(),
    // -1, -1, "", -1, "",
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // }
    // try {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, idContact);
    // values.put(COL_NAME, EncryptUtils.encryptV1(name));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(address));
    // values.put(COL_TIME, time);
    // values.put(COL_DURATION_CALL_LOG,
    // EncryptUtils.encryptV1(duration + ""));
    // values.put(COL_TYPE, type);
    // values.put(COL_READ, read);
    // values.put(COL_DATE, new Date().getTime());
    // values.put(COL_STATE, state);
    // values.put(COL_NUMBER_INDEX, numberIndex);
    // values.put(COL_TYPE_COMPARE, 2);
    // values.put(COL_BODY, EncryptUtils.encryptV1(""));
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // if (result == null) {
    // return 0;
    // }
    // return Long.parseLong(result.getLastPathSegment());
    // } catch (Exception ex) {
    // return 0;
    // }
    // }
    //
    // public synchronized int removeID(long id) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return -1;
    // }
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().delete(contentUri,
    // COL_ID + "=" + id, null);
    // }
    //
    // public synchronized int removeByGroupID(long groupID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return -1;
    // }
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().delete(contentUri,
    // COL_GROUP_ID + "=" + groupID, null);
    // }
    //
    // public synchronized int clear() {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return -1;
    // }
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().delete(contentUri, null, null);
    // }
    //
    public synchronized ArrayList<SmsCallLogItem> getAll() {
        ArrayList<SmsCallLogItem> items = new ArrayList<SmsCallLogItem>();
        if (!isDBFileExist()) {
            return items;
        }
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver()
                    .query(contentUri,
                            new String[]{COL_ID, COL_GROUP_ID, COL_TYPE,
                                    COL_NAME, COL_ADDRESS, COL_TIME, COL_BODY,
                                    COL_READ, COL_DATE, COL_STATE,
                                    COL_NUMBER_INDEX, COL_DURATION_CALL_LOG,
                                    COL_TYPE_COMPARE, COL_THREAD_ID_SMS},
                            null, null, COL_TIME + " DESC");
            while (cursor != null && cursor.moveToNext()) {
                SmsCallLogItem item = new SmsCallLogItem();
                item.setId(cursor.getInt(0));
                item.setGroupId(cursor.getInt(1));
                item.setType(cursor.getInt(2));
                item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
                item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
                item.setTime(Long.parseLong(cursor.getString(5)));
                item.setBodySms(EncryptUtils.decryptV1(cursor.getString(6)));
                item.setRead(cursor.getInt(7));
                if (item.getRead() == 0) {
                    updateType(item.getId());
                }
                item.setDate(cursor.getLong(8));
                item.setState(cursor.getInt(9));
                item.setNumberIndex(cursor.getInt(10));
                item.setDurationCallLog(Long.parseLong(EncryptUtils
                        .decryptV1(cursor.getString(11))));
                item.setTypeCompare(cursor.getInt(12));
                item.setThreadIdSms(cursor.getInt(13));
                items.add(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return items;
    }

    public synchronized boolean checkData() {
        if (!isDBFileExist()) {
            return false;
        }
        Cursor cursor = null;
        boolean isCheck = false;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver()
                    .query(contentUri,
                            new String[]{COL_ID, COL_GROUP_ID, COL_TYPE,
                                    COL_NAME, COL_ADDRESS, COL_TIME, COL_BODY,
                                    COL_READ, COL_DATE, COL_STATE,
                                    COL_NUMBER_INDEX, COL_DURATION_CALL_LOG,
                                    COL_TYPE_COMPARE, COL_THREAD_ID_SMS},
                            null, null, COL_TIME + " DESC");
            if (cursor != null && cursor.moveToNext()) {
                isCheck = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return isCheck;
    }

    //
    // public synchronized ArrayList<SmsCalLogObject> getAllByGroupID(int
    // groupID) {
    // ArrayList<SmsCalLogObject> items = new ArrayList<SmsCalLogObject>();
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return items;
    // }
    // Cursor cursor = null;
    // try {
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // cursor = mContext.getContentResolver().query(
    // contentUri,
    // new String[] { COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
    // COL_ADDRESS, COL_TIME, COL_BODY, COL_READ,
    // COL_DATE, COL_STATE, COL_NUMBER_INDEX,
    // COL_DURATION_CALL_LOG, COL_TYPE_COMPARE,
    // COL_THREAD_ID_SMS }, COL_GROUP_ID + " = ? ",
    // new String[] { groupID + "" }, COL_TIME + " DESC");// ASC
    // while (cursor != null && cursor.moveToNext()) {
    // SmsCalLogObject item = new SmsCalLogObject();
    // item.setId(cursor.getInt(0));
    // item.setGroupId(cursor.getInt(1));
    // item.setType(cursor.getInt(2));
    // item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
    // item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
    // item.setTime(Long.parseLong(cursor.getString(5)));
    // item.setBodySms(EncryptUtils.decryptV1(cursor.getString(6)));
    // item.setRead(cursor.getInt(7));
    // if (item.getRead() == 0) {
    // updateType(item.getId());
    // }
    // item.setDate(cursor.getLong(8));
    // item.setState(cursor.getInt(9));
    // item.setNumberIndex(cursor.getInt(10));
    // item.setDurationCallLog(Long.parseLong(EncryptUtils
    // .decryptV1(cursor.getString(11))));
    // item.setTypeCompare(cursor.getInt(12));
    // item.setThreadIdSms(cursor.getInt(13));
    // items.add(item);
    // }
    // } catch (Exception ex) {
    //
    // } finally {
    // if (cursor != null)
    // cursor.close();
    // }
    // return items;
    // }
    //
    public synchronized SmsCallLogItem getSMSCallLogLast(int groupID) {
        if (!isDBFileExist()) {
            return null;
        }
        SmsCallLogItem item = null;
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
                            COL_ADDRESS, COL_TIME, COL_BODY, COL_READ,
                            COL_DATE, COL_STATE, COL_NUMBER_INDEX,
                            COL_DURATION_CALL_LOG, COL_TYPE_COMPARE,
                            COL_THREAD_ID_SMS}, COL_GROUP_ID + " = ? ",
                    new String[]{groupID + ""}, COL_TIME + " DESC");
            if (cursor != null && cursor.moveToNext()) {
                item = new SmsCallLogItem();
                item.setId(cursor.getInt(0));
                item.setGroupId(cursor.getInt(1));
                item.setType(cursor.getInt(2));
                item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
                item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
                item.setTime(Long.parseLong(cursor.getString(5)));
                item.setBodySms(EncryptUtils.decryptV1(cursor.getString(6)));
                item.setRead(cursor.getInt(7));
                item.setDate(cursor.getLong(8));
                item.setState(cursor.getInt(9));
                item.setNumberIndex(cursor.getInt(10));
                item.setDurationCallLog(Long.parseLong(EncryptUtils
                        .decryptV1(cursor.getString(11))));
                item.setTypeCompare(cursor.getInt(12));
                item.setThreadIdSms(cursor.getInt(13));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return item;
    }

    public synchronized int getSMSCallNotRead(int groupID) {
        if (!isDBFileExist()) {
            return 0;
        }
        Cursor cursor = null;
        int count = 0;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_ID},
                    COL_GROUP_ID + " = ? AND " + COL_READ + " =? AND "
                            + COL_TYPE_COMPARE + " = ? AND " + COL_TYPE
                            + " = ?",
                    new String[]{groupID + "", "0",
                            SmsCallLogTableAdapter.TYPE_SMS + "",
                            SmsCallLogTableAdapter.TYPE_SMS_INBOX + ""}, null);
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getCount();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public synchronized int getAllSMS(int passID) {
        if (!isDBFileExist()) {
            return -1;
        }
        PrivateContactTableAdapter contactTable = PrivateContactTableAdapter
                .getInstance(mContext);
        Uri contentUri = Uri.withAppendedPath(
                SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
        ArrayList<ContactItem> contact = contactTable.getAllContact(passID);
        int count = 0;
        for (ContactItem item : contact) {
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(
                        contentUri,
                        null,
                        COL_TYPE_COMPARE + " = ? AND " + COL_GROUP_ID + " = ?",
                        new String[]{SmsCallLogTableAdapter.TYPE_SMS + "",
                                item.getId() + ""}, null);
                if (cursor != null) {
                    count += cursor.getCount();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return count;
    }

    public synchronized int updateType(long id) {
        if (!isDBFileExist()) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COL_READ, 1);
        Uri contentUri = Uri.withAppendedPath(
                SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
        return mContext.getContentResolver().update(contentUri, values,
                COL_ID + " = ?", new String[]{id + ""});
    }
}
