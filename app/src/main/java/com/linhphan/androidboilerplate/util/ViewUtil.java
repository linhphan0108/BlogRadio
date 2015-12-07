package com.linhphan.androidboilerplate.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by linhphan on 11/11/15.
 */
public class ViewUtil {
    /**
     * convert pixel unit to dp unit
     * @param px the width in pixel
     * @return the width in dp
     */
    public static float convertPx2Dp(float px){
        float densityDpi = Resources.getSystem().getDisplayMetrics().density;
        return px /(densityDpi /160f);
    }

    /**
     * convert pixel unit to dp unit
     * @param dp the width in dp
     * @return the width in pixel
     */
    public static float convertDp2Px(float dp){
        float density = Resources.getSystem().getDisplayMetrics().density;
        return dp * density;
    }

    public static void hideKeyBoard(Activity activity){
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public void showSoftKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
}
