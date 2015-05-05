package com.protector.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.ContactLockedAdapter;
import com.protector.adapters.EncryptMediaAdapter;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SmsCallLogsLockFragment extends Fragment implements OnClickListener {
    EncryptMediaAdapter mAdapter;
    private ContactLockedAdapter myAdapter;
    private ProgressDialog myProgressDialog;
    View mViewBack;
    ImageView mViewAdd;
    ImageFragment mImageFragment;
    TextView tvRestore;
    ListView mListView;
    private ArrayList<Pair<ContactItem, SmsCallLogItem>> myArrayContact;
    private int myNumContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myArrayContact=new ArrayList<>();
        myAdapter=new ContactLockedAdapter(getActivity(),
                myArrayContact);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms_call_logs_lock, container,
                false);

        mViewBack = rootView.findViewById(R.id.view_back);
        mViewAdd = (ImageView) rootView.findViewById(R.id.imv_add);
        tvRestore = (TextView) rootView.findViewById(R.id.tv_restore_all);
        mListView = (ListView) rootView.findViewById(R.id.list_video);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mViewBack.setOnClickListener(this);
        mViewAdd.setOnClickListener(this);
        tvRestore.setOnClickListener(this);

        myProgressDialog = new ProgressDialog(getActivity());
        myProgressDialog.setMessage(getString(R.string.loading));
        myProgressDialog.setCancelable(false);
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        super.onActivityCreated(savedInstanceState);
        new ASynLocked().execute();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public class ASynLocked extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            getAllContact();
            SmsCallLogTableAdapter table = SmsCallLogTableAdapter
                    .getInstance(getActivity());
            myNumContact = table
                    .getAllSMS(PasswordTableAdapter.PASSWORD_CURRENT_ID);
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
            myAdapter.clear();
            myProgressDialog.dismiss();
            if (myArrayContact.size() > 0) {
                for (Pair<ContactItem, SmsCallLogItem> item : myArrayContact) {
                    myAdapter.add(item);
                }
            }
            myAdapter.notifyDataSetChanged();

            // myTvSBar.setText(getString(R.string.txt_seekbar, myNumContact +
            // "%"));
            // mySBar.setProgress(myNumContact);

            // if (myAdapter.getCount() > 0) {
            // myTvMessage.setVisibility(View.GONE);
            // myLvContact.setVisibility(View.VISIBLE);
            // } else {
            // myTvMessage.setVisibility(View.VISIBLE);
            // myLvContact.setVisibility(View.GONE);
            // }
        }
    }

    public void getAllContact() {
        PrivateContactTableAdapter privateContactAdapter = PrivateContactTableAdapter
                .getInstance(getActivity());
        myArrayContact = privateContactAdapter
                .getAllContactWithPair(PasswordTableAdapter.PASSWORD_CURRENT_ID);
        Collections.sort(myArrayContact,
                new Comparator<Pair<ContactItem, SmsCallLogItem>>() {

                    @Override
                    public int compare(Pair<ContactItem, SmsCallLogItem> lhs,
                                       Pair<ContactItem, SmsCallLogItem> rhs) {
                        long left = 0, right = 0;
                        try {
                            left = lhs.second.getTime();
                            right = rhs.second.getTime();
                        } catch (Exception e) {
                            return 0;
                        }

                        if (left < right) {
                            return 1;
                        } else if (left > right) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }

                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.imv_add:
                addFragmentStack(new SmsCallLogsLockFromFragment());
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
