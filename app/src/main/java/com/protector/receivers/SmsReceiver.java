package com.protector.receivers;

import java.util.ArrayList;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;

import com.protector.Configs;
import com.protector.R;
import com.protector.activities.DetailContactLockedActivity;
import com.protector.activities.PasswordActivity;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.PhoneNumberUtils;
import com.protector.utils.SmsLocker;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SMSReceiver";
	private static final String smsuri = "android.provider.Telephony.SMS_RECEIVED";
	private static final String smssend = "android.provider.Telephony.SMS_DELIVER";
	private static final String mmsuri = "android.provider.Telephony.WAP_PUSH_RECEIVED";
	public static final String KEY_ACTION = "Action";
	public static final String UPDATE = "Update";
	public static final String ACTION_UPDATE = "com.spamsmsfilter.update";
	private SmsCallLogTableAdapter mTable;
	private Context myContext;

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		myContext = arg0;
		mTable = SmsCallLogTableAdapter.getInstance(arg0);
		Bundle bundle = intent.getExtras();
		if (intent.getAction().equals(smsuri)
				|| intent.getAction().equals(smssend)) {
			if (null != bundle) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] smg = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					smg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}

				for (SmsMessage cursmg : smg) {
					
					String myNumber = cursmg.getOriginatingAddress();// cursmg.getDisplayOriginatingAddress();
					PrivateContactTableAdapter contactTable = PrivateContactTableAdapter
							.getInstance(myContext);
					SmsCallLogTableAdapter smsTable = SmsCallLogTableAdapter
							.getInstance(myContext);
					long idGroup = contactTable.checkContactByAddresss(
							myNumber, PrivateContactTableAdapter.TYPE_PRIVATE,
							PasswordTableAdapter.PASSWORD_1);
					SmsLocker smsThread = SmsLocker.getInstance(myContext);
					int count = smsTable
							.getAllSMS(PasswordTableAdapter.PASSWORD_1);
					if (idGroup != -1) {
						String[] adr = new String[2];
						try {
							adr = PhoneNumberUtils.getPhoneNumber(myContext, myNumber);
						} catch (Exception ex) {
							adr[0] = myNumber;
							adr[1] = myNumber;
						}
						ArrayList<SmsCallLogItem> myArr = smsThread
								.getAllSMSByAddress(adr);
						String msg = cursmg.getMessageBody();
						int id = cursmg.getIndexOnSim();
						int state = cursmg.getStatus();
                        SmsCallLogItem object = new SmsCallLogItem();
						if (intent.getAction().equals(smsuri)
								|| intent.getAction().equals(mmsuri)) {
							ContactItem contact = getContactName(myContext,
									myNumber);
							object.setTime(new Date().getTime());
							object.setBodySms(msg);
							object.setAddress(myNumber);
							object.setName(contact.getName());
							object.setState(state);
							object.setGroupId((int) idGroup);
							object.setType(1);
							object.setTypeCompare(1);
							object.setNumberIndex(id);
							object.setRead(0);
							if (myArr.size() > 0) {
								if (!myArr.get(0).getBodySms()
										.equals(object.getBodySms())) {
									myArr.add(object);
								}
							} else {
								myArr.add(object);
							}
						} else {
                            ContactItem contact = getContactName(myContext,
									myNumber);
							object.setTime(new Date().getTime());
							object.setBodySms(msg);
							object.setAddress(myNumber);
							object.setName(contact.getName());
							object.setState(state);
							object.setGroupId((int) idGroup);
							object.setType(2);
							object.setNumberIndex(id);
							object.setTypeCompare(1);
							object.setRead(0);
							if (myArr.size() > 0) {
								if (!myArr.get(0).getBodySms()
										.equals(object.getBodySms())) {
									myArr.add(object);
								}
							} else {
								myArr.add(object);
							}
						}

						if (myArr.size() > 0) {
							smsTable.addArraySms(myArr, idGroup);
							String[] addr = new String[2];
							try {
								addr = PhoneNumberUtils.getPhoneNumber(myContext,
										myNumber);
							} catch (Exception ex) {
								addr[0] = myNumber;
								addr[1] = myNumber;
							}
							myContext.getContentResolver().delete(
									Uri.parse("content://sms"),
									"address IN (?,?)",
									new String[] { addr[0].toString(),
											addr[1].toString() });
							Intent i = new Intent(
									DetailContactLockedActivity.ACTION_SMS_CALL_LOG);
							i.putExtra("ADDRESS", myNumber);
							myContext.sendBroadcast(i);
						}
						showNotification(arg0, myNumber);
						abortBroadcast();
					} else {

					}

				}
			}
		} else {

		}
	}



	@SuppressWarnings("deprecation")
	private static void showNotification(Context context, String address) {
		PrivateContactTableAdapter table = PrivateContactTableAdapter
				.getInstance(context);
        ContactItem contact = table.getContactByAddress(address,
				PasswordTableAdapter.PASSWORD_1);
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, "", when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, PasswordActivity.class);
		notificationIntent.putExtra("CONTACT", contact);
		notificationIntent.putExtra("SMS_RECEIVER", true);
		// notificationIntent.putExtra("ms", message);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(context, title, "", intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.defaults |= Notification.DEFAULT_SOUND;

		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
	}

	public ContactItem getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
        ContactItem contact = new ContactItem();
		Cursor cursor = null;
		if (phoneNumber == null
				|| (phoneNumber != null && phoneNumber.length() == 0)) {
			return contact;
		}
		try {
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(phoneNumber));
			cursor = cr.query(uri, new String[] { PhoneLookup._ID,
					PhoneLookup.DISPLAY_NAME }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				contact.setName(cursor.getString(cursor
						.getColumnIndex(PhoneLookup.DISPLAY_NAME)));
				contact.setId(cursor.getInt(cursor
						.getColumnIndex(PhoneLookup._ID)));
			} else {
				contact.setName(phoneNumber);
			}
		} catch (Exception ex) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return contact;
	}
}
