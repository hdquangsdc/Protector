package com.protector.fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.MediaAdapter;
import com.protector.objects.MediaItem;
import com.protector.objects.PhotoItem;

import java.util.ArrayList;
import java.util.Collections;

public class ImageFragment extends Fragment implements OnClickListener {
	ListView mImageList;
	MediaAdapter mAdapter;
	GridView mGridView;
	View mViewBack;
	ImageView mDone;
	IChooseImage mChooseImageListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image, container,
				false);
		mGridView = (GridView) rootView.findViewById(R.id.grid_image);
		mViewBack = rootView.findViewById(R.id.view_back);
		mDone = (ImageView) rootView.findViewById(R.id.tv_done);
        mDone.setVisibility(View.GONE);
		return rootView;
	}

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        menu.clear();
//        inflater.inflate(R.menu.menu_image, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//
//    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAdapter = new MediaAdapter(getActivity(), getGalleryPhotos(), new MediaAdapter.OnTouchItemListerner() {
            @Override
            public void onTouch() {
                if (mAdapter.getSelectedItem().size()>0){
                    mDone.setVisibility(View.VISIBLE);
                } else{
                    mDone.setVisibility(View.GONE);
                }
            }
        });
		mGridView.setAdapter(mAdapter);
		mViewBack.setOnClickListener(this);
		mDone.setOnClickListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	private ArrayList<MediaItem> getGalleryPhotos() {
		ArrayList<MediaItem> list = new ArrayList<MediaItem>();
		final String[] columns = { MediaColumns.DATA, BaseColumns._ID,
				MediaColumns.DATE_MODIFIED };
		final String orderBy = BaseColumns._ID;

		ContentResolver cr = getActivity().getContentResolver();
		Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				columns, null, null, orderBy);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				PhotoItem item = new PhotoItem();
				int dataColumnIndex = cursor.getColumnIndex(MediaColumns.DATA);
				item.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
				item.setDateModified(cursor.getLong(cursor
						.getColumnIndex(MediaColumns.DATE_MODIFIED)));
				item.setPath(cursor.getString(dataColumnIndex));
				/*
				 * item.solution = "1200 x 1200"; if (selectedFiles != null) {
				 * for (int i = 0; i < selectedFiles.size(); i++) { if
				 * (item.sdcardPath.equalsIgnoreCase(selectedFiles
				 * .get(i).sdcardPath)) { item.isSeleted = true; } } }
				 */
				list.add(item);
			}
		}

		// show newest photo at beginning of the list
		Collections.reverse(list);
		return list;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_back:
			getActivity().onBackPressed();
			break;
		case R.id.tv_done:
			if (mChooseImageListener != null) {
				mChooseImageListener.onDone(mAdapter.getSelectedItem());
			}
			break;
		default:
			break;
		}
	}

	public void setOnChooseImageListener(IChooseImage listener) {
		this.mChooseImageListener = listener;
	}
}
