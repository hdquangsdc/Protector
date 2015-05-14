package com.protector.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.protector.AppPreference;
import com.protector.R;
import com.protector.fragments.BackupFragment;
import com.protector.utils.NotificationIdManager;


public class DropboxBackupTask extends AsyncTask<Void, Integer, String> {
	public static final String FOLDER_BACKUP = "Backup";
	public static final int BUFFER_SIZE = 2048;
	public static final String EXTENTION_BACKUP = ".backup";

	Context mContext;
	private ArrayList<File> mFiles;

	public DbxAccountManager mDbxAcctMgr;

	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	private int mNotificationId;
	private double mTotalTranfer;
	private int lastPercent;
	private DbxPath dbxFilePath;
	private String fileName;
	private long mTotal;
	private String mMessage = "";
	private String myPassword;

	public DropboxBackupTask(Context context, ArrayList<File> files,
			String password) {
		mContext = context;
		mFiles = files;
		PendingIntent pend = PendingIntent.getActivity(context, 0,
				new Intent(), 0);
		mNotificationId = NotificationIdManager.obtainAnId();
		mNotifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setAutoCancel(false);
		mBuilder.setContentTitle(
				mContext.getString(R.string.backup_dropbox))
				.setContentText(mContext.getString(R.string.connecting))
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pend);
		myPassword = password;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			mDbxAcctMgr = DbxAccountManager.getInstance(mContext,
					"r1nf045h3z6u88d", "0gv1e3znv60lgs3");
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
					.getLinkedAccount());
			SystemClock.sleep(5000);

			for (File file : mFiles) {
				mMessage = mContext.getString(R.string.transferring) + " "
						+ file.getName();
				mTotalTranfer = 0;
				lastPercent = 0;
				fileName = file.getName();
				dbxFilePath = new DbxPath(FOLDER_BACKUP + "/" + fileName);
				DbxPath folder = new DbxPath(FOLDER_BACKUP);
				if (!dbxFs.exists(folder)) {
					dbxFs.createFolder(folder);
				}

				DbxFile dbxFile;
				if (dbxFs.isFile(dbxFilePath)) {
					dbxFile = dbxFs.open(dbxFilePath);
				} else {
					dbxFile = dbxFs.create(dbxFilePath);
				}
				FileOutputStream outputStream = dbxFile.getWriteStream();
				ZipOutputStream out = new ZipOutputStream(
						new BufferedOutputStream(outputStream));
				if (file.isDirectory()) {
					mTotal = folderSize(file);
				} else {
					mTotal = file.length();
				}
				try {
					addFolderToZip("", file.getPath(), out);
				} catch (Exception ex) {
					break;
				} finally {
					out.close();
					dbxFile.close();
					outputStream.close();
				}
				DbxPath newPath = new DbxPath(FOLDER_BACKUP + "/" + fileName
						+ EXTENTION_BACKUP);
				if (dbxFs.exists(newPath)) {
					dbxFs.delete(newPath);
				}
				dbxFs.move(dbxFilePath, newPath);
				if (file.getPath().equals(
						AppPreference.getInstance(mContext)
								.getHideImageRootPath())) {
					sendTest(BackupFragment.PHOTO_SIZE, folderSize(file));
				} else if (file.getPath().equals(
						AppPreference.getInstance(mContext)
								.getHideVideoRootPath())) {
					sendTest(BackupFragment.VIDEO_SIZE, folderSize(file));
				}
			}
			sendPassword(myPassword);
			return null;
		} catch (Exception e) {
			AppPreference.getInstance(mContext).setDropboxBackup(false);
			e.printStackTrace();
			return e.getMessage();
		}

	}

	public long folderSize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	public int addFileToZip(String path, String file, ZipOutputStream out) {
		try {
			byte data[] = new byte[BUFFER_SIZE];
			File folder = new File(file);
			if (folder.isDirectory()) {
				try {
					addFolderToZip(path, file, out);
					return -1;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			BufferedInputStream origin = null;
			FileInputStream fi = new FileInputStream(file);
			origin = new BufferedInputStream(fi, BUFFER_SIZE);
			try {
				ZipEntry entry = new ZipEntry(path + "/" + folder.getName());
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
					out.write(data, 0, count);
					mTotalTranfer += count;
					int percent = (int) (100 * mTotalTranfer / mTotal);
					if (percent - lastPercent >= 1) {
						lastPercent = percent;
						publishProgress(lastPercent);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					origin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
		return -1;
	}

	public void addFolderToZip(String path, String srcFolder,
			ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);
		if (folder.isFile()) {
			addFileToZip("", srcFolder, zip);
			return;
		}
		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
						+ fileName, zip);
			}
		}
	}

	@Override
	protected void onPostExecute(String result) {
		AppPreference.getInstance(mContext).setDropboxBackup(false);
		if (result == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.backup_success),
					Toast.LENGTH_SHORT).show();
			mBuilder.setContentText(
					mContext.getString(R.string.backup_success))
					.setProgress(0, 0, false);
			mBuilder.setAutoCancel(true);
			mBuilder.setOngoing(false);
			Uri uri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(uri);
			mNotifyManager.notify(mNotificationId, mBuilder.build());
		} else {
			Toast.makeText(mContext,
					mContext.getString(R.string.backup_fail),
					Toast.LENGTH_SHORT).show();
			mBuilder.setContentText(
					mContext.getString(R.string.backup_fail)).setProgress(
					0, 0, false);
			mBuilder.setAutoCancel(true);

			Intent resultIntent = new Intent();
			Uri uri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(uri);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					mContext, mNotificationId, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentIntent(resultPendingIntent);

			mNotifyManager.notify(mNotificationId, mBuilder.build());
		}
		// clearApplicationData(mContext);
		// mDbxAcctMgr.unlink();
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mMessage.contains(".videos")) {
			mMessage = mContext.getString(R.string.transfering_video);
		}
		if (mMessage.contains(".images")) {
			mMessage = mContext.getString(R.string.transfering_image);
		}
		mBuilder.setContentText(mMessage + " (" + values[0] + "%)...");
		mBuilder.setProgress(100, values[0], false);
		mBuilder.setOngoing(true);
		mNotifyManager.notify(mNotificationId, mBuilder.build());
		Log.d("%%%%%", mMessage);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPreExecute() {
		mBuilder.setProgress(0, 0, true);
		mBuilder.setOngoing(true);
		mNotifyManager.notify(mNotificationId, mBuilder.build());
		AppPreference.getInstance(mContext).setDropboxBackup(true);
		super.onPreExecute();
	};

	public void sendTest(String filename, long size) {
		try {
			DbxPath testPath = new DbxPath(DbxPath.ROOT, FOLDER_BACKUP + "/"
					+ filename);
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
					.getLinkedAccount());
			List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
			if (!dbxFs.exists(testPath)) {
				DbxFile testFile = dbxFs.create(testPath);

				try {
					testFile.writeString(String.valueOf(size));
				} finally {
					testFile.close();
				}
			} else {
				DbxFile testFile = dbxFs.open(testPath);
				try {
					testFile.writeString(String.valueOf(size));
				} finally {
					testFile.close();
				}
			}
		} catch (IOException e) {
		}
	}

	public void sendPassword(String password) {
		try {
			DbxPath testPath = new DbxPath(DbxPath.ROOT, FOLDER_BACKUP + "/"
					+ "config.hl");
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
					.getLinkedAccount());
			List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
			if (!dbxFs.exists(testPath)) {
				DbxFile testFile = dbxFs.create(testPath);

				try {
					testFile.writeString(password);
				} finally {
					testFile.close();
				}
			} else {
				DbxFile testFile = dbxFs.open(testPath);
				try {
					testFile.writeString(password);
				} finally {
					testFile.close();
				}
			}
		} catch (IOException e) {
		}
	}

	public static void clearApplicationData(Context context) {
		File cache = context.getCacheDir();
		SharedPreferences settings = context.getSharedPreferences(
				"cda-preferences", Context.MODE_PRIVATE);
		settings.edit().clear().commit();
		File appDir = new File(cache.getParent());
		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				File f = new File(appDir, s);
				if (s.equals("libs")) {
					deleteDir(f);
					// runDir(f);
				}
			}
		}
	}

	private static boolean runDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				// if(children[i].equals("files") ||
				// children[i].equals("logs")){
				deleteDir(new File(dir, children[i]));
				/*
				 * }else{ runDir(new File(dir, children[i])); }
				 */

			}
		}
		return true;
	}

	private static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File f = new File(dir, children[i]);
				boolean success = deleteDir(f);
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static long folderSize1(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize1(file);
		}
		return length;
	}
}
