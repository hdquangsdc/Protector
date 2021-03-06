package com.protector.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ho Duy Quang
 */
public class AppAdapter extends ArrayAdapter<String> {
    private boolean isSelectable;
    private ArrayList<String> mSelectedItems;
    List<String> data;
    private LayoutInflater mInflater;
    Context mContext;

    public AppAdapter(Context context, List<String> objects,
                      boolean isSelectable) {
        super(context,R.layout.item_app,objects);
        this.isSelectable = isSelectable;
        mSelectedItems = new ArrayList<>();
        mContext = context;
        data = objects;
        this.mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_app, parent,
                    false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        String app = data.get(position);
        try {
            ApplicationInfo info = mContext.getPackageManager()
                    .getApplicationInfo(app, 0);
            if (info!=null) {
                holder.mIcon.setImageDrawable(info.loadIcon(mContext
                        .getPackageManager()));
                String title = info.loadLabel(mContext.getPackageManager())
                        .toString();
                holder.mAppTitle.setText(title);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (isSelectable) {
            holder.mLock.setVisibility(View.GONE);
            if (mSelectedItems.contains(app)) {

            } else {

            }
        } else {

            if (mSelectedItems.contains(app)) {
                holder.mLock.setVisibility(View.VISIBLE);
                holder.parent.setBackgroundColor(Color.parseColor("#eeeeee"));
            } else {
                holder.mLock.setVisibility(View.INVISIBLE);
                holder.parent.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }


        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = data.get(position);
                if (mSelectedItems.contains(item)) {
                    mSelectedItems.remove(item);
                } else {
                    mSelectedItems.add(item);
                }

                if (mClickListener != null) {
                    mClickListener.onClick(getItem(position));
                }

                notifyDataSetChanged();
            }
        });


        return convertView;
    }

    public ArrayList<String> getSelectedItems() {
        return mSelectedItems;
    }

    public void onRemoveSelect() {
        data.removeAll(mSelectedItems);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        ImageView mIcon;
        ImageView mLock;
        TextView mAppTitle;


        public ViewHolder(View rootView) {
            super(rootView);
            parent = rootView;
            mIcon = (ImageView) rootView
                    .findViewById(R.id.app_icon);
            mAppTitle = (TextView) rootView
                    .findViewById(R.id.app_title);

            mLock = (ImageView) rootView.findViewById(R.id.img_lock);
        }
    }

    OnClickItemListener mClickListener;

    public void setOnItemClickListener(OnClickItemListener listener) {
        this.mClickListener = listener;
    }

    public interface OnClickItemListener {
        void onClick(String item);
    }


}

