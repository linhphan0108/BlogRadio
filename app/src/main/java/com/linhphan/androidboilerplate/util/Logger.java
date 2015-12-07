package com.linhphan.androidboilerplate.util;

import android.util.Log;

import com.linhphan.blogradio.BuildConfig;


/**
 * Created by linhphan on 11/11/15.
 */
public class Logger {

    public static void i(String tag, String msg){
        if (BuildConfig.DEBUG){
            Log.i(tag, msg);
        }
    }


    public static void w(String tag, String msg){
        if (BuildConfig.DEBUG){
            Log.w(tag, msg);
        }
    }


    public static void d(String tag, String msg){
        if (BuildConfig.DEBUG){
            Log.d(tag, msg);
        }
    }


    public static void e(String tag, String msg){
        if (BuildConfig.DEBUG){
            Log.e(tag, msg);
        }
    }


    public static void e(String tag, String msg, Throwable throwable){
        if (BuildConfig.DEBUG){
            Log.e(tag, msg, throwable);
        }
    }

    public static void e(Exception e){
        e.printStackTrace();
    }
}
