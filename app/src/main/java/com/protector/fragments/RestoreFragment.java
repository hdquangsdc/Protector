package com.protector.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
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

import com.gc.materialdesign.views.CheckBox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.protector.AppPreference;
import com.protector.R;
import com.protector.backup.GoogleDriveBackupTask;
import com.protector.backup.GoogleDriveRestoreTask;
import com.protector.database.AppContentProvider;
import com.protector.database.AppTableAdapter;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.SmsCallLogContentProvider;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.database.VideoTableAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ho on 5/7/2015.
 */
public class RestoreFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static int RESTORE_FINISH = -1;
    public static int RESTORE_CANCEL = -2;
    public static int RESTORE_ERROR = -3;
    public static int RESTORE_COPIED = -4;

    private static final int REQUEST_LINK_TO_DBX = 0;
    static final int REQUEST_CODE_RESOLUTION = 2;


    private Button myBtnRestore;
    private CheckBox myCbAll, myCbSms, myCbApp, myCbVideo, myCbImage;
    private View mViewBack;

    private GoogleApiClient mGoogleApiClient;

    public int TYPE_BACKUP;
    private Dialog myDialogPassword;
    private EditText myEdtPassword;
    private Button myBtnPassaword;
    boolean isOnback;
    BroadcastReceiver receiver;
    private boolean myIsRestore;

    private ArrayList<Pair<File, Integer>> myFiles;

