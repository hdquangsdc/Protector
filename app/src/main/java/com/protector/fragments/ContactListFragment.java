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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.protector.R;
import com.protector.adapters.ContactAdapter;
import com.protector.adapters.SmsAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.SmsLocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactListFragment extends Fragment implements View.OnClickListener {
    private ProgressDialog myProgressDialog;
    private ArrayList<ContactItem> myArrayContact, myArrayContactPhone;
    ;
    private ContactAdapter myAdapter;
    private EditText myEdtSearch;
    private TextView tvDone;
    private IPick listener;
    private int myType;

    ListView myLv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact_list, container,
                false);
        myLv = (ListView) rootView.findViewById(R.id.list_message);
        tvDone = (TextView) rootView.findViewById(R.id.tv_done);


        myEdtSearch = (EditText) rootView.findViewById(R.id.edt_search);
        myEdtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                new SynSearch().execute();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        myEdtSearch
//                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//                    @Override
//                    public boolean onEditorAction(TextView v, int actionId,
//                                                  KeyEvent event) {
//                        // TODO Auto-generated method stub
//                        if (actionId == EditorInfo.IME_ACTION_DONE) {
//                            if (myAdapter.getArrayChecks().size() > 0) {
//                                new SynPrivateContact().execute();
//                            } else {
//                                Toast.makeText(
//                                        ListContactActivity.this,
//                                        getString(R.string.txt_no_item_selected),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        return false;
//                    }
//                });

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

        myArrayContact = new ArrayList<>();
        myArrayContactPhone = new ArrayList<>();
        new SynContact().execute();
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
            default:
                break;
        }
    }


    public void setOnPickListener(IPick listener) {
        this.listener = listener;
    }

    public class SynContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
//            if (myType == PrivateContactTableAdapter.TYPE_PRIVATE) {
//                getAllContactForPrivateContact();
//            } else {
            getAllContact();
//            }
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
            } catch (final IllegalArgumentException e) {
            } catch (final Exception e) {
            } finally {

            }
            myAdapter = new ContactAdapter(getActivity(),
                    myArrayContact, myType);
            myLv.setAdapter(myAdapter);
