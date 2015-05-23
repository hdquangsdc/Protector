/**
 *
 */
package com.protector.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.protector.Configs;
import com.protector.R;
import com.protector.adapters.DetailContactLockedAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.CallLogLocker;
import com.protector.utils.PhoneNumberUtils;
import com.protector.utils.SmsLocker;
import com.protector.views.CustomDialog;

/**
 * @author SDC-Deverloper
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailContactLockedActivity extends AppCompatActivity implements
        OnClickListener {
    private ContactItem myContact;
    private DetailContactLockedAdapter myAdapter;
    private ListView myLv;
    private ProgressDialog myProgressDialog;
    private ArrayList<SmsCallLogItem> myArray, myArrayTmp;
    private MenuItem myMenuViewSMS;
    private AlertDialog.Builder myDialog;
    private TextView myTvTitle;
    public static String ACTION_SMS_CALL_LOG = "com.technatives.hurtlocker.action_sms_call_log";
    private BroadcastReceiver receiver;
    private int myPositionChange;
    private AlertDialog.Builder myDialogAction, myDialogDelete;
    private int myNumContact;
    private TextView myTvMessageNoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contact_locked);
        initComponents();
        addListeners();
    }


    public void initComponents() {
        myLv = (ListView) findViewById(R.id.list_contact_locked);
        myTvMessageNoData = (TextView) findViewById(R.id.tv_message);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SMS_CALL_LOG);


        initProperties();
        initDialogAction();
        initDialogActionDelete();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // do something based on the intent's action
                NotificationManager nMgr = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancel(0);

                String addr1 = intent.getStringExtra("ADDRESS");
                String[] addr = new String[2];
                try {
                    addr = PhoneNumberUtils.getPhoneNumber(
                            DetailContactLockedActivity.this, addr1);
                } catch (Exception ex) {
                    addr[0] = addr1;
                    addr[1] = addr1;
                }
                if (addr[0].toString().equals(myContact.getAddress())
                        || addr[1].toString().equals(myContact.getAddress())) {
                    new ASynContactLocked().execute();
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    public void initProperties() {
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.loading));
        myProgressDialog.setCancelable(false);
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myContact = (ContactItem) getIntent().getSerializableExtra("CONTACT");
        myArray = new ArrayList<SmsCallLogItem>();
        myArrayTmp = new ArrayList<SmsCallLogItem>();
        myAdapter = new DetailContactLockedAdapter(this, myArray);
        myLv.setAdapter(myAdapter);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        new ASynContactLocked().execute();
        super.onResume();
    }

    public void addListeners() {
        // TODO Auto-generated method stub
        findViewById(R.id.btn_message).setOnClickListener(this);
        findViewById(R.id.btn_call).setOnClickListener(this);
        myAdapter
                .setOnLongClickListenerCustom(new DetailContactLockedAdapter.OnLongClickListener() {

                    @Override
                    public void onLongClick(int position) {
                        // TODO Auto-generated method stub
                        if (myAdapter.getItem(position).getTypeCompare() == SmsCallLogTableAdapter.TYPE_SMS) {
                            myPositionChange = position;
                            myDialogAction.show();
                        } else {
                            myPositionChange = position;
                            myDialogDelete.show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		getMenuInflater().inflate(R.menu.menu_detail_contact, menu);
//		myMenuViewSMS = menu.findItem(R.id.action_view_sms_only);
//
//		getSupportActionBar().setDisplayShowHomeEnabled(false);
//		getSupportActionBar().setDisplayShowCustomEnabled(true);
//		getSupportActionBar().setDisplayShowTitleEnabled(false);
//		LayoutParams layout = new LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = inflater.inflate(R.layout.view_actiobar_detail_contact,
//				null);
//		getSupportActionBar().setCustomView(view, layout);
//		getSupportActionBar().setBackgroundDrawable(
//				new ColorDrawable(Color.parseColor("#f25d62")));
//		myTvTitle = ((TextView) view.findViewById(R.id.tv_title));
//		myTvTitle.setText(myContact.getName());
//		if (myContact.getType() == PrivateContactTableAdapter.TYPE_PRIVATE) {
//			ImageView ic = ((ImageView) view.findViewById(R.id.icon));
//			if (myContact.getAvatarByte() != null
//					&& myContact.getAvatarByte().length > 0) {
//				ic.setImageBitmap(getBitmap(myContact.getAvatarByte()));
//			} else {
//				ic.setImageDrawable(getResources().getDrawable(
//						R.drawable.icon_avatar));
//			}
//			view.findViewById(R.id.ic_key).setVisibility(View.VISIBLE);
//			ic.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					Intent i = new Intent(DetailContactLockedActivity.this,
//							AddPrivateContactactivity.class);
//					i.putExtra("EDIT", true);
//					i.putExtra("CONTACT", myContact);
//					startActivityForResult(i, 2324);
//				}
//			});
//		} else {
//			getSupportActionBar().setIcon(R.drawable.icon_private);
//			view.findViewById(R.id.ic_key).setVisibility(View.GONE);
//			ImageView imv = ((ImageView) view.findViewById(R.id.icon));
//			if (myContact.getAvatarByte() != null
//					&& myContact.getAvatarByte().length > 0) {
//				imv.setImageBitmap(getBitmap(myContact.getAvatarByte()));
//			} else {
//				imv.setImageDrawable(getResources().getDrawable(
//						R.drawable.icon_avatar));
//			}
//		}
//		return true;
//	}

    public Bitmap getBitmap(byte[] data) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                opts);
        return bitmap;

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);
        if (arg0 == 2324) {
            if (arg1 == RESULT_CANCELED) {
                if (arg2 != null) {
                    myContact = (ContactItem) arg2
                            .getSerializableExtra("CONTACT");
                    myTvTitle.setText(myContact.getName());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case android.R.id.home:
//                finish();
//                break;
//            case R.id.action_add_all:
//                SmsCallLogTableAdapter table = SmsCallLogTableAdapter
//                        .getInstance(DetailContactLockedActivity.this);
//                myNumContact = table
//                        .getAllSMS(PasswordTableAdapter.PASSWORD_CURRENT_ID);
//                new AsynAddContact().execute();
//                break;
//            case R.id.action_view_sms_only:
//                if (myMenuViewSMS.getTitle().equals(
//                        getString(R.string.view_message_only))) {
//                    if (checkSMS()) {
//                        myMenuViewSMS.setTitle(R.string.txt_view_message_calllog);
//                        showSMS();
//                    } else {
//                        showDialogNotSMS();
//                    }
//                } else {
//                    myMenuViewSMS.setTitle(R.string.view_message_only);
//                    showAll();
//
//                }
//                break;
//            case R.id.action_share:
//                Uri smsUri = Uri.parse("sms:" + myContact.getAddress());
//                Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
//                intent.putExtra("sms_body", getString(R.string.txt_share_message));
//                startActivity(intent);
//                break;
//            case R.id.action_restore_sms:
//                if (!checkSmsRemain()) {
//                    Toast.makeText(this, getString(R.string.txt_not_sms_restore),
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    showDialogRestoreSMS();
//                }
//                break;
//            case R.id.action_delete_history:
//                showDiaLogDeleteAll();
//                break;
//            default:
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialogNotSMS() {
        CustomDialog dialog = new CustomDialog(this,
                getString(R.string.alert),
                getString(R.string.not_message));
        dialog.setOnClickOk(new CustomDialog.OnClickOk() {

            @Override
            public void Click() {

            }
        });
        dialog.show();
        dialog.setTextBtn(getString(R.string.btn_ok),
                getString(R.string.btn_cancel));
        dialog.goneBtn2();
    }

    public void showDialogRestoreSMS() {
        CustomDialog dialog = new CustomDialog(this,
                getString(R.string.restore_all_sms_to_phone),
                getString(R.string.restore_sms_ms));
        dialog.setOnClickOk(new CustomDialog.OnClickOk() {

            @Override
            public void Click() {
                new ASynRestoreSMS().execute();
            }
        });
        dialog.show();
    }

    public boolean checkSMS() {
        for (SmsCallLogItem item : myArrayTmp) {
            if (item.getTypeCompare() == 1)
                return true;
        }
        return false;
    }

    public void showSMS() {
        myAdapter.clear();
        for (SmsCallLogItem item : myArrayTmp) {
            if (item.getTypeCompare() == 1) {
                myAdapter.add(item);
            }
        }
        myAdapter.notifyDataSetChanged();
    }

    public void showAll() {
        myAdapter.clear();
        // myAdapter.addAll(myArrayTmp);
        for (SmsCallLogItem item : myArrayTmp) {
            myAdapter.add(item);
        }
        myAdapter.notifyDataSetChanged();
    }

    public class ASynContactLocked extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            getAllContact();
            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.show();
            myAdapter.clear();
            myArray.clear();
            myArrayTmp.clear();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            myAdapter.clear();
            myProgressDialog.dismiss();
            if (myArray.size() > 0) {
                for (SmsCallLogItem item : myArray) {
                    myAdapter.add(item);
                }
                myLv.setVisibility(View.VISIBLE);
                myTvMessageNoData.setVisibility(View.GONE);
            } else {
                myLv.setVisibility(View.GONE);
                myTvMessageNoData.setVisibility(View.VISIBLE);
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    public class ASynRestoreSMS extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            restoreSMS();
            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            myProgressDialog.setMessage(getString(R.string.restoring));
            myProgressDialog.show();
            Configs.EXPORT_SMS = true;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Toast.makeText(DetailContactLockedActivity.this,
                    getString(R.string.restore_success), Toast.LENGTH_SHORT)
                    .show();
            Configs.EXPORT_SMS = false;
            myProgressDialog.dismiss();
            myAdapter.clear();
            for (SmsCallLogItem item : myArrayTmp) {
                myAdapter.add(item);
            }
            myAdapter.notifyDataSetChanged();
            if (myAdapter.getCount() == 0
                    && myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                PrivateContactTableAdapter table = PrivateContactTableAdapter
                        .getInstance(DetailContactLockedActivity.this);
                table.removeByID(myContact.getId());
                finish();
            } else {

            }

            if (myAdapter.getCount() > 0) {
                myLv.setVisibility(View.VISIBLE);
                myTvMessageNoData.setVisibility(View.GONE);
            } else {
                myLv.setVisibility(View.GONE);
                myTvMessageNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    public void restoreSMS() {
        SmsLocker thread = SmsLocker.getInstance(this);
        SmsCallLogTableAdapter table = SmsCallLogTableAdapter.getInstance(this);
        for (int i = myArrayTmp.size() - 1; i >= 0; i--) {
            try {
                if (i < myArrayTmp.size()) {
                    SmsCallLogItem item = myArrayTmp.get(i);
                    if (item.getTypeCompare() == SmsCallLogTableAdapter.TYPE_SMS) {
                        boolean b = thread.restoreSms(item);
                        if (b) {
                            myArrayTmp.remove(item);
                            table.removeID(item.getId());
                        }
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    public boolean checkSmsRemain() {
        for (SmsCallLogItem item : myArrayTmp) {
            if (item.getTypeCompare() == SmsCallLogTableAdapter.TYPE_SMS) {
                return true;
            }
        }
        return false;
    }

    public class ASynDeleteContactLocked extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return deleteAll();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            myProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(DetailContactLockedActivity.this,
                        getString(R.string.deleted_success),
                        Toast.LENGTH_SHORT).show();
                myProgressDialog.dismiss();
                myAdapter.clear();
                myAdapter.notifyDataSetChanged();
                myLv.setVisibility(View.GONE);
                myTvMessageNoData.setVisibility(View.VISIBLE);
                if (myAdapter.getCount() == 0
                        && myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                    PrivateContactTableAdapter table = PrivateContactTableAdapter
                            .getInstance(DetailContactLockedActivity.this);
                    table.removeByID(myContact.getId());
                    finish();
                } else {

                }
            } else {
                Toast.makeText(DetailContactLockedActivity.this,
                        getString(R.string.please_try_again),
                        Toast.LENGTH_SHORT).show();
                myProgressDialog.dismiss();
            }
        }
    }

    public void showDiaLogDeleteAll() {
        CustomDialog dialog = new CustomDialog(this,
                getString(R.string.delete_entire_history),
                getString(R.string.delete_entire_history_message));
        dialog.setOnClickOk(new CustomDialog.OnClickOk() {

            @Override
            public void Click() {
                new ASynDeleteContactLocked().execute();
            }
        });
        dialog.show();
    }

    public void getAllContact() {
        SmsCallLogTableAdapter table = SmsCallLogTableAdapter.getInstance(this);
        myArray = table.getAllByGroupID(myContact.getGroupId());
        myArrayTmp.addAll(myArray);
    }

    public boolean deleteAll() {
        try {
            myProgressDialog.setMessage(getString(R.string.deleting));
            SmsCallLogTableAdapter table = SmsCallLogTableAdapter
                    .getInstance(this);
            table.removeByGroupID(myContact.getId());
            if (myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                PrivateContactTableAdapter privateContact = PrivateContactTableAdapter
                        .getInstance(this);
                privateContact.removeByID(myContact.getId());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_message:
                Uri smsUri = Uri.parse("sms:" + myContact.getAddress());
                Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                intent.putExtra("sms_body", "");
                PackageManager pm = this.getPackageManager();
                List<ResolveInfo> res = pm.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                if (res.size() > 0) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, getString(R.string.not_support_phone),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_call:
                if (myContact.getAddress() != null
                        && myContact.getAddress().length() > 0) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + myContact.getAddress()));
                    PackageManager pm1 = this.getPackageManager();
                    List<ResolveInfo> res1 = pm1.queryIntentActivities(callIntent,
                            PackageManager.MATCH_DEFAULT_ONLY);
                    if (res1.size() > 0) {
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(this,
                                getString(R.string.not_support_phone),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                break;
        }
    }

    public class AsynAddContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SmsCallLogTableAdapter mTable = SmsCallLogTableAdapter
                    .getInstance(DetailContactLockedActivity.this);
            String address = myContact.getAddress();
            SmsLocker locker = SmsLocker
                    .getInstance(DetailContactLockedActivity.this);
            CallLogLocker callLogThread = CallLogLocker
                    .getInstance(DetailContactLockedActivity.this);
            // Get All SMS
            ArrayList<SmsCallLogItem> arraySms = locker
                    .getAllSMSByAddress(address);

			/*
             * mTable.addArraySms(arraySms, myContact.getGroupId()); // Delete
			 * SMS SmsLocker.deleteSMS(myContact.getAddress());
			 */

            mTable.addArraySms(arraySms, myContact.getGroupId());
            locker.deleteSMS(myContact.getAddress());

            // Get All Call Log
            ArrayList<SmsCallLogItem> arrayCallLog = callLogThread
                    .getAllCallLogByAddress(address);
            mTable.addArrayCallLog(arrayCallLog, myContact.getGroupId());
            // Delete Call log from Phone
            callLogThread.deleteCallLog(myContact.getAddress());

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myArray.clear();
            myArrayTmp.clear();
            myProgressDialog.setMessage(getString(R.string.importing));
            myProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            myProgressDialog.dismiss();
            myMenuViewSMS.setTitle(R.string.view_message_only);
            new ASynContactLocked().execute();
        }
    }

    public boolean deleteSms(int smsId) {
        boolean isSmsDeleted = false;
        try {
            this.getContentResolver().delete(
                    Uri.parse("content://sms/" + smsId), null, null);
            isSmsDeleted = true;

        } catch (Exception ex) {
            isSmsDeleted = false;
        }
        return isSmsDeleted;
    }

    public void initDialogAction() {
        String[] title = getResources().getStringArray(
                R.array.arr_detail_action);
        String[] icon = getResources().getStringArray(
                R.array.arr_detail_action_icon);
        final Item[] items = new Item[title.length];
        for (int i = 0; i < title.length; i++) {
            items[i] = new Item(title[i], icon[i]);
        }
        ListAdapter adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                int resId = DetailContactLockedActivity.this.getResources()
                        .getIdentifier(
                                items[position].icon,
                                "drawable",
                                DetailContactLockedActivity.this
                                        .getPackageName());
                tv.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
                int dp5 = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };

        myDialogAction = new AlertDialog.Builder(this).setTitle(
                getString(R.string.actions)).setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SmsCallLogItem object = myAdapter
                                .getItem(myPositionChange);
                        switch (which) {
                            case 0:
                                // Forward
                                Uri smsUri = Uri.parse("sms:");
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        smsUri);
                                intent.putExtra("sms_body", object.getBodySms());
                                startActivity(intent);
                                break;
                            case 1:
                                // Restore to phone
                                new ASynRestoreSingleSMS().execute();
                                break;
                            case 2:
                                // delete
                                SmsCallLogItem itemSms = myAdapter
                                        .getItem(myPositionChange);
                                SmsCallLogTableAdapter tableSms = SmsCallLogTableAdapter
                                        .getInstance(DetailContactLockedActivity.this);
                                tableSms.removeID(itemSms.getId());
                                myArrayTmp.remove(itemSms);
                                myAdapter.remove(itemSms);
                                myAdapter.notifyDataSetChanged();

                                if (myAdapter.getCount() == 0) {
                                    for (SmsCallLogItem item : myArrayTmp) {
                                        myAdapter.add(item);
                                    }
                                    myAdapter.notifyDataSetChanged();
                                    if (myAdapter.getCount() == 0
                                            && myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                                        PrivateContactTableAdapter table = PrivateContactTableAdapter
                                                .getInstance(DetailContactLockedActivity.this);
                                        table.removeByID(myContact.getId());
                                        finish();
                                    } else {

                                    }

                                    if (myAdapter.getCount() > 0) {
                                        myLv.setVisibility(View.VISIBLE);
                                        myTvMessageNoData.setVisibility(View.GONE);
                                    } else {
                                        myLv.setVisibility(View.GONE);
                                        myTvMessageNoData
                                                .setVisibility(View.VISIBLE);
                                    }
                                    myMenuViewSMS
                                            .setTitle(R.string.view_message_only);
                                    Toast.makeText(
                                            DetailContactLockedActivity.this,
                                            getString(R.string.deleted_success),
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
    }

    public class ASynRestoreSingleSMS extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            SmsLocker thread = SmsLocker
                    .getInstance(DetailContactLockedActivity.this);
            SmsCallLogItem item = myAdapter.getItem(myPositionChange);
            if (item.getTypeCompare() == SmsCallLogTableAdapter.TYPE_SMS) {
                return thread.restoreSms(item);
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Configs.EXPORT_SMS = true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Toast.makeText(DetailContactLockedActivity.this,
                    getString(R.string.restore_success), Toast.LENGTH_SHORT)
                    .show();
            if (result) {
                SmsCallLogTableAdapter table = SmsCallLogTableAdapter
                        .getInstance(DetailContactLockedActivity.this);
                SmsCallLogItem item = myAdapter.getItem(myPositionChange);
                myArrayTmp.remove(item);
                myAdapter.remove(item);
                table.removeID(item.getId());
                myAdapter.notifyDataSetChanged();
            }
            if (myAdapter.getCount() == 0) {
                for (SmsCallLogItem item : myArrayTmp) {
                    myAdapter.add(item);
                }
                myAdapter.notifyDataSetChanged();
                if (myAdapter.getCount() == 0
                        && myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                    PrivateContactTableAdapter table = PrivateContactTableAdapter
                            .getInstance(DetailContactLockedActivity.this);
                    table.removeByID(myContact.getId());
                    finish();
                } else {

                }

                if (myAdapter.getCount() > 0) {
                    myLv.setVisibility(View.VISIBLE);
                    myTvMessageNoData.setVisibility(View.GONE);
                } else {
                    myLv.setVisibility(View.GONE);
                    myTvMessageNoData.setVisibility(View.VISIBLE);
                }
                myMenuViewSMS.setTitle(R.string.view_message_only);
                ;
            }
            Configs.EXPORT_SMS = false;
        }
    }

    public void initDialogActionDelete() {
        String[] title = getResources().getStringArray(
                R.array.arr_detail_action_delete);
        String[] icon = getResources().getStringArray(
                R.array.arr_detail_action_delete_icon);
        final Item[] items = new Item[title.length];
        for (int i = 0; i < title.length; i++) {
            items[i] = new Item(title[i], icon[i]);
        }
        ListAdapter adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                int resId = DetailContactLockedActivity.this.getResources()
                        .getIdentifier(
                                items[position].icon,
                                "drawable",
                                DetailContactLockedActivity.this
                                        .getPackageName());
                tv.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
                int dp5 = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };

        myDialogDelete = new AlertDialog.Builder(this).setTitle(
                getString(R.string.actions)).setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SmsCallLogItem object = myAdapter
                                .getItem(myPositionChange);
                        switch (which) {
                            case 0:
                                // delete
                                SmsCallLogItem itemSms = myAdapter
                                        .getItem(myPositionChange);
                                SmsCallLogTableAdapter tableSms = SmsCallLogTableAdapter
                                        .getInstance(DetailContactLockedActivity.this);
                                tableSms.removeID(itemSms.getId());
                                myArrayTmp.remove(itemSms);
                                myAdapter.remove(itemSms);
                                myAdapter.notifyDataSetChanged();
                                if (myArrayTmp.size() == 0
                                        && myContact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC) {
                                    PrivateContactTableAdapter privateContact = PrivateContactTableAdapter
                                            .getInstance(DetailContactLockedActivity.this);
                                    privateContact.removeByID(myContact.getId());
                                    finish();
                                }
                                if (myAdapter.getCount() > 0) {
                                    myLv.setVisibility(View.VISIBLE);
                                    myTvMessageNoData.setVisibility(View.GONE);
                                } else {
                                    myLv.setVisibility(View.GONE);
                                    myTvMessageNoData.setVisibility(View.VISIBLE);
                                }
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
    }

    public class Item {
        public final String text;
        public final String icon;

        public Item(String text, String icon) {
            this.text = text;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
