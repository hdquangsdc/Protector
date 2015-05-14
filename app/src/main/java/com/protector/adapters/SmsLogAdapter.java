package com.protector.adapters;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
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

public class SmsLogAdapter extends ArrayAdapter<SmsCallLogItem> {
    private OnClickListener myOnClickListener;
    private ArrayList<Integer> myArrayChecked;
    private Activity myActivity;
    private int myType;
    private PrivateContactTableAdapter myContactTable;
    private AlertDialog.Builder myDialog;
    private Executor taskExecutor;
    private LruCache<String, Bitmap> mMemoryCache;

    public SmsLogAdapter(Context context, ArrayList<SmsCallLogItem> arraySms,
                         int type) {
        super(context, R.layout.item_sms_log, arraySms);
        myArrayChecked = new ArrayList<Integer>();
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
                    R.layout.item_sms_log, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvContent = (TextView) convertView
                    .findViewById(R.id.tv_content);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb_select);
            holder.imgAvatar = (ImageView) convertView
                    .findViewById(R.id.img_avatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final SmsCallLogItem sms = getItem(position);
        if (myArrayChecked.contains(position)) {
            holder.cb.setChecked(true);
        } else {
            holder.cb.setChecked(false);
        }
        holder.tvContent.setText(sms.getBodySms());
        holder.tvDate.setText(getDate(sms.getTime()));
        if (sms.getName() == null || sms.getName().length() == 0) {
            sms.setName(getContactName(myActivity, sms.getAddress()));
        } else {
        }
        if (sms.getAddress().equals(sms.getName())) {
            holder.tvName.setText(sms.getName());
        } else {
            holder.tvName.setText(sms.getName() + "(" + sms.getAddress() + ")");
        }
        if (sms.getAvatarByte() != null) {
            final Bitmap bm = getBitmapFromMemCache(sms.getAddress() + "");
            if (bm == null) {
                taskExecutor
                        .execute(new LoadImageRunable(holder.imgAvatar, sms));
            } else {
                if (!bm.isRecycled()) {
                    holder.imgAvatar.setImageBitmap(bm);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }
        holder.cb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (myArrayChecked.contains(position)) {
                    myArrayChecked.remove((Object) position);
                    holder.cb.setChecked(false);
                } else {
                    if (myType == PrivateContactTableAdapter.TYPE_PRIVATE) {
                        String[] arrAdr = sms.getAddress().split(";");
                        if (arrAdr.length > 1) {
                            myArrayChecked.add(position);
                            holder.cb.setChecked(true);
                        } else {
                            if (!myContactTable.checkContactByAddress(
                                    sms.getAddress(), myType,
                                    PasswordTableAdapter.PASSWORD_CURRENT_ID)) {
                                myArrayChecked.add(position);
                                holder.cb.setChecked(true);
                            } else {
                                myDialog.show();
                            }
                        }
                    } else {
                        myArrayChecked.add(position);
                        holder.cb.setChecked(true);
                    }
                }
//				myActivity.resetButtonNext();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myArrayChecked.contains(position)) {
                    myArrayChecked.remove((Object) position);
                    holder.cb.setChecked(false);
                } else {
                    if (myType == PrivateContactTableAdapter.TYPE_PRIVATE) {
                        String[] arrAdr = sms.getAddress().split(";");
                        if (arrAdr.length > 1) {
                            myArrayChecked.add(position);
                            holder.cb.setChecked(true);
                        } else {
                            if (!myContactTable.checkContactByAddress(
                                    sms.getAddress(), myType,
                                    PasswordTableAdapter.PASSWORD_CURRENT_ID)) {
                                myArrayChecked.add(position);
                                holder.cb.setChecked(true);
                            } else {
                                myDialog.show();
                            }
                        }
                    } else {
                        myArrayChecked.add(position);
                        holder.cb.setChecked(true);
                    }
                }
//				myActivity.resetButtonNext();
            }
        });
        return convertView;
    }

    public ArrayList<Integer> getArrayChecks() {
        return myArrayChecked;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvContent;
        TextView tvDate;
        CheckBox cb;
        ImageView imgAvatar;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        myOnClickListener = onClickListener;
    }

    public static interface OnClickListener {
        public void onClick(int position);
    }

    public int getCountChecked() {
        return myArrayChecked.size();
    }

    public void clearChecked() {
        myArrayChecked.clear();
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = phoneNumber;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    public String getDate(long milliseconds) {
        Date d = new Date(milliseconds);
        Calendar calNow = Calendar.getInstance();
        Calendar calYesterday = Calendar.getInstance();
        calYesterday.add(Calendar.DATE, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        String dateNow = dateFormat.format(calNow.getTime());
        String dateYesterday = dateFormat.format(calYesterday.getTime());
        String dateLog = dateFormat.format(d.getTime());

        if (dateLog.equals(dateNow))
            return getHours(milliseconds);
        else if (dateLog.compareTo(dateYesterday) == 0)
            return myActivity.getString(R.string.yesterday);
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("MM-dd");
        return f.format(date);
    }

    public String getHours(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        return f.format(date);
    }

    private class LoadImageRunable implements Runnable {
        private final WeakReference<ImageView> imageViewReference;
        private SmsCallLogItem myObject;

        public LoadImageRunable(ImageView imageView, SmsCallLogItem object) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            myObject = object;
        }

        @Override
        public void run() {
            try {
                final Bitmap mLoadedBitmap;
                mLoadedBitmap = getBitmap(myObject.getAvatarByte());
                addBitmapToMemoryCache(myObject.getAddress() + "",
                        mLoadedBitmap);
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (imageViewReference != null && mLoadedBitmap != null) {
                            final ImageView imageView = (ImageView) imageViewReference
                                    .get();
                            if (imageView != null) {
                                imageView.setImageBitmap(mLoadedBitmap);
                            }
                        }
                    }
                });
            } catch (OutOfMemoryError e) {

            }
        }
    }

    public Bitmap getBitmap(byte[] data) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                opts);
        return bitmap;

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
