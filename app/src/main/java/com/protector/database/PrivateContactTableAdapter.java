package com.protector.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Pair;

import com.google.i18n.phonenumbers.NumberParseException;
import com.protector.AppPreference;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.EncryptUtils;
import com.protector.utils.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.Date;

public class PrivateContactTableAdapter extends BaseTableAdapter {
    public static final String TABLE_NAME = "private_contact_table";

    public static final int TYPE_PUBLIC = 2;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_BLACK = 3;

    public static final String COL_ID = "_id";
    public static final String COL_GROUP_ID = "groupid";
    public static final String COL_TYPE = "type";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_TIME = "time";
    public static final String COL_PASSWORD_ID = "passwrod_id";
    public static final String COL_CONTACT_INDEX = "contact_index";
    public static final String COL_PHONE_ID = "phone_id";
    public static final String COL_PHONE_TYPE = "phone_type";
    public static final String COL_PHONE_LABEL = "phone_label";
    public static final String COL_NUMBER_INDEX = "number_index";
    public static final String COL_ADDR_LOCATION = "addr_location";
    public static final String COL_EMAIL = "email";
    public static final String COL_AVATAR = "avatar";
    public static final String COL_AVATAR_BYTE = "avatar_byte";
    public static final String COL_BIRTHDAY = "birthday";
    public static final String COL_NOTES = "notes";
    public static final String COL_TYPE_TMP = "is_black";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + COL_ID + " integer primary key, "
            + COL_GROUP_ID + " integer, " + COL_TYPE + " integer not null, "
            + COL_NAME + " text, " + COL_ADDRESS + " text not null, "
            + COL_TIME + " text not null, " + COL_PASSWORD_ID
            + " integer not null, " + COL_AVATAR + " text, "
            + COL_CONTACT_INDEX + " integer, " + COL_PHONE_ID + " integer, "
            + COL_PHONE_TYPE + " integer, " + COL_ADDR_LOCATION + " text, "
            + COL_EMAIL + " text, " + COL_AVATAR_BYTE + " blod, "
            + COL_BIRTHDAY + " text, " + COL_NOTES + " text, " + COL_TYPE_TMP
            + " integer, " + COL_PHONE_LABEL + " text, " + COL_NUMBER_INDEX
            + " integer)";

    private static PrivateContactTableAdapter instance;

    private PrivateContactTableAdapter(Context context) {
        super(context);
    }

