package com.protector.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.protector.AppPreference;
import com.protector.R;
import com.protector.fragments.BackupFragment;
import com.protector.utils.NotificationIdManager;

public class GoogleDriveBackupTask extends AsyncTask<Void, Integer, String> {
	public static final int BUFFER_SIZE = 2048;
	public static final String MIMEFLDR = "application/vnd.google-apps.folder";
	// Context mContext;
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	private int mNotificationId;
	private double mTotalTranfer;
	private int lastPercent;
	private GoogleApiClient mGoogleApiClient;
	private DriveFile mDriveFile;
	private DriveId mFolderDriveId;
	private CountDownLatch latch = new CountDownLatch(1);
	private ArrayList<File> mFiles;
	private long mTotal;
	private String mMessage = "";
	Activity activity;
	private String myPassword;

	public GoogleDriveBackupTask(Activity activity, ArrayList<File> files,
			String password) {
		myPassword = password;
		this.activity = activity;
		mFiles = files;
		PendingIntent pend = PendingIntent.getActivity(activity, 0,
				new Intent(), 0);
		mNotificationId = NotificationIdManager.obtainAnId();
		mNotifyManager = (NotificationManager) activity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(activity);

		mBuilder.setContentTitle(
				activity.getString(R.string.backup_drive))
				.setContentText(activity.getString(R.string.connecting))
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pend);

