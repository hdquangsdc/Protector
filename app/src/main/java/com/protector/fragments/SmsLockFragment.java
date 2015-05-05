package com.protector.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.SmsAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.SmsLocker;

import java.util.ArrayList;

public class SmsLockFragment extends Fragment implements View.OnClickListener {
    ListView mMessageList;
    private ProgressDialog myProgressDialog;
    private ArrayList<SmsCallLogItem> myArraySMS, myArraySMSPhone;
    private SmsAdapter myAdapter;
    private TextView tvDone;
    private IPick listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms_lock, container,
                false);
        mMessageList = (ListView) rootView.findViewById(R.id.list_message);
        tvDone = (TextView) rootView.findViewById(R.id.tv_done);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvDone.setOnClickListener(this);
        myProgressDialog = new ProgressDialog(getActivity());
        myProgressDialog.setMessage(getString(R.string.loading));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);

        myArraySMS = new ArrayList<>();
        myArraySMSPhone = new ArrayList<>();
        new SynSMS().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_done:
                if (listener!=null) {
//                    listener.onPick(myAdapter.getArrayChecks());
                    new SynAddSMS().execute(myAdapter.getArrayChecks());
                }
                break;
            default:
                break;
        }
    }

    private class SynSMS extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getAllSMS();
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
            myProgressDialog.dismiss();
            myAdapter = new SmsAdapter(getActivity(), myArraySMS);
            mMessageList.setAdapter(myAdapter);
            // myEdtSearch.setHint(getString(R.string.txt_hint_search_message,
            // myArraySMSPhone.size()));
        }
    }

    public void getAllSMS() {
        SmsLocker thread = SmsLocker.getInstance(getActivity());
        ArrayList<SmsCallLogItem> myArr = thread.getAllSMS();
        myArraySMS.addAll(myArr);
        myArraySMSPhone.addAll(myArr);
    }

    public void setOnPickListener(IPick listener) {
        this.listener=listener;
    }

    public class SynAddSMS extends AsyncTask<ArrayList<SmsCallLogItem>, Void, Void> {

        private ProgressDialog myProgressDialog;

        @Override
        protected Void doInBackground(ArrayList<SmsCallLogItem>... params) {
            SmsCallLogTableAdapter mTable = SmsCallLogTableAdapter
                    .getInstance(getActivity());
            for (SmsCallLogItem object : params[0]) {
                mTable.addSMS(object.getGroupId(), object.getType(),
                        object.getName(), object.getAddress(),
                        object.getTime(), object.getBodySms(),
                        object.getRead(), object.getState(),
                        object.getNumberIndex());

                // Delete SMS from Phone
//                deleteSms(object.getNumberIndex());
            }
            // remove from adapter
//			for (int i = arrayChecks.size() - 1; i >= 0; i--) {
//				myArraySMS.remove(myAdapter.getItem(arrayChecks.get(i)));
//			}
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myProgressDialog = new ProgressDialog(getActivity());
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.setCancelable(false);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
//			myAdapter = new SmsAdapter(ListSmsActivity.this, myArraySMS);
//			myLV.setAdapter(myAdapter);
            myProgressDialog.dismiss();
        }
    }

    public boolean deleteSms(int smsId) {
        boolean isSmsDeleted = false;
        try {
            getActivity().getContentResolver().delete(
                    Uri.parse("content://sms/" + smsId), null, null);
            isSmsDeleted = true;

        } catch (Exception ex) {
            isSmsDeleted = false;
        }
        return isSmsDeleted;
    }
}
