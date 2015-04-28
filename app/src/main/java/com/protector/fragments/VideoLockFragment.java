package com.protector.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.VideoAdapter;
import com.protector.asynctasks.HideFile;
import com.protector.asynctasks.RestoreFile;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.objects.MediaStorageItem;

import java.util.ArrayList;

public class VideoLockFragment extends Fragment implements OnClickListener {
	VideoAdapter mAdapter;
	ListView mListView;
	View mViewBack;
	ImageView mViewAdd;
	VideoFragment mImageFragment;
	TextView tvRestore;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_video_lock,
				container, false);
		mListView = (ListView) rootView.findViewById(R.id.list_video);
		mViewBack = (View) rootView.findViewById(R.id.view_back);
		mViewAdd = (ImageView) rootView.findViewById(R.id.imv_add);
		tvRestore = (TextView) rootView.findViewById(R.id.tv_restore_all);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mListView.setAdapter(mAdapter);
		mViewBack.setOnClickListener(this);
		mViewAdd.setOnClickListener(this);
		tvRestore.setOnClickListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		new AsynReload().execute();
		super.onResume();
	}

	public class AsynReload extends
			AsyncTask<Void, Void, ArrayList<MediaStorageItem>> {
		private boolean isSdCard;

		@Override
		protected ArrayList<MediaStorageItem> doInBackground(Void... params) {
			isSdCard = android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
			if (isSdCard) {
				return getEncryptedVideos();
			}
			return null;
		}

		protected void onPostExecute(
				ArrayList<MediaStorageItem> result) {
			try {
				if (isSdCard && result != null) {
					mListView.setVisibility(View.VISIBLE);
					// mTvMessage.setVisibility(View.GONE);
					mAdapter = new VideoAdapter(getActivity(),
							result);
					mListView.setAdapter(mAdapter);
					// if (mAdapter.getCount() > 0) {
					// mImagesGrid.setVisibility(View.VISIBLE);
					// mTvMessage.setVisibility(View.GONE);
					// } else {
					// mImagesGrid.setVisibility(View.GONE);
					// mTvMessage.setVisibility(View.VISIBLE);
					// }
					// if (mIsShowDeleteImported) {
					// mIsShowDeleteImported = false;
					// showDiaLogDeleteItemsImported();
					// }
				} else {
					// mTvMessage.setVisibility(View.VISIBLE);
					// mImagesGrid.setVisibility(View.GONE);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		};

	}

	private ArrayList<MediaStorageItem> getEncryptedVideos() {
		return VideoTableAdapter.getInstance(getActivity()).getAll();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_back:
			getActivity().onBackPressed();
			break;
		case R.id.imv_add:
			if (mImageFragment == null) {
				mImageFragment = new VideoFragment();
				mImageFragment.setOnChooseImageListener(new IChooseImage() {

					@Override
					public void onCancel() {

					}

					@Override
					public void onDone(ArrayList<MediaItem> list) {
						getActivity().onBackPressed();
						new HideFile(getActivity(), list, MediaItem.Type.VIDEO) {
							protected void onPostExecute(Void result) {
								super.onPostExecute(result);
								new AsynReload().execute();
							};
						}.execute();
					}
				});
			}
			addFragmentStack(mImageFragment);
			break;
		case R.id.tv_restore_all:
			new RestoreFile(getActivity(), VideoTableAdapter.getInstance(
					getActivity().getApplicationContext()).getAll(),
					MediaItem.Type.VIDEO).execute();
			break;
		default:
			break;
		}
	}

	public void addFragmentStack(Fragment fragment) {
		FragmentManager fragmentManager = getActivity()
				.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

}
