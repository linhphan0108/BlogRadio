package com.linhphan.androidboilerplate.api;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.linhphan.androidboilerplate.api.Parser.IParser;
import com.linhphan.androidboilerplate.callback.DownloadCallback;
import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.androidboilerplate.util.NetworkUtil;
import com.linhphan.androidboilerplate.util.NoInternetConnectionException;
import com.linhphan.blogradio.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by linhphan on 11/17/15.
 */
public class BaseDownloadWorker extends AsyncTask<String, Integer, Object> {
    protected final WeakReference<Context> mContext;

    protected Method mType = Method.GET;//the method of request whether POST or GET, default value is GET
    protected Map<String, String> mParams;
    protected IParser mParser;

    private DownloadCallback mCallback;
    private int mRequestCode = DownloadCallback.UNKNOWN_REQUEST_CODE;

    //progress dialog
    protected ProgressDialog mProgressbar;

    //exception
    protected Exception mException;

    /**
     * constructs an AsyncTask download worker. this will initialize a progress bar dialog with a STYLE_SPINNER if isShowDialog is set true
     * @param isShowDialog if this argument is set true, then a dialog will be showed when this download worker is working.
     * @param mCallback a callback which do something after the download worker is finish or error.
     */
    public BaseDownloadWorker(Context context, boolean isShowDialog, DownloadCallback mCallback) {
        this.mContext = new WeakReference<>(context);
        this.mCallback = mCallback;

        if (isShowDialog && context != null) {
            this.mProgressbar = new ProgressDialog(this.mContext.get());
            this.mProgressbar.setMessage("Please! wait a minute");
            mProgressbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressbar.setCancelable(false);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (!NetworkUtil.isNetworkConnected(mContext.get())) {//determine whether internet connection is available
            this.mException = new NoInternetConnectionException();
            return;
        }

        if (mContext.get() != null && mProgressbar != null) {
            mProgressbar.show();
        }
    }

    @Override
    protected Object doInBackground(String... params) {
        return null;
    }


    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (mException == null)
            mCallback.onDownloadSuccessfully(o, mRequestCode);
        else {
            mCallback.onDownloadFailed(mException, mRequestCode);
        }
        if (mProgressbar != null && mProgressbar.isShowing())
            mProgressbar.dismiss();

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mProgressbar != null && mProgressbar.isShowing())
            mProgressbar.setProgress(values[0]);
    }

    public BaseDownloadWorker setRequestCode(int requestCode){
        this.mRequestCode = requestCode;
        return this;
    }

    public BaseDownloadWorker setType(Method type) {
        this.mType = type;
        return this;
    }

    public BaseDownloadWorker setParams(Map<String, String> params) {
        this.mParams = params;
        return this;
    }

    public BaseDownloadWorker setParser(IParser jsonParser) {
        mParser = jsonParser;
        return this;
    }

    public BaseDownloadWorker setDialogCancelCallback(String buttonName, DialogInterface.OnClickListener callback) {
        if (mProgressbar != null) {
            mProgressbar.setButton(DialogInterface.BUTTON_NEGATIVE, buttonName, callback);
        }
        return this;
    }

    public BaseDownloadWorker setDialogTitle(String title) {
        if (mProgressbar != null) {
            if (title != null && !title.isEmpty()) {
                mProgressbar.setTitle(title);
            }
        }
        return this;
    }

    /**
     * set a message to the dialog, if
     */
    public BaseDownloadWorker setDialogMessage(String message) {
        if (mProgressbar != null) {
            if (message != null && !message.isEmpty()) {
                mProgressbar.setMessage(message);
            }
        }
        return this;
    }

    /**
     * setup the horizontal progressbar which will be showed on screen
     * @return JsonDownloadWorker object
     */
    public BaseDownloadWorker setHorizontalProgressbar() {
        mProgressbar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressbar.setMax(100);
        mProgressbar.setProgress(0);

        return this;
    }

    /**
     * try to retrieve data from remote server
     *
     * @return data from server which is presented by a string
     * @throws IOException
     */
    protected String sendGet(String path) throws IOException {
        String query = null;
        if (mParams != null)
            query = encodeQueryString(mParams);
        if (query != null && !query.isEmpty())
            query = "?" + query;
        URL url = new URL(path + query);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        int responseCode = httpURLConnection.getResponseCode();
        Logger.i(getClass().getName(), "sending POST  request to URL: " + path);
        Logger.i(getClass().getName(), "post parameters: " + query);
        Logger.i(getClass().getName(), "response code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            Logger.e(getClass().getName(), "connection is failed");
            return null;
        }

        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
        InputStreamReader inputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String result = readBuffer(bufferedReader);

        bufferedReader.close();
        inputStreamReader.close();
        in.close();
        httpURLConnection.disconnect();
        return result;
    }

    /**
     * try to retrieve data from remote server
     * dump server: http://www.posttestserver.com/
     *
     * @return data from server which is presented by a string
     * @throws IOException
     */
    protected String sendPost(String path, Map<String, String> params) throws IOException {
        URL url = new URL(path);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        String query = encodeQueryString(params);


        //== add header
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(query.getBytes().length));

        //== set post request
        httpURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeBytes(query);
        dataOutputStream.flush();
        dataOutputStream.close();

        int responseCode = httpURLConnection.getResponseCode();
        Logger.i(getClass().getName(), "sending POST  request to URL: " + path);
        Logger.i(getClass().getName(), "post parameters: " + query);
        Logger.i(getClass().getName(), "response code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            Logger.e(getClass().getName(), "connection is failed");
            return null;
        }

        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
        InputStreamReader inputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String result = readBuffer(bufferedReader);

        //== close streams
        bufferedReader.close();
        inputStreamReader.close();
        in.close();
        httpURLConnection.disconnect();
        return result;
    }

    /**
     * read data from buffer
     *
     * @return data which is presented by a string
     * @throws IOException
     */
    private String readBuffer(BufferedReader reader) throws IOException {
        if (reader == null) return null;
        StringBuilder builder = new StringBuilder();
        int tamp;
        while ((tamp = reader.read()) != -1) {
            builder.append((char) tamp);
        }
        return builder.toString();
    }

    private String encodeQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        final char PARAMETER_DELIMITER = '&';
        final char PARAMETER_EQUALS_CHAR = '=';

        StringBuilder builder = new StringBuilder();
        if (params != null) {
            boolean firstParameter = true;
            for (String key : params.keySet()) {
                if (!firstParameter) {
                    builder.append(PARAMETER_DELIMITER);
                }
                builder.append(key)
                        .append(PARAMETER_EQUALS_CHAR)
                        .append(URLEncoder.encode(params.get(key), "UTF-8"));

                if (firstParameter)
                    firstParameter = false;
            }
        }

        return builder.toString();
    }

    /**
     * show notification progress on notification bar. this will show the progress of downloading.
     * @param contentText the message will be showed in the notification
     * @param percent the percent of downloading progress
     */
    protected void showNotificationProgress(Context context, String contentText, int percent){
        int notId = 898989;
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText)
                .setProgress(100, percent, false)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, notification);
    }

    protected String getTag() {
        return getClass().getName();
    }
}
