package com.protector.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.protector.R;
import com.protector.database.PhotoTableAdapter;
import com.protector.database.VideoTableAdapter;
import com.protector.objects.MediaItem;
import com.protector.utils.ImageUtils;


public class CustomDialog extends Dialog {
	private String title, message;
	public OnClickOk OnClickOk;
	public OnClickNo OnClickNo;
	private Button myBtnYes, myBtnNo;
	private TextView myTvTitle, myTvMessage;
	private ImageView myImgPhoto;

	public CustomDialog(Context context, String title, String message) {
		super(context);
		this.title = title;
		this.message = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_dialog_custom);
		Window window = this.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		this.getWindow().setAttributes(wlp);

		init();
	}

	public void init() {
		myBtnYes = (Button) findViewById(R.id.btn_yes);
		myBtnNo = (Button) findViewById(R.id.btn_no);
		myTvTitle = (TextView) findViewById(R.id.tv_title);
		myTvMessage = (TextView) findViewById(R.id.tv_message);
		myImgPhoto = (ImageView) findViewById(R.id.img_dialog);
		if(title != null)
		myTvTitle.setText(title);
		if(message != null)
		myTvMessage.setText(Html.fromHtml(message));
		myBtnYes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (OnClickOk != null) {
					CustomDialog.this.dismiss();
					OnClickOk.Click();
				}
			}
		});
		myBtnNo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (OnClickNo != null) {
					CustomDialog.this.dismiss();
					OnClickNo.Click();
				}
				CustomDialog.this.dismiss();
			}
		});
	}

	public void setTextBtn(String btn1, String btn2) {
		myBtnYes.setText(btn1);
		myBtnNo.setText(btn2);
	}

	public void setTitle(String title) {
		myTvTitle.setText(title);
	}

	public void setMessage(String message) {
		myTvMessage.setText(Html.fromHtml(message));
	}

	public void setTextBtn1(String text) {
		myBtnYes.setText(text);
	}

	public void goneBtn2() {
		myBtnNo.setVisibility(View.GONE);
	}

	public void setOnClickOk(OnClickOk onClickOk) {
		this.OnClickOk = onClickOk;
	}

	public void setOnClickNo(OnClickNo onClickNo) {
		this.OnClickNo = onClickNo;
	}

	public void show() {
		super.show();
	}

	public static interface OnClickOk {
		public void Click();
	}

	public static interface OnClickNo {
		public void Click();
	}

	public void showImage(MediaItem item, int type) {
		PhotoTableAdapter photoTable = PhotoTableAdapter
				.getInstance(getContext());
		VideoTableAdapter videoAdapter = VideoTableAdapter
				.getInstance(getContext());
		if (type == 1) {
			myImgPhoto.setImageBitmap(ImageUtils.bitmapFromByteArray(photoTable
					.getThumbnail(item.getId())));
		} else {
			myImgPhoto
					.setImageBitmap(ImageUtils.bitmapFromByteArray(videoAdapter
							.getThumbnail(item.getId())));
		}
		myImgPhoto.setVisibility(View.VISIBLE);
	}
	
}
