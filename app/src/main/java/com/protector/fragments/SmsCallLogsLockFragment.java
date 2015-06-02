package com.protector.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.protector.R;
import com.protector.activities.DetailContactLockedActivity;
import com.protector.adapters.ContactLockedAdapter;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SmsCallLogsLockFragment extends Fragment implements OnClickListener {

    public static int RQ_DETAIL = 212;

    private ContactLockedAdapter myAdapter;
    private ProgressDialog myProgressDialog;
    View mViewBack;
    private ContactItem myContact;

    ImageFragment mImageFragment;
    ListView mListView;
    private ArrayList<Pair<ContactItem, SmsCallLogItem>> myArrayContact;
    private int myNumContact;
    ButtonFloat mBtnAdd;
    private AlertDialog.Builder myDialogContact, myDialogPrivateContact;
    private TextView myTvTitleContact, myTvTitlePrivateContact, myTvSBar;
    private Pair<ContactItem, SmsCallLogItem> myContactChange;
    private int myPositionChange, myTypeChange;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myArrayContact = new ArrayList<>();
        myAdapter = new ContactLockedAdapter(getActivity(),
                myArrayContact);
        myAdapter
                .setOnLongClickListener(new ContactLockedAdapter.OnLongClickListener() {

                    @Override
                    public void onLongClick(int position) {
                        myContactChange = myAdapter.getItem(position);
                        myPositionChange = position;
                        ContactItem contact = myContactChange.first;
                        if (contact.getType() == PrivateContactTableAdapter.TYPE_PUBLIC
                                || contact.getType() == PrivateContactTableAdapter.TYPE_BLACK) {
                            if (myDialogContact != null) {
                                myDialogContact.setTitle(contact.getName());
                                myDialogContact.show();
                            }
                        } else {
                            if (myTvTitlePrivateContact != null) {
                                myDialogPrivateContact.setTitle(contact
                                        .getName());
                                myDialogPrivateContact.show();
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms_call_logs_lock, container,
                false);

        mViewBack = rootView.findViewById(R.id.view_back);
        mListView = (ListView) rootView.findViewById(R.id.list_video);
        mBtnAdd = (ButtonFloat) rootView.findViewById(R.id.btn_add);

        mListView.setAdapter(myAdapter);

        return rootView;
    }

//    public void initDialogContact() {
//        String[] title = getResources()
//                .getStringArray(R.array.arr_item_contact);
//        String[] icon = getResources().getStringArray(
//                R.array.arr_item_contact_icon);
//        final Item[] items = new Item[title.length];
//        for (int i = 0; i < title.length; i++) {
//            items[i] = new Item(title[i], icon[i]);
//        }
//        ListAdapter adapter = new ArrayAdapter<Item>(this,
//                android.R.layout.select_dialog_item, android.R.id.text1, items) {
//            public View getView(int position, View convertView, ViewGroup parent) {
//                View v = super.getView(position, convertView, parent);
//                TextView tv = (TextView) v.findViewById(android.R.id.text1);
//                int resId = ContactLockedActivity.this.getResources()
//                        .getIdentifier(items[position].icon, "drawable",
//                                ContactLockedActivity.this.getPackageName());
//                tv.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
//                Typeface t = Typeface.createFromAsset(getAssets(),
//                        "fonts/MuseoSans_500.otf");
//                tv.setTypeface(t);
//                tv.setTextSize((float) 20);
//                int dp5 = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
//                tv.setCompoundDrawablePadding(dp5);
//
//                return v;
//            }
//        };
//
//        myDialogContact = new AlertDialog.Builder(this).setAdapter(adapter,
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        ContactObject object = myAdapter
//                                .getItem(myPositionChange).first;
//                        switch (which) {
//                            case 0:
//                                if (object.getAddress() != null
//                                        && object.getAddress().length() > 0) {
//                                    Intent callIntent = new Intent(
//                                            Intent.ACTION_CALL);
//                                    callIntent.setData(Uri.parse("tel:"
//                                            + object.getAddress()));
//                                    startActivity(callIntent);
//                                }
//                                break;
//                            case 1:
//                                Uri smsUri = Uri.parse("sms:" + object.getAddress());
//                                Intent intent = new Intent(Intent.ACTION_VIEW,
//                                        smsUri);
//                                intent.putExtra("sms_body", "");
//                                startActivity(intent);
//                                break;
//                            case 2:
//                                new ASynChangeTypeContact().execute();
//                                break;
//                            case 3:
//                                showDiaLogDeleteItems();
//                                break;
//                            default:
//                                break;
//                        }
//                        dialog.dismiss();
//                    }
//                });
//        myTvTitleContact = new TextView(this);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                (int) LayoutParams.WRAP_CONTENT,
//                (int) LayoutParams.WRAP_CONTENT);
//        params.leftMargin = 50;
//        myTvTitleContact.setTextSize((float) 22);
//        myTvTitleContact.setPadding(20, 20, 20, 20);
//        myTvTitleContact.setLayoutParams(params);
//        Typeface t = Typeface.createFromAsset(getAssets(),
//                "fonts/MuseoSans_700.otf");
//        myTvTitleContact.setTypeface(t);
//        myTvTitleContact.setText("Actions");
//
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mViewBack.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);

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

            myAdapter
                    .setOnItemClickListener(new ContactLockedAdapter.OnClickItemListener() {

                        @Override
                        public void onClick(int position) {
                            myContact = myAdapter.getItem(position).first;
                            Intent i = new Intent(getActivity(),
                                    DetailContactLockedActivity.class);
                            i.putExtra("CONTACT", myContact);
                            startActivityForResult(i, RQ_DETAIL);
                        }
                    });

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
            case R.id.btn_add:
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
