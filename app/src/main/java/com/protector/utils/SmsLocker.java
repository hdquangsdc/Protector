package com.protector.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.SmsCallLogItem;

import java.util.ArrayList;
import java.util.HashMap;


public class SmsLocker {
    private Context mContext;
    private static SmsLocker instance;
    private ArrayList<SmsCallLogItem> myArraySMS;
    private Cursor cursor = null;

    public static final String SMS_CONTENT = "content://sms/";

    public static final Uri SMS_CONTENT_URI = Uri.parse(SMS_CONTENT);
    private String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
    private Uri callUri = Uri.parse("content://call_log/calls");

    public class TextSmsColumns {
        public static final String ID = "_id";
        public static final String ADDRESS = "address";
        public static final String DATE = "date";
        public static final String BODY = "body";
        public static final String THREAD_ID = "thread_id";
        public static final String PROTOCOL = "protocol";
        public static final String PERSON = "person";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String SUBJECT = "subject";
        public static final String READ = "read";
    }

    private SmsLocker(Context context) {
        mContext = context;
    }

    public static SmsLocker getInstance(Context context) {
        if (instance == null) {
            instance = new SmsLocker(context);
        }
        return instance;
    }

    public ArrayList<SmsCallLogItem> getAllSMS() {
        myArraySMS = new ArrayList<>();
        cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    SMS_CONTENT_URI,
                    new String[]{TextSmsColumns.ID, TextSmsColumns.ADDRESS,
                            TextSmsColumns.DATE, TextSmsColumns.BODY,
                            TextSmsColumns.THREAD_ID, TextSmsColumns.PROTOCOL,
                            TextSmsColumns.PERSON, TextSmsColumns.STATUS,
                            TextSmsColumns.TYPE, TextSmsColumns.SUBJECT,
                            TextSmsColumns.READ}, null, null,
                    TextSmsColumns.DATE + " DESC");
            HashMap<String, byte[]> hmAvatar = new HashMap<>();
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor
                            .getColumnIndex(TextSmsColumns.ADDRESS)) == null) {
                        continue;
                    }
                    try {
                        SmsCallLogItem object = new SmsCallLogItem();
                        object.setTime(cursor.getLong(cursor
                                .getColumnIndex(TextSmsColumns.DATE)));
                        object.setBodySms(cursor.getString(cursor
                                .getColumnIndex(TextSmsColumns.BODY)));
                        object.setAddress(cursor.getString(cursor
                                .getColumnIndex(TextSmsColumns.ADDRESS)));
                        try {
                            if (object.getAddress().length() > 0) {
                                byte[] avatar = hmAvatar.get(object
                                        .getAddress());
                                if (avatar != null) {
                                    if (avatar.length == 0) {
                                        object.setAvatarByte(null);
                                    } else {
                                        object.setAvatarByte(avatar);
                                    }
                                } else {
                                    byte[] avatarGet = getPhotoContact(
                                            object.getAddress());
                                    object.setAvatarByte(avatarGet);
                                    if (avatarGet == null) {
                                        hmAvatar.put(object.getAddress(),
                                                new byte[0]);
                                    } else {
                                        hmAvatar.put(object.getAddress(),
                                                avatarGet);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        object.setName("");
                        object.setState(cursor.getInt(cursor
                                .getColumnIndex(TextSmsColumns.STATUS)));
                        object.setGroupId(-1);
                        object.setType(cursor.getInt(cursor
                                .getColumnIndex(TextSmsColumns.TYPE)));
                        object.setNumberIndex(cursor.getInt(cursor
                                .getColumnIndex(TextSmsColumns.ID)));
                        int read = cursor.getInt(cursor
                                .getColumnIndex(TextSmsColumns.READ));
                        object.setRead(read);
                        myArraySMS.add(object);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return myArraySMS;
    }

    public byte[] getPhotoContact(String phoneNumber) {
        if (phoneNumber == null)
            return null;
        byte[] byteImg = null;
        Cursor cursor = null;
        Cursor c = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            String[] projection = new String[]{PhoneLookup.PHOTO_ID};
            Uri contactUri = Uri.withAppendedPath(
                    PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            cursor = cr.query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int photoId = cursor.getInt(cursor
                        .getColumnIndex(PhoneLookup.PHOTO_ID));

                Uri photoUri = ContentUris.withAppendedId(
                        ContactsContract.Data.CONTENT_URI, photoId);
                c = mContext
                        .getContentResolver()
                        .query(photoUri,
                                new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                                null, null, null);
                if (c.moveToFirst()) {
                    byteImg = c.getBlob(0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return byteImg;
    }


    public ArrayList<SmsCallLogItem> getAllSMSByAddress(String address) {
        String name = getContactName(mContext, address);
        if (name == null) {
            name = address;
        }
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        cursor = null;
        myArraySMS = new ArrayList<>();
        try {
            cursor = mContext.getContentResolver().query(
                    SMS_CONTENT_URI,
                    new String[]{TextSmsColumns.ID, TextSmsColumns.ADDRESS,
                            TextSmsColumns.DATE, TextSmsColumns.BODY,
                            TextSmsColumns.THREAD_ID, TextSmsColumns.PROTOCOL,
                            TextSmsColumns.PERSON, TextSmsColumns.STATUS,
                            TextSmsColumns.TYPE, TextSmsColumns.SUBJECT,
                            TextSmsColumns.READ},
                    TextSmsColumns.ADDRESS + " IN (?,?)",
                    new String[]{addr[0].toString(), addr[1].toString()},
                    TextSmsColumns.DATE + " DESC");
            SmsCallLogItem object = null;
            if (cursor.moveToFirst()) {
                do {
                    object = new SmsCallLogItem();
                    object.setTime(cursor.getLong(cursor
                            .getColumnIndex(TextSmsColumns.DATE)));
                    object.setBodySms(cursor.getString(cursor
                            .getColumnIndex(TextSmsColumns.BODY)));
                    object.setAddress(cursor.getString(cursor
                            .getColumnIndex(TextSmsColumns.ADDRESS)));
                    object.setName(name);
                    object.setState(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.STATUS)));
                    object.setGroupId(-1);
                    object.setType(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.TYPE)));
                    object.setNumberIndex(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.ID)));
                    int read = cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.READ));
                    object.setRead(read);
                    myArraySMS.add(object);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return myArraySMS;
    }

    public ArrayList<SmsCallLogItem> getAllSMSByAddress(String[] address) {
        String name = getContactName(mContext, address[0]);
        if (name == null) {
            name = address[0];
        }
        cursor = null;
        myArraySMS = new ArrayList<>();
        try {
            cursor = mContext
                    .getContentResolver()
                    .query(SMS_CONTENT_URI,
                            new String[]{TextSmsColumns.ID,
                                    TextSmsColumns.ADDRESS,
                                    TextSmsColumns.DATE, TextSmsColumns.BODY,
                                    TextSmsColumns.THREAD_ID,
                                    TextSmsColumns.PROTOCOL,
                                    TextSmsColumns.PERSON,
                                    TextSmsColumns.STATUS, TextSmsColumns.TYPE,
                                    TextSmsColumns.SUBJECT, TextSmsColumns.READ},
                            TextSmsColumns.ADDRESS + " IN (?,?)",
                            new String[]{address[0].toString(),
                                    address[1].toString()},
                            TextSmsColumns.DATE + " DESC");
            SmsCallLogItem object = null;
            if (cursor.moveToFirst()) {
                do {
                    object = new SmsCallLogItem();
                    object.setTime(cursor.getLong(cursor
                            .getColumnIndex(TextSmsColumns.DATE)));
                    object.setBodySms(cursor.getString(cursor
                            .getColumnIndex(TextSmsColumns.BODY)));
                    object.setAddress(cursor.getString(cursor
                            .getColumnIndex(TextSmsColumns.ADDRESS)));
                    object.setName(name);
                    object.setState(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.STATUS)));
                    object.setGroupId(-1);
                    object.setType(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.TYPE)));
                    object.setNumberIndex(cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.ID)));
                    int read = cursor.getInt(cursor
                            .getColumnIndex(TextSmsColumns.READ));
                    object.setRead(read);
                    myArraySMS.add(object);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return myArraySMS;
    }

    public String getContactName(Context context, String phoneNumber) {
        Cursor cursor = null;
        String contactName = phoneNumber;
        if (phoneNumber == null
                || (phoneNumber != null && phoneNumber.length() == 0)) {
            return "No name";
        }
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor
                        .getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception ex) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactName;
    }

    public boolean restoreSms(SmsCallLogItem object) {
        boolean ret = false;
        try {
            ContentValues values = new ContentValues();
            /*
             * if(object.getNumberIndex() != -1){ values.put("_id",
			 * object.getNumberIndex()); }
			 */
            values.put("address", object.getAddress());
            values.put("date", object.getTime());
            values.put("body", object.getBodySms());
            values.put("status", object.getState());
            values.put("type", object.getType());
            if (object.getType() == SmsCallLogTableAdapter.TYPE_SMS_INBOX) {
                values.put("read", 1);
            }
            String strType = "content://sms/inbox";
            switch (object.getType()) {
                case 0:
                    strType = "content://sms/all";
                    break;
                case 1:
                    strType = "content://sms/inbox";
                    break;
                case 2:
                    strType = "content://sms/sent";
                    break;
                case 3:
                    strType = "content://sms/draft";
                    break;
                case 4:
                    strType = "content://sms/outbox";
                    break;
                case 5:
                    strType = "content://sms/failed";
                    break;
                case 6:
                    strType = "content://sms/queued";
                    break;

                default:
                    break;
            }
            mContext.getContentResolver().insert(Uri.parse(strType), values);
            ret = true;
        } catch (Exception ex) {
            ret = false;
        }
        return ret;
    }

    public int deleteSMS(String address) {
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        return mContext.getContentResolver().delete(Uri.parse("content://sms"),
                "address IN (?,?)",
                new String[]{addr[0].toString(), addr[1].toString()});
    }