//    private String myCurrentPasswordDRopbox, myCurrentPasswordGDrive;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restore, container,
                false);
        myBtnRestore = (Button) rootView.findViewById(R.id.btn_backup);
        myCbAll = (CheckBox) rootView.findViewById(R.id.cb_all);
        myCbSms = (CheckBox) rootView.findViewById(R.id.cb_sms);
        myCbApp = (CheckBox) rootView.findViewById(R.id.cb_app);
        myCbVideo = (CheckBox) rootView.findViewById(R.id.cb_video);
        myCbImage = (CheckBox) rootView.findViewById(R.id.cb_image);


        mViewBack = rootView.findViewById(R.id.view_back);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mViewBack.setOnClickListener(this);

        myBtnRestore.setOnClickListener(this);


        myCbAll.setOncheckListener(new CheckBox.OnCheckListener() {
            @Override
            public void onCheck(boolean b) {
                if (b) {
                    myCbApp.setChecked(true);
                    myCbImage.setChecked(true);
                    myCbVideo.setChecked(true);
                    myCbSms.setChecked(true);
                }
            }
        });

        CheckBox.OnCheckListener onCheckListener = new CheckBox.OnCheckListener() {
            @Override
            public void onCheck(boolean b) {
                if (!b) {
                    myCbAll.setChecked(false);
                }
            }
        };
        myCbVideo.setOncheckListener(onCheckListener);
        myCbApp.setOncheckListener(onCheckListener);
        myCbSms.setOncheckListener(onCheckListener);
        myCbImage.setOncheckListener(onCheckListener);


        initComponents();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.cb_all:

                break;

            case R.id.btn_ok:

                if (TYPE_BACKUP == 0) {
//                        if (myEdtPassword.getText().toString().trim()
//                                .equals(myCurrentPasswordDRopbox)) {
//                            new DropboxRestoreTask(RestoreActivity.this, myFiles)
//                                    .execute();
//                            myEdtPassword.setText("");
//                            myDialogPassword.dismiss();
//                            try {
//                                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                                imm.toggleSoftInput(
//                                        InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//                            } catch (Exception ex) {
//
//                            }
//                        } else {
//                            myEdtPassword.setText("");
//                            Toast.makeText(this,
//                                    getString(R.string.txt_password_incorrect),
//                                    Toast.LENGTH_SHORT).show();
//                        }
                } else {

                    new GoogleDriveRestoreTask(getActivity(),
                            myFiles).execute();
                    myEdtPassword.setText("");
                    myDialogPassword.dismiss();
                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(
                                InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    } catch (Exception ex) {

                    }

                }

                break;
            case R.id.btn_backup:
                doRestore();
                break;
        }
    }

    public void doRestore() {
        myFiles = new ArrayList<Pair<File, Integer>>();
        String folder = AppPreference.getInstance(getActivity()).getHideRootPath()
                + "/TmpData";
        File f;
        if (myCbSms.isCheck()) {
            f = new File(folder + "/"
                    + SmsCallLogContentProvider.DATABASE_NAME);
            myFiles.add(new Pair<File, Integer>(f, 0));
        }
        if (myCbApp.isCheck()) {
            f = new File(folder + "/" + AppContentProvider.DATABASE_NAME);
            myFiles.add(new Pair<File, Integer>(f, 0));
        }
        if (myCbVideo.isCheck()) {
            f = new File(AppPreference.getInstance(getActivity()).getHideVideoRootPath());
            if (f.exists()) {
                myFiles.add(new Pair<File, Integer>(f, 1));
            }

        }
        if (myCbImage.isCheck()) {
            f = new File(AppPreference.getInstance(getActivity()).getHideImageRootPath());
            if (f.exists()) {
                myFiles.add(new Pair<File, Integer>(f, 1));
            }
        }
        if (myFiles.size() > 0) {
            myDialog.show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_item_selected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
//        if (myCurrentPasswordGDrive == null) {
//            new GetPasswordRestoreGDriveControl(RestoreActivity.this,
//                    RestoreActivity.this).execute();
//        } else {
        myEdtPassword.setText("");
        myDialogPassword.show();
//        }
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
            return;
        }
        try {
            if (!isOnback) {
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
                isOnback = true;
            } else {
                isOnback = false;
            }
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LINK_TO_DBX) {
//            if (resultCode == Activity.RESULT_OK) {
//                if (myCurrentPasswordDRopbox == null) {
//                    new GetPasswordRestoreDBControl(RestoreActivity.this,
//                            RestoreActivity.this).execute();
//                } else {
//                    myEdtPassword.setText("");
//                    myDialogPassword.show();
//                }
//            } else {
//                Toast.makeText(this,
//                        getString(R.string.txt_link_dropbox_cancel),
//                        Toast.LENGTH_SHORT).show();
//            }
        } else if (requestCode == REQUEST_CODE_RESOLUTION) {
            mGoogleApiClient.connect();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private ProgressDialog myDialogWating;
    private ProgressDialog mDialog, myDialogRestoring, myDialogClearData,
            myDialogCopyData;
    private AlertDialog.Builder myDialog;

    public void initComponents() {

        initDialogPW();
        myDialogWating = new ProgressDialog(getActivity());
        myDialogWating.setMessage("");
        myDialogWating.setCancelable(false);
        // Copy Data
        myDialogCopyData = new ProgressDialog(getActivity());
        myDialogCopyData.setCancelable(false);
        myDialogCopyData.setMessage(this.getString(R.string.copying_data));
        // Dialog restoring
        myDialogRestoring = new ProgressDialog(getActivity());
        myDialogRestoring.setCancelable(false);
        myDialogRestoring.setMessage(getString(R.string.getting_data));
        myDialogRestoring.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.btn_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopAllSyncTask();
                        myDialogRestoring.dismiss();
                        AppPreference.getInstance(getActivity()).setDriveRestore(false);

                        SystemClock.sleep(500);
                        new DeleteAllAsynTask().execute();
                    }
                });

        myDialogClearData = new ProgressDialog(getActivity());
        myDialogClearData.setCancelable(false);
        myDialogClearData.setMessage(getString(R.string.cleaning_data));
        if (AppPreference.getInstance(getActivity()).isDriveRestore()) {
            myDialogRestoring
                    .setMessage(getString(R.string.synchronizing_data));
            myIsRestore = true;
            myDialogRestoring.show();
        }
        // Dialog Restore
        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.connecting));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMax(100);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        stopAllSyncTask();
                        mDialog.dismiss();
                        AppPreference.getInstance(getActivity())
                                .setDriveRestore(false);
                        SystemClock.sleep(500);
                        new DeleteAllAsynTask().execute();
                    }
                });

        IntentFilter filter = new IntentFilter();
        filter.addAction("BACKUP.DATA");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // do something based on the intent's action
                String message = intent.getStringExtra("MESSAGE");
                int progress = intent.getIntExtra("PROGRESS", 0);
                if (myDialogRestoring.isShowing())
                    myDialogRestoring.dismiss();
                if (mDialog == null) {
                    mDialog = new ProgressDialog(getActivity());
                    mDialog.setCancelable(false);
                    mDialog.setMessage(getString(R.string.connecting));
                    mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mDialog.setMax(100);
                    return;
                }
                try {
                    if (progress == RESTORE_FINISH) {
                        mDialog.dismiss();
                        myDialogCopyData.show();
                        return;
                    } else if (progress == RESTORE_COPIED) {
                        myCbAll.setChecked(false);
                        myCbApp.setChecked(false);
                        myCbImage.setChecked(false);
                        myCbSms.setChecked(false);
                        myCbVideo.setChecked(false);
                        myBtnRestore.setEnabled(false);
                        myDialogCopyData.dismiss();
                    } else if (progress == RESTORE_ERROR) {
                        mDialog.dismiss();
                        myDialogRestoring.dismiss();
                        AppPreference.getInstance(getActivity()).setDriveRestore(false);

                        new DeleteAllAsynTask().execute();
                        return;
                    } else {
                        if (!mDialog.isShowing() && myIsRestore)
                            mDialog.show();
                        mDialog.setProgress(progress);
                        if (!message.equals(getString(R.string.connecting))) {
                            mDialog.setMessage(Html.fromHtml(message + "("
                                    + mDialog.getProgress() + "%)..."));
                        } else {
                            mDialog.setMessage(message);
                            // mDialog.setMessage("Syncing data from DBox/GDrive to your device. Please wating...");
                        }

                    }
                } catch (Exception e) {
                }
            }
        };
        getActivity().registerReceiver(receiver, filter);


