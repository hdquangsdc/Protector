package com.protector.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.objects.MediaItem;
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

public class VideoPickerAdapter extends ArrayAdapter<MediaItem> {
    private ArrayList<MediaItem> mSelectedVideo;


    private Executor taskExecutor;
    private Activity myActivty;
    private LruCache<String, Bitmap> mMemoryCache;
    private OnTouchItemListerner mListener;

    public VideoPickerAdapter(Activity context, List<MediaItem> items, OnTouchItemListerner listener) {
        super(context, R.layout.item_video, items);
        mSelectedVideo = new ArrayList<>();
        taskExecutor = new ScheduledThreadPoolExecutor(3);
        myActivty = context;
        final int memClass = ((ActivityManager) myActivty
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
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
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
        MediaItem item = getItem(position);
        File file = new File(item.getPath());

        final Bitmap bm = getBitmapFromMemCache(item.getPath());
        if (bm == null) {
            taskExecutor
                    .execute(new LoadImageRunable(holder.imgThumbnail, item));
        } else {
            holder.imgThumbnail.setImageBitmap(bm);
        }
        holder.tvTitle.setText(file.getName());
        long duration = 0;
        Cursor cursor = null;
        try {
            Uri uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(item.getId()));
            cursor = MediaStore.Video.query(getContext().getContentResolver(),
                    uri,
                    new String[]{MediaStore.Video.VideoColumns.DURATION});

            if (cursor.moveToFirst())
                duration = cursor.getLong(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        holder.tvDuration.setText(DateTimeUtils.formatTime(duration));

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
        // MediaItem item = getItem(position);
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
        // myActivty.resetButtonNext();
        // }
        //
        // }
        // });

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MediaItem item = getItem(position);
                if (mSelectedVideo.contains(item)) {
                    mSelectedVideo.remove(item);
                } else {
                    mSelectedVideo.add(item);
                }
                if (mListener != null) {
                    mListener.onTouch();
                }
                notifyDataSetChanged();
            }
        });
        if (mSelectedVideo.contains(item)) {
            holder.imgSelected.setVisibility(View.VISIBLE);
        } else {
            holder.imgSelected.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public interface OnTouchItemListerner {
        void onTouch();
    }

    public ArrayList<MediaItem> getSelectedItem() {
        return mSelectedVideo;
    }



    private class ViewHolder {
        ImageView imgThumbnail, imgSelected;
        TextView tvTitle, tvDuration, tvSize;

    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private class LoadImageRunable implements Runnable {
        private final WeakReference<ImageView> imageViewReference;
        private MediaItem myMediaItem;

        public LoadImageRunable(ImageView imageView, MediaItem item) {
            imageViewReference = new WeakReference<>(imageView);
            myMediaItem = item;
        }

        @Override
        public void run() {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = ImageUtils.calculateInSampleSize(
                        options, 100, 100);
                final Bitmap mLoadedBitmap;
                options.inJustDecodeBounds = false;
                mLoadedBitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        myActivty.getContentResolver(),
                        myMediaItem != null ? myMediaItem.getId() : -1,
                        MediaStore.Video.Thumbnails.MINI_KIND, options);
                addBitmapToMemoryCache(myMediaItem.getPath(), mLoadedBitmap);
                myActivty.runOnUiThread(new Runnable() {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String formatSize(long fileSizeInByte) {
        return FileUtils.getFileSizeInMegabyte(fileSizeInByte)
                + " " + myActivty.getString(R.string.megabyte);
    }

}
