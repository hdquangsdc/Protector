package com.protector.receivers;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;

import com.protector.activities.DetailContactLockedActivity;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.CallLogLocker;


public class CallPhoneBroadcastReceiver extends BroadcastReceiver {
	private Context myContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		myContext = context;
		String outgoingno = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		myContext.getContentResolver().registerContentObserver(
				CallLog.Calls.CONTENT_URI, true,
				new MyContentObserver(new Handler(), outgoingno));
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
			// Delete call log
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
					myContext.getContentResolver().unregisterContentObserver(
							this);
					Intent i = new Intent(DetailContactLockedActivity.ACTION_SMS_CALL_LOG);
					i.putExtra("ADDRESS", myNumber);
					myContext.sendBroadcast(i);
				}

			} else {

			}
			super.onChange(selfChange);
		}
	}
}
