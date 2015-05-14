package com.protector.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.VideoPickerAdapter;
import com.protector.objects.MediaItem;

import java.util.ArrayList;
import java.util.Collections;

public class VideoFragment extends Fragment implements OnClickListener {
	ListView mImageList;
	VideoPickerAdapter mAdapter;
	ListView mListView;
	View mViewBack;
    ImageView mDone;
	IChooseImage mChooseMediaListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_video, container,
				false);
		mListView = (ListView) rootView.findViewById(R.id.list_video);
		mViewBack = rootView.findViewById(R.id.view_back);
		mDone = (ImageView) rootView.findViewById(R.id.tv_done);
        mDone.setVisibility(View.GONE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAdapter = new VideoPickerAdapter(getActivity(), getGalleryVideos(), new VideoPickerAdapter.OnTouchItemListerner() {
            @Override
            public void onTouch() {
                if (mAdapter.getSelectedItem().size()>0){
                    mDone.setVisibility(View.VISIBLE);
                } else{
                    mDone.setVisibility(View.GONE);
                }
            }
        });
		mListView.setAdapter(mAdapter);
		mViewBack.setOnClickListener(this);
		mDone.setOnClickListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	private ArrayList<MediaItem> getGalleryVideos() {
		ArrayList<MediaItem> galleryList = new ArrayList<MediaItem>();
		Cursor imagecursor = null;
		try {
			final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
			final String orderBy = BaseColumns._ID;
			imagecursor = getActivity().getContentResolver().query(
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null,
					null, orderBy);
			if (imagecursor != null && imagecursor.getCount() > 0) {

				while (imagecursor.moveToNext()) {
					MediaItem item = new MediaItem();

					int dataColumnIndex = imagecursor
							.getColumnIndex(MediaColumns.DATA);
					item.setId(imagecursor.getInt(imagecursor
							.getColumnIndex(BaseColumns._ID)));
					// item. = Type.VIDEO;
					item.setPath(imagecursor.getString(dataColumnIndex));
					galleryList.add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (imagecursor != null && !imagecursor.isClosed())
			imagecursor.close();

		// show newest photo at beginning of the list
		Collections.reverse(galleryList);
		return galleryList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_back:
			getActivity().onBackPressed();
			break;
		case R.id.tv_done:
			if (mChooseMediaListener != null) {
				mChooseMediaListener.onDone(mAdapter.getSelectedItem());
			}
			break;
		default:
			break;
		}
	}

	public void setOnChooseImageListener(IChooseImage listener) {
		this.mChooseMediaListener = listener;
	}
}
