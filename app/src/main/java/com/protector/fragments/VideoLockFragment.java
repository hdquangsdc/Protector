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

import com.gc.materialdesign.views.ButtonFloat;
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
    ButtonFloat mViewAdd;
    VideoFragment mImageFragment;
    ImageView mRestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_lock,
                container, false);
        mListView = (ListView) rootView.findViewById(R.id.list_video);
        mViewBack = rootView.findViewById(R.id.view_back);
        mViewAdd = (ButtonFloat) rootView.findViewById(R.id.btn_add);
        mRestore = (ImageView) rootView.findViewById(R.id.tv_done);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mListView.setAdapter(mAdapter);
        mViewBack.setOnClickListener(this);
        mViewAdd.setOnClickListener(this);
        mRestore.setOnClickListener(this);
        checkButtonRestore();
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
                    mAdapter = new VideoAdapter(getActivity(),
                            result);
                    mAdapter.setOnTouchListener(new VideoAdapter.OnTouchListener() {
                        @Override
                        public void onTouch() {

                        }
                    });
                    mListView.setAdapter(mAdapter);
                    checkButtonRestore();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        ;

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
            case R.id.btn_add:
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
                                }

                                ;
                            }.execute();
                        }
                    });
                }
                addFragmentStack(mImageFragment);
                break;
            case R.id.tv_done:
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

    public void checkButtonRestore() {
        if (mAdapter != null && mAdapter.getSelectedItem().size() > 0) {
            mRestore.setVisibility(View.VISIBLE);
        } else {
            mRestore.setVisibility(View.GONE);
        }
    }

}
