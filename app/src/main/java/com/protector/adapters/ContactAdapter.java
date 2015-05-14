package com.protector.adapters;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.protector.objects.ContactItem;
import com.protector.utils.AndroidVersion;

public class ContactAdapter extends ArrayAdapter<ContactItem> {
	private OnClickListener myOnClickListener;
	private ArrayList<Integer> myArrayChecked;
	private Context myActivity;
	private int myType;
	private PrivateContactTableAdapter myContactTable;
	private AlertDialog.Builder myDialog;
	private Executor taskExecutor;
	private LruCache<String, Bitmap> mMemoryCache;
    public ContactAdapter(Context context, ArrayList<ContactItem> arrayJobs, int type) {
        super(context, R.layout.item_contact, arrayJobs);
        myArrayChecked = new ArrayList<Integer>();
        myActivity = context;
        myType = type;
        myContactTable = PrivateContactTableAdapter.getInstance(myActivity);
//        myDialog = new AlertDialog.Builder(myActivity)
//        .setTitle(myActivity.getResources().getString(R.string.txt_new_private))
//        .setMessage(myActivity.getString(R.string.txt_contact_already))
//        .setNegativeButton(myActivity.getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
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
                    R.layout.item_contact, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView
                    .findViewById(R.id.tv_name);
            holder.tvAddress = (TextView) convertView
                    .findViewById(R.id.tv_address);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb_select);
            holder.imgAvatar = (ImageView) convertView.findViewById(R.id.img_avatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(position >= this.getCount())return null;
        final ContactItem contact = getItem(position);
        if(myArrayChecked.contains(position)){
        	holder.cb.setChecked(true);
            convertView.setSelected(true);
        }else{
        	holder.cb.setChecked(false);
            convertView.setSelected(false);
        }
        holder.tvAddress.setText(contact.getAddress());
        if(contact.getName() != null && contact.getName().length() > 0){
        	holder.tvName.setText(contact.getName());
        }else{
        	holder.tvName.setText(contact.getAddress());
        }
        if (contact.getAvatarByte() != null) {
			final Bitmap bm = getBitmapFromMemCache(contact.getId() + "");
			if (bm == null) {
				taskExecutor.execute(new LoadImageRunable(holder.imgAvatar,
						contact));
			} else {
				if (!bm.isRecycled()) {
					holder.imgAvatar.setImageBitmap(bm);
				}
			}
		} else {
			holder.imgAvatar.setImageResource(R.drawable.icon_avatar);
		}

		View.OnClickListener listener=new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(myArrayChecked.contains(position)){
					myArrayChecked.remove((Object)position);
					holder.cb.setChecked(false);
				}else{
					if(myType== PrivateContactTableAdapter.TYPE_PRIVATE && contact.getAddress() != null && contact.getAddress().length() > 0){
						if(!myContactTable.checkContactByAddress(contact.getAddress(), myType, PasswordTableAdapter.PASSWORD_CURRENT_ID)){
							myArrayChecked.add(position);
							holder.cb.setChecked(true);
						}else{
							myDialog.show();
						}
					}else{
						myArrayChecked.add(position);
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

    public ArrayList<Integer> getArrayChecks(){
    	return myArrayChecked;
    }
    private static class ViewHolder {
        TextView tvName;
        TextView tvAddress;
        CheckBox cb;
        ImageView imgAvatar;
    }
    
    public void setOnClickListener(OnClickListener onClickListener){
    	myOnClickListener = onClickListener;
    }
    
    public static interface OnClickListener{
    	public void onClick(int position);
    }
    
	public int getCountChecked(){
		return myArrayChecked.size();
	}
	public void clearChecked(){
		myArrayChecked.clear();
	}
	private class LoadImageRunable implements Runnable {
		private final WeakReference<ImageView> imageViewReference;
		private ContactItem myObject;

		public LoadImageRunable(ImageView imageView, ContactItem object) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			myObject = object;
		}

		@Override
		public void run() {
			try {
				final Bitmap mLoadedBitmap;
				mLoadedBitmap = getBitmap(myObject.getAvatarByte());
				addBitmapToMemoryCache(myObject.getId() + "", mLoadedBitmap);
				((Activity)myActivity)
						.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (imageViewReference != null
										&& mLoadedBitmap != null) {
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
