package com.protector.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.protector.R;

public class MainFragment extends Fragment implements OnClickListener {
    View mViewPhoto, mViewVideo, mViewContact, mViewSMS, mViewApp, mViewBackup;
    IMainFunction mMainListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container,
                false);
        mViewPhoto = rootView.findViewById(R.id.ll_photo);
        mViewVideo = rootView.findViewById(R.id.ll_video);
        mViewContact = rootView.findViewById(R.id.ll_contact);
        mViewSMS = rootView.findViewById(R.id.ll_message);
        mViewApp = rootView.findViewById(R.id.ll_app);
        mViewBackup = rootView.findViewById(R.id.ll_backup);
        mViewPhoto.setOnClickListener(this);
        mViewVideo.setOnClickListener(this);
        mViewContact.setOnClickListener(this);
        mViewSMS.setOnClickListener(this);
        mViewApp.setOnClickListener(this);
        mViewBackup.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_photo:
                if (mMainListener != null)
                    mMainListener.onPhotos();
                break;
            case R.id.ll_video:
                if (mMainListener != null)
                    mMainListener.onVideo();
                break;
            case R.id.ll_message:
                if (mMainListener != null)
                    mMainListener.onSMS();
                break;
            case R.id.ll_app:
                if (mMainListener != null)
                    mMainListener.onApp();
                break;
            case R.id.ll_backup:
                if (mMainListener != null)
                    mMainListener.onBackup();
                break;
            default:
                break;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT)
                    .show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT)
                    .show();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainListener = (IMainFunction) activity;
    }
}
