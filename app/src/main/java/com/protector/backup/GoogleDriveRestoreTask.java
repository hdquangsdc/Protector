package com.protector.backup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
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
import com.protector.database.AppContentProvider;
import com.protector.database.PhotoContentProvider;
import com.protector.database.SmsCallLogContentProvider;
import com.protector.database.VideoContentProvider;
import com.protector.fragments.BackupFragment;
import com.protector.fragments.RestoreFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GoogleDriveRestoreTask extends AsyncTask<Void, Integer, String> {
    public static final String FOLDER_BACKUP = "Backup";
    public static final int BUFFER_SIZE = 2048;
    public static final String MIMEFLDR = "application/vnd.google-apps.folder";

    Activity mActivity;

    private int lastPercent;
    private GoogleApiClient mGoogleApiClient;
    private Metadata mFolderDriveMetadata = null;
    private DriveId mFolderDriveId;
    private CountDownLatch latch = new CountDownLatch(1);
    private long totalSize;
    private ArrayList<Pair<File, Integer>> mFiles;
    private String mMessage = "";
    private boolean myIsContinue, myIsContinueTimer;
    private int myAsyncTaskID;
    private Thread myThreadTimer;

    public GoogleDriveRestoreTask(Activity activity,
                                  ArrayList<Pair<File, Integer>> files) {
        mActivity = activity;
        myIsContinue = true;
        myIsContinueTimer = true;
        mFiles = files;
        lastPercent = -1;
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API).addScope(Drive.SCOPE_FILE);
        mGoogleApiClient = builder.build();

        // Random ID
        Random rand = new Random();
        myAsyncTaskID = rand.nextInt(99999);
        SharedPreferences pref = mActivity
                .getPreferences(Activity.MODE_PRIVATE);
        Editor edt = pref.edit();
        edt.putBoolean(myAsyncTaskID + "", true);
        edt.commit();

        myThreadTimer = new Thread(run);
        myThreadTimer.start();

    }

    public Runnable run = new Runnable() {

        @Override
        public void run() {
            SharedPreferences pref = mActivity
                    .getPreferences(Activity.MODE_PRIVATE);
            while (myIsContinueTimer) {
                SystemClock.sleep(200);
                myIsContinue = pref.getBoolean(myAsyncTaskID + "", true);
                if (!myIsContinue) {
                    return;
                }
            }
        }
    };

    @Override
    protected String doInBackground(Void... params) {
        try {
            mGoogleApiClient
                    .registerConnectionCallbacks(new ConnectionCallbacks() {
                        @Override
                        public void onConnectionSuspended(int cause) {
                        }

                        @Override
                        public void onConnected(Bundle arg0) {
                            latch.countDown();
                        }

                    });
            mGoogleApiClient
                    .registerConnectionFailedListener(new OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult arg0) {
                            latch.countDown();
                        }
                    });
            mGoogleApiClient.connect();
            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }
            if (!mGoogleApiClient.isConnected()) {
                return "";
            }
            SystemClock.sleep(5000);
            // Check BACK_UP folder exist
            mFolderDriveId = getDriveId(mGoogleApiClient, FOLDER_BACKUP,
                    MIMEFLDR, Drive.DriveApi.getRootFolder(mGoogleApiClient));
            if (mFolderDriveId != null) {
                DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
                        mFolderDriveId);
                MetadataBufferResult result = folder.listChildren(
                        mGoogleApiClient).await();
                if (result.getStatus().isSuccess()) {
                    for (Pair<File, Integer> item : mFiles) {
                        if (!myIsContinue) {
                            return null;
                        }
                        Metadata data = checkFile(item.first.getName());
                        if (data != null) {
                            mMessage = mActivity.getString(R.string.restoring_item) + " " + item.first.getName();
                            publishProgress(0);
                            lastPercent = -1;
                            mFolderDriveMetadata = data;
                            if (item.first.getPath().equals(
                                    AppPreference.getInstance(mActivity)
                                            .getHideImageRootPath())) {
                                totalSize = getSizeFile(BackupFragment.PHOTO_SIZE);
                            } else if (item.first.getPath().equals(
                                    AppPreference.getInstance(mActivity)
                                            .getHideVideoRootPath())) {
                                totalSize = getSizeFile(BackupFragment.VIDEO_SIZE);
                            }
                            if (totalSize < data.getFileSize()) {
                                totalSize = data.getFileSize();
                            }
                            String rs = restore(item.second);
                            if (rs != null && rs.equals(mActivity.getString(R.string.cancel))) {
                                return null;
                            }
                        }
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AppPreference.getInstance(mActivity).setDriveRestore(false);
        }
        return null;

    }

    public Metadata checkFile(String title) {
        if (mFolderDriveId != null) {
            DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
                    mFolderDriveId);
            MetadataBufferResult result = folder.listChildren(mGoogleApiClient)
                    .await();
            if (result.getStatus().isSuccess()) {
                for (int i = 0; i < result.getMetadataBuffer().getCount(); i++) {
                    Metadata data = result.getMetadataBuffer().get(i);
                    if (!data.isTrashed() && !data.isFolder()) {
                        String name = data.getTitle();
                        if (name.equals(title
                                + DropboxBackupTask.EXTENTION_BACKUP)) {
                            return data;
                        }
                    }

                }
            }

        }
        return null;
    }

    private String restore(int type) {
        try {
            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient,
                    mFolderDriveMetadata.getDriveId());
            DriveContentsResult contentsResult = driveFile.open(
                    mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return "";
            }
            File unZipFolder;
            if (type == 0) {
                /*
				 * unZipFolder = new File(Preferences.getInstance(mActivity)
				 * .getHideRootPath());
				 */
                unZipFolder = new File(AppPreference.getInstance(mActivity)
                        .getHideRootPath() + "/TmpData");
            } else {
				/*
				 * unZipFolder = new File(Preferences.getInstance(mActivity)
				 * .getHideRootPathV1());
				 */
                unZipFolder = new File(AppPreference.getInstance(mActivity)
                        .getHideRootPath() + "/TmpDataMedia");
            }
            if (!unZipFolder.exists()) {
                unZipFolder.mkdirs();
                unZipFolder.createNewFile();
            }
            if (unZipFolder.exists() && unZipFolder.isDirectory()) {
                ZipInputStream zin = new ZipInputStream(contentsResult
                        .getDriveContents().getInputStream());
                try {
                    ZipEntry ze = null;
                    int totalUnzip = 0;
                    while ((ze = zin.getNextEntry()) != null) {
                        File unzipFile = new File(unZipFolder, ze.getName());

                        if (ze.isDirectory()) {
                            if (!unzipFile.isDirectory()) {
                                unzipFile.mkdirs();
                            }
                        } else {
                            if (unzipFile.getAbsoluteFile().toString()
                                    .contains(".images")) {
                                File file1 = new File(AppPreference
                                        .getInstance(mActivity)
                                        .getHideRootPath()
                                        + "/TmpDataMedia/.images");
                                if (!file1.exists()) {
                                    file1.mkdirs();
                                    file1.createNewFile();
                                }
                            }
                            if (unzipFile.getAbsoluteFile().toString()
                                    .contains(".videos")) {
                                File file1 = new File(AppPreference
                                        .getInstance(mActivity)
                                        .getHideRootPath()
                                        + "/TmpDataMedia/.videos");
                                if (!file1.exists()) {
                                    file1.mkdirs();
                                    file1.createNewFile();
                                }
                            }
                            FileOutputStream fout = new FileOutputStream(
                                    unzipFile.getAbsolutePath(), false);
                            try {
                                final int BUFFER_SIZE = 4096;
                                int count = 0;
                                byte[] buffer = new byte[BUFFER_SIZE];
                                while ((count = zin
                                        .read(buffer, 0, BUFFER_SIZE)) != -1) {
                                    fout.write(buffer, 0, count);
                                    if (!myIsContinue) {
                                        zin.closeEntry();
                                        fout.close();
                                        return mActivity.getString(R.string.cancel);
                                    }
                                    totalUnzip += count;
                                    int percent = (int) (totalUnzip * 100.0f / totalSize);
                                    if (lastPercent != percent) {
                                        lastPercent = percent;
                                        publishProgress(percent);
                                    }
                                    //Log.d("Total", totalUnzip + "");
                                }
                                zin.closeEntry();
                            } finally {
                                fout.close();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.getMessage();
                } finally {
                    zin.close();
                }
            } else {
                return mActivity.getString(R.string.no_sdcard_available);
            }
            contentsResult.getDriveContents().discard(mGoogleApiClient);

        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    public long getSizeFile(String filename) {
        DriveContentsResult contentsResult;
        DriveFile drFile = checkFileText(filename);
        long size = 0;
        if (drFile != null) {
            contentsResult = drFile.open(mGoogleApiClient,
                    DriveFile.MODE_READ_ONLY, null).await();
            InputStream ios = contentsResult.getDriveContents().getInputStream();
            try {
                size = Long.parseLong(getString(ios));
            } catch (IOException e) {
            }
            try {
                ios.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public String getString(InputStream is) throws IOException {
        int ch;
        StringBuilder sb = new StringBuilder();
        while ((ch = is.read()) != -1)
            sb.append((char) ch);
        return sb.toString();
    }

    public DriveFile checkFileText(String title) {
        if (mFolderDriveId != null) {
            DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
                    mFolderDriveId);
            MetadataBufferResult result = folder.listChildren(mGoogleApiClient)
                    .await();
            if (!result.getStatus().isSuccess()) {

            } else {
                for (int i = 0; i < result.getMetadataBuffer().getCount(); i++) {
                    Metadata data = result.getMetadataBuffer().get(i);
                    if (!data.isTrashed() && !data.isFolder()) {
                        String name = data.getTitle();
                        if (name.equals(title)) {
                            DriveFile file = Drive.DriveApi.getFile(
                                    mGoogleApiClient, data.getDriveId());
                            return file;
                        }
                    }

                }
            }

        }
        return null;
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
        MetadataBufferResult rslt = (fldr == null) ? Drive.DriveApi.query(
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

    @Override
    protected void onPostExecute(String result) {
        AppPreference.getInstance(mActivity).setDriveRestore(false);
        myIsContinueTimer = false;
        myThreadTimer.interrupt();
        SharedPreferences pref = mActivity
                .getPreferences(Activity.MODE_PRIVATE);
        pref.edit().remove(myAsyncTaskID + "").commit();
        if (result == null) {
            if (myIsContinue) {
                Intent ii = new Intent();
                ii.putExtra("MESSAGE", mActivity.getString(R.string.finishing));
                Log.d("PROGRESS", "Finish");
                ii.putExtra("PROGRESS", RestoreFragment.RESTORE_FINISH);
                ii.setAction("BACKUP.DATA");
                mActivity.sendBroadcast(ii);
                SystemClock.sleep(200);
                new FileCopyAsyn().execute();
            }
        } else {
            if (result.length() > 0) {
                Intent ii = new Intent();
                ii.putExtra("MESSAGE", mActivity.getString(R.string.error));
                ii.putExtra("PROGRESS", RestoreFragment.RESTORE_ERROR);
                ii.setAction("BACKUP.DATA");
                mActivity.sendBroadcast(ii);
                Toast.makeText(mActivity, mActivity.getString(R.string.request_failed),
                        Toast.LENGTH_LONG).show();
            }
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mMessage.contains(".videos")) {
            mMessage = mActivity.getString(R.string.restoring_video);
        }
        if (mMessage.contains(".images")) {
            mMessage = mActivity.getString(R.string.restoring_image);
        }
        if (values[0] <= 99) {
            Intent ii = new Intent();
            ii.putExtra("MESSAGE", mMessage);
            ii.putExtra("PROGRESS", values[0]);
            ii.setAction("BACKUP.DATA");
            mActivity.sendBroadcast(ii);
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPreExecute() {
        AppPreference.getInstance(mActivity).setDriveRestore(true);
        Intent ii = new Intent();
        ii.putExtra("MESSAGE", mActivity.getString(R.string.connecting));
        ii.putExtra("PROGRESS", 0);
        ii.setAction("BACKUP.DATA");
        mActivity.sendBroadcast(ii);
        super.onPreExecute();
    }

    public class FileCopyAsyn extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            CopyVideos();
            CopyContact();
            CopyImage();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent ii = new Intent();
            ii.putExtra("MESSAGE", mActivity.getString(R.string.copied));
            ii.putExtra("PROGRESS", RestoreFragment.RESTORE_COPIED);
            ii.setAction("BACKUP.DATA");
            mActivity.sendBroadcast(ii);
            Toast.makeText(mActivity, mActivity.getString(R.string.restore_success), Toast.LENGTH_LONG)
                    .show();
            Toast.makeText(mActivity,
                    mActivity.getString(R.string.restore_success),
                    Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }

    public void CopyImage() {
        File f_res = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/TmpDataMedia" + "/.images");
        File f_des = new File(AppPreference.getInstance(mActivity)
                .getHideImageRootPath());
        if (f_res.exists() && f_res.listFiles().length > 0) {
            try {
                deleteDirectory(f_des);
                copyDirectoryOneLocationToAnotherLocation(f_res, f_des);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deleteDirectoryAll(f_res);
    }

    public void CopyVideos() {
        File f_res = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/TmpDataMedia" + "/.videos");
        File f_des = new File(AppPreference.getInstance(mActivity)
                .getHideVideoRootPath());
        if (f_res.exists() && f_res.listFiles().length > 0) {
            try {
                deleteDirectory(f_des);
                copyDirectoryOneLocationToAnotherLocation(f_res, f_des);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deleteDirectoryAll(f_res);
    }

    public void CopyContact() {
        File f_res_app = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/TmpData" + "/" + AppContentProvider.DATABASE_NAME);
        File f_des_app = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/" + AppContentProvider.DATABASE_NAME);
        if (f_res_app.exists()) {
            try {
                deleteDirectory(f_des_app);
                copyDirectoryOneLocationToAnotherLocation(f_res_app, f_des_app);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File f_res_sms = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/TmpData" + "/" + SmsCallLogContentProvider.DATABASE_NAME);
        File f_des_sms = new File(AppPreference.getInstance(mActivity)
                .getHideRootPath() + "/" + SmsCallLogContentProvider.DATABASE_NAME);
        if (f_res_sms.exists()) {
            try {
                deleteDirectory(f_des_sms);
                copyDirectoryOneLocationToAnotherLocation(f_res_sms, f_des_sms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deleteDirectoryAll(f_res_app);
        deleteDirectoryAll(f_res_sms);
    }
	/*public void CopyContact() {
		File f = new File(Preferences.getInstance(mActivity).getHideRootPath()
				+ "/TmpData");
		if (f.exists() && f.listFiles().length > 0) {
			try {
				copyDirectoryOneLocationToAnotherLocation(f, new File(
						Preferences.getInstance(mActivity).getHideRootPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		deleteDirectoryAll(f);
	}*/

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file:files){
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.getName().equals(
                            PhotoContentProvider.DATABASE_NAME)) {
                        if (!file.getName().equals(
                                VideoContentProvider.DATABASE_NAME)) {
                            file.delete();
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean deleteDirectoryAll(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file:files){
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return true;
    }

    public static void copyDirectoryOneLocationToAnotherLocation(
            File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(
                        sourceLocation, children[i]), new File(targetLocation,
                        children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
