package com.protector.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import com.protector.R;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.objects.MediaStorageItem;
import com.protector.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RestoreFile extends AsyncTask<Void, Void, Void> {
	public static final String ACTION_DECRYPTED = "com.example.testencryptdata.Decypted";
	private ProgressDialog mDialog;
	private int progress = 0;
	private List<MediaStorageItem> items;
	private Context context;
	private MediaItem.Type myType;

	public RestoreFile(Context context, List<MediaStorageItem> items,
			MediaItem.Type type) {
		this.context = context;
		this.items = new ArrayList<MediaStorageItem>();
		this.items.addAll(items);
		myType = type;
	}

	@Override
	protected void onPreExecute() {
		mDialog = new ProgressDialog(context);
		mDialog.setCancelable(false);
		mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDialog.setMessage(context.getString(R.string.restoring, 0 + "%"));
		mDialog.show();
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... params) {
		for (MediaStorageItem item : items) {
			try {
				File encryptedFile = new File(item.getNewPath());
				if (encryptedFile.exists()) {

					File newFile = new File(item.getOrgPath());
					int i = 0;
					while (newFile.exists()) {
						String extention = item.getOrgPath().substring(
								item.getOrgPath().lastIndexOf("."));
						String file = item.getOrgPath().substring(0,
								item.getOrgPath().lastIndexOf(".") - 1)
								+ "_" + i + extention;
						newFile = new File(file);
						i++;
					}
					newFile.getParentFile().mkdirs();
					encryptedFile.renameTo(newFile);
					boolean success = FileUtils.changeAcessFile(newFile
							.getAbsolutePath());
					if (success) {
						Log.d("RESTORE", encryptedFile.length() + "=>"
								+ newFile.length());
						if (myType == MediaItem.Type.IMAGE) {
							PhotoTableAdapter.getInstance(context).remove(
									item.getId());
							encryptedFile.delete();
						} else {
							VideoTableAdapter.getInstance(context).remove(
									item.getId());
							encryptedFile.delete();
						}
					}
					if (myType == MediaItem.Type.IMAGE) {
						MediaScannerConnection
								.scanFile(
										context,
										new String[] { newFile.getPath() },
										new String[] { "image/"
												+ item.getExtention() }, null);
					} else {
						MediaScannerConnection
								.scanFile(
										context,
										new String[] { newFile.getPath() },
										new String[] { "video/"
												+ item.getExtention() }, null);
					}
				} else {
					if (myType == MediaItem.Type.IMAGE) {
						PhotoTableAdapter.getInstance(context).remove(
								item.getId());
					} else {
						VideoTableAdapter.getInstance(context).remove(
								item.getId());
					}
				}
				progress++;
				publishProgress();
			} catch (Exception ex) {

			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		mDialog.dismiss();
		// context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
		// .parse("file://" + Environment.getExternalStorageDirectory())));
		context.sendBroadcast(new Intent(ACTION_DECRYPTED));
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		int prog = (int) ((100.0 * progress) / items.size());
		mDialog.setMessage(context.getString(R.string.restoring, prog + "%"));
		mDialog.setProgress(prog);
		super.onProgressUpdate(values);
	}
}
