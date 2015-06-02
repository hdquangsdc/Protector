package com.protector.adapters;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaStorageItem;
import com.protector.utils.AndroidVersion;
import com.protector.utils.DateTimeUtils;
import com.protector.utils.FileUtils;
import com.protector.utils.ImageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class VideoAdapter extends ArrayAdapter<MediaStorageItem> {

    private ArrayList<MediaStorageItem> mSelectedVideo;


    private Executor taskExecutor;

    private VideoTableAdapter mDBAdapter;

    private Activity mActivity;
    private LruCache<String, Bitmap> mMemoryCache;

    public VideoAdapter(Activity context, List<MediaStorageItem> items) {
        super(context, R.layout.item_video, items);
        mSelectedVideo = new ArrayList<>();
        taskExecutor = new ScheduledThreadPoolExecutor(3);

        mDBAdapter = VideoTableAdapter.getInstance(context);
        mActivity = context;
        final int memClass = ((ActivityManager) mActivity
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (AndroidVersion.isHoneycombMr2OrHigher()) {
                    return bitmap.getByteCount();
                }
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_video, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = (ImageView) convertView
                    .findViewById(R.id.iv_video_thumbnail);
            holder.imgSelected = (ImageView) convertView
                    .findViewById(R.id.iv_checked);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvSize = (TextView) convertView.findViewById(R.id.tv_size);
            holder.tvDuration = (TextView) convertView
                    .findViewById(R.id.tv_duration);
            holder.tvTitle.setSelected(true);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MediaStorageItem item = getItem(position);
        final Bitmap bm = getBitmapFromMemCache(getItem(position).getId() + "");
        if (bm == null) {
            taskExecutor.execute(new LoadImageRunable(holder.imgThumbnail,
                    getItem(position).getId()));
        } else {
            if (!bm.isRecycled()) {
                holder.imgThumbnail.setImageBitmap(bm);
            }
        }

        holder.tvTitle.setText(item.getName());
        holder.tvDuration.setText(DateTimeUtils.formatTime(item.getVideoTime()));
        File file = new File(item.getNewPath());
        if (!file.exists()) {
            file = new File(item.getNewPath() + "." + item.getExtention());
        }

        if (mSelectedVideo.contains(item)) {
            holder.imgSelected.setVisibility(View.VISIBLE);
            convertView.setSelected(true);
        } else {
            holder.imgSelected.setVisibility(View.INVISIBLE);
            convertView.setSelected(false);
        }
        if (file.exists()) {
            String size = formatSize(file.length());
            holder.tvSize.setText(size);
        } else {
            holder.tvSize.setText("");
        }
        // if (!isSelectable) {
        // holder.imgSelected.setVisibility(View.GONE);
        // } else {
        // if (selectedItem.contains(item)) {
        // holder.imgSelected.setVisibility(View.VISIBLE);
        // holder.imgSelected.setImageDrawable(myContext.getResources()
        // .getDrawable(R.drawable.radioselected));
        // } else {
        // holder.imgSelected.setVisibility(View.VISIBLE);
        // holder.imgSelected.setImageDrawable(myContext.getResources()
        // .getDrawable(R.drawable.radionormal));
        // }
        // }
        // convertView.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // MediaStorageItem item = getItem(position);
        // if (isSelectable) {
        // ViewHolder holder = (ViewHolder) v.getTag();
        // if (selectedItem.contains(item)) {
        // selectedItem.remove(item);
        // holder.imgSelected.setVisibility(View.VISIBLE);
        // holder.imgSelected.setImageDrawable(myContext
        // .getResources().getDrawable(
        // R.drawable.radionormal));
        // } else {
        // selectedItem.add(item);
        // holder.imgSelected.setVisibility(View.VISIBLE);
        // holder.imgSelected.setImageDrawable(myContext
        // .getResources().getDrawable(
        // R.drawable.radioselected));
        // }
        // } else {
        // if (mListener != null) {
        // mListener.onClickItem(item, position);
        // }
        // }
        //
        // }
        // });
        // convertView.setOnLongClickListener(new View.OnLongClickListener() {
        //
        // @Override
        // public boolean onLongClick(View v) {
        // if (mListener != null) {
        // mListener.onLongClickItem(getItem(position), position);
        // }
        // return false;
        // }
        // });
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSelectedVideo.add(getItem(position));
                if (mListener != null) {
                    mListener.onTouch();
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }


    public ArrayList<MediaStorageItem> getSelectedItem() {
        return mSelectedVideo;
    }


    private class ViewHolder {
        ImageView imgThumbnail, imgSelected;
        TextView tvTitle, tvDuration, tvSize;
    }

    private class LoadImageRunable implements Runnable {
        private final WeakReference<ImageView> imageViewReference;
        private long mID = -1;

        public LoadImageRunable(ImageView imageView, long id) {
            imageViewReference = new WeakReference<>(imageView);
            mID = id;
        }

        @Override
        public void run() {
            try {
                final Bitmap mLoadedBitmap;
                mLoadedBitmap = ImageUtils.bitmapFromByteArray(mDBAdapter
                        .getThumbnail(mID));
                addBitmapToMemoryCache(String.valueOf(mID), mLoadedBitmap);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadedBitmap != null) {
                            final ImageView imageView = imageViewReference
                                    .get();
                            if (imageView != null) {
                                imageView.setImageBitmap(mLoadedBitmap);
                            }
                        }
                    }
                });
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnActionListener {
        public void onClickItem(MediaStorageItem item, int position);

        public void onLongClickItem(MediaStorageItem item, int position);
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private OnTouchListener mListener;

    public void setOnTouchListener(OnTouchListener listener) {
        this.mListener = listener;
    }

    public interface OnTouchListener {
        void onTouch();
    }

    private String formatSize(long fileSizeInByte) {
        return FileUtils.getFileSizeInMegabyte(fileSizeInByte)
                + " " + mActivity.getString(R.string.megabyte);
    }
}
