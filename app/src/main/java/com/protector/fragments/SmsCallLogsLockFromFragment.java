package com.protector.fragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.protector.R;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.GroupContactObject;
import com.protector.objects.SmsCallLogItem;

import java.util.ArrayList;

public class SmsCallLogsLockFromFragment extends Fragment implements
        OnClickListener {
    Button mBtnHidePhoneNumbers, mBtnHideMessages;
    View mViewChoice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_sms_lock_from_number, container, false);
        mBtnHidePhoneNumbers = (Button) rootView
                .findViewById(R.id.btn_hide_phone_numbers);
        mBtnHideMessages = (Button) rootView
                .findViewById(R.id.btn_hide_messages);
        mViewChoice = rootView.findViewById(R.id.hide_phone_number_from);

        rootView.findViewById(R.id.btn_from_contact).setOnClickListener(this);
        rootView.findViewById(R.id.btn_from_call_logs).setOnClickListener(this);
        rootView.findViewById(R.id.btn_from_sms_logs).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBtnHideMessages.setOnClickListener(this);
        mBtnHidePhoneNumbers.setOnClickListener(this);
        mViewChoice.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hide_phone_numbers:
                if (mViewChoice.getVisibility()==View.VISIBLE){
                    mViewChoice.setVisibility(View.GONE);
                } else {
                    mViewChoice.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_hide_messages:
                SmsLockFragment fragment = new SmsLockFragment();
                fragment.setOnPickListener(new IPick() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onPick(Object item) {
                        getActivity().onBackPressed();
                        ArrayList<SmsCallLogItem> array = (ArrayList<SmsCallLogItem>) item;
                    }
                });
                addFragmentStack(fragment);
                break;
            case R.id.btn_from_contact:
                addFragmentStack(new ContactListFragment());
                break;
            case R.id.btn_from_call_logs:
                addFragmentStack(new CallLogListFragment());
                break;
            case R.id.btn_from_sms_logs:
                addFragmentStack(new SmsListFragment());
                break;
            default:
                break;
        }

    }

    public void addFragmentStack(Fragment fragment) {
        FragmentManager fragmentManager = getActivity()
                .getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}
