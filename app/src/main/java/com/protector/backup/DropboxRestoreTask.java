//package com.protector.backup;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Random;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.os.AsyncTask;
//import android.os.SystemClock;
//import android.util.Log;
//import android.util.Pair;
//import android.widget.Toast;
//
//import com.dropbox.sync.android.DbxAccountManager;
//import com.dropbox.sync.android.DbxFile;
//import com.dropbox.sync.android.DbxFileSystem;
//import com.dropbox.sync.android.DbxPath;
//
//public class DropboxRestoreTask extends AsyncTask<Void, Integer, String> {
//	public static final int BUFFER_SIZE = 2048;
//	Activity mActivity;
//
//	private DbxAccountManager mDbxAcctMgr;
//
//	private long totalSize;
//	private int lastPercent;
//	private ArrayList<Pair<File, Integer>> mFiles;
//	private String mMessage = "";
//	private boolean myIsContinue, myIsContinueTimer;
//	private int myAsyncTaskID;
//	private Thread myThreadTimer;
//
//	public DropboxRestoreTask(Activity activity,
//			ArrayList<Pair<File, Integer>> files) {
//		mActivity = activity;
//		mFiles = files;
//		myIsContinue = true;
//		myIsContinueTimer = true;
//		mDbxAcctMgr = DbxAccountManager.getInstance(
//				activity.getApplicationContext(), "r1nf045h3z6u88d",
//				"0gv1e3znv60lgs3");
//
//		// Random ID
//		Random rand = new Random();
//		myAsyncTaskID = rand.nextInt(99999);
//		SharedPreferences pref = mActivity
//				.getPreferences(Activity.MODE_PRIVATE);
//		Editor edt = pref.edit();
//		edt.putBoolean(myAsyncTaskID + "", true);
//		edt.commit();
//
//		myThreadTimer = new Thread(run);
//		myThreadTimer.start();
//
//	}
//
//	public Runnable run = new Runnable() {
//
//		@Override
//		public void run() {
//			SharedPreferences pref = mActivity
//					.getPreferences(Activity.MODE_PRIVATE);
//			while (myIsContinueTimer) {
//				SystemClock.sleep(200);
//				myIsContinue = pref.getBoolean(myAsyncTaskID + "", true);
//				if (!myIsContinue) {
//					return;
//				}
//			}
//		}
//	};
//
//	@Override
//	protected String doInBackground(Void... params) {
//		try {
//			DbxPath folder = new DbxPath(DropboxBackupTask.FOLDER_BACKUP);
//			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
//					.getLinkedAccount());
//			SystemClock.sleep(5000);
//			if (!dbxFs.exists(folder)) {
//				return mActivity.getString(R.string.txt_no_backup_files);
//			}
//			for (Pair<File, Integer> item : mFiles) {
//				if (!myIsContinue) {
//					return null;
//				}
//				File file = item.first;
//				mMessage = mActivity.getString(R.string.txt_restoring1) + " "
//						+ file.getName();
//				publishProgress(0);
//				totalSize = 0;
//				lastPercent = -1;
//				DbxPath backupFile = new DbxPath(
//						DropboxBackupTask.FOLDER_BACKUP + "/" + file.getName()
//								+ DropboxBackupTask.EXTENTION_BACKUP);
//				if (dbxFs.exists(backupFile)) {
//					DbxFile dbxFile;
//					File unZipFolder = null;
//					if (item.second == 0) {
//						/*
//						 * unZipFolder = new File(Preferences.getInstance(
//						 * mActivity).getHideRootPath());
//						 */
//						unZipFolder = new File(Preferences.getInstance(
//								mActivity).getHideRootPath()
//								+ "/TmpData");
//					} else {
//						/*
//						 * unZipFolder = new File(Preferences.getInstance(
//						 * mActivity).getHideRootPathV1());
//						 */
//						unZipFolder = new File(Preferences.getInstance(
//								mActivity).getHideRootPath()
//								+ "/TmpDataMedia");
//
//					}
//					if (!unZipFolder.exists()) {
//						unZipFolder.mkdirs();
//						unZipFolder.createNewFile();
//					}
//					if (unZipFolder.exists() && unZipFolder.isDirectory()) {
//						dbxFile = dbxFs.open(backupFile);
//						if (item.first.getPath().equals(
//								Preferences.getInstance(mActivity)
//										.getHideImageRootPath())) {
//							totalSize = getSizeFile(BackupActivty.PHOTO_SIZE);
//						} else if (item.first.getPath().equals(
//								Preferences.getInstance(mActivity)
//										.getHideVideoRootPath())) {
//							totalSize = getSizeFile(BackupActivty.VIDEO_SIZE);
//						} else {
//							totalSize = dbxFile.getInfo().size;
//						}
//						if (totalSize < dbxFile.getInfo().size) {
//							totalSize = dbxFile.getInfo().size;
//						}
//						Log.d("Start", "Start ...");
//						FileInputStream in = dbxFile.getReadStream();
//						Log.d("GetInput", "getInput...");
//						ZipInputStream zin = new ZipInputStream(in);
//
//						try {
//							ZipEntry ze = null;
//							int totalUnzip = 0;
//							while ((ze = zin.getNextEntry()) != null) {
//								File unzipFile = new File(unZipFolder,
//										ze.getName());
//								if (ze.isDirectory()) {
//									if (!unzipFile.isDirectory()) {
//										unzipFile.mkdirs();
//									}
//								} else {
//									if (unzipFile.getAbsoluteFile().toString()
//											.contains(".images")) {
//										File file1 = new File(Preferences
//												.getInstance(mActivity)
//												.getHideRootPath()
//												+ "/TmpDataMedia/.images");
//										if (!file1.exists()) {
//											file1.mkdirs();
//											file1.createNewFile();
//										}
//									}
//									if (unzipFile.getAbsoluteFile().toString()
//											.contains(".videos")) {
//										File file1 = new File(Preferences
//												.getInstance(mActivity)
//												.getHideRootPath()
//												+ "/TmpDataMedia/.videos");
//										if (!file1.exists()) {
//											file1.mkdirs();
//											file1.createNewFile();
//										}
//									}
//									FileOutputStream fout = null;
//									try {
//										fout = new FileOutputStream(unzipFile,
//												true);
//										final int BUFFER_SIZE = 4096;// 2048;
//										int count = 0;
//										byte[] buffer = new byte[BUFFER_SIZE];
//										while ((count = zin.read(buffer, 0,
//												BUFFER_SIZE)) != -1) {
//											if (!myIsContinue) {
//												fout.close();
//												zin.close();
//												return null;
//											}
//											fout.write(buffer, 0, count);
//											totalUnzip += count;
//											int percent = (int) (totalUnzip * 100.0f / totalSize);
//											if (lastPercent != percent) {
//												lastPercent = percent;
//												publishProgress(percent);
//											}
//											// Log.d("Total", totalUnzip + "");
//										}
//										zin.closeEntry();
//									} catch (Exception ex) {
//										return ex.getMessage();
//									} finally {
//										fout.close();
//									}
//								}
//							}
//						} catch (Exception e) {
//							return e.getMessage();
//						} finally {
//							zin.close();
//							dbxFile.close();
//						}
//					} else {
//						return mActivity
//								.getString(R.string.txt_no_sdcard_available);
//					}
//				}
//			}
//		} catch (Exception e) {
//			StoreRestore.getInstance(mActivity).setStore(false);
//			return e.getMessage();
//		}
//		return null;
//	}
//
//	@Override
//	protected void onPostExecute(String result) {
//		// mActivity.unregisterReceiver(receiver);
//		myIsContinueTimer = false;
//		myThreadTimer.interrupt();
//		SharedPreferences pref = mActivity
//				.getPreferences(Activity.MODE_PRIVATE);
//		pref.edit().remove(myAsyncTaskID + "").commit();
//		StoreRestore.getInstance(mActivity).setStore(false);
//		if (result == null) {
//			if (myIsContinue) {
//				Intent ii = new Intent();
//				ii.putExtra("MESSAGE",
//						mActivity.getString(R.string.txt_finishing));
//				ii.putExtra("PROGRESS", RestoreActivity.RESTORE_FINISH);
//				ii.setAction("BACKUP.DATA");
//				mActivity.sendBroadcast(ii);
//				SystemClock.sleep(200);
//				new FileCopyAsyn().execute();
//			} else {
//			}
//		} else {
//			if (result.length() > 0) {
//				Intent ii = new Intent();
//				ii.putExtra("MESSAGE", mActivity.getString(R.string.txt_error1));
//				ii.putExtra("PROGRESS", RestoreActivity.RESTORE_ERROR);
//				ii.setAction("BACKUP.DATA");
//				mActivity.sendBroadcast(ii);
//				Toast.makeText(
//						mActivity,
//						mActivity
//								.getString(R.string.txt_fail_please_check_network),
//						Toast.LENGTH_LONG).show();
//			} else {
//
//			}
//		}
//		super.onPostExecute(result);
//	}
//
//	public long getSizeFile(String filename) {
//		long size = 0;
//		try {
//			DbxPath testPath = new DbxPath(DbxPath.ROOT,
//					DropboxBackupTask.FOLDER_BACKUP + "/" + filename);
//			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
//					.getLinkedAccount());
//			if (dbxFs.isFile(testPath)) {
//				DbxFile testFile = dbxFs.open(testPath);
//				try {
//					size = Long.parseLong(testFile.readString());
//				} finally {
//					testFile.close();
//				}
//			} else if (dbxFs.isFolder(testPath)) {
//			}
//
//		} catch (IOException e) {
//		}
//		return size;
//	}
//
//	@Override
//	protected void onProgressUpdate(Integer... values) {
//		if (mMessage.contains(".videos")) {
//			mMessage = mActivity.getString(R.string.txt_restoring_videos);
//		}
//		if (mMessage.contains(".images")) {
//			mMessage = mActivity.getString(R.string.txt_restoring_images);
//		}
//		if (values[0] <= 99) {
//			Intent ii = new Intent();
//			ii.putExtra("MESSAGE", mMessage);
//			ii.putExtra("PROGRESS", values[0]);
//			ii.setAction("BACKUP.DATA");
//			mActivity.sendBroadcast(ii);
//		} else {
//		}
//		super.onProgressUpdate(values);
//	}
//
//	@Override
//	protected void onPreExecute() {
//
//		StoreRestore.getInstance(mActivity).setStore(true);
//		Intent ii = new Intent();
//		ii.putExtra("MESSAGE", mActivity.getString(R.string.txt_connecting));
//		ii.putExtra("PROGRESS", 0);
//		ii.setAction("BACKUP.DATA");
//		mActivity.sendBroadcast(ii);
//		super.onPreExecute();
//	};
//
//	public class FileCopyAsyn extends AsyncTask<Void, Void, Void> {
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//		}
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			CopyVideos();
//			CopyContact();
//			CopyImage();
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			Intent ii = new Intent();
//			ii.putExtra("MESSAGE", mActivity.getString(R.string.txt_copied));
//			ii.putExtra("PROGRESS", RestoreActivity.RESTORE_COPIED);
//			ii.setAction("BACKUP.DATA");
//			mActivity.sendBroadcast(ii);
//			Toast.makeText(mActivity,
//					mActivity.getString(R.string.txt_restore_success),
//					Toast.LENGTH_LONG).show();
//			if (!StoreHL3Dot1.getInstance(mActivity)
//					.getRateApp()) {
//				new CustomDialogRateApp(mActivity).show();
//			}
//			super.onPostExecute(result);
//		}
//	}
//
//	public void CopyImage() {
//		File f_res = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/TmpDataMedia" + "/.images");
//		File f_des = new File(Preferences.getInstance(mActivity)
//				.getHideImageRootPath());
//		if (f_res.exists() && f_res.listFiles().length > 0) {
//			try {
//				deleteDirectory(f_des);
//				copyDirectoryOneLocationToAnotherLocation(f_res, f_des);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		deleteDirectoryAll(f_res);
//	}
//
//	public void CopyVideos() {
//		File f_res = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/TmpDataMedia" + "/.videos");
//		File f_des = new File(Preferences.getInstance(mActivity)
//				.getHideVideoRootPath());
//		if (f_res.exists() && f_res.listFiles().length > 0) {
//			try {
//				deleteDirectory(f_des);
//				copyDirectoryOneLocationToAnotherLocation(f_res, f_des);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		deleteDirectoryAll(f_res);
//	}
//
//	public void CopyContact() {
//		File f_res_app = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/TmpData" + "/" + AppLockContentProviderDB.DATABASE_NAME);
//		File f_des_app = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/" + AppLockContentProviderDB.DATABASE_NAME);
//		if (f_res_app.exists()) {
//			try {
//				deleteDirectory(f_des_app);
//				copyDirectoryOneLocationToAnotherLocation(f_res_app, f_des_app);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		File f_res_sms = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/TmpData" + "/" + SmsCallLogContentProviderDB.DATABASE_NAME);
//		File f_des_sms = new File(Preferences.getInstance(mActivity)
//				.getHideRootPath() + "/" + SmsCallLogContentProviderDB.DATABASE_NAME);
//		if (f_res_sms.exists()) {
//			try {
//				deleteDirectory(f_des_sms);
//				copyDirectoryOneLocationToAnotherLocation(f_res_sms, f_des_sms);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		deleteDirectoryAll(f_res_app);
//		deleteDirectoryAll(f_res_sms);
//	}
//
//	/*public void CopyContact() {//DATABASE_NAME
//		File f = new File(Preferences.getInstance(mActivity).getHideRootPath()
//				+ "/TmpData");
//		if (f.exists() && f.listFiles().length > 0) {
//			try {
//				copyDirectoryOneLocationToAnotherLocation(f, new File(
//						Preferences.getInstance(mActivity).getHideRootPath()));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		deleteDirectoryAll(f);
//	}*/
//
//	public static boolean deleteDirectory(File path) {
//		if (path.exists()) {
//			File[] files = path.listFiles();
//			if (files == null) {
//				return true;
//			}
//			for (int i = 0; i < files.length; i++) {
//				if (files[i].isDirectory()) {
//					deleteDirectory(files[i]);
//				} else {
//					if (!files[i].getName().equals(
//							ImagesContentProviderDB.DATABASE_NAME)) {
//						if (!files[i].getName().equals(
//								VideosContentProviderDB.DATABASE_NAME)) {
//							files[i].delete();
//						}
//					}
//				}
//			}
//		}
//		return true;
//	}
//
//	public static boolean deleteDirectoryAll(File path) {
//		if (path.exists()) {
//			File[] files = path.listFiles();
//			if (files == null) {
//				return true;
//			}
//			for (int i = 0; i < files.length; i++) {
//				if (files[i].isDirectory()) {
//					deleteDirectory(files[i]);
//				} else {
//					files[i].delete();
//				}
//			}
//		}
//		return true;
//	}
//
//	public static void copyDirectoryOneLocationToAnotherLocation(
//			File sourceLocation, File targetLocation) throws IOException {
//
//		if (sourceLocation.isDirectory()) {
//			if (!targetLocation.exists()) {
//				targetLocation.mkdir();
//			}
//
//			String[] children = sourceLocation.list();
//			for (int i = 0; i < sourceLocation.listFiles().length; i++) {
//
//				copyDirectoryOneLocationToAnotherLocation(new File(
//						sourceLocation, children[i]), new File(targetLocation,
//						children[i]));
//			}
//		} else {
//			InputStream in = new FileInputStream(sourceLocation);
//			OutputStream out = new FileOutputStream(targetLocation);
//			byte[] buf = new byte[1024];
//			int len;
//			while ((len = in.read(buf)) > 0) {
//				out.write(buf, 0, len);
//			}
//			in.close();
//			out.close();
//		}
//	}
//}
