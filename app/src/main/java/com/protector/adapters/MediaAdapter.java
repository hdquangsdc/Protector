package com.protector.adapters;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.objects.MediaItem;
import com.protector.utils.AndroidVersion;
import com.protector.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MediaAdapter extends BaseAdapter {
	private Activity mActivity;
	private LayoutInflater mInflater;
	private ArrayList<MediaItem> mMediaList;
	private ArrayList<MediaItem> mSelectedList;
	private LruCache<String, Bitmap> mMemoryCache;
	private Executor mExcutor;
	private int mMaxImageWidth;
    private OnTouchItemListerner mListener;

	public MediaAdapter(Activity activity, ArrayList<MediaItem> mMediaList, OnTouchItemListerner listener) {
		this.mActivity = activity;
		this.mMediaList = mMediaList;
		this.mSelectedList = new ArrayList<MediaItem>();
		this.mInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final int memClass = ((ActivityManager) mActivity
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 2;
		this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				if (AndroidVersion.isHoneycombMr2OrHigher()) {
					return bitmap.getByteCount();
				} else {
                    return (bitmap.getRowBytes() * bitmap.getHeight());
                }
			}
		};
		mExcutor = new ScheduledThreadPoolExecutor(20);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		mMaxImageWidth = displaymetrics.widthPixels / 3;

        mListener=listener;
	}

	@Override
	public int getCount() {
		return mMediaList.size();
	}

	@Override
	public MediaItem getItem(int index) {
		return mMediaList.get(index);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_pick_media, parent,
					false);
			holder = new ViewHolder();
			holder.imvImage = (ImageView) convertView
					.findViewById(R.id.imgQueue);

			holder.imvCheck = (ImageView) convertView
					.findViewById(R.id.imgQueueMultiSelected);
			holder.vBorder =  convertView.findViewById(R.id.view_boder);
			holder.tvIndex = (TextView) convertView.findViewById(R.id.tv_index);

			convertView.setTag(holder);

			// if (getItem(position).type == Type.VIDEO) {
			// holder.imgVideo.setVisibility(View.VISIBLE);
			// } else {
			// holder.imgVideo.setVisibility(View.GONE);
			// }
			// holder.imvCheck.setSelected(mMediaList.get(position).isSeleted);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		MediaItem item = getItem(position);
		if (mSelectedList.contains(item)) {
			holder.imvImage.setAlpha(0.5f);
			holder.vBorder.setVisibility(View.VISIBLE);
			holder.tvIndex.setVisibility(View.VISIBLE);
			holder.tvIndex.setText("" + (mSelectedList.indexOf(item) + 1));
		} else {
			holder.imvImage.setAlpha(1f);
			holder.vBorder.setVisibility(View.INVISIBLE);
			holder.tvIndex.setVisibility(View.INVISIBLE);
		}

		holder.imvCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				holder.imvCheck.setSelected(!holder.imvCheck.isSelected());
				getItem(position).setSelected(holder.imvCheck.isSelected());
			}
		});

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MediaItem item = getItem(position);
				if (mSelectedList.contains(item)) {
					mSelectedList.remove(item);
					holder.vBorder.setVisibility(View.INVISIBLE);
					holder.tvIndex.setVisibility(View.INVISIBLE);
				} else {
					mSelectedList.add(item);
					holder.vBorder.setVisibility(View.VISIBLE);
					holder.tvIndex.setVisibility(View.VISIBLE);
				}
                if (mListener!=null){
                    mListener.onTouch();
                }
				notifyDataSetChanged();

			}
		});
		int imageWidth = mMaxImageWidth;
		if (convertView.getWidth() > 0) {
			imageWidth = convertView.getWidth();
		}

		final Bitmap bm = getBitmapFromMemCache(item.getPath());
		if (bm == null) {
			mExcutor.execute(new LoadImageRunable(holder.imvImage, item,
					imageWidth));
		} else {
			if (!bm.isRecycled()) {
				holder.imvImage.setImageBitmap(bm);
			}
		}

		return convertView;
	}

    public interface OnTouchItemListerner{
        void onTouch();
    }

	private static class ViewHolder {
		ImageView imvImage;
		ImageView imvCheck;
		View vBorder;
		TextView tvIndex;
	}

	private class LoadImageRunable implements Runnable {
		private final WeakReference<ImageView> refImageView;
		private MediaItem mMediaItem;
		private int mSize;

		public LoadImageRunable(ImageView imageView, MediaItem item, int size) {
			refImageView = new WeakReference<>(imageView);
			mMediaItem = item;
			mSize = size;
		}

		@Override
		public void run() {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = ImageUtils.calculateInSampleSize(options,
					mSize, mSize);
			try {
				Bitmap mLoadedBitmap;
				options.inJustDecodeBounds = false;
				Bitmap tmpBitmap = MediaStore.Images.Thumbnails.getThumbnail(
						mActivity.getContentResolver(),
						mMediaItem != null ? mMediaItem.getId() : -1,
						MediaStore.Images.Thumbnails.MINI_KIND, null);
				File imageFile = new File(mMediaItem.getPath());

				if (imageFile.exists()) {
					ExifInterface ei;
					ei = new ExifInterface(imageFile.getAbsolutePath());
					int orientation = ei.getAttributeInt(
							ExifInterface.TAG_ORIENTATION,
							ExifInterface.ORIENTATION_NORMAL);
					Matrix matrix = new Matrix();
					switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						// degree = 90;
						matrix.postRotate(90);
						mLoadedBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0,
								tmpBitmap.getWidth(), tmpBitmap.getHeight(),
								matrix, true);
						tmpBitmap.recycle();
						tmpBitmap = null;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						// degree = 180;
						matrix.postRotate(180);
						mLoadedBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0,
								tmpBitmap.getWidth(), tmpBitmap.getHeight(),
								matrix, true);
						tmpBitmap.recycle();
						tmpBitmap = null;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						// degree = 270;
						matrix.postRotate(270);
						mLoadedBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0,
								tmpBitmap.getWidth(), tmpBitmap.getHeight(),
								matrix, true);
						tmpBitmap.recycle();
						tmpBitmap = null;
						break;
					default:
						mLoadedBitmap = tmpBitmap;
						break;
					}

				} else {
					mLoadedBitmap = tmpBitmap;
				}
				if (mLoadedBitmap != null) {
					int width = mLoadedBitmap.getWidth();
					int height = mLoadedBitmap.getHeight();

					float bitmapRatio = (float) width / (float) height;
					if (bitmapRatio <= 1) {
						width = 150;
						height = (int) (width / bitmapRatio);
					} else {
						height = 150;
						width = (int) (height * bitmapRatio);
					}
					final Bitmap myBitmap = Bitmap.createScaledBitmap(
							mLoadedBitmap, width, height, true);
					mLoadedBitmap.recycle();
					mLoadedBitmap = null;

					addBitmapToMemoryCache(mMediaItem.getPath(), myBitmap);
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (refImageView != null && myBitmap != null) {
								final ImageView imageView = (ImageView) refImageView
										.get();
								if (imageView != null) {
									imageView.setImageBitmap(myBitmap);
								}
							}
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {		
		return mMemoryCache.get(key);
	}

	public ArrayList<MediaItem> getSelectedItem() {
		return mSelectedList;
	}
}
