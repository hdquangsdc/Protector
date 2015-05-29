package com.protector.receivers;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.protector.R;
import com.protector.activities.DetailContactLockedActivity;
import com.protector.activities.PasswordActivity;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.CallLogLocker;


public class PhoneStateBroadcastReceiver extends BroadcastReceiver {
	private Context myContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		myContext = context;
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
	}

	private class CustomPhoneStateListener extends PhoneStateListener {

		Context context;

		public CustomPhoneStateListener(Context context) {
			super();
			this.context = context;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//End
				 /*myContext.getContentResolver().registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true,
				            new MyContentObserver(new Handler(), incomingNumber));*/
				 break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				/*myContext.getContentResolver().registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true,
			            new MyContentObserver(new Handler(), incomingNumber));*/
				//Call
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				myContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true,
			            new MyContentObserver(new Handler(), incomingNumber));
				//Reciver
				break;

			default:
				break;
			}
		}
	}
	
	class MyContentObserver extends ContentObserver {
		private String myNumber = "";
	    public MyContentObserver(Handler handler, String number) {
			super(handler);
			this.myNumber = number;
		}
	    public MyContentObserver(Handler handler, Context context) {
			super(handler);
		}
		@Override
	    public boolean deliverSelfNotifications() {
	        return true;
	    }

	    @Override
	    public void onChange(boolean selfChange) {
	    	PrivateContactTableAdapter contactTable = PrivateContactTableAdapter
					.getInstance(myContext);
			SmsCallLogTableAdapter smsTable = SmsCallLogTableAdapter
					.getInstance(myContext);
			long idGroup = contactTable.checkContactByAddresss(myNumber,
					PrivateContactTableAdapter.TYPE_PRIVATE, PasswordTableAdapter.PASSWORD_1);
			CallLogLocker callThread = CallLogLocker.getInstance(myContext);
			int count = smsTable.getAllSMS(PasswordTableAdapter.PASSWORD_1); 
			if (idGroup != -1) {

				ArrayList<SmsCallLogItem> myArr = callThread
						.getAllCallLogByAddress(myNumber);
				if (myArr.size() > 0) {
					smsTable.addArrayCallLog(myArr, idGroup);
					myContext.getContentResolver().delete(
							CallLog.Calls.CONTENT_URI,
							CallLog.Calls.NUMBER + " = ?",
							new String[] { myNumber });
					Intent i = new Intent(DetailContactLockedActivity.ACTION_SMS_CALL_LOG);
					i.putExtra("ADDRESS", myNumber);
					myContext.sendBroadcast(i);
					myContext.getContentResolver().unregisterContentObserver(
							this);
					showNotification(myContext, myNumber);
				}

			} else {

			}
	        super.onChange(selfChange);
	    }
	}
	
	@SuppressWarnings("deprecation")
	private static void showNotification(Context context, String address) {
		PrivateContactTableAdapter table = PrivateContactTableAdapter.getInstance(context);
		ContactItem contact = table.getContactByAddress(address, PasswordTableAdapter.PASSWORD_1);
	    int icon = R.drawable.ic_launcher;
	    long when = System.currentTimeMillis();
	    NotificationManager notificationManager = (NotificationManager)
	        context.getSystemService(Context.NOTIFICATION_SERVICE);
	    Notification notification = new Notification(icon, "", when);

	    String title = context.getString(R.string.app_name);

	    Intent notificationIntent = new Intent(context, PasswordActivity.class);
	    notificationIntent.putExtra("CONTACT", contact);
	    notificationIntent.putExtra("SMS_RECEIVER", true);
	    //notificationIntent.putExtra("ms", message);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    notification.setLatestEventInfo(context, title, "", intent);
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;

	    notification.defaults |= Notification.DEFAULT_SOUND;

	    notification.defaults |= Notification.DEFAULT_VIBRATE;
	    notificationManager.notify(0, notification);     
	}
}
