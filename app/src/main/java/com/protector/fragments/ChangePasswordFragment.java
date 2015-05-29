package com.protector.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.protector.R;
import com.protector.database.PasswordTableAdapter;

public class ChangePasswordFragment extends Fragment implements OnClickListener {
    View mViewBack;
    EditText edtPassword,edtNewPassword,edtConfirmNewPassword;
    Button btnOK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_password, container,
                false);
        mViewBack = rootView.findViewById(R.id.view_back);
        edtPassword = (EditText) rootView.findViewById(R.id.edt_password);
        edtNewPassword = (EditText) rootView.findViewById(R.id.edt_new_password);
        edtConfirmNewPassword = (EditText) rootView.findViewById(R.id.edt_confirm_new_password);
        btnOK = (Button) rootView.findViewById(R.id.btn_ok);
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

        mViewBack.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.view_change_passcode:

                break;
            case R.id.btn_ok:
                String password=edtPassword.getText().toString();
                String newpassword=edtNewPassword.getText().toString();
                String confirmpassword=edtConfirmNewPassword.getText().toString();
                PasswordTableAdapter table = PasswordTableAdapter
                        .getInstance(getActivity());
                int passID = table.checkPassword(password);
                if (password.length()!=4){
                    toast(R.string.invalid_password_length);
                    break;
                }

                if (newpassword.length()!=4){
                    toast(R.string.invalid_new_password_length);
                    break;
                }

                if (confirmpassword.length()!=4){
                    toast(R.string.invalid_confirm_password_length);
                    break;
                }

                if (!confirmpassword.equals(newpassword)){
                    toast(R.string.confirm_password_must_be_same);
                    break;
                }

                if (passID == -1) {
                    toast(R.string.incorrect_password);
                    break;
                }
                long id = table.updatePassword(1, newpassword);
                if (id >= 0) {
                    toast(R.string.change_password_successfully);
                    getActivity().onBackPressed();
                }
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

    Toast mToast;
    void toast(int resStringId) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast
                .makeText(getActivity(), getString(resStringId), Toast.LENGTH_LONG);
        mToast.show();
    }

}
