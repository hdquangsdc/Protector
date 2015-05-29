package com.protector.fragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.SnackBar;
import com.protector.R;
import com.protector.adapters.VideoAdapter;
import com.protector.asynctasks.DeleteMediaAsyncTask;
import com.protector.asynctasks.HideFile;
import com.protector.asynctasks.RestoreFile;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.objects.MediaStorageItem;

import java.io.File;
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
                            checkButtonRestore();
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
                        public void onDone(final ArrayList<MediaItem> list) {
                            getActivity().onBackPressed();
                            new HideFile(getActivity(), list, MediaItem.Type.VIDEO) {
                                protected void onPostExecute(Void result) {
                                    super.onPostExecute(result);
                                    new AsynReload().execute();
                                    SnackBar snackBar = new SnackBar(getActivity(),
                                            getString(R.string.comfirm_delete_video),
                                            getString(R.string.btn_delete), new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            new AsynDeleteDataImported(list).execute();
                                        }
                                    });
                                    snackBar.setDismissTimer(10000);
                                    snackBar.show();
                                }
                            }.execute();
                        }
                    });
                }
                addFragmentStack(mImageFragment);
                break;
            case R.id.tv_done:
                new RestoreFile(getActivity(), mAdapter.getSelectedItem(),
                        MediaItem.Type.VIDEO) {
                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        new AsynReload().execute();
                    }
                }.execute();
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

    public class AsynDeleteData extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;
        ArrayList<MediaStorageItem> mList;

        AsynDeleteData(ArrayList<MediaStorageItem> list){
            super();
            mList=list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(getActivity());
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.deleting));
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (MediaStorageItem video : mAdapter.getSelectedItem()) {
                VideoTableAdapter.getInstance(getActivity().getApplicationContext()).remove(
                        video.getId());
                try {
                    new File(video.getNewPath()).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(getActivity(),
                    getString(R.string.deleted_success), Toast.LENGTH_SHORT)
                    .show();
            for (MediaStorageItem video : mAdapter.getSelectedItem()) {
                mAdapter.remove(video);
            }
            try {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }

    }

    public class AsynDeleteDataImported extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;
        ArrayList<MediaItem> mList;

        AsynDeleteDataImported(ArrayList<MediaItem> list){
            super();
            mList=list;
        }
        @Override
        protected Void doInBackground(Void... params) {
            if (mList != null) {
                for (MediaItem item : mList) {
                    Uri uri = Uri.withAppendedPath(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            String.valueOf(item.getId()));
                    getActivity().getContentResolver().delete(uri,
                            null, null);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(getActivity());
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.deleting));
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(getActivity(),
                    getString(R.string.deleted_success), Toast.LENGTH_SHORT)
                    .show();

            try {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
            } catch (final Exception e) {
            } finally {
            }
        }
    }

}
