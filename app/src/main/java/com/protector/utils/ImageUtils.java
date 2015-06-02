package com.protector.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Ho Duy Quang
 */

public class ImageUtils {
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		/**
		 * Raw height and width of image
		 */
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			/**
			 * Calculate the largest inSampleSize value that is a power of 2 and
			 * keeps both height and width larger than the requested height and
			 * width.
			 */
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static byte[] bitmapToByteArray(Bitmap bm) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// Log.d("ENCRYPT", String.valueOf(bm.getWidth()));
		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		try {
			stream.flush();
			stream.close();
			stream = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	public static Bitmap bitmapFromByteArray(byte[] data) {
		if (data != null) {
			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else
			return null;
	}
}
