package com.protector.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.gc.materialdesign.views.CheckBox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.protector.AppPreference;
import com.protector.R;
import com.protector.backup.DropboxBackupTask;
import com.protector.backup.GoogleDriveBackupTask;
import com.protector.database.AppContentProvider;
import com.protector.database.AppTableAdapter;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.SmsCallLogContentProvider;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.database.VideoTableAdapter;
import com.protector.utils.PhoneMemoryUtils;
import com.protector.views.CustomDialog;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by Ho on 5/7/2015.
 */
public class BackupFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private Button myBtnBackup;
    private CheckBox myCbAll, myCbSms, myCbApp, myCbVideo, myCbImage;
    private ArrayList<File> myFiles;
    private boolean isBackup;
    private String myPassword;
    static final int REQUEST_CODE_RESOLUTION = 2;
    public static final String PHOTO_SIZE = "file_photo_size.txt";
    public static final String VIDEO_SIZE = "file_video_size.txt";
    private GoogleApiClient mGoogleApiClient;
    private AlertDialog.Builder myDialog;
    public int TYPE_BACKUP;
    private EditText myEdtPassword, myEdtConfirmPass;
    private Button myBtnPassaword;
    private Dialog myDialogPassword;
    private static final int REQUEST_LINK_TO_DBX = 1212;

    private static final String appKey = "r1nf045h3z6u88d";
    private static final String appSecret = "0gv1e3znv60lgs3";

    private DbxAccountManager mDbxAcctMgr;

    private DecimalFormat fileSizeFormater = new DecimalFormat("##0.##");
//    private DbxAccountManager mDbxAcctMgr;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mDbxAcctMgr = DbxAccountManager.getInstance(getActivity().getApplicationContext(),
//                appKey, appSecret);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_backup, container,
                false);
        myBtnBackup = (Button) rootView.findViewById(R.id.btn_backup);
        myCbAll = (CheckBox) rootView.findViewById(R.id.cb_all);
        myCbSms = (CheckBox) rootView.findViewById(R.id.cb_sms);
        myCbApp = (CheckBox) rootView.findViewById(R.id.cb_app);
        myCbVideo = (CheckBox) rootView.findViewById(R.id.cb_video);
        myCbImage = (CheckBox) rootView.findViewById(R.id.cb_image);

        myBtnBackup.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDialog();
        initDialogPW();
        new asynCheckData().execute();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_backup:
                doBackUp();
            case R.id.btn_ok:
                isBackup = false;
