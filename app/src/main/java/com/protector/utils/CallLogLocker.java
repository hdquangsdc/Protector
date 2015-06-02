package com.protector.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.protector.objects.SmsCallLogItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Ho Duy Quang
 */
public class CallLogLocker {
    private Context mContext;
    private static CallLogLocker instance;
    private String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
    private ArrayList<SmsCallLogItem> myArrayObject;
    private Uri callUri = Uri.parse("content://call_log/calls");

    private CallLogLocker(Context context) {
        mContext = context;
    }

    public static CallLogLocker getInstance(Context context) {
        if (instance == null) {
            instance = new CallLogLocker(context);
        }
        return instance;
    }

    public ArrayList<SmsCallLogItem> getAllCallLog() {
        myArrayObject = new ArrayList<>();
        Cursor curLog = null;
        try {
            curLog = mContext.getContentResolver().query(callUri, null, null,
                    null, strOrder);
            HashMap<String, byte[]> hmAvatar = new HashMap<>();
            while (curLog.moveToNext()) {
                SmsCallLogItem object = new SmsCallLogItem();
                String callNumber = curLog.getString(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                object.setAddress(callNumber);
                String callName = curLog
                        .getString(curLog
                                .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
                if (callName == null) {
                    object.setName(callNumber);
                } else
                    object.setName(callName);
                try {
                    byte[] avatar = hmAvatar.get(callNumber);
                    if (avatar != null) {
                        if (avatar.length == 0) {
                            object.setAvatarByte(null);
                        } else {
                            object.setAvatarByte(avatar);
                        }
                    } else {
                        byte[] avatarGet = getPhotoContact(mContext, callNumber);
                        object.setAvatarByte(avatarGet);
                        if (avatarGet == null) {
                            hmAvatar.put(callNumber, new byte[0]);
                        } else {
                            hmAvatar.put(callNumber, avatarGet);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                object.setTime(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DATE)));
                object.setType(curLog.getInt(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.TYPE)));
                object.setState(0);
                try {
                    object.setRead(curLog.getInt(curLog
                            .getColumnIndex(android.provider.CallLog.Calls.IS_READ)));
                } catch (Exception ex) {
                    object.setRead(1);
                }
                object.setGroupId(-1);
                object.setDurationCallLog(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DURATION)));
                object.setNumberIndex(curLog.getInt(curLog
                        .getColumnIndex(CallLog.Calls._ID)));
                myArrayObject.add(object);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (curLog != null) {
                curLog.close();
            }
        }
        return myArrayObject;
    }

    public ArrayList<SmsCallLogItem> getAllCallLogByAddress(String address) {
        String[] addrs = new String[2];
        try {
            addrs = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addrs[0] = address;
            addrs[1] = address;
        }
        Cursor curLog = null;
        myArrayObject = new ArrayList<>();
        try {
            curLog = mContext.getContentResolver().query(callUri, null,
                    android.provider.CallLog.Calls.NUMBER + " IN (?,?)",
                    new String[]{addrs[0], addrs[1]},
                    strOrder);
            while (curLog.moveToNext()) {
                SmsCallLogItem object = new SmsCallLogItem();
                String callNumber = curLog.getString(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                object.setAddress(callNumber);
                String callName = curLog
                        .getString(curLog
                                .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
                if (callName == null) {
                    object.setName(callNumber);
                } else
                    object.setName(callName);
                object.setTime(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DATE)));
                object.setType(curLog.getInt(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.TYPE)));
                object.setState(0);
                try {
                    object.setRead(curLog.getInt(curLog
                            .getColumnIndex(android.provider.CallLog.Calls.IS_READ)));
                } catch (Exception ex) {
                    object.setRead(0);
                }
                object.setGroupId(-1);
                object.setDurationCallLog(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DURATION)));
                object.setNumberIndex(curLog.getInt(curLog
                        .getColumnIndex(CallLog.Calls._ID)));
                myArrayObject.add(object);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (curLog != null) {
                curLog.close();
            }
        }
        return myArrayObject;
    }

    public ArrayList<SmsCallLogItem> getAllCallLogByAddressContain0(
            String address) {
        String[] addrs = new String[2];
        try {
            addrs = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addrs[0] = address;
            addrs[1] = address;
        }
        if (address.indexOf("0") == 0) {
            if (addrs.length > 0 && addrs[0].indexOf("0") != 0) {
                addrs[0] = "0" + addrs[0];
            }
        }
        Cursor curLog = null;
        myArrayObject = new ArrayList<>();
        try {
            curLog = mContext.getContentResolver().query(callUri, null,
                    android.provider.CallLog.Calls.NUMBER + " IN (?,?)",
                    new String[]{addrs[0], addrs[1]},
                    strOrder);
            while (curLog.moveToNext()) {
                SmsCallLogItem object = new SmsCallLogItem();
                String callNumber = curLog.getString(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                object.setAddress(callNumber);
                String callName = curLog
                        .getString(curLog
                                .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
                if (callName == null) {
                    object.setName(callNumber);
                } else
                    object.setName(callName);
                object.setTime(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DATE)));
                object.setType(curLog.getInt(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.TYPE)));
                object.setState(0);
                try {
                    object.setRead(curLog.getInt(curLog
                            .getColumnIndex(android.provider.CallLog.Calls.IS_READ)));
                } catch (Exception ex) {
                    object.setRead(0);
                }
                object.setGroupId(-1);
                object.setDurationCallLog(curLog.getLong(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.DURATION)));
                object.setNumberIndex(curLog.getInt(curLog
                        .getColumnIndex(CallLog.Calls._ID)));
                myArrayObject.add(object);
            }
        } catch (Exception x) {
            x.printStackTrace();
        } finally {
            if (curLog != null) {
                curLog.close();
            }
        }
        return myArrayObject;
    }

    public void deleteCallLog(String address) {
        String[] addrs = new String[2];
        try {
            addrs = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addrs[0] = address;
            addrs[1] = address;
        }

        mContext.getContentResolver().delete(
                android.provider.CallLog.Calls.CONTENT_URI,
                android.provider.CallLog.Calls.NUMBER + " IN (?,?)",
                new String[]{addrs[0], addrs[1]});
    }

    public void deleteCallLogContain0(String address) {
        String[] addrs = new String[2];
        try {
            addrs = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addrs[0] = address;
            addrs[1] = address;
        }
        if (address.indexOf("0") == 0) {
            if (addrs.length > 0 && addrs[0].indexOf("0") != 0) {
                addrs[0] = "0" + addrs[0];
            }
        }
        mContext.getContentResolver().delete(
                android.provider.CallLog.Calls.CONTENT_URI,
                android.provider.CallLog.Calls.NUMBER + " IN (?,?)",
                new String[]{addrs[0], addrs[1]});
    }

    public byte[] getPhotoContact(Context context, String phoneNumber) {
        if (phoneNumber == null)
            return null;
        ContentResolver cr = context.getContentResolver();
        byte[] byteImg = null;
        Cursor cursor = null;
        try {
            String[] projection = new String[]{ContactsContract.PhoneLookup.PHOTO_ID};
            Uri contactUri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            cursor = cr.query(contactUri, projection, null, null, null);
            if (cursor.moveToFirst()) {
                int photoId = cursor.getInt(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));

                Uri photoUri = ContentUris.withAppendedId(
                        ContactsContract.Data.CONTENT_URI, photoId);
                Cursor c = mContext
                        .getContentResolver()
                        .query(photoUri,
                                new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                                null, null, null);
                try {
                    if (c.moveToFirst()) {
                        byteImg = c.getBlob(0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    c.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return byteImg;
    }
}

