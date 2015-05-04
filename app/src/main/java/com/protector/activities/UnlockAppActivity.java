package com.protector.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.protector.AppPreference;
import com.protector.R;
import com.protector.database.AppTableAdapter;
import com.protector.database.PasswordTableAdapter;

import java.util.ArrayList;

public class UnlockAppActivity extends FragmentActivity {
	public static final String BLOCKED_PACKAGE = "locked package name";
	public static final String BLOCKED_ACTIVITY = "locked activity name";
	public static final String ACTION_APPLICATION_PASSED = "com.protector.applocker.applicationpassedtest";
	public static final String EXTRA_PACKAGE_NAME = "com.protector.applocker.extra.package.name";

	private String mPassword = "";
	private String mSetPassword = "";
	private String mConfirmPassword = "";
	private Mode mode;

	private ImageView dot1, dot2, dot3, dot4;

	public enum Mode {
		SET, CONFIRM, PASS
	}

	View.OnClickListener mKeyPadListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.imv_number_0:
					mPassword += "0";
					break;
				case R.id.imv_number_1:
					mPassword += "1";
					break;
				case R.id.imv_number_2:
					mPassword += "2";
					break;
				case R.id.imv_number_3:
					mPassword += "3";
					break;
				case R.id.imv_number_4:
					mPassword += "4";
					break;
				case R.id.imv_number_5:
					mPassword += "5";
					break;
				case R.id.imv_number_6:
					mPassword += "6";
					break;
				case R.id.imv_number_7:
					mPassword += "7";
					break;
				case R.id.imv_number_8:
					mPassword += "8";
					break;
				case R.id.imv_number_9:
					mPassword += "9";
					break;
				case R.id.imv_backspace:
					if (mPassword.length() > 0)
						mPassword = mPassword.substring(0, mPassword.length() - 1);
					break;
				default:
					break;
			}
			// if (mPassword.equals("1111"))
			// toast(R.string.password_correct);


			PasswordTableAdapter table = PasswordTableAdapter
					.getInstance(UnlockAppActivity.this);
			showDot(mPassword);
			if (mPassword.length() == 4) {
				if (mode == Mode.PASS) {
					toast(mPassword);
					int passID = table.checkPassword(mPassword.toString());
					if (passID != -1) {
						PasswordTableAdapter.PASSWORD_CURRENT_ID = passID;
						Intent i = new Intent(UnlockAppActivity.this,
								MainActivity.class);
						startActivity(i);
						finish();
					}
				} else if (mode == Mode.SET) {
					toast(mPassword);
					mSetPassword = mPassword;
					mode = Mode.CONFIRM;
				} else if (mode == Mode.CONFIRM) {
					mConfirmPassword = mPassword;
					if (mSetPassword.equals(mConfirmPassword)) {
						long id = table.addPassword(1, mSetPassword);
						if (id >= 0) {
							PasswordTableAdapter.PASSWORD_CURRENT_ID = 1;
							PasswordTableAdapter.PASSWORD_CURRENT_TEXT = mSetPassword;
							finish();
						}

					} else {
						toast(R.string.confirm_password_incorrect);
					}
				}
				mPassword = "";
			}
		}
	};

	private void showDot(String password) {
		if (password.length() >= 1) {
			dot1.setImageResource(R.drawable.ic_passcode_dot_checked);
		} else {
			dot1.setImageResource(R.drawable.ic_passcode_dot);
		}
		if (password.length() >= 2) {
			dot2.setImageResource(R.drawable.ic_passcode_dot_checked);
		} else {
			dot2.setImageResource(R.drawable.ic_passcode_dot);
		}
		if (password.length() >= 3) {
			dot3.setImageResource(R.drawable.ic_passcode_dot_checked);
		} else {
			dot3.setImageResource(R.drawable.ic_passcode_dot);
		}
		if (password.length() >= 4) {
			dot4.setImageResource(R.drawable.ic_passcode_dot_checked);
		} else {
			dot4.setImageResource(R.drawable.ic_passcode_dot);
		}

	}

	Toast mToast;

	void toast(int resStringId) {
		if (mToast != null)
			mToast.cancel();
		mToast = Toast
				.makeText(this, getString(resStringId), Toast.LENGTH_LONG);
		mToast.show();
	}

	void toast(String msg) {
		if (mToast != null)
			mToast.cancel();
		mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		mToast.show();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.keypad);

		findViewById(R.id.imv_number_1).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_2).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_3).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_4).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_5).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_6).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_7).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_8).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_9).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_number_0).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_camera).setOnClickListener(mKeyPadListener);
		findViewById(R.id.imv_backspace).setOnClickListener(mKeyPadListener);

		dot1 = (ImageView) findViewById(R.id.dot_1);
		dot2 = (ImageView) findViewById(R.id.dot_2);
		dot3 = (ImageView) findViewById(R.id.dot_3);
		dot4 = (ImageView) findViewById(R.id.dot_4);

		PasswordTableAdapter pwTable = PasswordTableAdapter.getInstance(this);
		if (pwTable.checkPassword(1) == null) {
			// if (!getIntent().getBooleanExtra("MUTIL_LANGUAGE", false)) {
			// startActivityForResult(new Intent(this,
			// SetPasswordActivity.class), 1111);
			// } else {
			// Intent i = new Intent(this, SetPasswordActivity.class);
			// i.putExtra("MUTIL_LANGUAGE", true);
			// startActivityForResult(i, 1111);
			// }
			mode = Mode.SET;
		} else {
			mode = Mode.PASS;
		}

		super.onCreate(arg0);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN)
				.addCategory(Intent.CATEGORY_HOME)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

//	private void verify() {
//		AppTableAdapter appTable = AppTableAdapter.getInstance(this);
//		String pakageName = AppPreference.getInstance(this).getPackageName();// getIntent().getStringExtra(BLOCKED_PACKAGE);
//		ArrayList<Integer> passIdApps = appTable.getPasswordApp(pakageName);
//		if (passIdApps.size() > 0) {
//			PasswordTableAdapter passTable = PasswordTableAdapter
//					.getInstance(this);
//			int passID = passTable.checkPassword(mEdtCode.getText().toString());
//			if (passIdApps.contains((int) passID)) {
//				/*
//				 * this.sendBroadcast(new Intent().setAction(
//				 * ACTION_APPLICATION_PASSED).putExtra(EXTRA_PACKAGE_NAME,
//				 * getIntent().getStringExtra(BLOCKED_PACKAGE)));
//				 */
//				finish();
//			} else {
//				myPassIncorrect = mEdtCode.getText().toString();
//				if (mCamera != null) {
//					new AsynTakePhoto().execute();
//				}
//				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//				v.vibrate(500);
//				mEdtCode.setText("");
//				Toast.makeText(getApplicationContext(),
//						getString(R.string.txt_wrong_password),
//						Toast.LENGTH_LONG).show();
//			}
//		} else {
//
//			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//			v.vibrate(500);
//			mEdtCode.setText("");
//			Toast.makeText(getApplicationContext(),
//					getString(R.string.txt_wrong_password), Toast.LENGTH_LONG)
//					.show();
//		}
//	}
}
