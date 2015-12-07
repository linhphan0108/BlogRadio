package com.linhphan.androidboilerplate.callback;

/**
 * Created by linhphan on 11/12/15.
 */
public interface DownloadCallback {
    void onDownloadSuccessfully(Object data);
    void onDownloadFailed(Exception e);
}
