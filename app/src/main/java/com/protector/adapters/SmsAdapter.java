package com.protector.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.protector.R;
import com.protector.objects.SmsCallLogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SmsAdapter extends ArrayAdapter<SmsCallLogItem> {
    private OnClickListener myOnClickListener;
    private ArrayList<SmsCallLogItem> myArrayChecked;
    private Activity myActivity;
    private OnTouchItemListerner mListener;

    public SmsAdapter(Context context, ArrayList<SmsCallLogItem> arrayJobs, OnTouchItemListerner listener) {
        super(context, R.layout.item_sms, arrayJobs);
        myArrayChecked = new ArrayList<>();
        myActivity = (Activity) context;
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_sms, null);
            holder = new ViewHolder();
            holder.imvIcon = (ImageView) convertView.findViewById(R.id.imv_sms);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvHours = (TextView) convertView.findViewById(R.id.tv_hours);
            holder.tvContent = (TextView) convertView
                    .findViewById(R.id.tv_content);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb_sms);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SmsCallLogItem sms = getItem(position);
        if (position != 0) {
            String d_s = getDate(sms.getTime());
            String d_f = getDate(getItem(position - 1).getTime());
            if (!(d_f.equals(d_s))) {
                holder.tvDate.setText(getDate(sms.getTime()));
                holder.tvDate.setVisibility(View.VISIBLE);
            } else {
                holder.tvDate.setText(getDate(sms.getTime()));
                holder.tvDate.setVisibility(View.GONE);
            }
        } else {
            holder.tvDate.setText(getDate(sms.getTime()));
            holder.tvDate.setVisibility(View.VISIBLE);
        }
        if (myArrayChecked.contains(sms)) {
            holder.cb.setChecked(true);
            convertView.findViewById(R.id.view_sms).setSelected(true);
        } else {
            holder.cb.setChecked(false);
            convertView.findViewById(R.id.view_sms).setSelected(false);
        }
        final SpannableStringBuilder sb = new SpannableStringBuilder(
                sms.getBodySms());
        final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(225,
                0, 0));
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
//		String strTextSearch = myActivity.getTextSearch();
//		int countChar = sms.getBodySms().toUpperCase()
//				.indexOf(strTextSearch.toUpperCase());
//		if (strTextSearch.length() > 0 && countChar >= 0) {
//			sb.setSpan(fcs, countChar, countChar + strTextSearch.length(),
//					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//			sb.setSpan(bss, countChar, countChar + strTextSearch.length(),
//					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//		}
        holder.tvContent.setText(sb);
        holder.tvHours.setText(getHours(sms.getTime()));
        String strFromOrTo = "";
        if (sms.getType() == 1) {
//            strFromOrTo = "From:";
            holder.imvIcon.setImageResource(R.drawable.ic_sms_from);
        } else if (sms.getType() == 2) {
//            strFromOrTo = "To:";
            holder.imvIcon.setImageResource(R.drawable.ic_sms_to);
        }
        if (sms.getName() == null || sms.getName().length() == 0) {
            sms.setName(getContactName(myActivity, sms.getAddress()));
            holder.tvName.setText(strFromOrTo + sms.getName());
        } else {
            holder.tvName.setText(strFromOrTo + sms.getName());
        }

        convertView.findViewById(R.id.view_sms).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SmsCallLogItem item = getItem(position);
                if (myArrayChecked.contains(item)) {
                    myArrayChecked.remove(item);
                    holder.cb.setChecked(false);
                } else {
                    myArrayChecked.add(item);
                    holder.cb.setChecked(true);
                }
                if (mListener != null) {
                    mListener.onTouch();
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    public ArrayList<SmsCallLogItem> getArrayChecks() {
        return myArrayChecked;
    }

    public interface OnTouchItemListerner {
        void onTouch();
    }

    private static class ViewHolder {
        ImageView imvIcon;
        TextView tvName;
        TextView tvDate;
        TextView tvHours;
        TextView tvContent;
        CheckBox cb;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        myOnClickListener = onClickListener;
    }

    public static interface OnClickListener {
        public void onClick(int position);
    }

    public String getDate(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        return f.format(date);
    }

    public String getHours(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        return f.format(date);
    }

    public int getCountChecked() {
        return myArrayChecked.size();
    }

    public void clearChecked() {
        myArrayChecked.clear();
    }

    public String getContactName(Context context, String phoneNumber) {
        Cursor cursor = null;
        String contactName = phoneNumber;
        if (phoneNumber == null
                || (phoneNumber != null && phoneNumber.length() == 0)) {
            return "No name";
        }
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor
                        .getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception ex) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactName;
    }
}

