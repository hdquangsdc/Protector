package com.protector.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.SmsCallLogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DetailContactLockedAdapter extends ArrayAdapter<SmsCallLogItem> {
	private OnLongClickListener myOnLongClickListener;
	private Context myContext;

	public DetailContactLockedAdapter(Context context,
			ArrayList<SmsCallLogItem> arrayContact) {
		super(context, R.layout.activity_detail_contact_locked_item,
				arrayContact);
		myContext = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.activity_detail_contact_locked_item, null);
			holder = new ViewHolder();
			holder.llReceive = convertView.findViewById(R.id.ll_recieve);
			holder.llSend = convertView.findViewById(R.id.ll_send);
			holder.llCallLog = convertView.findViewById(R.id.ll_call_log);
			holder.tvBodyReceive = (TextView) convertView
					.findViewById(R.id.tv_body_receive);
			holder.tvDateReceive = (TextView) convertView
					.findViewById(R.id.tv_date_receive);
			holder.imgNewReceive = (ImageView) convertView
					.findViewById(R.id.img_new);

			holder.tvBodySend = (TextView) convertView
					.findViewById(R.id.tv_body_send);
			holder.tvDateSend = (TextView) convertView
					.findViewById(R.id.tv_date_send);
			holder.tvTypeSend = (TextView) convertView
					.findViewById(R.id.tv_type_send);
			holder.imgTypeSend = (ImageView) convertView
					.findViewById(R.id.img_type_send);

			holder.imgTypeCallLog = (ImageView) convertView
					.findViewById(R.id.img_type_call_log);
			holder.tvDateCallLog = (TextView) convertView
					.findViewById(R.id.tv_date_call_log);
			holder.tvTypeCallLog = (TextView) convertView
					.findViewById(R.id.tv_type_call_log);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SmsCallLogItem contact = getItem(position);
		if (contact.getTypeCompare() == 1) {
			holder.llCallLog.setVisibility(View.GONE);
			if (contact.getType() == SmsCallLogTableAdapter.TYPE_SMS_INBOX) {
				holder.llReceive.setVisibility(View.VISIBLE);
				holder.llSend.setVisibility(View.GONE);
				holder.tvBodyReceive.setText(contact.getBodySms());
				holder.tvDateReceive.setText(getDate(contact.getTime()));
				if (contact.getRead() == 1) {
					holder.imgNewReceive.setVisibility(View.GONE);
				} else {
					holder.imgNewReceive.setVisibility(View.VISIBLE);
				}
			} else {
				holder.llReceive.setVisibility(View.GONE);
				holder.llSend.setVisibility(View.VISIBLE);
				holder.tvBodySend.setText(contact.getBodySms());
				holder.tvDateSend.setText(getDate(contact.getTime()));
				if (contact.getType() == SmsCallLogTableAdapter.TYPE_SMS_SEND) {
					holder.tvTypeSend.setVisibility(View.INVISIBLE);
					holder.imgTypeSend.setVisibility(View.INVISIBLE);
					/*
					 * holder.tvTypeSend.setText("Sent");
					 * holder.imgTypeSend.setImageDrawable
					 * (myContext.getResources
					 * ().getDrawable(R.drawable.ic_tick));
					 */
				} else {
					holder.tvTypeSend.setVisibility(View.INVISIBLE);
					holder.imgTypeSend.setVisibility(View.INVISIBLE);
					/*
					 * holder.tvTypeSend.setText("");
					 * holder.imgTypeSend.setImageDrawable(null);
					 */
				}

			}

		} else {
			holder.llReceive.setVisibility(View.GONE);
			holder.llSend.setVisibility(View.GONE);
			holder.llCallLog.setVisibility(View.VISIBLE);
			holder.tvDateCallLog.setText(getDate(contact.getTime()));
			if (contact.getType() == SmsCallLogTableAdapter.TYPE_CALL_INCOMING) {
				holder.tvTypeCallLog.setText(myContext.getResources()
						.getString(R.string.incomming_call));
				holder.imgTypeCallLog.setImageDrawable(myContext.getResources()
						.getDrawable(R.drawable.ic_incoming_item));
			} else if (contact.getType() == SmsCallLogTableAdapter.TYPE_CALL_OUTGOING) {
				holder.tvTypeCallLog.setText(myContext.getResources()
						.getString(R.string.outgoing_call));
				holder.imgTypeCallLog.setImageDrawable(myContext.getResources()
						.getDrawable(R.drawable.ic_outgoing_item));
			} else {
				holder.tvTypeCallLog.setText(myContext.getResources()
						.getString(R.string.missed_call));
				holder.imgTypeCallLog.setImageDrawable(myContext.getResources()
						.getDrawable(R.drawable.ic_call_miss_item));
			}
		}
		convertView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (myOnLongClickListener != null) {
					myOnLongClickListener.onLongClick(position);
				}
				return false;
			}
		});
		/*
         * if (position != 0) { String d_s = getDateString(sms.getTime()); String d_f
		 * = getDateString(getItem(position - 1).getTime()); if (!(d_f.equals(d_s))) {
		 * holder.tvDate.setText(getDateString(sms.getTime()));
		 * holder.tvDate.setVisibility(View.VISIBLE); } else {
		 * holder.tvDate.setText(getDateString(sms.getTime()));
		 * holder.tvDate.setVisibility(View.GONE); } } else {
		 * holder.tvDate.setText(getDateString(sms.getTime()));
		 * holder.tvDate.setVisibility(View.VISIBLE); } final
		 * SpannableStringBuilder sb = new SpannableStringBuilder(
		 * sms.getBodySms()); final ForegroundColorSpan fcs = new
		 * ForegroundColorSpan(Color.rgb(225, 0, 0)); final StyleSpan bss = new
		 * StyleSpan(android.graphics.Typeface.BOLD); String strTextSearch =
		 * myActivity.getTextSearch(); int countChar =
		 * sms.getBodySms().toUpperCase() .indexOf(strTextSearch.toUpperCase());
		 * if (strTextSearch.length() > 0 && countChar >= 0) { sb.setSpan(fcs,
		 * countChar, countChar + strTextSearch.length(),
		 * Spannable.SPAN_INCLUSIVE_INCLUSIVE); sb.setSpan(bss, countChar,
		 * countChar + strTextSearch.length(),
		 * Spannable.SPAN_INCLUSIVE_INCLUSIVE); } holder.tvContent.setText(sb);
		 * holder.tvHours.setText(getHoursString(sms.getTime())); String strFromOrTo =
		 * ""; if (sms.getType() == 1) { strFromOrTo = "From:"; } else if
		 * (sms.getType() == 2) { strFromOrTo = "To:"; } if (sms.getName() ==
		 * null || sms.getName().length() == 0) {
		 * sms.setName(getContactName(myActivity, sms.getAddress()));
		 * holder.tvName.setText(strFromOrTo + sms.getName()); } else {
		 * holder.tvName.setText(strFromOrTo + sms.getName()); }
		 */
		return convertView;
	}

	private static class ViewHolder {
		View llSend;
		View llReceive;
		View llCallLog;
		TextView tvBodyReceive;
		TextView tvBodySend;
		TextView tvDateReceive;
		ImageView imgNewReceive;
		TextView tvDateSend;
		TextView tvTypeSend;
		TextView tvTypeCallLog;
		TextView tvDateCallLog;
		ImageView imgTypeSend;
		ImageView imgTypeCallLog;
	}

	public void setOnLongClickListenerCustom(
			OnLongClickListener onLongClickListener) {
		myOnLongClickListener = onLongClickListener;
	}

	public static interface OnLongClickListener {
		public void onLongClick(int position);
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
			return myContext.getString(R.string.today) + " "
					+ getHours(milliseconds);
		else if (dateLog.compareTo(dateYesterday) == 0)
			return myContext.getString(R.string.yesterday) + " "
					+ getHours(milliseconds);
		Date date = new Date(milliseconds);
		SimpleDateFormat f = new SimpleDateFormat("MM-dd HH:mm");
		return f.format(date);
	}

	public String getHours(long milliseconds) {
		Date date = new Date(milliseconds);
		SimpleDateFormat f = new SimpleDateFormat("HH:mm");
		return f.format(date);
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
			cursor = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
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
