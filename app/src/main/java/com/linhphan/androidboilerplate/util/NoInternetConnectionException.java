package com.linhphan.androidboilerplate.util;

/**
 * Created by linhphan on 11/23/15.
 */
public class NoInternetConnectionException extends Exception {
    @Override
    public String getMessage() {
        return "No internet connection available";
    }
}