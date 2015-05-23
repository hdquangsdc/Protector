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

import com.protector.R;
import com.protector.adapters.CallLogAdapter;
import com.protector.adapters.ContactAdapter;
import com.protector.objects.ContactItem;
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
        mViewBack=rootView.findViewById(R.id.view_back);

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
                if (listener != null) {
//                    listener.onPick(myAdapter.getArrayChecks());
//                    new SynAddSMS().execute(myAdapter.getArrayChecks());
                }
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
}
