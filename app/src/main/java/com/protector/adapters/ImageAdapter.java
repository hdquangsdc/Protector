
package com.protector.adapters;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.PhotoTableAdapter;
import com.protector.objects.MediaStorageItem;
import com.protector.utils.AndroidVersion;
import com.protector.utils.ImageUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ImageAdapter extends BaseAdapter {
    private Activity mActivity;
    private LayoutInflater mInflater;
	private ArrayList<MediaStorageItem> mMediaList;
    private OnTouchListener mListener;


    private ArrayList<MediaStorageItem> mSelectedList;
	private LruCache<String, Bitmap> mMemoryCache;
	private PhotoTableAdapter mDBAdapter;
	private Executor mExcutor;

    public ImageAdapter(Activity activity) {
        this.mActivity = activity;
        this.mInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mMediaList=new ArrayList<>();
		this.mSelectedList = new ArrayList<>();
		mDBAdapter = PhotoTableAdapter.getInstance(mActivity);
		mExcutor = new ScheduledThreadPoolExecutor(3);
		final int memClass = ((ActivityManager) mActivity
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 2;
		this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				if (AndroidVersion.isHoneycombMr2OrHigher()) {
					return bitmap.getByteCount();
				} else
					return (bitmap.getRowBytes() * bitmap.getHeight());
			}
		};
	}

	@Override
	public int getCount() {
		return mMediaList.size();
	}

	@Override
	public MediaStorageItem getItem(int index) {
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

		MediaStorageItem item = getItem(position);
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

//		holder.imvCheck.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				holder.imvCheck.setSelected(!holder.imvCheck.isSelected());
//				getItem(position).setSelected(holder.imvCheck.isSelected());
//			}
//		});

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MediaStorageItem item = getItem(position);
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

		final Bitmap bm = getBitmapFromMemCache(item.getId() + "");
		if (bm == null) {
			mExcutor.execute(new LoadImageRunable(holder.imvImage, item.getId()));
		} else {
			if (!bm.isRecycled()) {
				holder.imvImage.setImageBitmap(bm);
			}
		}

		return convertView;
	}

	private static class ViewHolder {
		ImageView imvImage;
		ImageView imvCheck;
		View vBorder;
		TextView tvIndex;
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}
	
	public void addAll(ArrayList<MediaStorageItem> files) {
		if (files == null) {
			return;
		}
		try {
			this.mMediaList.clear();
			for (MediaStorageItem item : files) {
				this.mMediaList.add(item);
			}
			// this.data.addAll(files);

		} catch (Exception e) {
			e.printStackTrace();
		}

		notifyDataSetChanged();
	}

	private class LoadImageRunable implements Runnable {
		private final WeakReference<ImageView> imageViewReference;
		private long mID = -1;
		private Bitmap mLoadedBitmap, myBitmap;

		public LoadImageRunable(ImageView imageView, long id) {
			imageViewReference = new WeakReference<>(imageView);
			mID = id;
		}

		@Override
		public void run() {
			try {
				mLoadedBitmap = ImageUtils.bitmapFromByteArray(mDBAdapter
						.getThumbnail(mID));
				/*
				 * myBitmap = Bitmap.createScaledBitmap(mLoadedBitmap, 150, 150,
				 * true); mLoadedBitmap.recycle();
				 */
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
				myBitmap = Bitmap.createScaledBitmap(mLoadedBitmap, width,
						height, true);
				addBitmapToMemoryCache(String.valueOf(mID), myBitmap);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (imageViewReference != null && myBitmap != null) {
							final ImageView imageView =  imageViewReference
									.get();
							if (imageView != null) {
								imageView.setImageBitmap(myBitmap);
							}
						}
					}
				});
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public ArrayList<MediaStorageItem> getmSelectedList() {
        return mSelectedList;
    }

    public void setOnTouchListener(OnTouchListener listener){
        this.mListener=listener;
    }

    public interface OnTouchListener{
        void onTouch();
    }
}
