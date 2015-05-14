package com.protector.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.SnackBar;
import com.protector.R;
import com.protector.adapters.EncryptMediaAdapter;
import com.protector.asynctasks.HideFile;
import com.protector.asynctasks.RestoreFile;
import com.protector.database.PhotoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.objects.MediaStorageItem;

import java.util.ArrayList;

public class ImageLockFragment extends Fragment implements OnClickListener {
    ListView mImageList;
    EncryptMediaAdapter mAdapter;
    GridView mGridView;
    View mViewBack;
    ButtonFloat mBtnAdd;
    ImageView mRestore;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    ImageFragment mImageFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_lock,
                container, false);
        mGridView = (GridView) rootView.findViewById(R.id.grid_image);
        mViewBack = rootView.findViewById(R.id.view_back);
        mBtnAdd = (ButtonFloat) rootView.findViewById(R.id.btn_add);
        mRestore = (ImageView) rootView.findViewById(R.id.tv_done);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_image_lock, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mGridView.setAdapter(mAdapter);
        mViewBack.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mRestore.setOnClickListener(this);
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
                return getEncryptedImages();
            }
            return null;
        }

        protected void onPostExecute(
                ArrayList<MediaStorageItem> result) {
            try {
                if (isSdCard && result != null) {
                    mGridView.setVisibility(View.VISIBLE);
                    // mTvMessage.setVisibility(View.GONE);
                    mAdapter = new EncryptMediaAdapter(getActivity());
                    mAdapter.addAll(result);
                    mGridView.setAdapter(mAdapter);
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
        }
    }

    private ArrayList<MediaStorageItem> getEncryptedImages() {
        return PhotoTableAdapter.getInstance(getActivity()).getAll();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.imv_add:
            case R.id.btn_add:
                if (mImageFragment == null) {
                    mImageFragment = new ImageFragment();
                    mImageFragment.setOnChooseImageListener(new IChooseImage() {

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onDone(ArrayList<MediaItem> list) {
                            getActivity().onBackPressed();
                            new HideFile(getActivity(), list, MediaItem.Type.IMAGE) {
                                protected void onPostExecute(Void result) {
                                    super.onPostExecute(result);
                                    new AsynReload().execute();
                                    new SnackBar(getActivity(),
                                            "Do you want remove all image?",
                                            "Delete", new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                        }
                                    }).show();

                                }
                            }.execute();
                        }
                    });
                }
                addFragmentStack(mImageFragment);
                break;
            case R.id.tv_done:

                new RestoreFile(getActivity(), PhotoTableAdapter.getInstance(
                        getActivity().getApplicationContext()).getAll(),
                        MediaItem.Type.IMAGE).execute();
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
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