		mTotalTranfer = 0;
		lastPercent = 0;
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity)
				.addApi(Drive.API).addScope(Drive.SCOPE_FILE);
		mGoogleApiClient = builder.build();

	}

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

			// Waiting for connect
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
			mFolderDriveId = getDriveId(mGoogleApiClient,
					GoogleDriveRestoreTask.FOLDER_BACKUP, MIMEFLDR,
					Drive.DriveApi.getRootFolder(mGoogleApiClient));
			if (mFolderDriveId == null) {
				// Create BACK_UP folder
				MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle(GoogleDriveRestoreTask.FOLDER_BACKUP)
						.setMimeType(MIMEFLDR).build();
				DriveFolderResult folderCreateResult = Drive.DriveApi
						.getRootFolder(mGoogleApiClient)
						.createFolder(mGoogleApiClient, changeSet).await();
				if (!folderCreateResult.getStatus().isSuccess()) {
					return "";
				}
				mFolderDriveId = folderCreateResult.getDriveFolder()
						.getDriveId();
			}
			boolean isEdit;
			for (File file : mFiles) {
				DriveContentsResult contentsResult;
				DriveFile drFile = checkFile(file.getName());
				isEdit = false;
				if (drFile != null) {
					mDriveFile = drFile;
					contentsResult = mDriveFile.open(mGoogleApiClient,
							DriveFile.MODE_WRITE_ONLY, null).await();
					isEdit = true;
					if (!contentsResult.getStatus().isSuccess()) {
						return "";
					}
				} else {
					// Create File
					contentsResult = Drive.DriveApi.newDriveContents(
							mGoogleApiClient).await();
					if (!contentsResult.getStatus().isSuccess()) {
						return "";
					}
					MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
							.setTitle(file.getName()).setStarred(true).build();
					DriveFolder folder = Drive.DriveApi.getFolder(
							mGoogleApiClient, mFolderDriveId);
					DriveFileResult createFileResult = folder.createFile(
							mGoogleApiClient, changeSet,
							contentsResult.getDriveContents()).await();
					if (!createFileResult.getStatus().isSuccess()) {
						return "";
					}
					mDriveFile = createFileResult.getDriveFile();
				}
				// Sending content
				String result = sendContent(file);
				if (result != null) {
					// return result;
				} else {
					if (!isEdit) {
						// Rename file
						MetadataChangeSet changeSetnew = new MetadataChangeSet.Builder()
								.setTitle(
										file.getName()
												+ DropboxBackupTask.EXTENTION_BACKUP)
								.setStarred(true).build();
						MetadataResult renameResult = mDriveFile
								.updateMetadata(mGoogleApiClient, changeSetnew)
								.await();
						if (!renameResult.getStatus().isSuccess()) {
							return "";
						}
					}
				}
				if (file.getPath().equals(
						AppPreference.getInstance(activity)
								.getHideImageRootPath())) {
					sendTest(BackupFragment.PHOTO_SIZE, folderSize(file));
				} else if (file.getPath().equals(
						AppPreference.getInstance(activity)
								.getHideVideoRootPath())) {
					sendTest(BackupFragment.VIDEO_SIZE, folderSize(file));
				}
			}
			sendPassword(myPassword);
		} catch (Exception ex) {
			AppPreference.getInstance(activity).setDriveBackup(false);
		}
		return null;
	}

	public void getText(String filename) {
		DriveContentsResult contentsResult;
		DriveFile drFile = checkFileText(filename);
		if (drFile != null) {
			contentsResult = drFile.open(mGoogleApiClient,
					DriveFile.MODE_READ_ONLY, null).await();
			InputStream ios = contentsResult.getDriveContents()
					.getInputStream();
			try {
				Log.d("Size", getString(ios));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ios.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			com.google.android.gms.common.api.Status status = drFile
//					.commitAndCloseContents(mGoogleApiClient,
//							contentsResult.getDriveContents()).await();
			PendingResult<com.google.android.gms.common.api.Status> status = contentsResult.getDriveContents().commit(mGoogleApiClient, null);
//			status.getStatus().isSuccess();
		}
	}

	public String getString(InputStream is) throws IOException {
		int ch;
		StringBuilder sb = new StringBuilder();
		while ((ch = is.read()) != -1)
			sb.append((char) ch);
		return sb.toString();
	}

	public void sendPassword(String password) {
		DriveContentsResult contentsResult;
		DriveFile drFile = checkFileText("config.hl");
		if (drFile != null) {
			contentsResult = drFile.open(mGoogleApiClient,
					DriveFile.MODE_WRITE_ONLY, null).await();
			OutputStream outputStream = contentsResult.getDriveContents()
					.getOutputStream();
			try {
				outputStream.write(String.valueOf(password).getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			com.google.android.gms.common.api.Status status = drFile
//					.commitAndCloseContents(mGoogleApiClient,
//							contentsResult.getDriveContents()).await();
			PendingResult<com.google.android.gms.common.api.Status> status = contentsResult.getDriveContents().commit(mGoogleApiClient, null);
//			status.getStatus().isSuccess();
		} else {
			contentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient)
					.await();
			OutputStream outputStream = contentsResult.getDriveContents()
					.getOutputStream();
			try {
				outputStream.write(String.valueOf(password).getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
					.setTitle("config.hl").setMimeType("text/plain").build();
			DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
					mFolderDriveId);

			DriveFileResult createFileResult = folder.createFile(
					mGoogleApiClient, changeSet, contentsResult.getDriveContents())
					.await();
			if (!createFileResult.getStatus().isSuccess()) {
				return;
			}
		}
	}

	public void sendTest(String filename, long size) {
		DriveContentsResult contentsResult;
		DriveFile drFile = checkFileText(filename);
		if (drFile != null) {
			contentsResult = drFile.open(mGoogleApiClient,
					DriveFile.MODE_WRITE_ONLY, null).await();
			OutputStream outputStream = contentsResult.getDriveContents()
					.getOutputStream();
			try {
				outputStream.write(String.valueOf(size).getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			com.google.android.gms.common.api.Status status = drFile
//					.commitAndCloseContents(mGoogleApiClient,
//							contentsResult.getDriveContents()).await();


			PendingResult<com.google.android.gms.common.api.Status> status = contentsResult.getDriveContents().commit(mGoogleApiClient, null);
//			status.isSuccess();
		} else {
			contentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient)
					.await();
			OutputStream outputStream = contentsResult.getDriveContents()
					.getOutputStream();
			try {
				outputStream.write(String.valueOf(size).getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
					.setTitle(filename).setMimeType("text/plain").build();
			DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
					mFolderDriveId);

//			DriveFileResult createFileResult = folder.createFile(
//					mGoogleApiClient, changeSet, contentsResult.getContents())
//					.await();

			PendingResult<com.google.android.gms.common.api.Status> status = contentsResult.getDriveContents().commit(mGoogleApiClient, changeSet);
//			if (!createFileResult.getStatus().isSuccess()) {
//				return;
//			}
		}
	}

	private String sendContent(File file) {
		try {
			DriveContentsResult contentsResult = mDriveFile.open(
					mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
			if (!contentsResult.getStatus().isSuccess()) {
				return "";
			}
			OutputStream outputStream = contentsResult.getDriveContents()
					.getOutputStream();
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					outputStream));
			if (file.isDirectory()) {
				mTotal = folderSize(file);
			} else {
				mTotal = file.length();
			}
			mMessage = activity.getString(R.string.transferring) + " "
					+ file.getName();
			mTotalTranfer = 0;
			lastPercent = 0;
			try {
				addFolderToZip("", file.getPath(), out);
			} finally {
				out.close();
				outputStream.close();

			}
//			com.google.android.gms.common.api.Status status = mDriveFile
//					.commitAndCloseContents(mGoogleApiClient,
//							contentsResult.getDriveContents()).await();

			PendingResult<com.google.android.gms.common.api.Status> status = contentsResult.getDriveContents().commit(mGoogleApiClient, null);
//			if (!status.getStatus().isSuccess()) {
//				return "";
//			}
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
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

	public int addFileToZip(String path, String file, ZipOutputStream out)
			throws IOException {
		try {
			byte data[] = new byte[BUFFER_SIZE];
			File folder = new File(file);
			if (folder.isDirectory()) {
				try {
					addFolderToZip(path, file, out);
					return -1;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			BufferedInputStream origin = null;
			FileInputStream fi = new FileInputStream(folder);
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
			ZipOutputStream zip) {
		try {
			File folder = new File(srcFolder);
			if (folder.isFile()) {
				addFileToZip("", srcFolder, zip);
				return;
			}
			for (String fileName : folder.list()) {
				if (path.equals("")) {
					addFileToZip(folder.getName(), srcFolder + "/" + fileName,
							zip);
				} else {
					addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
							+ fileName, zip);
				}
			}
		} catch (IOException e) {
			e.getMessage();
			try {
				zip.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	protected void onPreExecute() {
		AppPreference.getInstance(activity).setDriveBackup(true);
		mBuilder.setProgress(0, 0, true);
		mBuilder.setOngoing(true);
		mBuilder.setAutoCancel(false);
		mNotifyManager.notify(mNotificationId, mBuilder.build());
		super.onPreExecute();
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
		AppPreference.getInstance(activity).setDriveBackup(false);
		if (result == null) {
			Toast.makeText(activity,
					activity.getString(R.string.backup_success),
					Toast.LENGTH_SHORT).show();
			mBuilder.setContentText(
					activity.getString(R.string.backup_success))
					.setProgress(0, 0, false);
			mBuilder.setAutoCancel(true);
			mBuilder.setOngoing(false);
			Uri uri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(uri);
			mNotifyManager.notify(mNotificationId, mBuilder.build());
		} else {
			Toast.makeText(activity,
					activity.getString(R.string.backup_fail),
					Toast.LENGTH_SHORT).show();
			mBuilder.setContentText(
					activity.getString(R.string.backup_fail)).setProgress(
					0, 0, false);
			mBuilder.setOngoing(false);
			mBuilder.setAutoCancel(true);
			Uri uri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(uri);

			Intent resultIntent = new Intent();
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					activity, mNotificationId, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentIntent(resultPendingIntent);
			mNotifyManager.notify(mNotificationId, mBuilder.build());
		}
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mMessage.contains(".videos")) {
			mMessage = activity.getString(R.string.transfering_video);
		}
		if (mMessage.contains(".images")) {
			mMessage = activity.getString(R.string.transfering_image);
		}
		mBuilder.setContentText(mMessage + " (" + values[0] + "%)...");
		mBuilder.setOngoing(true);
		mBuilder.setProgress(100, values[0], false);
		mNotifyManager.notify(mNotificationId, mBuilder.build());
		super.onProgressUpdate(values);
	}

	public DriveFile checkFile(String title) {
		if (mFolderDriveId != null) {
			DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
					mFolderDriveId);
			MetadataBufferResult result = folder.listChildren(mGoogleApiClient)
					.await();
			if (!result.getStatus().isSuccess()) {

			} else {
				int a = result.getMetadataBuffer().getCount();
				for (int i = 0; i < result.getMetadataBuffer().getCount(); i++) {
					Metadata data = result.getMetadataBuffer().get(i);
					if (!data.isTrashed() && !data.isFolder()) {
						String name = data.getTitle();
						if (name.equals(title
								+ DropboxBackupTask.EXTENTION_BACKUP)) {
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

	public DriveFile checkFileText(String title) {
		if (mFolderDriveId != null) {
			DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,
					mFolderDriveId);
			MetadataBufferResult result = folder.listChildren(mGoogleApiClient)
					.await();
			if (!result.getStatus().isSuccess()) {

			} else {
				int a = result.getMetadataBuffer().getCount();
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

}
