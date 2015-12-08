package com.linhphan.androidboilerplate.callback;

/**
 * Created by linhphan on 11/12/15.
 */
public interface DownloadCallback {
    int UNKNOWN_REQUEST_CODE = 1000;
    int GET_DIRECT_URL_TO_DOWNLOAD_REQUEST_CODE = 1111;
    int DOWNLOAD_FILE_REQUEST_CODE = 1114;
    int GET_BLOG_LIST_REQUEST_CODE = 1112;
    int GET_MORE_BLOG_LIST_REQUEST_CODE = 1115;
    int GET_DIRECT_BLOG_REQUEST_CODE = 1113;

    void onDownloadSuccessfully(Object data, int requestCode);
    void onDownloadFailed(Exception e, int requestCode);
}
