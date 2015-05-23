package com.protector.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.protector.Configs;
import com.protector.activities.DetailContactLockedActivity;
import com.protector.asynctasks.MonitorThread;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.handlers.ActivityStartingHandler;
import com.protector.objects.SmsCallLogItem;
import com.protector.receivers.ScreenReceiver;
import com.protector.utils.PhoneNumberUtils;
import com.protector.utils.SmsLocker;

import java.util.ArrayList;
import java.util.Date;

public class ProtectorService extends Service {

    private static final String APPSOLUT_INTENT_ACTION_BIND_MESSAGE_SERVICE = "appsolut.intent.action.bindMessageService";
    private static final String LOG_TAG = "ProtectorService";
    public static String HOME_PACKAGE;
    BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true,
                new SmsObserver(new Handler(), this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBroadcastReceiver();
        getHomePackage();
        handleCommand(intent);
        registerScreenListener();
        return Service.START_STICKY;
    }

    private void registerScreenListener() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "The AIDLMessageService was destroyed.");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (APPSOLUT_INTENT_ACTION_BIND_MESSAGE_SERVICE.equals(intent
                .getAction())) {
            Log.d(LOG_TAG, "The AIDLMessageService was binded.");
            return new IRemoteProtector(this);
        }
        return null;
    }

    String getABC() {
        return "abc";
    }

    private void getHomePackage() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        HOME_PACKAGE = resolveInfo.activityInfo.packageName;
        Log.i("Service", "" + HOME_PACKAGE);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void handleCommand(Intent intent) {
        startMonitorThread((ActivityManager) this
                .getSystemService(Context.ACTIVITY_SERVICE));
    }

    private void startMonitorThread(final ActivityManager am) {
        if (mThread != null)
            mThread.interrupt();

        mThread = new MonitorThread(this, new ActivityStartingHandler(this));
        mThread.start();
    }

    private static Thread mThread;

    public class SmsObserver extends ContentObserver {
        private Context context;
        private final Uri uriSMS = Uri.parse("content://sms/sent");

        public SmsObserver(Handler handler, Context ctx) {
            super(handler);
            context = ctx;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (!Configs.EXPORT_SMS) {
                new SmsSendAsyn().execute();
            }
        }

        public class SmsSendAsyn extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    Cursor cur = context.getContentResolver().query(uriSMS,
                            null, null, null, null);
                    if (cur != null && cur.moveToFirst()) {
                        long date = cur.getLong(cur
                                .getColumnIndex(SmsLocker.TextSmsColumns.DATE));
                        if ((new Date()).getTime() - date < 10000) {
                            String myNumber = cur.getString(cur
                                    .getColumnIndex("address"));
                            cur.close();
                            PrivateContactTableAdapter contactTable = PrivateContactTableAdapter
                                    .getInstance(context);
                            SmsCallLogTableAdapter smsTable = SmsCallLogTableAdapter
                                    .getInstance(context);
                            long idGroup = contactTable.checkContactByAddresss(
                                    myNumber,
                                    PrivateContactTableAdapter.TYPE_PRIVATE,
                                    PasswordTableAdapter.PASSWORD_1);
                            SmsLocker smsThread = SmsLocker
                                    .getInstance(context);
                            if (idGroup != -1) {
                                String[] adr = new String[2];
                                try {
                                    adr = PhoneNumberUtils.getPhoneNumber(context,
                                            myNumber);
                                } catch (Exception ex) {
                                    adr[0] = myNumber;
                                    adr[1] = myNumber;
                                }

                                ArrayList<SmsCallLogItem> myArr = smsThread
                                        .getAllSMSByAddress(adr);

                                if (myArr.size() > 0) {
                                    smsTable.addArraySms(myArr, idGroup);
                                    String[] addr = new String[2];
                                    try {
                                        addr = PhoneNumberUtils.getPhoneNumber(
                                                context, myNumber);
                                    } catch (Exception ex) {
                                        addr[0] = myNumber;
                                        addr[1] = myNumber;
                                    }
                                    context.getContentResolver().delete(
                                            Uri.parse("content://sms"),
                                            "address IN (?,?)",
                                            new String[]{addr[0],
                                                    addr[1]});
                                    return myNumber;
                                }
                            }
                        } else {
                            cur.close();
                        }
                    } else {
                        if (cur != null) {
                            cur.close();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (result != null) {
                    Intent i = new Intent(
                            DetailContactLockedActivity.ACTION_SMS_CALL_LOG);
                    i.putExtra("ADDRESS", result);
                    context.sendBroadcast(i);
                }
            }
        }
    }
}