//            myEdtSearch.setHint(getString(R.string.txt_hint_search,
//                    myArrayContactPhone.size()));
//            if (myArrayContact.size() == 0) {
//                showDialogNotMessage();
//            }
        }
    }

    public void getAllContact() {
        SmsLocker thread = SmsLocker
                .getInstance(getActivity());
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        ContactItem object = null;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                try {
                    object = new ContactItem();
                    int id = cur.getInt(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    object.setId(id);
                    object.setName(cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    int photoId = cur
                            .getInt(cur
                                    .getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    byte[] photoBytes = null;

                    Uri photoUri = ContentUris.withAppendedId(
                            ContactsContract.Data.CONTENT_URI, photoId);

                    Cursor c = cr
                            .query(photoUri,
                                    new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                                    null, null, null);

                    try {
                        if (c.moveToFirst())
                            photoBytes = c.getBlob(0);

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();

                    } finally {

                        c.close();
                    }

                    object.setAvatarByte(photoBytes);
                    ArrayList<String> arrayAddress = new ArrayList<String>();
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = ?", new String[]{id
                                                + ""}, null);
                        while (pCur.moveToNext()) {
                            arrayAddress
                                    .add(pCur
                                            .getString(
                                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            .replace(" ", "").replace("-", ""));
                        }
                        pCur.close();
                    }

                    if (arrayAddress.size() > 0) {
                        if (arrayAddress.size() == 1) {
                            object.setAddress(arrayAddress.get(0));
                            object.setPhoneOther(new ArrayList<String>());
                            myArrayContact.add(object);
                            myArrayContactPhone.add(object);
                        } else {
                            for (String item : arrayAddress) {
                                ContactItem ob = new ContactItem();
                                ob.compareContact(object);
                                ob.setAddress(item);
                                ArrayList<String> arrs = new ArrayList<String>();
                                for (String string : arrayAddress) {
                                    if (!string.equals(item)) {
                                        arrs.add(string);
                                    }
                                }
                                ob.setPhoneOther(arrs);
                                myArrayContact.add(ob);
                                myArrayContactPhone.add(ob);
                            }
                        }
                    } else {
                        object.setAddress("");
                        myArrayContact.add(object);
                        myArrayContactPhone.add(object);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(myArrayContact, new Comparator<ContactItem>() {

            @Override
            public int compare(ContactItem lhs, ContactItem rhs) {
                if (lhs.getName() == null || rhs.getName() == null) {
                    return -1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }

        });
        Collections.sort(myArrayContactPhone, new Comparator<ContactItem>() {

            @Override
            public int compare(ContactItem lhs, ContactItem rhs) {
                if (lhs.getName() == null || rhs.getName() == null) {
                    return -1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }

        });
    }

    public void getAllContactForPrivateContact() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        ContactItem object = null;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                try {
                    object = new ContactItem();
                    int id = cur.getInt(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    object.setId(id);
                    object.setName(cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    int photoId = cur
                            .getInt(cur
                                    .getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    byte[] photoBytes = null;

                    Uri photoUri = ContentUris.withAppendedId(
                            ContactsContract.Data.CONTENT_URI, photoId);

                    Cursor c = cr
                            .query(photoUri,
                                    new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                                    null, null, null);

                    try {
                        if (c.moveToFirst())
                            photoBytes = c.getBlob(0);

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();

                    } finally {

                        c.close();
                    }

                    object.setAvatarByte(photoBytes);
                    ArrayList<String> arrayAddress = new ArrayList<String>();
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = ?", new String[]{id
                                                + ""}, null);
                        while (pCur.moveToNext()) {
                            arrayAddress
                                    .add(pCur
                                            .getString(
                                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            .replace(" ", "").replace("-", ""));
                        }
                        pCur.close();
                    }

                    if (arrayAddress.size() > 0) {
                        if (arrayAddress.size() == 1) {
                            object.setAddress(arrayAddress.get(0));
                            object.setPhoneOther(new ArrayList<String>());
                            myArrayContact.add(object);
                            myArrayContactPhone.add(object);
                        } else {
                            for (String item : arrayAddress) {
                                ContactItem ob = new ContactItem();
                                ob.compareContact(object);
                                ob.setAddress(item);
                                ArrayList<String> arrs = new ArrayList<String>();
                                for (String string : arrayAddress) {
                                    if (!string.equals(item)) {
                                        arrs.add(string);
                                    }
                                }
                                ob.setPhoneOther(arrs);
                                myArrayContact.add(ob);
                                myArrayContactPhone.add(ob);
                            }
                        }
                    } else {
                        object.setAddress("");
                        myArrayContact.add(object);
                        myArrayContactPhone.add(object);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        Collections.sort(myArrayContact, new Comparator<ContactItem>() {

            @Override
            public int compare(ContactItem lhs, ContactItem rhs) {
                if (lhs.getName() == null || rhs.getName() == null) {
                    return -1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }

        });
        Collections.sort(myArrayContactPhone, new Comparator<ContactItem>() {

            @Override
            public int compare(ContactItem lhs, ContactItem rhs) {
                if (lhs.getName() == null || rhs.getName() == null) {
                    return -1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }

        });
    }

    public class SynSearch extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getSearchContact();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
			/*
			 * myAdapter = new ContactAdapter(ListContactActivity.this,
			 * myArrayContact); myLv.setAdapter(myAdapter);
			 */
            myAdapter.notifyDataSetChanged();
            myAdapter.clearChecked();
//            resetButtonNext();
        }
    }

    public void getSearchContact() {
        myArrayContact.clear();
        for (ContactItem item : myArrayContactPhone) {
            if (item.getName().toUpperCase()
                    .contains(myEdtSearch.getText().toString().toUpperCase())) {
                myArrayContact.add(item);
            }
        }
    }
}