//	public void deleteSmsSingle(int id) {
//		mContext.getContentResolver().delete(Uri.parse("content://sms"),
//				"_id = ?", new String[] { id + "" });
//	}


    public boolean checkContact(String address) {
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        boolean isReturn = false;
        try {
            cursor = mContext.getContentResolver().query(
                    SMS_CONTENT_URI,
                    new String[]{TextSmsColumns.ID, TextSmsColumns.ADDRESS,
                            TextSmsColumns.DATE, TextSmsColumns.BODY,
                            TextSmsColumns.THREAD_ID, TextSmsColumns.PROTOCOL,
                            TextSmsColumns.PERSON, TextSmsColumns.STATUS,
                            TextSmsColumns.TYPE, TextSmsColumns.SUBJECT,
                            TextSmsColumns.READ},
                    TextSmsColumns.ADDRESS + " IN (?,?)",
                    new String[]{addr[0].toString(), addr[1].toString()},
                    TextSmsColumns.DATE + " DESC");
            if (cursor.moveToFirst()) {
                isReturn = true;
            } else {
                isReturn = getAllCallLogByAddress(address);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isReturn;
    }

    public boolean getAllCallLogByAddress(String address) {
        String[] addrs = new String[2];
        try {
            addrs = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addrs[0] = address;
            addrs[1] = address;
        }
        Cursor curLog = null;
        boolean isReturn = false;
        try {
            curLog = mContext.getContentResolver().query(callUri, null,
                    android.provider.CallLog.Calls.NUMBER + " IN (?,?)",
                    new String[]{addrs[0].toString(), addrs[1].toString()},
                    strOrder);
            if (curLog.moveToNext()) {
                isReturn = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (curLog != null) {
                curLog.close();
            }
        }
        return isReturn;
    }
}
