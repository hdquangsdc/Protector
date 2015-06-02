package com.protector.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.PasswordTableAdapter;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.objects.SmsCallLogItem;
import com.protector.utils.AndroidVersion;
import com.protector.utils.DateTimeUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class CallLogAdapter extends ArrayAdapter<SmsCallLogItem> {
    private OnClickListener myOnClickListener;
    private ArrayList<SmsCallLogItem> myArrayChecked;
    private Activity myActivity;
    private int myType;
    private AlertDialog.Builder myDialog;
    private PrivateContactTableAdapter myContactTable;
    private Executor taskExecutor;
    private LruCache<String, Bitmap> mMemoryCache;

    public CallLogAdapter(Context context,
                          ArrayList<SmsCallLogItem> arrayJobs, int type) {
        super(context, R.layout.item_call_log, arrayJobs);
        myArrayChecked = new ArrayList<>();
        myActivity = (Activity) context;
        myType = type;
        myContactTable = PrivateContactTableAdapter.getInstance(myActivity);
        myDialog = new AlertDialog.Builder(myActivity)
                .setTitle(
                        myActivity.getResources().getString(
                                R.string.new_private))
                .setMessage(myActivity.getString(R.string.already_contact))
                .setNegativeButton(myActivity.getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
        taskExecutor = new ScheduledThreadPoolExecutor(20);

        final int memClass = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 2;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (AndroidVersion.isHoneycombMr2OrHigher()) {
                    return bitmap.getByteCount();
                }
                return (bitmap.getRowBytes() * bitmap.getHeight());
            }

        };
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_call_log, parent, false);
            holder = new ViewHolder();
            holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvHours = (TextView) convertView.findViewById(R.id.tv_hours);
            holder.tvAddress = (TextView) convertView
                    .findViewById(R.id.tv_address);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb_select);
            holder.imgAvatar = (ImageView) convertView
                    .findViewById(R.id.img_avatar);
            holder.imgIcon = (ImageView) convertView
                    .findViewById(R.id.img_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SmsCallLogItem callLog = getItem(position);
        if (myArrayChecked.contains(callLog)) {
            holder.cb.setChecked(true);
            convertView.setBackgroundColor(Color.parseColor("#eeeeee"));
        } else {
            holder.cb.setChecked(false);
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        holder.tvAddress.setText(callLog.getAddress());
        holder.tvHours.setText(DateTimeUtils.getHoursString(callLog.getTime()));
        holder.tvDate.setText(DateTimeUtils.getDateString(myActivity, callLog.getTime()));
        if (callLog.getAvatarByte() != null) {
            final Bitmap bm = getBitmapFromMemCache(callLog.getAddress() + "");
            if (bm == null) {
                taskExecutor.execute(new LoadImageRunable(holder.imgAvatar,
                        callLog));
            } else {
                if (!bm.isRecycled()) {
                    holder.imgAvatar.setImageBitmap(bm);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }
        if (callLog.getName() != null && callLog.getName().length() > 0) {
            holder.tvName.setText(callLog.getName());
        } else {
            holder.tvName.setText(callLog.getAddress());
        }
        switch (callLog.getType()) {
            case 1:
                holder.imgIcon.setImageResource(R.drawable.ic_incoming_item);
                break;
            case 2:
                holder.imgIcon.setImageResource(R.drawable.ic_outgoing_item);
                break;
            case 3:
                holder.imgIcon.setImageResource(R.drawable.ic_call_miss_item);
                break;
            default:
                holder.imgIcon.setImageResource(R.drawable.ic_incoming_item);
                break;
        }

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SmsCallLogItem item = getItem(position);
                if (myArrayChecked.contains(item)) {
                    myArrayChecked.remove(item);
                    holder.cb.setChecked(false);
                } else {
                    if (myType == PrivateContactTableAdapter.TYPE_PRIVATE) {
                        if (!myContactTable.checkContactByAddress(
                                item.getAddress(), myType,
                                PasswordTableAdapter.PASSWORD_CURRENT_ID)) {
                            myArrayChecked.add(item);
                            holder.cb.setChecked(true);
                        } else {
                            myDialog.show();
                        }
                    } else {
                        myArrayChecked.add(item);
                        holder.cb.setChecked(true);
                    }
                }
                notifyDataSetChanged();
            }
        };
        holder.cb.setOnClickListener(listener);
        convertView.setOnClickListener(listener);
        return convertView;
    }

    public ArrayList<SmsCallLogItem> getArrayChecks() {
        return myArrayChecked;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvHours;
        TextView tvAddress;
        CheckBox cb;
        ImageView imgAvatar, imgIcon;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        myOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int position);
    }

    private class LoadImageRunable implements Runnable {
        private final WeakReference<ImageView> imageViewReference;
        private SmsCallLogItem myObject;

        public LoadImageRunable(ImageView imageView, SmsCallLogItem object) {
            imageViewReference = new WeakReference<>(imageView);
            myObject = object;
        }

        @Override
        public void run() {
            try {
                final Bitmap mLoadedBitmap;
                mLoadedBitmap = getBitmap(myObject.getAvatarByte());
                addBitmapToMemoryCache(myObject.getAddress() + "", mLoadedBitmap);
                myActivity
                        .runOnUiThread(new Runnable() {
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

    public Bitmap getBitmap(byte[] data) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        return BitmapFactory.decodeByteArray(data, 0, data.length,
                opts);

    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void clearBitmap(String key) {
        mMemoryCache.remove(key);
    }
}
