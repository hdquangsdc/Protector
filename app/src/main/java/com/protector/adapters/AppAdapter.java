package com.protector.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ho on 4/20/2015.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private boolean isSelectable;
    private Set<String> mSelectedItems;
    List<String> data;
    Context mContext;

    public AppAdapter(Context context, List<String> objects) {
        this(context, objects, false);
    }

    public AppAdapter(Context context, List<String> objects,
                      boolean isSelectable) {
        this.isSelectable = isSelectable;
        mSelectedItems = new LinkedHashSet<>();
        mContext=context;
        data=objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_app, viewGroup, false);
        return new ViewHolder(v);
    }

    public Set<String> getSelectedItems() {
        return mSelectedItems;
    }

    public void onRemoveSelect(String app) {
        data.removeAll(mSelectedItems);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        View parent;
        ImageView mIcon;
        View mLock;
        TextView mAppTitle;
        View mCheck;

        public ViewHolder(View rootView){
            super(rootView);
            parent=rootView;
            mIcon = (ImageView) rootView
                    .findViewById(R.id.app_icon);
            mAppTitle = (TextView) rootView
                    .findViewById(R.id.app_title);
            mCheck = rootView.findViewById(R.id.checkbox);
            mLock = rootView.findViewById(R.id.img_lock);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        try {
            String app = data.get(position);
            ApplicationInfo info = mContext.getPackageManager()
                    .getApplicationInfo(app, 0);
            holder.mIcon.setImageDrawable(info.loadIcon(mContext
                    .getPackageManager()));
            String title = info.loadLabel(mContext.getPackageManager())
                    .toString();
            holder.mAppTitle.setText(title);
            if (isSelectable) {
                holder.mCheck.setVisibility(View.VISIBLE);
            } else {
                holder.mCheck.setVisibility(View.GONE);
            }
            if (isSelectable) {
                holder.mLock.setVisibility(View.GONE);
                holder.mCheck.setVisibility(View.VISIBLE);
                if (mSelectedItems.contains(app)) {
                    holder.mCheck.setSelected(true);
                } else {
                    holder.mCheck.setSelected(false);
                }
            } else {
                holder.mLock.setVisibility(View.VISIBLE);
                holder.mCheck.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}