//        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
//                appKey, appSecret);
        initDialog();
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        try {
//            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
//                    "XYTEST");
//            mWakeLock.acquire();
//        } catch (Exception ex) {
//        }
        //new asynCheckData().execute();

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
                        myIsRestore = true;
                        switch (TYPE_BACKUP) {
                            case 0:
//                                if (mDbxAcctMgr.hasLinkedAccount()) {
//                                    if (myCurrentPasswordDRopbox == null) {
//                                        new GetPasswordRestoreDBControl(
//                                                RestoreActivity.this,
//                                                RestoreActivity.this).execute();
//                                    } else {
//                                        myEdtPassword.setText("");
//                                        myDialogPassword.show();
//                                    }
//								/*
//								 * new DropboxRestoreTask(RestoreActivity.this,
//								 * myFiles).execute();
//								 */
//                                } else {
//                                    mDbxAcctMgr.startLink(
//                                            (Activity) RestoreActivity.this,
//                                            REQUEST_LINK_TO_DBX);
//                                }
                                break;
                            case 1:
                                if (mGoogleApiClient == null) {
                                    mGoogleApiClient = new GoogleApiClient.Builder(
                                            getActivity())
                                            .addApi(Drive.API)
                                            .addScope(Drive.SCOPE_FILE)
                                            .addScope(Drive.SCOPE_APPFOLDER)
                                            .addConnectionCallbacks(RestoreFragment.this)
                                            .addOnConnectionFailedListener(
                                                    RestoreFragment.this).build();
                                }
                                if (mGoogleApiClient.isConnected()) {
                                    isOnback = false;
//                                    if (myCurrentPasswordGDrive == null) {
//                                        new GetPasswordRestoreGDriveControl(
//                                                RestoreActivity.this,
//                                                RestoreActivity.this).execute();
//                                    } else {
                                    myEdtPassword.setText("");
                                    myDialogPassword.show();
//                                    }

                                } else {
                                    isOnback = false;
                                    mGoogleApiClient.connect();
                                }
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        myDialog.setTitle(getString(R.string.restore));
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
                R.layout.view_edittext_dialog, null);
        ((TextView) view.findViewById(R.id.tv_title))
                .setText(R.string.enter_backup_pass);
        myEdtPassword = (EditText) view.findViewById(R.id.edt_password);
        myEdtPassword.setHint(getString(R.string.backup_password));
        myBtnPassaword = (Button) view.findViewById(R.id.btn_ok);
        myBtnPassaword.setOnClickListener(this);
        return view;
    }

    public void stopAllSyncTask() {
        SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.d("map values", entry.getKey() + ": "
                    + entry.getValue().toString());
            try {
                int id = Integer.parseInt(entry.getKey());
                edit.putBoolean(id + "", false);
            } catch (Exception ex) {
            }
        }
        edit.commit();
    }

    public class DeleteAllAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if (!myDialogClearData.isShowing()) {
                myDialogClearData.show();
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            File images = new File(AppPreference
                    .getInstance(getActivity()).getHideRootPath()
                    + "/TmpDataMedia" + "/.images");
            deleteDirectory(images);
            File video = new File(AppPreference.getInstance(getActivity())
                    .getHideRootPath() + "/TmpDataMedia" + "/.videos");
            deleteDirectory(video);
            File f = new File(AppPreference.getInstance(getActivity())
                    .getHideRootPath() + "/TmpData");
            deleteDirectory(f);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            myDialogClearData.dismiss();
        }
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return true;
    }


}
