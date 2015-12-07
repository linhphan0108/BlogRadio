package com.linhphan.androidboilerplate.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.linhphan.androidboilerplate.ui.activity.BaseActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by linhphan on 11/13/15.
 */
public class BaseFragment extends Fragment {

    protected Handler mBaseHandler;//this mHandler will be gotten from an activity which this fragment is attached

    public static BaseFragment newInstance(Class<?> c, Bundle bundle){
        BaseFragment baseFragment = null;
        try {
            Constructor<?> constructor = c.getConstructors()[0];
            baseFragment = (BaseFragment) constructor.newInstance();
            if (bundle != null){
                baseFragment.setArguments(bundle);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return baseFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseActivity baseActivity = (BaseActivity) context;
        mBaseHandler = baseActivity.getBaseHandler();
    }
}
