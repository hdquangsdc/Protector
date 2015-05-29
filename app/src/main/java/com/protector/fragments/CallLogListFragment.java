package com.protector.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.protector.R;
import com.protector.adapters.CallLogAdapter;
import com.protector.adapters.ContactAdapter;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.GroupContactObject;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.CallLogLocker;
import com.protector.utils.SmsLocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CallLogListFragment extends Fragment implements View.OnClickListener {
    private ProgressDialog myProgressDialog;
    private ArrayList<SmsCallLogItem> myArrayObject;

    private CallLogAdapter myAdapter;
    private EditText myEdtSearch;
    private ImageView tvDone;
    private IPick listener;
    private int myType;
    private View mViewBack;

    ListView myLv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_call_log_list, container,
                false);
        myLv = (ListView) rootView.findViewById(R.id.list_message);
        tvDone = (ImageView) rootView.findViewById(R.id.tv_done);
        mViewBack = rootView.findViewById(R.id.view_back);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvDone.setOnClickListener(this);
        mViewBack.setOnClickListener(this);
        myProgressDialog = new ProgressDialog(getActivity());
        myProgressDialog.setMessage(getString(R.string.loading));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);

        myArrayObject = new ArrayList<>();
        new SynCallLog().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_done:
//                if (listener != null) {
//                    listener.onPick(myAdapter.getArrayChecks());
//                    new SynAddSMS().execute(myAdapter.getArrayChecks());
                new SynPrivateContact().execute();
//                }
                break;
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            default:
                break;
        }
    }


    public void setOnPickListener(IPick listener) {
        this.listener = listener;
    }

    public class SynCallLog extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getAllCallLog();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                if ((myProgressDialog != null) && myProgressDialog.isShowing()) {
                    myProgressDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            myAdapter = new CallLogAdapter(getActivity(),
                    myArrayObject, myType);
            myLv.setAdapter(myAdapter);
        }
    }

    public void getAllCallLog() {
        CallLogLocker thread = CallLogLocker.getInstance(getActivity());
        ArrayList<SmsCallLogItem> myArr = thread.getAllCallLog();
        getCallLogSingleAdrress(myArr);
    }

    public ArrayList<SmsCallLogItem> getCallLogSingleAdrress(
            ArrayList<SmsCallLogItem> myArr) {
        ArrayList<String> myArrayStr = new ArrayList<>();
        for (SmsCallLogItem item : myArr) {
            if (!myArrayStr.contains(item.getAddress())
                    && !item.getAddress().equals("-1")) {
                myArrayStr.add(item.getAddress());
                myArrayObject.add(item);
            }
        }
        return null;
    }

    public class SynPrivateContact extends AsyncTask<Void, Void, Void> {
        ArrayList<GroupContactObject> myArray;

        public SynPrivateContact() {
            myArray = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            ArrayList<SmsCallLogItem> arrayChecks = myAdapter.getArrayChecks();
            PrivateContactTableAdapter contactTable = PrivateContactTableAdapter
                    .getInstance(getActivity());
            GroupContactObject group = null;
            for (SmsCallLogItem object : arrayChecks) {
                long groupId = contactTable.addContact(object, myType,
                        PasswordTableAdapter.PASSWORD_CURRENT_ID);
                String address = object.getAddress();
                group = new GroupContactObject(address, groupId);
                myArray.add(group);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (myProgressDialog != null) {
                myProgressDialog.setMessage(getString(R.string.saving));
                myProgressDialog.show();
            } else {
                myProgressDialog = new ProgressDialog(getActivity());
                myProgressDialog.setMessage(getString(R.string.saving));
                myProgressDialog.setCancelable(false);
                myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                myProgressDialog.setCancelable(false);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            myProgressDialog.dismiss();
//            Intent i = new Intent();
//            i.putExtra("OBJECT", new ArrayGroupContactSerializable(myArray));
//            i.putExtra("TYPE", 2);
//            setResult(RESULT_OK, i);
//            finish();
            new SynAddContactByAddress().execute(myArray);
        }
    }

    public class SynAddContactByAddress extends
            AsyncTask<ArrayList<GroupContactObject>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<GroupContactObject>... params) {
            ArrayList<GroupContactObject> param = params[0];
            SmsCallLogTableAdapter mTable = SmsCallLogTableAdapter
                    .getInstance(getActivity());
            SmsLocker smsThread = SmsLocker
                    .getInstance(getActivity());
            CallLogLocker callLogThread = CallLogLocker
                    .getInstance(getActivity());

            for (GroupContactObject item : param) {
                GroupContactObject group = item;
                // Get and add all SMS
                ArrayList<SmsCallLogItem> arraySms = smsThread
                        .getAllSMSByAddress(group.getAddress());
                count += arraySms.size();
                countSms += arraySms.size();
                mTable.addArraySms(arraySms, group.getGroupID());
                // Delete all SMS
                int deleteNum = smsThread.deleteSMS(group.getAddress());
                if (deleteNum <= 0) {
//                    isDeleteSmsCallLog = true;
                } else {
//                    isDeleteSmsCallLog = false;
                }
                // Get All Call Log
                ArrayList<SmsCallLogItem> arrayCallLog = callLogThread
                        .getAllCallLogByAddressContain0(group.getAddress());
                count += arrayCallLog.size();
                mTable.addArrayCallLog(arrayCallLog, group.getGroupID());
                // Delete all Call Log
                callLogThread.deleteCallLogContain0(group.getAddress());
            }

            if (countSms == 0) {
//                isDeleteSmsCallLog = false;
            }
            return null;
        }

        int count;
        int countSms;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            count = 0;
            countSms = 0;
            myProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (count > 0) {
                Toast.makeText(getActivity(),
                        getString(R.string.contact_imported_successfully),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),
                        getString(R.string.there_contact_call_log),
                        Toast.LENGTH_SHORT).show();
            }
            myProgressDialog.dismiss();
        }
    }

}
