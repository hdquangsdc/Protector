package com.protector.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.protector.utils.ImageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class VideoPickerAdapter extends ArrayAdapter<MediaItem> {
    private boolean isSelectable;
    private ArrayList<MediaItem> mSelectedVideo;
    private DecimalFormat timeFormater = new DecimalFormat("00");
    private DecimalFormat fileSizeFormater = new DecimalFormat("##0.##");
    private Executor taskExecutor;
    private HashMap<String, Bitmap> bitmapCache;
    // private Activity myContext;
    private Activity myActivty;
    private LruCache<String, Bitmap> mMemoryCache;
    private OnTouchItemListerner mListener;

    public VideoPickerAdapter(Activity context, List<MediaItem> items, OnTouchItemListerner listener) {
        super(context, R.layout.item_video, items);
        mSelectedVideo = new ArrayList<>();
        isSelectable = true;
        taskExecutor = new ScheduledThreadPoolExecutor(3);
        bitmapCache = new LinkedHashMap<>();
        myActivty = (Activity) context;
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
        holder.tvDuration.setText(formatTime(duration));

        // if (file.exists()) {
        // String size = formatSize(file.length());
        // if (StoreLanguage.getInstance(myContext).getStore() == 0) {
        // } else {
        // size = size.replace(".", ",");
        // }
        // holder.tvSize.setText(size);
        // } else {
        // holder.tvSize.setText("");
        // }
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

        if (mSelectedVideo.contains(item)) {
        } else {
        }
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

    public void setSelectable(boolean isSelectable) {
        mSelectedVideo.clear();
        this.isSelectable = isSelectable;
        notifyDataSetChanged();
    }

    public boolean isSelectable() {
        return isSelectable;
    }

    public ArrayList<MediaItem> getSelectedItem() {
        return mSelectedVideo;
    }

    private String formatTime(long timeInMilisecond) {
        long hour = timeInMilisecond / (60 * 60 * 1000);
        long minute = (timeInMilisecond / (60 * 1000)) % 60;
        long second = (timeInMilisecond / 1000) % 60;
        return timeFormater.format(hour) + ":" + timeFormater.format(minute)
                + ":" + timeFormater.format(second);
    }

    private class ViewHolder {
        ImageView imgThumbnail, imgSelected;
        TextView tvTitle, tvDuration, tvSize;

    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private MediaItem myMediaItem;

        public BitmapWorkerTask(ImageView imageView, MediaItem item) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            myMediaItem = item;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // int id = Integer.parseInt(params[0]);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = ImageUtils.calculateInSampleSize(options,
                    100, 100);
            Bitmap mLoadedBitmap = null;
            options.inJustDecodeBounds = false;
            try {
                mLoadedBitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        myActivty.getContentResolver(),
                        myMediaItem != null ? myMediaItem.getId() : -1,
                        MediaStore.Video.Thumbnails.MINI_KIND, options);
            } catch (OutOfMemoryError e) {

            }
            addBitmapToMemoryCache(myMediaItem.getPath(), mLoadedBitmap);
            return mLoadedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = (ImageView) imageViewReference
                        .get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
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
            imageViewReference = new WeakReference<ImageView>(imageView);
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
                        if (imageViewReference != null && mLoadedBitmap != null) {
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

}
