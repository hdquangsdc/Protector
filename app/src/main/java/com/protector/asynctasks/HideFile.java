package com.protector.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.protector.AppPreference;
import com.protector.R;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.utils.FileUtils;
import com.protector.utils.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public class HideFile extends AsyncTask<Void, Void, Void> {
	public static final String ACTION_ENCRYPTED = "com.protector.encrypt";

	private ProgressDialog mDialog;
	private int progress;
	private List<MediaItem> items;
	private Context context;
	private MediaItem.Type myType;

	public HideFile(Context context, List<MediaItem> items, MediaItem.Type type) {
		this.context = context;
		this.items = items;
		progress = 0;
		myType = type;

	}

	@Override
	protected void onPreExecute() {
		mDialog = new ProgressDialog(context);
		mDialog.setCancelable(false);
		mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDialog.setMessage(context.getString(R.string.importing, 0 + "%"));
		mDialog.show();
		/*
		 * TextView textView = (TextView)
		 * mDialog.findViewById(android.R.id.message); textView.setTextSize(40);
		 */
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... params) {
		for (MediaItem item : items) {
			try {
				File hideRootPath;
				if (myType == MediaItem.Type.IMAGE) {
					hideRootPath = new File(AppPreference.getInstance(context)
							.getHideImageRootPath());
				} else if (myType == MediaItem.Type.VIDEO) {
					hideRootPath = new File(AppPreference.getInstance(context)
							.getHideVideoRootPath());
				} else
					return null;
				File newFile = new File(hideRootPath, String.valueOf(new Date()
						.getTime()));
				File orgFile = new File(item.getPath());

				ExifInterface ei = new ExifInterface(orgFile.getAbsolutePath());
				int orientation = ei.getAttributeInt(
						ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);
				if (orgFile.exists()) {
					// orgFile.renameTo(newFile);
					copyFile(orgFile, newFile);
					boolean success = FileUtils.changeAcessFile(newFile
							.getAbsolutePath());
					if (success) {
						long duration = 0;
						if (myType == MediaItem.Type.VIDEO) {
							Cursor cursor = null;
							try {
								Uri uri = Uri
										.withAppendedPath(
												MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
												String.valueOf(item.getId()));
								cursor = MediaStore.Video
										.query(context.getContentResolver(),
												uri,
												new String[] { MediaStore.Video.VideoColumns.DURATION });

								if (cursor != null && cursor.moveToFirst())
									duration = cursor.getLong(0);
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								if (cursor != null) {
									cursor.close();
								}
							}
						}
						String extention = null;
						try {
							extention = orgFile.getAbsolutePath()
									.substring(
											orgFile.getAbsolutePath()
													.lastIndexOf(".") + 1);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Bitmap bmp;
						if (myType == MediaItem.Type.IMAGE) {
							bmp = MediaStore.Images.Thumbnails.getThumbnail(
									context.getContentResolver(), item.getId(),
									MediaStore.Images.Thumbnails.MINI_KIND,
									(BitmapFactory.Options) null);
							try {
								Matrix matrix = new Matrix();
								switch (orientation) {
								case ExifInterface.ORIENTATION_ROTATE_90:
									// degree = 90;
									matrix.postRotate(90);
									bmp = Bitmap.createBitmap(bmp, 0, 0,
											bmp.getWidth(), bmp.getHeight(),
											matrix, true);
									break;
								case ExifInterface.ORIENTATION_ROTATE_180:
									// degree = 180;
									matrix.postRotate(180);
									bmp = Bitmap.createBitmap(bmp, 0, 0,
											bmp.getWidth(), bmp.getHeight(),
											matrix, true);
									break;
								case ExifInterface.ORIENTATION_ROTATE_270:
									// degree = 270;
									matrix.postRotate(270);
									bmp = Bitmap.createBitmap(bmp, 0, 0,
											bmp.getWidth(), bmp.getHeight(),
											matrix, true);
									break;
								default:

									break;
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}

						} else {
							bmp = MediaStore.Video.Thumbnails.getThumbnail(
									context.getContentResolver(), item.getId(),
									MediaStore.Images.Thumbnails.MINI_KIND,
									(BitmapFactory.Options) null);
						}
						if (myType == MediaItem.Type.IMAGE) {
							PhotoTableAdapter
									.getInstance(context)
									.add(orgFile
											.getAbsolutePath()
											.replace(
													AppPreference
															.getInstance(
																	context)
															.getExternalStorageDirectory(),
													""),
											newFile.getAbsolutePath()
													.replace(
															AppPreference
																	.getInstance(
																			context)
																	.getExternalStorageDirectory(),
															""),
											orgFile.getName(), duration,
											extention,
											ImageUtils.bitmapToByteArray(bmp),
											item.getDateModified(),
											item.getSolution());
						} else {
							VideoTableAdapter
									.getInstance(context)
									.add(orgFile
											.getAbsolutePath()
											.replace(
													AppPreference
															.getInstance(
																	context)
															.getExternalStorageDirectory(),
													""),
											newFile.getAbsolutePath()
													.replace(
															AppPreference
																	.getInstance(
																			context)
																	.getExternalStorageDirectory(),
															""),
											orgFile.getName(),
											duration,
											extention,
											bmp != null ? ImageUtils
													.bitmapToByteArray(bmp)
													: null);
						}
						if (null != bmp) {
							try {
								bmp.recycle();
								bmp = null;
							} catch (Exception ex) {
							}
						}

						progress++;
						// Required delete
						/*
						 * if (myType == MediaStorageItem.VIDEO) { Uri uri =
						 * Uri.withAppendedPath(
						 * MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						 * String.valueOf(item.id));
						 * context.getContentResolver().delete(uri, null, null);
						 * ======= progress++; >>>>>>> .r246
						 * 
						 * //Required delete /*if (myType ==
						 * MediaStorageItem.VIDEO) { Uri uri = Uri
						 * .withAppendedPath(
						 * MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						 * String.valueOf(item.id));
						 * context.getContentResolver() .delete(uri, null,
						 * null);
						 * 
						 * } else { Uri uri = Uri .withAppendedPath(
						 * MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						 * String.valueOf(item.id));
						 * context.getContentResolver() .delete(uri, null,
						 * null); }
						 */

						publishProgress();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				progress++;
			}

		}
		return null;
	}

	public void copyFile(File oldPath, File newPath) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File afile = oldPath;
			File bfile = newPath;

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			System.out.println("File is copied successful!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		if (myType == MediaItem.Type.IMAGE) {
			Toast.makeText(context, context.getString(R.string.image_imported),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, context.getString(R.string.video_imported),
					Toast.LENGTH_SHORT).show();
		}
		try {
			if ((this.mDialog != null) && this.mDialog.isShowing()) {
				this.mDialog.dismiss();
			}
		} catch (final IllegalArgumentException e) {
		} catch (final Exception e) {
		} finally {

		}
		// context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
		// .parse("file://" + Environment.getExternalStorageDirectory())));

		Intent i = new Intent(ACTION_ENCRYPTED);
		i.putExtra("DELETE", true);
		context.sendBroadcast(i);
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		int prog = (int) ((100.0 * progress) / items.size());
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.setMessage(context
					.getString(R.string.importing, prog + "%"));
			mDialog.setProgress(prog);
		}
		super.onProgressUpdate(values);
	}

}
