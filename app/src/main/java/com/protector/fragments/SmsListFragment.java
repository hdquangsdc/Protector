package com.protector.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.SmsLogAdapter;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.SmsLocker;

import java.util.ArrayList;

public class SmsListFragment extends Fragment implements View.OnClickListener {
    private ProgressDialog myProgressDialog;
    private ArrayList<SmsCallLogItem> myArraySMS, myArraySmsPhone;

    private SmsLogAdapter myAdapter;
    private EditText myEdtSearch;
    private ImageView mDone;
    private IPick listener;
    private int myType;
    private View mViewBack;

    ListView myLv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms_list, container,
                false);
        myLv = (ListView) rootView.findViewById(R.id.list_message);
        mDone = (ImageView) rootView.findViewById(R.id.tv_done);
        mViewBack=rootView.findViewById(R.id.view_back);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDone.setOnClickListener(this);
        mViewBack.setOnClickListener(this);
        myProgressDialog = new ProgressDialog(getActivity());
        myProgressDialog.setMessage(getString(R.string.loading));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);

        myArraySMS=new ArrayList<>();
        myArraySmsPhone = new ArrayList<>();
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

    public class SynSMS extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            myArraySMS=SmsLocker.getInstance(getActivity()).getAllSMS();
            getSmsSingleAdrress();
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
            myAdapter = new SmsLogAdapter(getActivity(),
                    myArraySmsPhone, myType);
            myLv.setAdapter(myAdapter);
        }
    }

    public ArrayList<SmsCallLogItem> getSmsSingleAdrress() {
        ArrayList<String> myArrayStr = new ArrayList<>();
        for (SmsCallLogItem item : myArraySMS) {
            try {
                if (!myArrayStr.contains(item.getAddress())
                        && !item.getAddress().equals("-1")) {
                    item.setAvatarByte(SmsLocker.getInstance(getActivity()).getPhotoContact(item.getAddress()));
                    myArrayStr.add(item.getAddress());
                    myArraySmsPhone.add(item);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
