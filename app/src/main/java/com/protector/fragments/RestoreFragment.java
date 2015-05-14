package com.protector.fragments;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gc.materialdesign.views.CheckBox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.protector.AppPreference;
import com.protector.R;
import com.protector.backup.GoogleDriveBackupTask;
import com.protector.database.AppContentProvider;
import com.protector.database.AppTableAdapter;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.SmsCallLogContentProvider;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.database.VideoTableAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ho on 5/7/2015.
 */
public class RestoreFragment extends Fragment  {
    public static int RESTORE_FINISH = -1;
    public static int RESTORE_CANCEL = -2;
    public static int RESTORE_ERROR = -3;
    public static int RESTORE_COPIED = -4;
}
