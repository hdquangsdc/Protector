package com.protector.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.protector.R;
import com.protector.objects.MediaItem;

import java.util.ArrayList;

/**
 * Created by Ho on 5/22/2015.
 */
public class DeleteMediaAsyncTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog mDialog;
    private ArrayList<MediaItem> mListItemsDeleteImported;
    private Context mContext;

    public DeleteMediaAsyncTask(ArrayList<MediaItem> list, Context context) {
        super();
        this.mListItemsDeleteImported = list;
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (MediaItem item : mListItemsDeleteImported) {
            Uri uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(item.getId()));
            mContext.getContentResolver().delete(uri, null,
                    null);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setMessage(mContext
                .getString(R.string.deleting));
        mDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Toast.makeText(mContext, mContext.
                getString(R.string.deleted_success), Toast.LENGTH_SHORT)
                .show();
        try {
            if ((mDialog != null) && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
