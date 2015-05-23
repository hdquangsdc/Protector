package com.protector.fragments;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.protector.R;
import com.protector.adapters.AppAdapter;
import com.protector.database.AppTableAdapter;
import com.protector.utils.AppUtils;

/**
 * Created by Ho on 4/21/2015.
 */
public class AppPickFragment extends Fragment implements View.OnClickListener {
    AppAdapter mAdapter;
    ListView mListView;
    View mViewBack;
    ImageView mDone;
    IChooseImage mChooseMediaListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_appitem_pick, container,
                false);
        mListView = (ListView) rootView.findViewById(R.id.my_recycler_view);

        mViewBack = (View) rootView.findViewById(R.id.view_back);
        mDone = (ImageView) rootView.findViewById(R.id.tv_done);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new AppAdapter(getActivity(), AppUtils.getApp(getActivity()), true);
        mListView.setAdapter(mAdapter);
        mViewBack.setOnClickListener(this);
        mDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.tv_done:
//                if (mChooseMediaListener != null) {
//                    mChooseMediaListener.onDone(mAdapter.getSelectedItem());
//                }
                new AsynAdd().execute();
                break;
            default:
                break;
        }
    }

    //    public void setOnChooseImageListener(IChooseImage listener) {
//        this.mChooseMediaListener = listener;
//    }
    public class AsynAdd extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (mAdapter.getSelectedItems().size() > 0) {
                for (String app : mAdapter.getSelectedItems()) {
                    try {
                        AppTableAdapter.getInstance(getActivity())
                                .add(getActivity().getPackageManager()
                                        .getApplicationInfo(app, 0)
                                        .loadLabel(getActivity().getPackageManager())
                                        .toString(), app);

                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return "";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            if (result != null) {
//                Toast.makeText(getActivity(),
//                        getString(R.string.txt_no_item_selected),
//                        Toast.LENGTH_SHORT).show();
//            }
            getActivity().onBackPressed();
        }

    }

}
