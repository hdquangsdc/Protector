package com.protector.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.protector.R;
import com.protector.fragments.AppLockFragment;
import com.protector.fragments.BackupFragment;
import com.protector.fragments.IMainFunction;
import com.protector.fragments.ImageLockFragment;
import com.protector.fragments.MainFragment;
import com.protector.fragments.SmsCallLogsLockFragment;
import com.protector.fragments.VideoLockFragment;

public class MainActivity extends AppCompatActivity implements IMainFunction {
	final String MAIN_FRAGMENT_TAG = "MainFragment";

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//
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

//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);

	}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
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
	public void onBackPressed() {
		super.onBackPressed();
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		if (fragmentManager.getBackStackEntryCount()==0){
//			setResult(RESULT_OK);
//			finish();
//		}
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
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		BackupFragment fragment=new BackupFragment();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

    }

}