//                if (myEdtPassword.getText().toString().length() == 0) {
//                    Toast.makeText(this, getString(R.string.txt_pw1_not_empty),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (myEdtPassword.getText().toString().length() < 4) {
//                    Toast.makeText(this, getString(R.string.txt_pass_least_4),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (myEdtPassword.getText().toString().length() > 15) {
//                    Toast.makeText(this, getString(R.string.txt_password_15),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (myEdtConfirmPass.getText().toString().length() == 0) {
//                    Toast.makeText(this, getString(R.string.txt_pw1_not_empty),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (myEdtConfirmPass.getText().toString().length() < 4) {
//                    Toast.makeText(this, getString(R.string.txt_pass_least_4),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (myEdtConfirmPass.getText().toString().length() > 15) {
//                    Toast.makeText(this, getString(R.string.txt_password_15),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (!myEdtPassword.getText().toString()
//                        .equals(myEdtConfirmPass.getText().toString())) {
//                    Toast.makeText(this, getString(R.string.txt_re_password_match),
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if (myEdtPassword.getText().toString()
                        .equals(myEdtConfirmPass.getText().toString())) {
                    myPassword = myEdtPassword.getText().toString();
                    myEdtPassword.setText("");
                    myEdtConfirmPass.setText("");
                    myDialogPassword.dismiss();
                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
                                0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    switch (TYPE_BACKUP) {
                        case 0:
                            if (AppPreference.getInstance(getActivity())
                                    .isDropboxBackup()) {
						/*
						 * Toast.makeText(BackupActivty.this,
						 * "Backing up by Dropbox", Toast.LENGTH_SHORT).show();
						 */
                                showDialogMessage(getString(R.string.dropbox_backing_up));
                            } else {
//                                if (mDbxAcctMgr.hasLinkedAccount()) {
//                                    new DropboxBackupTask(getActivity().getApplicationContext(),
//                                            myFiles, myPassword).execute();
//                                } else {
//                                    mDbxAcctMgr.startLink(
//                                            (Activity) BackupActivty.this,
//                                            REQUEST_LINK_TO_DBX);
//                                }
                            }
                            break;
                        case 1:
                            if (AppPreference.getInstance(getActivity())
                                    .getDriveBackup()) {
						/*
						 * Toast.makeText(BackupActivty.this,
						 * "Backing up by GoogleDrive",
						 * Toast.LENGTH_SHORT).show();
						 */
                                showDialogMessage(getString(R.string.drive_backing_up));
                            } else {
                                if (mGoogleApiClient == null) {
                                    mGoogleApiClient = new GoogleApiClient.Builder(
                                            getActivity())
                                            .addApi(Drive.API)
                                            .addScope(Drive.SCOPE_FILE)
                                            .addScope(Drive.SCOPE_APPFOLDER)
                                            .addConnectionCallbacks(this)
                                            .addOnConnectionFailedListener(
                                                    this).build();
                                }
                                if (mGoogleApiClient.isConnected()) {
                                    new GoogleDriveBackupTask(getActivity(),
                                            myFiles, myPassword).execute();
                                } else {
                                    mGoogleApiClient.connect();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    myEdtPassword.setText("");
                    myEdtConfirmPass.setText("");
                    Toast.makeText(getActivity(),
                            getString(R.string.incorrect_password),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        isBackup = true;
        new GoogleDriveBackupTask(getActivity(), myFiles, myPassword)
                .execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(),
                    0).show();
            isBackup = false;
            return;
        }
        try {
            if (!isBackup) {
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
                isBackup = true;
            }

        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public class asynCheckData extends AsyncTask<Void, Void, Void> {
        boolean isApp, isSms, isVideo, isPhoto;

        @Override
        protected Void doInBackground(Void... params) {
            isApp = AppTableAdapter.getInstance(getActivity())
                    .checkData();
            isSms = SmsCallLogTableAdapter.getInstance(getActivity())
                    .checkData();
            isVideo = VideoTableAdapter.getInstance(getActivity())
                    .checkData();
            isPhoto = PhotoTableAdapter.getInstance(getActivity())
                    .checkData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!isApp) {
                myCbApp.setClickable(false);
//                myCbApp.setTextColor(getResources().getColor(
//                        R.color.color_text_checkbox));
            } else {
//                myCbApp.setTextColor(Color.BLACK);
            }

            if (!isSms) {
                myCbSms.setClickable(false);
//                myCbSms.setTextColor(getResources().getColor(
//                        R.color.color_text_checkbox));
            } else {
//                myCbSms.setTextColor(Color.BLACK);
            }

            if (!isVideo) {
                myCbVideo.setClickable(false);
//                myCbVideo.setTextColor(getResources().getColor(
//                        R.color.color_text_checkbox));
            } else {
//                myCbVideo.setTextColor(Color.BLACK);
            }

            if (!isPhoto) {
                myCbImage.setClickable(false);
//                myCbImage.setTextColor(getResources().getColor(
//                        R.color.color_text_checkbox));
            } else {
//                myCbImage.setTextColor(Color.BLACK);
            }

            if (!isApp && !isSms && !isPhoto && !isVideo) {
                myCbAll.setClickable(false);
//                myCbAll.setTextColor(getResources().getColor(
//                        R.color.color_text_checkbox));
            } else {
//                myCbAll.setTextColor(Color.BLACK);
            }
        }

    }

    public void doBackUp() {
        myFiles = new ArrayList<>();
        String folder = AppPreference.getInstance(getActivity()).getHideRootPath();
        File f;

        if (myCbSms.isCheck()) {
            f = new File(folder + "/"
                    + SmsCallLogContentProvider.DATABASE_NAME);
            myFiles.add(f);
        }
        if (myCbApp.isCheck()) {
            f = new File(folder + "/" + AppContentProvider.DATABASE_NAME);
            myFiles.add(f);
        }
        if (myCbVideo.isCheck()) {
            f = new File(AppPreference.getInstance(getActivity()).getHideVideoRootPath());
            if (f.exists()) {
                myFiles.add(f);
            }

        }
        if (myCbImage.isCheck()) {
            f = new File(AppPreference.getInstance(getActivity()).getHideImageRootPath());
            if (f.exists()) {
                myFiles.add(f);
            }

        }
        if (myFiles.size() > 0) {
            long dataSize = getSizeDataBackup();
            long freeSpace = PhoneMemoryUtils.getAvailableInternalMemorySize();
            if (dataSize < freeSpace) {
                myDialog.show();
            } else {
                showDialogFullInternalMemory(dataSize, freeSpace);
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_item_selected),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void showDialogFullInternalMemory(long dataSize, long freeSpace) {
        CustomDialog dialog = new CustomDialog(getActivity(),
                getString(R.string.alert), getString(
                R.string.internal_memory_full,
                formatSize(freeSpace), formatSize(dataSize)));
        dialog.setOnClickOk(new CustomDialog.OnClickOk() {

            @Override
            public void Click() {
                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            }
        });
        dialog.show();
        dialog.setTextBtn(getString(R.string.btn_ok),
                getString(R.string.btn_cancel));
    }

    private String formatSize(long fileSizeInByte) {
        double nb = fileSizeInByte * 1.0 / (1024 * 1024 * 1024);
        if (nb >= 1.0) {
            return fileSizeFormater.format(nb) + " GB";
        }
        nb = fileSizeInByte * 1.0 / (1024 * 1024);
        if (nb >= 1.0) {
            return fileSizeFormater.format(nb) + " MB";
        } else
            return fileSizeFormater.format(fileSizeInByte * 1.0 / 1024) + " KB";
    }

    public long getSizeDataBackup() {
        long size = 0;
        for (File file : myFiles) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += folderSize(file);
            }
        }
        return size;
    }

    public long folderSize(File directory) {
        long length = 0;
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        }
        return length;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
//                if (mDbxAcctMgr.hasLinkedAccount()) {
//                    new DropboxBackupTask(getActivity(), myFiles,
//                            myPassword).execute();
//                }
            } else {
                Toast.makeText(getActivity(),
                        getString(R.string.link_dropbox_cancel),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_RESOLUTION) {
            mGoogleApiClient.connect();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public DriveId getDriveId(GoogleApiClient client, String title,
                              String mime, DriveFolder fldr) {
        DriveId dId = null;
        ArrayList<Filter> fltrs = new ArrayList<Filter>();
        fltrs.add(Filters.eq(SearchableField.TRASHED, false));
        if (title != null)
            fltrs.add(Filters.eq(SearchableField.TITLE, title));
        if (mime != null)
            fltrs.add(Filters.eq(SearchableField.MIME_TYPE, mime));
        Query qry = new Query.Builder().addFilter(Filters.and(fltrs)).build();
        DriveApi.MetadataBufferResult rslt = (fldr == null) ? Drive.DriveApi.query(
                client, qry).await() : fldr.queryChildren(client, qry).await();
        if (rslt.getStatus().isSuccess()) {
            MetadataBuffer mdb = null;
            try {
                mdb = rslt.getMetadataBuffer();
                if (mdb != null) {
                    for (Metadata md : mdb) {
                        if (md == null)
                            continue;
                        dId = md.getDriveId(); // here is the "Drive ID"

                    }
                }
            } finally {
                if (mdb != null)
                    mdb.close();
            }
        }
        return dId;
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

    public void initDialog() {
        String[] title = getResources().getStringArray(R.array.arr_backup);
        String[] icon = getResources().getStringArray(R.array.arr_backup_icon);
        final Item[] items = new Item[title.length];
        for (int i = 0; i < title.length; i++) {
            items[i] = new Item(title[i], icon[i]);
        }
        ListAdapter adapter = new ArrayAdapter<Item>(getActivity(),
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                int resId = getResources().getIdentifier(
                        items[position].icon, "drawable",
                        getActivity().getPackageName());
                tv.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
                Typeface t = Typeface.createFromAsset(getActivity().getAssets(),
                        "fonts/roboto_regular.ttf");
                tv.setTypeface(t);
                tv.setTextSize((float) 20);
                int dp5 = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);
                return v;
            }
        };
        myDialog = new AlertDialog.Builder(getActivity()).setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TYPE_BACKUP = which;
                        myEdtPassword.setText("");
                        myEdtConfirmPass.setText("");
                        myEdtPassword.setFocusable(true);
                        myEdtPassword.setFocusableInTouchMode(true);
                        myEdtPassword.requestFocus();
                        myDialogPassword.show();
                        dialog.dismiss();
                    }
                });

        myDialog.setTitle(getActivity().getString(R.string.backup));
    }

    public void initDialogPW() {
        myDialogPassword = new Dialog(getActivity());
        myDialogPassword.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialogPassword.setContentView(getViewPW());
        Window window = myDialogPassword.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.FILL_PARENT;
        wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        myDialogPassword.getWindow().setAttributes(wlp);
    }

    public View getViewPW() {
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.view_password_bk_dialog, null);
        ((TextView) view.findViewById(R.id.tv_title))
                .setText(R.string.set_backup_password);
        myEdtConfirmPass = (EditText) view
                .findViewById(R.id.edt_confirm_password);
        myEdtPassword = (EditText) view.findViewById(R.id.edt_password);
        myBtnPassaword = (Button) view.findViewById(R.id.btn_ok);
        myBtnPassaword.setOnClickListener(this);
        return view;
    }

    public void showDialogMessage(String message) {
        CustomDialog myDialog = new CustomDialog(getActivity(),
                getString(R.string.message), message);
        myDialog.setOnClickOk(new CustomDialog.OnClickOk() {

            @Override
            public void Click() {
            }
        });
        myDialog.show();
        myDialog.setTextBtn1(getString(R.string.btn_ok));
        myDialog.goneBtn2();
        myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
            }
        });
    }
}
