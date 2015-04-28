package com.protector.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
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

        myArraySMS = new ArrayList<SmsCallLogItem>();
        myArraySMSPhone = new ArrayList<SmsCallLogItem>();
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
                    listener.onPick(myAdapter.getArrayChecks());
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
}
