package com.linhphan.androidboilerplate.api;

import android.content.Context;

import com.linhphan.androidboilerplate.callback.DownloadCallback;
import com.linhphan.androidboilerplate.util.Logger;

import java.io.IOException;

/**
 * Created by linhphan on 11/12/15.
 */
public class JsonDownloadWorker extends BaseDownloadWorker {

    public JsonDownloadWorker(Context mContext, boolean isShowDialog, DownloadCallback mCallback) {
        super(mContext, isShowDialog, mCallback);
    }

    @Override
    protected Object doInBackground(String... params) {
        if (mException != null)
            return null;

        String url = params[0];
        String data;
        try {
            if (mType == Method.POST) {
                data = sendPost(url, mParams);
            } else {
                data = sendGet(url);
            }
            Logger.i(getClass().getName(), "got data from server: " + data);
            if (mParser != null)
                return mParser.parse(data);
            else
                return data;
        } catch (IOException e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }
}