    public static PrivateContactTableAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new PrivateContactTableAdapter(context);
        }
        return instance;
    }

    @Override
    protected String getDatabaseName() {
        return SmsCallLogContentProvider.DATABASE_NAME;
    }

    public synchronized long addContact(int groupId, int type, String name,
                                        String address, int contactIndex, int phoneId, int phoneType,
                                        String phoneLabel, int numberIndex, String avatar, int passID) {
        if (!isDBFileExist()) {
            return -1;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(COL_GROUP_ID, groupId);
            values.put(COL_NAME, EncryptUtils.encryptV1(name));
            values.put(COL_ADDRESS, EncryptUtils.encryptV1(address));
            values.put(COL_TIME, new Date().getTime());
            values.put(COL_PASSWORD_ID, passID);
            values.put(COL_TYPE, type);
            values.put(COL_CONTACT_INDEX, contactIndex);
            values.put(COL_PHONE_ID, phoneId);
            values.put(COL_PHONE_TYPE, phoneType);
            values.put(COL_PHONE_LABEL, phoneLabel);
            values.put(COL_NUMBER_INDEX, numberIndex);
            values.put(COL_AVATAR, avatar);
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            Uri result = mContext.getContentResolver().insert(contentUri,
                    values);
            if (result == null) {
                return 0;
            }
            long id = Long.parseLong(result.getLastPathSegment());
            // if(groupId == -1){
            // Update groupID
            updateGroupIDContact(id);
            // }
            return id;
        } catch (Exception ex) {
            return 0;
        }

    }


    public synchronized long addContact(SmsCallLogItem smsCallLog, int type,
                                        int passID) {
        if (!isDBFileExist()) {
            return 0;
        }
        try {
            ContactItem ContactItem = getContactByAddress(
                    smsCallLog.getAddress(), passID);
            long idContact = -1;
            if (ContactItem != null) {
                idContact = ContactItem.getId();
                if (type == PrivateContactTableAdapter.TYPE_PRIVATE) {
                    updateTypeContact(idContact, type);
                }
            } else {
                ContactItem ContactItemAdd = getContactName(mContext,
                        smsCallLog.getAddress());
                ContentValues values = new ContentValues();
                values.put(COL_GROUP_ID, -1);
                values.put(COL_NAME,
                        EncryptUtils.encryptV1(ContactItemAdd.getName()));
                values.put(COL_ADDRESS,
                        EncryptUtils.encryptV1(smsCallLog.getAddress()));
                values.put(COL_TIME, "");
                values.put(COL_PASSWORD_ID, passID);
                values.put(COL_TYPE, type);
                values.put(COL_CONTACT_INDEX, ContactItemAdd.getId());
                values.put(COL_PHONE_ID, 0);
                values.put(COL_PHONE_TYPE, 0);
                values.put(COL_PHONE_LABEL, "");
                values.put(COL_NUMBER_INDEX, 0);
                values.put(COL_AVATAR, "");
                values.put(COL_AVATAR_BYTE, smsCallLog.getAvatarByte());
                Uri contentUri = Uri.withAppendedPath(
                        SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
                Uri result = mContext.getContentResolver().insert(contentUri,
                        values);
                if (result == null) {
                    return 0;
                }
                idContact = Long.parseLong(result.getLastPathSegment());
                // Update groupID
                updateGroupIDContact(idContact);
            }
            return idContact;
        } catch (Exception ex) {
            return 0;
        }

    }

    //
    // public synchronized long addContact(String addr, int type, int passID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return 0;
    // }
    // ContactItem ContactItem = getContactByAddress(addr, passID);
    // try {
    // long idContact = -1;
    // if (ContactItem != null) {
    // idContact = ContactItem.getId();
    // if (type == PrivateContactTableAdapter.TYPE_PRIVATE) {
    // updateTypeContact(idContact, type);
    // }
    // } else {
    // ContactItem ContactItemAdd = getContactName(mContext, addr);
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, -1);
    // values.put(COL_NAME,
    // EncryptUtils.encryptV1(ContactItemAdd.getName()));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(addr));
    // values.put(COL_TIME, "");
    // values.put(COL_PASSWORD_ID, passID);
    // values.put(COL_TYPE, type);
    // values.put(COL_CONTACT_INDEX, ContactItemAdd.getId());
    // values.put(COL_PHONE_ID, 0);
    // values.put(COL_PHONE_TYPE, 0);
    // values.put(COL_PHONE_LABEL, "");
    // values.put(COL_NUMBER_INDEX, 0);
    // values.put(COL_AVATAR, "");
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // if (result == null) {
    // return 0;
    // }
    // idContact = Long.parseLong(result.getLastPathSegment());
    // // Update groupID
    // updateGroupIDContact(idContact);
    // }
    // return idContact;
    // } catch (Exception ex) {
    // return 0;
    // }
    //
    // }
    //
    // public synchronized long addContact(String addr, String name, int type,
    // int passID) {
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
    // try {
    // ContactItem ContactItem = getContactByAddress(addr, passID);
    // long idContact = -1;
    // if (ContactItem != null) {
    // idContact = ContactItem.getId();
    // if (type == PrivateContactTableAdapter.TYPE_PRIVATE) {
    // updateTypeContact(idContact, type);
    // }
    // } else {
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, -1);
    // values.put(COL_NAME, EncryptUtils.encryptV1(name));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(addr));
    // values.put(COL_TIME, "");
    // values.put(COL_PASSWORD_ID, passID);
    // values.put(COL_TYPE, type);
    // values.put(COL_CONTACT_INDEX, 0);
    // values.put(COL_PHONE_ID, 0);
    // values.put(COL_PHONE_TYPE, 0);
    // values.put(COL_PHONE_LABEL, "");
    // values.put(COL_NUMBER_INDEX, 0);
    // values.put(COL_AVATAR, "");
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // if (result == null) {
    // return 0;
    // }
    // idContact = Long.parseLong(result.getLastPathSegment());
    // // Update groupID
    // updateGroupIDContact(idContact);
    // }
    // return idContact;
    // } catch (Exception ex) {
    // return 0;
    // }
    //
    // }
    //
    // public synchronized long addBlackContact(String name, String addr,
    // String addrLocation, String email, byte[] avatar, int type,
    // int passID, String birthday, String notes) {
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
    // long idContact = -1;
    // boolean ContactItem = checkBlackContactByAddress(addr,
    // PrivateContactTableAdapter.TYPE_BLACK,
    // PasswordTableAdapter.PASSWORD_CURRENT_ID);
    // try {
    // if (ContactItem && addr != null && addr.length() > 0) {
    //
    // } else {
    //
    // ContentValues values = new ContentValues();
    // values.put(COL_GROUP_ID, -1);
    // values.put(COL_NAME, EncryptUtils.encryptV1(name));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(addr));
    // values.put(COL_TIME, "");
    // values.put(COL_PASSWORD_ID, passID);
    // values.put(COL_TYPE, type);
    // values.put(COL_CONTACT_INDEX, 0);
    // values.put(COL_PHONE_ID, 0);
    // values.put(COL_PHONE_TYPE, 0);
    // values.put(COL_PHONE_LABEL, "");
    // values.put(COL_NUMBER_INDEX, 0);
    // values.put(COL_AVATAR, "");
    // values.put(COL_ADDR_LOCATION, addrLocation);
    // values.put(COL_EMAIL, email);
    // values.put(COL_AVATAR_BYTE, avatar);
    // values.put(COL_BIRTHDAY, birthday);
    // values.put(COL_NOTES, notes);
    // values.put(COL_TYPE_TMP, TYPE_BLACK);
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // Uri result = mContext.getContentResolver().insert(contentUri,
    // values);
    // if (result == null) {
    // return 0;
    // }
    // idContact = Long.parseLong(result.getLastPathSegment());
    // // Update groupID
    // updateGroupIDContact(idContact);
    // // }
    // }
    // return idContact;
    // } catch (Exception ex) {
    // return 0;
    // }
    // }
    //
    public synchronized long addContact(ContactItem contact, int type,
                                        int passID) {
        if (!isDBFileExist()) {
            return -1;
        }
        try {
            ContactItem ContactItem = getContactByAddress(
                    contact.getAddress(), passID);
            long idContact = -1;
            if (ContactItem != null && contact.getAddress() != null
                    && contact.getAddress().length() > 0) {
                idContact = ContactItem.getId();
                if (type == PrivateContactTableAdapter.TYPE_PRIVATE) {
                    updateTypeContact(idContact, type);
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(COL_GROUP_ID, -1);
                values.put(COL_NAME, EncryptUtils.encryptV1(contact.getName()));
                values.put(COL_ADDRESS,
                        EncryptUtils.encryptV1(contact.getAddress()));
                values.put(COL_TIME, "");
                values.put(COL_PASSWORD_ID, passID);
                values.put(COL_TYPE, type);
                values.put(COL_CONTACT_INDEX, contact.getId());
                values.put(COL_PHONE_ID, 0);
                values.put(COL_PHONE_TYPE, 0);
                values.put(COL_PHONE_LABEL, "");
                values.put(COL_NUMBER_INDEX, 0);
                values.put(COL_AVATAR, "");
                values.put(COL_AVATAR_BYTE, contact.getAvatarByte());
                Uri contentUri = Uri.withAppendedPath(
                        SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
                Uri result = mContext.getContentResolver().insert(contentUri,
                        values);
                if (result == null) {
                    return 0;
                }
                idContact = Long.parseLong(result.getLastPathSegment());
                // Update groupID
                updateGroupIDContact(idContact);
            }
            return idContact;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

    }


    public synchronized ContactItem getContactName(Context context,
                                                   String phoneNumber) {
        if (!isDBFileExist()) {
            return new ContactItem();
        }
        ContactItem contact = new ContactItem();
        Cursor cursor = null;
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
                contact.setId(-1);
            }
        } catch (Exception ex) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    public synchronized int updateGroupIDContact(long id) {
        if (!isDBFileExist()) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COL_GROUP_ID, id);
        Uri contentUri = Uri.withAppendedPath(
                SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
        return mContext.getContentResolver().update(contentUri, values,
                COL_ID + " = ?", new String[]{id + ""});
    }

    public synchronized int removeByID(long id) {
        if (!isDBFileExist()) {
            return -1;
        }
        Uri contentUri = Uri.withAppendedPath(
                SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
        return mContext.getContentResolver().delete(contentUri,
                COL_ID + "=" + id, null);
    }

    public synchronized ArrayList<ContactItem> getAllContact(int passID) {

        ArrayList<ContactItem> items = new ArrayList<>();
        if (!isDBFileExist()) {
            return items;
        }
        Cursor cursor = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
                            COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
                            COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
                            COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR},
                    COL_PASSWORD_ID + " =?", new String[]{passID + ""},
                    COL_TIME + " DESC");

            while (cursor != null && cursor.moveToNext()) {
                ContactItem item = new ContactItem();
                item.setId(cursor.getInt(0));
                item.setGroupId(cursor.getInt(1));
                item.setType(cursor.getInt(2));
                item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
                item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
                item.setTime(cursor.getLong(5));
                item.setPasswordText(cursor.getString(6));
                item.setContactIndex(cursor.getInt(7));
                item.setPhoneId(cursor.getInt(8));
                item.setPhoneType(cursor.getInt(9));
                item.setPhoneLabel(cursor.getString(10));
                item.setNumberIndex(cursor.getInt(11));
                item.setAvatar(cursor.getString(12));
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

    // public synchronized ArrayList<ContactItem> getAllPrivateContact(int
    // passID) {
    // ArrayList<ContactItem> items = new ArrayList<ContactItem>();
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
    // COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
    // COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
    // COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR,
    // COL_AVATAR_BYTE },
    // COL_TYPE + "=? AND " + COL_PASSWORD_ID + "=?",
    // new String[] { "1", passID + "" }, COL_TIME + " DESC");
    //
    // while (cursor != null && cursor.moveToNext()) {
    // ContactItem item = new ContactItem();
    // item.setId(cursor.getInt(0));
    // item.setGroupId(cursor.getInt(1));
    // item.setType(cursor.getInt(2));
    // item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
    // item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
    // item.setTime(cursor.getLong(5));
    // item.setPasswordText(cursor.getString(6));
    // item.setContactIndex(cursor.getInt(7));
    // item.setPhoneId(cursor.getInt(8));
    // item.setPhoneType(cursor.getInt(9));
    // item.setPhoneLabel(cursor.getString(10));
    // item.setNumberIndex(cursor.getInt(11));
    // item.setAvatar(cursor.getString(12));
    // item.setAvatarByte(cursor.getBlob(13));
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
    // public synchronized ArrayList<ContactItem> getAllBlackContact(int
    // passID) {
    // ArrayList<ContactItem> items = new ArrayList<ContactItem>();
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
    // COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
    // COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
    // COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR,
    // COL_ADDR_LOCATION, COL_EMAIL, COL_AVATAR_BYTE,
    // COL_BIRTHDAY, COL_NOTES },
    // COL_TYPE + "=? AND " + COL_PASSWORD_ID + "=?",
    // new String[] { TYPE_BLACK + "", passID + "" },
    // COL_TIME + " DESC");
    //
    // while (cursor != null && cursor.moveToNext()) {
    // ContactItem item = new ContactItem();
    // item.setId(cursor.getInt(0));
    // item.setGroupId(cursor.getInt(1));
    // item.setType(cursor.getInt(2));
    // item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
    // item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
    // item.setTime(cursor.getLong(5));
    // item.setPasswordText(cursor.getString(6));
    // item.setContactIndex(cursor.getInt(7));
    // item.setPhoneId(cursor.getInt(8));
    // item.setPhoneType(cursor.getInt(9));
    // item.setPhoneLabel(cursor.getString(10));
    // item.setNumberIndex(cursor.getInt(11));
    // item.setAvatar(cursor.getString(12));
    // item.setAddressLocation(cursor.getString(13));
    // item.setEmail(cursor.getString(14));
    // item.setAvatarByte(cursor.getBlob(15));
    // item.setBirthday(cursor.getString(16));
    // item.setNotes(cursor.getString(17));
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
    // public synchronized ContactItem getBlackContact(int passID, int id) {
    // ContactItem item = new ContactItem();
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return item;
    // }
    // Cursor cursor = null;
    // try {
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // cursor = mContext.getContentResolver().query(
    // contentUri,
    // new String[] { COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
    // COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
    // COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
    // COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR,
    // COL_ADDR_LOCATION, COL_EMAIL, COL_AVATAR_BYTE,
    // COL_BIRTHDAY, COL_NOTES },
    // COL_TYPE + "=? AND " + COL_PASSWORD_ID + "=? AND " + COL_ID
    // + "=?",
    // new String[] { TYPE_BLACK + "", passID + "", id + "" },
    // COL_TIME + " DESC");
    //
    // if (cursor != null && cursor.moveToNext()) {
    // item.setId(cursor.getInt(0));
    // item.setGroupId(cursor.getInt(1));
    // item.setType(cursor.getInt(2));
    // item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
    // item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
    // item.setTime(cursor.getLong(5));
    // item.setPasswordText(cursor.getString(6));
    // item.setContactIndex(cursor.getInt(7));
    // item.setPhoneId(cursor.getInt(8));
    // item.setPhoneType(cursor.getInt(9));
    // item.setPhoneLabel(cursor.getString(10));
    // item.setNumberIndex(cursor.getInt(11));
    // item.setAvatar(cursor.getString(12));
    // item.setAddressLocation(cursor.getString(13));
    // item.setEmail(cursor.getString(14));
    // item.setAvatarByte(cursor.getBlob(15));
    // item.setBirthday(cursor.getString(16));
    // item.setNotes(cursor.getString(17));
    // }
    // } catch (Exception e) {
    //
    // } finally {
    // if (cursor != null)
    // cursor.close();
    // }
    // return item;
    // }
    //
    public synchronized ArrayList<Pair<ContactItem, SmsCallLogItem>> getAllContactWithPair(
            int passID) {
        ArrayList<Pair<ContactItem, SmsCallLogItem>> myArray = new ArrayList<Pair<ContactItem, SmsCallLogItem>>();
        if (!isDBFileExist()) {
            return myArray;
        }
        Cursor cursor = null;
        try {
            SmsCallLogTableAdapter smsCallLogAdapter = SmsCallLogTableAdapter
                    .getInstance(mContext);

            ArrayList<ContactItem> items = new ArrayList<ContactItem>();
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
                            COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
                            COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
                            COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR,
                            COL_TYPE_TMP, COL_AVATAR_BYTE},
                    COL_PASSWORD_ID + " = ? ", new String[]{passID + ""},
                    COL_TIME + " DESC");

            while (cursor != null && cursor.moveToNext()) {
                ContactItem item = new ContactItem();
                item.setId(cursor.getInt(0));
                item.setGroupId(cursor.getInt(1));
                item.setType(cursor.getInt(2));
                item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
                item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
                item.setTime(cursor.getLong(5));
                item.setPasswordText(cursor.getString(6));
                item.setContactIndex(cursor.getInt(7));
                item.setPhoneId(cursor.getInt(8));
                item.setPhoneType(cursor.getInt(9));
                item.setPhoneLabel(cursor.getString(10));
                item.setNumberIndex(cursor.getInt(11));
                item.setAvatar(cursor.getString(12));
                item.setTypeTmpBlack(cursor.getInt(13));
                item.setAvatarByte(cursor.getBlob(14));
                item.setNewSMS(smsCallLogAdapter.getSMSCallNotRead(item.getId()));
                items.add(item);
                SmsCallLogItem smsLast = smsCallLogAdapter
                        .getSMSCallLogLast(item.getId());
                if (item.getType() == PrivateContactTableAdapter.TYPE_PUBLIC
                        && smsLast == null) {
                    removeByID(item.getId());
                } else {
                    if (smsLast == null
                            && item.getType() == PrivateContactTableAdapter.TYPE_BLACK) {
                    } else {
                        myArray.add(new Pair<ContactItem, SmsCallLogItem>(item,
                                smsLast));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return myArray;
    }

    public synchronized int updateTypeContact(long id, int type) {
        if (!isDBFileExist()) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, type);
        Uri contentUri = Uri.withAppendedPath(
                SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
        return mContext.getContentResolver().update(contentUri, values,
                COL_ID + " = ?", new String[]{id + ""});
    }
    //
    // public synchronized int updateContact(long id, String name) {
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
    // ContentValues values = new ContentValues();
    // values.put(COL_NAME, EncryptUtils.encryptV1(name.trim()));
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().update(contentUri, values,
    // COL_ID + " = ?", new String[] { id + "" });
    // }
    //
    // public synchronized int updateBlackContact(long id, String name,
    // String address, String addressLocation, String email,
    // byte[] avatar, String birthday, String notes) {
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
    // boolean ContactItem = checkContactByAddress(address,
    // PrivateContactTableAdapter.TYPE_BLACK,
    // PasswordTableAdapter.PASSWORD_CURRENT_ID, id);
    // if (ContactItem) {
    // return -2;
    // } else {
    // ContentValues values = new ContentValues();
    // values.put(COL_NAME, EncryptUtils.encryptV1(name.trim()));
    // values.put(COL_ADDRESS, EncryptUtils.encryptV1(address.trim()));
    // values.put(COL_ADDR_LOCATION, addressLocation);
    // values.put(COL_EMAIL, email);
    // values.put(COL_AVATAR_BYTE, avatar);
    // values.put(COL_BIRTHDAY, birthday);
    // values.put(COL_NOTES, notes);
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().update(contentUri, values,
    // COL_ID + " = ?", new String[] { id + "" });
    // }
    // }
    //
    // public synchronized int updateAvatarBlackContact(long id, byte[] avatar)
    // {
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
    // ContentValues values = new ContentValues();
    // values.put(COL_AVATAR_BYTE, avatar);
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // return mContext.getContentResolver().update(contentUri, values,
    // COL_ID + " = ?", new String[] { id + "" });
    // }

    public synchronized boolean checkContactByAddress(String address, int
            passID) {
        if (!isDBFileExist()) {
            return false;
        }
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        Cursor cursor = null;
        boolean isCheck = false;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    null,
                    COL_ADDRESS + " IN (?,?) AND " + COL_PASSWORD_ID + " = ?",
                    new String[]{EncryptUtils.encryptV1(addr[0].toString()),
                            EncryptUtils.encryptV1(addr[1].toString()),
                            passID + ""}, null);
            if (cursor != null && cursor.moveToNext()) {
                isCheck = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isCheck;
    }


    public synchronized boolean checkContactByAddress(String address, int
            type, int passID) {
        if (!isDBFileExist()) {
            return false;
        }
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        Cursor cursor = null;
        boolean isCheck = false;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    null,
                    COL_ADDRESS + " IN (?,?) AND " + COL_TYPE + " = ? AND "
                            + COL_PASSWORD_ID + " = ?",
                    new String[]{EncryptUtils.encryptV1(addr[0].toString()),
                            EncryptUtils.encryptV1(addr[1].toString()),
                            type + "", passID + ""}, null);
            if (cursor != null && cursor.moveToNext()) {
                isCheck = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isCheck;
    }

    // public synchronized boolean checkBlackContactByAddress(String address,
    // int type, int passID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return false;
    // }
    // String[] addr = new String[3];
    // String[] addrTmp = new String[2];
    // try {
    //
    // addrTmp = Utilities.getPhoneNumber(mContext, address);
    // addr[0] = addrTmp[0];
    // addr[1] = addrTmp[1];
    // } catch (Exception ex) {
    // addr[0] = address;
    // addr[1] = address;
    // }
    // addr[2] = address;
    // Cursor cursor = null;
    // boolean isCheck = false;
    // try {
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // cursor = mContext.getContentResolver().query(
    // contentUri,
    // null,
    // COL_ADDRESS + " IN (?,?,?) AND " + COL_TYPE + " = ? AND "
    // + COL_PASSWORD_ID + " = ?",
    // new String[] { EncryptUtils.encryptV1(addr[0].toString()),
    // EncryptUtils.encryptV1(addr[1].toString()),
    // EncryptUtils.encryptV1(addr[2].toString()),
    // type + "", passID + "" }, null);
    // if (cursor != null && cursor.moveToNext()) {
    // isCheck = true;
    // }
    // } catch (Exception ex) {
    //
    // } finally {
    // if (cursor != null) {
    // cursor.close();
    // }
    // }
    // return isCheck;
    // }
    //
    // public synchronized boolean checkContactByAddress(String address, int
    // type,
    // int passID, long selfID) {
    // String folder = Preferences.getInstance(mContext).getHideRootPath();
    // String DB_PATH = folder + "/"
    // + SmsCallLogContentProviderDB.DATABASE_NAME;
    // File f = new File(DB_PATH);
    //
    // if (f.exists()
    // || Environment.MEDIA_MOUNTED.equals(Environment
    // .getExternalStorageState())) {
    // } else {
    // return false;
    // }
    // String[] addr = new String[2];
    // try {
    // addr = Utilities.getPhoneNumber(mContext, address);
    // } catch (Exception ex) {
    // addr[0] = address;
    // addr[1] = address;
    // }
    // Cursor cursor = null;
    // boolean isCheck = false;
    // try {
    // Uri contentUri = Uri.withAppendedPath(
    // SmsCallLogContentProviderDB.CONTENT_URI, TABLE_NAME);
    // cursor = mContext.getContentResolver().query(
    // contentUri,
    // null,
    // COL_ADDRESS + " IN (?,?) AND " + COL_TYPE + " = ? AND "
    // + COL_PASSWORD_ID + " = ? AND " + COL_ID + " <> ?",
    // new String[] { EncryptUtils.encryptV1(addr[0].toString()),
    // EncryptUtils.encryptV1(addr[1].toString()),
    // type + "", passID + "", selfID + "" }, null);
    // if (cursor != null && cursor.moveToNext()) {
    // isCheck = true;
    // }
    // } catch (Exception ex) {
    //
    // } finally {
    // if (cursor != null) {
    // cursor.close();
    // }
    // }
    // return isCheck;
    // }
    //
    public synchronized long checkContactByAddresss(String address, int type,
                                                    int passID) {
        if (!isDBFileExist()) {
            return -1;
        }
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (Exception ex) {
            addr[0] = address;
            addr[1] = address;
        }
        Cursor cursor = null;
        int count = -1;
        Cursor cursorBlack = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    null,
                    COL_ADDRESS + " IN (?,?) AND " + COL_TYPE + " = ? AND "
                            + COL_PASSWORD_ID + " =?",
                    new String[]{EncryptUtils.encryptV1(addr[0].toString()),
                            EncryptUtils.encryptV1(addr[1].toString()),
                            type + "", passID + ""}, null);
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getInt(cursor.getColumnIndex(COL_ID));
            } else {
                if (type == TYPE_PRIVATE) {
                    if (AppPreference.getInstance(mContext).getCatchContact() == 0) {
                        Uri contentUriBlack = Uri.withAppendedPath(
                                SmsCallLogContentProvider.CONTENT_URI,
                                TABLE_NAME);
                        cursorBlack = mContext.getContentResolver()
                                .query(contentUriBlack,
                                        null,
                                        COL_ADDRESS + " IN (?,?) AND "
                                                + COL_TYPE + " = ? AND "
                                                + COL_PASSWORD_ID + " =?",
                                        new String[]{
                                                EncryptUtils.encryptV1(addr[0]
                                                        .toString()),
                                                EncryptUtils.encryptV1(addr[1]
                                                        .toString()),
                                                TYPE_BLACK + "", passID + ""},
                                        null);
                        if (cursorBlack != null && cursorBlack.moveToNext()) {
                            count = cursorBlack.getInt(cursor
                                    .getColumnIndex(COL_ID));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (cursorBlack != null) {
                cursorBlack.close();
            }
        }
        return count;
    }

    public synchronized ContactItem getContactByAddress(String address,
                                                        int passID) {
        if (!isDBFileExist()) {
            return null;
        }
        String[] addr = new String[2];
        try {
            addr = PhoneNumberUtils.getPhoneNumber(mContext, address);
        } catch (NumberParseException ex) {
            ex.printStackTrace();
            addr[0] = address;
            addr[1] = address;
        }
        Cursor cursor = null;
        ContactItem item = null;
        try {
            Uri contentUri = Uri.withAppendedPath(
                    SmsCallLogContentProvider.CONTENT_URI, TABLE_NAME);
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    new String[]{COL_ID, COL_GROUP_ID, COL_TYPE, COL_NAME,
                            COL_ADDRESS, COL_TIME, COL_PASSWORD_ID,
                            COL_CONTACT_INDEX, COL_PHONE_ID, COL_PHONE_TYPE,
                            COL_PHONE_LABEL, COL_NUMBER_INDEX, COL_AVATAR},
                    COL_ADDRESS + " IN (?,?) AND " + COL_PASSWORD_ID + " = ?",
                    new String[]{EncryptUtils.encryptV1(addr[0].toString()),
                            EncryptUtils.encryptV1(addr[1].toString()),
                            passID + ""}, COL_TIME + " DESC");

            if (cursor != null && cursor.moveToNext()) {
                item = new ContactItem();
                item.setId(cursor.getInt(0));
                item.setGroupId(cursor.getInt(1));
                item.setType(cursor.getInt(2));
                item.setName(EncryptUtils.decryptV1(cursor.getString(3)));
                item.setAddress(EncryptUtils.decryptV1(cursor.getString(4)));
                item.setTime(cursor.getLong(5));
                item.setPasswordText(cursor.getString(6));
                item.setContactIndex(cursor.getInt(7));
                item.setPhoneId(cursor.getInt(8));
                item.setPhoneType(cursor.getInt(9));
                item.setPhoneLabel(cursor.getString(10));
                item.setNumberIndex(cursor.getInt(11));
                item.setAvatar(cursor.getString(12));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return item;
    }
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

}
