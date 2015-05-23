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
import com.protector.asynctasks.DeleteMediaAsyncTask;
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
                return getEncryptedImages();
            }
            return null;
        }

        protected void onPostExecute(
                ArrayList<MediaStorageItem> result) {
            try {
                if (isSdCard && result != null) {
                    mGridView.setVisibility(View.VISIBLE);
                    mAdapter = new EncryptMediaAdapter(getActivity());
                    mAdapter.addAll(result);
                    mAdapter.setOnTouchListener(new EncryptMediaAdapter.OnTouchListener() {
                        @Override
                        public void onTouch() {
                           checkButtonRestore();
                        }
                    });
                    mGridView.setAdapter(mAdapter);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void checkButtonRestore(){
        if (mAdapter!=null&&mAdapter.getmSelectedList().size()>0){
            mRestore.setVisibility(View.VISIBLE);
        }else {
            mRestore.setVisibility(View.GONE);
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
            case R.id.btn_add:
                if (mImageFragment == null) {
                    mImageFragment = new ImageFragment();
                    mImageFragment.setOnChooseImageListener(new IChooseImage() {

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onDone(final ArrayList<MediaItem> list) {
                            getActivity().onBackPressed();
                            new HideFile(getActivity(), list, MediaItem.Type.IMAGE) {
                                protected void onPostExecute(Void result) {
                                    super.onPostExecute(result);
                                    new AsynReload().execute();
                                    SnackBar snackBar = new SnackBar(getActivity(),
                                            getString(R.string.comfirm_delete_photo),
                                            getString(R.string.btn_delete), new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            new DeleteMediaAsyncTask(list, getActivity()).execute();
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
                new RestoreFile(getActivity(), mAdapter.getmSelectedList(),
                        MediaItem.Type.IMAGE) {
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
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

//    public class AsynDeleteData extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog mDialog;
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            for (MediaStorageItem image : mAdapter.getmSelectedList()) {
//                PhotoTableAdapter.getInstance(getActivity()).remove(
//                        image.getId());
//                try {
//                    new File(image.getNewPath()).delete();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//            mAdapter.removeSelectedItem();
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mDialog = new ProgressDialog(getActivity());
//            mDialog.setCancelable(false);
//            mDialog.setMessage(getActivity()
//                    .getString(R.string.deleting));
//            mDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            Toast.makeText(getActivity(),
//                    getString(R.string.deleted_success), Toast.LENGTH_SHORT)
//                    .show();
//            try {
//                if ((this.mDialog != null) && this.mDialog.isShowing()) {
//                    this.mDialog.dismiss();
//                }
//            } catch (final IllegalArgumentException e) {
//            } catch (final Exception e) {
//            } finally {
//            }
//        }
//
//    }


}
