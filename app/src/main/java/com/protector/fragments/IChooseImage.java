package com.protector.fragments;

import com.protector.objects.MediaItem;

import java.util.ArrayList;

public interface IChooseImage {
	public void onDone(ArrayList<MediaItem> list);

	public void onCancel();
}
