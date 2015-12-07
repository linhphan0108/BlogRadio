package com.linhphan.androidboilerplate.api;

import android.content.Context;

import com.linhphan.androidboilerplate.callback.DownloadCallback;
import com.linhphan.androidboilerplate.util.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by linhphan on 11/17/15.
 */
public class JSoupDownloadWorker extends BaseDownloadWorker {

    public JSoupDownloadWorker(Context mContext, boolean isShowDialog, DownloadCallback mCallback) {
        super(mContext, isShowDialog, mCallback);
    }

    @Override
    protected Object doInBackground(String... params) {
        if (mException != null)
            return null;

        if (params.length <= 0) return null;

        String url = params[0];
        if (url == null || url.isEmpty()) return null;

        Logger.d(getTag(), "begin getting song list from " + url);
        Object result = null;
        try {
            Document document = Jsoup.connect(url).get();
            result = mParser.parse(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
