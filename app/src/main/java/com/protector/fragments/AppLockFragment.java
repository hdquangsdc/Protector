package com.protector.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.gc.materialdesign.views.Button;
import com.protector.R;
import com.protector.adapters.AppAdapter;
import com.protector.database.AppTableAdapter;
import com.protector.database.PasswordTableAdapter;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class AppLockFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;
    private ImageView mImvAdd;
    private View mViewBack;
    private Button mBtnAdd;
    private View mViewAdd;


    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private AppAdapter mListApp;

    // TODO: Rename and change types of parameters
    public static AppLockFragment newInstance(String param1, String param2) {
        AppLockFragment fragment = new AppLockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppLockFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mListApp = new AppAdapter(getActivity(), new ArrayList<String>(),false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appitem, container, false);
        mImvAdd=(ImageView)view.findViewById(R.id.first_add);
        mBtnAdd=(Button) view.findViewById(R.id.btn_add);

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.my_recycler_view);


        mListView.setAdapter(mListApp);
        mViewBack = view.findViewById(R.id.view_back);
        mViewAdd=view.findViewById(R.id.view_add);

        // Set OnItemClickListener so we can be notified on item clicks
        mImvAdd.setOnClickListener(this);
        mViewBack.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);

        view.findViewById(R.id.tv_restore_all).setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        reload();
    }

    private void reload() {
        new ReloadAsynTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = mListView.getEmptyView();
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.btn_add:
            case R.id.first_add:
                addFragmentStack(new AppPickFragment());
                break;
            case R.id.tv_restore_all:
                restore();
                break;
            default:
                break;
        }
    }

    private void restore() {
        new AsynRemove().execute();
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    public class ReloadAsynTask extends
            AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            return AppTableAdapter.getInstance(getActivity())
                    .getAll();
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            mListApp = new AppAdapter(getActivity(), result,false);
            mListView.setAdapter(mListApp);
            refreshView();
        }
    }

    public class AsynRemove extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Object[] str = mListApp.getSelectedItems().toArray();
            for (int i = str.length - 1; i >= 0; i--) {
                String app = str[i].toString();
                AppTableAdapter.getInstance(getActivity()).remove(
                        app, PasswordTableAdapter.PASSWORD_CURRENT_ID);
                // /mAdapter.onRemoveSelect(app);
                // mAdapter.remove(app);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mListApp.onRemoveSelect();
            mListApp.notifyDataSetChanged();
            refreshView();
        }
    }

    public void refreshView(){
        if (mListApp.getCount() == 0) {
            mListView.setVisibility(View.GONE);
            mViewAdd.setVisibility(View.VISIBLE);
            mBtnAdd.setVisibility(View.GONE);
        } else{
            mListView.setVisibility(View.VISIBLE);
            mViewAdd.setVisibility(View.GONE);
            mBtnAdd.setVisibility(View.VISIBLE);
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
