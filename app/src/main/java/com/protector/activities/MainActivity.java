package com.protector.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.protector.R;
import com.protector.fragments.AppLockFragment;
import com.protector.fragments.IMainFunction;
import com.protector.fragments.ImageLockFragment;
import com.protector.fragments.MainFragment;
import com.protector.fragments.SmsCallLogsLockFragment;
import com.protector.fragments.VideoLockFragment;

public class MainActivity extends FragmentActivity implements IMainFunction {
	final String MAIN_FRAGMENT_TAG = "MainFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			MainFragment fragment = new MainFragment();
			fragmentTransaction.setCustomAnimations(
					android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			fragmentTransaction.replace(R.id.fragment_container, fragment,
					MAIN_FRAGMENT_TAG);
			fragmentTransaction.commit();
		} else {
			MainFragment fragment = (MainFragment) getSupportFragmentManager()
					.findFragmentByTag(MAIN_FRAGMENT_TAG);
		}

	}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
//                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT);
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.view_actiobar_title_black, null);
//        getSupportActionBar().setCustomView(view, layout);
//        getSupportActionBar().setBackgroundDrawable(
//                new ColorDrawable(Color.parseColor("#f25d62")));
//        myTvTitle = ((TextView) view.findViewById(R.id.tv_title));
//        myTvTitle.setText(getString(R.string.txt_photo));
//        ImageView ic = ((ImageView) view.findViewById(R.id.icon));
//        ic.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_nav));
//        ic.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case android.R.id.home:
//                onBackPressed();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    void addFragmentStack(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onVideo() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		VideoLockFragment fragment = new VideoLockFragment();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

	@Override
	public void onPhotos() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		ImageLockFragment fragment = new ImageLockFragment();
		fragmentTransaction.setCustomAnimations(
				android.R.anim.slide_in_left,
				android.R.anim.slide_out_right);
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onContact() {
	}

	@Override
	public void onSMS() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		SmsCallLogsLockFragment fragment = new SmsCallLogsLockFragment();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

    @Override
    public void onApp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        AppLockFragment fragment=new AppLockFragment();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackup() {

    }

}
