package com.protector.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.PrivateContactTableAdapter;
import com.protector.database.SmsCallLogTableAdapter;
import com.protector.objects.ContactItem;
import com.protector.objects.SmsCallLogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



public class ContactLockedAdapter extends ArrayAdapter<Pair<ContactItem, SmsCallLogItem>> {
	private OnLongClickListener myOnLongClickListener;
	private OnClickItemListener myOnClickListener;
	private OnClickAvatarListener myOnClickAvatarListener;
	private ArrayList<Integer> myArrayChecked;
	private Context myContext;
    public ContactLockedAdapter(Context context, ArrayList<Pair<ContactItem, SmsCallLogItem>> arrayJobs) {
        super(context, R.layout.item_sms_locked, arrayJobs);
        myArrayChecked = new ArrayList<Integer>();
        myContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_sms_locked, null);
            holder = new ViewHolder();
            holder.tvDate = (TextView) convertView
                    .findViewById(R.id.tv_date);
            holder.tvName = (TextView) convertView
                    .findViewById(R.id.tv_name);
            holder.tvContent = (TextView) convertView
                    .findViewById(R.id.tv_content);
            holder.tvNewSms = (TextView) convertView.findViewById(R.id.tv_num);
            holder.imgType = (ImageView) convertView.findViewById(R.id.img_type);
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.img_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Pair<ContactItem, SmsCallLogItem> object = getItem(position);
        ContactItem contact = object.first;
        SmsCallLogItem smsOrCall = object.second;
        if(contact.getName() != null && contact.getName().length() > 0){
        	holder.tvName.setText(contact.getName());
        }else{
        	holder.tvName.setText(contact.getAddress());
        }
        if(contact.getType() == PrivateContactTableAdapter.TYPE_PRIVATE){
        	holder.imgIcon.setImageDrawable(myContext.getResources().getDrawable(R.drawable.icon_private));
        }else{
        	holder.imgIcon.setImageDrawable(myContext.getResources().getDrawable(R.drawable.icon_avatar));
        }
        holder.imgIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(myOnClickAvatarListener != null){
					myOnClickAvatarListener.onClick(position);
				}
			}
		});
        if(contact.getNewSMS() > 0){
        	holder.tvNewSms.setText(contact.getNewSMS() + "");
        	holder.tvNewSms.setVisibility(View.VISIBLE);
        }else{
        	holder.tvNewSms.setVisibility(View.GONE);
        }
        if(smsOrCall != null){
	        holder.tvDate.setText(getDate(smsOrCall.getTime()));
	        
	        if(smsOrCall.getTypeCompare() == SmsCallLogTableAdapter.TYPE_SMS){
	        	holder.tvContent.setText(smsOrCall.getBodySms());
	        	holder.imgType.setBackgroundResource(R.drawable.ic_conversation);
	        }else{
	        	switch (smsOrCall.getType()) {
				case SmsCallLogTableAdapter.TYPE_CALL_INCOMING:
					holder.tvContent.setText("Incoming call");
		        	holder.imgType.setBackgroundResource(R.drawable.ic_incoming_item);
					break;
				case SmsCallLogTableAdapter.TYPE_CALL_MISSED:
					holder.tvContent.setText("Missed");
		        	holder.imgType.setBackgroundResource(R.drawable.ic_call_miss_item);
					break;
				case SmsCallLogTableAdapter.TYPE_CALL_OUTGOING:
					holder.tvContent.setText("Outgoing call");
		        	holder.imgType.setBackgroundResource(R.drawable.ic_outgoing_item);
					break;
	
				default:
					holder.imgType.setBackgroundResource(R.drawable.ic_outgoing_item);
					break;
				}
	        	
	        }
        }else{
        	holder.tvDate.setText("");
        	holder.tvContent.setText(myContext.getResources().getString(R.string.no_records));
        	holder.imgType.setBackgroundDrawable(null);        	
        }
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
                if(myOnLongClickListener !=null) {
                    myOnLongClickListener.onLongClick(position);
                }
				return false;
			}
		});
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(myOnClickListener !=null){
					myOnClickListener.onClick(position);
				}
			}
		});
        return convertView;
    }

    public ArrayList<Integer> getArrayChecks(){
    	return myArrayChecked;
    }
    private static class ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvContent;
        ImageView imgIcon;
        ImageView imgType;
        TextView tvNewSms;
    }
    
    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
    	myOnLongClickListener = onLongClickListener;
    }
    public void setOnItemClickListener(OnClickItemListener onClickListener){
    	myOnClickListener = onClickListener;
    }
    
    public void setOnClickAvatarListener(OnClickAvatarListener onClickListener){
    	myOnClickAvatarListener = onClickListener;
    }
    
    public static interface OnLongClickListener{
    	public void onLongClick(int position);
    }
    public static interface OnClickItemListener{
    	public void onClick(int position);
    }
    public static interface OnClickAvatarListener{
    	public void onClick(int position);
    }
        
    public String getDate_1(long milliseconds){
		Date date = new Date(milliseconds);
		SimpleDateFormat f = new SimpleDateFormat("dd-MM");
		return f.format(date);
	}
	
    public String getDate(long milliseconds){
    	Date d = new Date(milliseconds);
    	Calendar calNow = Calendar.getInstance();
    	Calendar calYesterday = Calendar.getInstance();
    	calYesterday.add(Calendar.DATE, -1);
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    	
    	String dateNow = dateFormat.format(calNow.getTime());
    	String dateYesterday = dateFormat.format(calYesterday.getTime());
    	String dateLog = dateFormat.format(d.getTime());
    	
    	if(dateLog.equals(dateNow))return getHours(milliseconds);
    	else if(dateLog.compareTo(dateYesterday) == 0) return "Yesterday";
		Date date = new Date(milliseconds);
		SimpleDateFormat f = new SimpleDateFormat("MM-dd");
		return f.format(date);
	}
    
	public String getHours(long milliseconds){
		Date date = new Date(milliseconds);
		SimpleDateFormat f = new SimpleDateFormat("HH:mm");
		return f.format(date);
	}
}

